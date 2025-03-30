package com.example.socialme_interfazgrafica.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.socialme_interfazgrafica.screens.InicioSesionScreen
import com.example.socialme_interfazgrafica.screens.MenuScreen
import com.example.socialme_interfazgrafica.screens.RegistroUsuarioScreen
import com.example.socialme_interfazgrafica.viewModel.UserViewModel

//App navigation
@Composable
fun AppNavigation(viewModel: UserViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppScreen.InicioSesionScreen.route) {
        composable(AppScreen.InicioSesionScreen.route) { InicioSesionScreen(navController, viewModel) }
        composable(AppScreen.RegistroUsuarioScreen.route){ RegistroUsuarioScreen(navController, viewModel) }
        composable(AppScreen.MenuScreen.route){ MenuScreen() }
    }
}
