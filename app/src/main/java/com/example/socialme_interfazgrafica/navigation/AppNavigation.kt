package com.example.socialme_interfazgrafica.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.socialme_interfazgrafica.screens.CrearActividadScreen
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.socialme_interfazgrafica.ModificarActividadScreen
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.screens.ActividadDetalleScreen
import com.example.socialme_interfazgrafica.screens.BusquedaScreen
import com.example.socialme_interfazgrafica.screens.ChatComunidadScreen
import com.example.socialme_interfazgrafica.screens.ComunidadDetalleScreen
import com.example.socialme_interfazgrafica.screens.ComprarPremiumScreen
import com.example.socialme_interfazgrafica.screens.CrearComunidadScreen
import com.example.socialme_interfazgrafica.screens.DenunciasScreen
import com.example.socialme_interfazgrafica.screens.EmailVerificationScreen
import com.example.socialme_interfazgrafica.screens.InicioSesionScreen
import com.example.socialme_interfazgrafica.screens.MenuScreen
import com.example.socialme_interfazgrafica.screens.ModificarComunidadScreen
import com.example.socialme_interfazgrafica.screens.ModificarUsuarioScreen
import com.example.socialme_interfazgrafica.screens.OpcionesScreen
import com.example.socialme_interfazgrafica.screens.RegistroUsuarioScreen
import com.example.socialme_interfazgrafica.screens.SolicitudesAmistadScreen
import com.example.socialme_interfazgrafica.screens.UsuarioDetallesScreen
import com.example.socialme_interfazgrafica.screens.UsuariosBloqueadosScreen
import com.example.socialme_interfazgrafica.screens.VerUsuariosPorActividadScreen
import com.example.socialme_interfazgrafica.screens.VerUsuariosPorComunidadScreen
import com.example.socialme_interfazgrafica.viewModel.UserViewModel
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(viewModel: UserViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    fun getAuthToken(): String {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", "") ?: ""
        return if (token.startsWith("Bearer ")) {
            token
        } else {
            "Bearer $token"
        }
    }

    NavHost(navController = navController, startDestination = AppScreen.InicioSesionScreen.route) {
        composable(AppScreen.InicioSesionScreen.route) {
            InicioSesionScreen(navController, viewModel)
        }

        composable(AppScreen.DenunciasScreen.route) {
            DenunciasScreen(navController = navController)
        }

        composable(AppScreen.CrearComunidadScreen.route) {
            CrearComunidadScreen(navController)
        }

        composable(AppScreen.RegistroUsuarioScreen.route) {
            RegistroUsuarioScreen(navController, viewModel)
        }

        composable(AppScreen.MenuScreen.route) {
            MenuScreen(navController)
        }

        composable(AppScreen.BusquedaScreen.route) {
            BusquedaScreen(navController)
        }

        composable(AppScreen.OpcionesScreen.route) {
            OpcionesScreen(navController, viewModel)
        }

        composable(AppScreen.ComprarPremiumScreen.route) {
            ComprarPremiumScreen(navController)
        }

        composable(
            route = AppScreen.ActividadDetalleScreen.route,
            arguments = listOf(
                navArgument("actividadId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val actividadId = backStackEntry.arguments?.getString("actividadId") ?: ""
            val authToken = getAuthToken()

            ActividadDetalleScreen(navController=navController, authToken = authToken , actividadId =  actividadId)
        }

        composable(
            route = AppScreen.ComunidadDetalleScreen.route,
            arguments = listOf(
                navArgument("comunidadUrl") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val comunidadUrl = backStackEntry.arguments?.getString("comunidadUrl") ?: ""
            val authToken = getAuthToken()

            var comunidad by remember { mutableStateOf<ComunidadDTO?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

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

            comunidad?.let {
                ComunidadDetalleScreen(comunidad = it, authToken = authToken, navController = navController)
            }
        }

        composable(
            route = AppScreen.UsuarioDetalleScreen.route,
            arguments = listOf(
                navArgument("username") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            UsuarioDetallesScreen(navController = navController, username = username)
        }

        composable(
            route = AppScreen.VerUsuariosPorActividadScreen.route,
            arguments = listOf(
                navArgument("actividadId") {
                    type = NavType.StringType
                },
                navArgument("nombreActividad") {
                    type = NavType.StringType
                }

            )
        ) { backStackEntry ->
            val actividadId = backStackEntry.arguments?.getString("actividadId") ?: ""
            val nombreActividadEncoded = backStackEntry.arguments?.getString("nombreActividad") ?: ""
            val nombreActividad = URLDecoder.decode(nombreActividadEncoded, StandardCharsets.UTF_8.toString())

            VerUsuariosPorActividadScreen(
                navController = navController,
                actividadId = actividadId,
                nombreActividad = nombreActividad,
            )
        }

        composable(
            route = AppScreen.VerUsuariosPorComunidadScreen.route,
            arguments = listOf(
                navArgument("comunidadId") {
                    type = NavType.StringType
                },
                navArgument("nombreComunidad") {
                    type = NavType.StringType
                },
                navArgument("modoSeleccion") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val comunidadId = backStackEntry.arguments?.getString("comunidadId") ?: ""
            val nombreComunidadEncoded = backStackEntry.arguments?.getString("nombreComunidad") ?: ""
            val nombreComunidad = URLDecoder.decode(nombreComunidadEncoded, StandardCharsets.UTF_8.toString())
            val modoSeleccion = backStackEntry.arguments?.getString("modoSeleccion") ?: ""

            VerUsuariosPorComunidadScreen(
                navController = navController,
                comunidadId = comunidadId,
                nombreComunidad = nombreComunidad,
                modoSeleccion = modoSeleccion
            )
        }

        composable(
            route = AppScreen.ModificarComunidadScreen.route,
            arguments = listOf(
                navArgument("comunidadUrl") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val comunidadUrl = backStackEntry.arguments?.getString("comunidadUrl") ?: ""

            ModificarComunidadScreen(comunidadUrl = comunidadUrl, navController = navController)
        }
        composable(
            route = AppScreen.CrearActividadScreen.route,
            arguments = listOf(
                navArgument("comunidadUrl") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val comunidadUrl = backStackEntry.arguments?.getString("comunidadUrl") ?: ""

            CrearActividadScreen(
                comunidadUrl = comunidadUrl,
                navController = navController
            )
        }
        composable(
            route = AppScreen.ModificarActividadScreen.route,
            arguments = listOf(
                navArgument("idActividad") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val idActividad = backStackEntry.arguments?.getString("idActividad") ?: ""

            ModificarActividadScreen(
                actividadId = idActividad,
                navController = navController
            )

        }
        composable(
            route = AppScreen.ModificarUsuarioScreen.route,
            arguments = listOf(
                navArgument("username") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""

            ModificarUsuarioScreen(
                username = username,
                navController = navController
            )
        }

        composable(
            route = AppScreen.EmailVerificationScreen.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("username") { type = NavType.StringType },
                navArgument("isRegistration") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val isRegistration = backStackEntry.arguments?.getBoolean("isRegistration") ?: true

            EmailVerificationScreen(
                navController = navController,
                email = email,
                username = username,
                isRegistration = isRegistration,
                viewModel=viewModel
            )
        }

        composable(AppScreen.SolicitudesAmistadScreen.route) {
            SolicitudesAmistadScreen(navController = navController)
        }

        composable(AppScreen.UsuariosBloqueadosScreen.route) {
            UsuariosBloqueadosScreen(navController = navController)
        }

        composable(
            route = AppScreen.ChatComunidadScreen.route,
            arguments = listOf(
                navArgument("comunidadUrl") {
                    type = NavType.StringType
                },
                navArgument("comunidadNombre") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val comunidadUrl = backStackEntry.arguments?.getString("comunidadUrl") ?: ""
            val comunidadNombreEncoded = backStackEntry.arguments?.getString("comunidadNombre") ?: ""
            val comunidadNombre = URLDecoder.decode(comunidadNombreEncoded, StandardCharsets.UTF_8.toString())

            ChatComunidadScreen(
                comunidadUrl = comunidadUrl,
                comunidadNombre = comunidadNombre,
                navController = navController
            )
        }
    }
}