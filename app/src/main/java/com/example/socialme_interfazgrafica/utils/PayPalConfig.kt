package com.example.socialme_interfazgrafica.utils

import android.content.Context
import java.util.Properties

object PayPalConfig {
    private var properties: Properties? = null

    fun init(context: Context) {
        if (properties == null) {
            properties = Properties()
            try {
                val inputStream = context.assets.open("paypal-config.properties")
                properties!!.load(inputStream)
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getClientId(): String {
        return properties?.getProperty("PAYPAL_APIKEY_PUBLICA") ?: ""
    }

    fun getEnvironment(): String {
        return properties?.getProperty("PAYPAL_MODE") ?: "sandbox"
    }

    fun isSandbox(): Boolean = getEnvironment() == "sandbox"
}