package com.retailstore.data.remote.api

import com.retailstore.data.remote.dto.WishlistItemDto
import retrofit2.Response
import retrofit2.http.*

interface WishlistApi {
    @GET("wishlist")
    suspend fun getWishlist(): Response<List<WishlistItemDto>>

    @POST("wishlist/{productId}")
    suspend fun addToWishlist(@Path("productId") productId: String): Response<WishlistItemDto>

    @DELETE("wishlist/{productId}")
    suspend fun removeFromWishlist(@Path("productId") productId: String): Response<Unit>
}
