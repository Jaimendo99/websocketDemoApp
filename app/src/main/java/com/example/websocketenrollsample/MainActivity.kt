package com.example.websocketenrollsample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.websocketenrollsample.api.CellKtorClient
import com.example.websocketenrollsample.api.DeviceEnroll
import com.example.websocketenrollsample.api.DeviceState
import com.example.websocketenrollsample.api.EnrollmentBody
import com.example.websocketenrollsample.api.WebSocketClient
import com.example.websocketenrollsample.api.WsMessageIn
import com.example.websocketenrollsample.ui.theme.WebSocketEnrollSampleTheme
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.serialization.WebsocketConverterNotFoundException
import io.ktor.serialization.WebsocketDeserializeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val client = WebSocketClient()
        val ktorClient = CellKtorClient()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebSocketEnrollSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        WebSocketConnection(client, ktorClient, Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun WebSocketConnection(
    webSocketClient: WebSocketClient,
    client: CellKtorClient,
    modifier: Modifier = Modifier
) {
    var code by remember { mutableStateOf("") }
    val messages by webSocketClient.messages.collectAsState()
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
    ) {
        messages.forEach { message ->
            Text(message)
        }
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Enter code") },
            enabled = !isProcessing
        )
        Button(
            onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    isProcessing = true
                    processEvents(webSocketClient, client, code)
                    isProcessing = false
                }
            },
            enabled = !isProcessing
        ) {
            Text(if (isProcessing) "Processing..." else "Enter")
        }
    }
}

suspend fun processEvents(client: WebSocketClient,ktorClient: CellKtorClient, code: String) {
    val events = listOf(
        "installation_complete",
        "enrollment_start",
        "enrollment_complete",
        "policy_review",
        "ready"
    )

    for (event in events) {
        when (event) {
            "installation_complete" -> handleInstallationComplete(client, code)
            "enrollment_start" -> handleEnrollmentStart(client, code)
            "enrollment_complete" -> handleEnrollmentComplete(client, ktorClient, code)
            "policy_review" -> handlePolicyReview(client, code)
            "ready" -> handleReady(client, code)
        }
    }
}

suspend fun handleInstallationComplete(client: WebSocketClient, code: String) {
    // Add any specific logic for installation complete here
    Log.d("MainActivity", "Installation Complete Action")
    client.sendSuccessMessage(code, "installation_complete") {
        runBlocking {
            try {
                val message = it.incoming.tryReceive().getOrNull()
                Log.d("WebSocket", "Received message: ${message}")
                val wsMessageIn: WsMessageIn = it.receiveDeserialized()
                if (wsMessageIn.data.token == null) {
                    client.addMessage("Token is null")
                } else {
                    client.addMessage("token: " + wsMessageIn.data.token)
                }
                Log.d("WebSocket", "Received message: $wsMessageIn")
            } catch (e: WebsocketDeserializeException) {
                client.addMessage("WebsocketDeserializeException: ${e.message}")
                Log.e("WebSocket", "$e: ${e.message}")
            } catch (e: WebsocketConverterNotFoundException) {
                client.addMessage("WebsocketConverterNotFoundException: ${e.message}")
                Log.e("WebSocket", "$e: ${e.message}")
            } catch (e: Exception) {
                client.addMessage("Error during installation_complete: ${e.message}")
                Log.e("WebSocket", "$e: ${e.message}")
            }
        }
    }.onSuccess {
        client.addMessage("Success during installation_complete")
    }.onFailure { e -> client.addMessage("Error during installation_complete: ${e.message}") }
}

suspend fun handleEnrollmentStart(client: WebSocketClient, code: String) {
    // Add any specific logic for enrollment start here
    Log.d("MainActivity", "Enrolling Start Action")
    client.sendSuccessMessage(code, "enrollment_start")
        .onSuccess { client.addMessage("Success during enrollment_start") }
        .onFailure { e ->
            client.addMessage("Error during enrollment_start: ${e.message}")
        }
}

suspend fun handleEnrollmentComplete( client: WebSocketClient, ktorClient: CellKtorClient, code: String ) {
    // Add any specific logic for enrollment complete here
    Log.d("MainActivity", "Enrolling Complete Action")
    val response = ktorClient.enrollDevice(
        EnrollmentBody(
            planID = code, enrollID = "enrollID",
            device = DeviceEnroll(
                brand = "brand", model = "model", tokenFirebaseID = "tokenFirebaseID"
            ),
            state = DeviceState(simCard = 1, locked = false, active = true)
        )
    )

    client.addMessage("Response: ${response.response.message}, ${response.response.status_code}")

    client.sendSuccessMessage(code, "enrollment_complete")
        .onSuccess { client.addMessage("Success during enrollment_complete") }
        .onFailure { e ->
            client.addMessage("Error during enrollment_complete: ${e.message}")
        }
}

suspend fun handlePolicyReview(client: WebSocketClient, code: String) {
    // Add any specific logic for policy review here
    client.sendSuccessMessage(code, "policy_review")
        .onSuccess { client.addMessage("Success during policy_review") }
        .onFailure { e ->
            client.addMessage("Error during policy_review: ${e.message}")
        }
}

suspend fun handleReady(client: WebSocketClient, code: String) {
    // Add any specific logic for ready state here
    client.sendSuccessMessage(code, "ready")
        .onSuccess { client.addMessage("Success during ready") }
        .onFailure { e ->
            client.addMessage("Error during ready: ${e.message}")
        }
}