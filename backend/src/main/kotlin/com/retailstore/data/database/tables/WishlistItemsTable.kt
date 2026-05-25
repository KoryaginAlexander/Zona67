package com.retailstore.data.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object WishlistItemsTable : Table("wishlist_items") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id)
    val productId = uuid("product_id").references(ProductsTable.id)
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
    init { uniqueIndex(userId, productId) }
}
