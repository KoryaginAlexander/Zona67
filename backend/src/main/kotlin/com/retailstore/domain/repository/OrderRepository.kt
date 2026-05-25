package com.retailstore.domain.repository

import com.retailstore.domain.model.Order
import java.util.UUID

interface OrderRepository {
    suspend fun createOrder(userId: UUID, deliveryAddress: String?, comment: String?): Order
    suspend fun getOrdersByUser(userId: UUID): List<Order>
    suspend fun getOrderById(id: UUID): Order?
    suspend fun getAllOrders(status: String?): List<Order>
    suspend fun updateStatus(id: UUID, status: String): Order?
    suspend fun cancelOrder(id: UUID): Order?
}
