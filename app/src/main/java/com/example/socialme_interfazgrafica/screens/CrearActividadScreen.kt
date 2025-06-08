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
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.Coordenadas
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.example.socialme_interfazgrafica.utils.PalabrasMalsonantesValidator
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("TOKEN", "") ?: ""
    val username = sharedPreferences.getString("USERNAME", "") ?: ""
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var privada by remember { mutableStateOf(false) }

    val comunidad = remember { mutableStateOf<ComunidadDTO?>(null) }
    val isLoadingComunidad = remember { mutableStateOf(true) }

    val fechaInicio = remember { mutableStateOf<Date?>(null) }
    val fechaFinalizacion = remember { mutableStateOf<Date?>(null) }

    val showFechaInicioDatePicker = remember { mutableStateOf(false) }
    val showFechaInicioTimePicker = remember { mutableStateOf(false) }
    val showFechaFinDatePicker = remember { mutableStateOf(false) }
    val showFechaFinTimePicker = remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    var imagenes by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imagenesBase64 by remember { mutableStateOf<List<String>>(emptyList()) }

    var isLoading by remember { mutableStateOf(false) }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var ubicacionSeleccionada by remember { mutableStateOf<GeoPoint?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var isMapExpanded by remember { mutableStateOf(false) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(comunidadUrl) {
        isLoadingComunidad.value = true
        try {
            val response = withContext(Dispatchers.IO) {
                retrofitService.verComunidadPorUrl("Bearer $authToken", comunidadUrl)
            }

            if (response.isSuccessful && response.body() != null) {
                comunidad.value = response.body()
                if (response.body()!!.privada) {
                    privada = true
                }
            }
        } catch (e: Exception) {
            Log.e("CrearActividad", "Error al cargar comunidad: ${e.message}")
        } finally {
            isLoadingComunidad.value = false
        }
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

    fun validarCampos(
        nombre: String,
        descripcion: String,
        lugar: String,
        fechaInicio: Date?,
        fechaFinalizacion: Date?
    ): Pair<Boolean, String?> {
        if (nombre.trim().isBlank()) {
            return Pair(false, "El nombre es requerido")
        }

        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(nombre.trim())) {
            return Pair(false, "El nombre contiene palabras no permitidas")
        }

        if (descripcion.trim().isBlank()) {
            return Pair(false, "La descripción es requerida")
        }

        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(descripcion.trim())) {
            return Pair(false, "La descripción contiene palabras no permitidas")
        }

        if (lugar.trim().isBlank()) {
            return Pair(false, "El lugar es requerido")
        }

        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(lugar.trim())) {
            return Pair(false, "El lugar contiene palabras no permitidas")
        }

        if (fechaInicio == null) {
            return Pair(false, "La fecha de inicio es requerida")
        }

        if (fechaFinalizacion == null) {
            return Pair(false, "La fecha de finalización es requerida")
        }

        if (fechaInicio >= fechaFinalizacion) {
            return Pair(false, "La fecha de inicio debe ser anterior a la fecha de finalización")
        }

        return Pair(true, null)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
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
                                .background(colorResource(R.color.cyanSecundario), CircleShape)
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
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoadingComunidad.value) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorResource(R.color.azulPrimario))
                    }// En la sección donde se muestra la información de la comunidad, reemplaza esta parte:

                    comunidad.value?.let { com ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorResource(R.color.cyanSecundario).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Comunidad:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorResource(R.color.textoSecundario)
                                )
                                Text(
                                    text = com.nombre,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.azulPrimario)
                                )
                                if (com.privada) {
                                    Text(
                                        text = "🔒 Comunidad privada • Las actividades creadas aquí serán privadas",
                                        fontSize = 12.sp,
                                        color = colorResource(R.color.textoSecundario),
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Nombre de la actividad",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nombre de la actividad", color = colorResource(R.color.textoSecundario)) },
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
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Describe la actividad", color = colorResource(R.color.textoSecundario)) },
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
                            color = colorResource(R.color.textoPrimario),
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
                                DisposableEffect(Unit) {
                                    val map = MapView(context).apply {
                                        setMultiTouchControls(true)
                                        setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                                        controller.setZoom(15.0)
                                        val startPoint = GeoPoint(40.416775, -3.703790)
                                        controller.setCenter(startPoint)
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

                                mapView?.let { map ->
                                    AndroidView(
                                        factory = { map },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

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
                                    containerColor = colorResource(R.color.azulPrimario),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        Icons.Default.Call,
                                        contentDescription = "Mi ubicación",
                                        tint = Color.White
                                    )
                                }

                                if (isLoadingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .align(Alignment.Center),
                                        color = colorResource(R.color.azulPrimario)
                                    )
                                }

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

                        ubicacionSeleccionada?.let { ubicacion ->
                            Text(
                                text = "Coordenadas seleccionadas:\nLatitud: ${String.format("%.6f", ubicacion.latitude)}\nLongitud: ${String.format("%.6f", ubicacion.longitude)}",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = colorResource(R.color.textoSecundario)
                            )
                        } ?: run {
                            Text(
                                text = "Toca en el mapa para seleccionar una ubicación o usa el botón de ubicación actual",
                                fontSize = 14.sp,
                                color = colorResource(R.color.textoSecundario),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Text(
                            text = "Lugar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = lugar,
                            onValueChange = { lugar = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Lugar de la actividad", color = colorResource(R.color.textoSecundario)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = "Lugar",
                                    tint = colorResource(R.color.azulPrimario)
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
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { showFechaInicioDatePicker.value = true },
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
                                    text = if (fechaInicio.value != null)
                                        dateFormat.format(fechaInicio.value!!)
                                    else "Seleccionar fecha",
                                    color = colorResource(R.color.azulPrimario)
                                )
                            }

                            OutlinedButton(
                                onClick = { showFechaInicioTimePicker.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                border = BorderStroke(1.dp, colorResource(R.color.azulPrimario)),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorResource(R.color.azulPrimario)
                                )
                            ) {
                                Text(
                                    text = if (fechaInicio.value != null)
                                        timeFormat.format(fechaInicio.value!!)
                                    else "Seleccionar hora",
                                    color = colorResource(R.color.azulPrimario)
                                )
                            }
                        }

                        Text(
                            text = "Fecha y hora de finalización",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
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
                                    color = colorResource(R.color.azulPrimario)
                                )
                            }

                            OutlinedButton(
                                onClick = { showFechaFinTimePicker.value = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                border = BorderStroke(1.dp, colorResource(R.color.azulPrimario)),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorResource(R.color.azulPrimario)
                                )
                            ) {
                                Text(
                                    text = if (fechaFinalizacion.value != null)
                                        timeFormat.format(fechaFinalizacion.value!!)
                                    else "Seleccionar hora",
                                    color = colorResource(R.color.azulPrimario)
                                )
                            }
                        }

                        if (comunidad.value?.privada != true) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Actividad privada",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colorResource(R.color.textoPrimario)
                                    )

                                    Switch(
                                        checked = privada,
                                        onCheckedChange = { privada = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = colorResource(R.color.azulPrimario),
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = colorResource(R.color.cyanSecundario)
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Añadir imágenes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario)
                        )

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
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, colorResource(R.color.cyanSecundario), RoundedCornerShape(12.dp)),
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
                                                .padding(4.dp)
                                                .size(28.dp)
                                                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = colorResource(R.color.error),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Añadir imagen",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Añadir imágenes",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val (isValid, errorMsg) = validarCampos(nombre, descripcion, lugar, fechaInicio.value, fechaFinalizacion.value)
                                if (isValid) {
                                    scope.launch {
                                        isLoading = true
                                        try {
                                            val coordenadas = ubicacionSeleccionada?.let {
                                                Coordenadas(
                                                    latitud = it.latitude.toString(),
                                                    longitud = it.longitude.toString()
                                                )
                                            }

                                            val actividad = ActividadCreateDTO(
                                                nombre = nombre.trim(),
                                                descripcion = descripcion.trim(),
                                                comunidad = comunidadUrl,
                                                creador = username,
                                                fechaInicio = fechaInicio.value!!,
                                                fechaFinalizacion = fechaFinalizacion.value!!,
                                                fotosCarruselBase64 = if (imagenesBase64.isNotEmpty()) imagenesBase64 else null,
                                                fotosCarruselIds = null,
                                                privada = privada,
                                                coordenadas = coordenadas,
                                                lugar = lugar.trim()
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
                                            }
                                        } catch (e: Exception) {
                                            val mensajeError = ErrorUtils.parseErrorMessage(e.message ?: "Error desconocido")
                                            Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, errorMsg ?: "Error de validación", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario),
                                disabledContainerColor = colorResource(R.color.textoSecundario)
                            ),
                            shape = RoundedCornerShape(12.dp)
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

                        OutlinedButton(
                            onClick = { navController.popBackStack() },
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
        }
    }

    if (showFechaInicioDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showFechaInicioDatePicker.value = false },
            confirmButton = {
                Button(
                    onClick = { showFechaInicioDatePicker.value = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario)
                    )
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFechaInicioDatePicker.value = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(R.color.textoSecundario)
                    )
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            val today = Calendar.getInstance().timeInMillis
            val datePickerState = rememberDatePickerState(
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis >= today - 86400000L
                    }
                }
            )
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )

            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { date ->
                    updateDate(date, fechaInicio)
                }
            }
        }
    }

    if (showFechaInicioTimePicker.value) {
        TimePickerDialog(
            onDismissRequest = { showFechaInicioTimePicker.value = false },
            onTimeSelected = { hour, minute ->
                updateTime(hour, minute, fechaInicio)
                showFechaInicioTimePicker.value = false
            }
        )
    }

    if (showFechaFinDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showFechaFinDatePicker.value = false },
            confirmButton = {
                Button(
                    onClick = { showFechaFinDatePicker.value = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario)
                    )
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFechaFinDatePicker.value = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(R.color.textoSecundario)
                    )
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            val today = Calendar.getInstance().timeInMillis
            val datePickerState = rememberDatePickerState(
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis >= today - 86400000L
                    }
                }
            )
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )

            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { date ->
                    updateDate(date, fechaFinalizacion)
                }
            }
        }
    }

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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Seleccionar hora",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorResource(R.color.textoPrimario),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Hora:",
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoSecundario)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        IconButton(
                            onClick = {
                                if (selectedHour < 23) selectedHour++ else selectedHour = 0
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(colorResource(R.color.cyanSecundario), CircleShape)
                        ) {
                            Text("+", fontSize = 24.sp, color = colorResource(R.color.azulPrimario))
                        }

                        Text(
                            text = String.format("%02d", selectedHour),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = colorResource(R.color.textoPrimario)
                        )

                        IconButton(
                            onClick = {
                                if (selectedHour > 0) selectedHour-- else selectedHour = 23
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(colorResource(R.color.cyanSecundario), CircleShape)
                        ) {
                            Text("-", fontSize = 24.sp, color = colorResource(R.color.azulPrimario))
                        }
                    }

                    Text(":", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = colorResource(R.color.textoPrimario))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Minuto:",
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoSecundario)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        IconButton(
                            onClick = {
                                if (selectedMinute < 59) selectedMinute++ else selectedMinute = 0
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(colorResource(R.color.cyanSecundario), CircleShape)
                        ) {
                            Text("+", fontSize = 24.sp, color = colorResource(R.color.azulPrimario))
                        }

                        Text(
                            text = String.format("%02d", selectedMinute),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = colorResource(R.color.textoPrimario)
                        )

                        IconButton(
                            onClick = {
                                if (selectedMinute > 0) selectedMinute-- else selectedMinute = 59
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(colorResource(R.color.cyanSecundario), CircleShape)
                        ) {
                            Text("-", fontSize = 24.sp, color = colorResource(R.color.azulPrimario))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorResource(R.color.textoSecundario)
                        )
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onTimeSelected(selectedHour, selectedMinute) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.azulPrimario)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}

fun convertToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)

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

            val resizedBitmap = if (width != newWidth || height != newHeight) {
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)

            if (resizedBitmap != bitmap) {
                bitmap.recycle()
            }

            val byteArray = outputStream.toByteArray()
            val sizeInKb = byteArray.size / 1024

            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        }
    } catch (e: Exception) {
        null
    }
}