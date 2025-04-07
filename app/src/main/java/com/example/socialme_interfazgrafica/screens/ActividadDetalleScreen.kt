package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.socialme_interfazgrafica.model.ParticipantesActividadDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

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

    LaunchedEffect(actividadId) {
        isLoading.value = true
        error.value = null

        try {
            val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

            val actividadResponse = withContext(Dispatchers.IO) {
                retrofitService.verActividadPorId(
                    token = authToken,
                    username = actividadId
                )
            }

            if (actividadResponse.isSuccessful && actividadResponse.body() != null) {
                actividad.value = actividadResponse.body()

                // Simular número de participantes (esto debería venir del backend)
                participantes.value = (5..25).random()

                val comunidadUrl = actividadResponse.body()?.creador
                if (!comunidadUrl.isNullOrEmpty()) {
                    val comunidadResponse = withContext(Dispatchers.IO) {
                        retrofitService.verComunidadPorUrl(
                            token = authToken,
                            username = comunidadUrl
                        )
                    }

                    if (comunidadResponse.isSuccessful && comunidadResponse.body() != null) {
                        comunidad.value = comunidadResponse.body()
                    }
                }
            } else {
                error.value = "No se pudo cargar la actividad: ${actividadResponse.message()}"
            }
        } catch (e: Exception) {
            error.value = "Error: ${e.message}"
        } finally {
            isLoading.value = false
        }
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                    participantes = participantes.value
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
    participantes: Int
) {
    val baseUrl = "https://social-me-tfg.onrender.com"
    val context = LocalContext.current

    // State to track if user is participating
    val isParticipating = remember { mutableStateOf(false) }
    // Get current username from SharedPreferences
    val username = remember { mutableStateOf("") }

    // ViewModel for API operations
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
    val scope = rememberCoroutineScope()

    // Loading state for button
    val isLoading = remember { mutableStateOf(false) }

    // Get username from SharedPreferences
    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username.value = sharedPreferences.getString("USERNAME", "") ?: ""

        // TODO: Check if user is already participating in this activity
        // This would require an additional API endpoint to check participation status
        // For now, we're assuming the user is not participating
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
        }

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
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.cyanSecundario)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (if (isParticipating.value) participantes else participantes).toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "usuarios se han unido ya",
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de unirse/abandonar
            Button(
                onClick = {
                    if (!isLoading.value) {
                        isLoading.value = true
                        scope.launch {
                            try {
                                if (!isParticipating.value) {
                                    // Unirse a la actividad
                                    val participantesDTO = ParticipantesActividadDTO(
                                        username = username.value,
                                        actividadId = actividad._id,
                                        nombreActividad = actividad.nombre
                                    )

                                    val response = retrofitService.unirseActividad(
                                        participantesActividadDTO = participantesDTO,
                                        token = authToken
                                    )

                                    if (response.isSuccessful) {
                                        isParticipating.value = true
                                        Toast.makeText(context, "Te has unido a la actividad", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error al unirse: ${response.message()}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val participantesDTO = ParticipantesActividadDTO(
                                        username = username.value,
                                        actividadId = actividad._id,
                                        nombreActividad = actividad.nombre
                                    )

                                    val response = retrofitService.salirActividad(
                                        participantesActividadDTO = participantesDTO,
                                        token = authToken
                                    )

                                    isParticipating.value = false
                                    Toast.makeText(context, "Has abandonado la actividad", Toast.LENGTH_SHORT).show()
                                    if (response.isSuccessful) {
                                        isParticipating.value = true
                                        Toast.makeText(context, "Te has unido a la actividad", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error al unirse: ${response.message()}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    containerColor = if (isParticipating.value)
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
                        text = if (isParticipating.value) "ABANDONAR" else "UNIRSE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isParticipating.value) Color.DarkGray else Color.White
                    )
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