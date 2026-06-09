package com.maka.flymoney.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.maka.flymoney.BuildConfig
import com.maka.flymoney.data.remote.model.BetResponse
import com.maka.flymoney.data.remote.model.CashoutResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameServerClient @Inject constructor(
    private val client: OkHttpClient,
    private val auth: FirebaseAuth,
    private val gson: Gson
) {
    private val baseUrl = "${BuildConfig.SERVER_URL}/api"

    suspend fun placeBet(amount: Double, autoCashoutAt: Double?): Result<BetResponse> {
        return try {
            val bodyMap = mutableMapOf<String, Any>("amount" to amount)
            autoCashoutAt?.let { bodyMap["autoCashoutAt"] = it }
            
            val requestBody = gson.toJson(bodyMap).toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$baseUrl/bet")
                .post(requestBody)
                .addHeader("Authorization", getAuthHeader())
                .build()

            makeRequest(request, BetResponse::class.java)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cashOut(): Result<CashoutResponse> {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/cashout")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", getAuthHeader())
                .build()

            makeRequest(request, CashoutResponse::class.java)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getAuthHeader(): String {
        // Force refresh the token (true) to ensure the session is active.
        // This is a common fix for gRPC 'unauthenticated' (Status 16) errors.
        val token = auth.currentUser?.getIdToken(true)?.await()?.token
            ?: throw Exception("User not authenticated")
        return "Bearer $token"
    }

    private suspend fun <T> makeRequest(request: Request, responseClass: Class<T>): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            
            if (response.isSuccessful && body != null) {
                Result.success(gson.fromJson(body, responseClass))
            } else {
                // Try to extract a clean error message from the server response
                val errorMessage = try {
                    val errorMap = gson.fromJson(body, Map::class.java)
                    errorMap["error"]?.toString() ?: "Server Error: ${response.code}"
                } catch (e: Exception) {
                    body ?: "Server Error: ${response.code}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}
