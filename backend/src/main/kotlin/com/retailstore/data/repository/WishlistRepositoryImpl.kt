package com.retailstore.data.repository

import com.retailstore.data.database.tables.ProductsTable
import com.retailstore.data.database.tables.WishlistItemsTable
import com.retailstore.domain.model.WishlistItem
import com.retailstore.domain.model.WishlistProduct
import com.retailstore.domain.repository.WishlistRepository
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

class WishlistRepositoryImpl : WishlistRepository {

    private fun parseImageUrls(raw: String?): List<String> =
        if (raw.isNullOrBlank()) emptyList()
        else try { Json.decodeFromString(raw) } catch (_: Exception) { emptyList() }

    private fun ResultRow.toWishlistItem() = WishlistItem(
        id = this[WishlistItemsTable.id],
        userId = this[WishlistItemsTable.userId],
        productId = this[WishlistItemsTable.productId],
        product = WishlistProduct(
            id = this[ProductsTable.id],
            name = this[ProductsTable.name],
            price = this[ProductsTable.price],
            imageUrl = parseImageUrls(this[ProductsTable.imageUrls]).firstOrNull(),
            stock = this[ProductsTable.stock]
        )
    )

    override suspend fun getWishlist(userId: UUID): List<WishlistItem> =
        newSuspendedTransaction {
            (WishlistItemsTable innerJoin ProductsTable)
                .select { WishlistItemsTable.userId eq userId }
                .map { it.toWishlistItem() }
        }

    override suspend fun addItem(userId: UUID, productId: UUID): WishlistItem =
        newSuspendedTransaction {
            val existing = WishlistItemsTable.select {
                (WishlistItemsTable.userId eq userId) and (WishlistItemsTable.productId eq productId)
            }.singleOrNull()

            if (existing == null) {
                WishlistItemsTable.insert {
                    it[WishlistItemsTable.id] = UUID.randomUUID()
                    it[WishlistItemsTable.userId] = userId
                    it[WishlistItemsTable.productId] = productId
                    it[WishlistItemsTable.createdAt] = LocalDateTime.now()
                }
            }

            (WishlistItemsTable innerJoin ProductsTable)
                .select { (WishlistItemsTable.userId eq userId) and (WishlistItemsTable.productId eq productId) }
                .single().toWishlistItem()
        }

    override suspend fun removeItem(userId: UUID, productId: UUID): Boolean =
        newSuspendedTransaction {
            WishlistItemsTable.deleteWhere {
                (WishlistItemsTable.userId eq userId) and (WishlistItemsTable.productId eq productId)
            } > 0
        }
}
