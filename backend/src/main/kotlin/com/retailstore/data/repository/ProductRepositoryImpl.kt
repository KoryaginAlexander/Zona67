package com.retailstore.data.repository

import com.retailstore.data.database.tables.CartItemsTable
import com.retailstore.data.database.tables.OrderItemsTable
import com.retailstore.data.database.tables.ProductSpecsTable
import com.retailstore.data.database.tables.ProductsTable
import com.retailstore.data.database.tables.ReviewsTable
import com.retailstore.data.database.tables.WishlistItemsTable
import com.retailstore.domain.model.Product
import com.retailstore.domain.model.ProductSpec
import com.retailstore.domain.repository.ProductFilter
import com.retailstore.domain.repository.ProductRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class ProductRepositoryImpl : ProductRepository {

    private fun parseImageUrls(raw: String?): List<String> =
        if (raw.isNullOrBlank()) emptyList()
        else try { Json.decodeFromString(raw) } catch (_: Exception) { emptyList() }

    private fun ResultRow.toProduct(specs: List<ProductSpec>, avgRating: Double = 0.0, reviewCount: Int = 0) = Product(
        id = this[ProductsTable.id],
        categoryId = this[ProductsTable.categoryId],
        name = this[ProductsTable.name],
        description = this[ProductsTable.description],
        price = this[ProductsTable.price],
        stock = this[ProductsTable.stock],
        brand = this[ProductsTable.brand],
        model = this[ProductsTable.model],
        imageUrls = parseImageUrls(this[ProductsTable.imageUrls]),
        isActive = this[ProductsTable.isActive],
        specs = specs,
        averageRating = avgRating,
        reviewCount = reviewCount,
        createdAt = this[ProductsTable.createdAt],
        updatedAt = this[ProductsTable.updatedAt]
    )

    private fun loadRatingsMap(productIds: List<UUID>): Map<UUID, Pair<Double, Int>> {
        if (productIds.isEmpty()) return emptyMap()
        val avgExpr = ReviewsTable.rating.avg()
        val countExpr = ReviewsTable.id.count()
        return ReviewsTable
            .slice(ReviewsTable.productId, avgExpr, countExpr)
            .select { ReviewsTable.productId inList productIds }
            .groupBy(ReviewsTable.productId)
            .associate { it[ReviewsTable.productId] to ((it[avgExpr]?.toDouble() ?: 0.0) to it[countExpr].toInt()) }
    }

    private fun loadSpecs(productId: UUID): List<ProductSpec> =
        ProductSpecsTable.select { ProductSpecsTable.productId eq productId }
            .map { ProductSpec(it[ProductSpecsTable.specKey], it[ProductSpecsTable.specValue]) }

    override suspend fun findAll(filter: ProductFilter): Pair<List<Product>, Int> =
        newSuspendedTransaction {
            var query = ProductsTable.selectAll()

            if (filter.activeOnly) query = query.andWhere { ProductsTable.isActive eq true }
            filter.categoryId?.let { query = query.andWhere { ProductsTable.categoryId eq it } }
            filter.brand?.let { query = query.andWhere { ProductsTable.brand.lowerCase() eq it.lowercase() } }
            filter.minPrice?.let { query = query.andWhere { ProductsTable.price greaterEq it } }
            filter.maxPrice?.let { query = query.andWhere { ProductsTable.price lessEq it } }
            filter.search?.let { s ->
                query = query.andWhere {
                    (ProductsTable.name.lowerCase() like "%${s.lowercase()}%") or
                            (ProductsTable.brand.lowerCase() like "%${s.lowercase()}%")
                }
            }

            val total = query.count().toInt()

            query = when (filter.sortBy) {
                "price_asc" -> query.orderBy(ProductsTable.price to SortOrder.ASC)
                "price_desc" -> query.orderBy(ProductsTable.price to SortOrder.DESC)
                else -> query.orderBy(ProductsTable.createdAt to SortOrder.DESC)
            }

            val offset = ((filter.page - 1) * filter.limit).toLong()
            val rows = query.limit(filter.limit, offset).toList()
            val productIds = rows.map { it[ProductsTable.id] }
            val ratingsMap = loadRatingsMap(productIds)
            val products = rows.map { row ->
                val id = row[ProductsTable.id]
                val specs = loadSpecs(id)
                val (avg, cnt) = ratingsMap[id] ?: (0.0 to 0)
                row.toProduct(specs, avg, cnt)
            }
            products to total
        }

    override suspend fun findById(id: UUID): Product? =
        newSuspendedTransaction {
            ProductsTable.select { ProductsTable.id eq id }.singleOrNull()?.let { row ->
                val specs = loadSpecs(id)
                val (avg, cnt) = loadRatingsMap(listOf(id))[id] ?: (0.0 to 0)
                row.toProduct(specs, avg, cnt)
            }
        }

    override suspend fun create(
        categoryId: Int, name: String, description: String?, price: BigDecimal,
        stock: Int, brand: String?, model: String?, imageUrls: List<String>,
        specs: List<Pair<String, String>>
    ): Product = newSuspendedTransaction {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()
        ProductsTable.insert {
            it[ProductsTable.id] = id
            it[ProductsTable.categoryId] = categoryId
            it[ProductsTable.name] = name
            it[ProductsTable.description] = description
            it[ProductsTable.price] = price
            it[ProductsTable.stock] = stock
            it[ProductsTable.brand] = brand
            it[ProductsTable.model] = model
            it[ProductsTable.imageUrls] = Json.encodeToString(imageUrls)
            it[ProductsTable.isActive] = true
            it[ProductsTable.createdAt] = now
            it[ProductsTable.updatedAt] = now
        }
        specs.forEach { (key, value) ->
            ProductSpecsTable.insert {
                it[ProductSpecsTable.productId] = id
                it[ProductSpecsTable.specKey] = key
                it[ProductSpecsTable.specValue] = value
            }
        }
        val row = ProductsTable.select { ProductsTable.id eq id }.single()
        row.toProduct(specs.map { ProductSpec(it.first, it.second) })
    }

    override suspend fun update(
        id: UUID, categoryId: Int?, name: String?, description: String?,
        price: BigDecimal?, stock: Int?, brand: String?, model: String?,
        imageUrls: List<String>?, specs: List<Pair<String, String>>?
    ): Product? = newSuspendedTransaction {
        val updated = ProductsTable.update({ ProductsTable.id eq id }) {
            if (categoryId != null) it[ProductsTable.categoryId] = categoryId
            if (name != null) it[ProductsTable.name] = name
            if (description != null) it[ProductsTable.description] = description
            if (price != null) it[ProductsTable.price] = price
            if (stock != null) it[ProductsTable.stock] = stock
            if (brand != null) it[ProductsTable.brand] = brand
            if (model != null) it[ProductsTable.model] = model
            if (imageUrls != null) it[ProductsTable.imageUrls] = Json.encodeToString(imageUrls)
            it[ProductsTable.updatedAt] = LocalDateTime.now()
        }
        if (updated == 0) return@newSuspendedTransaction null

        if (specs != null) {
            ProductSpecsTable.deleteWhere { ProductSpecsTable.productId eq id }
            specs.forEach { (key, value) ->
                ProductSpecsTable.insert {
                    it[ProductSpecsTable.productId] = id
                    it[ProductSpecsTable.specKey] = key
                    it[ProductSpecsTable.specValue] = value
                }
            }
        }
        val row = ProductsTable.select { ProductsTable.id eq id }.singleOrNull() ?: return@newSuspendedTransaction null
        row.toProduct(loadSpecs(id))
    }

    override suspend fun setActive(id: UUID, active: Boolean): Boolean =
        newSuspendedTransaction {
            ProductsTable.update({ ProductsTable.id eq id }) {
                it[ProductsTable.isActive] = active
                it[ProductsTable.updatedAt] = LocalDateTime.now()
            } > 0
        }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        val hasOrders = OrderItemsTable.select { OrderItemsTable.productId eq id }.count() > 0
        if (hasOrders) throw IllegalStateException("Товар присутствует в заказах и не может быть удалён")
        ReviewsTable.deleteWhere { ReviewsTable.productId eq id }
        WishlistItemsTable.deleteWhere { WishlistItemsTable.productId eq id }
        CartItemsTable.deleteWhere { CartItemsTable.productId eq id }
        ProductSpecsTable.deleteWhere { ProductSpecsTable.productId eq id }
        ProductsTable.deleteWhere { ProductsTable.id eq id } > 0
    }
}
