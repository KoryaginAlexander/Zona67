package com.retailstore.domain.repository

import com.retailstore.domain.model.Product
import java.math.BigDecimal
import java.util.UUID

data class ProductFilter(
    val page: Int = 1,
    val limit: Int = 20,
    val categoryId: Int? = null,
    val brand: String? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val search: String? = null,
    val sortBy: String? = null,
    val activeOnly: Boolean = true
)

interface ProductRepository {
    suspend fun findAll(filter: ProductFilter): Pair<List<Product>, Int>
    suspend fun findById(id: UUID): Product?
    suspend fun create(
        categoryId: Int, name: String, description: String?, price: BigDecimal,
        stock: Int, brand: String?, model: String?, imageUrls: List<String>,
        specs: List<Pair<String, String>>
    ): Product
    suspend fun update(
        id: UUID, categoryId: Int?, name: String?, description: String?,
        price: BigDecimal?, stock: Int?, brand: String?, model: String?,
        imageUrls: List<String>?, specs: List<Pair<String, String>>?
    ): Product?
    suspend fun setActive(id: UUID, active: Boolean): Boolean
    suspend fun delete(id: UUID): Boolean
}
