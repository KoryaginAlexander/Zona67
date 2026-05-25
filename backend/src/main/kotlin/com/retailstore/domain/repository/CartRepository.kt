package com.retailstore.domain.repository

import com.retailstore.domain.model.CartItem
import java.util.UUID

interface CartRepository {
    suspend fun getCart(userId: UUID): List<CartItem>
    suspend fun addItem(userId: UUID, productId: UUID, quantity: Int): CartItem
    suspend fun updateItem(userId: UUID, productId: UUID, quantity: Int): CartItem?
    suspend fun removeItem(userId: UUID, productId: UUID): Boolean
    suspend fun clearCart(userId: UUID)
    suspend fun mergeGuestCart(userId: UUID, items: List<Pair<UUID, Int>>)
}
