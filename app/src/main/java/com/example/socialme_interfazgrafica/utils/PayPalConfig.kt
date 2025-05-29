package com.example.socialme_interfazgrafica.utils

import android.content.Context
import android.util.Log
import java.util.*

object PayPalConfig {
    private const val TAG = "PayPalConfig"
    private var properties: Properties? = null
    private var isInitialized = false
    private var initError: String? = null

    fun init(context: Context) {
        Log.d(TAG, "=== Iniciando configuración de PayPal ===")

        if (!isInitialized) {
            properties = Properties()
            try {
                Log.d(TAG, "Cargando paypal-config.properties...")
                val inputStream = context.assets.open("paypal-config.properties")
                properties!!.load(inputStream)
                inputStream.close()

                validateCredentials()
                isInitialized = true
                Log.d(TAG, "✅ PayPal configurado exitosamente")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando configuración PayPal", e)
                initError = "Error cargando configuración: ${e.message}"
                setDefaultProperties()
                isInitialized = true
            }
        }
    }

    private fun validateCredentials() {
        val clientId = getClientId()
        Log.d(TAG, "Validando credenciales...")
        Log.d(TAG, "Client ID: ${clientId.take(20)}... (${clientId.length} chars)")

        when {
            clientId.isEmpty() -> {
                initError = "Client ID está vacío"
                Log.e(TAG, "❌ $initError")
            }
            clientId.length < 60 -> {
                initError = "Client ID muy corto"
                Log.e(TAG, "❌ $initError")
            }
            !clientId.startsWith("A") -> {
                initError = "Client ID tiene formato inválido"
                Log.e(TAG, "❌ $initError")
            }
            else -> {
                Log.d(TAG, "✅ Client ID válido")
            }
        }
    }

    private fun setDefaultProperties() {
        properties = Properties().apply {
            setProperty("PAYPAL_CLIENT_ID", "AU0J5DKiu9Nn5r6okm5OKKWHs_jg2buprPFXAMA6eaqCECcECr87KZSThKb_Q7utlkrSAHAyQ3np-3rG")
            setProperty("PAYPAL_CLIENT_SECRET", "EHjO5v8kD8F0J7dG2Rz3QYxU9P6S4A1HfK3LmN8VbC5EwR7T2I9YqU4ZsX6B0N3M")
            setProperty("PAYPAL_MODE", "sandbox")
        }
    }

    fun getClientId(): String {
        return properties?.getProperty("PAYPAL_CLIENT_ID", "")?.trim() ?:
        "AVmrX7JqXI0-4M4FCKz6pSJkBJEqrzodReAKtusGrrZNauuqeidpzfP8BA88S0vvrM57UZDDh2KgiFaZ"
    }

    fun getClientSecret(): String {
        return properties?.getProperty("PAYPAL_CLIENT_SECRET", "")?.trim() ?:
        "EKCRBBT0kWWejTjNL9gF9_PDDWpRcQtT4J1CfHLbt8q1aau0w9rG4w7AwUH1GLr13SbBfqdPjkctsmTl"
    }

    fun getEnvironment(): String {
        return properties?.getProperty("PAYPAL_MODE", "sandbox") ?: "sandbox"
    }

    fun isSandbox(): Boolean = getEnvironment() == "sandbox"

    fun hasError(): Boolean = initError != null

    fun getError(): String? = initError

    fun isValidConfig(): Boolean {
        val clientId = getClientId()
        return clientId.isNotEmpty() &&
                clientId.length >= 60 &&
                clientId.startsWith("A") &&
                !hasError()
    }

    // Configuración para REST API
    fun getBaseUrl(): String {
        return if (isSandbox()) {
            "https://api-m.sandbox.paypal.com"
        } else {
            "https://api-m.paypal.com"
        }
    }

    fun getBasicAuthHeader(): String {
        val credentials = "${getClientId()}:${getClientSecret()}"
        return "Basic " + android.util.Base64.encodeToString(
            credentials.toByteArray(),
            android.util.Base64.NO_WRAP
        )
    }

    fun logConfiguration() {
        Log.d(TAG, "=== Configuración PayPal ===")
        Log.d(TAG, "Ambiente: ${getEnvironment()}")
        Log.d(TAG, "Base URL: ${getBaseUrl()}")
        Log.d(TAG, "Client ID: ${getClientId().take(20)}...")
        Log.d(TAG, "Configuración válida: ${isValidConfig()}")
        if (hasError()) {
            Log.e(TAG, "Error: ${getError()}")
        }
        Log.d(TAG, "========================")
    }
}