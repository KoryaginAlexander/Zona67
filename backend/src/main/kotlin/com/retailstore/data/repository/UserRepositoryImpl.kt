package com.retailstore.data.repository

import com.retailstore.data.database.tables.RefreshTokensTable
import com.retailstore.data.database.tables.UsersTable
import com.retailstore.domain.model.User
import com.retailstore.domain.repository.UserRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

class UserRepositoryImpl : UserRepository {

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id],
        firebaseUid = this[UsersTable.firebaseUid],
        email = this[UsersTable.email],
        fullName = this[UsersTable.fullName],
        phone = this[UsersTable.phone],
        address = this[UsersTable.address],
        role = this[UsersTable.role],
        createdAt = this[UsersTable.createdAt],
        updatedAt = this[UsersTable.updatedAt]
    )

    override suspend fun findByFirebaseUid(firebaseUid: String): User? =
        newSuspendedTransaction {
            UsersTable.select { UsersTable.firebaseUid eq firebaseUid }
                .singleOrNull()?.toUser()
        }

    override suspend fun findById(id: UUID): User? =
        newSuspendedTransaction {
            UsersTable.select { UsersTable.id eq id }
                .singleOrNull()?.toUser()
        }

    override suspend fun findByEmail(email: String): User? =
        newSuspendedTransaction {
            UsersTable.select { UsersTable.email eq email }
                .singleOrNull()?.toUser()
        }

    override suspend fun create(firebaseUid: String, email: String, fullName: String?, role: String): User =
        newSuspendedTransaction {
            val id = UUID.randomUUID()
            val now = LocalDateTime.now()
            UsersTable.insert {
                it[UsersTable.id] = id
                it[UsersTable.firebaseUid] = firebaseUid
                it[UsersTable.email] = email
                it[UsersTable.fullName] = fullName
                it[UsersTable.role] = role
                it[UsersTable.createdAt] = now
                it[UsersTable.updatedAt] = now
            }
            UsersTable.select { UsersTable.id eq id }.single().toUser()
        }

    override suspend fun update(id: UUID, fullName: String?, phone: String?, address: String?): User? =
        newSuspendedTransaction {
            val updated = UsersTable.update({ UsersTable.id eq id }) {
                if (fullName != null) it[UsersTable.fullName] = fullName
                if (phone != null) it[UsersTable.phone] = phone
                if (address != null) it[UsersTable.address] = address
                it[UsersTable.updatedAt] = LocalDateTime.now()
            }
            if (updated == 0) null
            else UsersTable.select { UsersTable.id eq id }.singleOrNull()?.toUser()
        }

    override suspend fun findAll(): List<User> =
        newSuspendedTransaction {
            UsersTable.selectAll().map { it.toUser() }
        }

    override suspend fun saveRefreshToken(userId: UUID, token: String, expiresAt: LocalDateTime) =
        newSuspendedTransaction {
            RefreshTokensTable.insert {
                it[RefreshTokensTable.id] = UUID.randomUUID()
                it[RefreshTokensTable.userId] = userId
                it[RefreshTokensTable.token] = token
                it[RefreshTokensTable.expiresAt] = expiresAt
                it[RefreshTokensTable.createdAt] = LocalDateTime.now()
            }
            Unit
        }

    override suspend fun findRefreshToken(token: String): Pair<UUID, LocalDateTime>? =
        newSuspendedTransaction {
            RefreshTokensTable.select { RefreshTokensTable.token eq token }
                .singleOrNull()?.let {
                    it[RefreshTokensTable.userId] to it[RefreshTokensTable.expiresAt]
                }
        }

    override suspend fun deleteRefreshToken(token: String) =
        newSuspendedTransaction {
            RefreshTokensTable.deleteWhere { RefreshTokensTable.token eq token }
            Unit
        }

    override suspend fun deleteAllRefreshTokens(userId: UUID) =
        newSuspendedTransaction {
            RefreshTokensTable.deleteWhere { RefreshTokensTable.userId eq userId }
            Unit
        }
}
