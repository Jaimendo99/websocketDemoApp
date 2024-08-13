package com.example.websocketenrollsample.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CellKtorClient {
    private val client = HttpClient(OkHttp) {
        defaultRequest {
            url(host = "api-cellarius-go.onrender.com"){
                protocol = URLProtocol.HTTPS
            }
            header("Content-Type", "application/json")
        }

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun enrollDevice(enrollmentBody: EnrollmentBody): ResponseBody {
        try {
            Log.d("CellKtorClient", "Enrolling device")
            return client.post("enroll_device") {
                setBody(enrollmentBody)
            }.body()
        }catch (e: Exception){
            Log.d("CellKtorClient", "Error: ${e.message}")
            return ResponseBody(ResponseMessage("Error", 500), "Error")
        }
    }

    suspend fun sendPolicies(policyCheckResults: PolicyCheckResults): ResponseBody {
        return client.post("policy") {
            setBody(policyCheckResults)
        }.body()
    }

}

@Serializable
data class PolicyCheckResults(
    val code: String,
    val deviceStatus: Boolean,
    val safeMode: Boolean,
    val addUsers: Boolean,
    val usbData: Boolean,
    val debugOptions: Boolean,
    val enableFactoryReset: Boolean,
    val frpAccounts: List<String>,
)

@Serializable
data class ResponseMessage(
    val message: String,
    val status_code: Int
)

@Serializable
data class ResponseBody(
    val response: ResponseMessage,
    val status: String
)


@Serializable
data class EnrollmentBody(
    val planID: String,
    val enrollID: String,
    val device: DeviceEnroll,
    val state: DeviceState,
)

@Serializable
data class DeviceEnroll(
    val brand: String?,
    val model: String?,
    val tokenFirebaseID: String?,
)

@Serializable
data class DeviceState(
    val simCard: Int?,
    val locked: Boolean,
    val active: Boolean,
)