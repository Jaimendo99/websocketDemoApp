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
import kotlinx.serialization.json.Json


class WebSocketManager {
    private val TAG = "WebSocketManager"


    private val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 1000
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    suspend fun connectAndSend(
        url: String,
        path: String,
        wsId: String,
        body: Any? = null,
        block: (DefaultClientWebSocketSession) -> Unit
    ) :Result<Unit>{
        return try {
        client.webSocket(
            method = HttpMethod.Get,
            host = url,
            path = "$path?code=$wsId"
        ) {
            Log.d(TAG, "Connected")
            if (body != null){
                try {
                    sendSerialized(body)
                }catch (e: Exception){
                    Log.e(TAG, "Error: ${e.message}")
                    Result.failure<Exception>(e)
                }
                Log.d(TAG, "Sent: $body")
            }
            block(this)
            Log.d(TAG, "Connection closed")
        }
            Result.success(Unit)
        }catch (e: Exception){
            Log.e(TAG, "Error: ${e.message}")
            Result.failure(e)
        }
    }
}