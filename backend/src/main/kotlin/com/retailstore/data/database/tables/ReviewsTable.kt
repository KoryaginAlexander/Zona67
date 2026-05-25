package com.retailstore.data.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object ReviewsTable : Table("reviews") {
    val id = uuid("id")
    val productId = uuid("product_id").references(ProductsTable.id)
    val userId = uuid("user_id").references(UsersTable.id)
    val rating = integer("rating")
    val comment = text("comment").nullable()
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(productId, userId)
    }
}
