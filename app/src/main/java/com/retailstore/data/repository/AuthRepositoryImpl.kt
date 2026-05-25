package com.retailstore.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.retailstore.data.local.TokenDataStore
import com.retailstore.data.remote.api.AuthApi
import com.retailstore.data.remote.api.UserApi
import com.retailstore.data.remote.dto.LoginRequest
import com.retailstore.data.remote.dto.LogoutRequest
import com.retailstore.data.remote.dto.RefreshRequest
import com.retailstore.data.remote.dto.UpdateUserRequest
import com.retailstore.domain.model.Result
import com.retailstore.domain.model.User
import com.retailstore.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val tokenDataStore: TokenDataStore,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun login(firebaseIdToken: String): Result<User> = try {
        val response = authApi.login(LoginRequest(firebaseIdToken))
        if (response.isSuccessful) {
            val body = response.body()!!
            tokenDataStore.saveTokens(
                accessToken = body.accessToken,
                refreshToken = body.refreshToken,
                userId = body.user.id,
                role = body.user.role
            )
            Result.Success(body.user.toDomain())
        } else {
            Result.Error(response.code(), "Login failed: ${response.code()}")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun register(email: String, password: String, fullName: String): Result<User> = try {
        val firebaseResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val idToken = firebaseResult.user?.getIdToken(false)?.await()?.token
            ?: return Result.Error(message = "Failed to get Firebase ID token")
        val response = authApi.login(LoginRequest(firebaseIdToken = idToken, fullName = fullName.takeIf { it.isNotBlank() }))
        if (response.isSuccessful) {
            val body = response.body()!!
            tokenDataStore.saveTokens(
                accessToken = body.accessToken,
                refreshToken = body.refreshToken,
                userId = body.user.id,
                role = body.user.role
            )
            if (fullName.isNotBlank()) {
                runCatching { userApi.updateMe(UpdateUserRequest(fullName = fullName)) }
            }
            val user = runCatching { userApi.getMe().body()?.toDomain() }.getOrNull()
                ?: body.user.toDomain()
            Result.Success(user)
        } else {
            Result.Error(response.code(), "Registration failed: ${response.code()}")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Registration failed")
    }

    override suspend fun logout(): Result<Unit> = try {
        val refreshToken = tokenDataStore.getRefreshTokenOnce()
        if (refreshToken != null) {
            authApi.logout(LogoutRequest(refreshToken))
        }
        tokenDataStore.clearTokens()
        firebaseAuth.signOut()
        Result.Success(Unit)
    } catch (e: Exception) {
        tokenDataStore.clearTokens()
        firebaseAuth.signOut()
        Result.Success(Unit)
    }

    override suspend fun refreshToken(): Result<String> = try {
        val refreshToken = tokenDataStore.getRefreshTokenOnce()
            ?: return Result.Error(message = "No refresh token")
        val response = authApi.refresh(RefreshRequest(refreshToken))
        if (response.isSuccessful) {
            val newAccessToken = response.body()?.get("accessToken")
                ?: return Result.Error(message = "No access token in response")
            tokenDataStore.updateAccessToken(newAccessToken)
            Result.Success(newAccessToken)
        } else {
            Result.Error(response.code(), "Token refresh failed")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override fun isLoggedIn(): Boolean = kotlinx.coroutines.runBlocking { tokenDataStore.isLoggedIn() }

    override suspend fun getCurrentUser(): User? {
        val userId = tokenDataStore.userId
        return null
    }
}

private fun com.retailstore.data.remote.dto.UserDto.toDomain() = User(
    id = id, email = email, fullName = fullName, phone = phone, address = address, role = role
)
