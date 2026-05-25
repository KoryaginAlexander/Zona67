package com.retailstore.data.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object ProductsTable : Table("products") {
    val id = uuid("id")
    val categoryId = integer("category_id").references(CategoriesTable.id)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val price = decimal("price", 12, 2)
    val stock = integer("stock").default(0)
    val brand = varchar("brand", 100).nullable()
    val model = varchar("model", 100).nullable()
    val imageUrls = text("image_urls").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    override val primaryKey = PrimaryKey(id)
}
