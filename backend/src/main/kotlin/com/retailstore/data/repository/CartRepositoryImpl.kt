package com.retailstore.data.repository

import com.retailstore.data.database.tables.CartItemsTable
import com.retailstore.data.database.tables.ProductsTable
import com.retailstore.domain.model.CartItem
import com.retailstore.domain.model.CartProduct
import com.retailstore.domain.repository.CartRepository
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

class CartRepositoryImpl : CartRepository {

    private fun parseImageUrls(raw: String?): List<String> =
        if (raw.isNullOrBlank()) emptyList()
        else try { Json.decodeFromString(raw) } catch (_: Exception) { emptyList() }

    private fun ResultRow.toCartItem(): CartItem {
        val imageUrls = parseImageUrls(this[ProductsTable.imageUrls])
        return CartItem(
            id = this[CartItemsTable.id],
            userId = this[CartItemsTable.userId],
            productId = this[CartItemsTable.productId],
            quantity = this[CartItemsTable.quantity],
            product = CartProduct(
                id = this[ProductsTable.id],
                name = this[ProductsTable.name],
                price = this[ProductsTable.price],
                imageUrl = imageUrls.firstOrNull(),
                stock = this[ProductsTable.stock]
            )
        )
    }

    override suspend fun getCart(userId: UUID): List<CartItem> =
        newSuspendedTransaction {
            (CartItemsTable innerJoin ProductsTable)
                .select { CartItemsTable.userId eq userId }
                .map { it.toCartItem() }
        }

    override suspend fun addItem(userId: UUID, productId: UUID, quantity: Int): CartItem =
        newSuspendedTransaction {
            val existing = CartItemsTable.select {
                (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq productId)
            }.singleOrNull()

            if (existing != null) {
                CartItemsTable.update({
                    (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq productId)
                }) {
                    it[CartItemsTable.quantity] = existing[CartItemsTable.quantity] + quantity
                }
            } else {
                CartItemsTable.insert {
                    it[CartItemsTable.id] = UUID.randomUUID()
                    it[CartItemsTable.userId] = userId
                    it[CartItemsTable.productId] = productId
                    it[CartItemsTable.quantity] = quantity
                    it[CartItemsTable.createdAt] = LocalDateTime.now()
                }
            }

            (CartItemsTable innerJoin ProductsTable)
                .select { (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq productId) }
                .single().toCartItem()
        }

    override suspend fun updateItem(userId: UUID, productId: UUID, quantity: Int): CartItem? =
        newSuspendedTransaction {
            val updated = CartItemsTable.update({
                (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq productId)
            }) {
                it[CartItemsTable.quantity] = quantity
            }
            if (updated == 0) return@newSuspendedTransaction null
            (CartItemsTable innerJoin ProductsTable)
                .select { (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq productId) }
                .singleOrNull()?.toCartItem()
        }

    override suspend fun removeItem(userId: UUID, productId: UUID): Boolean =
        newSuspendedTransaction {
            CartItemsTable.deleteWhere {
                (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq productId)
            } > 0
        }

    override suspend fun clearCart(userId: UUID) =
        newSuspendedTransaction {
            CartItemsTable.deleteWhere { CartItemsTable.userId eq userId }
            Unit
        }

    override suspend fun mergeGuestCart(userId: UUID, items: List<Pair<UUID, Int>>) =
        newSuspendedTransaction {
            items.forEach { (productId, quantity) ->
                val existing = CartItemsTable.select {
                    (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq productId)
                }.singleOrNull()
                if (existing != null) {
                    CartItemsTable.update({
                        (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq productId)
                    }) {
                        it[CartItemsTable.quantity] = quantity
                    }
                } else {
                    CartItemsTable.insert {
                        it[CartItemsTable.id] = UUID.randomUUID()
                        it[CartItemsTable.userId] = userId
                        it[CartItemsTable.productId] = productId
                        it[CartItemsTable.quantity] = quantity
                        it[CartItemsTable.createdAt] = LocalDateTime.now()
                    }
                }
            }
        }
}
