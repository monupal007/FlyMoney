package com.maka.flymoney.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.maka.flymoney.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameWebSocketClient @Inject constructor(
    private val auth: FirebaseAuth,
    private val gson: Gson
) {
    private var client: WebSocketClient? = null
    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val serverUri = URI(BuildConfig.SERVER_URL.replace("http", "ws"))

    sealed class GameEvent {
        data class Tick(val multiplier: Double) : GameEvent()
        data class Crashed(val crashAt: Double) : GameEvent()
        data class Waiting(val countdownSec: Int) : GameEvent()
        data class BetPlaced(val uid: String, val username: String, val amount: Double) : GameEvent()
        data class CashedOut(val uid: String, val username: String, val multiplier: Double, val winnings: Double) : GameEvent()
        data class ChatReceived(val uid: String, val username: String, val message: String, val timestamp: Long) : GameEvent()
        object Disconnected : GameEvent()
        object Connected : GameEvent()
        object AuthSuccess : GameEvent()
    }

    fun connect() {
        if (client?.isOpen == true) return

        client = object : WebSocketClient(serverUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                scope.launch {
                    _events.emit(GameEvent.Connected)
                    authenticate()
                }
            }

            override fun onMessage(message: String?) {
                message?.let { parseMessage(it) }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                scope.launch {
                    _events.emit(GameEvent.Disconnected)
                    delay(2000) // Reconnect delay
                    // Explicitly call the outer class connect() to create a new client instance
                    this@GameWebSocketClient.connect()
                }
            }

            override fun onError(ex: Exception?) {
                ex?.printStackTrace()
            }
        }
        client?.connect()
    }

    private suspend fun authenticate() {
        try {
            val token = auth.currentUser?.getIdToken(false)?.await()?.token
            token?.let {
                val authMsg = mapOf("type" to "AUTH", "idToken" to it)
                client?.send(gson.toJson(authMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseMessage(message: String) {
        scope.launch {
            try {
                val map = gson.fromJson(message, Map::class.java)
                when (map["type"]) {
                    "TICK" -> _events.emit(GameEvent.Tick((map["multiplier"] as Number).toDouble()))
                    "CRASHED" -> _events.emit(GameEvent.Crashed((map["crashAt"] as Number).toDouble()))
                    "WAITING" -> _events.emit(GameEvent.Waiting((map["countdownSec"] as Number).toInt()))
                    "BET" -> _events.emit(GameEvent.BetPlaced(
                        map["uid"] as String,
                        map["username"] as String,
                        (map["amount"] as Number).toDouble()
                    ))
                    "CASHOUT" -> _events.emit(GameEvent.CashedOut(
                        map["uid"] as String,
                        map["username"] as String,
                        (map["multiplier"] as Number).toDouble(),
                        (map["winnings"] as Number).toDouble()
                    ))
                    "CHAT" -> _events.emit(GameEvent.ChatReceived(
                        map["uid"] as String,
                        map["username"] as String,
                        map["message"] as String,
                        (map["timestamp"] as Number).toLong()
                    ))
                    "AUTH_SUCCESS" -> _events.emit(GameEvent.AuthSuccess)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendChat(message: String) {
        val chatMsg = mapOf("type" to "CHAT", "message" to message)
        client?.send(gson.toJson(chatMsg))
    }

    fun disconnect() {
        client?.close()
    }
}
