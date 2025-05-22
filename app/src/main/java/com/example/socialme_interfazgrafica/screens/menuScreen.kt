package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.screens.PreferenciasUsuario.getDistanciaRadar
import com.example.socialme_interfazgrafica.viewModel.NotificacionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Composable
fun MenuScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
    val username = remember { mutableStateOf("") }
    val radar = remember { mutableStateOf("") }
    val token = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Estado para el número de solicitudes pendientes
    var solicitudesPendientes by remember { mutableStateOf(0) }

    // ViewModel para notificaciones
    val notificacionViewModel: NotificacionViewModel = viewModel()

    // Recuperar datos guardados
    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username.value = sharedPreferences.getString("USERNAME", "") ?: ""
        token.value = sharedPreferences.getString("TOKEN", "") ?: ""
        radar.value = getDistanciaRadar(context).toString()

        // Cargar el número de solicitudes pendientes si el usuario está logueado
        if (username.value.isNotEmpty()) {
            scope.launch {
                try {
                    val authToken = "Bearer ${token.value}"
                    val response = apiService.verSolicitudesAmistad(authToken, username.value)
                    if (response.isSuccessful) {
                        solicitudesPendientes = (response.body() ?: emptyList()).size
                    }
                } catch (e: Exception) {
                    Log.e("MenuScreen", "Error al cargar solicitudes: ${e.message}")
                }
            }
        }
    }

    // Inicializar WebSocket para notificaciones y cargar notificaciones no leídas
    LaunchedEffect(username.value) {
        if (username.value.isNotEmpty()) {
            notificacionViewModel.inicializarWebSocket(username.value)
            notificacionViewModel.contarNoLeidas(username.value, token.value)
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
                .verticalScroll(scrollState)
                .padding(top = 16.dp, bottom = 80.dp)
        ) {
            // Header con perfil del usuario, notificaciones y botón de solicitudes de amistad
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Perfil de usuario con tamaño ampliado (ahora ocupa más espacio)
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                ) {
                    UserProfileHeader(
                        username = username.value,
                        navController = navController,
                    )
                }

                // Espacio entre perfil y botones
                Spacer(modifier = Modifier.width(10.dp))

                // Contenedor para los botones (ahora con menor peso)
                Row(
                    modifier = Modifier.weight(0.7f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de notificaciones con contador
                    BadgedIconImproved(
                        count = notificacionViewModel.numeroNoLeidas.toInt(),
                        iconPainter = painterResource(id = R.drawable.ic_email),
                        contentDescription = "Notificaciones",
                        onClick = { navController.navigate(AppScreen.NotificacionesScreen.route) }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Botón de solicitudes de amistad con contador
                    BadgedIconImproved(
                        count = solicitudesPendientes,
                        iconPainter = painterResource(id = R.drawable.ic_user),
                        contentDescription = "Solicitudes de Amistad",
                        onClick = { navController.navigate(AppScreen.SolicitudesAmistadScreen.route) },
                        isAmistadButton = true
                    )
                }
            }

            Divider(
                color = colorResource(R.color.cyanSecundario),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // IMPORTANTE: Secciones de Comunidades y Actividades
            if (username.value.isNotEmpty()) {
                ComunidadCarousel(username = username.value, navController)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (username.value.isNotEmpty()) {
                ActividadCarousel(username = username.value, navController)
            }

            if (username.value.isNotEmpty()){
                VerTodasComunidadesCarrousel(username=username.value, navController, radar.value)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            if (username.value.isNotEmpty()) {
                CarrouselActvidadesPorComunidad(username = username.value, navController)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (username.value.isNotEmpty()){
                CarrouselActividadesEnZona(username=username.value, navController, radar.value)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Navigation Bar
        BottomNavBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


// Componente para mostrar un contador de notificaciones con tamaño reducido
@Composable
fun BadgedIconImproved(
    count: Int,
    iconPainter: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    isAmistadButton: Boolean = false
) {
    Box {
        // Colores según si es botón de amistad o no
        val backgroundColor = if (isAmistadButton)
            colorResource(R.color.azulPrimario) else colorResource(R.color.cyanSecundario)
        val iconColor = if (isAmistadButton)
            Color.White else colorResource(R.color.azulPrimario)

        // Botón con tamaño más pequeño pero manteniendo la sombra
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp) // Tamaño reducido
                .shadow(
                    elevation = 3.dp,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(backgroundColor)
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(26.dp) // Icono más pequeño
            )
        }

        // Badge con contador
        if (count > 0) {
            Badge(
                containerColor = colorResource(R.color.error),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(18.dp) // Badge más pequeño
            ) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    fontSize = 9.sp, // Texto más pequeño
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
// Componente que podemos agregar al BottomNavBar con IconButton para notificaciones
@Composable
fun NotificacionIndicator(
    count: Int,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick
    ) {
        BadgedBox(
            badge = {
                if (count > 0) {
                    Badge {
                        Text(
                            text = if (count > 99) "99+" else count.toString(),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificaciones",
                tint = colorResource(R.color.cyanSecundario)
            )
        }
    }
}

// BottomNavBar actualizado para incluir notificaciones
@Composable
fun BottomNavBar(navController: NavController, modifier: Modifier = Modifier) {
    // Obtener el número de notificaciones no leídas
    val notificacionViewModel: NotificacionViewModel = viewModel()
    val notificacionesNoLeidas = notificacionViewModel.numeroNoLeidas.toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.background)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Home
            IconButton(
                onClick = {
                    navController.navigate(AppScreen.MenuScreen.route) {
                        // Pop hasta el inicio de la pila y luego añade la pantalla destino
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = colorResource(R.color.cyanSecundario)
                )
            }

            // Botón Buscar
            IconButton(
                onClick = {
                    navController.navigate(AppScreen.BusquedaScreen.route)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = colorResource(R.color.cyanSecundario)
                )
            }

            // Botón Notificaciones
            NotificacionIndicator(
                count = notificacionesNoLeidas,
                onClick = { navController.navigate(AppScreen.NotificacionesScreen.route) }
            )

            // Botón Opciones
            IconButton(
                onClick = {
                    navController.navigate(AppScreen.OpcionesScreen.route)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Opciones",
                    tint = colorResource(R.color.cyanSecundario)
                )
            }
        }
    }
}
@Composable
fun UserProfileHeader(
    username: String,
    navController: NavController
) {
    val context = LocalContext.current
    // Obtener el token de autenticación
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val authToken = "Bearer $token"

    // Estados para la foto de perfil y nombre completo
    var nombreCompleto by remember { mutableStateOf("") }
    var fotoPerfilId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    // Cargar datos de usuario
    LaunchedEffect(username) {
        scope.launch {
            try {
                val response = apiService.verUsuarioPorUsername(authToken, username)
                if (response.isSuccessful && response.body() != null) {
                    val usuario = response.body()!!
                    nombreCompleto = "${usuario.nombre} ${usuario.apellido}"
                    fotoPerfilId = usuario.fotoPerfilId ?: ""
                }
            } catch (e: Exception) {
                Log.e("UserProfileHeader", "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Configurar ImageLoader
    val imageLoader = ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .build()

    // Tarjeta mejorada con mejor manejo de nombres largos
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 2.dp, top = 2.dp, bottom = 2.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                navController.navigate(AppScreen.UsuarioDetalleScreen.createRoute(username))
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del usuario
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.cyanSecundario)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorResource(R.color.azulPrimario),
                        strokeWidth = 2.dp
                    )
                } else if (fotoPerfilId.isNotEmpty()) {
                    val baseUrl = "https://social-me-tfg.onrender.com"
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("$baseUrl/files/download/$fotoPerfilId")
                            .crossfade(true)
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .setHeader("Authorization", authToken)
                            .build(),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        imageLoader = imageLoader
                    )
                } else {
                    // Por defecto mostramos un icono de usuario
                    Icon(
                        painter = painterResource(id = R.drawable.ic_user),
                        contentDescription = "Usuario",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del usuario (ahora con mejor manejo de nombres largos)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                // Determinar el tamaño de fuente basado en la longitud del nombre
                val nombreFontSize = when {
                    nombreCompleto.length > 25 -> 14.sp
                    nombreCompleto.length > 20 -> 16.sp
                    else -> 18.sp
                }

                // Mostrar el nombre completo con líneas múltiples si es necesario
                Text(
                    text = if (nombreCompleto.isNotEmpty()) nombreCompleto else username,
                    fontSize = nombreFontSize,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.azulPrimario),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                // Nombre de usuario
                Text(
                    text = "@$username",
                    fontSize = 14.sp,
                    color = colorResource(R.color.textoSecundario),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Icono para indicar que es clickable
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Ver perfil",
                tint = colorResource(R.color.azulPrimario),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// El resto del código permanece igual...
@Composable
fun ComunidadCarousel(username: String,navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var comunidades by remember { mutableStateOf<List<ComunidadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Función para cargar las comunidades
    fun cargarComunidades() {
        scope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Obtener el token desde SharedPreferences
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                // Realizar la petición con el token formateado correctamente
                val authToken = "Bearer $token"
                val response = apiService.verComunidadPorUsuario(authToken, username)

                if (response.isSuccessful) {
                    comunidades = response.body() ?: emptyList()
                } else {
                    // Tratamiento especial para el error 500 cuando no hay comunidades
                    if (response.code() == 500) {
                        // Asumimos que es porque el usuario no tiene comunidades
                        comunidades = emptyList()
                    } else {
                        errorMessage = when (response.code()) {
                            401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                            404 -> "No se encontraron comunidades para este usuario."
                            else -> "Error al cargar comunidades: ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                // Mostrar un mensaje más específico en caso de error de conexión
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace() // Imprime la traza completa para depuración
            } finally {
                isLoading = false
            }
        }
    }

    // Cargar comunidades cuando se inicializa el componente
    LaunchedEffect(username) {
        cargarComunidades()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Título de sección
        Text(
            text = "Tus Comunidades",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        // Mostrar estado de carga, error o el carrusel
        when {
            isLoading -> {
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
                            onClick = { cargarComunidades() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario)
                            )
                        ) {
                            Text("Intentar de nuevo")
                        }
                    }
                }
            }
            comunidades.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No perteneces a ninguna comunidad",
                        color = colorResource(R.color.textoSecundario),
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                // Carrusel de comunidades optimizado
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = comunidades,
                        key = { it.url }
                    ) { comunidad ->
                        ComunidadCard(comunidad = comunidad, navController = navController)
                    }
                }
            }
        }
    }
}
@Composable
fun ActividadCarousel(username: String,navController:NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var actividades by remember { mutableStateOf<List<ActividadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Función para cargar las actividades
    fun cargarActividades(navController:NavController) {
        scope.launch {
            isLoading = true
            errorMessage = null
            Log.d("ActividadCarousel", "Iniciando carga de actividades para usuario: $username")

            try {
                // Obtener el token desde SharedPreferences
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    Log.e("ActividadCarousel", "Token vacío, no se puede proceder")
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                // Realizar la petición con el token formateado correctamente
                val authToken = "Bearer $token"
                Log.d("ActividadCarousel", "Realizando petición API con token: ${token.take(5)}...")
                val response = apiService.verActividadPorUsername(authToken, username)

                if (response.isSuccessful) {
                    val actividadesRecibidas = response.body() ?: emptyList()
                    Log.d("ActividadCarousel", "Actividades recibidas correctamente: ${actividadesRecibidas.size}")
                    actividades = actividadesRecibidas
                } else {
                    // Tratamiento especial para el error 500 cuando no hay actividades
                    if (response.code() == 500) {
                        // Asumimos que es porque el usuario no tiene actividades
                        Log.w("ActividadCarousel", "Código 500 recibido, asumiendo lista vacía")
                        actividades = emptyList()
                    } else {
                        val errorCode = response.code()
                        Log.e("ActividadCarousel", "Error al cargar actividades. Código: $errorCode, Mensaje: ${response.message()}")
                        errorMessage = when (errorCode) {
                            401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                            404 -> "No estas apuntado a ninguna actividad."
                            else -> "Error al cargar actividades: ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                // Mostrar un mensaje más específico en caso de error de conexión
                Log.e("ActividadCarousel", "Excepción al cargar actividades", e)
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace() // Imprime la traza completa para depuración
            } finally {
                isLoading = false
                Log.d("ActividadCarousel", "Finalizada carga de actividades. isLoading: $isLoading, errorMessage: $errorMessage")
            }
        }
    }

    // Cargar actividades cuando se inicializa el componente
    LaunchedEffect(username) {
        Log.d("ActividadCarousel", "LaunchedEffect iniciado para usuario: $username")
        cargarActividades(navController)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Título de sección
        Text(
            text = "Tus Actividades",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        // Mostrar estado de carga, error o el carrusel
        when {
            isLoading -> {
                Log.d("ActividadCarousel", "Mostrando indicador de carga")
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
                Log.d("ActividadCarousel", "Mostrando mensaje de error: $errorMessage")
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
                                Log.d("ActividadCarousel", "Botón 'Intentar de nuevo' pulsado")
                                cargarActividades(navController)
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
                Log.d("ActividadCarousel", "Mostrando mensaje de lista vacía")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No participas en ninguna actividad",
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
                        Log.d("ActividadCarousel", "Cargando actividad: ${actividad.nombre}")
                        ActividadCard(actividad = actividad, navController = navController)
                    }
                }
            }
        }
    }
}@Composable
fun CarrouselActvidadesPorComunidad(username: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var actividades by remember { mutableStateOf<List<ActividadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Función para cargar las actividades
    fun cargarActividades() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Obtener el token desde SharedPreferences
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    Log.e("CarrouselActvidadesPorComunidad", "Token vacío, no se puede proceder")
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                // Realizar la petición con el token formateado correctamente
                val authToken = "Bearer $token"
                Log.d("CarrouselActvidadesPorComunidad", "Realizando petición API con token: ${token.take(5)}...")
                val response = apiService.verActividadNoParticipaUsuario(token = authToken, username = username)

                if (response.isSuccessful) {
                    val actividadesRecibidas = response.body() ?: emptyList()
                    Log.d("CarrouselActvidadesPorComunidad", "Actividades recibidas correctamente: ${actividadesRecibidas.size}")
                    actividades = actividadesRecibidas
                } else {
                    // Tratamiento especial para el error 500 cuando no hay actividades
                    if (response.code() == 500) {
                        // Asumimos que es porque el usuario no tiene actividades
                        Log.w("CarrouselActvidadesPorComunidad", "Código 500 recibido, asumiendo lista vacía")
                        actividades = emptyList()
                    } else {
                        val errorCode = response.code()
                        Log.e("CarrouselActvidadesPorComunidad", "Error al cargar actividades. Código: $errorCode, Mensaje: ${response.message()}")
                        errorMessage = when (errorCode) {
                            401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                            404 -> "No se encontraron actividades publicas en esta zona."
                            else -> "Error al cargar actividades: ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                // Mostrar un mensaje más específico en caso de error de conexión
                Log.e("CarrouselActvidadesPorComunidad", "Excepción al cargar actividades", e)
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace() // Imprime la traza completa para depuración
            } finally {
                isLoading = false
                Log.d("CarrouselActvidadesPorComunidad", "Finalizada carga de actividades. isLoading: $isLoading, errorMessage: $errorMessage")
            }
        }
    }

    // Usar LaunchedEffect para llamar a la función de carga cuando el componente se compone
    LaunchedEffect(username) {
        cargarActividades()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Título de sección
        Text(
            text = "¡Unete a actividades de tus comunidades!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        // Mostrar estado de carga, error o el carrusel
        when {
            isLoading -> {
                Log.d("CarrouselActvidadesPorComunidad", "Mostrando indicador de carga")
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
                Log.d("CarrouselActvidadesPorComunidad", "Mostrando mensaje de error: $errorMessage")
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
                                Log.d("CarrouselActvidadesPorComunidad", "Botón 'Intentar de nuevo' pulsado")
                                cargarActividades()
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
                Log.d("CarrouselActvidadesPorComunidad", "Mostrando mensaje de lista vacía")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay actividades disponibles para unirse",
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
                        Log.d("CarrouselActvidadesPorComunidad", "Cargando actividad: ${actividad.nombre}")
                        ActividadCard(actividad = actividad, navController)
                    }
                }
            }
        }
    }
}
// Modified Activity Card with navigation
@Composable
fun ActividadCard(actividad: ActividadDTO, navController: NavController) {
    val context = LocalContext.current
    // Base URL para las imágenes de MongoDB
    val baseUrl = "https://social-me-tfg.onrender.com"

    Log.d("ActividadCard", "Inicializando card para actividad: ${actividad.nombre}")

    // Obtener el token de autenticación de SharedPreferences
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val authToken = "Bearer $token"

    Log.d("ActividadCard", "Token recuperado (primeros 5 caracteres): ${token.take(5)}...")

    // Construir URL para imágenes
    val tieneImagenes = actividad.fotosCarruselIds.isNotEmpty()
    val imagenUrl = if (tieneImagenes)
        "$baseUrl/files/download/${actividad.fotosCarruselIds[0]}"
    else ""

    Log.d("ActividadCard", "Tiene imágenes: $tieneImagenes, URL primera imagen: ${if (tieneImagenes) imagenUrl.take(50) + "..." else "N/A"}")

    // Configurar cliente HTTP con timeouts
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
                .directory(context.cacheDir.resolve("actividad_images"))
                .maxSizeBytes(50 * 1024 * 1024) // Cache de 50MB
                .build()
        }
        .okHttpClient(okHttpClient) // Usar el cliente HTTP configurado
        .build()

    // Formatear fechas
    val fechaInicio =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(actividad.fechaInicio)
    val fechaFinalizacion =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(actividad.fechaFinalizacion)

    Log.d("ActividadCard", "Fechas formateadas - Inicio: $fechaInicio, Fin: $fechaFinalizacion")

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(250.dp)
            .clickable {
                Log.d("ActividadCard", "Card clickeada, navegando a detalle de actividad: ${actividad.nombre}")
                // Navegar a la pantalla de detalle de la actividad, pasando el ID como parámetro
                navController.navigate("actividadDetalle/${actividad._id}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.white)
        )
    ) {
        // El resto del código de ActividadCard se mantiene igual
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de actividad (primera del carrusel si existe)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorResource(R.color.cyanSecundario)),
                contentAlignment = Alignment.Center
            ) {
                if (tieneImagenes) {
                    Log.d("ActividadCard", "Iniciando carga de imagen: ${actividad.fotosCarruselIds[0]}")
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imagenUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.app_icon)
                            .error(R.drawable.app_icon)
                            .setHeader("Authorization", authToken)
                            .memoryCacheKey("actividad_${actividad.fotosCarruselIds[0]}")
                            .diskCacheKey("actividad_${actividad.fotosCarruselIds[0]}")
                            .build(),
                        contentDescription = "Foto de ${actividad.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        imageLoader = imageLoader,
                        onLoading = {
                            Log.d("ActividadCard", "Cargando imagen de actividad: ${actividad.nombre}")
                        },
                        onSuccess = {
                            Log.d("ActividadCard", "Imagen cargada exitosamente: ${actividad.nombre}")
                        },
                        onError = {
                            Log.e("ActividadCard", "Error al cargar imagen de actividad: ${actividad.nombre}")
                        }
                    )
                } else {
                    Log.d("ActividadCard", "Usando imagen por defecto para actividad: ${actividad.nombre}")
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "Imagen por defecto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre de la actividad
            Text(
                text = actividad.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Creador
            Text(
                text = "Por: @${actividad.creador}",
                fontSize = 12.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Descripción
            Text(
                text = actividad.descripcion,
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fechas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = "Fechas",
                    tint = colorResource(R.color.textoSecundario),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$fechaInicio - $fechaFinalizacion",
                    fontSize = 10.sp,
                    color = colorResource(R.color.textoSecundario)
                )
            }
        }
    }
}

// Modified Community Card with navigation
@Composable
fun ComunidadCard(comunidad: ComunidadDTO, navController: NavController) {
    val context = LocalContext.current
    // Base URL para las imágenes de MongoDB
    val baseUrl = "https://social-me-tfg.onrender.com"

    // Obtener el token de autenticación de SharedPreferences
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val authToken = "Bearer $token"

    // Construir URL completa
    val fotoPerfilUrl = if (comunidad.fotoPerfilId.isNotEmpty())
        "$baseUrl/files/download/${comunidad.fotoPerfilId}"
    else ""

    // Configurar cliente HTTP con timeouts
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
                .directory(context.cacheDir.resolve("comunidad_images"))
                .maxSizeBytes(50 * 1024 * 1024) // Cache de 50MB
                .build()
        }
        .okHttpClient(okHttpClient) // Usar el cliente HTTP configurado
        .build()

    Card(
        modifier = Modifier
            .width(180.dp)
            .height(220.dp)
            .clickable {
                Log.d("ComunidadCard", "Card clickeada, navegando a detalle de comunidad: ${comunidad.nombre}")
                // Navegar a la pantalla de detalle de la comunidad, pasando la URL como parámetro
                navController.navigate("comunidadDetalle/${comunidad.url}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.white)
        )
    ) {
        // El resto del código de ComunidadCard se mantiene igual
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto de perfil con manejo mejorado de errores y caching
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.cyanSecundario)),
                contentAlignment = Alignment.Center
            ) {
                if (fotoPerfilUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(fotoPerfilUrl)
                            .crossfade(true)
                            .size(128, 128) // Solicitar imagen más pequeña
                            .placeholder(R.drawable.app_icon)
                            .error(R.drawable.app_icon)
                            .setHeader("Authorization", authToken)
                            .memoryCacheKey(fotoPerfilUrl) // Clave para cache
                            .diskCacheKey(fotoPerfilUrl)
                            .build(),
                        contentDescription = "Foto de ${comunidad.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        imageLoader = imageLoader,
                        onLoading = {
                            Log.d("ComunidadCard", "Cargando imagen: $fotoPerfilUrl")
                        },
                        onSuccess = {
                            Log.d("ComunidadCard", "Imagen cargada exitosamente: $fotoPerfilUrl")
                        },
                        onError = {
                            Log.e("ComunidadCard", "Error al cargar imagen: $fotoPerfilUrl")
                        }
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

            // Nombre de la comunidad y demás contenido...
            Spacer(modifier = Modifier.height(8.dp))

            // Nombre de la comunidad
            Text(
                text = comunidad.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // URL
            Text(
                text = "@${comunidad.url}",
                fontSize = 12.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Descripción
            Text(
                text = comunidad.descripcion,
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tags/Intereses
            if (comunidad.intereses.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tagsToShow = if (comunidad.intereses.size > 2) 2 else comunidad.intereses.size

                    items(comunidad.intereses.take(tagsToShow)) { interes ->
                        Badge(
                            containerColor = colorResource(R.color.cyanSecundario)
                        ) {
                            Text(
                                text = interes,
                                color = colorResource(R.color.azulPrimario),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Indicador de más tags
                    if (comunidad.intereses.size > 2) {
                        item {
                            Badge(
                                containerColor = colorResource(R.color.cyanSecundario)
                            ) {
                                Text(
                                    text = "+${comunidad.intereses.size - 2}",
                                    color = colorResource(R.color.azulPrimario),
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Etiquetas privada/global
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (comunidad.privada) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = "Comunidad privada",
                        tint = colorResource(R.color.textoSecundario),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Privada",
                        fontSize = 10.sp,
                        color = colorResource(R.color.textoSecundario)
                    )
                }

                if (comunidad.comunidadGlobal) {
                    if (comunidad.privada) {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_user),
                        contentDescription = "Comunidad global",
                        tint = colorResource(R.color.textoSecundario),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Global",
                        fontSize = 10.sp,
                        color = colorResource(R.color.textoSecundario)
                    )
                }
            }
        }
    }
}

// Componente actualizado para corregir el error de conversión del radar
@Composable
fun VerTodasComunidadesCarrousel(username: String, navController: NavController, radar: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var comunidades by remember { mutableStateOf<List<ComunidadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Extraer el valor numérico del radar de forma segura
    val radarValue = try {
        radar.toFloatOrNull() ?: 50f
    } catch (e: Exception) {
        Log.e("VerTodasComunidadesCarrousel", "Error al convertir radar: $radar", e)
        50f // Valor predeterminado en caso de error
    }

    // Función para cargar las comunidades
    fun cargarTodasComunidades() {
        scope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Obtener el token desde SharedPreferences
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                // Realizar la petición con el token formateado correctamente
                val authToken = "Bearer $token"
                val response = apiService.verComunidadesPublicas(authToken, username, radarValue)

                if (response.isSuccessful) {
                    comunidades = response.body() ?: emptyList()
                } else {
                    // Tratamiento especial para el error 500 cuando no hay comunidades
                    if (response.code() == 500) {
                        // Asumimos que es porque el usuario no tiene comunidades
                        comunidades = emptyList()
                    } else {
                        errorMessage = when (response.code()) {
                            401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                            404 -> "No se encontraron comunidades públicas en esta zona."
                            else -> "Error al cargar comunidades: ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                // Mostrar un mensaje más específico en caso de error de conexión
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace() // Imprime la traza completa para depuración
            } finally {
                isLoading = false
            }
        }
    }

    // Cargar comunidades cuando se inicializa el componente
    LaunchedEffect(username) {
        cargarTodasComunidades()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Título de sección
        Text(
            text = "Comunidades públicas en tu zona",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        // Mostrar estado de carga, error o el carrusel
        when {
            isLoading -> {
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
                            onClick = { cargarTodasComunidades() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario)
                            )
                        ) {
                            Text("Intentar de nuevo")
                        }
                    }
                }
            }
            comunidades.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay comunidades públicas en tu zona",
                        color = colorResource(R.color.textoSecundario),
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                // Carrusel de comunidades optimizado
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = comunidades,
                        key = { it.url }
                    ) { comunidad ->
                        ComunidadCard(comunidad = comunidad, navController = navController)
                    }
                }
            }
        }
    }
}

// También actualizamos este componente para mejorar el manejo del radar
@Composable
fun CarrouselActividadesEnZona(username: String, navController: NavController, radar: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var actividades by remember { mutableStateOf<List<ActividadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Extraer el valor numérico del radar de forma segura
    val radarValue = try {
        radar.toFloatOrNull() ?: 50f
    } catch (e: Exception) {
        Log.e("CarrouselActividadesEnZona", "Error al convertir radar: $radar", e)
        50f // Valor predeterminado en caso de error
    }

    // Función para cargar las actividades
    fun cargarActividadesPublicasEnTuZona(navController: NavController) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Obtener el token desde SharedPreferences
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    Log.e("CarrouselActividadesEnZona", "Token vacío, no se puede proceder")
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                // Realizar la petición con el token formateado correctamente
                val authToken = "Bearer $token"
                Log.d("CarrouselActividadesEnZona", "Realizando petición API con token: ${token.take(5)}...")
                val response = apiService.verActividadesPublicas(authToken, username, radarValue)

                if (response.isSuccessful) {
                    val actividadesRecibidas = response.body() ?: emptyList()
                    Log.d("CarrouselActividadesEnZona", "Actividades recibidas correctamente: ${actividadesRecibidas.size}")
                    actividades = actividadesRecibidas
                } else {
                    // Tratamiento especial para el error 500 cuando no hay actividades
                    if (response.code() == 500) {
                        // Asumimos que es porque el usuario no tiene actividades
                        Log.w("CarrouselActividadesEnZona", "Código 500 recibido, asumiendo lista vacía")
                        actividades = emptyList()
                    } else {
                        val errorCode = response.code()
                        Log.e("CarrouselActividadesEnZona", "Error al cargar actividades. Código: $errorCode, Mensaje: ${response.message()}")
                        errorMessage = when (errorCode) {
                            401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                            404 -> "No se encontraron actividades públicas en esta zona."
                            else -> "Error al cargar actividades: ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                // Mostrar un mensaje más específico en caso de error de conexión
                Log.e("CarrouselActividadesEnZona", "Excepción al cargar actividades", e)
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace() // Imprime la traza completa para depuración
            } finally {
                isLoading = false
                Log.d("CarrouselActividadesEnZona", "Finalizada carga de actividades. isLoading: $isLoading, errorMessage: $errorMessage")
            }
        }
    }

    // Cargar actividades cuando se inicializa el componente
    LaunchedEffect(username) {
        Log.d("CarrouselActividadesEnZona", "LaunchedEffect iniciado para usuario: $username")
        cargarActividadesPublicasEnTuZona(navController)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Título de sección
        Text(
            text = "Actividades públicas en tu zona",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        // Mostrar estado de carga, error o el carrusel
        when {
            isLoading -> {
                Log.d("CarrouselActividadesEnZona", "Mostrando indicador de carga")
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
                Log.d("CarrouselActividadesEnZona", "Mostrando mensaje de error: $errorMessage")
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
                                Log.d("ActividadCarousel", "Botón 'Intentar de nuevo' pulsado")
                                cargarActividadesPublicasEnTuZona(navController)
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
                Log.d("CarrouselActividadesEnZona", "Mostrando mensaje de lista vacía")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay actividades públicas en tu zona",
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
                        Log.d("CarrouselActividadesEnZona", "Cargando actividad: ${actividad.nombre}")
                        ActividadCard(actividad = actividad, navController=navController)
                    }
                }
            }
        }
    }
}