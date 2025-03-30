package com.example.socialme_interfazgrafica.navigation

//Rutas de navegacion
sealed class AppScreen(val route: String) {
    object InicioSesionScreen : AppScreen("inicioSesionScreen")
    object RegistroUsuarioScreen : AppScreen("registroUsuarioScreen")
    object MenuScreen : AppScreen("menuScreen")
}
