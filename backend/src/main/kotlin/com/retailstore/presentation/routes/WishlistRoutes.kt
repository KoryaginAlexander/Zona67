package com.retailstore.presentation.routes

import com.retailstore.domain.repository.WishlistRepository
import com.retailstore.presentation.dto.WishlistItemResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.wishlistRoutes() {
    val wishlistRepository by inject<WishlistRepository>()

    authenticate("auth-jwt") {
        route("/wishlist") {
            get {
                val userId = call.userId()
                val items = wishlistRepository.getWishlist(userId)
                call.respond(items.map { item ->
                    WishlistItemResponse(
                        id = item.id.toString(),
                        productId = item.productId.toString(),
                        productName = item.product.name,
                        productPrice = item.product.price.toDouble(),
                        productImageUrl = item.product.imageUrl,
                        stock = item.product.stock
                    )
                })
            }

            post("/{productId}") {
                val userId = call.userId()
                val productId = UUID.fromString(call.parameters["productId"])
                val item = wishlistRepository.addItem(userId, productId)
                call.respond(
                    HttpStatusCode.Created,
                    WishlistItemResponse(
                        id = item.id.toString(),
                        productId = item.productId.toString(),
                        productName = item.product.name,
                        productPrice = item.product.price.toDouble(),
                        productImageUrl = item.product.imageUrl,
                        stock = item.product.stock
                    )
                )
            }

            delete("/{productId}") {
                val userId = call.userId()
                val productId = UUID.fromString(call.parameters["productId"])
                wishlistRepository.removeItem(userId, productId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun ApplicationCall.userId(): UUID {
    val principal = principal<JWTPrincipal>()!!
    return UUID.fromString(principal.payload.getClaim("userId").asString())
}
