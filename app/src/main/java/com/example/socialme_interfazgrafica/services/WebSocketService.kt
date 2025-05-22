// services/WebSocketService.kt
package com.example.socialme_interfazgrafica.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.socialme_interfazgrafica.model.NotificacionDTO
import com.example.socialme_interfazgrafica.utils.WebSocketManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebSocketService : Service() {
    private var webSocketManager: WebSocketManager? = null
    private val gson = Gson()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val username = intent?.getStringExtra("username") ?: return START_NOT_STICKY

        if (webSocketManager == null) {
            webSocketManager = WebSocketManager("ws://tuservidorbackend.com", username)
            webSocketManager?.addOnMessageCallback { mensaje ->
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val notificacion = gson.fromJson(mensaje, NotificacionDTO::class.java)
                        NotificacionService.showNotification(applicationContext, notificacion)
                    } catch (e: Exception) {
                        // Manejar errores
                    }
                }
            }
            webSocketManager?.connect()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        webSocketManager?.disconnect()
    }
}