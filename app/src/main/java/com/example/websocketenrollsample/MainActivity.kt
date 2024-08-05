package com.example.websocketenrollsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.websocketenrollsample.api.WebSocketClient
import com.example.websocketenrollsample.api.WsData
import com.example.websocketenrollsample.api.WsMessage
import com.example.websocketenrollsample.ui.theme.WebSocketEnrollSampleTheme
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val client = WebSocketClient()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebSocketEnrollSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        WebSocketConnection(client, Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun WebSocketConnection(client: WebSocketClient, modifier: Modifier = Modifier) {
    var code by remember { mutableStateOf("") }
    val messages by client.messages.collectAsState()
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
                    processEvents(client, code)
                    isProcessing = false
                }
            },
            enabled = !isProcessing
        ) {
            Text(if (isProcessing) "Processing..." else "Enter")
        }
    }
}

suspend fun processEvents(client: WebSocketClient, code: String) {
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
            "enrollment_complete" -> handleEnrollmentComplete(client, code)
            "policy_review" -> handlePolicyReview(client, code)
            "ready" -> handleReady(client, code)
        }
    }
}

suspend fun handleInstallationComplete(client: WebSocketClient, code: String) {
    // Add any specific logic for installation complete here
    client.sendSuccessMessage(code, "installation_complete").onFailure { e ->
        client.addMessage("Error during installation_complete: ${e.message}")
    }
}

suspend fun handleEnrollmentStart(client: WebSocketClient, code: String) {
    // Add any specific logic for enrollment start here
    client.sendSuccessMessage(code, "enrollment_start").onFailure { e ->
        client.addMessage("Error during enrollment_start: ${e.message}")
    }
}

suspend fun handleEnrollmentComplete(client: WebSocketClient, code: String) {
    // Add any specific logic for enrollment complete here
    client.sendSuccessMessage(code, "enrollment_complete").onFailure { e ->
        client.addMessage("Error during enrollment_complete: ${e.message}")
    }
}

suspend fun handlePolicyReview(client: WebSocketClient, code: String) {
    // Add any specific logic for policy review here
    client.sendSuccessMessage(code, "policy_review").onFailure { e ->
        client.addMessage("Error during policy_review: ${e.message}")
    }
}

suspend fun handleReady(client: WebSocketClient, code: String) {
    // Add any specific logic for ready state here
    client.sendSuccessMessage(code, "ready").onFailure { e ->
        client.addMessage("Error during ready: ${e.message}")
    }
}