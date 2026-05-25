package com.retailstore.domain.repository

import com.retailstore.domain.model.Result

data class WishlistItem(
    val id: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String?,
    val stock: Int
)

interface WishlistRepository {
    suspend fun getWishlist(): Result<List<WishlistItem>>
    suspend fun addToWishlist(productId: String): Result<WishlistItem>
    suspend fun removeFromWishlist(productId: String): Result<Unit>
}
