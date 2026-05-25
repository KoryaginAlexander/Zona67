package com.retailstore.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductSpecDto(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String
)

data class ProductDto(
    @SerializedName("id") val id: String,
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("stock") val stock: Int,
    @SerializedName("brand") val brand: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("imageUrls") val imageUrls: List<String>?,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("specs") val specs: List<ProductSpecDto>?,
    @SerializedName("averageRating") val averageRating: Double? = null,
    @SerializedName("reviewCount") val reviewCount: Int? = null
)

data class ProductsPageDto(
    @SerializedName("items") val items: List<ProductDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int
)

data class CategoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("imageUrl") val imageUrl: String?
)

data class CreateProductRequest(
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("stock") val stock: Int,
    @SerializedName("brand") val brand: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("imageUrls") val imageUrls: List<String>,
    @SerializedName("specs") val specs: List<ProductSpecDto>
)

data class UpdateProductRequest(
    @SerializedName("categoryId") val categoryId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("stock") val stock: Int? = null,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("imageUrls") val imageUrls: List<String>? = null,
    @SerializedName("specs") val specs: List<ProductSpecDto>? = null
)
