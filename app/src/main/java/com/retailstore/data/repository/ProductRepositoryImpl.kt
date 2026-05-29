package com.retailstore.data.repository

import com.retailstore.data.remote.api.CategoryApi
import com.retailstore.data.remote.api.ProductApi
import com.retailstore.domain.model.*
import com.retailstore.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productApi: ProductApi,
    private val categoryApi: CategoryApi
) : ProductRepository {

    override suspend fun getProducts(
        page: Int, limit: Int, categoryId: Int?, brand: String?,
        minPrice: Double?, maxPrice: Double?, search: String?,
        sortBy: String?, activeOnly: Boolean
    ): Result<ProductsPage> = try {
        val response = productApi.getProducts(
            page = page, limit = limit, categoryId = categoryId,
            brand = brand, minPrice = minPrice, maxPrice = maxPrice,
            search = search, sortBy = sortBy, activeOnly = activeOnly
        )
        if (response.isSuccessful) {
            val body = response.body()!!
            Result.Success(ProductsPage(
                items = body.items.map { it.toDomain() },
                total = body.total,
                page = body.page,
                limit = body.limit
            ))
        } else {
            Result.Error(response.code(), "Failed to load products")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun getProductById(id: String): Result<Product> = try {
        val response = productApi.getProductById(id)
        if (response.isSuccessful) {
            Result.Success(response.body()!!.toDomain())
        } else {
            Result.Error(response.code(), "Product not found")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun deleteProduct(id: String): Result<Unit> = try {
        val response = productApi.deleteProduct(id)
        when {
            response.isSuccessful -> Result.Success(Unit)
            response.code() == 409 -> Result.Error(409, "Товар присутствует в заказах и не может быть удалён")
            response.code() == 404 -> Result.Error(404, "Товар не найден")
            else -> Result.Error(response.code(), "Ошибка удаления")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun getCategories(): Result<List<Category>> = try {
        val response = categoryApi.getCategories()
        if (response.isSuccessful) {
            Result.Success(response.body()!!.map { Category(it.id, it.name, it.slug, it.imageUrl) })
        } else {
            Result.Error(response.code(), "Failed to load categories")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }
}

fun com.retailstore.data.remote.dto.ProductDto.toDomain() = Product(
    id = id, categoryId = categoryId, name = name, description = description,
    price = price, stock = stock, brand = brand, model = model,
    imageUrls = imageUrls ?: emptyList(), isActive = isActive,
    specs = specs?.map { ProductSpec(it.key, it.value) } ?: emptyList(),
    averageRating = averageRating ?: 0.0,
    reviewCount = reviewCount ?: 0
)
