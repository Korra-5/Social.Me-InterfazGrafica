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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.socialme_interfazgrafica.BuildConfig
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.ComunidadUpdateDTO
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.Coordenadas
import com.example.socialme_interfazgrafica.navigation.AppScreen
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

private fun validarInteres(interes: String): String? {
    val interesLimpio = interes.trim()

    if (interesLimpio.length > 25) {
        return "Los intereses no pueden superar 25 caracteres"
    }

    if (interesLimpio.contains(" ")) {
        return "Los intereses no pueden contener espacios"
    }

    if (interesLimpio.contains(",")) {
        return "Los intereses no pueden contener comas"
    }

    return null
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModificarComunidadScreen(comunidadUrl: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("TOKEN", "") ?: ""
    val baseUrl = BuildConfig.URL_API

    val comunidadOriginal = remember { mutableStateOf<ComunidadDTO?>(null) }
    val nombre = remember { mutableStateOf("") }
    val descripcion = remember { mutableStateOf("") }
    val url = remember { mutableStateOf(comunidadUrl) }
    val intereses = remember { mutableStateOf<List<String>>(emptyList()) }
    val interesInput = remember { mutableStateOf("") }
    val administradores = remember { mutableStateOf<List<String>>(emptyList()) }
    val adminInput = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(true) }
    val isSaving = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    val showDeleteConfirmation = remember { mutableStateOf(false) }
    val isDeleting = remember { mutableStateOf(false) }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var ubicacionSeleccionada by remember { mutableStateOf<GeoPoint?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var isMapExpanded by remember { mutableStateOf(false) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val fotoPerfilBase64 = remember { mutableStateOf<String?>(null) }
    val fotoPerfilUri = remember { mutableStateOf<Uri?>(null) }

    val fotosCarruselBase64 = remember { mutableStateOf<List<String>>(emptyList()) }
    val fotosCarruselUri = remember { mutableStateOf<List<Uri>>(emptyList()) }
    val fotosCarruselExistentesIds = remember { mutableStateOf<List<String>>(emptyList()) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

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

        intereses.value.forEach { interes ->
            val errorInteres = validarInteres(interes)
            if (errorInteres != null) {
                return Pair(false, errorInteres)
            }
        }

        if (PalabrasMalsonantesValidator.validarLista(intereses.value)) {
            return Pair(false, "Algunos intereses contienen palabras no permitidas")
        }

        if (PalabrasMalsonantesValidator.validarLista(administradores.value)) {
            return Pair(false, "Algunos administradores contienen palabras no permitidas")
        }

        return Pair(true, null)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            isLoadingLocation = true
            obtenerUbicacionActual(context) { location ->
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                ubicacionSeleccionada = geoPoint
                mapView?.let { actualizarMarcador(it, geoPoint) }
                isLoadingLocation = false
            }
        } else {
            Toast.makeText(
                context,
                "Se necesitan permisos de ubicación para mostrar tu ubicación actual",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(comunidadUrl) {
        isLoading.value = true
        try {
            Log.d("ModificarComunidad", "Cargando datos para: $comunidadUrl")

            val response = withContext(Dispatchers.IO) {
                retrofitService.verComunidadPorUrl("Bearer $authToken", comunidadUrl)
            }

            if (response.isSuccessful && response.body() != null) {
                val comunidad = response.body()!!

                comunidadOriginal.value = comunidad
                nombre.value = comunidad.nombre
                descripcion.value = comunidad.descripcion
                url.value = comunidad.url
                intereses.value = comunidad.intereses
                administradores.value = comunidad.administradores ?: emptyList()
                fotosCarruselExistentesIds.value = comunidad.fotoCarruselIds ?: emptyList()

                comunidad.coordenadas?.let { coords ->
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
                    } else {
                        Toast.makeText(context, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                        fotoPerfilUri.value = null
                    }
                } catch (e: Exception) {
                    Log.e("ModificarComunidad", "Error procesando imagen", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    fotoPerfilUri.value = null
                }
            }
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
                        Log.e("ModificarComunidad", "Error procesando imagen", e)
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
            .background(colorResource(R.color.background))
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
                                } else if (comunidadOriginal.value?.fotoPerfilId?.isNotEmpty() == true) {
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

                        VistaPreviewUrl(url.value)

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

                                            // Establecer referencia para uso posterior
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
                                onValueChange = {
                                    if (!it.contains(" ") && !it.contains(",")) {
                                        interesInput.value = it
                                    }
                                },
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
                                        val errorValidacion = validarInteres(interesTrimmed)
                                        if (errorValidacion != null) {
                                            Toast.makeText(context, errorValidacion, Toast.LENGTH_SHORT).show()
                                        } else if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(interesTrimmed)) {
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
                                            administradores.value += adminTrimmed.toLowerCase()
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

                        if (fotosCarruselExistentesIds.value.isNotEmpty()) {
                            Text(
                                text = "Imágenes actuales: ${fotosCarruselExistentesIds.value.size}",
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
                                itemsIndexed(fotosCarruselExistentesIds.value) { index, imagenId ->
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
                                                tint = colorResource(R.color.error),
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
                                itemsIndexed(fotosCarruselUri.value) { index, uri ->
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
                                                tint = colorResource(R.color.error),
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

                                // Capturar la nueva URL antes de la actualización
                                val nuevaUrl = formatearTextoAUrl(url.value)

                                val comunidadUpdate = ComunidadUpdateDTO(
                                    currentURL = comunidadUrl,
                                    newUrl = nuevaUrl,
                                    nombre = nombre.value.trim(),
                                    descripcion = descripcion.value.trim(),
                                    intereses = intereses.value,
                                    fotoPerfilBase64 = fotoPerfilBase64.value,
                                    fotoPerfilId = comunidadOriginal.value?.fotoPerfilId,
                                    fotoCarruselBase64 = fotosCarruselBase64.value.takeIf { it.isNotEmpty() },
                                    fotoCarruselIds = fotosCarruselExistentesIds.value,
                                    administradores = administradores.value,
                                    privada = comunidadOriginal.value!!.privada,
                                    coordenadas = coordenadas
                                )

                                isSaving.value = true
                                scope.launch {
                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            retrofitService.modificarComunidad(
                                                "Bearer $authToken",
                                                comunidadUpdateDTO = comunidadUpdate
                                            )
                                        }

                                        if (response.isSuccessful) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Comunidad actualizada correctamente", Toast.LENGTH_SHORT).show()

                                                if (nuevaUrl != comunidadUrl) {
                                                    navController.popBackStack()
                                                    navController.navigate("comunidadDetalle/$nuevaUrl") {
                                                        popUpTo(navController.graph.startDestinationId)
                                                        launchSingleTop = true
                                                    }
                                                } else {
                                                    // Si la URL no cambió, simplemente volver
                                                    navController.popBackStack()
                                                }
                                            }
                                        } else {
                                            val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                                            Log.e("ModificarComunidad", "Error: ${response.code()} - $errorBody")
                                            errorMessage.value = "Error al actualizar: ${response.code()}\n$errorBody"
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ModificarComunidad", "Excepción", e)
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
                            text = ErrorUtils.parseErrorMessage(errorMessage.value ?:"") ,
                            color = colorResource(R.color.error),
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
                                        navController.navigate(AppScreen.MenuScreen.route) {
                                            popUpTo(AppScreen.MenuScreen.route) { inclusive = true }
                                        }
                                    }
                                } else {
                                    val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                                    Log.e("EliminarComunidad", "Error: ${response.code()} - $errorBody")
                                    errorMessage.value = ErrorUtils.parseErrorMessage(errorBody)
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