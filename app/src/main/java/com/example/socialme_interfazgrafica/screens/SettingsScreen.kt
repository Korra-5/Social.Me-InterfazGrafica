package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.socialme_interfazgrafica.model.ComunidadDTO
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
    const val PREMIUM_KEY = "PREMIUM"

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
    val scope = rememberCoroutineScope()

    // Estado para manejar diálogos
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    var mostrarDialogoError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    // Estado para verificar si es premium
    var isPremium by remember { mutableStateOf(false) }

    // Obtener información del usuario
    val sharedPreferences = context.getSharedPreferences(PreferenciasUsuario.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    val token = sharedPreferences.getString(PreferenciasUsuario.TOKEN_KEY, "") ?: ""
    val username = sharedPreferences.getString(PreferenciasUsuario.USERNAME_KEY, "") ?: ""

    // Cargar estado premium del usuario
    LaunchedEffect(Unit) {
        if (token.isNotEmpty() && username.isNotEmpty()) {
            scope.launch {
                try {
                    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                    val response = apiService.verUsuarioPorUsername("Bearer $token", username)
                    if (response.isSuccessful) {
                        isPremium = response.body()?.premium ?: false
                        // Actualizar SharedPreferences con el estado premium
                        with(sharedPreferences.edit()) {
                            putBoolean(PreferenciasUsuario.PREMIUM_KEY, isPremium)
                            apply()
                        }
                    }
                } catch (e: Exception) {
                    // Usar valor de SharedPreferences si falla la consulta
                    isPremium = sharedPreferences.getBoolean(PreferenciasUsuario.PREMIUM_KEY, false)
                }
            }
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

            // Botón para comprar premium (solo si no es premium)
            if (!isPremium) {
                Button(
                    onClick = {
                        navController.navigate(AppScreen.ComprarPremiumScreen.route)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Premium",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hacerse Premium",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComunidadesSection(navController: NavController) {
    var expandedComunidades by remember { mutableStateOf(false) }
    var aceptarTerminos by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Estado para almacenar las comunidades creadas por el usuario
    var comunidadesCreadas by remember { mutableStateOf<List<ComunidadDTO>>(emptyList()) }
    // Estado para mostrar mensaje de error
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // Estado para manejar carga
    var isLoading by remember { mutableStateOf(false) }

    // Obtener datos de SharedPreferences
    val sharedPreferences = context.getSharedPreferences(PreferenciasUsuario.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    val token = sharedPreferences.getString(PreferenciasUsuario.TOKEN_KEY, "") ?: ""
    val username = sharedPreferences.getString(PreferenciasUsuario.USERNAME_KEY, "") ?: ""
    val isPremium = sharedPreferences.getBoolean(PreferenciasUsuario.PREMIUM_KEY, false)

    // Determinar límite de comunidades según estado premium
    val limiteComunidades = if (isPremium) 10 else 3

    // Función para cargar las comunidades creadas por el usuario
    fun cargarComunidadesCreadas() {
        if (token.isBlank() || username.isBlank()) {
            errorMessage = "No se pudo identificar la sesión"
            return
        }

        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                val tokenBearer = "Bearer $token"
                val response = apiService.verComunidadesPorUsuarioCreador(tokenBearer, username)

                withContext(Dispatchers.Main) {
                    isLoading = false
                    if (response.isSuccessful) {
                        comunidadesCreadas = response.body() ?: emptyList()
                    } else {
                        errorMessage = "Error al cargar comunidades: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    errorMessage = "Error de conexión: ${e.message}"
                }
            }
        }
    }

    // Cargar comunidades cuando se expande la sección
    DisposableEffect(expandedComunidades) {
        if (expandedComunidades) {
            cargarComunidadesCreadas()
        }
        onDispose { }
    }

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
                // Texto con información sobre las comunidades
                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Texto que menciona el límite según el estado premium
                Text(
                    text = if (isPremium)
                        "Como usuario Premium, puedes crear hasta 10 comunidades."
                    else
                        "Solo puedes crear un máximo de 3 comunidades por usuario. ¡Hazte Premium para crear hasta 10!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPremium) Color(0xFFFFD700) else colorResource(R.color.cyanSecundario),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Mostrar comunidades creadas por el usuario
                Text(
                    text = "Comunidades que has creado:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (isLoading) {
                    // Mostrar indicador de carga
                    Text(
                        text = "Cargando comunidades...",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else if (errorMessage != null) {
                    // Mostrar mensaje de error
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 14.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else if (comunidadesCreadas.isEmpty()) {
                    // Mostrar mensaje si no hay comunidades
                    Text(
                        text = "No has creado ninguna comunidad todavía.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    // Mostrar lista de comunidades como tags
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        comunidadesCreadas.forEach { comunidad ->
                            // Tag por cada comunidad
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = colorResource(R.color.cyanSecundario).copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.clickable {
                                    // Navegar a la pantalla de detalles de la comunidad
                                    navController.navigate("comunidad/${comunidad.url}")
                                }
                            ) {
                                Text(
                                    text = comunidad.nombre,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Checkbox de aceptar términos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
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

                // Botón Crear comunidad (deshabilitado si alcanza el límite)
                Button(
                    onClick = {
                        navController.navigate(AppScreen.CrearComunidadScreen.route)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.cyanSecundario),
                        disabledContainerColor = Color.Gray
                    ),
                    enabled = aceptarTerminos && comunidadesCreadas.size < limiteComunidades,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear comunidad")
                }

                // Mostrar mensaje si alcanza el límite
                if (comunidadesCreadas.size >= limiteComunidades) {
                    Text(
                        text = if (isPremium)
                            "Has alcanzado el límite máximo de 10 comunidades."
                        else
                            "Has alcanzado el límite de 3 comunidades. ¡Hazte Premium para crear hasta 10!",
                        fontSize = 12.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )

                    if (!isPremium && comunidadesCreadas.size >= 3) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                navController.navigate(AppScreen.ComprarPremiumScreen.route)
                            }
                        ) {
                            Text("Hacerse Premium", color = Color(0xFFFFD700))
                        }
                    }
                }
            }
        }
    }
}

// ... Resto de las funciones existentes (NotificacionesSection, PrivacidadSection, etc.)

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