package com.example.socialme_interfazgrafica.navigation

//Rutas de navegacion
sealed class AppScreen(val route: String) {
    object InicioSesionScreen : AppScreen("inicioSesionScreen")
    object RegistroUsuarioScreen : AppScreen("registroUsuarioScreen")
    object MenuScreen : AppScreen("menuScreen")

    // Nuevas rutas con argumentos
    object ActividadDetalleScreen : AppScreen("actividadDetalle/{actividadId}") {
        fun createRoute(actividadId: String) = "actividadDetalle/$actividadId"
    }

    object ComunidadDetalleScreen : AppScreen("comunidadDetalle/{comunidadUrl}") {
        fun createRoute(comunidadUrl: String) = "comunidadDetalle/$comunidadUrl"
    }
}
