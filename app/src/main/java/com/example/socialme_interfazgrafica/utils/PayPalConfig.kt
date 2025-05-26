package com.example.socialme_interfazgrafica.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.paypal.android.sdk.payments.PayPalConfiguration
import java.util.*

object PayPalConfig {
    private const val TAG = "PayPalConfig"
    private var properties: Properties? = null
    private var isInitialized = false
    private var initError: String? = null

    /**
     * Inicializa la configuración de PayPal cargando las credenciales desde assets
     */
    fun init(context: Context) {
        Log.d(TAG, "=== Iniciando configuración de PayPal ===")

        if (!isInitialized) {
            properties = Properties()
            try {
                Log.d(TAG, "Cargando paypal-config.properties...")
                val inputStream = context.assets.open("paypal-config.properties")
                properties!!.load(inputStream)
                inputStream.close()

                // Validar credenciales
                validateCredentials()

                isInitialized = true
                Log.d(TAG, "✅ PayPal configurado exitosamente")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando configuración PayPal", e)
                initError = "Error cargando configuración: ${e.message}"

                // Configuración por defecto para evitar crashes
                setDefaultProperties()
                isInitialized = true
            }
        } else {
            Log.d(TAG, "PayPal ya está inicializado")
        }
    }

    /**
     * Valida que las credenciales sean correctas
     */
    private fun validateCredentials() {
        val clientId = getClientId()
        val environment = getEnvironment()

        Log.d(TAG, "Validando credenciales...")
        Log.d(TAG, "Client ID: ${clientId.take(20)}... (${clientId.length} chars)")
        Log.d(TAG, "Environment: $environment")

        when {
            clientId.isEmpty() -> {
                initError = "Client ID está vacío"
                Log.e(TAG, "❌ $initError")
            }
            clientId.contains("Your_PayPal_Client_ID_Here") -> {
                initError = "Client ID es un placeholder"
                Log.e(TAG, "❌ $initError")
            }
            clientId.contains("demo_client_id") -> {
                initError = "Client ID es de demostración"
                Log.e(TAG, "❌ $initError")
            }
            clientId.length < 60 -> {
                initError = "Client ID muy corto (${clientId.length} chars)"
                Log.e(TAG, "❌ $initError")
            }
            !clientId.startsWith("A") -> {
                initError = "Client ID tiene formato inválido (debe empezar con 'A')"
                Log.e(TAG, "❌ $initError")
            }
            else -> {
                Log.d(TAG, "✅ Client ID válido")
            }
        }
    }

    /**
     * Establece propiedades por defecto para evitar crashes
     */
    private fun setDefaultProperties() {
        properties = Properties().apply {
            setProperty("PAYPAL_APIKEY_PUBLICA", "INVALID_CLIENT_ID_FOR_DEMO")
            setProperty("PAYPAL_MODE", "sandbox")
        }
    }

    /**
     * Obtiene el Client ID de PayPal
     */
    fun getClientId(): String {
        val clientId = properties?.getProperty("PAYPAL_APIKEY_PUBLICA", "")?.trim() ?: ""
        return clientId.ifEmpty { "INVALID_CLIENT_ID_FOR_DEMO" }
    }

    /**
     * Obtiene el ambiente de PayPal (sandbox/live)
     */
    fun getEnvironment(): String {
        return properties?.getProperty("PAYPAL_MODE", "sandbox") ?: "sandbox"
    }

    /**
     * Verifica si está en modo sandbox
     */
    fun isSandbox(): Boolean = getEnvironment() == "sandbox"

    /**
     * Verifica si hay errores de configuración
     */
    fun hasError(): Boolean = initError != null

    /**
     * Obtiene el mensaje de error si existe
     */
    fun getError(): String? = initError

    /**
     * Verifica si la configuración es válida
     */
    fun isValidConfig(): Boolean {
        val clientId = getClientId()
        val isValid = !hasError() &&
                !clientId.contains("INVALID_CLIENT_ID") &&
                !clientId.contains("demo_client_id") &&
                !clientId.contains("Your_PayPal_Client_ID_Here") &&
                clientId.isNotEmpty() &&
                clientId.length >= 60 &&
                clientId.startsWith("A")

        Log.d(TAG, "Config válido: $isValid")
        return isValid
    }

    /**
     * Crea una configuración de PayPal optimizada
     */
    fun createPayPalConfiguration(): PayPalConfiguration {
        Log.d(TAG, "Creando configuración de PayPal...")

        val environment = if (isSandbox()) {
            Log.d(TAG, "Usando ambiente SANDBOX")
            PayPalConfiguration.ENVIRONMENT_SANDBOX
        } else {
            Log.d(TAG, "Usando ambiente PRODUCTION")
            PayPalConfiguration.ENVIRONMENT_PRODUCTION
        }

        val config = PayPalConfiguration()
            .environment(environment)
            .clientId(getClientId())
            .acceptCreditCards(true)
            .merchantName("SocialMe")
            .merchantPrivacyPolicyUri(Uri.parse("https://socialme.app/privacy"))
            .merchantUserAgreementUri(Uri.parse("https://socialme.app/terms"))
            .languageOrLocale(Locale.getDefault().toString())
            .defaultUserEmail("sb-test@example.com") // Email de prueba para sandbox
            .defaultUserPhoneCountryCode("34") // España

        Log.d(TAG, "✅ Configuración PayPal creada")
        return config
    }

    /**
     * Limpia la configuración (para testing)
     */
    fun reset() {
        properties = null
        isInitialized = false
        initError = null
    }
}