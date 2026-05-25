package com.retailstore.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddCartItemRequest(val productId: String, val quantity: Int = 1)

@Serializable
data class UpdateCartItemRequest(val quantity: Int)

@Serializable
data class MergeCartRequest(val items: List<MergeItem>)

@Serializable
data class MergeItem(val productId: String, val quantity: Int)

@Serializable
data class CartItemResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String? = null,
    val stock: Int,
    val quantity: Int
)

@Serializable
data class CartResponse(
    val items: List<CartItemResponse>,
    val total: Double
)
