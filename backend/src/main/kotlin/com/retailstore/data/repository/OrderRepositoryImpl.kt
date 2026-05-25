package com.retailstore.data.repository

import com.retailstore.data.database.tables.*
import com.retailstore.domain.model.Order
import com.retailstore.domain.model.OrderItem
import com.retailstore.domain.model.OrderStatus
import com.retailstore.domain.repository.OrderRepository
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

class OrderRepositoryImpl : OrderRepository {

    private fun parseImageUrls(raw: String?): List<String> =
        if (raw.isNullOrBlank()) emptyList()
        else try { Json.decodeFromString(raw) } catch (_: Exception) { emptyList() }

    private fun loadOrderItems(orderId: UUID): List<OrderItem> =
        OrderItemsTable.select { OrderItemsTable.orderId eq orderId }.map {
            OrderItem(
                id = it[OrderItemsTable.id],
                orderId = it[OrderItemsTable.orderId],
                productId = it[OrderItemsTable.productId],
                productName = it[OrderItemsTable.productName],
                productPrice = it[OrderItemsTable.productPrice],
                quantity = it[OrderItemsTable.quantity]
            )
        }

    private fun ResultRow.toOrder() = Order(
        id = this[OrdersTable.id],
        userId = this[OrdersTable.userId],
        status = this[OrdersTable.status],
        totalAmount = this[OrdersTable.totalAmount],
        deliveryAddress = this[OrdersTable.deliveryAddress],
        comment = this[OrdersTable.comment],
        items = loadOrderItems(this[OrdersTable.id]),
        createdAt = this[OrdersTable.createdAt],
        updatedAt = this[OrdersTable.updatedAt]
    )

    override suspend fun createOrder(userId: UUID, deliveryAddress: String?, comment: String?): Order =
        newSuspendedTransaction {
            val cartItems = (CartItemsTable innerJoin ProductsTable)
                .select { CartItemsTable.userId eq userId }
                .toList()

            if (cartItems.isEmpty()) error("Cart is empty")

            cartItems.forEach { row ->
                val stock = row[ProductsTable.stock]
                val qty = row[CartItemsTable.quantity]
                if (stock < qty) error("OUT_OF_STOCK:${row[ProductsTable.name]}")
            }

            val total = cartItems.sumOf { row ->
                row[ProductsTable.price] * row[CartItemsTable.quantity].toBigDecimal()
            }

            val orderId = UUID.randomUUID()
            val now = LocalDateTime.now()

            OrdersTable.insert {
                it[OrdersTable.id] = orderId
                it[OrdersTable.userId] = userId
                it[OrdersTable.status] = OrderStatus.PENDING
                it[OrdersTable.totalAmount] = total
                it[OrdersTable.deliveryAddress] = deliveryAddress
                it[OrdersTable.comment] = comment
                it[OrdersTable.createdAt] = now
                it[OrdersTable.updatedAt] = now
            }

            cartItems.forEach { row ->
                val productId = row[ProductsTable.id]
                val qty = row[CartItemsTable.quantity]

                OrderItemsTable.insert {
                    it[OrderItemsTable.id] = UUID.randomUUID()
                    it[OrderItemsTable.orderId] = orderId
                    it[OrderItemsTable.productId] = productId
                    it[OrderItemsTable.productName] = row[ProductsTable.name]
                    it[OrderItemsTable.productPrice] = row[ProductsTable.price]
                    it[OrderItemsTable.quantity] = qty
                }

                ProductsTable.update({ ProductsTable.id eq productId }) {
                    it[ProductsTable.stock] = row[ProductsTable.stock] - qty
                    it[ProductsTable.updatedAt] = now
                }
            }

            CartItemsTable.deleteWhere { CartItemsTable.userId eq userId }

            OrdersTable.select { OrdersTable.id eq orderId }.single().toOrder()
        }

    override suspend fun getOrdersByUser(userId: UUID): List<Order> =
        newSuspendedTransaction {
            OrdersTable.select { OrdersTable.userId eq userId }
                .orderBy(OrdersTable.createdAt to SortOrder.DESC)
                .map { it.toOrder() }
        }

    override suspend fun getOrderById(id: UUID): Order? =
        newSuspendedTransaction {
            OrdersTable.select { OrdersTable.id eq id }.singleOrNull()?.toOrder()
        }

    override suspend fun getAllOrders(status: String?): List<Order> =
        newSuspendedTransaction {
            val query = if (status != null)
                OrdersTable.select { OrdersTable.status eq status }
            else
                OrdersTable.selectAll()
            query.orderBy(OrdersTable.createdAt to SortOrder.DESC).map { it.toOrder() }
        }

    override suspend fun updateStatus(id: UUID, status: String): Order? =
        newSuspendedTransaction {
            val order = OrdersTable.select { OrdersTable.id eq id }.singleOrNull()
                ?: return@newSuspendedTransaction null

            val currentStatus = order[OrdersTable.status]
            val allowed = OrderStatus.allowedTransitions(currentStatus)
            if (status !in allowed) error("INVALID_TRANSITION:$currentStatus->$status")

            OrdersTable.update({ OrdersTable.id eq id }) {
                it[OrdersTable.status] = status
                it[OrdersTable.updatedAt] = LocalDateTime.now()
            }

            OrdersTable.select { OrdersTable.id eq id }.single().toOrder()
        }

    override suspend fun cancelOrder(id: UUID): Order? =
        newSuspendedTransaction {
            val order = OrdersTable.select { OrdersTable.id eq id }.singleOrNull()
                ?: return@newSuspendedTransaction null

            if (order[OrdersTable.status] == OrderStatus.CANCELLED) return@newSuspendedTransaction order.toOrder()

            val items = OrderItemsTable.select { OrderItemsTable.orderId eq id }.toList()
            items.forEach { itemRow ->
                val productId = itemRow[OrderItemsTable.productId]
                val qty = itemRow[OrderItemsTable.quantity]
                ProductsTable.update({ ProductsTable.id eq productId }) {
                    with(SqlExpressionBuilder) {
                        it.update(ProductsTable.stock, ProductsTable.stock + qty)
                    }
                    it[ProductsTable.updatedAt] = LocalDateTime.now()
                }
            }

            OrdersTable.update({ OrdersTable.id eq id }) {
                it[OrdersTable.status] = OrderStatus.CANCELLED
                it[OrdersTable.updatedAt] = LocalDateTime.now()
            }

            OrdersTable.select { OrdersTable.id eq id }.single().toOrder()
        }
}
