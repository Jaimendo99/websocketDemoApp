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
                            .padding(innerPadding)
                    ) {
                        WebSocketConnection(client)
                    }
                }
            }
        }
    }
}

@Composable
fun WebSocketConnection(client: WebSocketClient) {
    var code by remember { mutableStateOf("") }
    var messages: List<String> by remember { mutableStateOf(emptyList()) }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        messages.forEach { message ->
            Text(message)
        }
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Enter code") }
        )
        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                client.sendSuccessMessage(code, "installation_complete").onSuccess {
                    messages = messages + "connected"
                }.onFailure { e ->
                    messages = messages + "Error during installation_complete: ${e.message}"
                }

                client.sendSuccessMessage(code, "enrollment_start").onSuccess {
                    messages = messages + "enrollment_start"
                }.onFailure { e ->
                    messages = messages + "Error during enrollment_start: ${e.message}"
                }

                client.sendSuccessMessage(code, "enrollment_complete").onSuccess {
                    messages = messages + "enrollment_complete"
                }.onFailure { e ->
                    messages = messages + "Error during enrollment_complete: ${e.message}"
                }

                client.sendSuccessMessage(code, "policy_review").onSuccess {
                    messages = messages + "policy_review"
                }.onFailure { e ->
                    messages = messages + "Error during policy_review: ${e.message}"
                }

                client.sendSuccessMessage(code, "ready").onSuccess {
                    messages = messages + "ready"
                }.onFailure { e ->
                    messages = messages + "Error during ready: ${e.message}"
                }
            }
        }) {
            Text("Enter")
        }
    }
}