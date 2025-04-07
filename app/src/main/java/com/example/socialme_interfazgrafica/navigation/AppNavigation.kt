
package com.example.socialme_interfazgrafica.navigation


import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.screens.ActividadDetalleScreen
import com.example.socialme_interfazgrafica.screens.ComunidadDetalleScreen
import com.example.socialme_interfazgrafica.screens.InicioSesionScreen
import com.example.socialme_interfazgrafica.screens.MenuScreen
import com.example.socialme_interfazgrafica.screens.RegistroUsuarioScreen
import com.example.socialme_interfazgrafica.viewModel.UserViewModel
import kotlinx.coroutines.launch

//App navigation
@Composable
fun AppNavigation(viewModel: UserViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    NavHost(navController = navController, startDestination = AppScreen.InicioSesionScreen.route) {
        composable(AppScreen.InicioSesionScreen.route) {
            InicioSesionScreen(navController, viewModel)
        }

        composable(AppScreen.RegistroUsuarioScreen.route) {
            RegistroUsuarioScreen(navController, viewModel)
        }

        composable(AppScreen.MenuScreen.route) {
            MenuScreen(navController)
        }

        // Nueva ruta para detalles de actividad con argumento de ID
        composable(
            route = AppScreen.ActividadDetalleScreen.route,
            arguments = listOf(
                navArgument("actividadId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val actividadId = backStackEntry.arguments?.getString("actividadId") ?: ""

            // Obtener el token de autenticación
            val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("TOKEN", "") ?: ""
            val authToken = "Bearer $token"

            ActividadDetalleScreen(navController=navController, authToken = authToken , actividadId =  actividadId)
        }

        // Nueva ruta para detalles de comunidad con argumento de URL
        composable(
            route = AppScreen.ComunidadDetalleScreen.route,
            arguments = listOf(
                navArgument("comunidadUrl") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val comunidadUrl = backStackEntry.arguments?.getString("comunidadUrl") ?: ""

            // Obtener el token de autenticación
            val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("TOKEN", "") ?: ""
            val authToken = "Bearer $token"

            // Estado para almacenar los datos de la comunidad
            var comunidad by remember { mutableStateOf<ComunidadDTO?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            // Cargar los datos de la comunidad al entrar en la pantalla
            LaunchedEffect(comunidadUrl) {
                scope.launch {
                    try {
                        val response = apiService.verComunidadPorUrl(authToken, comunidadUrl)
                        if (response.isSuccessful) {
                            comunidad = response.body()
                            isLoading = false
                        } else {
                            errorMessage = "Error al cargar la comunidad: ${response.code()}"
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error de conexión: ${e.message}"
                        isLoading = false
                    }
                }
            }

            // Renderizar la pantalla de detalle cuando los datos estén disponibles
            comunidad?.let {
                ComunidadDetalleScreen(comunidad = it, authToken = authToken, navController = navController)
            }
        }
    }
}