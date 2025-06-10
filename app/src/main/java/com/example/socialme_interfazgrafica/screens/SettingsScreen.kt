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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.CambiarContrasenaDTO
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

// Objeto para almacenar constantes de SharedPreferences (solo para token, username, etc.)
object PreferenciasUsuario {
    const val SHARED_PREFS_NAME = "UserPrefs"
    const val PREMIUM_KEY = "PREMIUM"
    const val TOKEN_KEY = "TOKEN"
    const val USERNAME_KEY = "USERNAME"
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
    var confirmarEliminacion by remember { mutableStateOf(false) } // NUEVO ESTADO AÑADIDO

    // Estado para verificar si es premium
    var isPremium by remember { mutableStateOf(false) }

    // Estado para controlar la carga inicial
    var configuracionesCargadas by remember { mutableStateOf(false) }

    // Obtener información del usuario
    val sharedPreferences = context.getSharedPreferences(PreferenciasUsuario.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    val token = sharedPreferences.getString(PreferenciasUsuario.TOKEN_KEY, "") ?: ""
    val username = sharedPreferences.getString(PreferenciasUsuario.USERNAME_KEY, "") ?: ""

    // Cargar configuraciones reales del servidor al inicializar
    LaunchedEffect(Unit) {
        if (token.isNotEmpty() && username.isNotEmpty()) {
            scope.launch {
                try {
                    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

                    // Cargar información del usuario (incluyendo premium)
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
                } finally {
                    configuracionesCargadas = true
                }
            }
        } else {
            configuracionesCargadas = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        if (!configuracionesCargadas) {
            // Mostrar indicador de carga mientras se cargan las configuraciones
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = colorResource(R.color.azulPrimario))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando configuraciones...",
                        color = colorResource(R.color.textoSecundario)
                    )
                }
            }
        } else {
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

                        // Sección de Privacidad con dropdowns
                        PrivacidadSection()

                        Divider(color = Color.LightGray, thickness = 0.5.dp)

                        // Cambiar contraseña
                        CambiarContrasenaSection()

                        Divider(color = Color.LightGray, thickness = 0.5.dp)

                        // Opción de Usuarios bloqueados
                        OptionItem(
                            text = "Usuarios bloqueados",
                            onClick = {
                                navController.navigate(AppScreen.UsuariosBloqueadosScreen.route)
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

                OutlinedButton(
                    onClick = {
                        mostrarDialogoConfirmacion = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(R.color.cyanSecundario)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Eliminar cuenta", color = Color.Red)
                }

                if (mostrarDialogoConfirmacion) {
                    AlertDialog(
                        onDismissRequest = {
                            mostrarDialogoConfirmacion = false
                            confirmarEliminacion = false // Resetear checkbox al cerrar
                        },
                        title = {
                            Text(
                                "Eliminar cuenta",
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        },
                        text = {
                            Column {
                                Text(
                                    text = "⚠️ Esta acción eliminará permanentemente tu cuenta",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    text = "Antes de eliminar tu cuenta debes asginar un nuevo creador a cada una de las comunidades que gestiones, de otra manera no será posible",
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    lineHeight = 20.sp
                                )

                                Text(
                                    text = "Esta acción NO se puede deshacer.",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { confirmarEliminacion = !confirmarEliminacion }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = confirmarEliminacion,
                                        onCheckedChange = { confirmarEliminacion = it }
                                    )
                                    Text(
                                        text = "Entiendo que esta acción eliminará mi cuenta de forma permanente",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(start = 8.dp),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    mostrarDialogoConfirmacion = false
                                    confirmarEliminacion = false // Resetear checkbox
                                    // Ejecutar la eliminación de la cuenta
                                    eliminarCuenta(context, navController, viewModel) { error ->
                                        mensajeError = error
                                        mostrarDialogoError = error.isNotEmpty()
                                    }
                                },
                                enabled = confirmarEliminacion // Solo habilitado si el checkbox está marcado
                            ) {
                                Text(
                                    "Eliminar definitivamente",
                                    color = if (confirmarEliminacion) Color.Red else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    mostrarDialogoConfirmacion = false
                                    confirmarEliminacion = false // Resetear checkbox al cancelar
                                }
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
        }

        // Bottom Navigation Bar
        BottomNavBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun CambiarContrasenaSection() {
    var expandedContrasena by remember { mutableStateOf(false) }
    var contrasenaActual by remember { mutableStateOf("") }
    var nuevaContrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }

    // Estados para mostrar/ocultar contraseñas
    var mostrarContrasenaActual by remember { mutableStateOf(false) }
    var mostrarNuevaContrasena by remember { mutableStateOf(false) }
    var mostrarConfirmarContrasena by remember { mutableStateOf(false) }

    // Estados para manejo de errores y carga
    var mensajeError by remember { mutableStateOf("") }
    var cambiandoContrasena by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Obtener datos del usuario
    val sharedPreferences = context.getSharedPreferences(PreferenciasUsuario.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    val token = sharedPreferences.getString(PreferenciasUsuario.TOKEN_KEY, "") ?: ""
    val username = sharedPreferences.getString(PreferenciasUsuario.USERNAME_KEY, "") ?: ""

    // Función para validar los campos
    fun validarCampos(): String? {
        if (contrasenaActual.isBlank()) {
            return "La contraseña actual es obligatoria"
        }
        if (nuevaContrasena.isBlank()) {
            return "La nueva contraseña es obligatoria"
        }
        if (nuevaContrasena.length < 6) {
            return "La nueva contraseña debe tener al menos 6 caracteres"
        }
        if (confirmarContrasena.isBlank()) {
            return "Debes confirmar la nueva contraseña"
        }
        if (nuevaContrasena != confirmarContrasena) {
            return "Las contraseñas nuevas no coinciden"
        }
        if (contrasenaActual == nuevaContrasena) {
            return "La nueva contraseña debe ser diferente a la actual"
        }
        return null
    }

    // Función para cambiar contraseña usando el DTO
    fun cambiarContrasena() {
        val errorValidacion = validarCampos()
        if (errorValidacion != null) {
            mensajeError = errorValidacion
            return
        }

        cambiandoContrasena = true
        mensajeError = ""

        scope.launch {
            try {
                val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

                // Crear el DTO con todos los datos
                val cambiarContrasenaDTO = CambiarContrasenaDTO(
                    username = username,
                    passwordRepeat = confirmarContrasena,
                    passwordActual = contrasenaActual,
                    passwordNueva = nuevaContrasena
                )

                // Enviar todo el DTO al backend
                val response = apiService.cambiarContrasena("Bearer $token", cambiarContrasenaDTO)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Limpiar campos
                        contrasenaActual = ""
                        nuevaContrasena = ""
                        confirmarContrasena = ""
                        expandedContrasena = false
                        Toast.makeText(context, "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: ""
                        mensajeError = try {
                            val jsonObject = JSONObject(errorBody)
                            jsonObject.optString("error", "Error al cambiar la contraseña")
                        } catch (e: Exception) {
                            "Error al cambiar la contraseña"
                        }
                    }
                    cambiandoContrasena = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mensajeError = "Error de conexión: ${e.message}"
                    cambiandoContrasena = false
                }
            }
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandedContrasena = !expandedContrasena }
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cambiar contraseña",
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                color = Color.Black
            )
            Icon(
                imageVector = if (expandedContrasena) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        AnimatedVisibility(
            visible = expandedContrasena,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Campo contraseña actual
                OutlinedTextField(
                    value = contrasenaActual,
                    onValueChange = {
                        contrasenaActual = it
                        mensajeError = ""
                    },
                    label = { Text("Contraseña actual", color = Color.Black) },
                    visualTransformation = if (mostrarContrasenaActual) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { mostrarContrasenaActual = !mostrarContrasenaActual }) {
                            Icon(
                                imageVector = if (mostrarContrasenaActual) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = if (mostrarContrasenaActual) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cambiandoContrasena,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo nueva contraseña
                OutlinedTextField(
                    value = nuevaContrasena,
                    onValueChange = {
                        nuevaContrasena = it
                        mensajeError = ""
                    },
                    label = { Text("Nueva contraseña", color = Color.Black) },
                    visualTransformation = if (mostrarNuevaContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { mostrarNuevaContrasena = !mostrarNuevaContrasena }) {
                            Icon(
                                imageVector = if (mostrarNuevaContrasena) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = if (mostrarNuevaContrasena) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cambiandoContrasena,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo confirmar nueva contraseña
                OutlinedTextField(
                    value = confirmarContrasena,
                    onValueChange = {
                        confirmarContrasena = it
                        mensajeError = ""
                    },
                    label = { Text("Confirmar nueva contraseña", color = Color.Black) },
                    visualTransformation = if (mostrarConfirmarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { mostrarConfirmarContrasena = !mostrarConfirmarContrasena }) {
                            Icon(
                                imageVector = if (mostrarConfirmarContrasena) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = if (mostrarConfirmarContrasena) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cambiandoContrasena,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Gray
                    )
                )

                // Mostrar mensaje de error si existe
                if (mensajeError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mensajeError,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para cambiar contraseña
                Button(
                    onClick = { cambiarContrasena() },
                    enabled = !cambiandoContrasena,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (cambiandoContrasena) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cambiando...")
                    } else {
                        Text("Cambiar contraseña")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// PrivacidadSection que carga valores directamente del servidor
@Composable
fun PrivacidadSection() {
    var expandedPrivacidad by remember { mutableStateOf(false) }
    var expandedComunidades by remember { mutableStateOf(false) }
    var expandedActividades by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPreferences = context.getSharedPreferences(PreferenciasUsuario.SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    // Obtener datos del usuario
    val token = sharedPreferences.getString(PreferenciasUsuario.TOKEN_KEY, "") ?: ""
    val username = sharedPreferences.getString(PreferenciasUsuario.USERNAME_KEY, "") ?: ""

    // Estados para los valores cargados del servidor - CORREGIDO: valores por defecto que coinciden con el backend
    var opcionComunidades by remember { mutableStateOf("Todos") }  // Backend default: "TODOS"
    var opcionActividades by remember { mutableStateOf("Todos") }   // Backend default: "TODOS"
    var cargandoConfiguraciones by remember { mutableStateOf(true) }

    val opcionesDisplay = listOf("Todos", "Amigos", "Nadie")

    // Función para convertir valor interno a display
    fun valorADisplay(valor: String): String {
        return when (valor.uppercase()) {
            "TODOS" -> "Todos"
            "AMIGOS" -> "Amigos"
            "NADIE" -> "Nadie"
            else -> valor
        }
    }

    // Función para convertir valor display a interno
    fun displayAValor(display: String): String {
        return when (display) {
            "Todos" -> "TODOS"
            "Amigos" -> "AMIGOS"
            "Nadie" -> "NADIE"
            else -> display.uppercase()
        }
    }

    // Cargar valores del servidor al inicializar el composable
    LaunchedEffect(Unit) {
        if (token.isNotEmpty() && username.isNotEmpty()) {
            scope.launch {
                try {
                    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                    val authToken = "Bearer $token"

                    // Cargar privacidad de comunidades - CORREGIDO: Leer como texto plano
                    val responseComunidades = apiService.verPrivacidadComunidad(authToken, username)
                    if (responseComunidades.isSuccessful) {
                        val valorComunidades = responseComunidades.body()?.string()?.trim() ?: "TODOS"
                        opcionComunidades = valorADisplay(valorComunidades)
                        println("DEBUG: Privacidad comunidades cargada: $valorComunidades -> ${valorADisplay(valorComunidades)}")
                    } else {
                        println("DEBUG: Error al cargar privacidad comunidades: ${responseComunidades.code()}")
                    }

                    // Cargar privacidad de actividades - CORREGIDO: Leer como texto plano
                    val responseActividades = apiService.verPrivacidadActividad(authToken, username)
                    if (responseActividades.isSuccessful) {
                        val valorActividades = responseActividades.body()?.string()?.trim() ?: "TODOS"
                        opcionActividades = valorADisplay(valorActividades)
                        println("DEBUG: Privacidad actividades cargada: $valorActividades -> ${valorADisplay(valorActividades)}")
                    } else {
                        println("DEBUG: Error al cargar privacidad actividades: ${responseActividades.code()}")
                    }

                } catch (e: Exception) {
                    println("DEBUG: Excepción al cargar configuraciones: ${e.message}")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al cargar configuraciones: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    cargandoConfiguraciones = false
                    println("DEBUG: Carga de configuraciones completada. Comunidades: $opcionComunidades, Actividades: $opcionActividades")
                }
            }
        } else {
            println("DEBUG: Token o username vacío - token: $token, username: $username")
            cargandoConfiguraciones = false
        }
    }

    // Función para cambiar privacidad de comunidades
    fun cambiarPrivacidadComunidades(nuevaOpcion: String) {
        scope.launch {
            try {
                val valorInterno = displayAValor(nuevaOpcion)
                println("DEBUG: Cambiando privacidad comunidades: $nuevaOpcion -> $valorInterno")
                val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                val response = apiService.cambiarPrivacidadComunidad("Bearer $token", username, valorInterno)

                if (response.isSuccessful) {
                    opcionComunidades = nuevaOpcion
                    println("DEBUG: Privacidad comunidades actualizada exitosamente")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Privacidad de comunidades actualizada", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    println("DEBUG: Error al actualizar privacidad comunidades: ${response.code()} - ${response.message()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al actualizar privacidad: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Excepción al cambiar privacidad comunidades: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Función para cambiar privacidad de actividades
    fun cambiarPrivacidadActividades(nuevaOpcion: String) {
        scope.launch {
            try {
                val valorInterno = displayAValor(nuevaOpcion)
                println("DEBUG: Cambiando privacidad actividades: $nuevaOpcion -> $valorInterno")
                val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                val response = apiService.cambiarPrivacidadActividad("Bearer $token", username, valorInterno)

                if (response.isSuccessful) {
                    opcionActividades = nuevaOpcion
                    println("DEBUG: Privacidad actividades actualizada exitosamente")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Privacidad de actividades actualizada", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    println("DEBUG: Error al actualizar privacidad actividades: ${response.code()} - ${response.message()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al actualizar privacidad: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Excepción al cambiar privacidad actividades: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
                fontSize = 16.sp,
                color = Color.Black
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
                if (cargandoConfiguraciones) {
                    // Mostrar indicador de carga
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = colorResource(R.color.azulPrimario)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cargando...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                } else {
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
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Text(
                                text = opcionComunidades,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
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
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .background(
                                    Color.White,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            opcionesDisplay.forEach { opcion ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            opcion,
                                            color = if (opcion == opcionComunidades) Color.Blue else Color.Black,
                                            fontWeight = if (opcion == opcionComunidades) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        expandedComunidades = false
                                        if (opcion != opcionComunidades) {
                                            cambiarPrivacidadComunidades(opcion)
                                        }
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
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Text(
                                text = opcionActividades,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
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
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .background(
                                    Color.White,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            opcionesDisplay.forEach { opcion ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            opcion,
                                            color = if (opcion == opcionActividades) Color.Blue else Color.Black,
                                            fontWeight = if (opcion == opcionActividades) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        expandedActividades = false
                                        if (opcion != opcionActividades) {
                                            cambiarPrivacidadActividades(opcion)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// RadarDistanciaSection que carga valor directamente del servidor
@Composable
fun RadarDistanciaSection() {
    var expandedRadar by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Obtener datos del usuario
    val sharedPreferences = remember {
        context.getSharedPreferences(PreferenciasUsuario.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }
    val token = sharedPreferences.getString(PreferenciasUsuario.TOKEN_KEY, "") ?: ""
    val username = sharedPreferences.getString(PreferenciasUsuario.USERNAME_KEY, "") ?: ""

    // Estados para el radar
    var sliderPosition by remember { mutableFloatStateOf(50f) }  // Valor por defecto
    var valorOriginal by remember { mutableFloatStateOf(50f) }   // Para comparar cambios
    var cargandoRadar by remember { mutableStateOf(true) }

    val distanciaKm = sliderPosition.roundToInt()

    // Estado para controlar si hay cambios pendientes
    var hayCambiosPendientes by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    // Cargar valor del servidor al inicializar el composable
    LaunchedEffect(Unit) {
        if (token.isNotEmpty() && username.isNotEmpty()) {
            scope.launch {
                try {
                    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                    val authToken = "Bearer $token"

                    val response = apiService.verRadarDistancia(authToken, username)
                    if (response.isSuccessful) {
                        val valorRadarString = response.body()?.string()?.trim() ?: "50.0"
                        val valorRadar = valorRadarString.toFloatOrNull() ?: 50f
                        sliderPosition = valorRadar
                        valorOriginal = valorRadar
                        println("DEBUG: Radar cargado: $valorRadarString -> $valorRadar")
                    } else {
                        println("DEBUG: Error al cargar radar: ${response.code()}")
                    }

                } catch (e: Exception) {
                    println("DEBUG: Excepción al cargar radar: ${e.message}")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al cargar configuración del radar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    cargandoRadar = false
                }
            }
        } else {
            cargandoRadar = false
        }
    }

    // Función para guardar en servidor
    fun guardarRadarEnServidor() {
        guardando = true
        scope.launch {
            try {
                val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                val response = apiService.cambiarRadarDistancia("Bearer $token", username, distanciaKm.toString())

                if (response.isSuccessful) {
                    valorOriginal = sliderPosition
                    hayCambiosPendientes = false
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Radar actualizado correctamente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al actualizar radar", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            } finally {
                guardando = false
            }
        }
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
                fontSize = 16.sp,
                color = Color.Black
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
                if (cargandoRadar) {
                    // Mostrar indicador de carga
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = colorResource(R.color.azulPrimario)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cargando...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    Slider(
                        value = sliderPosition,
                        onValueChange = { newValue ->
                            sliderPosition = newValue
                            hayCambiosPendientes = (newValue.roundToInt() != valorOriginal.roundToInt())
                        },
                        valueRange = 10f..100f,
                        steps = 9,  // Crear 9 pasos para tener intervalos de 10 km
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text(
                        text = "$distanciaKm km",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón Guardar (solo visible si hay cambios)
                    if (hayCambiosPendientes) {
                        Button(
                            onClick = { guardarRadarEnServidor() },
                            enabled = !guardando,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (guardando) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Guardando...")
                            } else {
                                Text("Guardar cambios")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
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
                fontSize = 16.sp,
                color = Color.Black
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
                    text = "Las comunidades siguen unas normas de respeto y convivencia basicas, cualquier incidente donde peligre la libertad de los usuarios en tu comunidad podría ser motivo de eiliminación de esta, creando una comunidad afirmas entender y responsabilizarte de las acciones llevadas a cabo en ella, tanto en actividades como por cualquier otro medio que ofrezca la aplicación.\n Con ello también afirmas encargarte activamente de la gestión de la comunidad así como sus actividades y de ofrecer al público un servicio real, sin datos engañosos sobre tu comunidad (intereses, titulo, ubicaciones...) y actividades reales y que se cumplan.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.Black,
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
                    color = Color.Black,
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
                                    navController.navigate("comunidadDetalle/${comunidad.url}")
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
                        fontSize = 14.sp,
                        color = Color.Black
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
            fontSize = 16.sp,
            color = Color.Black
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

private fun eliminarCuenta(context: Context, navController: NavController, viewModel: UserViewModel, onError: (String) -> Unit) {
    val sharedPreferences = context.getSharedPreferences(PreferenciasUsuario.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    val token = sharedPreferences.getString(PreferenciasUsuario.TOKEN_KEY, "") ?: ""
    val username = sharedPreferences.getString(PreferenciasUsuario.USERNAME_KEY, "") ?: ""

    if (token.isBlank() || username.isBlank()) {
        onError("No se pudo identificar tu sesión. Intenta cerrar sesión y volver a iniciar.")
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
            val tokenBearer = "Bearer $token"
            val response = apiService.eliminarUsuario(tokenBearer, username)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    with(sharedPreferences.edit()) {
                        clear()
                        apply()
                    }

                    viewModel.resetLoginState()

                    Toast.makeText(
                        context,
                        "Cuenta eliminada correctamente",
                        Toast.LENGTH_LONG
                    ).show()

                    navController.navigate(AppScreen.InicioSesionScreen.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"

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
            withContext(Dispatchers.Main) {
                onError(ErrorUtils.parseErrorMessage(e.message ?: "Error de conexión"))
            }
        }
    }
}

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