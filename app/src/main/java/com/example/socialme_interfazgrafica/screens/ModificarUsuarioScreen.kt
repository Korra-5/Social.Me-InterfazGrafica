package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.model.Direccion
import com.example.socialme_interfazgrafica.model.UsuarioDTO
import com.example.socialme_interfazgrafica.model.UsuarioUpdateDTO
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModificarUsuarioScreen(username: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("TOKEN", "") ?: ""
    val baseUrl = "https://social-me-tfg.onrender.com"

    // Estados para los datos del usuario
    val usuarioOriginal = remember { mutableStateOf<UsuarioDTO?>(null) }
    val nombre = remember { mutableStateOf("") }
    val apellido = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val descripcion = remember { mutableStateOf("") }
    val intereses = remember { mutableStateOf<List<String>>(emptyList()) }
    val interesInput = remember { mutableStateOf("") }

    // Dirección (con valores por defecto)
    val municipio = remember { mutableStateOf("") }
    val provincia = remember { mutableStateOf("") }

    // Estados para la carga y errores
    val isLoading = remember { mutableStateOf(true) }
    val isSaving = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Estados para manejo de imágenes
    val fotoPerfilBase64 = remember { mutableStateOf<String?>(null) }
    val fotoPerfilUri = remember { mutableStateOf<Uri?>(null) }

    // Carga inicial de datos del usuario
    LaunchedEffect(username) {
        isLoading.value = true
        try {
            Log.d("ModificarUsuario", "Intentando cargar datos para usuario: $username")
            Log.d("ModificarUsuario", "Token de autenticación: ${authToken.take(10)}...")

            val response = withContext(Dispatchers.IO) {
                retrofitService.verUsuarioPorUsername("Bearer $authToken", username)
            }

            Log.d("ModificarUsuario", "Respuesta del servidor: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val usuario = response.body()!!
                Log.d(
                    "ModificarUsuario",
                    "Datos recibidos: nombre=${usuario.nombre}, apellido=${usuario.apellido}"
                )

                usuarioOriginal.value = usuario
                nombre.value = usuario.nombre
                apellido.value = usuario.apellido
                email.value = usuario.email
                descripcion.value = usuario.descripcion
                intereses.value = usuario.intereses

                // Asignación de dirección
                municipio.value = usuario.direccion.municipio ?: ""
                provincia.value = usuario.direccion.provincia ?: ""

                Log.d(
                    "ModificarUsuario",
                    "Valores asignados: nombre=${nombre.value}, apellido=${apellido.value}"
                )
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                Log.e("ModificarUsuario", "Error al cargar datos: ${response.code()} - $errorBody")
                errorMessage.value =
                    "Error al cargar los datos del usuario: ${response.code()} - ${response.message()}"
            }
        } catch (e: Exception) {
            Log.e("API", "Exception class: ${e.javaClass.name}")
            Log.e("API", "Message: ${e.message}")
            Log.e("API", "Stack trace: ${e.stackTraceToString()}")
            errorMessage.value = ErrorUtils.parseErrorMessage(e.message ?: "Error desconocido")
        } finally {
            isLoading.value = false
        }
    }

    // Launcher para seleccionar imagen de perfil
    val fotoPerfilLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fotoPerfilUri.value = it
            // Convertir a Base64 con compresión y verificación
            scope.launch {
                try {
                    // Usar la función de compresión
                    val base64 = compressAndConvertToBase64(it, context)
                    if (base64 != null) {
                        fotoPerfilBase64.value = base64
                        Log.d(
                            "ModificarUsuario",
                            "Base64 generado para perfil (longitud): ${base64.length}"
                        )
                    } else {
                        Toast.makeText(context, "Error al procesar la imagen", Toast.LENGTH_SHORT)
                            .show()
                        fotoPerfilUri.value = null
                    }
                } catch (e: Exception) {
                    Log.e("ModificarUsuario", "Error al procesar imagen de perfil", e)
                    Toast.makeText(
                        context,
                        "Error al procesar la imagen: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    fotoPerfilUri.value = null // Limpiar URI si hay error
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Barra superior con botón de retroceso y título
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = colorResource(R.color.azulPrimario)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Editar Perfil",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.azulPrimario)
                )
            }

            if (isLoading.value) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(R.color.azulPrimario))
                }
            } else {
                // Formulario para editar los datos del usuario
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Foto de perfil
                        Text(
                            text = "Foto de perfil",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Vista previa de la imagen de perfil
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                                    .border(1.dp, Color.Gray, CircleShape)
                            ) {
                                // Si hay una nueva imagen seleccionada, mostrarla
                                if (fotoPerfilUri.value != null) {
                                    AsyncImage(
                                        model = fotoPerfilUri.value,
                                        contentDescription = "Foto de perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else if (usuarioOriginal.value?.fotoPerfilId?.isNotEmpty() == true) {
                                    // Si no hay nueva imagen pero existe una foto de perfil, mostrarla
                                    val fotoPerfilUrl =
                                        "${baseUrl}/files/download/${usuarioOriginal.value?.fotoPerfilId}"
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(fotoPerfilUrl)
                                            .crossfade(true)
                                            .setHeader("Authorization", authToken)
                                            .build(),
                                        contentDescription = "Foto de perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Si no hay imagen, mostrar un ícono
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Foto de perfil",
                                        tint = Color.Gray,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Button(
                                onClick = {
                                    fotoPerfilLauncher.launch("image/*")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(R.color.azulPrimario)
                                )
                            ) {
                                Text("Seleccionar imagen")
                            }
                        }

                        // Username (no editable)
                        Text(
                            text = "Nombre de usuario",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = username,
                            onValueChange = { /* No permitir cambios */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Nombre de usuario") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Gray,
                                unfocusedBorderColor = Color.Gray
                            ),
                            readOnly = true, // Campo no editable
                            enabled = false
                        )

                        // Email
                        Text(
                            text = "Correo electrónico",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = email.value,
                            onValueChange = { email.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Correo electrónico") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        // Nombre
                        Text(
                            text = "Nombre",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = nombre.value,
                            onValueChange = { nombre.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Nombre") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        // Apellido
                        Text(
                            text = "Apellido",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = apellido.value,
                            onValueChange = { apellido.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Apellido") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        // Descripción
                        Text(
                            text = "Descripción",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = descripcion.value,
                            onValueChange = { descripcion.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Describe tu perfil") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        // Intereses/Tags
                        Text(
                            text = "Intereses",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = interesInput.value,
                                onValueChange = { interesInput.value = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                placeholder = { Text("Añadir interés") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorResource(R.color.azulPrimario),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            Button(
                                onClick = {
                                    if (interesInput.value.isNotEmpty() && !intereses.value.contains(
                                            interesInput.value
                                        )
                                    ) {
                                        intereses.value = intereses.value + interesInput.value
                                        interesInput.value = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(R.color.azulPrimario)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Añadir",
                                    tint = Color.White
                                )
                            }
                        }

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            intereses.value.forEach { interes ->
                                Surface(
                                    modifier = Modifier,
                                    shape = RoundedCornerShape(16.dp),
                                    color = colorResource(R.color.cyanSecundario)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = interes,
                                            color = colorResource(R.color.azulPrimario),
                                            fontSize = 14.sp
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = colorResource(R.color.azulPrimario),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    intereses.value =
                                                        intereses.value.filter { it != interes }
                                                }
                                        )
                                    }
                                }
                            }
                        }

                        // Sección de dirección
                        Text(
                            text = "Dirección",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Municipio
                        Text(
                            text = "Municipio",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = municipio.value,
                            onValueChange = { municipio.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Municipio") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        // Provincia
                        Text(
                            text = "Provincia",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = provincia.value,
                            onValueChange = { provincia.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Provincia") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        Button(
                            onClick = {
                                if (nombre.value.isEmpty()) {
                                    errorMessage.value = "El nombre no puede estar vacío"
                                    return@Button
                                }

                                if (apellido.value.isEmpty()) {
                                    errorMessage.value = "El apellido no puede estar vacío"
                                    return@Button
                                }

                                if (email.value.isEmpty()) {
                                    errorMessage.value = "El email no puede estar vacío"
                                    return@Button
                                }

                                val direccion = Direccion(
                                    municipio = municipio.value,
                                    provincia = provincia.value
                                )

                                // Log de los datos que se van a enviar
                                Log.d(
                                    "ModificarUsuario",
                                    "Enviando datos: nombre=${nombre.value}, apellido=${apellido.value}"
                                )
                                Log.d(
                                    "ModificarUsuario",
                                    "¿Hay foto de perfil base64? ${fotoPerfilBase64.value != null}"
                                )

                                // Preparar objeto de actualización
                                val usuarioUpdate = UsuarioUpdateDTO(
                                    currentUsername = username,
                                    newUsername = null, // No permitir cambios de username
                                    email = email.value,
                                    nombre = nombre.value,
                                    apellido = apellido.value,
                                    descripcion = descripcion.value,
                                    intereses = intereses.value,
                                    fotoPerfilBase64 = fotoPerfilBase64.value,
                                    fotoPerfilId = usuarioOriginal.value?.fotoPerfilId,
                                    direccion = direccion
                                )

                                // Añadir logs para depuración
                                Log.d("ModificarUsuario", "Token: ${authToken.take(10)}...")
                                Log.d("ModificarUsuario", "Username actual: $username")

                                // Verificar datos de imágenes
                                if (fotoPerfilBase64.value != null) {
                                    Log.d(
                                        "ModificarUsuario",
                                        "Foto perfil Base64 longitud: ${fotoPerfilBase64.value!!.length}"
                                    )
                                    Log.d(
                                        "ModificarUsuario",
                                        "Foto perfil Base64 primeros 30 chars: ${
                                            fotoPerfilBase64.value!!.take(30)
                                        }"
                                    )
                                }

                                // Guardar el email original para compararlo después
                                val emailOriginal = usuarioOriginal.value?.email ?: ""
                                val emailCambiado = email.value != emailOriginal

                                if (emailCambiado) {
                                    Log.d("ModificarUsuario", "El email ha cambiado de $emailOriginal a ${email.value}")
                                }

                                // Enviar actualización
                                isSaving.value = true
                                scope.launch {
                                    try {
                                        Log.d(
                                            "ModificarUsuario",
                                            "Iniciando petición de actualización"
                                        )
                                        val response = withContext(Dispatchers.IO) {
                                            retrofitService.modificarUsuario(
                                                "Bearer $authToken",
                                                usuarioUpdateDTO = usuarioUpdate
                                            )
                                        }

                                        Log.d(
                                            "ModificarUsuario",
                                            "Respuesta recibida: ${response.code()}"
                                        )
                                        if (response.isSuccessful) {
                                            // Verificar si el email ha cambiado
                                            if (emailCambiado) {
                                                // Si el email ha cambiado, navegar a la pantalla de verificación
                                                Log.d("ModificarUsuario", "Email cambiado. Redirigiendo a verificación")
                                                withContext(Dispatchers.Main) {
                                                    navController.navigate(
                                                        AppScreen.EmailVerificationScreen.createRoute(
                                                            email = email.value,
                                                            username = username,
                                                            isRegistration = false
                                                        )
                                                    )
                                                }
                                            } else {
                                                // Si el email no ha cambiado, simplemente volver atrás
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Perfil actualizado correctamente",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    navController.popBackStack()
                                                }
                                            }
                                        } else {
                                            val errorBody = response.errorBody()?.string()
                                                ?: "Sin cuerpo de error"
                                            Log.e(
                                                "ModificarUsuario",
                                                "Error al actualizar: ${response.code()} - $errorBody"
                                            )
                                            errorMessage.value =
                                                "Error al actualizar: ${response.code()} - ${response.message()}\n$errorBody"
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ModificarUsuario", "Excepción al actualizar", e)
                                        Log.e(
                                            "ModificarUsuario",
                                            "Tipo de excepción: ${e.javaClass.name}"
                                        )
                                        Log.e(
                                            "ModificarUsuario",
                                            "Mensaje de excepción: ${e.message}"
                                        )
                                        Log.e(
                                            "ModificarUsuario",
                                            "Stack trace: ${e.stackTraceToString()}"
                                        )
                                        errorMessage.value = ErrorUtils.parseErrorMessage(
                                            e.message ?: "Error desconocido"
                                        )
                                    } finally {
                                        isSaving.value = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp
                            ),
                            enabled = !isSaving.value
                        ) {
                            if (isSaving.value) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "GUARDAR CAMBIOS",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Botón para cancelar
                        OutlinedButton(
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(top = 8.dp, bottom = 16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorResource(R.color.azulPrimario)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colorResource(R.color.azulPrimario))
                        ) {
                            Text(
                                text = "CANCELAR",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Mensaje de error si existe
            if (errorMessage.value != null) {
                Text(
                    text = errorMessage.value ?: "",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Overlay de carga mientras se está guardando
        if (isSaving.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(100.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(R.color.azulPrimario)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Guardando...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}