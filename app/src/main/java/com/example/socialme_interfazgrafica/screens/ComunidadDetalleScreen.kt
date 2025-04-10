package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
import com.example.socialme_interfazgrafica.model.ParticipantesComunidadDTO
import com.example.socialme_interfazgrafica.model.RegistroResponse
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
import retrofit2.Response
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComunidadDetalleScreen(comunidad: ComunidadDTO, authToken: String, navController: NavController) {
    val baseUrl = "https://social-me-tfg.onrender.com"
    val context = LocalContext.current

    // Estado para controlar si el usuario está participando en la comunidad
    val isUserParticipating = remember { mutableStateOf(false) }
    // Estado de carga para el botón
    val isLoading = remember { mutableStateOf(false) }
    // Para almacenar el nombre de usuario actual
    val username = remember { mutableStateOf("") }

    // Obtener el nombre de usuario actual desde SharedPreferences
    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username.value = sharedPreferences.getString("USERNAME", "") ?: ""
    }

    // Verificar si el usuario ya participa en la comunidad
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
    val scope = rememberCoroutineScope()

    // Verificar participación cuando se cargue la pantalla
    LaunchedEffect(username.value) {
        if (username.value.isEmpty()) return@LaunchedEffect

        isLoading.value = true
        try {
            supervisorScope {
                val participantesComunidadDTO = ParticipantesComunidadDTO(
                    username = username.value,
                    comunidad = comunidad.url,
                )

                val participacionResponseDeferred = async(Dispatchers.IO) {
                    retrofitService.booleanUsuarioApuntadoComunidad(
                        participantesComunidadDTO = participantesComunidadDTO,
                        token = authToken
                    )
                }

                val participacionResponse = withTimeout(8000) { participacionResponseDeferred.await() }
                isUserParticipating.value = participacionResponse.isSuccessful &&
                        participacionResponse.body() == true
            }
        } catch (e: Exception) {
            Log.e("ComunidadDetalle", "Error verificando participación: ${e.message}")
        } finally {
            isLoading.value = false
        }
    }

    // Configurar cliente HTTP con timeouts para imágenes
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Configurar ImageLoader optimizado con caching
    val imageLoader = ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .okHttpClient(okHttpClient)
        .build()

    // Construir URL para foto de perfil
    val fotoPerfilUrl = if (comunidad.fotoPerfilId.isNotEmpty())
        "$baseUrl/files/download/${comunidad.fotoPerfilId}"
    else ""

    // Formatear fecha de creación
    val fechaCreacion = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(comunidad.fechaCreacion)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Usar un Column dentro de un ScrollState para hacer scroll vertical
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header con foto de perfil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (fotoPerfilUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(fotoPerfilUrl)
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
                    // Fondo por defecto
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorResource(R.color.azulPrimario))
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

            // Contenido principal con fondo blanco
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // Nombre y URL
                Text(
                    text = comunidad.nombre,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "@${comunidad.url}",
                    fontSize = 16.sp,
                    color = colorResource(R.color.textoSecundario)
                )

                // Etiquetas privada/global
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                ) {
                    if (comunidad.privada) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = "Comunidad privada",
                            tint = colorResource(R.color.textoSecundario),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Privada",
                            fontSize = 14.sp,
                            color = colorResource(R.color.textoSecundario)
                        )
                    }

                    if (comunidad.comunidadGlobal) {
                        if (comunidad.privada) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = "Comunidad global",
                            tint = colorResource(R.color.textoSecundario),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Global",
                            fontSize = 14.sp,
                            color = colorResource(R.color.textoSecundario)
                        )
                    }
                }

                // Divider
                Divider(color = Color.LightGray, thickness = 1.dp)

                // Descripción
                Text(
                    text = "Descripción",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.azulPrimario),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Text(
                    text = comunidad.descripcion,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Divider
                Divider(color = Color.LightGray, thickness = 1.dp)

                // Intereses/Tags
                Text(
                    text = "Intereses",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.azulPrimario),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    comunidad.intereses.forEach { interes ->
                        Surface(
                            modifier = Modifier.padding(bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = colorResource(R.color.cyanSecundario)
                        ) {
                            Text(
                                text = interes,
                                color = colorResource(R.color.azulPrimario),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Divider
                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 16.dp)
                )

                // Información de creación
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_user),
                        contentDescription = "Creador",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Creado por: @${comunidad.creador}",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = "Fecha de creación",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fecha de creación: $fechaCreacion",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }

                // Mostrar administradores si hay
                if (!comunidad.administradores.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Administradores:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.azulPrimario)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    comunidad.administradores.take(3).forEach { admin ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colorResource(R.color.cyanSecundario)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = admin.first().toString().uppercase(),
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
                    }

                    if ((comunidad.administradores.size) > 3) {
                        Text(
                            text = "Y ${comunidad.administradores.size - 3} más...",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Añadir carrusel de imágenes si hay
                if (!comunidad.fotoCarruselIds.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)

                    Text(
                        text = "Galería",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.azulPrimario),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    CarruselImagenes(
                        imagenIds = comunidad.fotoCarruselIds,
                        baseUrl = baseUrl,
                        authToken = authToken,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)

                // Botón de unirse/abandonar con estado actualizado
                Button(
                    onClick = {
                        if (!isLoading.value && username.value.isNotEmpty()) {
                            isLoading.value = true
                            scope.launch {
                                try {
                                    val participantesComunidadDTO = ParticipantesComunidadDTO(
                                        username = username.value,
                                        comunidad = comunidad.url,
                                    )

                                    withContext(Dispatchers.IO) {
                                        if (!isUserParticipating.value) {
                                            // Unirse a la comunidad
                                            val response = withTimeout(5000) {
                                                retrofitService.unirseComunidad(
                                                    participantesComunidadDTO = participantesComunidadDTO,
                                                    token = authToken
                                                )
                                            }

                                            withContext(Dispatchers.Main) {
                                                if (response.isSuccessful) {
                                                    isUserParticipating.value = true
                                                    Toast.makeText(context, "Te has unido a la comunidad", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Error al unirse: ${response.message()}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            // Salir de la comunidad
                                            val response = withTimeout(5000) {
                                                retrofitService.salirComunidad(
                                                    participantesComunidadDTO = participantesComunidadDTO,
                                                    token = authToken
                                                )
                                            }

                                            withContext(Dispatchers.Main) {
                                                if (response.isSuccessful) {
                                                    isUserParticipating.value = false
                                                    Toast.makeText(context, "Has abandonado la comunidad", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Error al abandonar: ${response.message()}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        Log.e("ComunidadDetalle", "Error: $e")
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

                // Carrusel de actividades de la comunidad
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)

                // Añadir el carrusel de actividades de la comunidad
                CarruselActividadesComunidad(comunidadUrl = comunidad.url, navController = navController)

                // Espacio adicional al final
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Función auxiliar para mostrar carrusel de imágenes
@Composable
fun CarruselImagenes(
    imagenIds: List<String>,
    baseUrl: String,
    authToken: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { imagenIds.size })

    // Configurar cliente HTTP con timeouts para imágenes
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Configurar ImageLoader optimizado con caching
    val imageLoader = ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25) // Usa 25% de la memoria para cache
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("carrusel_images"))
                .maxSizeBytes(50 * 1024 * 1024) // Cache de 50MB
                .build()
        }
        .okHttpClient(okHttpClient) // Usar el cliente HTTP configurado
        .build()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = modifier
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
                        .memoryCacheKey("carrusel_${imagenIds[page]}")
                        .diskCacheKey("carrusel_${imagenIds[page]}")
                        .build(),
                    contentDescription = "Imagen ${page + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    imageLoader = imageLoader
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Indicadores de páginas
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(imagenIds.size) { iteration ->
                val color = if (pagerState.currentPage == iteration)
                    colorResource(R.color.azulPrimario) else colorResource(R.color.cyanSecundario)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun CarruselActividadesComunidad(comunidadUrl: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var actividades by remember { mutableStateOf<List<ActividadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Función para cargar las actividades de la comunidad
    fun cargarActividadesComunidad() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Obtener el token desde SharedPreferences
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    Log.e("CarruselActividadesComunidad", "Token vacío, no se puede proceder")
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                // Realizar la petición con el token formateado correctamente
                val authToken = "Bearer $token"
                Log.d("CarruselActividadesComunidad", "Realizando petición API con token: ${token.take(5)}... para comunidad: $comunidadUrl")
                val response = apiService.verActividadesPorComunidad(authToken, comunidadUrl)

                if (response.isSuccessful) {
                    val actividadesRecibidas = response.body() ?: emptyList()
                    Log.d("CarruselActividadesComunidad", "Actividades recibidas correctamente: ${actividadesRecibidas.size}")
                    actividades = actividadesRecibidas
                } else {
                    // Tratamiento especial para el error 500 cuando no hay actividades
                    if (response.code() == 500) {
                        // Asumimos que es porque la comunidad no tiene actividades
                        Log.w("CarruselActividadesComunidad", "Código 500 recibido, asumiendo lista vacía")
                        actividades = emptyList()
                    } else {
                        val errorCode = response.code()
                        Log.e("CarruselActividadesComunidad", "Error al cargar actividades. Código: $errorCode, Mensaje: ${response.message()}")
                        errorMessage = when (errorCode) {
                            401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                            404 -> "No se encontraron actividades en esta comunidad."
                            else -> "Error al cargar actividades: ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                // Mostrar un mensaje más específico en caso de error de conexión
                Log.e("CarruselActividadesComunidad", "Excepción al cargar actividades", e)
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace() // Imprime la traza completa para depuración
            } finally {
                isLoading = false
                Log.d("CarruselActividadesComunidad", "Finalizada carga de actividades. isLoading: $isLoading, errorMessage: $errorMessage")
            }
        }
    }

    // Cargar actividades cuando se inicializa el componente
    LaunchedEffect(comunidadUrl) {
        Log.d("CarruselActividadesComunidad", "LaunchedEffect iniciado para comunidad: $comunidadUrl")
        cargarActividadesComunidad()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Título de sección
        Text(
            text = "Actividades de esta comunidad",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        // Mostrar estado de carga, error o el carrusel
        when {
            isLoading -> {
                Log.d("CarruselActividadesComunidad", "Mostrando indicador de carga")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorResource(R.color.azulPrimario)
                    )
                }
            }
            errorMessage != null -> {
                Log.d("CarruselActividadesComunidad", "Mostrando mensaje de error: $errorMessage")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = colorResource(R.color.error),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                Log.d("CarruselActividadesComunidad", "Botón 'Intentar de nuevo' pulsado")
                                cargarActividadesComunidad()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario)
                            )
                        ) {
                            Text("Intentar de nuevo")
                        }
                    }
                }
            }
            actividades.isEmpty() -> {
                Log.d("CarruselActividadesComunidad", "Mostrando mensaje de lista vacía")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay actividades en esta comunidad",
                        color = colorResource(R.color.textoSecundario),
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                // Carrusel de actividades optimizado
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = actividades,
                        key = { it.nombre }
                    ) { actividad ->
                        Log.d("CarruselActividadesComunidad", "Cargando actividad: ${actividad.nombre}")
                        ActividadCard(actividad = actividad, navController = navController)
                    }
                }
            }
        }
    }
}