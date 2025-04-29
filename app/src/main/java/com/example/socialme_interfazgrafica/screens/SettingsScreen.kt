package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.viewModel.UserViewModel
import kotlin.math.roundToInt

@Composable
fun OpcionesScreen(navController: NavController, viewModel: UserViewModel) {
    // Obtener el contexto para acceder a SharedPreferences
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Opciones",
                color = colorResource(R.color.cyanSecundario),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Card para opciones de usuario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Sección de Notificaciones con checkboxes expandibles
                    NotificacionesSection()

                    Divider(color = Color.LightGray, thickness = 0.5.dp)

                    // Sección de Privacidad con dropdowns
                    PrivacidadSection()

                    Divider(color = Color.LightGray, thickness = 0.5.dp)

                    // Opción de Usuarios bloqueados
                    OptionItem(
                        text = "Usuarios bloqueados",
                        onClick = {
                            // Acción al hacer clic en Usuarios bloqueados
                        }
                    )

                    Divider(color = Color.LightGray, thickness = 0.5.dp)

                    // Sección Sobre las comunidades con texto y checkbox
                    ComunidadesSection(navController)

                    Divider(color = Color.LightGray, thickness = 0.5.dp)

                    // Sección de Radar de distancia con slider
                    RadarDistanciaSection()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de cerrar sesión separado
            Button(
                onClick = {
                    // Función para cerrar sesión
                    cerrarSesion(context, navController, viewModel)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.cyanSecundario)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }

            // Espacio adicional para evitar que el BottomNavBar tape contenido
            Spacer(modifier = Modifier.height(60.dp))
        }

        // Bottom Navigation Bar
        BottomNavBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun NotificacionesSection() {
    var expandedNotifications by remember { mutableStateOf(false) }
    var notificarComunidad by remember { mutableStateOf(true) }
    var notificarSolicitudes by remember { mutableStateOf(true) }
    var notificarActividades by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandedNotifications = !expandedNotifications }
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notificaciones",
                modifier = Modifier.weight(1f),
                fontSize = 16.sp
            )
            Icon(
                imageVector = if (expandedNotifications) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        AnimatedVisibility(
            visible = expandedNotifications,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp)
            ) {
                // Checkbox 1: Notificar nuevas actividades de tu comunidad
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = notificarComunidad,
                        onCheckedChange = { notificarComunidad = it }
                    )
                    Text(
                        text = "Notificar nuevas actividades de tu comunidad",
                        fontSize = 14.sp
                    )
                }

                // Checkbox 2: Notificar solicitudes de amistad
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = notificarSolicitudes,
                        onCheckedChange = { notificarSolicitudes = it }
                    )
                    Text(
                        text = "Notificar solicitudes de amistad",
                        fontSize = 14.sp
                    )
                }

                // Checkbox 3: Notificar actividades cercanas
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = notificarActividades,
                        onCheckedChange = { notificarActividades = it }
                    )
                    Text(
                        text = "Notificar actividades cercanas",
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PrivacidadSection() {
    var expandedPrivacidad by remember { mutableStateOf(false) }
    var expandedComunidades by remember { mutableStateOf(false) }
    var expandedActividades by remember { mutableStateOf(false) }
    var opcionComunidades by remember { mutableStateOf("Amigos") }
    var opcionActividades by remember { mutableStateOf("Todos") }

    val opciones = listOf("Todos", "Amigos", "Nadie")

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandedPrivacidad = !expandedPrivacidad }
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Privacidad",
                modifier = Modifier.weight(1f),
                fontSize = 16.sp
            )
            Icon(
                imageVector = if (expandedPrivacidad) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        AnimatedVisibility(
            visible = expandedPrivacidad,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp)
            ) {
                // Dropdown 1: Quien puede ver tus comunidades
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedComunidades = !expandedComunidades }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quien puede ver tus comunidades:",
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = opcionComunidades,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (expandedComunidades) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedComunidades,
                        onDismissRequest = { expandedComunidades = false },
                        modifier = Modifier.fillMaxWidth(0.5f)
                    ) {
                        opciones.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    opcionComunidades = opcion
                                    expandedComunidades = false
                                }
                            )
                        }
                    }
                }

                // Dropdown 2: Quien puede ver tus actividades
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedActividades = !expandedActividades }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quien puede ver tus actividades:",
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = opcionActividades,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (expandedActividades) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedActividades,
                        onDismissRequest = { expandedActividades = false },
                        modifier = Modifier.fillMaxWidth(0.5f)
                    ) {
                        opciones.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    opcionActividades = opcion
                                    expandedActividades = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ComunidadesSection(navController: NavController) {
    var expandedComunidades by remember { mutableStateOf(false) }
    var aceptarTerminos by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandedComunidades = !expandedComunidades }
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sobre las comunidades",
                modifier = Modifier.weight(1f),
                fontSize = 16.sp
            )
            Icon(
                imageVector = if (expandedComunidades) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        AnimatedVisibility(
            visible = expandedComunidades,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, bottom = 16.dp)
            ) {
                // Texto Lorem Ipsum
                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Checkbox de aceptar términos
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = aceptarTerminos,
                        onCheckedChange = { aceptarTerminos = it }
                    )
                    Text(
                        text = "Acepto los términos y condiciones",
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón Crear comunidad
                Button(
                    onClick = {
                        navController.navigate(AppScreen.CrearComunidadScreen.route)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.cyanSecundario),
                        disabledContainerColor = Color.Gray
                    ),
                    enabled = aceptarTerminos,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Crear comunidad")
                }
            }
        }
    }
}

@Composable
fun RadarDistanciaSection() {
    var expandedRadar by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(50f) }
    val distanciaKm = sliderPosition.roundToInt()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandedRadar = !expandedRadar }
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Radar de distancia",
                modifier = Modifier.weight(1f),
                fontSize = 16.sp
            )
            Icon(
                imageVector = if (expandedRadar) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        AnimatedVisibility(
            visible = expandedRadar,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    valueRange = 10f..100f,
                    steps = 9,  // Crear 9 pasos para tener intervalos de 10 km
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    text = "$distanciaKm km",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun OptionItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

/**
 * Función para cerrar la sesión del usuario:
 * - Limpia los datos de SharedPreferences
 * - Resetea el estado de login en el ViewModel
 * - Muestra un mensaje de confirmación
 * - Navega a la pantalla de inicio de sesión
 */
private fun cerrarSesion(context: Context, navController: NavController, viewModel: UserViewModel) {
    // 1. Limpiar datos de SharedPreferences
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        clear() // Elimina todos los datos almacenados
        apply()
    }

    // 2. Resetear el estado de login en el ViewModel
    viewModel.resetLoginState()

    // 3. Mostrar mensaje de confirmación
    Toast.makeText(
        context,
        "Sesión cerrada correctamente",
        Toast.LENGTH_SHORT
    ).show()

    // 4. Navegar a la pantalla de inicio de sesión
    navController.navigate(AppScreen.InicioSesionScreen.route) {
        // Eliminar todas las pantallas anteriores del back stack
        popUpTo(navController.graph.startDestinationId) { inclusive = true }
    }
}