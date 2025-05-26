package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
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
import com.example.socialme_interfazgrafica.model.SolicitudAmistadDTO
import com.example.socialme_interfazgrafica.model.UsuarioDTO
import com.example.socialme_interfazgrafica.navigation.AppScreen
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesAmistadScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    // Obtener credenciales del usuario
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val username = sharedPreferences.getString("USERNAME", "") ?: ""
    val authToken = "Bearer $token"

    // Estados para manejar las solicitudes
    var solicitudes by remember { mutableStateOf<List<SolicitudAmistadDTO>>(emptyList()) }
    var usuarios by remember { mutableStateOf<Map<String, UsuarioDTO>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Formatear fechas
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Configurar ImageLoader para optimizar carga de imágenes
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
                .directory(context.cacheDir.resolve("profile_images"))
                .maxSizeBytes(50 * 1024 * 1024)
                .build()
        }
        .okHttpClient {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        }
        .build()

    // Función para cargar solicitudes de amistad
    fun cargarSolicitudes() {
        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                // Obtener las solicitudes pendientes
                val response = apiService.verSolicitudesAmistad(authToken, username)

                if (response.isSuccessful) {
                    val listaSolicitudes = response.body() ?: emptyList()
                    solicitudes = listaSolicitudes

                    // Obtener información de cada usuario remitente
                    val usuariosMap = mutableMapOf<String, UsuarioDTO>()

                    listaSolicitudes.forEach { solicitud ->
                        try {
                            val usuarioResponse = apiService.verUsuarioPorUsername(authToken, solicitud.remitente)
                            if (usuarioResponse.isSuccessful) {
                                usuarioResponse.body()?.let { usuario ->
                                    usuariosMap[solicitud.remitente] = usuario
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("SolicitudesAmistadScreen", "Error al obtener usuario ${solicitud.remitente}: ${e.message}")
                        }
                    }

                    usuarios = usuariosMap
                    isLoading = false
                } else {
                    errorMessage = "Error al cargar solicitudes: ${response.code()}"
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                isLoading = false
                Log.e("SolicitudesAmistadScreen", "Error: ${e.message}")
            }
        }
    }

    // Función para aceptar solicitud
    fun aceptarSolicitud(solicitudId: String) {
        scope.launch {
            try {
                val response = apiService.aceptarSolicitud(authToken, solicitudId)

                if (response.isSuccessful) {
                    // Actualizar la lista eliminando la solicitud aceptada
                    solicitudes = solicitudes.filter { it._id != solicitudId }
                    Toast.makeText(context, "Solicitud aceptada correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al aceptar la solicitud", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                Log.e("SolicitudesAmistadScreen", "Error al aceptar solicitud: ${e.message}")
            }
        }
    }

    // Función para rechazar solicitud (eliminarla)
    fun rechazarSolicitud(solicitudId: String) {
        scope.launch {
            try {
                val response = apiService.rechazarSolicitud(authToken, solicitudId)

                if (response.isSuccessful) {
                    // Actualizar la lista eliminando la solicitud rechazada
                    solicitudes = solicitudes.filter { it._id != solicitudId }
                    Toast.makeText(context, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al rechazar la solicitud", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                Log.e("SolicitudesAmistadScreen", "Error al rechazar solicitud: ${e.message}")
            }
        }
    }

    // Cargar solicitudes cuando se inicia la pantalla
    LaunchedEffect(Unit) {
        cargarSolicitudes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes de Amistad") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.azulPrimario),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.background))
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorResource(R.color.azulPrimario))
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = colorResource(R.color.error),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { cargarSolicitudes() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(R.color.azulPrimario)
                                )
                            ) {
                                Text("Intentar de nuevo")
                            }
                        }
                    }
                }
                solicitudes.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes solicitudes de amistad pendientes",
                            color = colorResource(R.color.textoSecundario),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(
                            items = solicitudes,
                            key = { it._id }
                        ) { solicitud ->
                            SolicitudCard(
                                solicitud = solicitud,
                                usuario = usuarios[solicitud.remitente],
                                onAceptar = { aceptarSolicitud(solicitud._id) },
                                onRechazar = { rechazarSolicitud(solicitud._id) },
                                onVerPerfil = { navController.navigate(AppScreen.UsuarioDetalleScreen.createRoute(solicitud.remitente)) },
                                imageLoader = imageLoader,
                                authToken = authToken
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SolicitudCard(
    solicitud: SolicitudAmistadDTO,
    usuario: UsuarioDTO?,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onVerPerfil: () -> Unit,
    imageLoader: ImageLoader,
    authToken: String
) {
    // Base URL para las imágenes
    val baseUrl = "https://social-me-tfg.onrender.com"
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onVerPerfil),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Información de usuario con foto
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Foto de perfil
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.cyanSecundario)),
                    contentAlignment = Alignment.Center
                ) {
                    if (usuario != null && usuario.fotoPerfilId!!.isNotEmpty()) {
                        val fotoPerfilUrl = "$baseUrl/files/download/${usuario.fotoPerfilId}"
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(fotoPerfilUrl)
                                .crossfade(true)
                                .setHeader("Authorization", authToken)
                                .placeholder(R.drawable.ic_user)
                                .error(R.drawable.ic_user)
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
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Información del usuario
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (usuario != null) {
                        Text(
                            text = "${usuario.nombre} ${usuario.apellido}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colorResource(R.color.azulPrimario)
                        )
                    }

                    Text(
                        text = "@${solicitud.remitente}",
                        fontSize = 14.sp,
                        color = colorResource(R.color.textoSecundario)
                    )

                    // Fecha de envío
                    solicitud.fechaEnviada?.let { fecha ->
                        val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fecha)
                        Text(
                            text = "Enviada el $formattedDate",
                            fontSize = 12.sp,
                            color = colorResource(R.color.textoSecundario)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de aceptar y rechazar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onAceptar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aceptar")
                }

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedButton(
                    onClick = onRechazar,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(R.color.error)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Rechazar")
                }
            }
        }
    }
}