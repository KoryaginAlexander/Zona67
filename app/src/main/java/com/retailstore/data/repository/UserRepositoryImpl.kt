package com.retailstore.data.repository

import com.retailstore.data.remote.api.UserApi
import com.retailstore.data.remote.dto.UpdateUserRequest
import com.retailstore.domain.model.Result
import com.retailstore.domain.model.User
import com.retailstore.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : UserRepository {

    override suspend fun getMe(): Result<User> = try {
        val response = userApi.getMe()
        if (response.isSuccessful) Result.Success(response.body()!!.toDomain())
        else Result.Error(response.code(), "Failed to load profile")
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun updateMe(fullName: String?, address: String?): Result<User> = try {
        val response = userApi.updateMe(UpdateUserRequest(fullName = fullName, address = address))
        if (response.isSuccessful) Result.Success(response.body()!!.toDomain())
        else Result.Error(response.code(), "Failed to update profile")
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun getAllUsers(): Result<List<User>> = try {
        val response = userApi.getAllUsers()
        if (response.isSuccessful) Result.Success(response.body()!!.map { it.toDomain() })
        else Result.Error(response.code(), "Failed to load users")
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun getUserById(id: String): Result<User> = try {
        val response = userApi.getUserById(id)
        if (response.isSuccessful) Result.Success(response.body()!!.toDomain())
        else Result.Error(response.code(), "User not found")
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }
}

private fun com.retailstore.data.remote.dto.UserDto.toDomain() =
    User(id = id, email = email, fullName = fullName, phone = phone, address = address, role = role)
