package com.retailstore.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlaceOrderRequest(
    val deliveryAddress: String? = null,
    val comment: String? = null
)

@Serializable
data class UpdateStatusRequest(val status: String)

@Serializable
data class OrderItemResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val quantity: Int
)

@Serializable
data class OrderResponse(
    val id: String,
    val userId: String,
    val userEmail: String = "",
    val status: String,
    val totalAmount: Double,
    val deliveryAddress: String? = null,
    val comment: String? = null,
    val items: List<OrderItemResponse> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)
