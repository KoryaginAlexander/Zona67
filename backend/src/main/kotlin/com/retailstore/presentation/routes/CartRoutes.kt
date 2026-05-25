package com.retailstore.presentation.routes

import com.retailstore.domain.repository.CartRepository
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

fun Route.cartRoutes() {
    val cartRepository by inject<CartRepository>()

    authenticate("auth-jwt") {
        route("/cart") {
            get {
                val userId = call.userId()
                val items = cartRepository.getCart(userId)
                val response = CartResponse(
                    items = items.map { item ->
                        CartItemResponse(
                            id = item.id.toString(),
                            productId = item.productId.toString(),
                            productName = item.product.name,
                            productPrice = item.product.price.toDouble(),
                            productImageUrl = item.product.imageUrl,
                            stock = item.product.stock,
                            quantity = item.quantity
                        )
                    },
                    total = items.sumOf { it.product.price.toDouble() * it.quantity }
                )
                call.respond(response)
            }

            post("/items") {
                val userId = call.userId()
                val req = call.receive<AddCartItemRequest>()
                val productId = UUID.fromString(req.productId)
                val item = cartRepository.addItem(userId, productId, req.quantity)
                call.respond(
                    HttpStatusCode.Created,
                    CartItemResponse(
                        id = item.id.toString(),
                        productId = item.productId.toString(),
                        productName = item.product.name,
                        productPrice = item.product.price.toDouble(),
                        productImageUrl = item.product.imageUrl,
                        stock = item.product.stock,
                        quantity = item.quantity
                    )
                )
            }

            patch("/items/{productId}") {
                val userId = call.userId()
                val productId = UUID.fromString(call.parameters["productId"])
                val req = call.receive<UpdateCartItemRequest>()
                val item = cartRepository.updateItem(userId, productId, req.quantity) ?: run {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Cart item not found", 404))
                    return@patch
                }
                call.respond(
                    CartItemResponse(
                        id = item.id.toString(),
                        productId = item.productId.toString(),
                        productName = item.product.name,
                        productPrice = item.product.price.toDouble(),
                        productImageUrl = item.product.imageUrl,
                        stock = item.product.stock,
                        quantity = item.quantity
                    )
                )
            }

            delete("/items/{productId}") {
                val userId = call.userId()
                val productId = UUID.fromString(call.parameters["productId"])
                cartRepository.removeItem(userId, productId)
                call.respond(HttpStatusCode.NoContent)
            }

            delete {
                val userId = call.userId()
                cartRepository.clearCart(userId)
                call.respond(HttpStatusCode.NoContent)
            }

            post("/merge") {
                val userId = call.userId()
                val req = call.receive<MergeCartRequest>()
                val items = req.items.map { UUID.fromString(it.productId) to it.quantity }
                cartRepository.mergeGuestCart(userId, items)
                val updatedCart = cartRepository.getCart(userId)
                val response = CartResponse(
                    items = updatedCart.map { item ->
                        CartItemResponse(
                            id = item.id.toString(),
                            productId = item.productId.toString(),
                            productName = item.product.name,
                            productPrice = item.product.price.toDouble(),
                            productImageUrl = item.product.imageUrl,
                            stock = item.product.stock,
                            quantity = item.quantity
                        )
                    },
                    total = updatedCart.sumOf { it.product.price.toDouble() * it.quantity }
                )
                call.respond(response)
            }
        }
    }
}

private fun ApplicationCall.userId(): UUID {
    val principal = principal<JWTPrincipal>()!!
    return UUID.fromString(principal.payload.getClaim("userId").asString())
}
