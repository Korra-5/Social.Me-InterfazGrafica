package com.example.socialme_interfazgrafica.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComunidadDetalleScreen(comunidad: ComunidadDTO, authToken: String, navController: NavController) {
    val baseUrl = "https://social-me-tfg.onrender.com"
    val context = LocalContext.current

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

                Spacer(modifier = Modifier.height(32.dp))

                // Botones de acción - Similar a la pantalla de actividades
                Button(
                    onClick = { /* Implementar lógica para unirse */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "UNIRSE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* Implementar lógica para ver actividades */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = BorderStroke(1.dp, colorResource(R.color.azulPrimario)),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(R.color.azulPrimario)
                    )
                ) {
                    Text(
                        text = "VER ACTIVIDADES",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

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