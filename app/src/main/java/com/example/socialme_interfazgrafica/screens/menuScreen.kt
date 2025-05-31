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
    val token = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    var solicitudesPendientes by remember { mutableStateOf(0) }
    val notificacionViewModel: NotificacionViewModel = viewModel()

    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username.value = sharedPreferences.getString("USERNAME", "") ?: ""
        token.value = sharedPreferences.getString("TOKEN", "") ?: ""

        if (username.value.isNotEmpty()) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1.5f)
                ) {
                    UserProfileHeader(
                        username = username.value,
                        navController = navController,
                    )
                }

                Spacer(modifier = Modifier.width(1.dp))

                Box(
                    modifier = Modifier.weight(0.5f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    BadgedIconImproved(
                        count = solicitudesPendientes,
                        iconPainter = painterResource(id = R.drawable.ic_email),
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

            if (username.value.isNotEmpty()) {
                ComunidadCarouselMenu(username = username.value, navController)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (username.value.isNotEmpty()) {
                ActividadCarouselMenu(username = username.value, navController)
            }

            if (username.value.isNotEmpty()){
                VerTodasComunidadesCarrousel(username=username.value, navController)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (username.value.isNotEmpty()) {
                CarrouselActvidadesPorComunidad(username = username.value, navController)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (username.value.isNotEmpty()){
                CarrouselActividadesEnZona(username=username.value, navController)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        BottomNavBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun BadgedIconImproved(
    count: Int,
    iconPainter: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    isAmistadButton: Boolean = false
) {
    Box {
        val backgroundColor = if (isAmistadButton)
            colorResource(R.color.azulPrimario) else colorResource(R.color.cyanSecundario)
        val iconColor = if (isAmistadButton)
            Color.White else colorResource(R.color.azulPrimario)

        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
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
                modifier = Modifier.size(26.dp)
            )
        }

        if (count > 0) {
            Badge(
                containerColor = colorResource(R.color.error),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(18.dp)
            ) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

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

@Composable
fun BottomNavBar(navController: NavController, modifier: Modifier = Modifier) {
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
            IconButton(
                onClick = {
                    navController.navigate(AppScreen.MenuScreen.route) {
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

            NotificacionIndicator(
                count = notificacionesNoLeidas,
                onClick = { navController.navigate(AppScreen.NotificacionesScreen.route) }
            )

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
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val authToken = "Bearer $token"

    var nombreCompleto by remember { mutableStateOf("") }
    var fotoPerfilId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    LaunchedEffect(username) {
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

    val imageLoader = ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .build()

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
                    Icon(
                        painter = painterResource(id = R.drawable.ic_user),
                        contentDescription = "Usuario",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                val nombreFontSize = when {
                    nombreCompleto.length > 25 -> 14.sp
                    nombreCompleto.length > 20 -> 16.sp
                    else -> 18.sp
                }

                Text(
                    text = if (nombreCompleto.isNotEmpty()) nombreCompleto else username,
                    fontSize = nombreFontSize,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.azulPrimario),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Text(
                    text = "@$username",
                    fontSize = 14.sp,
                    color = colorResource(R.color.textoSecundario),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Ver perfil",
                tint = colorResource(R.color.azulPrimario),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ComunidadCarouselMenu(username: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var comunidades by remember { mutableStateOf<List<ComunidadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(username) {
        isLoading = true
        errorMessage = null

        try {
            val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("TOKEN", "") ?: ""

            if (token.isEmpty()) {
                errorMessage = "No se ha encontrado un token de autenticación"
                isLoading = false
                return@LaunchedEffect
            }

            val authToken = "Bearer $token"
            val response = apiService.verComunidadPorUsuario(authToken, username, username)

            if (response.isSuccessful) {
                comunidades = response.body() ?: emptyList()
            } else {
                if (response.code() == 500) {
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
            errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    fun cargarComunidades() {
        scope.launch {
            isLoading = true
            errorMessage = null

            try {
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                val authToken = "Bearer $token"
                val response = apiService.verComunidadPorUsuario(authToken, username, username)

                if (response.isSuccessful) {
                    comunidades = response.body() ?: emptyList()
                } else {
                    if (response.code() == 500) {
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
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Comunidades",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

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
fun ActividadCarouselMenu(username: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var actividades by remember { mutableStateOf<List<ActividadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(username) {
        isLoading = true
        errorMessage = null

        try {
            val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("TOKEN", "") ?: ""

            if (token.isEmpty()) {
                errorMessage = "No se ha encontrado un token de autenticación"
                isLoading = false
                return@LaunchedEffect
            }

            val authToken = "Bearer $token"
            val response = apiService.verActividadPorUsernameFechaSuperior(authToken, username, username)

            if (response.isSuccessful) {
                val actividadesRecibidas = response.body() ?: emptyList()
                actividades = actividadesRecibidas.sortedBy { it.fechaInicio }
            } else {
                if (response.code() == 500) {
                    actividades = emptyList()
                } else {
                    errorMessage = when (response.code()) {
                        401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                        404 -> "No estas apuntado a ninguna actividad próxima."
                        else -> "Error al cargar actividades: ${response.message()}"
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    fun cargarActividades() {
        scope.launch {
            isLoading = true
            errorMessage = null

            try {
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                val authToken = "Bearer $token"
                val response = apiService.verActividadPorUsernameFechaSuperior(authToken, username, username)

                if (response.isSuccessful) {
                    val actividadesRecibidas = response.body() ?: emptyList()
                    actividades = actividadesRecibidas.sortedBy { it.fechaInicio }
                } else {
                    if (response.code() == 500) {
                        actividades = emptyList()
                    } else {
                        errorMessage = when (response.code()) {
                            401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                            404 -> "No estas apuntado a ninguna actividad próxima."
                            else -> "Error al cargar actividades: ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Actividades próximas",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

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
                            onClick = { cargarActividades() },
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No participas en ninguna actividad próxima",
                        color = colorResource(R.color.textoSecundario),
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = actividades,
                        key = { it.nombre }
                    ) { actividad ->
                        ActividadCard(actividad = actividad, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun CarrouselActvidadesPorComunidad(username: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var actividades by remember { mutableStateOf<List<ActividadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(username) {
        isLoading = true
        errorMessage = null
        try {
            val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("TOKEN", "") ?: ""

            if (token.isEmpty()) {
                errorMessage = "No se ha encontrado un token de autenticación"
                isLoading = false
                return@LaunchedEffect
            }

            val authToken = "Bearer $token"
            val response = apiService.verActividadNoParticipaUsuarioFechaSuperior(token = authToken, username = username)

            if (response.isSuccessful) {
                val actividadesRecibidas = response.body() ?: emptyList()
                actividades = actividadesRecibidas.sortedBy { it.fechaInicio }
            } else {
                if (response.code() == 500) {
                    actividades = emptyList()
                } else {
                    errorMessage = when (response.code()) {
                        401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                        404 -> "No se encontraron actividades públicas próximas en esta zona."
                        else -> "Error al cargar actividades: ${response.message()}"
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    fun cargarActividades() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                val authToken = "Bearer $token"
                val response = apiService.verActividadNoParticipaUsuarioFechaSuperior(token = authToken, username = username)

                if (response.isSuccessful) {
                    val actividadesRecibidas = response.body() ?: emptyList()
                    actividades = actividadesRecibidas.sortedBy { it.fechaInicio }
                } else {
                    if (response.code() == 500) {
                        actividades = emptyList()
                    } else {
                        errorMessage = when (response.code()) {
                            401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                            404 -> "No se encontraron actividades públicas próximas en esta zona."
                            else -> "Error al cargar actividades: ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "¡Únete a actividades de tus comunidades!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

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
                            onClick = { cargarActividades() },
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay actividades próximas disponibles para unirse",
                        color = colorResource(R.color.textoSecundario),
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = actividades,
                        key = { it.nombre }
                    ) { actividad ->
                        ActividadCard(actividad = actividad, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun ActividadCard(actividad: ActividadDTO, navController: NavController) {
    val context = LocalContext.current
    val baseUrl = "https://social-me-tfg.onrender.com"

    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val authToken = "Bearer $token"

    val tieneImagenes = actividad.fotosCarruselIds.isNotEmpty()
    val imagenUrl = if (tieneImagenes)
        "$baseUrl/files/download/${actividad.fotosCarruselIds[0]}"
    else ""

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

    val fechaInicio =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(actividad.fechaInicio)
    val fechaFinalizacion =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(actividad.fechaFinalizacion)

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(250.dp)
            .clickable {
                navController.navigate("actividadDetalle/${actividad._id}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.white)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorResource(R.color.background)),
                contentAlignment = Alignment.Center
            ) {
                if (tieneImagenes) {
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
                        imageLoader = imageLoader
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "Imagen por defecto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = actividad.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Por: @${actividad.creador}",
                fontSize = 12.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = actividad.descripcion,
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

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

@Composable
fun ComunidadCard(comunidad: ComunidadDTO, navController: NavController) {
    val context = LocalContext.current
    val baseUrl = "https://social-me-tfg.onrender.com"

    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val authToken = "Bearer $token"

    val fotoPerfilUrl = if (comunidad.fotoPerfilId.isNotEmpty())
        "$baseUrl/files/download/${comunidad.fotoPerfilId}"
    else ""

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
                .directory(context.cacheDir.resolve("comunidad_images"))
                .maxSizeBytes(50 * 1024 * 1024)
                .build()
        }
        .okHttpClient(okHttpClient)
        .build()

    Card(
        modifier = Modifier
            .width(180.dp)
            .height(220.dp)
            .clickable {
                navController.navigate("comunidadDetalle/${comunidad.url}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.white)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.background)),
                contentAlignment = Alignment.Center
            ) {
                if (fotoPerfilUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(fotoPerfilUrl)
                            .crossfade(true)
                            .size(128, 128)
                            .placeholder(R.drawable.app_icon)
                            .error(R.drawable.app_icon)
                            .setHeader("Authorization", authToken)
                            .memoryCacheKey(fotoPerfilUrl)
                            .diskCacheKey(fotoPerfilUrl)
                            .build(),
                        contentDescription = "Foto de ${comunidad.nombre}",
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = comunidad.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "@${comunidad.url}",
                fontSize = 12.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comunidad.descripcion,
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

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
            }
        }
    }
}

@Composable
fun VerTodasComunidadesCarrousel(username: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var comunidades by remember { mutableStateOf<List<ComunidadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(username) {
        isLoading = true
        errorMessage = null

        try {
            val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("TOKEN", "") ?: ""

            if (token.isEmpty()) {
                errorMessage = "No se ha encontrado un token de autenticación"
                isLoading = false
                return@LaunchedEffect
            }

            val authToken = "Bearer $token"
            val response = apiService.verComunidadesPublicas(authToken, username)

            if (response.isSuccessful) {
                comunidades = response.body() ?: emptyList()
            } else {
                if (response.code() == 500) {
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
            errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    fun cargarTodasComunidades() {
        scope.launch {
            isLoading = true
            errorMessage = null

            try {
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                val authToken = "Bearer $token"
                val response = apiService.verComunidadesPublicas(authToken, username)

                if (response.isSuccessful) {
                    comunidades = response.body() ?: emptyList()
                } else {
                    if (response.code() == 500) {
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
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Comunidades públicas en tu zona",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

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
fun CarrouselActividadesEnZona(username: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var actividades by remember { mutableStateOf<List<ActividadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(username) {
        isLoading = true
        errorMessage = null
        try {
            val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("TOKEN", "") ?: ""

            if (token.isEmpty()) {
                errorMessage = "No se ha encontrado un token de autenticación"
                isLoading = false
                return@LaunchedEffect
            }

            val authToken = "Bearer $token"
            val response = apiService.verActividadesPublicasFechaSuperior(authToken, username)

            if (response.isSuccessful) {
                val actividadesRecibidas = response.body() ?: emptyList()
                actividades = actividadesRecibidas.sortedBy { it.fechaInicio }
            } else {
                if (response.code() == 500) {
                    actividades = emptyList()
                } else {
                    errorMessage = when (response.code()) {
                        401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                        404 -> "No se encontraron actividades públicas y próximas en esta zona."
                        else -> "Error al cargar actividades: ${response.message()}"
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    fun cargarActividadesPublicasEnTuZona() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                val authToken = "Bearer $token"
                val response = apiService.verActividadesPublicasFechaSuperior(authToken, username)

                if (response.isSuccessful) {
                    val actividadesRecibidas = response.body() ?: emptyList()
                    actividades = actividadesRecibidas.sortedBy { it.fechaInicio }
                } else {
                    if (response.code() == 500) {
                        actividades = emptyList()
                    } else {
                        errorMessage = when (response.code()) {
                            401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                            404 -> "No se encontraron actividades públicas y próximas en esta zona."
                            else -> "Error al cargar actividades: ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Actividades públicas en tu zona",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

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
                            onClick = { cargarActividadesPublicasEnTuZona() },
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
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = actividades,
                        key = { it.nombre }
                    ) { actividad ->
                        ActividadCard(actividad = actividad, navController=navController)
                    }
                }
            }
        }
    }
}