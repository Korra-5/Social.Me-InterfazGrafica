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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.DenunciaCreateDTO
import com.example.socialme_interfazgrafica.model.ParticipantesActividadDTO
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.example.socialme_interfazgrafica.utils.FunctionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

    // Estados para el menú desplegable
    val showMenu = remember { mutableStateOf(false) }

    // Estados para el diálogo de denuncia
    val showReportDialog = remember { mutableStateOf(false) }
    val reportReason = remember { mutableStateOf("") }
    val reportBody = remember { mutableStateOf("") }
    val isReportLoading = remember { mutableStateOf(false) }

    val utils = FunctionUtils

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Obtener el nombre de usuario actual desde SharedPreferences
    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username.value = sharedPreferences.getString("USERNAME", "") ?: ""
    }

    // Cargar datos de la actividad
    LaunchedEffect(actividadId, username.value) {
        if (username.value.isEmpty()) return@LaunchedEffect

        isLoading.value = true
        error.value = null

        try {
            val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

            // Use supervisorScope to prevent child coroutine failures from canceling the parent
            supervisorScope {
                try {
                    // Obtain data with timeout to prevent hanging
                    val actividadResponseDeferred = async(Dispatchers.IO) {
                        retrofitService.verActividadPorId(
                            token = authToken,
                            id = actividadId
                        )
                    }

                    val actividadResponse = withTimeout(10000) { actividadResponseDeferred.await() }

                    if (actividadResponse.isSuccessful && actividadResponse.body() != null) {
                        actividad.value = actividadResponse.body()
                        participantes.value = (5..25).random()

                        // If the activity is loaded successfully, check participation status
                        val participantesDTO = ParticipantesActividadDTO(
                            username = username.value,
                            actividadId = actividadId,
                            nombreActividad = actividadResponse.body()?.nombre ?: ""
                        )

                        val participacionResponseDeferred = async(Dispatchers.IO) {
                            retrofitService.booleanUsuarioApuntadoActividad(
                                participantesActividadDTO = participantesDTO,
                                token = authToken
                            )
                        }

                        val participacionResponse = withTimeout(8000) { participacionResponseDeferred.await() }
                        isParticipating.value = participacionResponse.isSuccessful &&
                                participacionResponse.body() == true
                    } else {
                        error.value = "No se pudo cargar la actividad: ${actividadResponse.message()}"
                    }
                } catch (e: Exception) {
                    error.value = when (e) {
                        is TimeoutCancellationException -> "Tiempo de espera agotado. Comprueba tu conexión."
                        else -> "Error de red: ${e.message}"
                    }
                }
            }
        } catch (e: Exception) {
            error.value = "Error general: ${e.message}"
        } finally {
            isLoading.value = false
        }
    }

    // Diálogo de denuncia
    if (showReportDialog.value) {
        utils.ReportDialog(
            onDismiss = { showReportDialog.value = false },
            onConfirm = { motivo, cuerpo ->
                // Crear denuncia
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
                            RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                                .crearDenuncia(authToken, denunciaDTO)
                        }

                        if (response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Denuncia enviada correctamente", Toast.LENGTH_SHORT).show()
                                showReportDialog.value = false
                            }
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                            withContext(Dispatchers.Main) {
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
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, ErrorUtils.parseErrorMessage(e.message ?: "Error de conexión"), Toast.LENGTH_LONG).show()
                        }
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

// Menú desplegable mejorado
                DropdownMenu(
                    expanded = showMenu.value,
                    onDismissRequest = { showMenu.value = false },
                    modifier = Modifier
                        .padding(end = 8.dp, top = 8.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    offset = DpOffset(x = (-8).dp, y = 4.dp)
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

                    // Si es el creador, añadir opción para modificar
                    if (actividad.value!!.creador == username.toString()) {
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
                                navController.navigate(AppScreen.ModificarActividadScreen.createRoute(actividadId))
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
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
    val baseUrl = "https://social-me-tfg.onrender.com"
    val context = LocalContext.current

    // Estado para controlar si el usuario está participando
    val isUserParticipating = remember { mutableStateOf(isParticipating) }

    // ViewModel para operaciones API
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
    val scope = rememberCoroutineScope()

    // Estado de carga para el botón
    val isLoading = remember { mutableStateOf(false) }

    // Estado para almacenar el número de participantes
    val cantidadParticipantes = remember { mutableStateOf(0) }
    // Estado para controlar si está cargando el contador de participantes
    val isLoadingParticipantes = remember { mutableStateOf(true) }

    // Estado para verificar si el usuario es creador o administrador de la actividad
    val isCreadorOAdmin = remember { mutableStateOf(false) }
    // Estado para controlar si está cargando la verificación
    val isLoadingVerificacion = remember { mutableStateOf(true) }

    // Cargar el número de participantes
    LaunchedEffect(actividad._id) {
        isLoadingParticipantes.value = true
        scope.launch {
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
                    // Fallback a un valor predeterminado en caso de error
                    cantidadParticipantes.value = participantes
                }
            } catch (e: Exception) {
                Log.e("ActividadDetalle", "Excepción al contar participantes: ${e.message}")
                // Fallback a un valor predeterminado en caso de error
                cantidadParticipantes.value = participantes
            } finally {
                isLoadingParticipantes.value = false
            }
        }
    }

    // Verificar si el usuario es creador o administrador de la actividad
    LaunchedEffect(actividad._id) {
        isLoadingVerificacion.value = true
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    withTimeout(10000) { // Increase timeout to 10 seconds
                        retrofitService.verificarCreadorAdministradorActividad(
                            idActvidad = actividad._id, // Fix the parameter name if needed
                            token = authToken,
                            username = username
                        )
                    }
                }

                if (response.isSuccessful) {
                    isCreadorOAdmin.value = response.isSuccessful && response.body() == true
                } else {
                    // More detailed error logging
                    Log.e(
                        "ActividadDetalle",
                        "Error al verificar permisos: ${response.code()} - ${response.message()}"
                    )
                    Log.e("ActividadDetalle", "Error body: ${response.errorBody()?.string()}")
                    isCreadorOAdmin.value = false
                }
            } catch (e: Exception) {
                // More detailed exception logging
                Log.e(
                    "ActividadDetalle",
                    "Excepción al verificar permisos: ${e.javaClass.simpleName} - ${e.message}"
                )
                e.printStackTrace()
                isCreadorOAdmin.value = false
            } finally {
                isLoadingVerificacion.value = false
            }
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
        // Header con imagen y botón de retroceso
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

            // Botón de retroceso en la esquina superior
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = colorResource(R.color.azulPrimario),
                )
            }

            // Botón de tres puntos para opciones
            IconButton(
                onClick = { showMenu.value = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Opciones",
                    tint = colorResource(R.color.azulPrimario),
                )
            }
        }

        // Resto del contenido permanece igual...
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título de la actividad
            Text(
                text = actividad.nombre,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Fecha y hora con icono
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

            // Lugar con icono
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, colorResource(R.color.azulPrimario).copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = "Ubicación",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = actividad.lugar,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Separador
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Descripción de la actividad
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

            // El resto del código permanece igual...
            // Sección de organizadores
            Text(
                text = "Organizadores",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Información de la comunidad
            if (comunidad != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, colorResource(R.color.cyanSecundario).copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            if (comunidad.fotoPerfilId.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data("$baseUrl/files/download/${comunidad.fotoPerfilId}")
                                        .crossfade(true)
                                        .placeholder(R.drawable.app_icon)
                                        .error(R.drawable.app_icon)
                                        .setHeader("Authorization", authToken)
                                        .build(),
                                    contentDescription = "Foto de perfil de ${comunidad.nombre}",
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

                        Column {
                            Text(
                                text = comunidad.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )

                            Text(
                                text = "Comunidad",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información del creador
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, colorResource(R.color.azulPrimario).copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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

                    Column {
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
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Número de usuarios unidos
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        // Navegar a la pantalla de usuarios por actividad
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

            // Botón de unirse/abandonar con estado actualizado
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
                                        // Unirse a la actividad
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

                                                // Actualizar el contador de participantes
                                                cantidadParticipantes.value += 1
                                            } else {
                                                Toast.makeText(context, "Error al unirse: ${response.message()}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        // Salir de la actividad
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

                                                // Actualizar el contador de participantes
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
            }// Botón MODIFICAR ACTIVIDAD - Solo visible si el usuario es creador o administrador
            if (isCreadorOAdmin.value) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Navegar a la pantalla de modificación de actividad
                        navController.navigate(
                            AppScreen.ModificarActividadScreen.createRoute(actividad._id)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    if (isLoadingVerificacion.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "MODIFICAR ACTIVIDAD",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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

        // Indicadores de páginas en la parte inferior
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