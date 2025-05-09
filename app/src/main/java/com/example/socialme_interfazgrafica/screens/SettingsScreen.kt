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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.example.socialme_interfazgrafica.viewModel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.roundToInt

// Objeto para almacenar constantes de SharedPreferences
object PreferenciasUsuario {
    const val SHARED_PREFS_NAME = "UserPrefs"
    const val DISTANCIA_KEY = "RADAR_DISTANCIA"

    // Otras claves que podrías necesitar
    const val TOKEN_KEY = "TOKEN"
    const val USERNAME_KEY = "USERNAME"
    const val ROLE_KEY = "ROLE"

    // Función de utilidad para obtener la distancia desde cualquier parte del código
    fun getDistanciaRadar(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getFloat(DISTANCIA_KEY, 50f).roundToInt()
    }
}

@Composable
fun OpcionesScreen(navController: NavController, viewModel: UserViewModel) {
    // Obtener el contexto para acceder a SharedPreferences
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estado para manejar diálogos
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    var mostrarDialogoError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

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

            // Botón de cerrar sesión
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

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de eliminar cuenta
            OutlinedButton(
                onClick = {
                    // Mostrar diálogo de confirmación
                    mostrarDialogoConfirmacion = true
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorResource(R.color.cyanSecundario)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar cuenta", color = Color.Red)
            }

            // Diálogo de confirmación para eliminar cuenta
            if (mostrarDialogoConfirmacion) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoConfirmacion = false },
                    title = { Text("Eliminar cuenta") },
                    text = { Text("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mostrarDialogoConfirmacion = false
                                // Ejecutar la eliminación de la cuenta
                                eliminarCuenta(context, navController, viewModel) { error ->
                                    mensajeError = error
                                    mostrarDialogoError = error.isNotEmpty()
                                }
                            }
                        ) {
                            Text("Eliminar", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { mostrarDialogoConfirmacion = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Diálogo de error para mostrar si la eliminación de cuenta falla
            if (mostrarDialogoError) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoError = false },
                    title = { Text("No se puede eliminar la cuenta") },
                    text = { Text(mensajeError) },
                    confirmButton = {
                        TextButton(
                            onClick = { mostrarDialogoError = false }
                        ) {
                            Text("Entendido")
                        }
                    }
                )
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
    val context = LocalContext.current

    // Cargar el valor guardado en SharedPreferences
    val sharedPreferences = remember {
        context.getSharedPreferences(PreferenciasUsuario.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }
    val valorGuardado = sharedPreferences.getFloat(PreferenciasUsuario.DISTANCIA_KEY, 50f)

    // Inicializar el slider con el valor guardado
    var sliderPosition by remember { mutableFloatStateOf(valorGuardado) }
    val distanciaKm = sliderPosition.roundToInt()

    // Guardar el valor cuando cambia
    DisposableEffect(distanciaKm) {
        with(sharedPreferences.edit()) {
            putFloat(PreferenciasUsuario.DISTANCIA_KEY, sliderPosition)
            apply()
        }

        onDispose { }
    }

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
                    onValueChange = { newValue ->
                        sliderPosition = newValue
                        // Guardar en SharedPreferences cuando cambia el valor
                        with(sharedPreferences.edit()) {
                            putFloat(PreferenciasUsuario.DISTANCIA_KEY, newValue)
                            apply()
                        }
                    },
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
 * Función para eliminar la cuenta del usuario:
 * - Intenta borrar la cuenta del servidor vía API
 * - Maneja posibles errores, especialmente si el usuario es dueño de comunidades
 * - Si tiene éxito, limpia los datos locales y redirige al login
 */
private fun eliminarCuenta(context: Context, navController: NavController, viewModel: UserViewModel, onError: (String) -> Unit) {
    val sharedPreferences = context.getSharedPreferences(PreferenciasUsuario.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    val token = sharedPreferences.getString(PreferenciasUsuario.TOKEN_KEY, "") ?: ""
    val username = sharedPreferences.getString(PreferenciasUsuario.USERNAME_KEY, "") ?: ""

    if (token.isBlank() || username.isBlank()) {
        onError("No se pudo identificar tu sesión. Intenta cerrar sesión y volver a iniciar.")
        return
    }

    // Ejecutar la llamada a la API en un hilo secundario
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
            val tokenBearer = "Bearer $token"
            val response = apiService.eliminarUsuario(tokenBearer, username)

            // Procesar la respuesta en el hilo principal
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    // Éxito: Limpiar datos locales y navegar a login
                    with(sharedPreferences.edit()) {
                        clear()
                        apply()
                    }

                    // Resetear el estado de login
                    viewModel.resetLoginState()

                    // Mostrar mensaje de éxito
                    Toast.makeText(
                        context,
                        "Cuenta eliminada correctamente",
                        Toast.LENGTH_LONG
                    ).show()

                    // Navegar a la pantalla de inicio de sesión
                    navController.navigate(AppScreen.InicioSesionScreen.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                } else {
                    // Error: Mostrar mensaje de error
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"

                    // Intentar extraer mensaje de error del backend
                    try {
                        val jsonObject = JSONObject(errorBody)
                        val errorMessage = jsonObject.optString("error", "")
                        if (errorMessage.isNotEmpty()) {
                            onError(errorMessage)
                        } else {
                            onError(ErrorUtils.parseErrorMessage(errorBody))
                        }
                    } catch (e: Exception) {
                        onError(ErrorUtils.parseErrorMessage(errorBody))
                    }
                }
            }
        } catch (e: Exception) {
            // Manejar excepciones (problemas de red, etc.)
            withContext(Dispatchers.Main) {
                onError(ErrorUtils.parseErrorMessage(e.message ?: "Error de conexión"))
            }
        }
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
    val sharedPreferences = context.getSharedPreferences(PreferenciasUsuario.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
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