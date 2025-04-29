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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ComunidadCreateDTO
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CrearComunidadScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Obtener SharedPreferences
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("TOKEN", "") ?: ""
    val username = sharedPreferences.getString("USERNAME", "") ?: ""

    // Estado para los campos del formulario
    var url by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var comunidadGlobal by remember { mutableStateOf(false) }
    var privada by remember { mutableStateOf(false) }

    // Estado para intereses
    var nuevoInteres by remember { mutableStateOf("") }
    var intereses by remember { mutableStateOf<List<String>>(emptyList()) }

    // Estado para la imagen de perfil
    var imagenPerfil by remember { mutableStateOf<Uri?>(null) }
    var imagenPerfilBase64 by remember { mutableStateOf<String?>(null) }

    // Estado para controlar la carga
    var isLoading by remember { mutableStateOf(false) }
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    // Lanzador para seleccionar imagen de perfil
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imagenPerfil = it
            scope.launch {
                imagenPerfilBase64 = convertToBase64(context, it)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crear Comunidad",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.azulPrimario)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorResource(R.color.azulPrimario)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta principal con todos los campos
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Imagen de perfil
                    Text(
                        text = "Imagen de perfil",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imagenPerfil != null) {
                            AsyncImage(
                                model = imagenPerfil,
                                contentDescription = "Imagen de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Añadir imagen",
                                    modifier = Modifier.size(48.dp),
                                    tint = colorResource(R.color.azulPrimario)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Añadir imagen de perfil",
                                    color = colorResource(R.color.azulPrimario)
                                )
                            }
                        }
                    }

                    // URL de la comunidad
                    Text(
                        text = "URL de la comunidad",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("URL única para tu comunidad") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.azulPrimario),
                            unfocusedBorderColor = Color.Gray
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )

                    // Nombre de la comunidad
                    Text(
                        text = "Nombre de la comunidad",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nombre de la comunidad") },
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
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = Color.Black
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Describe tu comunidad") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.azulPrimario),
                            unfocusedBorderColor = Color.Gray
                        ),
                        minLines = 3
                    )

                    // Intereses
                    Text(
                        text = "Intereses",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Añadir interés
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nuevoInteres,
                            onValueChange = { nuevoInteres = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Añadir interés") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (nuevoInteres.isNotBlank() && !intereses.contains(nuevoInteres)) {
                                    intereses = intereses + nuevoInteres
                                    nuevoInteres = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario)
                            )
                        ) {
                            Text("Añadir")
                        }
                    }

                    // Lista de intereses
                    // Replace the FlowRow section with this code
                    if (intereses.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            intereses.forEach { interes ->
                                InputChip(
                                    selected = false,
                                    onClick = { /* No action needed on click */ },
                                    label = { Text(interes) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    intereses = intereses - interes
                                                }
                                        )
                                    },
                                    border = BorderStroke(1.dp, colorResource(R.color.azulPrimario)),
                                    colors = InputChipDefaults.inputChipColors(
                                        containerColor = Color.White,
                                        labelColor = colorResource(R.color.azulPrimario)
                                    )
                                )
                            }
                        }
                    }

                    // Opciones de comunidad
                    Text(
                        text = "Opciones de comunidad",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Comunidad global
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Comunidad global",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        Switch(
                            checked = comunidadGlobal,
                            onCheckedChange = { comunidadGlobal = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colorResource(R.color.azulPrimario),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    // Comunidad privada
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Comunidad privada",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        Switch(
                            checked = privada,
                            onCheckedChange = { privada = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colorResource(R.color.azulPrimario),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón para crear comunidad
                    Button(
                        onClick = {
                            if (validarCampos(url, nombre, descripcion)) {
                                scope.launch {
                                    isLoading = true
                                    try {
                                        Log.d("CrearComunidad", "Creando comunidad con token: ${authToken.take(10)}...")

                                        val comunidad = ComunidadCreateDTO(
                                            url = url,
                                            nombre = nombre,
                                            descripcion = descripcion,
                                            intereses = intereses,
                                            fotoPerfilBase64 = imagenPerfilBase64,
                                            fotoPerfilId = null,
                                            creador = username,
                                            comunidadGlobal = comunidadGlobal,
                                            privada = privada
                                        )

                                        val response = retrofitService.crearComunidad(
                                            token = "Bearer $authToken",
                                            comunidadCreateDTO = comunidad
                                        )

                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Comunidad creada correctamente", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        } else {
                                            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                                            val mensajeError = ErrorUtils.parseErrorMessage(errorMsg)
                                            Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()
                                            Log.e("CrearComunidad", "Error: $errorMsg")
                                        }
                                    } catch (e: Exception) {
                                        val mensajeError = ErrorUtils.parseErrorMessage(e.message ?: "Error desconocido")
                                        Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()
                                        Log.e("CrearComunidad", "Excepción: ${e.message}", e)
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Por favor, completa todos los campos obligatorios",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.azulPrimario)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                "CREAR COMUNIDAD",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Botón para cancelar
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorResource(R.color.azulPrimario)
                        ),
                        shape = RoundedCornerShape(8.dp),
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
    }
}


// Función para validar los campos obligatorios
private fun validarCampos(
    url: String,
    nombre: String,
    descripcion: String
): Boolean {
    return url.isNotBlank() && nombre.isNotBlank() && descripcion.isNotBlank()
}
