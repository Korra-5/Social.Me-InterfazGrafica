package com.example.socialme_interfazgrafica.screens
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.example.socialme_interfazgrafica.viewModel.LoginState
import com.example.socialme_interfazgrafica.viewModel.UserViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// Constantes para SharedPreferences
private const val SHARED_PREFS_NAME = "UserPrefs"
private const val DISTANCIA_KEY = "RADAR_DISTANCIA"

@Composable
fun InicioSesionScreen(navController: NavController, viewModel: UserViewModel) {
    // Estados para los campos del formulario
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Separate state variables for different success messages
    var showRegistrationSuccessMessage by remember { mutableStateOf(false) }
    var showLoginSuccessMessage by remember { mutableStateOf(false) }

    // Estado local para el rol del usuario
    val userRole = remember { mutableStateOf("") }

    // Contexto local para Toast y SharedPreferences
    val context = LocalContext.current

    // Observar el estado de login del ViewModel
    val loginState by viewModel.loginState.observeAsState()
    val tokenLogin by viewModel.tokenLogin.observeAsState("")

    // Verificar si viene de registro exitoso
    val registroExitoso = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<Boolean>("registro_exitoso") ?: false

    // Si viene de registro exitoso, mostrar mensaje
    LaunchedEffect(registroExitoso) {
        if (registroExitoso) {
            showRegistrationSuccessMessage = true
            // Reset the flag so it doesn't show again on navigation
            navController.currentBackStackEntry?.savedStateHandle?.set("registro_exitoso", false)
        }
    }

    // Efecto para manejar los cambios en el estado de login
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Loading -> {
                isLoading = true
                errorMessage = null
                showLoginSuccessMessage = false
            }
            is LoginState.Success -> {
                isLoading = false
                showLoginSuccessMessage = true
                showRegistrationSuccessMessage = false

                // Extraer el rol del resultado del login
                userRole.value = state.role ?: "USER"

                // Guardar datos en SharedPreferences
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("TOKEN", state.token)
                    putString("USERNAME", username)
                    putString("ROLE", userRole.value)
                    apply()
                }

                // Log para debugging
                Log.d("InicioSesionScreen", "Login exitoso - Usuario: $username, Rol: ${userRole.value}")

                // Mostrar mensaje y navegar después de un breve delay
                try {
                    Toast.makeText(
                        context,
                        "Bienvenido, $username",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navegar a la pantalla de menú
                    navController.navigate(AppScreen.MenuScreen.route) {
                        popUpTo(AppScreen.InicioSesionScreen.route) { inclusive = true }
                    }
                } catch (e: Exception) {
                    Log.e("InicioSesionScreen", "Error en navegación: ${e.message}")
                    errorMessage = "Error interno: ${e.message}"
                }
            }
            is LoginState.Error -> {
                isLoading = false
                errorMessage = ErrorUtils.parseErrorMessage(state.message)
                showLoginSuccessMessage = false
                showRegistrationSuccessMessage = false
            }
            else -> {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 30.dp)
            )

            Text(
                text = "Bienvenido",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Inicia sesión para continuar",
                fontSize = 16.sp,
                color = colorResource(R.color.textoSecundario),
                modifier = Modifier.padding(bottom = 30.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.white)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //Campo para el usuario
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Usuario") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_user),
                                contentDescription = "Usuario",
                                modifier = Modifier.size(20.dp),
                                tint = colorResource(R.color.azulPrimario)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.azulPrimario),
                            unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                            focusedLabelColor = colorResource(R.color.azulPrimario),
                            unfocusedLabelColor = colorResource(R.color.textoSecundario),
                            focusedTextColor = colorResource(R.color.black),
                            unfocusedTextColor = colorResource(R.color.black)
                        )
                    )

                    // Campo de contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = "Contraseña",
                                modifier = Modifier.size(20.dp),
                                tint = colorResource(R.color.azulPrimario)
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.azulPrimario),
                            unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                            focusedLabelColor = colorResource(R.color.azulPrimario),
                            unfocusedLabelColor = colorResource(R.color.textoSecundario),
                            focusedTextColor = colorResource(R.color.black),
                            unfocusedTextColor = colorResource(R.color.black)
                        )
                    )

                    // Mensaje de error
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = colorResource(R.color.error),
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Mensaje de registro exitoso
                    if (showRegistrationSuccessMessage) {
                        Text(
                            text = "¡Registro exitoso! Ahora puedes iniciar sesión.",
                            color = colorResource(R.color.correcto),
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Mensaje de inicio de sesión exitoso
                    if (showLoginSuccessMessage) {
                        Text(
                            text = "Inicio de sesión exitoso",
                            color = colorResource(R.color.correcto),
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Botón de inicio de sesión
                    Button(
                        onClick = {
                            if (username.isNotEmpty() && password.isNotEmpty()) {
                                viewModel.login(context, username, password)
                            } else {
                                errorMessage = "Por favor, completa todos los campos"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.azulPrimario),
                            disabledContainerColor = colorResource(R.color.cyanSecundario),
                            contentColor = colorResource(R.color.white)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = colorResource(R.color.white),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Iniciar Sesión", fontSize = 16.sp)
                        }
                    }
                }
            }

            // Enlace a registro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes una cuenta? ",
                    color = colorResource(R.color.textoSecundario)
                )

                TextButton(
                    onClick = {
                        navController.navigate(AppScreen.RegistroUsuarioScreen.route)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(R.color.azulPrimario)
                    )
                ) {
                    Text(
                        text = "Regístrate",
                        color = colorResource(R.color.azulPrimario),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}