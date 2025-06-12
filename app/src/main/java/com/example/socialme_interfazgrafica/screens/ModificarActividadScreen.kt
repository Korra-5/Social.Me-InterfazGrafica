package com.example.socialme_interfazgrafica

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.view.MotionEvent
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ActividadUpdateDTO
import com.example.socialme_interfazgrafica.model.Coordenadas
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.screens.actualizarMarcador
import com.example.socialme_interfazgrafica.screens.obtenerUbicacionActual
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.example.socialme_interfazgrafica.utils.PalabrasMalsonantesValidator
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
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
    val baseUrl = BuildConfig.URL_API

    val actividadOriginal = remember { mutableStateOf<ActividadDTO?>(null) }
    val nombre = remember { mutableStateOf("") }
    val descripcion = remember { mutableStateOf("") }
    val fechaInicio = remember { mutableStateOf<Date?>(null) }
    val fechaFinalizacion = remember { mutableStateOf<Date?>(null) }
    val lugar = remember { mutableStateOf("") }

    val showFechaInicioDatePicker = remember { mutableStateOf(false) }
    val showFechaInicioTimePicker = remember { mutableStateOf(false) }
    val showFechaFinDatePicker = remember { mutableStateOf(false) }
    val showFechaFinTimePicker = remember { mutableStateOf(false) }

    val isLoading = remember { mutableStateOf(true) }
    val isSaving = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    val showDeleteConfirmation = remember { mutableStateOf(false) }
    val isDeleting = remember { mutableStateOf(false) }

    val fotosCarruselBase64 = remember { mutableStateOf<List<String>>(emptyList()) }
    val fotosCarruselUri = remember { mutableStateOf<List<Uri>>(emptyList()) }
    val fotosCarruselExistentesIds = remember { mutableStateOf<List<String>>(emptyList()) }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var ubicacionSeleccionada by remember { mutableStateOf<GeoPoint?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var isMapExpanded by remember { mutableStateOf(false) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun updateDate(date: Long, currentDateTime: MutableState<Date?>) {
        val calendar = Calendar.getInstance()

        currentDateTime.value?.let {
            calendar.time = it
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            calendar.timeInMillis = date

            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
        } ?: run {
            calendar.timeInMillis = date
            calendar.set(Calendar.HOUR_OF_DAY, 12)
            calendar.set(Calendar.MINUTE, 0)
        }

        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        currentDateTime.value = calendar.time
    }

    fun updateTime(hour: Int, minute: Int, currentDateTime: MutableState<Date?>) {
        val calendar = Calendar.getInstance()

        if (currentDateTime.value != null) {
            calendar.time = currentDateTime.value!!
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        currentDateTime.value = calendar.time
    }

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

    fun validarCampos(): Pair<Boolean, String?> {
        if (nombre.value.trim().isEmpty()) {
            return Pair(false, "El nombre de la actividad no puede estar vacío")
        }

        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(nombre.value.trim())) {
            return Pair(false, "El nombre contiene palabras no permitidas")
        }

        if (descripcion.value.trim().isEmpty()) {
            return Pair(false, "La descripción no puede estar vacía")
        }

        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(descripcion.value.trim())) {
            return Pair(false, "La descripción contiene palabras no permitidas")
        }

        if (lugar.value.trim().isEmpty()) {
            return Pair(false, "El lugar no puede estar vacío")
        }

        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(lugar.value.trim())) {
            return Pair(false, "El lugar contiene palabras no permitidas")
        }

        if (fechaInicio.value == null) {
            return Pair(false, "Debes seleccionar una fecha de inicio")
        }

        if (fechaFinalizacion.value == null) {
            return Pair(false, "Debes seleccionar una fecha de finalización")
        }

        if (fechaInicio.value!! >= fechaFinalizacion.value!!) {
            return Pair(false, "La fecha de inicio debe ser anterior a la fecha de finalización")
        }

        return Pair(true, null)
    }

    LaunchedEffect(actividadId) {
        isLoading.value = true
        try {
            Log.d("ModificarActividad", "Cargando actividad: $actividadId")

            val response = withContext(Dispatchers.IO) {
                retrofitService.verActividadPorId("Bearer $authToken", actividadId)
            }

            if (response.isSuccessful && response.body() != null) {
                val actividad = response.body()!!

                actividadOriginal.value = actividad
                nombre.value = actividad.nombre
                descripcion.value = actividad.descripcion
                fechaInicio.value = actividad.fechaInicio
                fechaFinalizacion.value = actividad.fechaFinalizacion
                lugar.value = actividad.lugar
                fotosCarruselExistentesIds.value = actividad.fotosCarruselIds ?: emptyList()

                actividad.coordenadas?.let { coords ->
                    val lat = coords.latitud.toDoubleOrNull()
                    val lng = coords.longitud.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        ubicacionSeleccionada = GeoPoint(lat, lng)
                    }
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                errorMessage.value = "Error al cargar los datos: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage.value = ErrorUtils.parseErrorMessage(e.message ?: "Error desconocido")
        } finally {
            isLoading.value = false
        }
    }

    val fotosCarruselLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val newUris = mutableListOf<Uri>()
                val newBase64List = mutableListOf<String>()
                var hasErrors = false

                uris.forEach { uri ->
                    try {
                        val base64 = compressAndConvertToBase64(uri, context)
                        if (base64 != null) {
                            newUris.add(uri)
                            newBase64List.add(base64)
                        } else {
                            hasErrors = true
                        }
                    } catch (e: Exception) {
                        Log.e("ModificarActividad", "Error procesando imagen", e)
                        hasErrors = true
                    }
                }

                if (newBase64List.isNotEmpty()) {
                    fotosCarruselUri.value = fotosCarruselUri.value + newUris
                    fotosCarruselBase64.value = fotosCarruselBase64.value + newBase64List

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Se añadieron ${newBase64List.size} imágenes${if (hasErrors) " (algunas fallaron)" else ""}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                if (hasErrors && newBase64List.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error procesando todas las imágenes", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun eliminarFotoCarruselExistente(index: Int) {
        val nuevaLista = fotosCarruselExistentesIds.value.toMutableList()
        if (index < nuevaLista.size) {
            nuevaLista.removeAt(index)
            fotosCarruselExistentesIds.value = nuevaLista
        }
    }

    fun eliminarFotoCarruselNueva(index: Int) {
        val nuevasUris = fotosCarruselUri.value.toMutableList()
        val nuevosBase64 = fotosCarruselBase64.value.toMutableList()

        if (index < nuevasUris.size && index < nuevosBase64.size) {
            nuevasUris.removeAt(index)
            nuevosBase64.removeAt(index)
            fotosCarruselUri.value = nuevasUris
            fotosCarruselBase64.value = nuevosBase64
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(colorResource(R.color.background), CircleShape)
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
                        color = colorResource(R.color.azulPrimario
                    ))
                }

                IconButton(
                    onClick = {
                        showDeleteConfirmation.value = true
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(colorResource(R.color.card_colors), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar actividad",
                        tint = Color(0xFFEF4444)
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Nombre de la actividad",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.azulPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = nombre.value,
                            onValueChange = { nombre.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Nombre de la actividad",
                                    color = Color.Gray.copy(alpha = 0.7f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                focusedTextColor = colorResource(R.color.textoPrimario),
                                unfocusedTextColor = colorResource(R.color.textoPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "Descripción",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color =colorResource(R.color.azulPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = descripcion.value,
                            onValueChange = { descripcion.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = {
                                Text(
                                    "Describe la actividad",
                                    color = Color.Gray.copy(alpha = 0.7f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                focusedTextColor = colorResource(R.color.textoPrimario),
                                unfocusedTextColor = colorResource(R.color.textoPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3
                        )

                        Text(
                            text = "Ubicación",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.azulPrimario),
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
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AndroidView(
                                    factory = { context ->
                                        MapView(context).apply {
                                            setMultiTouchControls(true)
                                            setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                                            controller.setZoom(15.0)

                                            // Configurar ubicación inicial
                                            val startPoint = ubicacionSeleccionada ?: GeoPoint(40.416775, -3.703790)
                                            controller.setCenter(startPoint)

                                            // Añadir overlay para detectar toques
                                            overlays.add(object : Overlay() {
                                                override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                                                    mapView?.let { mv ->
                                                        val projection = mv.projection
                                                        val geoPoint = projection.fromPixels(
                                                            e?.x?.toInt() ?: 0,
                                                            e?.y?.toInt() ?: 0
                                                        ) as GeoPoint
                                                        ubicacionSeleccionada = geoPoint
                                                        actualizarMarcador(mv, geoPoint)
                                                    }
                                                    return true
                                                }
                                            })

                                            // Establecer referencia
                                            mapView = this
                                        }
                                    },
                                    update = { map ->
                                        // Actualizar el mapa cuando cambie la ubicación seleccionada
                                        ubicacionSeleccionada?.let { location ->
                                            actualizarMarcador(map, location)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Botón de ubicación actual
                                FloatingActionButton(
                                    onClick = {
                                        if (hasLocationPermission) {
                                            isLoadingLocation = true
                                            obtenerUbicacionActual(context) { location ->
                                                val geoPoint = GeoPoint(location.latitude, location.longitude)
                                                ubicacionSeleccionada = geoPoint
                                                mapView?.let { actualizarMarcador(it, geoPoint) }
                                                isLoadingLocation = false
                                            }
                                        } else {
                                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp)
                                        .size(48.dp),
                                    containerColor = colorResource(R.color.azulPrimario),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Mi ubicación",
                                        tint = Color.White
                                    )
                                }

                                // Indicador de carga
                                if (isLoadingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .align(Alignment.Center),
                                        color = colorResource(R.color.azulPrimario)
                                    )
                                }

                                // Botón de expandir/contraer
                                IconButton(
                                    onClick = { isMapExpanded = !isMapExpanded },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                                ) {
                                    Icon(
                                        if (isMapExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isMapExpanded) "Contraer" else "Expandir",
                                        tint = colorResource(R.color.azulPrimario)
                                    )
                                }
                            }
                        }

                        ubicacionSeleccionada?.let { ubicacion ->
                            Text(
                                text = "Coordenadas seleccionadas:\nLatitud: ${
                                    String.format(
                                        "%.6f",
                                        ubicacion.latitude
                                    )
                                }\nLongitud: ${String.format("%.6f", ubicacion.longitude)}",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF4B5563)
                            )
                        } ?: run {
                            Text(
                                text = "Toca en el mapa para seleccionar una ubicación o usa el botón de ubicación actual",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Text(
                            text = "Lugar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.azulPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = lugar.value,
                            onValueChange = { lugar.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Ubicación de la actividad",
                                    color = Color.Gray.copy(alpha = 0.7f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                focusedTextColor = colorResource(R.color.textoPrimario),
                                unfocusedTextColor = colorResource(R.color.textoPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "Fecha y hora de inicio",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color =  colorResource(R.color.azulPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { showFechaInicioDatePicker.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                border = BorderStroke(1.dp,  colorResource(R.color.azulPrimario)),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor =  colorResource(R.color.azulPrimario)
                                )
                            ) {
                                Text(
                                    text = if (fechaInicio.value != null)
                                        dateFormat.format(fechaInicio.value!!)
                                    else "Seleccionar fecha",
                                    color =  colorResource(R.color.azulPrimario)
                                )
                            }

                            OutlinedButton(
                                onClick = { showFechaInicioTimePicker.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                border = BorderStroke(1.dp,  colorResource(R.color.azulPrimario)),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor =  colorResource(R.color.azulPrimario)
                                )
                            ) {
                                Text(
                                    text = if (fechaInicio.value != null)
                                        timeFormat.format(fechaInicio.value!!)
                                    else "Seleccionar hora",
                                    color =  colorResource(R.color.azulPrimario)
                                )
                            }
                        }

                        Text(
                            text = "Fecha y hora de finalización",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color =  colorResource(R.color.azulPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { showFechaFinDatePicker.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                border = BorderStroke(1.dp, colorResource(R.color.azulPrimario)),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorResource(R.color.azulPrimario)
                                )
                            ) {
                                Text(
                                    text = if (fechaFinalizacion.value != null)
                                        dateFormat.format(fechaFinalizacion.value!!)
                                    else "Seleccionar fecha",
                                    color =  colorResource(R.color.azulPrimario)
                                )
                            }

                            OutlinedButton(
                                onClick = { showFechaFinTimePicker.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                border = BorderStroke(1.dp,  colorResource(R.color.azulPrimario)),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor =  colorResource(R.color.azulPrimario)
                                )
                            ) {
                                Text(
                                    text = if (fechaFinalizacion.value != null)
                                        timeFormat.format(fechaFinalizacion.value!!)
                                    else "Seleccionar hora",
                                    color =  colorResource(R.color.azulPrimario)
                                )
                            }
                        }

                        Text(
                            text = "Fotos de carrusel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color =  colorResource(R.color.azulPrimario),
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )

                        Button(
                            onClick = {
                                fotosCarruselLauncher.launch("image/*")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor =  colorResource(R.color.azulPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Añadir imágenes",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Seleccionar imágenes",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (fotosCarruselExistentesIds.value.isNotEmpty()) {
                            Text(
                                text = "Imágenes actuales: ${fotosCarruselExistentesIds.value.size}",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(fotosCarruselExistentesIds.value) { index, imagenId ->
                                    val imagenUrl = "${baseUrl}/files/download/$imagenId"
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp,  colorResource(R.color.card_colors), RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(imagenUrl)
                                                .crossfade(true)
                                                .setHeader("Authorization", "Bearer $authToken")
                                                .build(),
                                            contentDescription = "Imagen de carrusel",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        IconButton(
                                            onClick = {
                                                eliminarFotoCarruselExistente(index)
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(28.dp)
                                                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (fotosCarruselUri.value.isNotEmpty()) {
                            Text(
                                text = "Nuevas imágenes: ${fotosCarruselUri.value.size}",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(fotosCarruselUri.value) { index, uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp,  colorResource(R.color.card_colors), RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Nueva imagen de carrusel",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        IconButton(
                                            onClick = {
                                                eliminarFotoCarruselNueva(index)
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(28.dp)
                                                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val (isValid, errorMsg) = validarCampos()
                                if (!isValid) {
                                    errorMessage.value = errorMsg
                                    return@Button
                                }

                                val coordenadas = ubicacionSeleccionada?.let {
                                    Coordenadas(
                                        latitud = it.latitude.toString(),
                                        longitud = it.longitude.toString()
                                    )
                                }

                                val actividadUpdate = ActividadUpdateDTO(
                                    _id = actividadId,
                                    nombre = nombre.value.trim(),
                                    descripcion = descripcion.value.trim(),
                                    lugar = lugar.value.trim(),
                                    fechaInicio = fechaInicio.value!!,
                                    fechaFinalizacion = fechaFinalizacion.value!!,
                                    fotosCarruselBase64 = fotosCarruselBase64.value.takeIf { it.isNotEmpty() },
                                    fotosCarruselIds = fotosCarruselExistentesIds.value,
                                    coordenadas = coordenadas
                                )

                                Log.d("ModificarActividad", "Enviando datos:")
                                Log.d("ModificarActividad", "Fotos nuevas: ${fotosCarruselBase64.value.size}")
                                Log.d("ModificarActividad", "Fotos existentes: ${fotosCarruselExistentesIds.value.size}")

                                isSaving.value = true
                                scope.launch {
                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            retrofitService.modificarActividad(
                                                "Bearer $authToken",
                                                actividadUpdateDTO = actividadUpdate
                                            )
                                        }

                                        if (response.isSuccessful) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Actividad actualizada correctamente",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.popBackStack()
                                            }
                                        } else {
                                            val errorBody =
                                                response.errorBody()?.string() ?: "Sin cuerpo de error"
                                            Log.e(
                                                "ModificarActividad",
                                                "Error: ${response.code()} - $errorBody"
                                            )
                                            errorMessage.value =
                                                "Error al actualizar: ${response.code()}\n$errorBody"
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ModificarActividad", "Excepción", e)
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
                                .height(54.dp),
                            enabled = !isSaving.value,
                            colors = ButtonDefaults.buttonColors(
                                containerColor =  colorResource(R.color.azulPrimario),
                                disabledContainerColor =  colorResource(R.color.card_colors)
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

                        OutlinedButton(
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor =  colorResource(R.color.azulPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp,  colorResource(R.color.azulPrimario))
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

            if (errorMessage.value != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =  colorResource(R.color.background)
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
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage.value ?: "",
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (isSaving.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.size(120.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color =  colorResource(R.color.azulPrimario),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Guardando...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }

    if (showFechaInicioDatePicker.value) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                if (fechaInicio.value != null) {
                    calendar.time = fechaInicio.value!!
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, 12)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }
                calendar.set(year, month, dayOfMonth)
                fechaInicio.value = calendar.time
                showFechaInicioDatePicker.value = false
            },
            fechaInicio.value?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.get(Calendar.YEAR)
            } ?: Calendar.getInstance().get(Calendar.YEAR),
            fechaInicio.value?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.get(Calendar.MONTH)
            } ?: Calendar.getInstance().get(Calendar.MONTH),
            fechaInicio.value?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.get(Calendar.DAY_OF_MONTH)
            } ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showFechaInicioTimePicker.value) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val calendar = Calendar.getInstance()
                if (fechaInicio.value != null) {
                    calendar.time = fechaInicio.value!!
                }
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                fechaInicio.value = calendar.time
                showFechaInicioTimePicker.value = false
            },
            fechaInicio.value?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.get(Calendar.HOUR_OF_DAY)
            } ?: 12,
            fechaInicio.value?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.get(Calendar.MINUTE)
            } ?: 0,
            true
        ).show()
    }

    if (showFechaFinDatePicker.value) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                if (fechaFinalizacion.value != null) {
                    calendar.time = fechaFinalizacion.value!!
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, 13)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }
                calendar.set(year, month, dayOfMonth)
                fechaFinalizacion.value = calendar.time
                showFechaFinDatePicker.value = false
            },
            fechaFinalizacion.value?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.get(Calendar.YEAR)
            } ?: Calendar.getInstance().get(Calendar.YEAR),
            fechaFinalizacion.value?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.get(Calendar.MONTH)
            } ?: Calendar.getInstance().get(Calendar.MONTH),
            fechaFinalizacion.value?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.get(Calendar.DAY_OF_MONTH)
            } ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showFechaFinTimePicker.value) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val calendar = Calendar.getInstance()
                if (fechaFinalizacion.value != null) {
                    calendar.time = fechaFinalizacion.value!!
                }
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                fechaFinalizacion.value = calendar.time
                showFechaFinTimePicker.value = false
            },
            fechaFinalizacion.value?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.get(Calendar.HOUR_OF_DAY)
            } ?: 13,
            fechaFinalizacion.value?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                cal.get(Calendar.MINUTE)
            } ?: 0,
            true
        ).show()
    }

    if (showDeleteConfirmation.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation.value = false },
            title = {
                Text(
                    "Eliminar actividad",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color =  colorResource(R.color.textoPrimario)
                )
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar esta actividad? Esta acción no se puede deshacer.",
                    fontSize = 15.sp,
                    color = colorResource(R.color.textoPrimario)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting.value = true
                        showDeleteConfirmation.value = false

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
                                        Toast.makeText(
                                            context,
                                            "Actividad eliminada correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate(AppScreen.MenuScreen.route)                                    }
                                } else {
                                    val errorBody =
                                        response.errorBody()?.string() ?: "Sin cuerpo de error"
                                    Log.e(
                                        "ModificarActividad",
                                        "Error: ${response.code()} - $errorBody"
                                    )
                                    errorMessage.value =
                                        "Error al eliminar: ${response.code()} - ${response.message()}"
                                }
                            } catch (e: Exception) {
                                Log.e("ModificarActividad", "Excepción", e)
                                errorMessage.value =
                                    ErrorUtils.parseErrorMessage(e.message ?: "Error desconocido")
                            } finally {
                                isDeleting.value = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ELIMINAR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation.value = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(R.color.textoSecundario)
                    ),
                    border = BorderStroke(1.dp, colorResource(R.color.card_colors)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CANCELAR", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (isDeleting.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.size(120.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFEF4444),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Eliminando...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.textoPrimario)
                    )
                }
            }
        }
    }
}

private suspend fun compressAndConvertToBase64(uri: Uri, context: Context): String? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Log.e("CompressImage", "No se pudo decodificar la imagen")
                return@withContext null
            }

            val maxSize = 800
            val width = originalBitmap.width
            val height = originalBitmap.height

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

            val resizedBitmap = if (width != newWidth || height != newHeight) {
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            val byteArray = outputStream.toByteArray()

            val sizeInKb = byteArray.size / 1024
            Log.d("CompressImage", "Imagen comprimida: $sizeInKb KB")

            return@withContext Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("CompressImage", "Error comprimiendo imagen", e)
            e.printStackTrace()
            return@withContext null
        }
    }
}