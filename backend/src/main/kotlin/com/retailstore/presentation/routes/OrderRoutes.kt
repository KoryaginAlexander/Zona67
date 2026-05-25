package com.retailstore.presentation.routes

import com.retailstore.domain.repository.OrderRepository
import com.retailstore.presentation.dto.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.orderRoutes() {
    val orderRepository by inject<OrderRepository>()

    authenticate("auth-jwt") {
        route("/orders") {
            post {
                val userId = call.userId()
                val req = call.receive<PlaceOrderRequest>()
                try {
                    val order = orderRepository.createOrder(userId, req.deliveryAddress, req.comment)
                    call.respond(HttpStatusCode.Created, order.toResponse())
                } catch (e: Exception) {
                    val message = e.message ?: "Unknown error"
                    if (message.startsWith("OUT_OF_STOCK:")) {
                        val productName = message.removePrefix("OUT_OF_STOCK:")
                        call.respond(
                            HttpStatusCode.Conflict,
                            ErrorResponse("OUT_OF_STOCK", "Product '$productName' is not available in the requested quantity", 409)
                        )
                    } else if (message == "Cart is empty") {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("CART_EMPTY", "Cart is empty", 400))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", message, 500))
                    }
                }
            }

            get("/my") {
                val userId = call.userId()
                val orders = orderRepository.getOrdersByUser(userId)
                call.respond(orders.map { it.toResponse() })
            }

            get("/my/{id}") {
                val userId = call.userId()
                val id = UUID.fromString(call.parameters["id"])
                val order = orderRepository.getOrderById(id) ?: run {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Order not found", 404))
                    return@get
                }
                if (order.userId != userId) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Access denied", 403))
                    return@get
                }
                call.respond(order.toResponse())
            }

            get {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@get
                }
                val status = call.request.queryParameters["status"]
                val orders = orderRepository.getAllOrders(status)
                call.respond(orders.map { it.toResponse() })
            }

            get("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@get
                }
                val id = UUID.fromString(call.parameters["id"])
                val order = orderRepository.getOrderById(id) ?: run {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Order not found", 404))
                    return@get
                }
                call.respond(order.toResponse())
            }

            patch("/{id}/status") {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@patch
                }
                val id = UUID.fromString(call.parameters["id"])
                val req = call.receive<UpdateStatusRequest>()
                try {
                    val order = orderRepository.updateStatus(id, req.status) ?: run {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Order not found", 404))
                        return@patch
                    }
                    call.respond(order.toResponse())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_TRANSITION", e.message ?: "Invalid status transition", 400))
                }
            }

            patch("/{id}/cancel") {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@patch
                }
                val id = UUID.fromString(call.parameters["id"])
                val order = orderRepository.cancelOrder(id) ?: run {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Order not found", 404))
                    return@patch
                }
                call.respond(order.toResponse())
            }
        }
    }
}

private fun ApplicationCall.userId(): UUID {
    val principal = principal<JWTPrincipal>()!!
    return UUID.fromString(principal.payload.getClaim("userId").asString())
}

private fun com.retailstore.domain.model.Order.toResponse() = OrderResponse(
    id = id.toString(),
    userId = userId.toString(),
    userEmail = userEmail,
    status = status,
    totalAmount = totalAmount.toDouble(),
    deliveryAddress = deliveryAddress,
    comment = comment,
    items = items.map { item ->
        OrderItemResponse(
            id = item.id.toString(),
            productId = item.productId.toString(),
            productName = item.productName,
            productPrice = item.productPrice.toDouble(),
            quantity = item.quantity
        )
    },
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)
