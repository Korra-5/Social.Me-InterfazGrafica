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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.example.socialme_interfazgrafica.viewModel.RegistroState
import com.example.socialme_interfazgrafica.viewModel.UserViewModel

@Composable
fun RegistroUsuarioScreen(navController: NavController, viewModel: UserViewModel) {
    // Estados para los campos del formulario
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var municipio by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }

    // Estados para intereses e imagen de perfil
    var intereses = remember { mutableStateListOf<String>() }
    var nuevoInteres by remember { mutableStateOf("") }
    var imagenPerfil by remember { mutableStateOf<Uri?>(null) }

    // Estados para validación
    val errorFields = remember { mutableStateMapOf<String, String>() }
    var isLoading by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf<String?>(null) }

    // Launcher para selección de imagen
    val imagenLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenPerfil = uri
    }

    // Observar estado de registro
    val registroState by viewModel.registroState.observeAsState()

    // Efecto para manejar cambios en el estado de registro
    // Efecto para manejar cambios en el estado de registro
    LaunchedEffect(registroState) {
        when (registroState) {
            is RegistroState.Loading -> {
                isLoading = true
                generalError = null
            }
            is RegistroState.Success -> {
                isLoading = false
                // En lugar de volver a login, navegamos a la pantalla de verificación
                navController.navigate(
                    AppScreen.EmailVerificationScreen.createRoute(
                        email = email.trim(),
                        username = username.trim(),
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

    // Función para validar campos
    fun validateFields(): Boolean {
        errorFields.clear()

        // Validación de usuario
        if (username.trim().isEmpty()) {
            errorFields["username"] = "El nombre de usuario es requerido"
        }

        // Validación de email
        if (email.trim().isEmpty()) {
            errorFields["email"] = "El email es requerido"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            errorFields["email"] = "El email no es válido"
        }

        // Validación de contraseña
        if (password.isEmpty()) {
            errorFields["password"] = "La contraseña es requerida"
        } else if (password.length < 6) {
            errorFields["password"] = "La contraseña debe tener al menos 6 caracteres"
        }

        // Validación de confirmación de contraseña
        if (confirmPassword.isEmpty()) {
            errorFields["confirmPassword"] = "Debe confirmar la contraseña"
        } else if (password != confirmPassword) {
            errorFields["confirmPassword"] = "Las contraseñas no coinciden"
        }

        // Validar municipio y provincia
        if (municipio.trim().isEmpty()) {
            errorFields["municipio"] = "El municipio es requerido"
        }
        if (provincia.trim().isEmpty()) {
            errorFields["provincia"] = "La provincia es requerida"
        }

        // Validar nombre
        if (nombre.trim().isEmpty()) {
            errorFields["nombre"] = "El nombre es requerido"
        }

        // Validar apellidos
        if (apellidos.trim().isEmpty()) {
            errorFields["apellidos"] = "Los apellidos son requeridos"
        }

        // Validar descripción (opcional, pero con límite de longitud)
        if (descripcion.trim().length > 500) {
            errorFields["descripcion"] = "La descripción no puede superar 500 caracteres"
        }

        return errorFields.isEmpty()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 40.dp, bottom = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "Crear una cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Selector de imagen de perfil
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.cyanSecundario))
                    .clickable { imagenLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imagenPerfil == null) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir foto de perfil",
                        modifier = Modifier.size(50.dp),
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
            if (errorFields["imagenPerfil"] != null) {
                Text(
                    text = errorFields["imagenPerfil"]!!,
                    color = colorResource(R.color.error),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Campos de registro
            CustomTextField(
                value = username,
                onValueChange = { username = it },
                label = "Nombre de usuario",
                icon = R.drawable.ic_user,
                errorMessage = errorFields["username"],
                iconSize = 20.dp
            )

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                icon = R.drawable.ic_user,
                keyboardType = KeyboardType.Email,
                errorMessage = errorFields["email"],
                iconSize = 20.dp
            )

            CustomTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                icon = R.drawable.ic_lock,
                isPassword = true,
                errorMessage = errorFields["password"],
                iconSize = 20.dp
            )

            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar contraseña",
                icon = R.drawable.ic_lock,
                isPassword = true,
                errorMessage = errorFields["confirmPassword"],
                iconSize = 20.dp
            )

            CustomTextField(
                value = municipio,
                onValueChange = { municipio = it },
                label = "Municipio",
                icon = R.drawable.ic_description,
                errorMessage = errorFields["municipio"],
                iconSize = 20.dp
            )

            CustomTextField(
                value = provincia,
                onValueChange = { provincia = it },
                label = "Provincia",
                icon = R.drawable.ic_description,
                errorMessage = errorFields["provincia"],
                iconSize = 20.dp
            )

            // Nuevos campos: nombre, apellidos, descripción
            CustomTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = "Nombre",
                icon = R.drawable.ic_user,
                errorMessage = errorFields["nombre"],
                iconSize = 20.dp
            )

            CustomTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = "Apellidos",
                icon = R.drawable.ic_user,
                errorMessage = errorFields["apellidos"],
                iconSize = 20.dp
            )

            CustomTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = "Descripción (opcional)",
                icon = R.drawable.ic_description,
                multiLine = true,
                errorMessage = errorFields["descripcion"],
                iconSize = 20.dp
            )

            // Sección de intereses
            Text(
                text = "Intereses",
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.textoPrimario),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Start
            )

            // Campo para añadir intereses
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomTextField(
                    value = nuevoInteres,
                    onValueChange = { nuevoInteres = it },
                    label = "Añadir interés",
                    icon = R.drawable.ic_add,
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Done,
                    onImeActionPerformed = {
                        if (nuevoInteres.trim().isNotEmpty()) {
                            if (!intereses.contains(nuevoInteres.trim())) {
                                intereses.add(nuevoInteres.trim())
                            }
                            nuevoInteres = ""
                        }
                    }
                )
            }

            // Mostrar intereses añadidos
            if (intereses.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    intereses.forEachIndexed { index, interes ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(
                                    color = colorResource(R.color.cyanSecundario),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = interes,
                                    color = colorResource(R.color.black),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar interés",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            intereses.removeAt(index)
                                        },
                                    tint = colorResource(R.color.error)
                                )
                            }
                        }
                    }
                }
            }

            // Mensaje de error general
            if (generalError != null) {
                Text(
                    text = generalError!!,
                    color = colorResource(R.color.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Botón de registro
            Button(
                onClick = {
                    if (validateFields()) {
                            viewModel.registrarUsuario(
                                username = username.trim(),
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
                                fotoPerfil = imagenPerfil?.toString() ?: "" // Asegúrate de que esto no sea null
                            )

                    } else {
                        generalError = "Por favor, corrige los errores en el formulario"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 16.dp),
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
                    Text("Registrarse", fontSize = 16.sp)
                }
            }

            // Enlace a login
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes una cuenta? ",
                    color = colorResource(R.color.textoSecundario)
                )

                TextButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(R.color.azulPrimario)
                    )
                ) {
                    Text(
                        text = "Iniciar sesión",
                        color = colorResource(R.color.azulPrimario),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Int,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String? = null,
    iconSize: Dp = 20.dp,
    multiLine: Boolean = false,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    onImeActionPerformed: (() -> Unit)? = null
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
                    modifier = Modifier.size(iconSize),
                    tint = colorResource(R.color.azulPrimario)
                )
            },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeActionPerformed?.invoke() }
            ),
            singleLine = !multiLine,
            maxLines = if (multiLine) 4 else 1,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = errorMessage != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.azulPrimario),
                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                errorBorderColor = colorResource(R.color.error),
                focusedLabelColor = colorResource(R.color.azulPrimario),
                unfocusedLabelColor = colorResource(R.color.textoSecundario),
                focusedTextColor = colorResource(R.color.black),
                unfocusedTextColor = colorResource(R.color.black),
                errorTextColor = colorResource(R.color.black)
            )
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
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

        // First pass: determine positions and row heights
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

        // Add last row
        rowWidths.add(xPos)
        rowHeights.add(rowHeight)

        // Calculate total height
        val totalHeight = rowHeights.sum()

        // Second pass: place the elements
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