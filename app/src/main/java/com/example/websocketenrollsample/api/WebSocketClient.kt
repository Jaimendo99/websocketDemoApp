package com.example.websocketenrollsample.api

import android.util.Log
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.util.UUID.randomUUID

class WebSocketClient {
    private val TAG = "WebSocketClient"
    private val webSocketManager = WebSocketManager()

    private val wsId = randomWsId(10)
    private val url = "api-cellarius-go.onrender.com"
    private val path = "/ws"

    suspend fun sendSuccessMessage(destUid: String, event: String): Result<Unit> {
        var result: Result<Unit> = Result.success(Unit)

        webSocketManager.connectAndSend(url, path, wsId) { session ->
            runBlocking {
                try {
                    session.sendSerialized(WsMessage(destUid, event, WsData("Success", true)))
                    Log.d(TAG, "Sent message: $destUid, $event")
                } catch (e: Exception) {
                    Log.e(TAG, "Error: ${e.message}")
                    result = Result.failure(e)
                }
            }
        }
        return result
    }


    suspend fun sendErrorMessage(destUid: String, event: String) {
        webSocketManager.connectAndSend(url, path, wsId) { session ->
            runBlocking {
                session.sendSerialized(WsMessage(destUid, event, WsData("Error", false)))
                Log.d(TAG, "Sent message: $destUid, $event")
            }
        }
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