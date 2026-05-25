package com.retailstore.data.database.tables

import org.jetbrains.exposed.sql.Table

object ProductSpecsTable : Table("product_specs") {
    val id = integer("id").autoIncrement()
    val productId = uuid("product_id").references(ProductsTable.id)
    val specKey = varchar("spec_key", 100)
    val specValue = varchar("spec_value", 255)
    override val primaryKey = PrimaryKey(id)
}
