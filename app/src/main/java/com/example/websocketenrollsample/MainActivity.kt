package com.example.websocketenrollsample

import android.content.Context
import android.os.Bundle
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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.websocketenrollsample.api.WebSocketClient
import com.example.websocketenrollsample.ui.theme.WebSocketEnrollSampleTheme
import io.ktor.client.plugins.api.createClientPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val client = WebSocketClient()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebSocketEnrollSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier
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
fun WebSocketConnection(client:WebSocketClient ){
    var code by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Enter code") })
        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                client.connect(code)
            }
        }) {
           Text("Enter" )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WebSocketEnrollSampleTheme {
        Greeting("Android")
    }
}