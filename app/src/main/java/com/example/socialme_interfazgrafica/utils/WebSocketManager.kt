// utils/WebSocketManager.kt
package com.example.socialme_interfazgrafica.utils

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URISyntaxException

class WebSocketManager(private val serverUrl: String, private val username: String) {

    private var webSocketClient: WebSocketClient? = null
    private var onMessageCallbacks = mutableListOf<(String) -> Unit>()
    private var isConnected = false

    fun connect() {
        try {
            Log.d("WebSocketManager", "Intentando conectar a: $serverUrl para usuario: $username")

            val uri = URI(serverUrl)
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    isConnected = true
                    Log.d("WebSocketManager", "✅ Conexión WebSocket establecida exitosamente")
                    Log.d("WebSocketManager", "Código de estado: ${handshakedata?.httpStatus}, " +
                            "Mensaje: ${handshakedata?.httpStatusMessage}")

                    subscribeToUserNotifications()
                }

                override fun onMessage(message: String?) {
                    message?.let {
                        Log.d("WebSocketManager", "📩 Mensaje recibido: $it")
                        onMessageCallbacks.forEach { callback ->
                            try {
                                callback(it)
                                Log.d("WebSocketManager", "Callback ejecutado exitosamente para mensaje")
                            } catch (e: Exception) {
                                Log.e("WebSocketManager", "❌ Error al procesar mensaje en callback: ${e.message}", e)
                            }
                        }
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    isConnected = false
                    Log.d("WebSocketManager", "🔴 Conexión WebSocket cerrada. " +
                            "Código: $code, Razón: $reason, Iniciado por servidor: $remote")
                }

                override fun onError(ex: Exception?) {
                    Log.e("WebSocketManager", "❌ Error WebSocket: ${ex?.message}", ex)

                    // Intentar reconectar en caso de error
                    if (isConnected) {
                        isConnected = false
                        Log.d("WebSocketManager", "Intentando reconectar en 5 segundos...")

                        // Esperar 5 segundos antes de intentar reconectar
                        Thread {
                            try {
                                Thread.sleep(5000)
                                reconnect()
                            } catch (e: InterruptedException) {
                                Log.e("WebSocketManager", "Interrupción durante espera para reconexión", e)
                            }
                        }.start()
                    }
                }
            }

            webSocketClient?.connectionLostTimeout = 30 // Segundos antes de detectar desconexión
            webSocketClient?.connect()
            Log.d("WebSocketManager", "Solicitud de conexión enviada, esperando...")

        } catch (e: URISyntaxException) {
            Log.e("WebSocketManager", "❌ Error de URI inválida: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("WebSocketManager", "❌ Error general al conectar WebSocket: ${e.message}", e)
        }
    }

    private fun subscribeToUserNotifications() {
        try {
            Log.d("WebSocketManager", "Suscribiendo a notificaciones para usuario: $username")

            val subscribeMessage = """
                {
                    "destination": "/app/subscribe",
                    "body": "{\"username\": \"$username\"}"
                }
            """.trimIndent()

            Log.d("WebSocketManager", "Enviando mensaje de suscripción: $subscribeMessage")
            webSocketClient?.send(subscribeMessage)
            Log.d("WebSocketManager", "✅ Mensaje de suscripción enviado correctamente")

            // Enviar un mensaje de prueba
            testConnection()

        } catch (e: Exception) {
            Log.e("WebSocketManager", "❌ Error al suscribirse a notificaciones: ${e.message}", e)
        }
    }

    private fun testConnection() {
        try {
            val testMessage = """
                {
                    "destination": "/app/test",
                    "body": "{\"username\": \"$username\", \"message\": \"test connection\"}"
                }
            """.trimIndent()

            Log.d("WebSocketManager", "Enviando mensaje de prueba")
            webSocketClient?.send(testMessage)
        } catch (e: Exception) {
            Log.e("WebSocketManager", "Error al enviar mensaje de prueba", e)
        }
    }

    private fun reconnect() {
        if (!isConnected && webSocketClient?.isClosed == true) {
            Log.d("WebSocketManager", "Intentando reconectar WebSocket...")
            connect()
        }
    }

    fun addOnMessageCallback(callback: (String) -> Unit) {
        Log.d("WebSocketManager", "Añadiendo nuevo callback para mensajes")
        onMessageCallbacks.add(callback)
    }

    fun disconnect() {
        Log.d("WebSocketManager", "Desconectando WebSocket...")
        try {
            isConnected = false
            webSocketClient?.close()
            Log.d("WebSocketManager", "WebSocket desconectado correctamente")
        } catch (e: Exception) {
            Log.e("WebSocketManager", "Error al desconectar WebSocket: ${e.message}", e)
        }
    }

    fun isConnected(): Boolean {
        val connectionStatus = webSocketClient?.isOpen == true
        Log.d("WebSocketManager", "Estado de conexión: ${if (connectionStatus) "Conectado" else "Desconectado"}")
        return connectionStatus
    }

    fun getConnectionInfo(): String {
        return "URL: $serverUrl, Usuario: $username, Conectado: ${isConnected()}"
    }
}