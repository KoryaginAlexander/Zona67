package com.retailstore.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AddCartItemRequest(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int = 1
)

data class UpdateCartItemRequest(
    @SerializedName("quantity") val quantity: Int
)

data class MergeCartRequest(
    @SerializedName("items") val items: List<MergeItemDto>
)

data class MergeItemDto(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int
)

data class CartItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("productPrice") val productPrice: Double,
    @SerializedName("productImageUrl") val productImageUrl: String?,
    @SerializedName("stock") val stock: Int,
    @SerializedName("quantity") val quantity: Int
)

data class CartDto(
    @SerializedName("items") val items: List<CartItemDto>,
    @SerializedName("total") val total: Double
)
