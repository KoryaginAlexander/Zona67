package com.retailstore.domain.repository

import com.retailstore.domain.model.WishlistItem
import java.util.UUID

interface WishlistRepository {
    suspend fun getWishlist(userId: UUID): List<WishlistItem>
    suspend fun addItem(userId: UUID, productId: UUID): WishlistItem
    suspend fun removeItem(userId: UUID, productId: UUID): Boolean
}
