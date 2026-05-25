package com.retailstore.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class WishlistItemResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String? = null,
    val stock: Int
)
