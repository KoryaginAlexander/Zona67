package com.retailstore.presentation.routes

import com.retailstore.domain.repository.CategoryRepository
import com.retailstore.presentation.dto.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.categoryRoutes() {
    val categoryRepository by inject<CategoryRepository>()

    route("/categories") {
        get {
            val categories = categoryRepository.findAll()
            call.respond(categories.map { CategoryResponse(it.id, it.name, it.slug, it.imageUrl) })
        }

        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@post
                }
                val req = call.receive<CategoryRequest>()
                val slug = req.name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                val category = categoryRepository.create(req.name, slug, req.imageUrl)
                call.respond(HttpStatusCode.Created, CategoryResponse(category.id, category.name, category.slug, category.imageUrl))
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@put
                }
                val id = call.parameters["id"]!!.toInt()
                val req = call.receive<CategoryRequest>()
                val slug = req.name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                val category = categoryRepository.update(id, req.name, slug, req.imageUrl) ?: run {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Category not found", 404))
                    return@put
                }
                call.respond(CategoryResponse(category.id, category.name, category.slug, category.imageUrl))
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@delete
                }
                val id = call.parameters["id"]!!.toInt()
                val deleted = categoryRepository.delete(id)
                if (deleted) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound, ErrorResponse("NOT_FOUND", "Category not found", 404))
            }
        }
    }
}
