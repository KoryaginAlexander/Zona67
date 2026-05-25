package com.retailstore.presentation.routes

import com.retailstore.domain.model.User
import com.retailstore.domain.repository.UserRepository
import com.retailstore.presentation.dto.*
import com.retailstore.utils.FirebaseUtils
import com.retailstore.utils.JwtUtils
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.time.LocalDateTime
import java.util.UUID

fun Route.authRoutes() {
    val userRepository by inject<UserRepository>()
    val jwtUtils by inject<JwtUtils>()
    val adminEmail = System.getenv("ADMIN_EMAIL") ?: ""

    route("/auth") {
        post("/login") {
            val req = call.receive<LoginRequest>()
            try {
                val firebaseData = FirebaseUtils.verifyIdToken(req.firebaseIdToken)
                var user = userRepository.findByFirebaseUid(firebaseData.uid)
                if (user == null) {
                    val role = if (firebaseData.email == adminEmail) "ADMIN" else "CUSTOMER"
                    user = userRepository.create(firebaseData.uid, firebaseData.email, req.fullName, role)
                }
                val accessToken = jwtUtils.generateAccessToken(user.id, user.role)
                val refreshToken = jwtUtils.generateRefreshToken()
                userRepository.saveRefreshToken(user.id, refreshToken, jwtUtils.getRefreshTokenExpiry())
                call.respond(
                    AuthResponse(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        user = user.toResponse()
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("INVALID_TOKEN", "Firebase token verification failed", 401)
                )
            }
        }

        post("/refresh") {
            val req = call.receive<RefreshRequest>()
            val tokenData = userRepository.findRefreshToken(req.refreshToken)
            if (tokenData == null || tokenData.second.isBefore(LocalDateTime.now())) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired", 401)
                )
                return@post
            }
            val user = userRepository.findById(tokenData.first) ?: run {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("USER_NOT_FOUND", "User not found", 404))
                return@post
            }
            val newAccessToken = jwtUtils.generateAccessToken(user.id, user.role)
            call.respond(AccessTokenResponse(newAccessToken))
        }

        authenticate("auth-jwt") {
            post("/logout") {
                val req = call.receive<LogoutRequest>()
                userRepository.deleteRefreshToken(req.refreshToken)
                call.respond(HttpStatusCode.OK, MessageResponse("Logged out successfully"))
            }
        }
    }
}

fun User.toResponse() = UserResponse(
    id = id.toString(),
    email = email,
    fullName = fullName,
    phone = phone,
    address = address,
    role = role
)
