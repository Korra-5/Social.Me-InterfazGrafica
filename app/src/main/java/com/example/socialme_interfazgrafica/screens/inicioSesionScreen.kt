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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.example.socialme_interfazgrafica.viewModel.LoginState
import com.example.socialme_interfazgrafica.viewModel.UserViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.roundToInt

private const val SHARED_PREFS_NAME = "UserPrefs"

@Composable
fun InicioSesionScreen(navController: NavController, viewModel: UserViewModel) {
    // Estados para los campos del formulario
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showRegistrationSuccessMessage by remember { mutableStateOf(false) }
    var showLoginSuccessMessage by remember { mutableStateOf(false) }
    val userRole = remember { mutableStateOf("") }
    val context = LocalContext.current

    // Observar el estado de login del ViewModel
    val loginState by viewModel.loginState.observeAsState()
    val tokenLogin by viewModel.tokenLogin.observeAsState("")

    // ✨ ANIMACIÓN DEL FONDO
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing), // 4 segundos más suave
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    // Animación para la card
    val cardScale by animateFloatAsState(
        targetValue = if (isLoading) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    // Verificar si viene de registro exitoso
    val registroExitoso = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<Boolean>("registro_exitoso") ?: false

    LaunchedEffect(registroExitoso) {
        if (registroExitoso) {
            showRegistrationSuccessMessage = true
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
                userRole.value = state.role ?: "USER"

                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("TOKEN", state.token)
                    putString("USERNAME", username)
                    putString("ROLE", userRole.value)
                    apply()
                }

                try {
                    Toast.makeText(context, "Bienvenido, $username", Toast.LENGTH_SHORT).show()
                    navController.navigate(AppScreen.MenuScreen.route) {
                        popUpTo(AppScreen.InicioSesionScreen.route) { inclusive = true }
                    }
                } catch (e: Exception) {
                    errorMessage = "Error interno: ${e.message}"
                }
            }
            is LoginState.Error -> {
                isLoading = false
                errorMessage = ErrorUtils.parseErrorMessage(state.message)
                showLoginSuccessMessage = false
                showRegistrationSuccessMessage = false
            }
            else -> isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colorResource(R.color.azulPrimario), // Tu color #FF3D5A80
                        Color(0xFF5A7BA8), // Variación más clara
                        Color(0xFF7B9BC9)  // Variación aún más clara
                    ),
                    // La magia de la animación: los puntos se mueven
                    start = androidx.compose.ui.geometry.Offset(0f, animatedOffset * 1000),
                    end = androidx.compose.ui.geometry.Offset(1000f, (1 - animatedOffset) * 1000)
                )
            )
    ) {
        // Círculos decorativos de fondo
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = 100.dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-30).dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo con animación
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.3f)
                    ),
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "¡BIENVENIDO!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Inicia sesión para continuar",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Card principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        scaleX = cardScale,
                        scaleY = cardScale
                    )
                    .shadow(
                        elevation = 25.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.4f),
                        spotColor = Color.Black.copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Campo usuario
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Usuario") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_user),
                                contentDescription = "Usuario",
                                modifier = Modifier.size(24.dp),
                                tint = colorResource(R.color.azulPrimario)
                            )
                        },
                        singleLine = true,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.azulPrimario),
                            unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                            focusedLabelColor = colorResource(R.color.azulPrimario),
                            unfocusedLabelColor = colorResource(R.color.textoSecundario),
                            focusedTextColor = colorResource(R.color.black),
                            unfocusedTextColor = colorResource(R.color.black),
                            cursorColor = colorResource(R.color.azulPrimario),
                            // ✨ COLORES PARA ESTADO DESHABILITADO
                            disabledTextColor = Color(0xFF424242),        // Gris oscuro para texto
                            disabledLabelColor = Color(0xFF757575),       // Gris medio para label
                            disabledBorderColor = colorResource(R.color.cyanSecundario),
                            disabledLeadingIconColor = colorResource(R.color.azulPrimario).copy(alpha = 0.6f)
                        )
                    )

                    // Campo contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = "Contraseña",
                                modifier = Modifier.size(24.dp),
                                tint = colorResource(R.color.azulPrimario)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = colorResource(R.color.azulPrimario)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.azulPrimario),
                            unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                            focusedLabelColor = colorResource(R.color.azulPrimario),
                            unfocusedLabelColor = colorResource(R.color.textoSecundario),
                            focusedTextColor = colorResource(R.color.black),
                            unfocusedTextColor = colorResource(R.color.black),
                            cursorColor = colorResource(R.color.azulPrimario),
                            // ✨ COLORES PARA ESTADO DESHABILITADO
                            disabledTextColor = Color(0xFF424242),        // Gris oscuro para texto
                            disabledLabelColor = Color(0xFF757575),       // Gris medio para label
                            disabledBorderColor = colorResource(R.color.cyanSecundario),
                            disabledLeadingIconColor = colorResource(R.color.azulPrimario).copy(alpha = 0.6f),
                            disabledTrailingIconColor = colorResource(R.color.azulPrimario).copy(alpha = 0.6f)
                        )
                    )

                    // Mensajes de estado con animaciones
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorResource(R.color.error).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = colorResource(R.color.error),
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showRegistrationSuccessMessage,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorResource(R.color.correcto).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "¡Registro exitoso! Ahora puedes iniciar sesión.",
                                color = colorResource(R.color.correcto),
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
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
                            .height(56.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
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
                            Text(
                                "INICIAR SESIÓN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Enlace a registro
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes una cuenta? ",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )

                TextButton(
                    onClick = { navController.navigate(AppScreen.RegistroUsuarioScreen.route) },
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Regístrate",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                }
            }
        }
    }
}