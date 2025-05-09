package com.example.socialme_interfazgrafica.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.VerificacionDTO
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.viewModel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    navController: NavController,
    email: String,
    username: String,
    isRegistration: Boolean = true,
    viewModel: UserViewModel
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estados para manejar la entrada del código y los mensajes
    var verificationCode by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var remainingTime by remember { mutableStateOf(300) } // 5 minutos en segundos
    var codeResent by remember { mutableStateOf(false) }

    // Temporizador para el tiempo restante
    LaunchedEffect(codeResent) {
        remainingTime = 300
        while (remainingTime > 0) {
            delay(1000)
            remainingTime--
        }
    }

    // Formatear el tiempo restante
    val minutes = remainingTime / 60
    val seconds = remainingTime % 60
    val timeDisplay = String.format("%02d:%02d", minutes, seconds)

    // UI principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra superior
            TopAppBar(
                title = { Text(text = "Verificación de correo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.azulPrimario),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icono de email
                Icon(
                    painter = painterResource(id = R.drawable.ic_email),
                    contentDescription = "Email",
                    tint = colorResource(R.color.azulPrimario),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Título y descripción
                Text(
                    text = "Verifica tu correo electrónico",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.azulPrimario),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Hemos enviado un código de verificación a:\n$email",
                    fontSize = 16.sp,
                    color = colorResource(R.color.textoSecundario),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tiempo restante
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.cyanSecundario)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tiempo restante",
                            fontSize = 14.sp,
                            color = colorResource(R.color.azulPrimario)
                        )

                        Text(
                            text = timeDisplay,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.azulPrimario)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo para ingresar el código
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = {
                        // Solo permitir dígitos y limitar a 6 caracteres
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            verificationCode = it
                        }
                    },
                    label = { Text("Código de verificación") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colorResource(R.color.azulPrimario),
                        unfocusedBorderColor = colorResource(R.color.textoSecundario),
                        focusedLabelColor = colorResource(R.color.azulPrimario)
                    ),
                    isError = errorMessage != null
                )

                // Mensaje de error si existe
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }

                // Mensaje de éxito si existe
                successMessage?.let {
                    Text(
                        text = it,
                        color = Color.Green,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para verificar el código
                Button(
                    onClick = {
                        if (verificationCode.length != 6) {
                            errorMessage = "Por favor, introduce el código de 6 dígitos"
                            return@Button
                        }

                        errorMessage = null
                        isSubmitting = true

                        scope.launch {
                            viewModel.verificarCodigo(email, verificationCode) { success ->
                                if (success) {
                                    successMessage = "¡Verificación exitosa!"
                                    // Esperar un momento para mostrar el mensaje de éxito
                                    scope.launch {
                                        delay(1500)
                                        if (isRegistration) {
                                            // Ir a la pantalla de inicio de sesión después del registro
                                            navController.navigate(AppScreen.InicioSesionScreen.route) {
                                                popUpTo(AppScreen.RegistroUsuarioScreen.route) { inclusive = true }
                                            }
                                        } else {
                                            // Volver a la pantalla anterior (posiblemente modificación de perfil)
                                            navController.popBackStack()
                                        }
                                    }
                                } else {
                                    errorMessage = "Código incorrecto. Por favor, verifica e intenta nuevamente."
                                }
                                isSubmitting = false
                            }
                        }
                    },
                    enabled = !isSubmitting && verificationCode.isNotEmpty() && remainingTime > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario),
                        disabledContainerColor = colorResource(R.color.azulPrimario).copy(alpha = 0.5f)
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Verificar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón para reenviar el código
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.reenviarCodigo(email) { success ->
                                if (success) {
                                    codeResent = !codeResent // Cambiar el estado para reiniciar el temporizador
                                    successMessage = "Se ha enviado un nuevo código a tu correo"
                                    // Limpiar el mensaje después de un tiempo
                                    scope.launch {
                                        delay(3000)
                                        successMessage = null
                                    }
                                } else {
                                    errorMessage = "No se pudo reenviar el código. Inténtalo más tarde."
                                }
                            }
                        }
                    },
                    enabled = remainingTime <= 0,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = if (remainingTime > 0) "Reenviar código en $timeDisplay" else "Reenviar código",
                        color = if (remainingTime > 0) colorResource(R.color.textoSecundario) else colorResource(R.color.azulPrimario)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Texto de ayuda
                Text(
                    text = "¿No has recibido el código? Revisa tu carpeta de spam o solicita un nuevo código cuando el temporizador termine.",
                    fontSize = 14.sp,
                    color = colorResource(R.color.textoSecundario),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }

        // Mostrar un indicador de carga si estamos enviando la verificación
        if (isSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorResource(R.color.azulPrimario))
            }
        }
    }
}