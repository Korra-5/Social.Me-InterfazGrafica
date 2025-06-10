package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.socialme_interfazgrafica.BuildConfig
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.Coordenadas
import com.example.socialme_interfazgrafica.model.DenunciaCreateDTO
import com.example.socialme_interfazgrafica.model.ParticipantesActividadDTO
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.example.socialme_interfazgrafica.utils.FunctionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun ActividadDetalleScreen(
    actividadId: String,
    authToken: String,
    navController: NavController
) {
    val actividad = remember { mutableStateOf<ActividadDTO?>(null) }
    val comunidad = remember { mutableStateOf<ComunidadDTO?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val participantes = remember { mutableStateOf(0) }
    val isParticipating = remember { mutableStateOf(false) }
    val username = remember { mutableStateOf("") }

    val showMenu = remember { mutableStateOf(false) }
    val showReportDialog = remember { mutableStateOf(false) }
    val reportReason = remember { mutableStateOf("") }
    val reportBody = remember { mutableStateOf("") }
    val isReportLoading = remember { mutableStateOf(false) }

    val utils = FunctionUtils
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Cargar username una sola vez
    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username.value = sharedPreferences.getString("USERNAME", "") ?: ""
    }

    // Cargar datos de la actividad con manejo mejorado de corrutinas
    LaunchedEffect(actividadId, username.value) {
        if (username.value.isEmpty()) return@LaunchedEffect

        isLoading.value = true
        error.value = null

        try {
            val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

            // Usar el scope del LaunchedEffect que se cancela automáticamente
            supervisorScope {
                try {
                    // Cargar actividad
                    val actividadResponseDeferred = async(Dispatchers.IO) {
                        withTimeout(10000) {
                            retrofitService.verActividadPorId(
                                token = authToken,
                                id = actividadId
                            )
                        }
                    }

                    val actividadResponse = actividadResponseDeferred.await()

                    if (actividadResponse.isSuccessful && actividadResponse.body() != null) {
                        actividad.value = actividadResponse.body()
                        participantes.value = (5..25).random()

                        val participantesDTO = ParticipantesActividadDTO(
                            username = username.value,
                            actividadId = actividadId,
                            nombreActividad = actividadResponse.body()?.nombre ?: ""
                        )

                        // Ejecutar llamadas en paralelo con timeouts más cortos
                        val participacionResponseDeferred = async(Dispatchers.IO) {
                            withTimeout(5000) {
                                retrofitService.booleanUsuarioApuntadoActividad(
                                    participantesActividadDTO = participantesDTO,
                                    token = authToken
                                )
                            }
                        }

                        val comunidadResponseDeferred = async(Dispatchers.IO) {
                            withTimeout(5000) {
                                retrofitService.verComunidadPorActividad(
                                    token = authToken,
                                    idActividad = actividadId
                                )
                            }
                        }

                        // Esperar resultados
                        val participacionResponse = participacionResponseDeferred.await()
                        val comunidadResponse = comunidadResponseDeferred.await()

                        // Actualizar estados solo si aún estamos en composición
                        if (isActive) {
                            isParticipating.value = participacionResponse.isSuccessful &&
                                    participacionResponse.body() == true

                            when {
                                comunidadResponse.isSuccessful && comunidadResponse.body() != null -> {
                                    comunidad.value = comunidadResponse.body()
                                }
                                comunidadResponse.code() == 403 -> {
                                    comunidad.value = ComunidadDTO(
                                        url = "comunidad_privada",
                                        nombre = "Comunidad privada",
                                        descripcion = "No tienes permisos para ver esta comunidad",
                                        privada = true,
                                        creador = "",
                                        fechaCreacion = Date(),
                                        intereses = emptyList(),
                                        fotoPerfilId = "",
                                        fotoCarruselIds = emptyList(),
                                        administradores = emptyList(),
                                        codigoUnion = null,
                                        coordenadas = Coordenadas("", "")
                                    )
                                }
                                else -> {
                                    Log.e("ActividadDetalle", "Error cargando comunidad: ${comunidadResponse.code()}")
                                }
                            }
                        }
                    } else {
                        if (isActive) {
                            error.value = "No se pudo cargar la actividad: ${actividadResponse.message()}"
                        }
                    }
                } catch (e: Exception) {
                    if (isActive) {
                        Log.e("ActividadDetalle", "Excepción: ${e.message}", e)
                        error.value = when (e) {
                            is TimeoutCancellationException -> "Tiempo de espera agotado. Comprueba tu conexión."
                            is CancellationException -> return@supervisorScope // Cancelación normal
                            else -> "Error de red: ${e.message}"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            if (isActive) {
                Log.e("ActividadDetalle", "Excepción general: ${e.message}", e)
                error.value = "Error general: ${e.message}"
            }
        } finally {
            if (isActive) {
                isLoading.value = false
            }
        }
    }

    if (showReportDialog.value) {
        utils.ReportDialog(
            onDismiss = { showReportDialog.value = false },
            onConfirm = { motivo, cuerpo ->
                scope.launch {
                    isReportLoading.value = true
                    try {
                        val denunciaDTO = DenunciaCreateDTO(
                            motivo = motivo,
                            cuerpo = cuerpo,
                            nombreItemDenunciado = actividadId,
                            tipoItemDenunciado = "actividad",
                            usuarioDenunciante = username.value
                        )

                        val response = withContext(Dispatchers.IO) {
                            withTimeout(8000) {
                                RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                                    .crearDenuncia(authToken, denunciaDTO)
                            }
                        }

                        if (response.isSuccessful) {
                            Toast.makeText(context, "Denuncia enviada correctamente", Toast.LENGTH_SHORT).show()
                            showReportDialog.value = false
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                            try {
                                val jsonObject = JSONObject(errorBody)
                                val errorMessage = jsonObject.optString("error", "")
                                if (errorMessage.isNotEmpty()) {
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, ErrorUtils.parseErrorMessage(errorBody), Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, ErrorUtils.parseErrorMessage(errorBody), Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, ErrorUtils.parseErrorMessage(e.message ?: "Error de conexión"), Toast.LENGTH_LONG).show()
                    } finally {
                        isReportLoading.value = false
                    }
                }
            },
            isLoading = isReportLoading.value,
            reportReason = reportReason,
            reportBody = reportBody
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("BoxActividad")
    ) {
        when {
            isLoading.value -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorResource(R.color.azulPrimario)
                )
            }
            error.value != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Error",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error.value ?: "Error desconocido",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.azulPrimario)
                        )
                    ) {
                        Text("Volver")
                    }
                }
            }
            actividad.value != null -> {
                ActividadDetalleContent(
                    actividad = actividad.value!!,
                    comunidad = comunidad.value,
                    authToken = authToken,
                    navController = navController,
                    participantes = participantes.value,
                    isParticipating = isParticipating.value,
                    username = username.value,
                    showMenu = showMenu,
                    showReportDialog = showReportDialog
                )
            }
        }
    }
}

@Composable
fun ActividadDetalleContent(
    actividad: ActividadDTO,
    comunidad: ComunidadDTO?,
    authToken: String,
    navController: NavController,
    participantes: Int,
    isParticipating: Boolean,
    username: String,
    showMenu: MutableState<Boolean>,
    showReportDialog: MutableState<Boolean>
) {
    val baseUrl = BuildConfig.URL_API
    val context = LocalContext.current

    val isUserParticipating = remember { mutableStateOf(isParticipating) }
    val isLoading = remember { mutableStateOf(false) }
    val cantidadParticipantes = remember { mutableStateOf(0) }
    val isLoadingParticipantes = remember { mutableStateOf(true) }
    val actividadExpirada = remember { mutableStateOf(false) }

    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
    val scope = rememberCoroutineScope()

    LaunchedEffect(actividad) {
        val fechaActual = Date()
        actividadExpirada.value = actividad.fechaFinalizacion.before(fechaActual)
    }

    LaunchedEffect(actividad._id) {
        isLoadingParticipantes.value = true
        try {
            val response = withContext(Dispatchers.IO) {
                withTimeout(5000) {
                    retrofitService.contarUsuariosEnUnaActividad(
                        actividadId = actividad._id,
                        token = authToken
                    )
                }
            }

            if (response.isSuccessful) {
                cantidadParticipantes.value = response.body() ?: 0
            } else {
                Log.e("ActividadDetalle", "Error al contar participantes: ${response.message()}")
                cantidadParticipantes.value = participantes
            }
        } catch (e: Exception) {
            Log.e("ActividadDetalle", "Excepción al contar participantes: ${e.message}")
            cantidadParticipantes.value = participantes
        } finally {
            isLoadingParticipantes.value = false
        }
    }

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val imageLoader = ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("actividad_images"))
                .maxSizeBytes(50 * 1024 * 1024)
                .build()
        }
        .okHttpClient(okHttpClient)
        .build()

    val formatoFecha = SimpleDateFormat("dd 'de' MMMM", Locale("es"))
    val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())

    val horaInicio = formatoHora.format(actividad.fechaInicio)
    val horaFin = formatoHora.format(actividad.fechaFinalizacion)
    val fechaInicio = formatoFecha.format(actividad.fechaInicio)
    val fechaFin = formatoFecha.format(actividad.fechaFinalizacion)

    val fechaFormateada = if (fechaInicio == fechaFin) {
        "$fechaInicio $horaInicio-$horaFin"
    } else {
        "$fechaInicio $horaInicio - $fechaFin $horaFin"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (!actividad.fotosCarruselIds.isNullOrEmpty()) {
                CarruselImagenesActividad(
                    imagenIds = actividad.fotosCarruselIds,
                    baseUrl = baseUrl,
                    authToken = authToken,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(colorResource(R.color.azulPrimario).copy(alpha = 0.1f))
                )
            }

            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = colorResource(R.color.azulPrimario),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { showMenu.value = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Opciones",
                        tint = colorResource(R.color.azulPrimario),
                    )
                }

                DropdownMenu(
                    expanded = showMenu.value,
                    onDismissRequest = { showMenu.value = false },
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    offset = DpOffset(x = (-160).dp, y = 0.dp),
                    properties = PopupProperties(
                        focusable = true,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calendar),
                                    contentDescription = "Reportar",
                                    tint = colorResource(R.color.error),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reportar actividad",
                                    color = Color.Black
                                )
                            }
                        },
                        onClick = {
                            showMenu.value = false
                            showReportDialog.value = true
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    if (actividad.creador == username) {
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_description),
                                        contentDescription = "Modificar",
                                        tint = colorResource(R.color.azulPrimario),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Modificar actividad",
                                        color = Color.Black
                                    )
                                }
                            },
                            onClick = {
                                showMenu.value = false
                                navController.navigate(AppScreen.ModificarActividadScreen.createRoute(
                                    actividad._id))
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = actividad.nombre,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (actividad.privada) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = "Actividad privada",
                        tint = colorResource(R.color.textoSecundario),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = "Fecha",
                    tint = colorResource(R.color.azulPrimario),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = fechaFormateada,
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = "Ubicación",
                    tint = colorResource(R.color.textoSecundario),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = actividad.lugar,
                    fontSize = 14.sp,
                    color = colorResource(R.color.textoSecundario)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (actividad.coordenadas.latitud.isNotEmpty() && actividad.coordenadas.longitud.isNotEmpty()) {
                Text(
                    text = "Ubicación en el mapa",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    MapaActividad(
                        latitud = actividad.coordenadas.latitud.toDoubleOrNull() ?: 0.0,
                        longitud = actividad.coordenadas.longitud.toDoubleOrNull() ?: 0.0,
                        lugar = actividad.lugar
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Acerca de esta actividad",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = actividad.descripcion,
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Organizadores",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Log.d("ActividadDetalle", "Renderizando organizadores. Comunidad: ${comunidad?.nombre}, URL: ${comunidad?.url}")

            comunidad?.let { comunidadData ->
                Log.d("ActividadDetalle", "Mostrando comunidad: ${comunidadData.nombre}")
                if (comunidadData.url == "comunidad_privada") {
                    Log.d("ActividadDetalle", "Renderizando tarjeta de comunidad privada")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Gray.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lock),
                                    contentDescription = "Comunidad privada",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Comunidad privada",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )

                                Text(
                                    text = "No tienes acceso a esta comunidad",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    Log.d("ActividadDetalle", "Renderizando tarjeta de comunidad normal")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate(
                                    AppScreen.ComunidadDetalleScreen.createRoute(comunidadData.url)
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, colorResource(R.color.cyanSecundario).copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colorResource(R.color.cyanSecundario)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (comunidadData.fotoPerfilId.isNotEmpty()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data("$baseUrl/files/download/${comunidadData.fotoPerfilId}")
                                            .crossfade(true)
                                            .placeholder(R.drawable.app_icon)
                                            .error(R.drawable.app_icon)
                                            .setHeader("Authorization", authToken)
                                            .build(),
                                        contentDescription = "Foto de perfil de ${comunidadData.nombre}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        imageLoader = imageLoader
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.app_icon),
                                        contentDescription = "Perfil por defecto",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comunidadData.nombre,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )

                                Text(
                                    text = "Comunidad organizadora",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Ver comunidad",
                                tint = colorResource(R.color.azulPrimario),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            } ?: run {
                Log.d("ActividadDetalle", "Comunidad es null, no se muestra tarjeta")
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        navController.navigate(
                            AppScreen.UsuarioDetalleScreen.createRoute(actividad.creador)
                        )
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, colorResource(R.color.azulPrimario).copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colorResource(R.color.azulPrimario).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = actividad.creador.first().toString().uppercase(),
                            color = colorResource(R.color.azulPrimario),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = actividad.creador,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )

                        Text(
                            text = "Convocante",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Ver usuario",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        val nombreActividadEncoded = URLEncoder.encode(actividad.nombre, StandardCharsets.UTF_8.toString())
                        navController.navigate(
                            AppScreen.VerUsuariosPorActividadScreen.createRoute(
                                actividadId = actividad._id,
                                nombreActividad = nombreActividadEncoded
                            )
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.cyanSecundario)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingParticipantes.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = cantidadParticipantes.value.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "usuarios se han unido ya",
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (actividadExpirada.value) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = false
                ) {
                    Text(
                        text = "ACTIVIDAD EXPIRADA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (!isLoading.value) {
                            isLoading.value = true
                            scope.launch {
                                try {
                                    val participantesDTO = ParticipantesActividadDTO(
                                        username = username,
                                        actividadId = actividad._id,
                                        nombreActividad = actividad.nombre
                                    )

                                    withContext(Dispatchers.IO) {
                                        if (!isUserParticipating.value) {
                                            val response = withTimeout(5000) {
                                                retrofitService.unirseActividad(
                                                    participantesActividadDTO = participantesDTO,
                                                    token = authToken
                                                )
                                            }

                                            withContext(Dispatchers.Main) {
                                                if (response.isSuccessful) {
                                                    isUserParticipating.value = true
                                                    Toast.makeText(context, "Te has unido a la actividad", Toast.LENGTH_SHORT).show()
                                                    cantidadParticipantes.value += 1
                                                } else {
                                                    Toast.makeText(context, "Error al unirse: ${response.message()}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            val response = withTimeout(5000) {
                                                retrofitService.salirActividad(
                                                    participantesActividadDTO = participantesDTO,
                                                    token = authToken
                                                )
                                            }

                                            withContext(Dispatchers.Main) {
                                                if (response.isSuccessful) {
                                                    isUserParticipating.value = false
                                                    Toast.makeText(context, "Has abandonado la actividad", Toast.LENGTH_SHORT).show()
                                                    if (cantidadParticipantes.value > 0) {
                                                        cantidadParticipantes.value -= 1
                                                    }
                                                } else {
                                                    Toast.makeText(context, "Error al abandonar: ${response.message()}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        Log.d("ActividadCarousel", "Error:"+e)
                                    }
                                } finally {
                                    isLoading.value = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUserParticipating.value)
                            Color.Gray
                        else
                            colorResource(R.color.azulPrimario)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isUserParticipating.value) "ABANDONAR" else "UNIRSE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUserParticipating.value) Color.DarkGray else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MapaActividad(
    latitud: Double,
    longitud: Double,
    lugar: String
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

            val mapView = MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                val geoPoint = GeoPoint(latitud, longitud)
                controller.setZoom(15.0)
                controller.setCenter(geoPoint)

                val marker = Marker(this).apply {
                    position = geoPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = lugar
                    snippet = "Ubicación de la actividad"
                }
                overlays.add(marker)

                setOnTouchListener { _, _ -> true }
            }
            mapView
        },
        modifier = Modifier.fillMaxSize()
    ) { mapView ->
        if (latitud != 0.0 && longitud != 0.0) {
            val geoPoint = GeoPoint(latitud, longitud)
            mapView.controller.setCenter(geoPoint)

            mapView.overlays.clear()
            val marker = Marker(mapView).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = lugar
                snippet = "Ubicación de la actividad"
            }
            mapView.overlays.add(marker)
            mapView.invalidate()
        }
    }
}

@Composable
fun CarruselImagenesActividad(
    imagenIds: List<String>,
    baseUrl: String,
    authToken: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { imagenIds.size })

    val imageLoader = ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .build()

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val imagenUrl = "$baseUrl/files/download/${imagenIds[page]}"

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imagenUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.app_icon)
                        .error(R.drawable.app_icon)
                        .setHeader("Authorization", authToken)
                        .build(),
                    contentDescription = "Imagen ${page + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    imageLoader = imageLoader
                )
            }
        }

        if (imagenIds.size > 1) {
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(imagenIds.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration)
                        colorResource(R.color.azulPrimario) else Color.White.copy(alpha = 0.6f)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}