package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.socialme_interfazgrafica.model.MensajeCreateDTO
import com.example.socialme_interfazgrafica.model.MensajeDTO
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatComunidadScreen(
    comunidadUrl: String,
    comunidadNombre: String,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    // Estado para el mensaje a enviar
    var mensajeTexto by remember { mutableStateOf("") }

    // Estado para la lista de mensajes
    var mensajes by remember { mutableStateOf<List<MensajeDTO>>(emptyList()) }

    // Estado para indicar carga INICIAL (solo primera vez)
    var isInitialLoading by remember { mutableStateOf(true) }

    // Estado para errores
    var error by remember { mutableStateOf<String?>(null) }

    // Estado del username actual
    val username = remember { mutableStateOf("") }

    // Estado para el token de autenticación
    val authToken = remember { mutableStateOf("") }

    // Controlador del scroll para la lista de mensajes
    val scrollState = rememberLazyListState()

    // Obtener el username y el token desde SharedPreferences
    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username.value = sharedPreferences.getString("USERNAME", "") ?: ""
        val token = sharedPreferences.getString("TOKEN", "") ?: ""
        authToken.value = "Bearer $token"
    }

    // Función para cargar mensajes (sin mostrar indicador de carga)
    suspend fun cargarMensajes(esInicialLoad: Boolean = false) {
        try {
            if (esInicialLoad) {
                isInitialLoading = true
            }
            error = null

            val response = apiService.obtenerMensajesComunidad(
                token = authToken.value,
                comunidadUrl = comunidadUrl
            )

            if (response.isSuccessful) {
                val nuevosMensajes = response.body() ?: emptyList()

                // Solo hacer scroll si hay mensajes nuevos o es la carga inicial
                if (esInicialLoad || nuevosMensajes.size > mensajes.size) {
                    mensajes = nuevosMensajes
                    // Hacer scroll al último mensaje si hay mensajes
                    if (mensajes.isNotEmpty()) {
                        scrollState.animateScrollToItem(mensajes.size - 1)
                    }
                } else {
                    mensajes = nuevosMensajes
                }
            } else {
                error = "Error al cargar mensajes: ${response.message()}"
            }
        } catch (e: Exception) {
            error = "Error de conexión: ${e.message}"
            Log.e("ChatComunidad", "Error al cargar mensajes", e)
        } finally {
            if (esInicialLoad) {
                isInitialLoading = false
            }
        }
    }

    // Función para enviar mensaje
    fun enviarMensaje() {
        if (mensajeTexto.isBlank()) return

        scope.launch {
            try {
                val mensajeCreateDTO = MensajeCreateDTO(
                    comunidadUrl = comunidadUrl,
                    username = username.value,
                    contenido = mensajeTexto
                )

                val response = apiService.enviarMensaje(
                    token = authToken.value,
                    mensajeCreateDTO = mensajeCreateDTO
                )

                if (response.isSuccessful) {
                    // Limpiar el campo de texto
                    mensajeTexto = ""
                    // Recargar mensajes inmediatamente sin indicador
                    cargarMensajes(esInicialLoad = false)
                } else {
                    Toast.makeText(
                        context,
                        "Error al enviar el mensaje: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("ChatComunidad", "Error al enviar mensaje", e)
            }
        }
    }

    // Cargar mensajes iniciales y configurar actualizaciones periódicas
    LaunchedEffect(comunidadUrl, authToken.value) {
        if (authToken.value.isNotEmpty()) {
            // Carga inicial
            cargarMensajes(esInicialLoad = true)

            // Configurar carga de mensajes cada 5 segundos (sin indicador)
            while (isActive) {
                delay(5000)
                cargarMensajes(esInicialLoad = false)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chat de $comunidadNombre",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
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
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                containerColor = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = mensajeTexto,
                        onValueChange = { mensajeTexto = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Escribe un mensaje...") },
                        singleLine = false,
                        maxLines = 3,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = colorResource(R.color.azulPrimario),
                            unfocusedBorderColor = colorResource(R.color.cyanSecundario),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    IconButton(
                        onClick = { enviarMensaje() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = colorResource(R.color.azulPrimario),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Enviar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                isInitialLoading && mensajes.isEmpty() -> {
                    // Mostrar indicador de carga inicial
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colorResource(R.color.azulPrimario)
                    )
                }
                error != null && mensajes.isEmpty() -> {
                    // Mostrar error
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Error desconocido",
                            color = colorResource(R.color.error),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    cargarMensajes(esInicialLoad = true)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.azulPrimario)
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
                mensajes.isEmpty() -> {
                    // Mostrar mensaje cuando no hay mensajes
                    Text(
                        text = "No hay mensajes aún. ¡Sé el primero en escribir!",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    // Mostrar lista de mensajes
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        state = scrollState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mensajes) { mensaje ->
                            MensajeBurbuja(
                                mensaje = mensaje,
                                esPropio = mensaje.username == username.value
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MensajeBurbuja(mensaje: MensajeDTO, esPropio: Boolean) {
    val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    val fechaFormateada = dateFormat.format(mensaje.fechaEnvio)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (esPropio) Alignment.End else Alignment.Start
    ) {
        if (!esPropio) {
            Text(
                text = "@${mensaje.username}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (esPropio) 8.dp else 0.dp,
                        topEnd = if (esPropio) 0.dp else 8.dp,
                        bottomStart = 8.dp,
                        bottomEnd = 8.dp
                    )
                )
                .background(
                    if (esPropio) colorResource(R.color.azulPrimario)
                    else colorResource(R.color.cyanSecundario).copy(alpha = 0.7f)
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = mensaje.contenido,
                    color = if (esPropio) Color.White else Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = fechaFormateada,
                    fontSize = 10.sp,
                    color = if (esPropio) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    textAlign = TextAlign.End,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}