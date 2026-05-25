package com.retailstore.presentation.screens.admin.products

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.retailstore.data.remote.api.ProductApi
import dagger.hilt.android.qualifiers.ApplicationContext
import com.retailstore.data.remote.dto.CreateProductRequest
import com.retailstore.data.remote.dto.ProductSpecDto
import com.retailstore.data.remote.dto.UpdateProductRequest
import com.retailstore.domain.model.Category
import com.retailstore.domain.model.Product
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

data class AdminProductFormUiState(
    val loading: Boolean = false,
    val product: Product? = null,
    val categories: List<Category> = emptyList(),
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AdminProductFormViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val productApi: ProductApi,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductFormUiState())
    val uiState: StateFlow<AdminProductFormUiState> = _uiState.asStateFlow()

    fun init(productId: String?) = viewModelScope.launch {
        when (val result = productRepository.getCategories()) {
            is Result.Success -> _uiState.update { it.copy(categories = result.data) }
            else -> {}
        }
        if (productId != null) {
            when (val result = productRepository.getProductById(productId)) {
                is Result.Success -> _uiState.update { it.copy(product = result.data) }
                else -> {}
            }
        }
    }

    fun uploadImageAndSave(
        imageUri: Uri?,
        categoryId: Int,
        name: String,
        description: String,
        price: Double,
        stock: Int,
        brand: String,
        model: String,
        specs: List<Pair<String, String>>,
        existingImageUrls: List<String>,
        productId: String?
    ) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }
        var imageUrls = existingImageUrls.toMutableList()

        if (imageUri != null) {
            try {
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("products/${UUID.randomUUID()}.jpg")
                val stream = context.contentResolver.openInputStream(imageUri)
                    ?: throw Exception("Не удалось открыть изображение")
                storageRef.putStream(stream).await()
                stream.close()
                val url = storageRef.downloadUrl.await().toString()
                imageUrls.add(0, url)
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = "Ошибка загрузки изображения: ${e.message}") }
                return@launch
            }
        }

        val specsDto = specs.map { ProductSpecDto(it.first, it.second) }
        try {
            val response = if (productId == null) {
                productApi.createProduct(
                    CreateProductRequest(categoryId, name, description.takeIf { it.isNotBlank() },
                        price, stock, brand.takeIf { it.isNotBlank() }, model.takeIf { it.isNotBlank() }, imageUrls, specsDto)
                )
            } else {
                productApi.updateProduct(
                    productId,
                    UpdateProductRequest(categoryId, name, description.takeIf { it.isNotBlank() },
                        price, stock, brand.takeIf { it.isNotBlank() }, model.takeIf { it.isNotBlank() }, imageUrls, specsDto)
                )
            }
            if (response.isSuccessful) {
                _uiState.update { it.copy(loading = false, success = true) }
            } else {
                _uiState.update { it.copy(loading = false, error = "Ошибка сохранения") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(loading = false, error = e.message) }
        }
    }
}
