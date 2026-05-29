package com.retailstore.domain.repository

import com.retailstore.domain.model.Category
import com.retailstore.domain.model.Product
import com.retailstore.domain.model.ProductsPage
import com.retailstore.domain.model.Result

interface ProductRepository {
    suspend fun getProducts(
        page: Int = 1, limit: Int = 20,
        categoryId: Int? = null, brand: String? = null,
        minPrice: Double? = null, maxPrice: Double? = null,
        search: String? = null, sortBy: String? = null,
        activeOnly: Boolean = true
    ): Result<ProductsPage>

    suspend fun getProductById(id: String): Result<Product>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun deleteProduct(id: String): Result<Unit>
}
