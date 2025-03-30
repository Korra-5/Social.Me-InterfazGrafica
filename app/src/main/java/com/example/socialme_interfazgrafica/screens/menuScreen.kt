package com.example.socialme_interfazgrafica.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.socialme_interfazgrafica.R
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import kotlinx.coroutines.launch


@Composable
fun MenuScreen() {
    val context = LocalContext.current
    val username = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Recuperar datos guardados
    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username.value = sharedPreferences.getString("USERNAME", "") ?: ""
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
                .padding(top = 16.dp, bottom = 80.dp) // Bottom padding for navigation bar
        ) {
            // Header with welcome message
            Text(
                text = "Hola, ${username.value}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Divider(
                color = colorResource(R.color.cyanSecundario),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Communities carousel
            if (username.value.isNotEmpty()) {
                ComunidadCarousel(username = username.value)
            }

            // Aquí puedes añadir más secciones como:
            // - Publicaciones recientes
            // - Sugerencias de comunidades
            // - Eventos próximos
            // - Etc.

            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder for future content sections
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.white)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Contenido adicional",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.azulPrimario)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Aquí puedes añadir más secciones como publicaciones recientes, sugerencias de comunidades, eventos próximos, etc.",
                        color = colorResource(R.color.textoSecundario)
                    )
                }
            }
        }
    }
}@Composable
fun ComunidadCarousel(username: String) {
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
                    Log.d("ComunidadCarousel", "Comunidades cargadas: ${comunidades.size}")
                } else {
                    errorMessage = when (response.code()) {
                        401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                        404 -> "No se encontraron comunidades para este usuario."
                        else -> "Error al cargar comunidades: ${response.message()}"
                    }
                    Log.e("ComunidadCarousel", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("ComunidadCarousel", "Excepción: ${e.message}", e)
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
                        .height(200.dp),
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
                        .height(200.dp)
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
                        .height(200.dp)
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
                // Carrusel de comunidades
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(comunidades) { comunidad ->
                        ComunidadCard(comunidad = comunidad)
                    }
                }
            }
        }
    }
}

@Composable
fun ComunidadCard(comunidad: ComunidadDTO) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(320.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.white)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen de portada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                // Imagen de fondo del carrusel (primera imagen o imagen por defecto)
                val carruselImage = comunidad.fotoCarrusel?.firstOrNull() ?: ""
                if (carruselImage.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(carruselImage)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Portada de ${comunidad.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(id = R.drawable.app_icon)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "Portada por defecto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Imagen de perfil sobrepuesta
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 40.dp)
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.white))
                        .padding(4.dp)
                ) {
                    if (comunidad.fotoPerfil.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(comunidad.fotoPerfil)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Perfil de ${comunidad.nombre}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            error = painterResource(id = R.drawable.app_icon)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon),
                            contentDescription = "Perfil por defecto",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }
            }

            // Espacio para compensar la imagen de perfil
            Spacer(modifier = Modifier.height(48.dp))

            // Información de la comunidad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Nombre de la comunidad
                Text(
                    text = comunidad.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.azulPrimario),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // URL
                Text(
                    text = "@${comunidad.url}",
                    fontSize = 14.sp,
                    color = colorResource(R.color.textoSecundario),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = comunidad.descripcion,
                    fontSize = 14.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Etiquetas de intereses
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(comunidad.intereses.take(3)) { interes ->
                        Badge(
                            containerColor = colorResource(R.color.cyanSecundario)
                        ) {
                            Text(
                                text = interes,
                                color = colorResource(R.color.azulPrimario),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Indicador de más intereses
                    if (comunidad.intereses.size > 3) {
                        item {
                            Badge(
                                containerColor = colorResource(R.color.cyanSecundario)
                            ) {
                                Text(
                                    text = "+${comunidad.intereses.size - 3}",
                                    color = colorResource(R.color.azulPrimario),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Estado (privada/global)
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
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Privada",
                            fontSize = 12.sp,
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
                            fontSize = 12.sp,
                            color = colorResource(R.color.textoSecundario)
                        )
                    }
                }
            }
        }
    }
}