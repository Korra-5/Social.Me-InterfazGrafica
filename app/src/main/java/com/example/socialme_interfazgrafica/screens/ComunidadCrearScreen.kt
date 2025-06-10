package com.example.socialme_interfazgrafica.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.net.Uri
import android.os.Looper
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ComunidadCreateDTO
import com.example.socialme_interfazgrafica.model.Coordenadas
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.example.socialme_interfazgrafica.utils.PalabrasMalsonantesValidator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.io.ByteArrayOutputStream
import java.io.InputStream

private fun normalizarUrl(url: String): String {
    return url
        .replace("á", "a").replace("Á", "A")
        .replace("é", "e").replace("É", "E")
        .replace("í", "i").replace("Í", "I")
        .replace("ó", "o").replace("Ó", "O")
        .replace("ú", "u").replace("Ú", "U")
        .replace("ý", "y").replace("Ý", "Y")
        .replace("à", "a").replace("À", "A")
        .replace("è", "e").replace("È", "E")
        .replace("ì", "i").replace("Ì", "I")
        .replace("ò", "o").replace("Ò", "O")
        .replace("ù", "u").replace("Ù", "U")
        .replace("â", "a").replace("Â", "A")
        .replace("ê", "e").replace("Ê", "E")
        .replace("î", "i").replace("Î", "I")
        .replace("ô", "o").replace("Ô", "O")
        .replace("û", "u").replace("Û", "U")
        .replace("ä", "a").replace("Ä", "A")
        .replace("ë", "e").replace("Ë", "E")
        .replace("ï", "i").replace("Ï", "I")
        .replace("ö", "o").replace("Ö", "O")
        .replace("ü", "u").replace("Ü", "U")
        .replace("ÿ", "y").replace("Ÿ", "Y")
        .replace("ã", "a").replace("Ã", "A")
        .replace("ñ", "n").replace("Ñ", "N")
        .replace("õ", "o").replace("Õ", "O")
        .replace("ç", "c").replace("Ç", "C")
        .replace("ş", "s").replace("Ş", "S")
        .replace("ţ", "t").replace("Ţ", "T")
        .replace("æ", "ae").replace("Æ", "AE")
        .replace("œ", "oe").replace("Œ", "OE")
        .replace("ø", "o").replace("Ø", "O")
        .replace("å", "a").replace("Å", "A")
        .replace("ß", "ss")
        .replace("ð", "d").replace("Ð", "D")
        .replace("þ", "th").replace("Þ", "TH")
        .replace("č", "c").replace("Č", "C")
        .replace("š", "s").replace("Š", "S")
        .replace("ž", "z").replace("Ž", "Z")
        .replace("ć", "c").replace("Ć", "C")
        .replace("đ", "d").replace("Đ", "D")
        .replace(Regex("[^a-zA-Z0-9._/\\s-]"), "")
        .replace(Regex("[.]{2,}"), ".")
        .replace(Regex("[/]{2,}"), "/")
        .trim('.', '_')
}

fun filtrarCaracteresUrl(texto: String): String {
    return texto.filter { char ->
        char.isLetterOrDigit() || char.isWhitespace() || char == '-'
    }
}

fun formatearTextoAUrl(texto: String): String {
    return normalizarUrl(texto)
        .trim()
        .lowercase()
        .replace(Regex("[\\s-]+"), "-")
        .replace(Regex("^-+"), "")
        .replace(Regex("-+$"), "")
}

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

