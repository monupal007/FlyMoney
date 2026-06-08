package com.dimts.gmcblChatBot.ui.screen.chat

import android.util.Log
import com.dimts.gmcblChatBot.di.ChatApiService
import com.dimts.gmcblChatBot.utils.Utils
import org.json.JSONObject

class ChatRepositoryImpl(
    private val apiService: ChatApiService
) : ChatRepository {

    override suspend fun getBotReply(message: String, deviceId: String): String {
        return try {
            val jsonObject = JSONObject()
            jsonObject.put("text", message)
            jsonObject.put("device_id", deviceId)

            // Encrypt the JSON data
            val encryptedData = Utils.getEncryptionNextBus(jsonObject.toString()) ?: ""
            Log.d("ChatRepository", "API Request: $encryptedData")

            val response = apiService.getBotReply(encryptedData)

            if (response.isSuccessful) {
                val responseString = response.body()?.string()?.trim() ?: ""
                Log.d("ChatRepository", "Raw API Response: $responseString")

                // The response is now a direct encrypted string, not wrapped in JSON
                val decryptedResponse = Utils.getDecryptionNextBus(responseString) ?: ""
                Log.d("ChatRepository", "Decrypted Response: $decryptedResponse")

                decryptedResponse
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("ChatRepository", "API Error: $errorBody")
                "Error: Server returned ${response.code()}"
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error hitting API", e)
            "Error: Could not connect to server."
        }
    }
}
