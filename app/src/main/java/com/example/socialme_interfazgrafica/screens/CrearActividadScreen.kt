package com.example.socialme_interfazgrafica.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ActividadCreateDTO
import com.example.socialme_interfazgrafica.model.Coordenadas
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
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
    val username = sharedPreferences.getString("USERNAME", "") ?: ""

    // Estado para los campos del formulario
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var privada by remember { mutableStateOf(false) }

    // Estado para fechas con MutableState para poder actualizarlas desde los pickers
    val fechaInicio = remember { mutableStateOf<Date?>(null) }
    val fechaFinalizacion = remember { mutableStateOf<Date?>(null) }

    // Estados para mostrar los pickers de fecha y hora
    val showFechaInicioDatePicker = remember { mutableStateOf(false) }
    val showFechaInicioTimePicker = remember { mutableStateOf(false) }
    val showFechaFinDatePicker = remember { mutableStateOf(false) }
    val showFechaFinTimePicker = remember { mutableStateOf(false) }

    // Formatos para mostrar fecha y hora
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // Estado para las imágenes
    var imagenes by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imagenesBase64 by remember { mutableStateOf<List<String>>(emptyList()) }

    // Estado para controlar la carga
    var isLoading by remember { mutableStateOf(false) }
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    // Estado para el mapa y coordenadas
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var ubicacionSeleccionada by remember { mutableStateOf<GeoPoint?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var isMapExpanded by remember { mutableStateOf(false) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    // Estado para controlar los permisos
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Lanzador para solicitar permisos de ubicación
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            obtenerUbicacionActual(context) { location ->
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                ubicacionSeleccionada = geoPoint
                mapView?.let { actualizarMarcador(it, geoPoint) }
            }
        } else {
            Toast.makeText(
                context,
                "Se necesitan permisos de ubicación para mostrar tu ubicación actual",
                Toast.LENGTH_LONG
            ).show()
        }
    }

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

    // Función para actualizar fecha manteniendo la hora existente
    fun updateDate(date: Long, currentDateTime: MutableState<Date?>) {
        val calendar = Calendar.getInstance()

        // Si ya hay una fecha, conservar la hora
        currentDateTime.value?.let {
            calendar.time = it
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // Establecer nueva fecha
            calendar.timeInMillis = date

            // Restaurar hora previa
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
        } ?: run {
            // Si no hay fecha previa, usar mediodía como hora por defecto
            calendar.timeInMillis = date
            calendar.set(Calendar.HOUR_OF_DAY, 12)
            calendar.set(Calendar.MINUTE, 0)
        }

        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        currentDateTime.value = calendar.time
    }

    // Función para actualizar hora manteniendo la fecha existente
    fun updateTime(hour: Int, minute: Int, currentDateTime: MutableState<Date?>) {
        val calendar = Calendar.getInstance()

        // Si ya hay una fecha, mantenerla
        if (currentDateTime.value != null) {
            calendar.time = currentDateTime.value!!
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        currentDateTime.value = calendar.time
    }

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
                        modifier = Modifier.padding(bottom = 4.dp),
                        color= Color.Black

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


                    // Sección del mapa
                    Text(
                        text = "Ubicación",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isMapExpanded) 300.dp else 200.dp)
                            .clickable { isMapExpanded = !isMapExpanded },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Configuración y visualización del mapa
                            DisposableEffect(Unit) {
                                val map = MapView(context).apply {
                                    setMultiTouchControls(true)
                                    setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)

                                    // Configurar el controlador del mapa
                                    controller.setZoom(15.0)

                                    // Posición inicial (Madrid como ejemplo)
                                    val startPoint = GeoPoint(40.416775, -3.703790)
                                    controller.setCenter(startPoint)

                                    // Configurar listener para clics en el mapa
                                    overlays.add(object : Overlay() {
                                        override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                                            mapView?.let {
                                                val projection = it.projection
                                                val geoPoint = projection.fromPixels(
                                                    e?.x?.toInt() ?: 0,
                                                    e?.y?.toInt() ?: 0
                                                ) as GeoPoint
                                                ubicacionSeleccionada = geoPoint
                                                actualizarMarcador(it, geoPoint)
                                            }
                                            return true
                                        }
                                    })
                                }

                                mapView = map

                                // Intentar obtener la ubicación actual si tenemos permisos
                                if (hasLocationPermission) {
                                    obtenerUbicacionActual(context) { location ->
                                        val geoPoint = GeoPoint(location.latitude, location.longitude)
                                        ubicacionSeleccionada = geoPoint
                                        actualizarMarcador(map, geoPoint)
                                    }
                                }

                                onDispose {
                                    map.onDetach()
                                }
                            }

                            // Vista del mapa
                            mapView?.let { map ->
                                AndroidView(
                                    factory = { map },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Botón para obtener ubicación actual
                            FloatingActionButton(
                                onClick = {
                                    if (hasLocationPermission) {
                                        obtenerUbicacionActual(context) { location ->
                                            val geoPoint = GeoPoint(location.latitude, location.longitude)
                                            ubicacionSeleccionada = geoPoint
                                            mapView?.let { actualizarMarcador(it, geoPoint) }
                                        }
                                    } else {
                                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .size(48.dp),
                                containerColor = colorResource(R.color.azulPrimario)
                            ) {
                                Icon(
                                    Icons.Default.Call,
                                    contentDescription = "Mi ubicación",
                                    tint = Color.White
                                )
                            }

                            // Indicador de carga de ubicación
                            if (isLoadingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .align(Alignment.Center),
                                    color = colorResource(R.color.azulPrimario)
                                )
                            }

                            // Ícono para expandir/contraer el mapa
                            IconButton(
                                onClick = { isMapExpanded = !isMapExpanded },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
                            ) {
                                Icon(
                                    if (isMapExpanded) Icons.Default.Home else Icons.Default.Settings,
                                    contentDescription = if (isMapExpanded) "Contraer" else "Expandir",
                                    tint = colorResource(R.color.azulPrimario)
                                )
                            }
                        }
                    }

                    // Mostrar coordenadas seleccionadas
                    ubicacionSeleccionada?.let { ubicacion ->
                        Text(
                            text = "Coordenadas seleccionadas:\nLatitud: ${String.format("%.6f", ubicacion.latitude)}\nLongitud: ${String.format("%.6f", ubicacion.longitude)}",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } ?: run {
                        Text(
                            text = "Toca en el mapa para seleccionar una ubicación o usa el botón de ubicación actual",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

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
                            fontWeight = FontWeight.Bold,
                            color= Color.Black
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
                            if (validarCampos(nombre, descripcion, lugar, fechaInicio.value, fechaFinalizacion.value)) {
                                scope.launch {
                                    isLoading = true
                                    try {
                                        Log.d("CrearActividad", "Creando actividad con token: ${authToken.take(10)}...")

                                        val coordenadas = ubicacionSeleccionada?.let {
                                            Coordenadas(
                                                latitud = it.latitude.toString(),
                                                longitud = it.longitude.toString()
                                            )
                                        }

                                        val actividad = ActividadCreateDTO(
                                            nombre = nombre,
                                            descripcion = descripcion,
                                            comunidad = comunidadUrl,
                                            creador = username,
                                            fechaInicio = fechaInicio.value!!,
                                            fechaFinalizacion = fechaFinalizacion.value!!,
                                            fotosCarruselBase64 = if (imagenesBase64.isNotEmpty()) imagenesBase64 else null,
                                            fotosCarruselIds = null,
                                            privada = privada,
                                            coordenadas =coordenadas,
                                            lugar = lugar
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

    // Date Picker para fecha de inicio
    if (showFechaInicioDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showFechaInicioDatePicker.value = false },
            confirmButton = {
                Button(onClick = { showFechaInicioDatePicker.value = false }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFechaInicioDatePicker.value = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            val datePickerState = rememberDatePickerState()
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )

            // Cuando selecciona una fecha, actualizar el estado
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { date ->
                    updateDate(date, fechaInicio)
                }
            }
        }
    }

    // Time Picker para hora de inicio
    if (showFechaInicioTimePicker.value) {
        TimePickerDialog(
            onDismissRequest = { showFechaInicioTimePicker.value = false },
            onTimeSelected = { hour, minute ->
                updateTime(hour, minute, fechaInicio)
                showFechaInicioTimePicker.value = false
            }
        )
    }

    // Date Picker para fecha de finalización
    if (showFechaFinDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showFechaFinDatePicker.value = false },
            confirmButton = {
                Button(onClick = { showFechaFinDatePicker.value = false }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFechaFinDatePicker.value = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            val datePickerState = rememberDatePickerState()
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )

            // Cuando selecciona una fecha, actualizar el estado
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { date ->
                    updateDate(date, fechaFinalizacion)
                }
            }
        }
    }

    // Time Picker para hora de finalización
    if (showFechaFinTimePicker.value) {
        TimePickerDialog(
            onDismissRequest = { showFechaFinTimePicker.value = false },
            onTimeSelected = { hour, minute ->
                updateTime(hour, minute, fechaFinalizacion)
                showFechaFinTimePicker.value = false
            }
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }

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
                Text(
                    "Seleccionar hora",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Time picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour selector
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hora:", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        IconButton(onClick = {
                            if (selectedHour > 0) selectedHour-- else selectedHour = 23
                        }) {
                            Text("+", fontSize = 24.sp)
                        }

                        Text(
                            text = String.format("%02d", selectedHour),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        IconButton(onClick = {
                            if (selectedHour < 23) selectedHour++ else selectedHour = 0
                        }) {
                            Text("-", fontSize = 24.sp)
                        }
                    }

                    Text(":", fontWeight = FontWeight.Bold, fontSize = 24.sp)

                    // Minute selector
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Minuto:", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        IconButton(onClick = {
                            if (selectedMinute > 0) selectedMinute-- else selectedMinute = 59
                        }) {
                            Text("+", fontSize = 24.sp)
                        }

                        Text(
                            text = String.format("%02d", selectedMinute),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        IconButton(onClick = {
                            if (selectedMinute < 59) selectedMinute++ else selectedMinute = 0
                        }) {
                            Text("-", fontSize = 24.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onTimeSelected(selectedHour, selectedMinute) }
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