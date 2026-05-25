package com.retailstore.data.remote.interceptor

import com.retailstore.data.local.TokenDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking { tokenDataStore.getAccessTokenOnce() }

        val request = if (accessToken != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            chain.request()
        }

        val response = chain.proceed(request)

        if (response.code == 401 && accessToken != null) {
            response.close()
            val refreshToken = runBlocking { tokenDataStore.getRefreshTokenOnce() } ?: return response

            val newAccessToken = runBlocking {
                try {
                    val client = okhttp3.OkHttpClient.Builder().build()
                    val refreshRequest = okhttp3.Request.Builder()
                        .url(chain.request().url.newBuilder()
                            .encodedPath("/api/v1/auth/refresh")
                            .build())
                        .post("""{"refreshToken":"$refreshToken"}"""
                            .toRequestBody("application/json".toMediaType()))
                        .build()
                    val refreshResp = client.newCall(refreshRequest).execute()
                    if (refreshResp.isSuccessful) {
                        val body = refreshResp.body?.string() ?: return@runBlocking null
                        com.google.gson.JsonParser.parseString(body)
                            ?.asJsonObject?.get("accessToken")?.asString
                    } else null
                } catch (_: Exception) {
                    null
                }
            }

            if (newAccessToken != null) {
                runBlocking { tokenDataStore.updateAccessToken(newAccessToken) }
                val retryRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $newAccessToken")
                    .build()
                return chain.proceed(retryRequest)
            } else {
                runBlocking { tokenDataStore.clearTokens() }
            }
        }

        return response
    }
}
