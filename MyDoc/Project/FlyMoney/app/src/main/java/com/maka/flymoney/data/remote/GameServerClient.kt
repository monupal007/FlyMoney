package com.maka.flymoney.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.maka.flymoney.BuildConfig
import com.maka.flymoney.data.remote.model.BetResponse
import com.maka.flymoney.data.remote.model.CashoutResponse
import kotlinx.coroutines.tasks.await
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
        val bodyMap = mutableMapOf<String, Any>("amount" to amount)
        autoCashoutAt?.let { bodyMap["autoCashoutAt"] = it }
        
        val requestBody = gson.toJson(bodyMap).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$baseUrl/bet")
            .post(requestBody)
            .addHeader("Authorization", getAuthHeader())
            .build()

        return makeRequest(request, BetResponse::class.java)
    }

    suspend fun cashOut(): Result<CashoutResponse> {
        val request = Request.Builder()
            .url("$baseUrl/cashout")
            .post("{}".toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", getAuthHeader())
            .build()

        return makeRequest(request, CashoutResponse::class.java)
    }

    private suspend fun getAuthHeader(): String {
        val token = auth.currentUser?.getIdToken(false)?.await()?.token
            ?: throw Exception("User not authenticated")
        return "Bearer $token"
    }

    private suspend fun <T> makeRequest(request: Request, responseClass: Class<T>): Result<T> {
        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && body != null) {
                Result.success(gson.fromJson(body, responseClass))
            } else {
                Result.failure(Exception(body ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
