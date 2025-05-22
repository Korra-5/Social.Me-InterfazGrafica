package com.example.socialme_interfazgrafica

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialme_interfazgrafica.navigation.AppNavigation
import com.example.socialme_interfazgrafica.services.NotificacionService
import com.example.socialme_interfazgrafica.services.WebSocketService
import com.example.socialme_interfazgrafica.ui.theme.SocialMeInterfazGraficaTheme
import com.example.socialme_interfazgrafica.utils.PayPalConfig
import com.example.socialme_interfazgrafica.viewModel.UserViewModel

class MainActivity : ComponentActivity() {

    // Registro para la solicitud de permisos
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Permiso de notificaciones concedido")
        } else {
            Log.d("MainActivity", "Permiso de notificaciones denegado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PayPalConfig.init(this)

        // Crear canal de notificación
        createNotificationChannel()

        // Verificar y solicitar permisos de notificación
        requestNotificationPermission()

        // Verificar si el usuario está logueado e iniciar el servicio WebSocket
        checkLoginAndStartService()

        enableEdgeToEdge()
        setContent {
            SocialMeInterfazGraficaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(Modifier.padding(innerPadding)){
                        AppNavigation(viewModel())
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SocialMe Notificaciones"
            val descriptionText = "Canal para notificaciones de actividades"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("socialme_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d("MainActivity", "Canal de notificaciones creado")
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "Permiso de notificaciones ya concedido")
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.d("MainActivity", "Mostrando explicación para permiso de notificaciones")
                    // En un caso real, podrías mostrar un diálogo explicando por qué necesitas el permiso
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                else -> {
                    Log.d("MainActivity", "Solicitando permiso de notificaciones")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun checkLoginAndStartService() {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("USERNAME", null)
        val token = sharedPreferences.getString("TOKEN", null)

        if (!username.isNullOrEmpty() && !token.isNullOrEmpty()) {
            Log.d("MainActivity", "Usuario logueado, iniciando servicio WebSocket")
            // El usuario está logueado, iniciar el servicio WebSocket
            val intent = Intent(this, WebSocketService::class.java).apply {
                putExtra("username", username)
                putExtra("token", token)
            }
            startService(intent)
        } else {
            Log.d("MainActivity", "Usuario no logueado")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Verificar si el usuario ha cerrado sesión
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.contains("TOKEN")

        if (!isLoggedIn) {
            Log.d("MainActivity", "Deteniendo servicio WebSocket al destruir MainActivity")
            // Si el usuario ha cerrado sesión, detener el servicio
            val intent = Intent(this, WebSocketService::class.java)
            stopService(intent)
        }
    }
}