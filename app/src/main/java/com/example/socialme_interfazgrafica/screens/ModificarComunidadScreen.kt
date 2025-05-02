package com.example.socialme_interfazgrafica.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.ComunidadUpdateDTO
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.Coordenadas
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.io.ByteArrayOutputStream
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModificarComunidadScreen(comunidadUrl: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("TOKEN", "") ?: ""
    val baseUrl = "https://social-me-tfg.onrender.com"

    // Estados para los datos de la comunidad
    val comunidadOriginal = remember { mutableStateOf<ComunidadDTO?>(null) }
    val nombre = remember { mutableStateOf("") }
    val descripcion = remember { mutableStateOf("") }
    val url = remember { mutableStateOf(comunidadUrl) }
    val intereses = remember { mutableStateOf<List<String>>(emptyList()) }
    val interesInput = remember { mutableStateOf("") }
    val isPrivada = remember { mutableStateOf(false) }
    val administradores = remember { mutableStateOf<List<String>>(emptyList()) }
    val adminInput = remember { mutableStateOf("") }

    // Estados para la carga y errores
    val isLoading = remember { mutableStateOf(true) }
    val isSaving = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Estado para confirmar eliminación
    val showDeleteConfirmation = remember { mutableStateOf(false) }
    val isDeleting = remember { mutableStateOf(false) }

    // Estado para el mapa y coordenadas
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var ubicacionSeleccionada by remember { mutableStateOf<GeoPoint?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var isMapExpanded by remember { mutableStateOf(false) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    // Estados para manejo de imágenes
    val fotoPerfilBase64 = remember { mutableStateOf<String?>(null) }
    val fotoPerfilUri = remember { mutableStateOf<Uri?>(null) }
    val fotosCarruselBase64 = remember { mutableStateOf<List<String>>(emptyList()) }
    val fotosCarruselUri = remember { mutableStateOf<List<Uri>>(emptyList()) }


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

    // Carga inicial de datos de la comunidad
    LaunchedEffect(comunidadUrl) {
        isLoading.value = true
        try {
            Log.d("ModificarComunidad", "Intentando cargar datos para comunidad: $comunidadUrl")
            Log.d("ModificarComunidad", "Token de autenticación: ${authToken.take(10)}...")

            val response = withContext(Dispatchers.IO) {
              retrofitService.verComunidadPorUrl("Bearer $authToken", comunidadUrl)
            }

            Log.d("ModificarComunidad", "Respuesta del servidor: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val comunidad = response.body()!!
                Log.d("ModificarComunidad", "Datos recibidos: nombre=${comunidad.nombre}, descripción=${comunidad.descripcion}")

                comunidadOriginal.value = comunidad
                nombre.value = comunidad.nombre
                descripcion.value = comunidad.descripcion
                url.value = comunidad.url
                intereses.value = comunidad.intereses
                isPrivada.value = comunidad.privada
                administradores.value = comunidad.administradores ?: emptyList()

                Log.d("ModificarComunidad", "Valores asignados: nombre=${nombre.value}, desc=${descripcion.value}")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                Log.e("ModificarComunidad", "Error al cargar datos: ${response.code()} - $errorBody")
                errorMessage.value = "Error al cargar los datos de la comunidad: ${response.code()} - ${response.message()}"
            }
        } catch (e: Exception) {
            Log.e("API", "Exception class: ${e.javaClass.name}")
            Log.e("API", "Message: ${e.message}")
            Log.e("API", "Stack trace: ${e.stackTraceToString()}")
            errorMessage.value = ErrorUtils.parseErrorMessage(e.message ?: "Error desconocido")
        } finally {
            isLoading.value = false
        }
    }// Launcher para seleccionar imagen de perfil
    val fotoPerfilLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fotoPerfilUri.value = it
            // Convertir a Base64 con compresión y verificación
            scope.launch {
                try {
                    // Usar la nueva función de compresión
                    val base64 = compressAndConvertToBase64(it, context)
                    if (base64 != null) {
                        fotoPerfilBase64.value = base64
                        Log.d("ModificarComunidad", "Base64 generado para perfil (longitud): ${base64.length}")
                    } else {
                        Toast.makeText(context, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                        fotoPerfilUri.value = null
                    }
                } catch (e: Exception) {
                    Log.e("ModificarComunidad", "Error al procesar imagen de perfil", e)
                    Toast.makeText(context, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                    fotoPerfilUri.value = null // Limpiar URI si hay error
                }
            }
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
                        // Usar la nueva función de compresión
                        val base64 = compressAndConvertToBase64(uri, context)
                        if (base64 != null) {
                            newFotosBase64.add(base64)
                            Log.d("ModificarComunidad", "Base64 generado para carrusel $index (longitud): ${base64.length}")
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error al procesar la imagen $index", Toast.LENGTH_SHORT).show()
                            }
                            errorOcurred = true
                        }
                    } catch (e: Exception) {
                        Log.e("ModificarComunidad", "Error al procesar imagen de carrusel $index", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error al procesar la imagen $index: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        errorOcurred = true
                    }
                }

                if (!errorOcurred || newFotosBase64.isNotEmpty()) {
                    fotosCarruselBase64.value = newFotosBase64
                    Log.d("ModificarComunidad", "Se procesaron ${newFotosBase64.size} imágenes de carrusel correctamente")
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
        Log.d("ModificarComunidad", "Valores actuales: nombre=${nombre.value}, desc=${descripcion.value}")
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
                        text = "Modificar Comunidad",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.azulPrimario)
                    )
                }

                // Botón de eliminar comunidad
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
                        contentDescription = "Eliminar comunidad",
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
                // Formulario para editar los datos de la comunidad
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
                        // URL de la comunidad
                        Text(
                            text = "URL de la comunidad",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = url.value,
                            onValueChange = { url.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            placeholder = { Text("URL de la comunidad") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        // Nombre de la comunidad
                        Text(
                            text = "Nombre de la comunidad",
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
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = descripcion.value,
                            onValueChange = { descripcion.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Describe tu comunidad") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color.Gray
                            )
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


                        // Switch para comunidad privada
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Comunidad privada",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Switch(
                                checked = isPrivada.value,
                                onCheckedChange = { isPrivada.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = colorResource(R.color.azulPrimario),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.Gray
                                )
                            )
                        }

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
                                    if (interesInput.value.isNotEmpty() && !intereses.value.contains(interesInput.value)) {
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
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                                                    intereses.value = intereses.value.filter { it != interes }
                                                }
                                        )
                                    }
                                }
                            }
                        }

                        // Administradores
                        Text(
                            text = "Administradores",
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
                                value = adminInput.value,
                                onValueChange = { adminInput.value = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                placeholder = { Text("Añadir administrador (username)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorResource(R.color.azulPrimario),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            Button(
                                onClick = {
                                    if (adminInput.value.isNotEmpty() && !administradores.value.contains(adminInput.value)) {
                                        administradores.value = administradores.value + adminInput.value
                                        adminInput.value = ""
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

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            administradores.value.forEach { admin ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(colorResource(R.color.cyanSecundario)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = admin.firstOrNull()?.toString()?.uppercase() ?: "",
                                                fontWeight = FontWeight.Bold,
                                                color = colorResource(R.color.azulPrimario)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "@$admin",
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            administradores.value = administradores.value.filter { it != admin }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }

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
                                } else if (comunidadOriginal.value?.fotoPerfilId?.isNotEmpty() == true) {
                                    // Si no hay nueva imagen pero existe una foto de perfil, mostrarla
                                    val fotoPerfilUrl = "${baseUrl}/files/download/${comunidadOriginal.value?.fotoPerfilId}"
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
                        } else if (comunidadOriginal.value?.fotoCarruselIds?.isNotEmpty() == true) {
                            Text(
                                text = "Imágenes actuales: ${comunidadOriginal.value?.fotoCarruselIds?.size ?: 0}",
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
                                items(comunidadOriginal.value?.fotoCarruselIds ?: emptyList()) { imagenId ->
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
                                    errorMessage.value = "El nombre de la comunidad no puede estar vacío"
                                    return@Button
                                }

                                if (descripcion.value.isEmpty()) {
                                    errorMessage.value = "La descripción no puede estar vacía"
                                    return@Button
                                }

                                if (intereses.value.isEmpty()) {
                                    errorMessage.value = "Debes añadir al menos un interés"
                                    return@Button
                                }

                                val coordenadas = ubicacionSeleccionada?.let {
                                    Coordenadas(
                                        latitud = it.latitude.toString(),
                                        longitud = it.longitude.toString()
                                    )
                                }

                                // Log de los datos que se van a enviar
                                Log.d("ModificarComunidad", "Enviando datos: nombre=${nombre.value}, desc=${descripcion.value}")
                                Log.d("ModificarComunidad", "¿Hay foto de perfil base64? ${fotoPerfilBase64.value != null}")
                                if (fotoPerfilBase64.value != null) {
                                    Log.d("ModificarComunidad", "Tamaño del string base64 de perfil: ${fotoPerfilBase64.value!!.length}")
                                }
                                Log.d("ModificarComunidad", "¿Hay fotos de carrusel base64? ${fotosCarruselBase64.value.isNotEmpty()}")
                                Log.d("ModificarComunidad", "Número de fotos de carrusel: ${fotosCarruselBase64.value.size}")

                                // Preparar objeto de actualización
                                val comunidadUpdate = ComunidadUpdateDTO(
                                    currentURL = comunidadUrl,
                                    newUrl = url.value,
                                    nombre = nombre.value,
                                    descripcion = descripcion.value,
                                    intereses = intereses.value,
                                    fotoPerfilBase64 = fotoPerfilBase64.value,
                                    fotoPerfilId = comunidadOriginal.value?.fotoPerfilId,
                                    fotoCarruselBase64 = if (fotosCarruselBase64.value.isNotEmpty()) fotosCarruselBase64.value else null,
                                    fotoCarruselIds = comunidadOriginal.value?.fotoCarruselIds,
                                    administradores = administradores.value,
                                    privada = isPrivada.value,
                                    coordenadas=coordenadas
                                )

// Añadir logs para depuración
                                Log.d("ModificarComunidad", "Token: ${authToken.take(10)}...")
                                Log.d("ModificarComunidad", "URL actual: $comunidadUrl")
                                Log.d("ModificarComunidad", "URL nueva: ${url.value}")

// Verificar datos de imágenes
                                if (fotoPerfilBase64.value != null) {
                                    Log.d("ModificarComunidad", "Foto perfil Base64 longitud: ${fotoPerfilBase64.value!!.length}")
                                    Log.d("ModificarComunidad", "Foto perfil Base64 primeros 30 chars: ${fotoPerfilBase64.value!!.take(30)}")
                                }

                                if (fotosCarruselBase64.value.isNotEmpty()) {
                                    Log.d("ModificarComunidad", "Carrusel: ${fotosCarruselBase64.value.size} imágenes")
                                    fotosCarruselBase64.value.forEachIndexed { index, base64 ->
                                        Log.d("ModificarComunidad", "Carrusel $index Base64 longitud: ${base64.length}")
                                    }
                                }

                                // Enviar actualización
                                isSaving.value = true
                                scope.launch {
                                    try {
                                        Log.d("ModificarComunidad", "Iniciando petición de actualización")
                                        val response = withContext(Dispatchers.IO) {
                                            retrofitService.modificarComunidad(
                                                "Bearer $authToken",
                                                comunidadUpdateDTO = comunidadUpdate
                                            )
                                        }

                                        Log.d("ModificarComunidad", "Respuesta recibida: ${response.code()}")
                                        if (response.isSuccessful) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Comunidad actualizada correctamente", Toast.LENGTH_SHORT).show()
                                                navController.popBackStack()
                                            }
                                        } else {
                                            val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                                            Log.e("ModificarComunidad", "Error al actualizar: ${response.code()} - $errorBody")
                                            errorMessage.value = "Error al actualizar: ${response.code()} - ${response.message()}\n$errorBody"
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ModificarComunidad", "Excepción al actualizar", e)
                                        Log.e("ModificarComunidad", "Tipo de excepción: ${e.javaClass.name}")
                                        Log.e("ModificarComunidad", "Mensaje de excepción: ${e.message}")
                                        Log.e("ModificarComunidad", "Stack trace: ${e.stackTraceToString()}")
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

    if (showDeleteConfirmation.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { showDeleteConfirmation.value = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.8f)
                    .clickable { /* Evitar que se cierre al hacer clic en la card */ },
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Estás seguro?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Esta acción eliminará la comunidad '${comunidadOriginal.value?.nombre ?: ""}' y todos sus datos asociados. Esta acción no se puede deshacer.",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { showDeleteConfirmation.value = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray
                            ),
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Text("Cancelar", color = Color.White)
                        }

                        Button(
                            onClick = {
                                isDeleting.value = true
                                scope.launch {
                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            retrofitService.eliminarComunidad(
                                                "Bearer $authToken",
                                                comunidadUrl
                                            )
                                        }

                                        if (response.isSuccessful) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Comunidad eliminada correctamente",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.popBackStack()
                                            }
                                        } else {
                                            val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                                            Log.e("EliminarComunidad", "Error: ${response.code()} - $errorBody")
                                            errorMessage.value = "Error al eliminar: ${response.code()} - ${response.message()}"
                                        }
                                    } catch (e: Exception) {
                                        Log.e("EliminarComunidad", "Excepción", e)
                                        errorMessage.value = ErrorUtils.parseErrorMessage(e.message ?: "Error desconocido")
                                    } finally {
                                        isDeleting.value = false
                                        showDeleteConfirmation.value = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            ),
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                            enabled = !isDeleting.value
                        ) {
                            if (isDeleting.value) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Eliminar", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // Overlay de carga existente
    if (isSaving.value || isDeleting.value) {
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
                        text = if (isDeleting.value) "Eliminando..." else "Guardando...",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

suspend fun compressAndConvertToBase64(uri: Uri, context: Context): String? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Log.e("CompressImage", "No se pudo decodificar la imagen")
                return@withContext null
            }

            // Redimensionar si es muy grande
            val maxSize = 800 // 800px máximo para cualquier dimensión
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
                // Si ya es menor que el tamaño máximo, mantener dimensiones
                newWidth = width
                newHeight = height
            }

            // Solo redimensionar si es necesario
            val resizedBitmap = if (width != newWidth || height != newHeight) {
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }

            // Comprimir
            val outputStream = ByteArrayOutputStream()
            // Usar una calidad moderada para reducir tamaño
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            val byteArray = outputStream.toByteArray()

            // Verificar tamaño final
            val sizeInKb = byteArray.size / 1024
            Log.d("CompressImage", "Tamaño de imagen comprimida: $sizeInKb KB")

            // Convertir a Base64 sin saltos de línea
            return@withContext Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("CompressImage", "Error comprimiendo imagen", e)
            e.printStackTrace()
            return@withContext null
        }
    }
}
