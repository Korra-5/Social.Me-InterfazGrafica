// screens/NotificacionesScreen.kt
package com.example.socialme_interfazgrafica.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.model.NotificacionDTO
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.viewModel.NotificacionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    navController: NavController,
    notificacionViewModel: NotificacionViewModel,
    username: String,
    authToken: String
) {
    val notificaciones = notificacionViewModel.notificaciones
    val isLoading = notificacionViewModel.isLoading
    val errorMessage = notificacionViewModel.errorMessage

    // Cargar notificaciones cuando se abre la pantalla
    LaunchedEffect(key1 = true) {
        // Inicializar WebSocket si no está inicializado
        notificacionViewModel.inicializarWebSocket(username)

        // Cargar notificaciones
        notificacionViewModel.cargarNotificaciones(username, authToken)

        // Actualizar contador de no leídas
        notificacionViewModel.contarNoLeidas(username, authToken)
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { notificacionViewModel.cargarNotificaciones(username, authToken) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.azulPrimario)
                        )
                    ) {
                        Text("Intentar de nuevo")
                    }
                }
            } else if (notificaciones.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No tienes notificaciones",
                        fontSize = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Las notificaciones aparecerán aquí cuando recibas alertas de actividades",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(notificaciones) { notificacion ->
                        NotificacionItem(
                            notificacion = notificacion,
                            onClick = {
                                // Marcar como leída
                                notificacionViewModel.marcarComoLeida(notificacion._id ?: "", authToken)

                                // Navegar a la pantalla correspondiente según el tipo
                                when {
                                    notificacion.tipo.contains("ACTIVIDAD") && notificacion.entidadId != null -> {
                                        navController.navigate(AppScreen.ActividadDetalleScreen.createRoute(notificacion.entidadId))
                                    }
                                    // Añadir más navegaciones según los tipos de notificación
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificacionItem(
    notificacion: NotificacionDTO,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(notificacion.fechaCreacion)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notificacion.leida)
                Color(0xFFF0F8FF) // Color de fondo azul claro para no leídas
            else
                Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Indicador de no leída
            if (!notificacion.leida) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colorResource(id = R.color.azulPrimario), shape = CircleShape)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Spacer(modifier = Modifier.width(24.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notificacion.titulo,
                    fontWeight = if (!notificacion.leida) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notificacion.mensaje,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}