package com.retailstore.domain.model

data class Order(
    val id: String,
    val userId: String,
    val userEmail: String = "",
    val status: String,
    val totalAmount: Double,
    val deliveryAddress: String?,
    val comment: String?,
    val items: List<OrderItem>,
    val createdAt: String,
    val updatedAt: String
)

data class OrderItem(
    val id: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val quantity: Int
)
