package com.retailstore.domain.repository

import com.retailstore.domain.model.Order
import com.retailstore.domain.model.Result

interface OrderRepository {
    suspend fun placeOrder(deliveryAddress: String?, comment: String?): Result<Order>
    suspend fun getMyOrders(): Result<List<Order>>
    suspend fun getMyOrderById(id: String): Result<Order>
    suspend fun getAllOrders(status: String? = null): Result<List<Order>>
    suspend fun getOrderById(id: String): Result<Order>
    suspend fun updateOrderStatus(id: String, status: String): Result<Order>
    suspend fun cancelOrder(id: String): Result<Order>
}
