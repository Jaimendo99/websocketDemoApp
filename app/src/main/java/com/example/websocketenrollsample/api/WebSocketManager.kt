package com.example.websocketenrollsample.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class WebSocketManager {
    private val TAG = "WebSocketManager"

    private val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 1000
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    private val _incomingMessages = MutableSharedFlow<String>()
    val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    suspend fun connectAndSend(
        url: String,
        path: String,
        wsId: String,
        body: Any? = null,
        block: suspend (DefaultClientWebSocketSession) -> Unit
    ): Result<Unit> {
        return try {
            client.webSocket(
                method = HttpMethod.Get,
                host = url,
                path = "$path?code=$wsId"
            ) {
                Log.d(TAG, "Connected")

                if (body != null) {
                    try {
                        sendSerialized(body)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error: ${e.message}")
                        return@webSocket
                    }
                    Log.d(TAG, "Sent: $body")
                }

                // Execute the provided block
                block(this)

                // Cancel the receive job when we're done
                Log.d(TAG, "Connection closed")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun DefaultClientWebSocketSession.receiverListener() {
        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        Log.d(TAG, "Received: $text")
                        _incomingMessages.emit(text)
                    }
                    is Frame.Binary -> Log.d(TAG, "Received binary frame")
                    is Frame.Close -> Log.d(TAG, "Received close frame")
                    is Frame.Ping -> Log.d(TAG, "Received ping frame")
                    is Frame.Pong -> Log.d(TAG, "Received pong frame")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in receiverListener: ${e.message}")
        } finally {
            Log.d(TAG, "Incoming closed")
        }
    }
}