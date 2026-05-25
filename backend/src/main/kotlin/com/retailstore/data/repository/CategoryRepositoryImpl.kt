package com.retailstore.data.repository

import com.retailstore.data.database.tables.CategoriesTable
import com.retailstore.domain.model.Category
import com.retailstore.domain.repository.CategoryRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class CategoryRepositoryImpl : CategoryRepository {

    private fun ResultRow.toCategory() = Category(
        id = this[CategoriesTable.id],
        name = this[CategoriesTable.name],
        slug = this[CategoriesTable.slug],
        imageUrl = this[CategoriesTable.imageUrl]
    )

    override suspend fun findAll(): List<Category> =
        newSuspendedTransaction {
            CategoriesTable.selectAll().map { it.toCategory() }
        }

    override suspend fun findById(id: Int): Category? =
        newSuspendedTransaction {
            CategoriesTable.select { CategoriesTable.id eq id }
                .singleOrNull()?.toCategory()
        }

    override suspend fun create(name: String, slug: String, imageUrl: String?): Category =
        newSuspendedTransaction {
            val id = CategoriesTable.insert {
                it[CategoriesTable.name] = name
                it[CategoriesTable.slug] = slug
                it[CategoriesTable.imageUrl] = imageUrl
            }[CategoriesTable.id]
            CategoriesTable.select { CategoriesTable.id eq id }.single().toCategory()
        }

    override suspend fun update(id: Int, name: String, slug: String, imageUrl: String?): Category? =
        newSuspendedTransaction {
            val updated = CategoriesTable.update({ CategoriesTable.id eq id }) {
                it[CategoriesTable.name] = name
                it[CategoriesTable.slug] = slug
                it[CategoriesTable.imageUrl] = imageUrl
            }
            if (updated == 0) null
            else CategoriesTable.select { CategoriesTable.id eq id }.singleOrNull()?.toCategory()
        }

    override suspend fun delete(id: Int): Boolean =
        newSuspendedTransaction {
            CategoriesTable.deleteWhere { CategoriesTable.id eq id } > 0
        }
}
