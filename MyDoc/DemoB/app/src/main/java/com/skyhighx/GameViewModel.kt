package com.skyhighx

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class GameStatus {
    IDLE, RUNNING, CRASHED
}

data class ChatMessage(val user: String, val message: String, val timestamp: Long = System.currentTimeMillis())

class GameViewModel : ViewModel() {
    var multiplier by mutableDoubleStateOf(1.00)
        private set

    var status by mutableStateOf(GameStatus.IDLE)
        private set

    var balance by mutableDoubleStateOf(1000.00)
        private set

    var currentBet by mutableDoubleStateOf(10.00)
        private set

    val history = mutableStateListOf<Double>()
    
    // Chat and Leaderboard states
    private val _chatMessages = mutableStateListOf<ChatMessage>()
    val chatMessages: List<ChatMessage> = _chatMessages
    
    private val firestore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private var gameJob: Job? = null
    private var crashPoint: Double = 0.0

    // Ktor Client for WebSockets
    private val client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    init {
        observeChat()
    }

    private fun observeChat() {
        // Placeholder for Firebase Realtime Database observation
        // database.getReference("chat").addValueEventListener(...)
    }

    fun sendMessage(text: String) {
        val msg = ChatMessage("Player", text)
        _chatMessages.add(msg)
        // database.getReference("chat").push().setValue(msg)
    }

    fun startGame() {
        if (status == GameStatus.RUNNING) return
        if (balance < currentBet) return

        balance -= currentBet
        multiplier = 1.00
        status = GameStatus.RUNNING
        
        // SERVER SIDE SIMULATION VIA WEBSOCKET
        // In a real app, you'd connect to your server here:
        // connectToGameServer()
        
        crashPoint = generateCrashPoint()

        gameJob = viewModelScope.launch {
            while (status == GameStatus.RUNNING) {
                delay(50)
                multiplier += 0.01 * (multiplier * 0.5)
                
                if (multiplier >= crashPoint) {
                    crash()
                }
            }
        }
    }

    private suspend fun connectToGameServer() {
        viewModelScope.launch {
            try {
                client.webSocket(method = HttpMethod.Get, host = "your-game-server.com", port = 8080, path = "/game") {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            // Update multiplier and status based on server message
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cashOut() {
        if (status != GameStatus.RUNNING) return
        
        val winAmount = currentBet * multiplier
        balance += winAmount
        status = GameStatus.IDLE
        gameJob?.cancel()
        history.add(0, multiplier)
        
        // Update leaderboard in Firestore
        updateLeaderboard(winAmount)
    }

    private fun updateLeaderboard(winAmount: Double) {
        val data = hashMapOf("user" to "Player", "win" to winAmount, "multiplier" to multiplier)
        firestore.collection("leaderboard").add(data)
    }

    private fun crash() {
        status = GameStatus.CRASHED
        gameJob?.cancel()
        history.add(0, multiplier)
    }

    private fun generateCrashPoint(): Double {
        val r = Random.nextDouble()
        return if (r < 0.05) 1.00 else 1.0 + (Random.nextDouble() * 5.0)
    }
    
    fun updateBet(amount: Double) {
        if (status == GameStatus.IDLE || status == GameStatus.CRASHED) {
            currentBet = amount
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
