package com.retailstore.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

class JwtUtils(
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val accessTokenTtlMinutes: Long,
    private val refreshTokenTtlDays: Long
) {
    private val algorithm = Algorithm.HMAC256(secret)

    fun generateAccessToken(userId: UUID, role: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId.toString())
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenTtlMinutes * 60 * 1000))
            .sign(algorithm)

    fun generateRefreshToken(): String =
        UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "")

    fun getRefreshTokenExpiry(): LocalDateTime =
        LocalDateTime.now().plusDays(refreshTokenTtlDays)
}
