package com.example.websocketenrollsample.api

import android.util.Log
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.util.UUID.randomUUID

class WebSocketClient {
    private val TAG = "WebSocketClient"
    private val webSocketManager = WebSocketManager()

    private val wsId = randomWsId(10)
    private val url = "api-cellarius-go.onrender.com"
    private val path = "/ws"

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            webSocketManager.incomingMessages.collect { message ->
                _messages.update { it + message }
            }
        }
    }

    suspend fun sendSuccessMessage(destUid: String, event: String): Result<Unit> {
        return webSocketManager.connectAndSend(url, path, wsId) { session ->
            try {
                session.sendSerialized(WsMessage(destUid, event, WsData("Success", true)))
                Log.d(TAG, "Sent message: $destUid, $event")
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                throw e
            }
        }
    }

    suspend fun sendErrorMessage(destUid: String, event: String): Result<Unit> {
        return webSocketManager.connectAndSend(url, path, wsId) { session ->
            try {
                session.sendSerialized(WsMessage(destUid, event, WsData("Error", false)))
                Log.d(TAG, "Sent message: $destUid, $event")
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                throw e
            }
        }
    }
    fun addMessage(message: String) {
        _messages.update { it + message }
    }
}


@Serializable
data class WsMessage(
    val dest_uid: String,
    val event: String,
    val data: WsData
)

@Serializable
data class WsData(
    val message: String,
    val status: Boolean
)

fun randomWsId(digits: Int): String {
    val uuid = randomUUID().toString().replace("-", "")
    return uuid.substring(0, digits)
}