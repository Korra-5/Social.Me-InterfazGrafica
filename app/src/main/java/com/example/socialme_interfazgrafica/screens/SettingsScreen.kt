package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.viewModel.UserViewModel

@Composable
fun OpcionesScreen(navController: NavController, viewModel: UserViewModel) {
    // Obtener el contexto para acceder a SharedPreferences
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Opciones",
                color = colorResource(R.color.cyanSecundario),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Aquí va el contenido de la pantalla de opciones
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Configuración de usuario",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Notificaciones")
                    Text("Privacidad")
                    Text("Idioma")
                    Text("Tema")

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // Función para cerrar sesión
                            cerrarSesion(context, navController, viewModel)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.cyanSecundario)
                        ),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }

        // Bottom Navigation Bar
        BottomNavBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Función para cerrar la sesión del usuario:
 * - Limpia los datos de SharedPreferences
 * - Resetea el estado de login en el ViewModel
 * - Muestra un mensaje de confirmación
 * - Navega a la pantalla de inicio de sesión
 */
private fun cerrarSesion(context: Context, navController: NavController, viewModel: UserViewModel) {
    // 1. Limpiar datos de SharedPreferences
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        clear() // Elimina todos los datos almacenados
        apply()
    }

    // 2. Resetear el estado de login en el ViewModel
    viewModel.resetLoginState()

    // 3. Mostrar mensaje de confirmación
    Toast.makeText(
        context,
        "Sesión cerrada correctamente",
        Toast.LENGTH_SHORT
    ).show()

    // 4. Navegar a la pantalla de inicio de sesión
    navController.navigate(AppScreen.InicioSesionScreen.route) {
        // Eliminar todas las pantallas anteriores del back stack
        popUpTo(navController.graph.startDestinationId) { inclusive = true }
    }
}