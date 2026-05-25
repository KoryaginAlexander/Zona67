package com.retailstore.data.repository

import com.retailstore.data.remote.api.OrderApi
import com.retailstore.data.remote.dto.OrderDto
import com.retailstore.data.remote.dto.PlaceOrderRequest
import com.retailstore.data.remote.dto.UpdateStatusRequest
import com.retailstore.domain.model.Order
import com.retailstore.domain.model.OrderItem
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.OrderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderApi: OrderApi
) : OrderRepository {

    override suspend fun placeOrder(deliveryAddress: String?, comment: String?): Result<Order> = try {
        val response = orderApi.placeOrder(PlaceOrderRequest(deliveryAddress, comment))
        if (response.isSuccessful) {
            Result.Success(response.body()!!.toDomain())
        } else {
            Result.Error(response.code(), "Failed to place order")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun getMyOrders(): Result<List<Order>> = try {
        val response = orderApi.getMyOrders()
        if (response.isSuccessful) Result.Success(response.body()!!.map { it.toDomain() })
        else Result.Error(response.code(), "Failed to load orders")
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun getMyOrderById(id: String): Result<Order> = try {
        val response = orderApi.getMyOrderById(id)
        if (response.isSuccessful) Result.Success(response.body()!!.toDomain())
        else Result.Error(response.code(), "Order not found")
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun getAllOrders(status: String?): Result<List<Order>> = try {
        val response = orderApi.getAllOrders(status)
        if (response.isSuccessful) Result.Success(response.body()!!.map { it.toDomain() })
        else Result.Error(response.code(), "Failed to load orders")
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun getOrderById(id: String): Result<Order> = try {
        val response = orderApi.getOrderById(id)
        if (response.isSuccessful) Result.Success(response.body()!!.toDomain())
        else Result.Error(response.code(), "Order not found")
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun updateOrderStatus(id: String, status: String): Result<Order> = try {
        val response = orderApi.updateOrderStatus(id, UpdateStatusRequest(status))
        if (response.isSuccessful) Result.Success(response.body()!!.toDomain())
        else Result.Error(response.code(), "Failed to update status")
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun cancelOrder(id: String): Result<Order> = try {
        val response = orderApi.cancelOrder(id)
        if (response.isSuccessful) Result.Success(response.body()!!.toDomain())
        else Result.Error(response.code(), "Failed to cancel order")
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }
}

private fun OrderDto.toDomain() = Order(
    id = id, userId = userId, userEmail = userEmail ?: "", status = status, totalAmount = totalAmount,
    deliveryAddress = deliveryAddress, comment = comment,
    items = items.map { OrderItem(it.id, it.productId, it.productName, it.productPrice, it.quantity) },
    createdAt = createdAt, updatedAt = updatedAt
)
