package com.example.socialme_interfazgrafica.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.socialme_interfazgrafica.model.UsuarioDTO
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioDetallesScreen(navController: NavController, username: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    // Estados para manejar los datos del usuario
    var usuario by remember { mutableStateOf<UsuarioDTO?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Base URL para las imágenes
    val baseUrl = "https://social-me-tfg.onrender.com"

    // Obtener el token de autenticación
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val authToken = "Bearer $token"

    // Configurar cliente HTTP con timeouts
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Configurar ImageLoader
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
                .directory(context.cacheDir.resolve("user_profile_images"))
                .maxSizeBytes(50 * 1024 * 1024)
                .build()
        }
        .okHttpClient(okHttpClient)
        .build()

    // Cargar los datos del usuario
    LaunchedEffect(username) {
        scope.launch {
            try {
                val response = apiService.verUsuarioPorUsername(authToken, username)
                if (response.isSuccessful) {
                    usuario = response.body()
                    isLoading = false
                } else {
                    errorMessage = "Error al cargar el usuario: ${response.code()}"
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                isLoading = false
            }
        }
    }

    // UI para mostrar los datos del usuario
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra superior con botón de retroceso
            TopAppBar(
                title = { Text(text = "Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = errorMessage!!,
                                color = colorResource(R.color.error),
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
                }
                usuario != null -> {
                    // Utilizamos un LazyColumn para permitir scroll
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Sección de datos del usuario
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Foto de perfil
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(colorResource(R.color.cyanSecundario)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (usuario!!.fotoPerfilId.isNotEmpty()) {
                                        val fotoPerfilUrl = "$baseUrl/files/download/${usuario!!.fotoPerfilId}"
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(fotoPerfilUrl)
                                                .crossfade(true)
                                                .placeholder(R.drawable.ic_user)
                                                .error(R.drawable.ic_user)
                                                .setHeader("Authorization", authToken)
                                                .memoryCacheKey(fotoPerfilUrl)
                                                .diskCacheKey(fotoPerfilUrl)
                                                .build(),
                                            contentDescription = "Foto de ${usuario!!.username}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            imageLoader = imageLoader
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_user),
                                            contentDescription = "Usuario",
                                            tint = colorResource(R.color.azulPrimario),
                                            modifier = Modifier.size(60.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Nombre completo
                                Text(
                                    text = "${usuario!!.nombre} ${usuario!!.apellido}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.azulPrimario),
                                    textAlign = TextAlign.Center
                                )

                                // Nombre de usuario
                                Text(
                                    text = "@${usuario!!.username}",
                                    fontSize = 16.sp,
                                    color = colorResource(R.color.textoSecundario),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Descripción
                                if (usuario!!.descripcion.isNotEmpty()) {
                                    Text(
                                        text = usuario!!.descripcion,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))
                                }

                                // Dirección si está disponible
                                if (usuario!!.direccion.municipio.isNotEmpty() || usuario!!.direccion.provincia.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_location),
                                            contentDescription = "Ubicación",
                                            tint = colorResource(R.color.textoSecundario),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when {
                                                usuario!!.direccion.municipio.isNotEmpty() && usuario!!.direccion.provincia.isNotEmpty() ->
                                                    "${usuario!!.direccion.municipio}, ${usuario!!.direccion.provincia}"
                                                usuario!!.direccion.municipio.isNotEmpty() -> usuario!!.direccion.municipio
                                                else -> usuario!!.direccion.provincia
                                            },
                                            fontSize = 14.sp,
                                            color = colorResource(R.color.textoSecundario)
                                        )
                                    }
                                }

                                // Email
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_email),
                                        contentDescription = "Email",
                                        tint = colorResource(R.color.textoSecundario),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = usuario!!.email,
                                        fontSize = 14.sp,
                                        color = colorResource(R.color.textoSecundario)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Intereses si están disponibles
                                if (usuario!!.intereses.isNotEmpty()) {
                                    Text(
                                        text = "Intereses",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorResource(R.color.azulPrimario),
                                        modifier = Modifier.align(Alignment.Start)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Mostrar intereses en filas
                                    FlowRow(
                                        mainAxisSpacing = 8.dp,
                                        crossAxisSpacing = 8.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        usuario!!.intereses.forEach { interes ->
                                            Badge(
                                                modifier = Modifier.padding(end = 4.dp),
                                                containerColor = colorResource(R.color.cyanSecundario)
                                            ) {
                                                Text(
                                                    text = interes,
                                                    color = colorResource(R.color.azulPrimario),
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(
                                                        horizontal = 8.dp,
                                                        vertical = 4.dp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Carrusel de Comunidades
                        item {
                            ComunidadCarousel(username = username, navController = navController)
                        }

                        // Carrusel de Actividades
                        item {
                            ActividadCarousel(username = username, navController = navController)
                        }

                        // Espacio adicional al final para evitar que el contenido quede oculto
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}