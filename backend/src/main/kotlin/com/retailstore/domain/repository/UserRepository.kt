package com.retailstore.domain.repository

import com.retailstore.domain.model.User
import java.time.LocalDateTime
import java.util.UUID

interface UserRepository {
    suspend fun findByFirebaseUid(firebaseUid: String): User?
    suspend fun findById(id: UUID): User?
    suspend fun findByEmail(email: String): User?
    suspend fun create(firebaseUid: String, email: String, fullName: String?, role: String): User
    suspend fun update(id: UUID, fullName: String?, phone: String?, address: String?): User?
    suspend fun findAll(): List<User>
    suspend fun saveRefreshToken(userId: UUID, token: String, expiresAt: LocalDateTime)
    suspend fun findRefreshToken(token: String): Pair<UUID, LocalDateTime>?
    suspend fun deleteRefreshToken(token: String)
    suspend fun deleteAllRefreshTokens(userId: UUID)
}
