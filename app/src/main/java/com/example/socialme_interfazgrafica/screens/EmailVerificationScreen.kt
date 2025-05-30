package com.example.socialme_interfazgrafica.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.viewModel.UserViewModel
import com.example.socialme_interfazgrafica.viewModel.VerificacionState
import com.example.socialme_interfazgrafica.utils.ErrorUtils
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

    var verificationCode by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var remainingTime by remember { mutableStateOf(300) }
    var codeResent by remember { mutableStateOf(false) }
    var isVerificationSuccess by remember { mutableStateOf(false) }

    val verificacionState by viewModel.verificacionState.observeAsState()

    LaunchedEffect(codeResent) {
        remainingTime = 300
        while (remainingTime > 0) {
            delay(1000)
            remainingTime--
        }
    }

    LaunchedEffect(verificacionState) {
        val currentState = verificacionState
        when (currentState) {
            is VerificacionState.Loading -> {
                isSubmitting = true
                errorMessage = null
                successMessage = null
            }
            is VerificacionState.Success -> {
                isSubmitting = false
                isVerificationSuccess = true
                successMessage = currentState.message
                delay(1500)
                if (isRegistration) {
                    viewModel.resetRegistroState()
                    navController.navigate(AppScreen.InicioSesionScreen.route) {
                        popUpTo(AppScreen.RegistroUsuarioScreen.route) { inclusive = true }
                        popUpTo(AppScreen.EmailVerificationScreen.route) { inclusive = true }
                    }
                } else {
                    viewModel.resetRegistroState()
                    navController.popBackStack()
                }
            }
            is VerificacionState.Error -> {
                isSubmitting = false
                errorMessage = ErrorUtils.parseErrorMessage(currentState.message)
            }
            else -> {
                isSubmitting = false
            }
        }
    }

    val minutes = remainingTime / 60
    val seconds = remainingTime % 60
    val timeDisplay = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = if (isRegistration) "Verificación de registro" else "Verificación de modificación"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetRegistroState()
                        navController.popBackStack()
                    }) {
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_email),
                    contentDescription = "Email",
                    tint = colorResource(R.color.azulPrimario),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isRegistration) "Verifica tu correo electrónico" else "Verifica tu nuevo correo",
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

                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = {
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.azulPrimario),
                        unfocusedBorderColor = colorResource(R.color.textoSecundario),
                        focusedLabelColor = colorResource(R.color.azulPrimario),
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorTextColor = Color.Black,
                        errorLabelColor = Color.Black,
                        errorBorderColor = Color(0xFFD32F2F)
                    ),
                    isError = errorMessage != null
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }

                successMessage?.let {
                    Text(
                        text = it,
                        color = Color.Green,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (verificationCode.length != 6) {
                            errorMessage = "Por favor, introduce el código de 6 dígitos"
                            return@Button
                        }

                        errorMessage = null

                        if (isRegistration) {
                            viewModel.verificarCodigo(email, verificationCode) { success ->
                            }
                        } else {
                            viewModel.verificarCodigoModificacion(
                                context = context,
                                email = email,
                                codigo = verificationCode
                            ) { success ->
                            }
                        }
                    },
                    enabled = !isSubmitting && verificationCode.isNotEmpty() && remainingTime > 0 && !isVerificationSuccess,
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

                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.reenviarCodigo(email) { success ->
                                if (success) {
                                    codeResent = !codeResent
                                    successMessage = "Se ha enviado un nuevo código a tu correo"
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
                    enabled = remainingTime <= 0 && !isVerificationSuccess,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = if (remainingTime > 0) "Reenviar código en $timeDisplay" else "Reenviar código",
                        color = if (remainingTime > 0) colorResource(R.color.textoSecundario) else colorResource(R.color.azulPrimario)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¿No has recibido el código? Revisa tu carpeta de spam o solicita un nuevo código cuando el temporizador termine.",
                    fontSize = 14.sp,
                    color = colorResource(R.color.textoSecundario),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }

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