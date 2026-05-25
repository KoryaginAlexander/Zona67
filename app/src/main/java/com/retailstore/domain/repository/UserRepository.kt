package com.retailstore.domain.repository

import com.retailstore.domain.model.Result
import com.retailstore.domain.model.User

interface UserRepository {
    suspend fun getMe(): Result<User>
    suspend fun updateMe(fullName: String?, address: String?): Result<User>
    suspend fun getAllUsers(): Result<List<User>>
    suspend fun getUserById(id: String): Result<User>
}
