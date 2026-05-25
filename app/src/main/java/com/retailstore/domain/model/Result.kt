package com.retailstore.domain.model

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val code: Int? = null, val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
