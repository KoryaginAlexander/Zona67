package com.retailstore.data.database.tables

import org.jetbrains.exposed.sql.Table

object OrderItemsTable : Table("order_items") {
    val id = uuid("id")
    val orderId = uuid("order_id").references(OrdersTable.id)
    val productId = uuid("product_id").references(ProductsTable.id)
    val productName = varchar("product_name", 255)
    val productPrice = decimal("product_price", 12, 2)
    val quantity = integer("quantity")
    override val primaryKey = PrimaryKey(id)
}
