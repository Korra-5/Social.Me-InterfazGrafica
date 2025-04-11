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

    object UsuarioDetalleScreen : AppScreen("usuarioDetalle/{username}") {
        fun createRoute(username: String) = "usuarioDetalle/$username"
    }

    // Nueva ruta para ver usuarios por actividad
    object VerUsuariosPorActividadScreen : AppScreen("verUsuariosPorActividad/{actividadId}/{nombreActividad}") {
        fun createRoute(actividadId: String, nombreActividad: String) =
            "verUsuariosPorActividad/$actividadId/${nombreActividad}"
    }

    object VerUsuariosPorComunidadScreen : AppScreen("verUsuariosPorComunidad/{comunidadId}/{nombreComunidad}") {
        fun createRoute(comunidadId: String, nombreComunidad: String) =
            "verUsuariosPorComunidad/$comunidadId/${nombreComunidad}"
    }
}