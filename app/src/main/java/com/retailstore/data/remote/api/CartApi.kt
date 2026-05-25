package com.retailstore.data.remote.api

import com.retailstore.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CartApi {
    @GET("cart")
    suspend fun getCart(): Response<CartDto>

    @POST("cart/items")
    suspend fun addItem(@Body request: AddCartItemRequest): Response<CartItemDto>

    @PATCH("cart/items/{productId}")
    suspend fun updateItem(@Path("productId") productId: String, @Body request: UpdateCartItemRequest): Response<CartItemDto>

    @DELETE("cart/items/{productId}")
    suspend fun removeItem(@Path("productId") productId: String): Response<Unit>

    @DELETE("cart")
    suspend fun clearCart(): Response<Unit>

    @POST("cart/merge")
    suspend fun mergeCart(@Body request: MergeCartRequest): Response<CartDto>
}
