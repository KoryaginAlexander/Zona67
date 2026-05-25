package com.retailstore.data.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object RefreshTokensTable : Table("refresh_tokens") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id)
    val token = varchar("token", 512).uniqueIndex()
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}
