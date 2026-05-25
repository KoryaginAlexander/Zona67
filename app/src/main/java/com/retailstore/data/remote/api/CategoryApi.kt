package com.retailstore.data.remote.api

import com.retailstore.data.remote.dto.CategoryDto
import retrofit2.Response
import retrofit2.http.GET

interface CategoryApi {
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>
}