@Composable
fun VistaPreviewUrl(texto: String) {
    if (texto.isNotBlank()) {
        val urlFormateada = formatearTextoAUrl(texto)
        if (urlFormateada.isNotEmpty()) {
            Text(
                text = "URL resultante: $urlFormateada",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CrearComunidadScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("TOKEN", "") ?: ""
    val username = sharedPreferences.getString("USERNAME", "") ?: ""

    var url by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var privada by remember { mutableStateOf(false) }

    var nuevoInteres by remember { mutableStateOf("") }
    var intereses by remember { mutableStateOf<List<String>>(emptyList()) }

    val errorMessage = remember { mutableStateOf<String?>(null) }

    var imagenPerfil by remember { mutableStateOf<Uri?>(null) }
    var imagenPerfilBase64 by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

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

    fun validarCampos(
        url: String,
        nombre: String,
        descripcion: String,
        intereses: List<String>
    ): Pair<Boolean, String?> {
        if (url.trim().isBlank()) {
            return Pair(false, "La URL es requerida")
        }

        val urlFormateada = formatearTextoAUrl(url.trim())
        if (urlFormateada.isEmpty()) {
            return Pair(false, "La URL no puede estar vacía después del formateo")
        }

        if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(urlFormateada)) {
            return Pair(false, "La URL contiene palabras no permitidas")
        }

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

        intereses.forEach { interes ->
            val errorInteres = validarInteres(interes)
            if (errorInteres != null) {
                return Pair(false, errorInteres)
            }
        }

        if (PalabrasMalsonantesValidator.validarLista(intereses)) {
            return Pair(false, "Algunos intereses contienen palabras no permitidas")
        }

        return Pair(true, null)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
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
                                .background(colorResource(R.color.card_colors), CircleShape)
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
                            text = "Imagen de perfil",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.azulPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, colorResource(R.color.card_colors), RoundedCornerShape(12.dp))
                                .background(Color.White)
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
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(colorResource(R.color.card_colors), CircleShape)
                                            .padding(12.dp),
                                        tint = colorResource(R.color.azulPrimario),
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Añadir imagen de perfil",
                                        color =colorResource(R.color.azulPrimario),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Text(
                            text = "URL de la comunidad",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.azulPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)

                        )

                        OutlinedTextField(
                            value = url,
                            onValueChange = {
                                val textoFiltrado = filtrarCaracteresUrl(it)
                                url = textoFiltrado
                            },
                            modifier = Modifier.fillMaxWidth()
                                .testTag("textoUrl")
                            ,
                            placeholder = {
                                Text(
                                    "URL única para tu comunidad",
                                    color = Color.Gray.copy(alpha = 0.7f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        VistaPreviewUrl(url)

                        Text(
                            text = "Nombre de la comunidad",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.azulPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            modifier = Modifier.fillMaxWidth()
                                .testTag("textoNombre")
                            ,
                            placeholder = {
                                Text(
                                    "Nombre de la comunidad",
                                    color = Color.Gray.copy(alpha = 0.7f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "Descripción",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.azulPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("textoDescripcion")
                                .height(120.dp),
                            placeholder = {
                                Text(
                                    "Describe tu comunidad",
                                    color = Color.Gray.copy(alpha = 0.7f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.azulPrimario),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B)
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
                                DisposableEffect(Unit) {
                                    val map = MapView(context).apply {
                                        setMultiTouchControls(true)
                                        setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                                        controller.setZoom(15.0)
                                        val startPoint = GeoPoint(40.416775, -3.703790)
                                        controller.setCenter(startPoint)
                                        overlays.add(object : Overlay() {
                                            override fun onSingleTapConfirmed(
                                                e: MotionEvent?,
                                                mapView: MapView?
                                            ): Boolean {
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
                                        color = colorResource(R.color.azulPrimario),
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
                                        tint = colorResource(R.color.azulPrimario),
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
                            text = "Intereses",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.azulPrimario),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = nuevoInteres,
                                onValueChange = {
                                    if (!it.contains(" ") && !it.contains(",")) {
                                        nuevoInteres = it
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        "Añadir interés",
                                        color = Color.Gray.copy(alpha = 0.7f)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorResource(R.color.azulPrimario),
                                    unfocusedBorderColor = Color(0xFFCBD5E1),
                                    focusedTextColor = Color(0xFF1E293B),
                                    unfocusedTextColor = Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val interesTrimmed = nuevoInteres.trim()
                                    if (interesTrimmed.isNotBlank()) {
                                        val errorValidacion = validarInteres(interesTrimmed)
                                        if (errorValidacion != null) {
                                            errorMessage.value=errorValidacion
                                        } else if (PalabrasMalsonantesValidator.contienepalabrasmalsonantes(interesTrimmed)) {
                                            Toast.makeText(context, "El interés contiene palabras no permitidas", Toast.LENGTH_SHORT).show()
                                        } else if (!intereses.contains(interesTrimmed)) {
                                            intereses = intereses + interesTrimmed
                                            nuevoInteres = ""
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(R.color.azulPrimario),
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Añadir", fontWeight = FontWeight.Medium)
                            }
                        }

                        if (intereses.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                intereses.forEach { interes ->
                                    Surface(
                                        modifier = Modifier,
                                        shape = RoundedCornerShape(50.dp),
                                        color = colorResource(R.color.card_colors),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 14.dp,
                                                vertical = 8.dp
                                            ),
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
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable {
                                                        intereses = intereses - interes
                                                    },
                                                tint = colorResource(R.color.azulPrimario),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Opciones de comunidad",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color =colorResource(R.color.azulPrimario),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor =colorResource(R.color.card_colors),
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
                                    color = Color.Black
                                )

                                Switch(
                                    checked = privada,
                                    onCheckedChange = { privada = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colorResource(R.color.azulPrimario),
                                        uncheckedThumbColor = colorResource(R.color.cyanSecundario),
                                        uncheckedTrackColor = colorResource(R.color.card_colors),
                                    )
                                )
                            }

                            Text(
                                text = "(La privacidad de la comunidad no se podrá modificar una vez creada)",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val (isValid, errorMsg) = validarCampos(url, nombre, descripcion, intereses)
                                if (isValid) {
                                    scope.launch {
                                        isLoading = true
                                        try {
                                            Log.d("CrearComunidad", "Creando comunidad con token: ${authToken.take(10)}...")

                                            val coordenadas = ubicacionSeleccionada?.let {
                                                Coordenadas(
                                                    latitud = it.latitude.toString(),
                                                    longitud = it.longitude.toString()
                                                )
                                            }

                                            val comunidad = ComunidadCreateDTO(
                                                url = formatearTextoAUrl(url.trim()),
                                                nombre = nombre.trim(),
                                                descripcion = descripcion.trim(),
                                                intereses = intereses,
                                                fotoPerfilBase64 = imagenPerfilBase64,
                                                fotoPerfilId = null,
                                                creador = username,
                                                privada = privada,
                                                coordenadas = coordenadas,
                                                codigoUnion = null
                                            )

                                            val response = retrofitService.crearComunidad(
                                                token = "Bearer $authToken",
                                                comunidadCreateDTO = comunidad
                                            )

                                            if (response.isSuccessful) {
                                                Toast.makeText(
                                                    context,
                                                    "Comunidad creada correctamente",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.popBackStack()
                                            } else {
                                                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                                                errorMessage.value= ErrorUtils.parseErrorMessage(errorMsg)
                                                Log.e("CrearComunidad", "Error: $errorMsg")
                                            }
                                        } catch (e: Exception) {
                                            val mensajeError = ErrorUtils.parseErrorMessage(e.message ?: "Error desconocido")
                                            errorMessage.value=mensajeError
                                            Log.e("CrearComunidad", "Excepción: ${e.message}", e)
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                } else {
                                    errorMessage.value = errorMsg
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("buttonCrearComunidad")
                                .height(54.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario),
                                disabledContainerColor =colorResource(R.color.card_colors),
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
                                    "CREAR COMUNIDAD",
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
                                contentColor = colorResource(R.color.azulPrimario),
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

                // Componente de error movido aquí, fuera de la Card
                errorMessage.value?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Error",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(24.dp)
                            )

                            Text(
                                text = error,
                                color = Color(0xFFD32F2F),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { errorMessage.value = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cerrar error",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Función simplificada que usa Madrid como ubicación por defecto
fun obtenerUbicacionActual(context: Context, onLocationReceived: (Location) -> Unit) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Intentar obtener la última ubicación conocida
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onLocationReceived(location)
                } else {
                    // Si no hay ubicación, usar Madrid por defecto
                    val madridLocation = Location("default").apply {
                        latitude = 40.4168
                        longitude = -3.7038
                    }
                    onLocationReceived(madridLocation)
                    Toast.makeText(context, "Usando ubicación de Madrid por defecto", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                // En caso de error, usar Madrid por defecto
                val madridLocation = Location("default").apply {
                    latitude = 40.4168
                    longitude = -3.7038
                }
                onLocationReceived(madridLocation)
                Toast.makeText(context, "Error obteniendo ubicación, usando Madrid", Toast.LENGTH_SHORT).show()
                Log.e("MapaUbicacion", "Error obteniendo ubicación", exception)
            }
        } else {
            // Sin permisos, usar Madrid por defecto
            val madridLocation = Location("default").apply {
                latitude = 40.4168
                longitude = -3.7038
            }
            onLocationReceived(madridLocation)
            Toast.makeText(context, "Sin permisos de ubicación, usando Madrid", Toast.LENGTH_SHORT).show()
            Log.d("MapaUbicacion", "No hay permisos de ubicación")
        }
    } catch (e: Exception) {
        // En cualquier excepción, usar Madrid por defecto
        val madridLocation = Location("default").apply {
            latitude = 40.4168
            longitude = -3.7038
        }
        onLocationReceived(madridLocation)
        Toast.makeText(context, "Error obteniendo ubicación, usando Madrid", Toast.LENGTH_SHORT).show()
        Log.e("MapaUbicacion", "Excepción al obtener ubicación: ${e.message}", e)
    }
}
fun actualizarMarcador(map: MapView, geoPoint: GeoPoint) {
    // Remover marcadores existentes
    val markersToRemove = map.overlays.filterIsInstance<Marker>()
    map.overlays.removeAll(markersToRemove)

    // Crear nuevo marcador
    val newMarker = Marker(map).apply {
        position = geoPoint
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = "Ubicación seleccionada"
    }

    map.overlays.add(newMarker)

    map.controller.animateTo(geoPoint, 15.0, 500L)

    map.invalidate()
}