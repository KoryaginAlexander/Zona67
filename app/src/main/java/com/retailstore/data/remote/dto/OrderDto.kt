package com.retailstore.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PlaceOrderRequest(
    @SerializedName("deliveryAddress") val deliveryAddress: String?,
    @SerializedName("comment") val comment: String?
)

data class UpdateStatusRequest(
    @SerializedName("status") val status: String
)

data class OrderItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("productPrice") val productPrice: Double,
    @SerializedName("quantity") val quantity: Int
)

data class OrderDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("status") val status: String,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("deliveryAddress") val deliveryAddress: String?,
    @SerializedName("comment") val comment: String?,
    @SerializedName("items") val items: List<OrderItemDto>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class WishlistItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("productPrice") val productPrice: Double,
    @SerializedName("productImageUrl") val productImageUrl: String?,
    @SerializedName("stock") val stock: Int
)
