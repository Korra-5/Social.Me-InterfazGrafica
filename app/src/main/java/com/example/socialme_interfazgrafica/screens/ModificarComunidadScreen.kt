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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import com.example.socialme_interfazgrafica.utils.PalabrasMalsonantesValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.io.ByteArrayOutputStream

@Composable
fun VistaPreviewUrl(texto: String, modifier: Modifier = Modifier) {
    if (texto.isNotBlank()) {
        val urlFormateada = formatearTextoAUrl(texto)
        if (urlFormateada.isNotEmpty()) {
            Text(
                text = "URL resultante: $urlFormateada",
                fontSize = 12.sp,
                color = colorResource(R.color.textoSecundario),
                modifier = modifier.padding(top = 4.dp, start = 4.dp),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

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

    // Función de validación
    fun validarCampos(): Pair<Boolean, String?> {
        if (nombre.value.trim().isEmpty()) {
            return Pair(false, "El nombre de la comunidad no puede estar vacío")
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

        val urlFormateada = formatearTextoAUrl(url.value)
        if (urlFormateada.isEmpty()) {
            return Pair(false, "La URL no puede estar vacía después del formateo")
        }

        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(urlFormateada)) {
            return Pair(false, "La URL contiene palabras no permitidas")
        }

        if (intereses.value.isEmpty()) {
            return Pair(false, "Debes añadir al menos un interés")
        }

        if (PalabrasMalsonantesValidator.validarLista(intereses.value)) {
            return Pair(false, "Algunos intereses contienen palabras no permitidas")
        }

        if (PalabrasMalsonantesValidator.validarLista(administradores.value)) {
            return Pair(false, "Algunos administradores contienen palabras no permitidas")
        }

        return Pair(true, null)
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

                // Configurar ubicación si existe
                comunidad.coordenadas?.let { coords ->
                    val lat = coords.latitud.toDoubleOrNull()
                    val lng = coords.longitud.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        ubicacionSeleccionada = GeoPoint(lat, lng)
                    }
                }

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
                        Log.d("ModificarComunidad", "Base64 generado para perfil (longitud): ${base64.length}")
                    } else {
                        Toast.makeText(context, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                        fotoPerfilUri.value = null
                    }
                } catch (e: Exception) {
                    Log.e("ModificarComunidad", "Error al procesar imagen de perfil", e)
                    Toast.makeText(context, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                    fotoPerfilUri.value = null
                }
            }
        }
    }

    // Launcher para seleccionar imágenes de carrusel
    val fotosCarruselLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            // Agregar las nuevas URIs a las existentes
            fotosCarruselUri.value = fotosCarruselUri.value + uris
            scope.launch {
                val newFotosBase64 = mutableListOf<String>()
                var errorOcurred = false

                uris.forEachIndexed { index, uri ->
                    try {
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
                    // Agregar las nuevas imágenes base64 a las existentes
                    fotosCarruselBase64.value = fotosCarruselBase64.value + newFotosBase64
                    Log.d("ModificarComunidad", "Se procesaron ${newFotosBase64.size} imágenes de carrusel correctamente")
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
                        .background(Color(0xFFFFD5D5), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar comunidad",
                        tint = colorResource(R.color.error)
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
                            // Vista previa de la imagen de perfil
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(colorResource(R.color.cyanSecundario))
                                    .border(1.dp, colorResource(R.color.azulPrimario), CircleShape)
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
                                            .setHeader("Authorization", "Bearer $authToken")
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

                        // URL de la comunidad
                        Text(
                            text = "URL de la comunidad",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = url.value,
                            onValueChange = {
                                val textoFiltrado = filtrarCaracteresUrl(it)
                                url.value = textoFiltrado
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("URL de la comunidad", color = colorResource(R.color.textoSecundario)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                focusedTextColor = colorResource(R.color.textoPrimario),
                                unfocusedTextColor = colorResource(R.color.textoPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Vista previa de la URL
                        VistaPreviewUrl(url.value)

                        // Nombre de la comunidad
                        Text(
                            text = "Nombre de la comunidad",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = nombre.value,
                            onValueChange = { nombre.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nombre de la comunidad", color = colorResource(R.color.textoSecundario)) },
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
                            placeholder = { Text("Describe tu comunidad", color = colorResource(R.color.textoSecundario)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                                focusedTextColor = colorResource(R.color.textoPrimario),
                                unfocusedTextColor = colorResource(R.color.textoPrimario)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3
                        )

                        // Sección del mapa
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
                                DisposableEffect(ubicacionSeleccionada) {
                                    val map = MapView(context).apply {
                                        setMultiTouchControls(true)
                                        setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                                        controller.setZoom(15.0)

                                        // Usar la ubicación seleccionada o Madrid como punto inicial
                                        val startPoint = ubicacionSeleccionada ?: GeoPoint(40.416775, -3.703790)
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

                                    // Si ya hay una ubicación seleccionada, mostrar el marcador
                                    ubicacionSeleccionada?.let { location ->
                                        actualizarMarcador(map, location)
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

                        // Switch para comunidad privada
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
                                    text = "Comunidad privada",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorResource(R.color.textoPrimario)
                                )

                                Switch(
                                    checked = isPrivada.value,
                                    onCheckedChange = { isPrivada.value = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colorResource(R.color.azulPrimario),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = colorResource(R.color.cyanSecundario)
                                    )
                                )
                            }
                        }

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
                                    val interesTrimmed = interesInput.value.trim()
                                    if (interesTrimmed.isNotEmpty()) {
                                        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(interesTrimmed)) {
                                            Toast.makeText(context, "El interés contiene palabras no permitidas", Toast.LENGTH_SHORT).show()
                                        } else if (!intereses.value.contains(interesTrimmed)) {
                                            intereses.value = intereses.value + interesTrimmed
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

                        // Administradores
                        Text(
                            text = "Administradores",
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
                                value = adminInput.value,
                                onValueChange = { adminInput.value = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                placeholder = { Text("Añadir administrador (username)", color = colorResource(R.color.textoSecundario)) },
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
                                    val adminTrimmed = adminInput.value.trim()
                                    if (adminTrimmed.isNotEmpty()) {
                                        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(adminTrimmed)) {
                                            Toast.makeText(context, "El nombre de administrador contiene palabras no permitidas", Toast.LENGTH_SHORT).show()
                                        } else if (!administradores.value.contains(adminTrimmed)) {
                                            administradores.value = administradores.value + adminTrimmed
                                            adminInput.value = ""
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

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            administradores.value.forEach { admin ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
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

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Text(
                                                text = "@$admin",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colorResource(R.color.textoPrimario)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                administradores.value = administradores.value.filter { it != admin }
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(Color(0xFFFFEDED), CircleShape)
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
                        }

                        // Fotos de carrusel
                        Text(
                            text = "Fotos de carrusel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.textoPrimario),
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )

                        Button(
                            onClick = {
                                fotosCarruselLauncher.launch("image/*")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario)
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

                        // Vista previa de imágenes de carrusel actuales
                        if (comunidadOriginal.value?.fotoCarruselIds?.isNotEmpty() == true) {
                            Text(
                                text = "Imágenes actuales: ${comunidadOriginal.value?.fotoCarruselIds?.size ?: 0}",
                                fontSize = 14.sp,
                                color = colorResource(R.color.textoSecundario),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(comunidadOriginal.value?.fotoCarruselIds ?: emptyList()) { imagenId ->
                                    val imagenUrl = "${baseUrl}/files/download/$imagenId"
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, colorResource(R.color.cyanSecundario), RoundedCornerShape(12.dp))
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
                                    }
                                }
                            }
                        }

                        // Vista previa de nuevas imágenes seleccionadas
                        if (fotosCarruselUri.value.isNotEmpty()) {
                            Text(
                                text = "Nuevas imágenes seleccionadas: ${fotosCarruselUri.value.size}",
                                fontSize = 14.sp,
                                color = colorResource(R.color.textoSecundario),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(fotosCarruselUri.value) { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, colorResource(R.color.cyanSecundario), RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Nueva imagen de carrusel",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        IconButton(
                                            onClick = {
                                                val index = fotosCarruselUri.value.indexOf(uri)
                                                val newUris = fotosCarruselUri.value.toMutableList()
                                                newUris.removeAt(index)
                                                fotosCarruselUri.value = newUris

                                                if (index < fotosCarruselBase64.value.size) {
                                                    val newBase64List = fotosCarruselBase64.value.toMutableList()
                                                    newBase64List.removeAt(index)
                                                    fotosCarruselBase64.value = newBase64List
                                                }
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
                                                tint = colorResource(R.color.error),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Botones de acción
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

                                // Preparar objeto de actualización
                                val comunidadUpdate = ComunidadUpdateDTO(
                                    currentURL = comunidadUrl,
                                    newUrl = formatearTextoAUrl(url.value),
                                    nombre = nombre.value.trim(),
                                    descripcion = descripcion.value.trim(),
                                    intereses = intereses.value,
                                    fotoPerfilBase64 = fotoPerfilBase64.value,
                                    fotoPerfilId = comunidadOriginal.value?.fotoPerfilId,
                                    fotoCarruselBase64 = if (fotosCarruselBase64.value.isNotEmpty()) fotosCarruselBase64.value else null,
                                    fotoCarruselIds = comunidadOriginal.value?.fotoCarruselIds,
                                    administradores = administradores.value,
                                    privada = isPrivada.value,
                                    coordenadas = coordenadas
                                )

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

    if (showDeleteConfirmation.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation.value = false },
            title = {
                Text(
                    "¿Estás seguro?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.textoPrimario)
                )
            },
            text = {
                Text(
                    "Esta acción eliminará la comunidad '${comunidadOriginal.value?.nombre ?: ""}' y todos sus datos asociados. Esta acción no se puede deshacer.",
                    fontSize = 15.sp,
                    color = colorResource(R.color.textoSecundario)
                )
            },
            confirmButton = {
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
                        containerColor = colorResource(R.color.error)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation.value = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(R.color.textoSecundario)
                    ),
                    border = BorderStroke(1.dp, colorResource(R.color.cyanSecundario)),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
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
                        color = colorResource(R.color.error),
                        strokeWidth = 3.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Eliminando...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.textoSecundario)
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
            Log.d("CompressImage", "Tamaño de imagen comprimida: $sizeInKb KB")

            return@withContext Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("CompressImage", "Error comprimiendo imagen", e)
            e.printStackTrace()
            return@withContext null
        }
    }
}