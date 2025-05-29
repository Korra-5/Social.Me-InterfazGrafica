package com.example.socialme_interfazgrafica.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class PayPalSimulationService {

    companion object {
        private const val TAG = "PayPalSimulation"
        // CAMBIA ESTA URL POR LA DE TU SERVIDOR
        private const val BASE_URL = "http://10.0.2.2:8080/api/payment" // Para emulador
        // Para dispositivo real: "http://TU_IP:8080/api/payment"

        private val client = OkHttpClient()
    }

    suspend fun simulatePremiumPurchase(
        username: String,
        amount: String,
        token: String
    ): PurchaseResult = withContext(Dispatchers.IO) {

        Log.d(TAG, "=== Iniciando simulación de compra ===")
        Log.d(TAG, "Usuario: $username, Cantidad: €$amount")

        try {
            val requestJson = JSONObject().apply {
                put("username", username)
                put("amount", amount)
            }

            val requestBody = requestJson.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/simulate-purchase")
                .post(requestBody)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "Response: $responseBody")

            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                val success = json.getBoolean("success")

                if (success) {
                    val orderId = json.optString("orderId", "")
                    val message = json.optString("message", "Compra exitosa")
                    Log.d(TAG, "✅ Simulación exitosa - Order ID: $orderId")
                    return@withContext PurchaseResult.Success(orderId, message)
                } else {
                    val errorMessage = json.optString("message", "Error desconocido")
                    Log.e(TAG, "❌ Error del servidor: $errorMessage")
                    return@withContext PurchaseResult.Error(errorMessage)
                }
            } else {
                Log.e(TAG, "❌ Error HTTP: ${response.code}")
                return@withContext PurchaseResult.Error("Error de conexión: ${response.code}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción en simulación", e)
            return@withContext PurchaseResult.Error("Error: ${e.message}")
        }
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/test")
                .get()
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error probando conexión", e)
            false
        }
    }
}

sealed class PurchaseResult {
    data class Success(val orderId: String, val message: String) : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}