package com.example.socialme_interfazgrafica.screens


import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient


// Screens para mostrar detalles de Actividad y Comunidad
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadDetallePantalla(actividadId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var actividad by remember { mutableStateOf<ActividadDTO?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Base URL para las imágenes
    val baseUrl = "https://social-me-tfg.onrender.com"

    // Obtener token
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val authToken = "Bearer $token"

    // Servicio Retrofit
    val service = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    // Efecto para cargar datos al inicio
    LaunchedEffect(actividadId) {
        coroutineScope.launch {
            try {
                isLoading = true
                error = null

                val response = service.verActividadPorId(authToken, actividadId)
                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    actividad = response.body()!![0]
                    Log.d("ActividadDetalle", "Actividad cargada: ${actividad?.nombre}")
                } else {
                    error = "No se pudo cargar la actividad: ${response.code()}"
                    Log.e("ActividadDetalle", error ?: "Error desconocido")
                }
            } catch (e: Exception) {
                error = "Error: ${e.message}"
                Log.e("ActividadDetalle", "Error al cargar actividad", e)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = actividad?.nombre ?: "Cargando actividad...") },
                navigationIcon = {
                    IconButton(onClick = { /* Navegar hacia atrás */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = colorResource(R.color.error),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "Error desconocido",
                            color = colorResource(R.color.error),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                actividad != null -> {
                    val act = actividad!!
                    ActividadDetalleContenido(act, authToken)
                }
            }
        }
    }
}

@Composable
fun ActividadDetalleContenido(actividad: ActividadDTO, authToken: String) {
    val baseUrl = "https://social-me-tfg.onrender.com"
    val context = LocalContext.current

    // Formatear fechas
    val fechaInicio = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(actividad.fechaInicio)
    val fechaFinalizacion = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(actividad.fechaFinalizacion)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Carrusel de imágenes
        if (actividad.fotosCarruselIds.isNotEmpty()) {
            CarruselImagenes(
                imagenIds = actividad.fotosCarruselIds,
                baseUrl = baseUrl,
                authToken = authToken,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorResource(R.color.cyanSecundario)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "Imagen por defecto",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Título
        Text(
            text = actividad.nombre,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Creador
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_user),
                contentDescription = "Creador",
                tint = colorResource(R.color.textoSecundario),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Creado por: @${actividad.creador}",
                fontSize = 14.sp,
                color = colorResource(R.color.textoSecundario)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Descripción
        Text(
            text = "Descripción",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = actividad.descripcion,
            fontSize = 16.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Fechas y lugar
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.cyanSecundario))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = "Fechas",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Desde: $fechaInicio",
                        fontSize = 14.sp,
                        color = colorResource(R.color.azulPrimario)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = "Fechas",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hasta: $fechaFinalizacion",
                        fontSize = 14.sp,
                        color = colorResource(R.color.azulPrimario)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = "Lugar",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = actividad.lugar,
                        fontSize = 14.sp,
                        color = colorResource(R.color.azulPrimario)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón para unirse a la actividad
        Button(
            onClick = { /* Implementar lógica para unirse a la actividad */ },
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
    }
}