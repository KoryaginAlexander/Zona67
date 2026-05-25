package com.retailstore.data.database.tables

import org.jetbrains.exposed.sql.Table

object CategoriesTable : Table("categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val slug = varchar("slug", 100).uniqueIndex()
    val imageUrl = varchar("image_url", 512).nullable()
    override val primaryKey = PrimaryKey(id)
}
