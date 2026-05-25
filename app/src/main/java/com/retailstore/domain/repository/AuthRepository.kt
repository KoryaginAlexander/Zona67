package com.retailstore.domain.repository

import com.retailstore.domain.model.Result
import com.retailstore.domain.model.User

interface AuthRepository {
    suspend fun login(firebaseIdToken: String): Result<User>
    suspend fun register(email: String, password: String, fullName: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(): Result<String>
    fun isLoggedIn(): Boolean
    suspend fun getCurrentUser(): User?
}
