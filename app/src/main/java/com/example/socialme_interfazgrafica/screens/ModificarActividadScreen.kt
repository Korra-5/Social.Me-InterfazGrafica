package com.example.socialme_interfazgrafica

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ActividadUpdateDTO
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ModificarActividadScreen(actividadId: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("TOKEN", "") ?: ""
    val baseUrl = "https://social-me-tfg.onrender.com"

    // Estados para los datos de la actividad
    val actividadOriginal = remember { mutableStateOf<ActividadDTO?>(null) }
    val nombre = remember { mutableStateOf("") }
    val descripcion = remember { mutableStateOf("") }
    val fechaInicio = remember { mutableStateOf<Date?>(null) }
    val fechaFinalizacion = remember { mutableStateOf<Date?>(null) }
    val lugar = remember { mutableStateOf("") }

    // Estado para mostrar diálogos de fecha y hora
    val showFechaInicioDatePicker = remember { mutableStateOf(false) }
    val showFechaInicioTimePicker = remember { mutableStateOf(false) }
    val showFechaFinDatePicker = remember { mutableStateOf(false) }
    val showFechaFinTimePicker = remember { mutableStateOf(false) }

    // Estados para la carga y errores
    val isLoading = remember { mutableStateOf(true) }
    val isSaving = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Estado para confirmar eliminación
    val showDeleteConfirmation = remember { mutableStateOf(false) }
    val isDeleting = remember { mutableStateOf(false) }

    // Estados para manejo de imágenes
    val fotosCarruselBase64 = remember { mutableStateOf<List<String>>(emptyList()) }
    val fotosCarruselUri = remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Formateador de fecha
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Carga inicial de datos de la actividad
    LaunchedEffect(actividadId) {
        isLoading.value = true
        try {
            Log.d("ModificarActividad", "Intentando cargar datos para actividad con ID: $actividadId")
            Log.d("ModificarActividad", "Token de autenticación: ${authToken.take(10)}...")

            val response = withContext(Dispatchers.IO) {
                retrofitService.verActividadPorId("Bearer $authToken", actividadId)
            }

            Log.d("ModificarActividad", "Respuesta del servidor: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val actividad = response.body()!!
                Log.d("ModificarActividad", "Datos recibidos: nombre=${actividad.nombre}, descripción=${actividad.descripcion}")

                actividadOriginal.value = actividad
                nombre.value = actividad.nombre
                descripcion.value = actividad.descripcion
                fechaInicio.value = actividad.fechaInicio
                fechaFinalizacion.value = actividad.fechaFinalizacion
                lugar.value = actividad.lugar

                Log.d("ModificarActividad", "Valores asignados: nombre=${nombre.value}, desc=${descripcion.value}")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                Log.e("ModificarActividad", "Error al cargar datos: ${response.code()} - $errorBody")
                errorMessage.value = "Error al cargar los datos de la actividad: ${response.code()} - ${response.message()}"
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

    // Launcher para seleccionar imágenes de carrusel
    val fotosCarruselLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            fotosCarruselUri.value = uris
            // Convertir a Base64 con compresión y verificación
            scope.launch {
                val newFotosBase64 = mutableListOf<String>()
                var errorOcurred = false

                uris.forEachIndexed { index, uri ->
                    try {
                        // Usar la función de compresión
                        val base64 = compressAndConvertToBase64(uri, context)
                        if (base64 != null) {
                            newFotosBase64.add(base64)
                            Log.d("ModificarActividad", "Base64 generado para carrusel $index (longitud): ${base64.length}")
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error al procesar la imagen $index", Toast.LENGTH_SHORT).show()
                            }
                            errorOcurred = true
                        }
                    } catch (e: Exception) {
                        Log.e("ModificarActividad", "Error al procesar imagen de carrusel $index", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error al procesar la imagen $index: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        errorOcurred = true
                    }
                }

                if (!errorOcurred || newFotosBase64.isNotEmpty()) {
                    fotosCarruselBase64.value = newFotosBase64
                    Log.d("ModificarActividad", "Se procesaron ${newFotosBase64.size} imágenes de carrusel correctamente")
                } else {
                    // Si hubo algún error, limpiar las URI
                    if (newFotosBase64.isEmpty()) {
                        fotosCarruselUri.value = emptyList()
                    }
                }
            }
        }
    }

    // Efecto para depurar si los valores se están actualizando correctamente
    LaunchedEffect(nombre.value, descripcion.value) {
        Log.d("ModificarActividad", "Valores actuales: nombre=${nombre.value}, desc=${descripcion.value}")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Barra superior con botón de retroceso, título y botón de eliminar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Grupo del botón de retroceso y título
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = "Modificar Actividad",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.azulPrimario)
                    )
                }

                // Botón de eliminar actividad
                IconButton(
                    onClick = {
                        showDeleteConfirmation.value = true
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar actividad",
                        tint = Color.Red
                    )
                }
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
                // Formulario para editar los datos de la actividad
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
                        // Nombre de la actividad
                        Text(
                            text = "Nombre de la actividad",
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
                            value = descripcion.value,
                            onValueChange = { descripcion.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Describe la actividad") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        // Lugar
                        Text(
                            text = "Lugar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = lugar.value,
                            onValueChange = { lugar.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Ubicación de la actividad") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        // Fecha y hora de inicio
                        Text(
                            text = "Fecha y hora de inicio",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Botón para seleccionar fecha de inicio
                            OutlinedButton(
                                onClick = { showFechaInicioDatePicker.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                border = BorderStroke(1.dp, colorResource(R.color.azulPrimario))
                            ) {
                                Text(
                                    text = if (fechaInicio.value != null)
                                        dateFormat.format(fechaInicio.value!!)
                                    else "Seleccionar fecha",
                                    color = colorResource(R.color.azulPrimario)
                                )
                            }

                            // Botón para seleccionar hora de inicio
                            OutlinedButton(
                                onClick = { showFechaInicioTimePicker.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                border = BorderStroke(1.dp, colorResource(R.color.azulPrimario))
                            ) {
                                Text(
                                    text = if (fechaInicio.value != null)
                                        timeFormat.format(fechaInicio.value!!)
                                    else "Seleccionar hora",
                                    color = colorResource(R.color.azulPrimario)
                                )
                            }
                        }

                        // Fecha y hora de finalización
                        Text(
                            text = "Fecha y hora de finalización",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Botón para seleccionar fecha de finalización
                            OutlinedButton(
                                onClick = { showFechaFinDatePicker.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                border = BorderStroke(1.dp, colorResource(R.color.azulPrimario))
                            ) {
                                Text(
                                    text = if (fechaFinalizacion.value != null)
                                        dateFormat.format(fechaFinalizacion.value!!)
                                    else "Seleccionar fecha",
                                    color = colorResource(R.color.azulPrimario)
                                )
                            }

                            // Botón para seleccionar hora de finalización
                            OutlinedButton(
                                onClick = { showFechaFinTimePicker.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                border = BorderStroke(1.dp, colorResource(R.color.azulPrimario))
                            ) {
                                Text(
                                    text = if (fechaFinalizacion.value != null)
                                        timeFormat.format(fechaFinalizacion.value!!)
                                    else "Seleccionar hora",
                                    color = colorResource(R.color.azulPrimario)
                                )
                            }
                        }

                        // Fotos de carrusel
                        Text(
                            text = "Fotos de carrusel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Button(
                            onClick = {
                                fotosCarruselLauncher.launch("image/*")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario)
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text("Seleccionar imágenes")
                        }

                        // Vista previa de imágenes de carrusel
                        if (fotosCarruselUri.value.isNotEmpty()) {
                            Text(
                                text = "Nuevas imágenes seleccionadas: ${fotosCarruselUri.value.size}",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(fotosCarruselUri.value) { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Imagen de carrusel",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        } else if (actividadOriginal.value?.fotosCarruselIds?.isNotEmpty() == true) {
                            Text(
                                text = "Imágenes actuales: ${actividadOriginal.value?.fotosCarruselIds?.size ?: 0}",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(actividadOriginal.value?.fotosCarruselIds ?: emptyList()) { imagenId ->
                                    val imagenUrl = "${baseUrl}/files/download/$imagenId"
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(imagenUrl)
                                                .crossfade(true)
                                                .setHeader("Authorization", authToken)
                                                .build(),
                                            contentDescription = "Imagen de carrusel",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (nombre.value.isEmpty()) {
                                    errorMessage.value = "El nombre de la actividad no puede estar vacío"
                                    return@Button
                                }

                                if (descripcion.value.isEmpty()) {
                                    errorMessage.value = "La descripción no puede estar vacía"
                                    return@Button
                                }

                                if (lugar.value.isEmpty()) {
                                    errorMessage.value = "El lugar no puede estar vacío"
                                    return@Button
                                }

                                if (fechaInicio.value == null) {
                                    errorMessage.value = "Debes seleccionar una fecha de inicio"
                                    return@Button
                                }

                                if (fechaFinalizacion.value == null) {
                                    errorMessage.value = "Debes seleccionar una fecha de finalización"
                                    return@Button
                                }

                                if (fechaInicio.value!! > fechaFinalizacion.value!!) {
                                    errorMessage.value = "La fecha de inicio debe ser anterior a la fecha de finalización"
                                    return@Button
                                }

                                // Log de los datos que se van a enviar
                                Log.d("ModificarActividad", "Enviando datos: nombre=${nombre.value}, desc=${descripcion.value}")
                                Log.d("ModificarActividad", "¿Hay fotos de carrusel base64? ${fotosCarruselBase64.value.isNotEmpty()}")
                                Log.d("ModificarActividad", "Número de fotos de carrusel: ${fotosCarruselBase64.value.size}")

                                // Preparar objeto de actualización
                                val actividadUpdate = ActividadUpdateDTO(
                                    _id=actividadId,
                                    nombre = nombre.value,
                                    descripcion = descripcion.value,
                                    lugar = lugar.value,
                                    fechaInicio = fechaInicio.value!!,
                                    fechaFinalizacion = fechaFinalizacion.value!!,
                                    fotosCarruselBase64 = if (fotosCarruselBase64.value.isNotEmpty()) fotosCarruselBase64.value else null,
                                    fotosCarruselIds = actividadOriginal.value?.fotosCarruselIds
                                )

                                // Verificar datos de imágenes
                                if (fotosCarruselBase64.value.isNotEmpty()) {
                                    Log.d("ModificarActividad", "Carrusel: ${fotosCarruselBase64.value.size} imágenes")
                                    fotosCarruselBase64.value.forEachIndexed { index, base64 ->
                                        Log.d("ModificarActividad", "Carrusel $index Base64 longitud: ${base64.length}")
                                    }
                                }

                                // Enviar actualización
                                isSaving.value = true
                                scope.launch {
                                    try {
                                        Log.d("ModificarActividad", "Iniciando petición de actualización")
                                        val response = withContext(Dispatchers.IO) {
                                            retrofitService.modificarActividad(
                                                "Bearer $authToken",
                                                actividadUpdateDTO = actividadUpdate
                                            )
                                        }

                                        Log.d("ModificarActividad", "Respuesta recibida: ${response.code()}")
                                        if (response.isSuccessful) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Actividad actualizada correctamente", Toast.LENGTH_SHORT).show()
                                                navController.popBackStack()
                                            }
                                        } else {
                                            val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                                            Log.e("ModificarActividad", "Error al actualizar: ${response.code()} - $errorBody")
                                            errorMessage.value = "Error al actualizar: ${response.code()} - ${response.message()}\n$errorBody"
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ModificarActividad", "Excepción al actualizar", e)
                                        Log.e("ModificarActividad", "Tipo de excepción: ${e.javaClass.name}")
                                        Log.e("ModificarActividad", "Mensaje de excepción: ${e.message}")
                                        Log.e("ModificarActividad", "Stack trace: ${e.stackTraceToString()}")
                                        errorMessage.value = ErrorUtils.parseErrorMessage(e.message ?: "Error desconocido")
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

        // Date & Time Pickers
        if (showFechaInicioDatePicker.value) {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val calendar = Calendar.getInstance()
                    calendar.time = fechaInicio.value ?: Date()
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    fechaInicio.value = calendar.time
                    showFechaInicioDatePicker.value = false
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        if (showFechaInicioTimePicker.value) {
            val calendar = Calendar.getInstance()
            calendar.time = fechaInicio.value ?: Date()
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val newCalendar = Calendar.getInstance()
                    newCalendar.time = fechaInicio.value ?: Date()
                    newCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    newCalendar.set(Calendar.MINUTE, minute)
                    fechaInicio.value = newCalendar.time
                    showFechaInicioTimePicker.value = false
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        if (showFechaFinDatePicker.value) {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val calendar = Calendar.getInstance()
                    calendar.time = fechaFinalizacion.value ?: Date()
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    fechaFinalizacion.value = calendar.time
                    showFechaFinDatePicker.value = false
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        if (showFechaFinTimePicker.value) {
            val calendar = Calendar.getInstance()
            calendar.time = fechaFinalizacion.value ?: Date()
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val newCalendar = Calendar.getInstance()
                    newCalendar.time = fechaFinalizacion.value ?: Date()
                    newCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    newCalendar.set(Calendar.MINUTE, minute)
                    fechaFinalizacion.value = newCalendar.time
                    showFechaFinTimePicker.value = false
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
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

// Dialog para confirmar eliminación
    if (showDeleteConfirmation.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation.value = false },
            title = { Text("Eliminar actividad") },
            text = { Text("¿Estás seguro de que deseas eliminar esta actividad? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting.value = true
                        showDeleteConfirmation.value = false

                        // Llamada a la API para eliminar la actividad
                        scope.launch {
                            try {
                                val response = withContext(Dispatchers.IO) {
                                    retrofitService.eliminarActividad(
                                        "Bearer $authToken",
                                        actividadId
                                    )
                                }

                                if (response.isSuccessful) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Actividad eliminada correctamente", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    }
                                } else {
                                    val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                                    Log.e("ModificarActividad", "Error al eliminar: ${response.code()} - $errorBody")
                                    errorMessage.value = "Error al eliminar: ${response.code()} - ${response.message()}"
                                }
                            } catch (e: Exception) {
                                Log.e("ModificarActividad", "Excepción al eliminar", e)
                                errorMessage.value = ErrorUtils.parseErrorMessage(e.message ?: "Error desconocido")
                            } finally {
                                isDeleting.value = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("ELIMINAR")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation.value = false }
                ) {
                    Text("CANCELAR")
                }
            }
        )
    }

    // Overlay para la eliminación en progreso
    if (isDeleting.value) {
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
                        text = "Eliminando...",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// Función para comprimir y convertir imágenes a Base64
private suspend fun compressAndConvertToBase64(uri: Uri, context: Context): String? {
    return withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)

            // Leer la imagen como un bitmap
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Comprimir la imagen
            val compressedBitmap = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * 0.7).toInt(),
                (bitmap.height * 0.7).toInt(),
                true
            )

            // Convertir a bytes
            val outputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            // Convertir a Base64
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

            // Liberar recursos
            bitmap.recycle()
            compressedBitmap.recycle()
            outputStream.close()

            // Devolver el string en Base64
            base64String
        } catch (e: Exception) {
            Log.e("ModificarActividad", "Error al procesar imagen", e)
            null
        }
    }
}