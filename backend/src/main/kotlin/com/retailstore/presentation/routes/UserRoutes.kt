package com.retailstore.presentation.routes

import com.retailstore.domain.repository.UserRepository
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

fun Route.userRoutes() {
    val userRepository by inject<UserRepository>()

    authenticate("auth-jwt") {
        route("/users") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(principal.payload.getClaim("userId").asString())
                val user = userRepository.findById(userId) ?: run {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("USER_NOT_FOUND", "User not found", 404))
                    return@get
                }
                call.respond(user.toResponse())
            }

            patch("/me") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = UUID.fromString(principal.payload.getClaim("userId").asString())
                val req = call.receive<UpdateUserRequest>()
                val user = userRepository.update(userId, req.fullName, req.phone, req.address) ?: run {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("USER_NOT_FOUND", "User not found", 404))
                    return@patch
                }
                call.respond(user.toResponse())
            }

            get {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@get
                }
                call.respond(userRepository.findAll().map { it.toResponse() })
            }

            get("/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("FORBIDDEN", "Admin access required", 403))
                    return@get
                }
                val id = UUID.fromString(call.parameters["id"])
                val user = userRepository.findById(id) ?: run {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("USER_NOT_FOUND", "User not found", 404))
                    return@get
                }
                call.respond(user.toResponse())
            }
        }
    }
}
