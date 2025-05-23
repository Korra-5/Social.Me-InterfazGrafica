
package com.example.socialme_interfazgrafica.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class AppScreen(val route: String) {
    object InicioSesionScreen : AppScreen("inicioSesionScreen")
    object RegistroUsuarioScreen : AppScreen("registroUsuarioScreen")
    object MenuScreen : AppScreen("menuScreen")
    object BusquedaScreen : AppScreen("busquedaScreen")
    object OpcionesScreen : AppScreen("opcionesScreen")
    object CrearComunidadScreen : AppScreen("crear_comunidad_screen")
    object ComprarPremiumScreen : AppScreen("comprar_premium_screen")

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

    object VerUsuariosPorActividadScreen :
        AppScreen("verUsuariosPorActividad/{actividadId}/{nombreActividad}") {
        fun createRoute(actividadId: String, nombreActividad: String) =
            "verUsuariosPorActividad/$actividadId/${nombreActividad}"
    }

    object VerUsuariosPorComunidadScreen :
        AppScreen("verUsuariosPorComunidad/{comunidadId}/{nombreComunidad}?modoSeleccion={modoSeleccion}") {
        fun createRoute(
            comunidadId: String,
            nombreComunidad: String,
            modoSeleccion: String = ""
        ): String {
            val baseRoute = "verUsuariosPorComunidad/$comunidadId/$nombreComunidad"
            return if (modoSeleccion.isNotEmpty()) {
                "$baseRoute?modoSeleccion=$modoSeleccion"
            } else {
                baseRoute
            }
        }
    }

    object ModificarComunidadScreen : AppScreen("modificar_comunidad/{comunidadUrl}") {
        fun createRoute(comunidadUrl: String): String {
            return "modificar_comunidad/$comunidadUrl"
        }
    }

    object CrearActividadScreen : AppScreen("crear_actividad/{comunidadUrl}") {
        fun createRoute(comunidadUrl: String): String {
            return "crear_actividad/$comunidadUrl"
        }
    }

    object ModificarActividadScreen : AppScreen("modificar_actividad/{idActividad}") {
        fun createRoute(idActividad: String): String {
            return "modificar_actividad/$idActividad"
        }
    }

    object ModificarUsuarioScreen : AppScreen("modificar_usuario/{username}") {
        fun createRoute(username: String): String {
            return "modificar_usuario/$username"
        }
    }

    object EmailVerificationScreen : AppScreen("email_verification/{email}/{username}/{isRegistration}") {
        fun createRoute(email: String, username: String, isRegistration: Boolean): String {
            return "email_verification/$email/$username/$isRegistration"
        }
    }

    object ChatComunidadScreen : AppScreen("chat_comunidad/{comunidadUrl}/{comunidadNombre}") {
        fun createRoute(comunidadUrl: String, comunidadNombre: String): String {
            val nombreEncoded = URLEncoder.encode(comunidadNombre, StandardCharsets.UTF_8.toString())
            return "chat_comunidad/$comunidadUrl/$nombreEncoded"
        }
    }
    object SolicitudesAmistadScreen : AppScreen("solicitudes_amistad")

    object NotificacionesScreen : AppScreen("notificaciones_screen")

    object UsuariosBloqueadosScreen : AppScreen("usuarios_bloqueados")
}