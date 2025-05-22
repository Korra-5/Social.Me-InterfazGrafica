// viewModel/NotificacionViewModel.kt
package com.example.socialme_interfazgrafica.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.NotificacionDTO
import com.example.socialme_interfazgrafica.utils.WebSocketManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

class NotificacionViewModel : ViewModel() {

    private val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var notificaciones by mutableStateOf<List<NotificacionDTO>>(emptyList())
        private set

    var numeroNoLeidas by mutableStateOf(0L)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var webSocketManager: WebSocketManager? = null
    private val gson = Gson()

    fun inicializarWebSocket(username: String) {
        webSocketManager = WebSocketManager("https://social-me-tfg.onrender.com", username)
        webSocketManager?.addOnMessageCallback { mensaje ->
            // Procesar la notificación recibida
            try {
                val notificacion = gson.fromJson(mensaje, NotificacionDTO::class.java)
                // Actualizar la lista de notificaciones
                notificaciones = listOf(notificacion) + notificaciones
                // Incrementar contador de no leídas
                numeroNoLeidas++

                Log.d("NotificacionViewModel", "Nueva notificación recibida: ${notificacion.titulo}")
            } catch (e: Exception) {
                errorMessage = "Error al procesar notificación: ${e.message}"
                Log.e("NotificacionViewModel", "Error al procesar notificación", e)
            }
        }
        webSocketManager?.connect()
    }

    fun cargarNotificaciones(username: String, authToken: String) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val response = apiService.obtenerNotificaciones(authToken, username)
                if (response.isSuccessful) {
                    notificaciones = response.body() ?: emptyList()
                    Log.d("NotificacionViewModel", "Notificaciones cargadas: ${notificaciones.size}")
                } else {
                    errorMessage = "Error al cargar notificaciones: ${response.message()}"
                    Log.e("NotificacionViewModel", "Error al cargar notificaciones: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("NotificacionViewModel", "Error de conexión al cargar notificaciones", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun contarNoLeidas(username: String, authToken: String) {
        viewModelScope.launch {
            try {
                val response = apiService.contarNoLeidas(authToken, username)
                if (response.isSuccessful) {
                    numeroNoLeidas = response.body() ?: 0
                    Log.d("NotificacionViewModel", "Notificaciones no leídas: $numeroNoLeidas")
                } else {
                    Log.e("NotificacionViewModel", "Error al contar no leídas: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NotificacionViewModel", "Error al contar notificaciones no leídas", e)
            }
        }
    }

    fun marcarComoLeida(notificacionId: String, authToken: String) {
        viewModelScope.launch {
            try {
                val response = apiService.marcarComoLeida(authToken, notificacionId)
                if (response.isSuccessful) {
                    // Actualizar la lista de notificaciones
                    notificaciones = notificaciones.map {
                        if (it._id == notificacionId) it.copy(leida = true) else it
                    }
                    // Actualizar contador
                    if (numeroNoLeidas > 0) numeroNoLeidas--

                    Log.d("NotificacionViewModel", "Notificación marcada como leída: $notificacionId")
                } else {
                    errorMessage = "Error al marcar como leída: ${response.message()}"
                    Log.e("NotificacionViewModel", "Error al marcar como leída: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "Error al marcar como leída: ${e.message}"
                Log.e("NotificacionViewModel", "Error al marcar como leída", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Desconectar WebSocket al destruir el ViewModel
        webSocketManager?.disconnect()
        Log.d("NotificacionViewModel", "WebSocket desconectado")
    }
}