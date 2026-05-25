package com.retailstore.presentation.routes

import com.retailstore.domain.repository.ProductFilter
import com.retailstore.domain.repository.ProductRepository
import com.retailstore.presentation.dto.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.math.BigDecimal
import java.util.UUID

fun Route.productRoutes() {
    val productRepository by inject<ProductRepository>()

    route("/products") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
            val categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull()
            val brand = call.request.queryParameters["brand"]
            val minPrice = call.request.queryParameters["minPrice"]?.toDoubleOrNull()?.let { BigDecimal.valueOf(it) }
            val maxPrice = call.request.queryParameters["maxPrice"]?.toDoubleOrNull()?.let { BigDecimal.valueOf(it) }
            val search = call.request.queryParameters["search"]
            val sortBy = call.request.queryParameters["sortBy"]
            val activeOnly = call.request.queryParameters["activeOnly"]?.toBooleanStrictOrNull() ?: true

            val filter = ProductFilter(
                page = page, limit = limit, categoryId = categoryId,
                brand = brand, minPrice = minPrice, maxPrice = maxPrice,
                search = search, sortBy = sortBy, activeOnly = activeOnly
            )
            val (products, total) = productRepository.findAll(filter)
            call.respond(
                ProductsPageResponse(
                    items = products.map { it.toResponse() },
                    total = total,
                    page = page,
                    limit = limit
                )
            )
        }

        get("/{id}") {
            val id = UUID.fromString(call.parameters["id"])
            val product = productRepository.findById(id) ?: run {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Product not found", 404))
                return@get
            }
            call.respond(product.toResponse())
        }

        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@post
                }
                val req = call.receive<CreateProductRequest>()
                val product = productRepository.create(
                    categoryId = req.categoryId,
                    name = req.name,
                    description = req.description,
                    price = BigDecimal.valueOf(req.price),
                    stock = req.stock,
                    brand = req.brand,
                    model = req.model,
                    imageUrls = req.imageUrls,
                    specs = req.specs.map { it.key to it.value }
                )
                call.respond(HttpStatusCode.Created, product.toResponse())
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@put
                }
                val id = UUID.fromString(call.parameters["id"])
                val req = call.receive<UpdateProductRequest>()
                val product = productRepository.update(
                    id = id,
                    categoryId = req.categoryId,
                    name = req.name,
                    description = req.description,
                    price = req.price?.let { BigDecimal.valueOf(it) },
                    stock = req.stock,
                    brand = req.brand,
                    model = req.model,
                    imageUrls = req.imageUrls,
                    specs = req.specs?.map { it.key to it.value }
                ) ?: run {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Product not found", 404))
                    return@put
                }
                call.respond(product.toResponse())
            }

            patch("/{id}/deactivate") {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@patch
                }
                val id = UUID.fromString(call.parameters["id"])
                productRepository.setActive(id, false)
                call.respond(HttpStatusCode.OK, MessageResponse("Product deactivated"))
            }

            patch("/{id}/activate") {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@patch
                }
                val id = UUID.fromString(call.parameters["id"])
                productRepository.setActive(id, true)
                call.respond(HttpStatusCode.OK, MessageResponse("Product activated"))
            }
        }
    }
}

private fun com.retailstore.domain.model.Product.toResponse() = ProductResponse(
    id = id.toString(),
    categoryId = categoryId,
    name = name,
    description = description,
    price = price.toDouble(),
    stock = stock,
    brand = brand,
    model = model,
    imageUrls = imageUrls,
    isActive = isActive,
    specs = specs.map { ProductSpecDto(it.key, it.value) }
)
