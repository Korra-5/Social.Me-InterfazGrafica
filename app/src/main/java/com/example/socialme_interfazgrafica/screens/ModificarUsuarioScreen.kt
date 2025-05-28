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
import androidx.compose.material.icons.filled.Clear
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
import com.example.socialme_interfazgrafica.utils.PalabrasMalsonantesValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

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
    val newUsername = remember { mutableStateOf("") } // Nuevo campo para el username editable
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

    // Función de validación actualizada
    fun validarCampos(
        newUsername: String,
        nombre: String,
        apellido: String,
        email: String,
        descripcion: String,
        municipio: String,
        provincia: String,
        intereses: List<String>
    ): Pair<Boolean, String?> {

        if (newUsername.isEmpty()) {
            return Pair(false, "El nombre de usuario no puede estar vacío")
        }

        if (newUsername.length < 3) {
            return Pair(false, "El nombre de usuario debe tener al menos 3 caracteres")
        }

        if (newUsername.length > 20) {
            return Pair(false, "El nombre de usuario no puede tener más de 20 caracteres")
        }

        // Verificar que solo contenga letras, números y guiones bajos
        if (!newUsername.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            return Pair(false, "El nombre de usuario solo puede contener letras, números y guiones bajos")
        }

        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(newUsername)) {
            return Pair(false, "El nombre de usuario contiene palabras no permitidas")
        }

        if (nombre.isEmpty()) {
            return Pair(false, "El nombre no puede estar vacío")
        }

        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(nombre)) {
            return Pair(false, "El nombre contiene palabras no permitidas")
        }

        if (apellido.isEmpty()) {
            return Pair(false, "El apellido no puede estar vacío")
        }

        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(apellido)) {
            return Pair(false, "El apellido contiene palabras no permitidas")
        }

        if (email.isEmpty()) {
            return Pair(false, "El email no puede estar vacío")
        }

        // Validar formato de email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Pair(false, "El formato del email no es válido")
        }

        if (descripcion.isNotEmpty() && PalabrasMalsonantesValidator.contienepalabrasmalsonantes(descripcion)) {
            return Pair(false, "La descripción contiene palabras no permitidas")
        }

        if (municipio.isNotEmpty() && PalabrasMalsonantesValidator.contienepalabrasmalsonantes(municipio)) {
            return Pair(false, "El municipio contiene palabras no permitidas")
        }

        if (provincia.isNotEmpty() && PalabrasMalsonantesValidator.contienepalabrasmalsonantes(provincia)) {
            return Pair(false, "La provincia contiene palabras no permitidas")
        }

        if (PalabrasMalsonantesValidator.validarLista(intereses)) {
            return Pair(false, "Algunos intereses contienen palabras no permitidas")
        }

        return Pair(true, null)
    }

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
                newUsername.value = usuario.username // Inicializar con el username actual
                nombre.value = usuario.nombre
                apellido.value = usuario.apellido
                email.value = usuario.email
                descripcion.value = usuario.descripcion
                intereses.value = usuario.intereses

                // Manejar dirección que puede ser null
                usuario.direccion?.let { dir ->
                    municipio.value = dir.municipio ?: ""
                    provincia.value = dir.provincia ?: ""
                }

                Log.d(
                    "ModificarUsuario",
                    "Valores asignados: username=${newUsername.value}, nombre=${nombre.value}, apellido=${apellido.value}"
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
            scope.launch {
                try {
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
                    fotoPerfilUri.value = null
                }
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
                        .background(colorResource(R.color.cyanSecundario), CircleShape)
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
                        containerColor = colorResource(R.color.card_colors)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Foto de perfil
                        Text(
                            text = "Foto de perfil",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(colorResource(R.color.cyanSecundario))
                                    .border(1.dp, colorResource(R.color.azulPrimario), CircleShape)
                            ) {
                                if (fotoPerfilUri.value != null) {
                                    AsyncImage(
                                        model = fotoPerfilUri.value,
                                        contentDescription = "Foto de perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else if (usuarioOriginal.value?.fotoPerfilId?.isNotEmpty() == true) {
                                    val fotoPerfilUrl =
                                        "${baseUrl}/files/download/${usuarioOriginal.value?.fotoPerfilId}"
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(fotoPerfilUrl)
                                            .crossfade(true)
                                            .setHeader("Authorization", "Bearer $authToken")
                                            .build(),
                                        contentDescription = "Foto de perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Foto de perfil",
                                        tint = colorResource(R.color.azulPrimario),
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
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Seleccionar imagen", color = Color.White)
                            }
                        }

                        // Username (ahora editable)
                        Text(
                            text = "Nombre de usuario",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = newUsername.value,
                            onValueChange = { newUsername.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nombre de usuario", color = colorResource(R.color.textoSecundario)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                focusedTextColor = colorResource(R.color.textoPrimario),
                                unfocusedTextColor = colorResource(R.color.textoPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Email
                        Text(
                            text = "Correo electrónico",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = email.value,
                            onValueChange = { email.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Correo electrónico", color = colorResource(R.color.textoSecundario)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                focusedTextColor = colorResource(R.color.textoPrimario),
                                unfocusedTextColor = colorResource(R.color.textoPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Nombre
                        Text(
                            text = "Nombre",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = nombre.value,
                            onValueChange = { nombre.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nombre", color = colorResource(R.color.textoSecundario)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                focusedTextColor = colorResource(R.color.textoPrimario),
                                unfocusedTextColor = colorResource(R.color.textoPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Apellido
                        Text(
                            text = "Apellido",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = apellido.value,
                            onValueChange = { apellido.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Apellido", color = colorResource(R.color.textoSecundario)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                focusedTextColor = colorResource(R.color.textoPrimario),
                                unfocusedTextColor = colorResource(R.color.textoPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Descripción
                        Text(
                            text = "Descripción",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = descripcion.value,
                            onValueChange = { descripcion.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Describe tu perfil", color = colorResource(R.color.textoSecundario)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                focusedTextColor = colorResource(R.color.textoPrimario),
                                unfocusedTextColor = colorResource(R.color.textoPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3
                        )

                        // Intereses/Tags
                        Text(
                            text = "Intereses",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
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
                                placeholder = { Text("Añadir interés", color = colorResource(R.color.textoSecundario)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorResource(R.color.azulPrimario),
                                    unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                    focusedTextColor = colorResource(R.color.textoPrimario),
                                    unfocusedTextColor = colorResource(R.color.textoPrimario)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    if (interesInput.value.isNotEmpty()) {
                                        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(interesInput.value)) {
                                            Toast.makeText(context, "El interés contiene palabras no permitidas", Toast.LENGTH_SHORT).show()
                                        } else if (!intereses.value.contains(interesInput.value)) {
                                            intereses.value = intereses.value + interesInput.value
                                            interesInput.value = ""
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(R.color.azulPrimario)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Añadir",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
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
                                    shape = RoundedCornerShape(50.dp),
                                    color = colorResource(R.color.cyanSecundario)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = interes,
                                            color = colorResource(R.color.azulPrimario),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = colorResource(R.color.azulPrimario),
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable {
                                                    intereses.value = intereses.value.filter { it != interes }
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
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Tarjeta de dirección
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Municipio
                                Text(
                                    text = "Municipio",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.textoPrimario)
                                )

                                OutlinedTextField(
                                    value = municipio.value,
                                    onValueChange = { municipio.value = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Municipio", color = colorResource(R.color.textoSecundario)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorResource(R.color.azulPrimario),
                                        unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                        focusedTextColor = colorResource(R.color.textoPrimario),
                                        unfocusedTextColor = colorResource(R.color.textoPrimario)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Provincia
                                Text(
                                    text = "Provincia",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.textoPrimario)
                                )

                                OutlinedTextField(
                                    value = provincia.value,
                                    onValueChange = { provincia.value = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Provincia", color = colorResource(R.color.textoSecundario)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorResource(R.color.azulPrimario),
                                        unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                        focusedTextColor = colorResource(R.color.textoPrimario),
                                        unfocusedTextColor = colorResource(R.color.textoPrimario)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        // Botones de acción
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val (isValid, errorMsg) = validarCampos(
                                    newUsername.value,
                                    nombre.value,
                                    apellido.value,
                                    email.value,
                                    descripcion.value,
                                    municipio.value,
                                    provincia.value,
                                    intereses.value
                                )

                                if (!isValid) {
                                    errorMessage.value = errorMsg
                                    return@Button
                                }

                                // Crear objeto direccion con los valores actuales
                                val direccionObj = Direccion(
                                    municipio = municipio.value,
                                    provincia = provincia.value
                                )

                                // Verificar si el username cambió
                                val usernameCambiado = newUsername.value != username

                                // Crear UsuarioUpdateDTO - SIEMPRE enviar valores actuales (no null)
                                val usuarioUpdate = UsuarioUpdateDTO(
                                    currentUsername = username,
                                    newUsername = if (usernameCambiado) newUsername.value else null,
                                    email = email.value,                    // Siempre enviar valor actual
                                    nombre = nombre.value,                  // Siempre enviar valor actual
                                    apellido = apellido.value,              // Siempre enviar valor actual
                                    descripcion = descripcion.value,        // Siempre enviar valor actual
                                    intereses = intereses.value,            // Siempre enviar lista actual
                                    fotoPerfilBase64 = if (!fotoPerfilBase64.value.isNullOrEmpty()) {
                                        fotoPerfilBase64.value
                                    } else {
                                        null
                                    },
                                    fotoPerfilId = usuarioOriginal.value?.fotoPerfilId,
                                    direccion = direccionObj                // Siempre enviar dirección actual
                                )

                                isSaving.value = true
                                scope.launch {
                                    try {
                                        Log.d("ModificarUsuario", "Iniciando petición de modificación")
                                        Log.d("ModificarUsuario", "Username actual: $username, Nuevo username: ${newUsername.value}")

                                        val response = withContext(Dispatchers.IO) {
                                            retrofitService.iniciarModificacionUsuario(
                                                "Bearer $authToken",
                                                usuarioUpdateDTO = usuarioUpdate
                                            )
                                        }

                                        Log.d("ModificarUsuario", "Respuesta recibida: ${response.code()}")
                                        if (response.isSuccessful && response.body() != null) {
                                            val responseBody = response.body()!!
                                            val requiresVerification = responseBody["requiresVerification"] == "true"

                                            if (requiresVerification) {
                                                // Si requiere verificación, navegar a la pantalla de verificación
                                                val emailToVerify = responseBody["email"] ?: email.value
                                                withContext(Dispatchers.Main) {
                                                    navController.navigate(
                                                        AppScreen.EmailVerificationScreen.createRoute(
                                                            email = emailToVerify,
                                                            username = if (usernameCambiado) newUsername.value else username,
                                                            isRegistration = false
                                                        )
                                                    )
                                                }
                                            } else {
                                                // Si no requiere verificación, modificación completada
                                                // Si cambió el username, actualizar SharedPreferences
                                                if (usernameCambiado) {
                                                    val editor = sharedPreferences.edit()
                                                    editor.putString("USERNAME", newUsername.value)
                                                    editor.apply()
                                                    Log.d("ModificarUsuario", "Username actualizado en SharedPreferences: ${newUsername.value}")
                                                }

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
                                            val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                                            Log.e("ModificarUsuario", "Error al modificar: ${response.code()} - $errorBody")
                                            errorMessage.value = "Error al modificar: ${response.code()} - ${response.message()}\n$errorBody"
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ModificarUsuario", "Excepción al modificar", e)
                                        Log.e("ModificarUsuario", "Tipo de excepción: ${e.javaClass.name}")
                                        Log.e("ModificarUsuario", "Mensaje de excepción: ${e.message}")
                                        Log.e("ModificarUsuario", "Stack trace: ${e.stackTraceToString()}")
                                        errorMessage.value = ErrorUtils.parseErrorMessage(e.message ?: "Error desconocido")
                                    } finally {
                                        isSaving.value = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            enabled = !isSaving.value,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario),
                                disabledContainerColor = colorResource(R.color.textoSecundario)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSaving.value) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    "GUARDAR CAMBIOS",
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
                                .height(54.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorResource(R.color.azulPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, colorResource(R.color.azulPrimario))
                        ) {
                            Text(
                                "CANCELAR",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Mensaje de error si existe
            if (errorMessage.value != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEDED)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Error",
                            tint = colorResource(R.color.error),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage.value ?: "",
                            color = colorResource(R.color.error),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Auto-limpiar mensaje de error después de 5 segundos
                LaunchedEffect(errorMessage.value) {
                    if (errorMessage.value != null) {
                        kotlinx.coroutines.delay(5000)
                        errorMessage.value = null
                    }
                }
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
                        .size(120.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(R.color.azulPrimario),
                            strokeWidth = 3.dp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Guardando...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(R.color.textoSecundario)
                        )
                    }
                }
            }
        }
    }
}