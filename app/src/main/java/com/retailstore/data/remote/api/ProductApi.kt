package com.retailstore.data.remote.api

import com.retailstore.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ProductApi {
    @GET("products")
    suspend fun getProducts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("categoryId") categoryId: Int? = null,
        @Query("brand") brand: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("search") search: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("activeOnly") activeOnly: Boolean = true
    ): Response<ProductsPageDto>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<ProductDto>

    @POST("products")
    suspend fun createProduct(@Body request: CreateProductRequest): Response<ProductDto>

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body request: UpdateProductRequest): Response<ProductDto>

    @PATCH("products/{id}/deactivate")
    suspend fun deactivateProduct(@Path("id") id: String): Response<Unit>

    @PATCH("products/{id}/activate")
    suspend fun activateProduct(@Path("id") id: String): Response<Unit>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Unit>
}
