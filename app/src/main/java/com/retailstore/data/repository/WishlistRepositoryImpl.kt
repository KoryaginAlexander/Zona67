package com.retailstore.data.repository

import com.retailstore.data.remote.api.WishlistApi
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.WishlistItem
import com.retailstore.domain.repository.WishlistRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepositoryImpl @Inject constructor(
    private val wishlistApi: WishlistApi
) : WishlistRepository {

    override suspend fun getWishlist(): Result<List<WishlistItem>> = try {
        val response = wishlistApi.getWishlist()
        if (response.isSuccessful) {
            Result.Success(response.body()!!.map {
                WishlistItem(it.id, it.productId, it.productName, it.productPrice, it.productImageUrl, it.stock)
            })
        } else {
            Result.Error(response.code(), "Failed to load wishlist")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun addToWishlist(productId: String): Result<WishlistItem> = try {
        val response = wishlistApi.addToWishlist(productId)
        if (response.isSuccessful) {
            val dto = response.body()!!
            Result.Success(WishlistItem(dto.id, dto.productId, dto.productName, dto.productPrice, dto.productImageUrl, dto.stock))
        } else {
            Result.Error(response.code(), "Failed to add to wishlist")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun removeFromWishlist(productId: String): Result<Unit> = try {
        wishlistApi.removeFromWishlist(productId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }
}
