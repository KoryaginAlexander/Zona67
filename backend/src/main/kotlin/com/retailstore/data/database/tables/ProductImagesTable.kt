package com.retailstore.data.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object ProductImagesTable : Table("product_images") {
    val id = uuid("id")
    val data = binary("data")
    val contentType = varchar("content_type", 50)
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}
