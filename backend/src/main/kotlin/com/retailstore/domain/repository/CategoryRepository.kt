package com.retailstore.domain.repository

import com.retailstore.domain.model.Category

interface CategoryRepository {
    suspend fun findAll(): List<Category>
    suspend fun findById(id: Int): Category?
    suspend fun create(name: String, slug: String, imageUrl: String?): Category
    suspend fun update(id: Int, name: String, slug: String, imageUrl: String?): Category?
    suspend fun delete(id: Int): Boolean
}
