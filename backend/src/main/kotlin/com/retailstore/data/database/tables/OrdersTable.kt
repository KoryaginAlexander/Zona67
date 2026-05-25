package com.retailstore.data.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object OrdersTable : Table("orders") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id)
    val status = varchar("status", 30).default("PENDING")
    val totalAmount = decimal("total_amount", 12, 2)
    val deliveryAddress = text("delivery_address").nullable()
    val comment = text("comment").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    override val primaryKey = PrimaryKey(id)
}
