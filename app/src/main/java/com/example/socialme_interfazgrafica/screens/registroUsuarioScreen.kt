package com.example.socialme_interfazgrafica.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.ui.layout.Layout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.example.socialme_interfazgrafica.utils.PalabrasMalsonantesValidator
import com.example.socialme_interfazgrafica.viewModel.RegistroState
import com.example.socialme_interfazgrafica.viewModel.UserViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.Search

private fun normalizarUrl(url: String): String {
    return url
        .replace("á", "a").replace("Á", "A")
        .replace("é", "e").replace("É", "E")
        .replace("í", "i").replace("Í", "I")
        .replace("ó", "o").replace("Ó", "O")
        .replace("ú", "u").replace("Ú", "U")
        .replace("ü", "u").replace("Ü", "U")
        .replace("ñ", "n").replace("Ñ", "N")
        .replace("ç", "c").replace("Ç", "C")
}

private fun normalizarUsername(username: String): String {
    return normalizarUrl(username)
        .lowercase()
        .filter { char -> char.isLetterOrDigit() || char == '_' }
}

@Composable
fun VistaPreviewUsername(username: String) {
    if (username.isNotBlank()) {
        val usernameNormalizado = normalizarUsername(username)
        if (usernameNormalizado.isNotEmpty()) {
            Text(
                text = "Username resultante: $usernameNormalizado",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
fun RegistroUsuarioScreen(navController: NavController, viewModel: UserViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.resetRegistroState()
    }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var municipio by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }

    var intereses = remember { mutableStateListOf<String>() }
    var nuevoInteres by remember { mutableStateOf("") }
    var imagenPerfil by remember { mutableStateOf<Uri?>(null) }

    val errorFields = remember { mutableStateMapOf<String, String>() }
    var isLoading by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf<String?>(null) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val imagenLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenPerfil = uri
    }

    val registroState by viewModel.registroState.observeAsState()

    LaunchedEffect(registroState) {
        when (registroState) {
            is RegistroState.Loading -> {
                isLoading = true
                generalError = null
            }
            is RegistroState.Success -> {
                isLoading = false
                navController.navigate(
                    AppScreen.EmailVerificationScreen.createRoute(
                        email = email.trim(),
                        username = normalizarUsername(username.trim()),
                        isRegistration = true
                    )
                )
            }
            is RegistroState.Error -> {
                isLoading = false
                generalError = ErrorUtils.parseErrorMessage((registroState as RegistroState.Error).message)
            }
            else -> {
                isLoading = false
            }
        }
    }

    fun validateFields(): Boolean {
        errorFields.clear()

        val usernameNormalizado = normalizarUsername(username.trim())
        if (username.trim().isEmpty()) {
            errorFields["username"] = "El nombre de usuario es requerido"
        } else if (usernameNormalizado.isEmpty()) {
            errorFields["username"] = "El nombre de usuario no es válido después de la normalización"
        } else if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(usernameNormalizado)) {
            errorFields["username"] = "El nombre de usuario contiene palabras no permitidas"
        }

        if (email.trim().isEmpty()) {
            errorFields["email"] = "El email es requerido"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            errorFields["email"] = "El email no es válido"
        }

        if (password.isEmpty()) {
            errorFields["password"] = "La contraseña es requerida"
        } else if (password.length < 6) {
            errorFields["password"] = "La contraseña debe tener al menos 6 caracteres"
        }

        if (confirmPassword.isEmpty()) {
            errorFields["confirmPassword"] = "Debe confirmar la contraseña"
        } else if (password != confirmPassword) {
            errorFields["confirmPassword"] = "Las contraseñas no coinciden"
        }

        if (municipio.trim().isEmpty()) {
            errorFields["municipio"] = "El municipio es requerido"
        } else if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(municipio.trim())) {
            errorFields["municipio"] = "El municipio contiene palabras no permitidas"
        }

        if (provincia.trim().isEmpty()) {
            errorFields["provincia"] = "La provincia es requerida"
        } else if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(provincia.trim())) {
            errorFields["provincia"] = "La provincia contiene palabras no permitidas"
        }

        if (nombre.trim().isEmpty()) {
            errorFields["nombre"] = "El nombre es requerido"
        } else if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(nombre.trim())) {
            errorFields["nombre"] = "El nombre contiene palabras no permitidas"
        }

        if (apellidos.trim().isEmpty()) {
            errorFields["apellidos"] = "Los apellidos son requeridos"
        } else if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(apellidos.trim())) {
            errorFields["apellidos"] = "Los apellidos contienen palabras no permitidas"
        }

        if (descripcion.trim().length > 500) {
            errorFields["descripcion"] = "La descripción no puede superar 500 caracteres"
        } else if (descripcion.trim().isNotEmpty() && PalabrasMalsonantesValidator.contienepalabrasmalsonantes(descripcion.trim())) {
            errorFields["descripcion"] = "La descripción contiene palabras no permitidas"
        }

        if (PalabrasMalsonantesValidator.validarLista(intereses.toList())) {
            errorFields["intereses"] = "Algunos intereses contienen palabras no permitidas"
        }

        return errorFields.isEmpty()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colorResource(R.color.azulPrimario),
                        Color(0xFF5A7BA8),
                        Color(0xFF7B9BC9)
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, animatedOffset * 800),
                    end = androidx.compose.ui.geometry.Offset(800f, (1 - animatedOffset) * 800)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-60).dp, y = 80.dp)
                .background(
                    Color.White.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-20).dp)
                .background(
                    Color.White.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "¡ÚNETE AHORA!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Crea tu cuenta y comienza",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 25.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { imagenLauncher.launch("image/*") }
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape
                            ),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (imagenPerfil == null)
                                colorResource(R.color.cyanSecundario)
                            else Color.Transparent
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imagenPerfil == null) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Añadir foto de perfil",
                                    modifier = Modifier.size(40.dp),
                                    tint = colorResource(R.color.azulPrimario)
                                )
                            } else {
                                AsyncImage(
                                    model = imagenPerfil,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Text(
                        text = "Toca para añadir foto",
                        fontSize = 12.sp,
                        color = colorResource(R.color.textoSecundario),
                        modifier = Modifier.padding(bottom = 24.dp, top = 8.dp)
                    )

                    Column {
                        ModernTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = "Nombre de usuario",
                            icon = R.drawable.ic_user,
                            errorMessage = errorFields["username"]
                        )
                        VistaPreviewUsername(username)
                    }

                    ModernTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        icon = R.drawable.ic_user,
                        keyboardType = KeyboardType.Email,
                        errorMessage = errorFields["email"]
                    )

                    ModernTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        icon = R.drawable.ic_lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityChange = { passwordVisible = it },
                        errorMessage = errorFields["password"]
                    )

                    ModernTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirmar contraseña",
                        icon = R.drawable.ic_lock,
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onPasswordVisibilityChange = { confirmPasswordVisible = it },
                        errorMessage = errorFields["confirmPassword"]
                    )

                    ModernTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = "Nombre",
                        icon = R.drawable.ic_user,
                        errorMessage = errorFields["nombre"]
                    )

                    ModernTextField(
                        value = apellidos,
                        onValueChange = { apellidos = it },
                        label = "Apellidos",
                        icon = R.drawable.ic_user,
                        errorMessage = errorFields["apellidos"]
                    )

                    ModernTextField(
                        value = municipio,
                        onValueChange = { municipio = it },
                        label = "Municipio",
                        icon = R.drawable.ic_description,
                        errorMessage = errorFields["municipio"]
                    )

                    ModernTextField(
                        value = provincia,
                        onValueChange = { provincia = it },
                        label = "Provincia",
                        icon = R.drawable.ic_description,
                        errorMessage = errorFields["provincia"]
                    )

                    ModernTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = "Descripción (opcional)",
                        icon = R.drawable.ic_description,
                        multiLine = true,
                        errorMessage = errorFields["descripcion"]
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(R.color.background)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "🎯 Intereses",
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.azulPrimario),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = nuevoInteres,
                                    onValueChange = { nuevoInteres = it },
                                    label = { Text("Añadir interés") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_add),
                                            contentDescription = "Añadir",
                                            tint = colorResource(R.color.azulPrimario),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (nuevoInteres.trim().isNotEmpty()) {
                                                if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(nuevoInteres.trim())) {
                                                    generalError = "El interés contiene palabras no permitidas"
                                                } else if (!intereses.contains(nuevoInteres.trim())) {
                                                    intereses.add(nuevoInteres.trim())
                                                    nuevoInteres = ""
                                                    generalError = null
                                                }
                                            }
                                        }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorResource(R.color.azulPrimario),
                                        unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                        focusedLabelColor = colorResource(R.color.azulPrimario),
                                        unfocusedLabelColor = colorResource(R.color.textoSecundario),
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        cursorColor = colorResource(R.color.azulPrimario)
                                    )
                                )
                            }

                            if (intereses.isNotEmpty()) {
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    intereses.forEachIndexed { index, interes ->
                                        Card(
                                            modifier = Modifier
                                                .padding(4.dp),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = colorResource(R.color.azulPrimario).copy(alpha = 0.1f)
                                            ),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = interes,
                                                    color = colorResource(R.color.azulPrimario),
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Eliminar interés",
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clickable { intereses.removeAt(index) },
                                                    tint = colorResource(R.color.error)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (errorFields["intereses"] != null) {
                                Text(
                                    text = errorFields["intereses"]!!,
                                    color = colorResource(R.color.error),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = generalError != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        generalError?.let { errorMsg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colorResource(R.color.error).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = errorMsg,
                                    color = colorResource(R.color.error),
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (validateFields()) {
                                viewModel.registrarUsuario(
                                    context = context,
                                    username = normalizarUsername(username.trim()),
                                    password = password,
                                    passwordRepeat = confirmPassword,
                                    email = email.trim(),
                                    rol = "USER",
                                    municipio = municipio.trim(),
                                    provincia = provincia.trim(),
                                    nombre = nombre.trim(),
                                    apellidos = apellidos.trim(),
                                    descripcion = descripcion.trim(),
                                    intereses = intereses.toList(),
                                    fotoPerfil = imagenPerfil?.toString() ?: ""
                                )
                            } else {
                                generalError = "Por favor, corrige los errores en el formulario"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 16.dp)
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
                                "CREAR CUENTA",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes una cuenta? ",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )

                TextButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Text(
                        text = "Iniciar sesión",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Int,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: ((Boolean) -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String? = null,
    multiLine: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .then(modifier)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    modifier = Modifier.size(20.dp),
                    tint = colorResource(R.color.azulPrimario)
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(
                        onClick = { onPasswordVisibilityChange?.invoke(!passwordVisible) }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = colorResource(R.color.azulPrimario)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = !multiLine,
            maxLines = if (multiLine) 4 else 1,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            isError = errorMessage != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.azulPrimario),
                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                errorBorderColor = colorResource(R.color.error),
                focusedLabelColor = colorResource(R.color.azulPrimario),
                unfocusedLabelColor = colorResource(R.color.textoSecundario),
                focusedTextColor = colorResource(R.color.black),
                unfocusedTextColor = colorResource(R.color.black),
                cursorColor = colorResource(R.color.azulPrimario)
            )
        )

        AnimatedVisibility(
            visible = errorMessage != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Text(
                text = errorMessage ?: "",
                color = colorResource(R.color.error),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(maxWidth = constraints.maxWidth))
        }

        var yPos = 0
        var xPos = 0
        var rowHeight = 0
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        placeables.forEach { placeable ->
            if (xPos + placeable.width > constraints.maxWidth) {
                rowWidths.add(xPos)
                rowHeights.add(rowHeight)
                xPos = 0
                yPos += rowHeight
                rowHeight = 0
            }

            xPos += placeable.width
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        rowWidths.add(xPos)
        rowHeights.add(rowHeight)

        val totalHeight = rowHeights.sum()

        var yPosition = 0
        var xPosition = 0
        var currentRow = 0

        layout(constraints.maxWidth, totalHeight) {
            placeables.forEach { placeable ->
                if (xPosition + placeable.width > constraints.maxWidth) {
                    currentRow++
                    xPosition = 0
                    yPosition += rowHeights[currentRow - 1]
                }

                placeable.place(x = xPosition, y = yPosition)
                xPosition += placeable.width
            }
        }
    }
}