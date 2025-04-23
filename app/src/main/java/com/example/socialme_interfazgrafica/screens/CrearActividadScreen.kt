package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ActividadCreateDTO
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearActividadScreen(comunidadUrl: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Obtener SharedPreferences en lugar de usar SessionManager
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("TOKEN", "") ?: ""
    val userId = sharedPreferences.getString("USER_ID", "") ?: ""

    // Estado para los campos del formulario
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var privada by remember { mutableStateOf(false) }

    // Estado para las fechas
    var fechaInicio by remember { mutableStateOf<Date?>(null) }
    var fechaFinalizacion by remember { mutableStateOf<Date?>(null) }

    // Estado para mostrar los selectores de fecha
    var showFechaInicioDialog by remember { mutableStateOf(false) }
    var showFechaFinDialog by remember { mutableStateOf(false) }

    // Estado para las imágenes
    var imagenes by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imagenesBase64 by remember { mutableStateOf<List<String>>(emptyList()) }

    // Estado para controlar la carga
    var isLoading by remember { mutableStateOf(false) }
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    // Lanzador para seleccionar imágenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            imagenes = imagenes + uris
            scope.launch {
                val base64List = uris.map { uri ->
                    convertToBase64(context, uri)
                }
                imagenesBase64 = imagenesBase64 + base64List.filterNotNull()
            }
        }
    }

    // Formato para mostrar las fechas
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crear Actividad",
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
                    // Nombre de la actividad
                    Text(
                        text = "Nombre de la actividad",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nombre de la actividad") },
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
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Describe la actividad") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.azulPrimario),
                            unfocusedBorderColor = Color.Gray
                        ),
                        minLines = 3
                    )

                    // Lugar
                    Text(
                        text = "Lugar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = lugar,
                        onValueChange = { lugar = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Lugar de la actividad") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = "Lugar",
                                tint = colorResource(R.color.azulPrimario)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.azulPrimario),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    // Fecha de inicio
                    Text(
                        text = "Fecha de inicio",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFechaInicioDialog = true },
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Fecha de inicio",
                                tint = colorResource(R.color.azulPrimario)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = fechaInicio?.let { dateFormat.format(it) } ?: "Seleccionar fecha de inicio",
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Fecha de finalización
                    Text(
                        text = "Fecha de finalización",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFechaFinDialog = true },
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Fecha de finalización",
                                tint = colorResource(R.color.azulPrimario)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = fechaFinalizacion?.let { dateFormat.format(it) } ?: "Seleccionar fecha de finalización",
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Selección privacidad
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Actividad privada",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
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

                    // Sección de imágenes
                    Text(
                        text = "Añadir imágenes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Vista previa de imágenes seleccionadas
                    if (imagenes.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(120.dp)
                        ) {
                            items(imagenes) { imagen ->
                                Box {
                                    AsyncImage(
                                        model = imagen,
                                        contentDescription = "Imagen de actividad",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = {
                                            val index = imagenes.indexOf(imagen)
                                            imagenes = imagenes.filterIndexed { i, _ -> i != index }
                                            imagenesBase64 = imagenesBase64.filterIndexed { i, _ -> i != index }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Botón para añadir imágenes
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.azulPrimario)
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir imagen")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir imágenes")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón para crear actividad
                    Button(
                        onClick = {
                            if (validarCampos(nombre, descripcion, lugar, fechaInicio, fechaFinalizacion)) {
                                scope.launch {
                                    isLoading = true
                                    try {
                                        Log.d("CrearActividad", "Creando actividad con token: ${authToken.take(10)}...")

                                        val actividad = ActividadCreateDTO(
                                            nombre = nombre,
                                            descripcion = descripcion,
                                            comunidad = comunidadUrl,
                                            creador = userId,
                                            lugar = lugar,
                                            fechaInicio = fechaInicio!!,
                                            fechaFinalizacion = fechaFinalizacion!!,
                                            fotosCarruselBase64 = if (imagenesBase64.isNotEmpty()) imagenesBase64 else null,
                                            fotosCarruselIds = null,
                                            privada = privada
                                        )

                                        val response = retrofitService.crearActividad(
                                            token = "Bearer $authToken",
                                            actividadCreateDTO = actividad
                                        )

                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Actividad creada correctamente", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        } else {
                                            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                                            val mensajeError = ErrorUtils.parseErrorMessage(errorMsg)
                                            Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()
                                            Log.e("CrearActividad", "Error: $errorMsg")
                                        }
                                    } catch (e: Exception) {
                                        val mensajeError = ErrorUtils.parseErrorMessage(e.message ?: "Error desconocido")
                                        Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()
                                        Log.e("CrearActividad", "Excepción: ${e.message}", e)
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
                                "CREAR ACTIVIDAD",
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

    // Dialog para seleccionar fecha de inicio
    if (showFechaInicioDialog) {
        DatePickerDialog(
            onDismissRequest = { showFechaInicioDialog = false },
            onDateSelected = { date ->
                fechaInicio = date
                showFechaInicioDialog = false
            }
        )
    }

    // Dialog para seleccionar fecha de finalización
    if (showFechaFinDialog) {
        DatePickerDialog(
            onDismissRequest = { showFechaFinDialog = false },
            onDateSelected = { date ->
                fechaFinalizacion = date
                showFechaFinDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Date) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancelar")
                    }

                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                onDateSelected(Date(millis))
                            }
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}

// Función para validar los campos obligatorios
private fun validarCampos(
    nombre: String,
    descripcion: String,
    lugar: String,
    fechaInicio: Date?,
    fechaFinalizacion: Date?
): Boolean {
    return nombre.isNotBlank() && descripcion.isNotBlank() && lugar.isNotBlank()
            && fechaInicio != null && fechaFinalizacion != null
}

// Función optimizada para convertir imagen a Base64
fun convertToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use { stream ->
            // Decodificar el stream a un bitmap
            val bitmap = BitmapFactory.decodeStream(stream)

            // Redimensionar si es necesario
            val maxSize = 800
            val width = bitmap.width
            val height = bitmap.height

            val newWidth: Int
            val newHeight: Int

            if (width > height && width > maxSize) {
                val ratio = width.toFloat() / height.toFloat()
                newWidth = maxSize
                newHeight = (maxSize / ratio).toInt()
            } else if (height > maxSize) {
                val ratio = height.toFloat() / width.toFloat()
                newHeight = maxSize
                newWidth = (maxSize / ratio).toInt()
            } else {
                newWidth = width
                newHeight = height
            }

            // Redimensionar bitmap si es necesario
            val resizedBitmap = if (width != newWidth || height != newHeight) {
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            // Comprimir el bitmap a JPEG con calidad del 75%
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)

            // Liberar memoria si se creó un nuevo bitmap
            if (resizedBitmap != bitmap) {
                bitmap.recycle()
            }

            // Convertir a base64 sin saltos de línea
            val byteArray = outputStream.toByteArray()
            val sizeInKb = byteArray.size / 1024
            Log.d("ConvertToBase64", "Tamaño de imagen: $sizeInKb KB")

            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        }
    } catch (e: Exception) {
        Log.e("ConvertToBase64", "Error: ${e.message}", e)
        null
    }
}