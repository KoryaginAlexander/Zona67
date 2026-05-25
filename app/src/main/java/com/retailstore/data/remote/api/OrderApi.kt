package com.retailstore.data.remote.api

import com.retailstore.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    @POST("orders")
    suspend fun placeOrder(@Body request: PlaceOrderRequest): Response<OrderDto>

    @GET("orders/my")
    suspend fun getMyOrders(): Response<List<OrderDto>>

    @GET("orders/my/{id}")
    suspend fun getMyOrderById(@Path("id") id: String): Response<OrderDto>

    @GET("orders")
    suspend fun getAllOrders(@Query("status") status: String? = null): Response<List<OrderDto>>

    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") id: String): Response<OrderDto>

    @PATCH("orders/{id}/status")
    suspend fun updateOrderStatus(@Path("id") id: String, @Body request: UpdateStatusRequest): Response<OrderDto>

    @PATCH("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): Response<OrderDto>
}
