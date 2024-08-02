package com.example.websocketenrollsample.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID.randomUUID

class WebSocketClient {

    private val TAG = "WebSocketClient"

    private val url = "api-cellarius-go.onrender.com"
    private val path = "/ws"
    private val wsId = randomWsId(5)


    private val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 1000
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    suspend fun connect(code: String): String {
        client.webSocket(
            method = HttpMethod.Get,
            host = url,
            path = "$path?code=$wsId"
        ) {
            sendSerialized(WsMessage(code, "installation_complete", WsData("Connected", true)))
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val message = frame.readText()
                Log.d(TAG, "Received: $message")
//                TODO: set planId
            }
        }
        return "Connected"
    }

    suspend fun sendMessage(wsMessage: WsMessage, block: suspend (String) -> Unit = {}) {
        client.webSocket(
            method = HttpMethod.Get,
            host = url,
            path = "$path?code=${wsId}"
        ) {
            sendSerialized(wsMessage)
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

fun randomWsId(digits : Int) : String{
    val uuid = randomUUID().toString().replace("-", "")
    return uuid.substring(0, digits)
}