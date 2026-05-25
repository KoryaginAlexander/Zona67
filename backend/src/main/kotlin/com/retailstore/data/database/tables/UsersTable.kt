package com.retailstore.data.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UsersTable : Table("users") {
    val id = uuid("id")
    val firebaseUid = varchar("firebase_uid", 128).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val fullName = varchar("full_name", 255).nullable()
    val phone = varchar("phone", 20).nullable()
    val address = text("address").nullable()
    val role = varchar("role", 20).default("CUSTOMER")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    override val primaryKey = PrimaryKey(id)
}
