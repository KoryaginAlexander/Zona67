package com.retailstore.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Order(
    val id: UUID,
    val userId: UUID,
    val status: String,
    val totalAmount: BigDecimal,
    val deliveryAddress: String?,
    val comment: String?,
    val items: List<OrderItem>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class OrderItem(
    val id: UUID,
    val orderId: UUID,
    val productId: UUID,
    val productName: String,
    val productPrice: BigDecimal,
    val quantity: Int
)

object OrderStatus {
    const val PENDING = "PENDING"
    const val CONFIRMED = "CONFIRMED"
    const val PROCESSING = "PROCESSING"
    const val SHIPPED = "SHIPPED"
    const val DELIVERED = "DELIVERED"
    const val CANCELLED = "CANCELLED"

    fun allowedTransitions(from: String): List<String> = when (from) {
        PENDING -> listOf(CONFIRMED, CANCELLED)
        CONFIRMED -> listOf(PROCESSING, CANCELLED)
        PROCESSING -> listOf(SHIPPED, CANCELLED)
        SHIPPED -> listOf(DELIVERED, CANCELLED)
        else -> emptyList()
    }
}
