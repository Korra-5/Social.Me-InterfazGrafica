package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.BloqueoDTO
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.DenunciaCreateDTO
import com.example.socialme_interfazgrafica.model.SolicitudAmistadDTO
import com.example.socialme_interfazgrafica.model.UsuarioDTO
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.example.socialme_interfazgrafica.utils.FunctionUtils
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

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

    // Estado para el menú desplegable
    val showMenu = remember { mutableStateOf(false) }

    // Estados para la relación de amistad
    var esAmigo by remember { mutableStateOf(false) }
    var hayPendiente by remember { mutableStateOf(false) }
    var seSolicitoAmistad by remember { mutableStateOf(false) }
    var amigosDelUsuario by remember { mutableStateOf<List<UsuarioDTO>>(emptyList()) }
    var cargandoAmigos by remember { mutableStateOf(false) }

    // Estados para las preferencias de privacidad del usuario del perfil - CORREGIDO
    var privacidadComunidades by remember { mutableStateOf("AMIGOS") }
    var privacidadActividades by remember { mutableStateOf("TODOS") }

    // Estados para los diálogos de denuncia
    val showReportDialog = remember { mutableStateOf(false) }
    val reportReason = remember { mutableStateOf("") }
    val reportBody = remember { mutableStateOf("") }
    val isReportLoading = remember { mutableStateOf(false) }

    // Obtener el username del usuario actual desde SharedPreferences
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val currentUsername = sharedPreferences.getString("USERNAME", "") ?: ""
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val authToken = "Bearer $token"

    // Verificar si el perfil que se está viendo pertenece al usuario logueado
    val isOwnProfile = username == currentUsername

    val utils = FunctionUtils

    // Base URL para las imágenes
    val baseUrl = "https://social-me-tfg.onrender.com"

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

    // Función para obtener mensaje de privacidad apropiado
    fun obtenerMensajePrivacidad(privacidad: String, seccion: String): String {
        return when (privacidad.uppercase()) {
            "AMIGOS" -> if (esAmigo) {
                "Error al cargar $seccion"
            } else {
                "Este usuario solo comparte sus $seccion con sus amigos"
            }
            "NADIE" -> "Este usuario ha decidido mantener sus $seccion privadas"
            else -> "No se pueden mostrar las $seccion"
        }
    }

    // Función para enviar solicitud de amistad
    fun enviarSolicitudAmistad() {
        scope.launch {
            try {
                val solicitudDTO = SolicitudAmistadDTO(
                    _id = "", // se generará en el servidor
                    remitente = currentUsername,
                    destinatario = username
                )

                val response = apiService.enviarSolicitudAmistad(authToken, solicitudDTO)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Solicitud de amistad enviada", Toast.LENGTH_SHORT).show()
                    seSolicitoAmistad = true
                    hayPendiente = true
                } else {
                    Toast.makeText(context, "Error al enviar solicitud de amistad", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("UsuarioDetalles", "Error al enviar solicitud: ${e.message}")
            }
        }
    }

    // Función para bloquear usuario
    fun bloquearUsuario() {
        scope.launch {
            try {
                val bloqueoDTO = BloqueoDTO(
                    bloqueador = currentUsername,
                    bloqueado = username
                )

                val response = apiService.bloquearUsuario(authToken, bloqueoDTO)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Usuario bloqueado", Toast.LENGTH_SHORT).show()
                    // Volver atrás después de bloquear
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Error al bloquear usuario", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("UsuarioDetalles", "Error al bloquear: ${e.message}")
            }
        }
    }

    // Función para cargar los amigos del usuario
    fun cargarAmigos() {
        cargandoAmigos = true
        scope.launch {
            try {
                val response = apiService.verAmigos(authToken, username)

                if (response.isSuccessful) {
                    amigosDelUsuario = response.body() ?: emptyList()
                    // Verificar si el usuario actual está entre los amigos
                    if (!isOwnProfile) {
                        esAmigo = amigosDelUsuario.any { it.username == currentUsername }
                    }
                } else {
                    Log.e("UsuarioDetalles", "Error al cargar amigos: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("UsuarioDetalles", "Error: ${e.message}")
            } finally {
                cargandoAmigos = false
            }
        }
    }

    // Cargar los datos del usuario y relaciones - CORREGIDO
    LaunchedEffect(username) {
        scope.launch {
            try {
                val response = apiService.verUsuarioPorUsername(authToken, username)
                if (response.isSuccessful) {
                    val usuarioData = response.body()
                    usuario = usuarioData

                    // IMPORTANTE: Obtener las preferencias de privacidad del usuario real
                    if (usuarioData != null) {
                        privacidadComunidades = usuarioData.privacidadComunidades
                        privacidadActividades = usuarioData.privacidadActividades
                    }

                    isLoading = false

                    // Cargar información adicional
                    cargarAmigos()
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

    // Diálogo de denuncia
    if (showReportDialog.value) {
        utils.ReportDialog(
            onDismiss = { showReportDialog.value = false },
            onConfirm = { motivo, cuerpo ->
                // Crear denuncia
                // ... código existente para denuncias ...
            },
            isLoading = isReportLoading.value,
            reportReason = reportReason,
            reportBody = reportBody
        )
    }

    // UI para mostrar los datos del usuario
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra superior con botón de retroceso y botón de opciones
            TopAppBar(
                title = { Text(text = "Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Botón de tres puntos (menú) - siempre visible
                    IconButton(onClick = { showMenu.value = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu.value,
                        onDismissRequest = { showMenu.value = false },
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        offset = DpOffset(x = 0.dp, y = 0.dp),
                        properties = PopupProperties(
                            focusable = true,
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true
                        )
                    ) {

                        // Solo mostrar opción de bloquear si no es el perfil propio
                        if (!isOwnProfile) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_lock),
                                            contentDescription = "Bloquear",
                                            tint = colorResource(R.color.error),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Bloquear usuario",
                                            color = Color.Black
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu.value = false
                                    bloquearUsuario()
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_description),
                                            contentDescription = "Reportar",
                                            tint = colorResource(R.color.error),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Reportar usuario",
                                            color = Color.Black
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu.value = false
                                    showReportDialog.value = true
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Si es el perfil propio, añadir opción para modificar
                        if (isOwnProfile) {
                            Divider(color = Color.LightGray, thickness = 0.5.dp)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_user),
                                            contentDescription = "Modificar",
                                            tint = colorResource(R.color.azulPrimario),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Modificar usuario",
                                            color = Color.Black
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu.value = false
                                    navController.navigate(AppScreen.ModificarUsuarioScreen.createRoute(username))
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.azulPrimario),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
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
                                    if (usuario!!.fotoPerfilId!!.isNotEmpty()) {
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

                                // Botón de solicitud de amistad (solo si no es el propio usuario y no son amigos)
                                if (!isOwnProfile && !esAmigo) {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = { enviarSolicitudAmistad() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorResource(R.color.azulPrimario),
                                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                                        ),
                                        enabled = !hayPendiente && !seSolicitoAmistad,
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .height(40.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 2.dp,
                                            pressedElevation = 4.dp
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_add),
                                                contentDescription = "Enviar solicitud",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (hayPendiente || seSolicitoAmistad) "Solicitud pendiente" else "Añadir amigo",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

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
                                if (usuario!!.direccion?.municipio?.isNotEmpty() == true || usuario!!.direccion?.provincia?.isNotEmpty() == true) {
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
                                        val direccionText = when {
                                            usuario!!.direccion?.municipio?.isNotEmpty() == true && usuario!!.direccion?.provincia?.isNotEmpty() == true ->
                                                "${usuario!!.direccion!!.municipio}, ${usuario!!.direccion!!.provincia}"
                                            usuario!!.direccion?.municipio?.isNotEmpty() == true -> usuario!!.direccion!!.municipio
                                            else -> usuario!!.direccion?.provincia ?: ""
                                        }
                                        Text(
                                            text = direccionText,
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

                        // Nueva sección: Amigos
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Amigos",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.azulPrimario),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                if (cargandoAmigos) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = colorResource(R.color.azulPrimario))
                                    }
                                } else if (amigosDelUsuario.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Sin amigos todavía",
                                            color = colorResource(R.color.textoSecundario),
                                            fontSize = 14.sp
                                        )
                                    }
                                } else {
                                    // Mostrar lista de amigos horizontal
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        items(
                                            items = amigosDelUsuario,
                                            key = { it.username }
                                        ) { amigo ->
                                            AmigoItem(
                                                amigo = amigo,
                                                onClick = { navController.navigate(AppScreen.UsuarioDetalleScreen.createRoute(amigo.username)) },
                                                imageLoader = imageLoader,
                                                authToken = authToken
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Sección de Comunidades con privacidad - CORREGIDO
                        item {
                            SeccionPrivacidad(
                                titulo = "Comunidades",
                                privacidad = privacidadComunidades,
                                esAmigo = esAmigo,
                                isOwnProfile = isOwnProfile,
                                username = username,
                                navController = navController,
                                obtenerMensajePrivacidad = ::obtenerMensajePrivacidad,
                                tipoContenido = "comunidades"
                            )
                        }

                        // Sección de Actividades con privacidad - CORREGIDO
                        item {
                            SeccionPrivacidad(
                                titulo = "Actividades",
                                privacidad = privacidadActividades,
                                esAmigo = esAmigo,
                                isOwnProfile = isOwnProfile,
                                username = username,
                                navController = navController,
                                obtenerMensajePrivacidad = ::obtenerMensajePrivacidad,
                                tipoContenido = "actividades"
                            )
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

// Composable para manejar secciones de privacidad
@Composable
fun SeccionPrivacidad(
    titulo: String,
    privacidad: String,
    esAmigo: Boolean,
    isOwnProfile: Boolean,
    username: String,
    navController: NavController,
    obtenerMensajePrivacidad: (String, String) -> String,
    tipoContenido: String
) {
    val deberíaMostrar = when (privacidad.uppercase()) {
        "TODOS" -> true
        "AMIGOS" -> isOwnProfile || esAmigo
        "NADIE" -> isOwnProfile
        else -> false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = titulo,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (deberíaMostrar) {
            // Mostrar el contenido correspondiente
            when (tipoContenido) {
                "comunidades" -> ComunidadCarouselUsuario(username = username, navController = navController)
                "actividades" -> ActividadCarouselUsuario(username = username, navController = navController)
            }
        } else {
            // Mostrar mensaje de privacidad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Contenido privado",
                    tint = colorResource(R.color.textoSecundario),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = obtenerMensajePrivacidad(privacidad, tipoContenido),
                    color = colorResource(R.color.textoSecundario),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ComunidadCarousel CORREGIDO para UsuarioDetalleScreen
@Composable
fun ComunidadCarouselUsuario(username: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var comunidades by remember { mutableStateOf<List<ComunidadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Obtener el username del usuario actual desde SharedPreferences
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val currentUsername = sharedPreferences.getString("USERNAME", "") ?: ""

    // Función para cargar las comunidades
    fun cargarComunidades() {
        scope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Obtener el token desde SharedPreferences
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                // CORREGIDO: Pasar correctamente el usuario solicitante
                val authToken = "Bearer $token"
                val response = apiService.verComunidadPorUsuario(authToken, username, currentUsername)

                if (response.isSuccessful) {
                    comunidades = response.body() ?: emptyList()
                } else {
                    if (response.code() == 500) {
                        comunidades = emptyList()
                    } else {
                        errorMessage = when (response.code()) {
                            401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                            403 -> "No tienes permisos para ver las comunidades de este usuario."
                            404 -> "No se encontraron comunidades para este usuario."
                            else -> "Error al cargar comunidades: ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Cargar comunidades cuando se inicializa el componente
    LaunchedEffect(username) {
        cargarComunidades()
    }

    // Mostrar estado de carga, error o el carrusel
    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
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
                    .height(160.dp)
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
                    .height(160.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No pertenece a ninguna comunidad",
                    color = colorResource(R.color.textoSecundario),
                    textAlign = TextAlign.Center
                )
            }
        }
        else -> {
            // Carrusel de comunidades optimizado
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = comunidades,
                    key = { it.url }
                ) { comunidad ->
                    ComunidadCardDetalle(comunidad = comunidad, navController = navController)
                }
            }
        }
    }
}

// ActividadCarousel CORREGIDO para UsuarioDetalleScreen
@Composable
fun ActividadCarouselUsuario(username: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var actividades by remember { mutableStateOf<List<ActividadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Obtener el username del usuario actual desde SharedPreferences
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val currentUsername = sharedPreferences.getString("USERNAME", "") ?: ""

    // Función para cargar las actividades
    fun cargarActividades() {
        scope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Obtener el token desde SharedPreferences
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    errorMessage = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                // CORREGIDO: Pasar correctamente el usuario solicitante
                val authToken = "Bearer $token"
                val response = apiService.verActividadPorUsername(authToken, username, currentUsername)

                if (response.isSuccessful) {
                    actividades = response.body() ?: emptyList()
                } else {
                    if (response.code() == 500) {
                        actividades = emptyList()
                    } else {
                        errorMessage = when (response.code()) {
                            401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                            403 -> "No tienes permisos para ver las actividades de este usuario."
                            404 -> "No se encontraron actividades para este usuario."
                            else -> "Error al cargar actividades: ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Cargar actividades cuando se inicializa el componente
    LaunchedEffect(username) {
        cargarActividades()
    }

    // Mostrar estado de carga, error o el carrusel
    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
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
                    .height(160.dp)
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
                        onClick = { cargarActividades() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.azulPrimario)
                        )
                    ) {
                        Text("Intentar de nuevo")
                    }
                }
            }
        }
        actividades.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No participa en ninguna actividad",
                    color = colorResource(R.color.textoSecundario),
                    textAlign = TextAlign.Center
                )
            }
        }
        else -> {
            // Carrusel de actividades optimizado
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = actividades,
                    key = { it.nombre }
                ) { actividad ->
                    ActividadCardDetalle(actividad = actividad, navController = navController)
                }
            }
        }
    }
}

@Composable
fun AmigoItem(
    amigo: UsuarioDTO,
    onClick: () -> Unit,
    imageLoader: ImageLoader,
    authToken: String
) {
    val baseUrl = "https://social-me-tfg.onrender.com"
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        // Foto de perfil
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.cyanSecundario)),
            contentAlignment = Alignment.Center
        ) {
            if (amigo.fotoPerfilId!!.isNotEmpty()) {
                val fotoPerfilUrl = "$baseUrl/files/download/${amigo.fotoPerfilId}"
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
                    contentDescription = "Foto de ${amigo.username}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    imageLoader = imageLoader
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "Usuario",
                    tint = colorResource(R.color.azulPrimario),
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Nombre de usuario
        Text(
            text = amigo.nombre,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.azulPrimario),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "@${amigo.username}",
            fontSize = 10.sp,
            color = colorResource(R.color.textoSecundario),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Cards específicas para el detalle de usuario
@Composable
fun ComunidadCardDetalle(comunidad: ComunidadDTO, navController: NavController) {
    val context = LocalContext.current
    val baseUrl = "https://social-me-tfg.onrender.com"

    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val authToken = "Bearer $token"

    val fotoPerfilUrl = if (comunidad.fotoPerfilId.isNotEmpty())
        "$baseUrl/files/download/${comunidad.fotoPerfilId}"
    else ""

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

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
                .directory(context.cacheDir.resolve("comunidad_images"))
                .maxSizeBytes(50 * 1024 * 1024)
                .build()
        }
        .okHttpClient(okHttpClient)
        .build()

    Card(
        modifier = Modifier
            .width(180.dp)
            .height(220.dp)
            .clickable {
                navController.navigate("comunidadDetalle/${comunidad.url}")
            },
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = colorResource(R.color.white)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto de perfil
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.cyanSecundario)),
                contentAlignment = Alignment.Center
            ) {
                if (fotoPerfilUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(fotoPerfilUrl)
                            .crossfade(true)
                            .size(128, 128)
                            .placeholder(R.drawable.app_icon)
                            .error(R.drawable.app_icon)
                            .setHeader("Authorization", authToken)
                            .memoryCacheKey(fotoPerfilUrl)
                            .diskCacheKey(fotoPerfilUrl)
                            .build(),
                        contentDescription = "Foto de ${comunidad.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        imageLoader = imageLoader
                    )
                } else {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "Perfil por defecto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre de la comunidad
            Text(
                text = comunidad.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // URL
            Text(
                text = "@${comunidad.url}",
                fontSize = 12.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Descripción
            Text(
                text = comunidad.descripcion,
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tags/Intereses
            if (comunidad.intereses.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tagsToShow = if (comunidad.intereses.size > 2) 2 else comunidad.intereses.size

                    items(comunidad.intereses.take(tagsToShow)) { interes ->
                        Badge(
                            containerColor = colorResource(R.color.cyanSecundario)
                        ) {
                            Text(
                                text = interes,
                                color = colorResource(R.color.azulPrimario),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (comunidad.intereses.size > 2) {
                        item {
                            Badge(
                                containerColor = colorResource(R.color.cyanSecundario)
                            ) {
                                Text(
                                    text = "+${comunidad.intereses.size - 2}",
                                    color = colorResource(R.color.azulPrimario),
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Etiquetas privada/global
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
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Privada",
                        fontSize = 10.sp,
                        color = colorResource(R.color.textoSecundario)
                    )
                }

                if (comunidad.comunidadGlobal) {
                    if (comunidad.privada) {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_user),
                        contentDescription = "Comunidad global",
                        tint = colorResource(R.color.textoSecundario),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Global",
                        fontSize = 10.sp,
                        color = colorResource(R.color.textoSecundario)
                    )
                }
            }
        }
    }
}

@Composable
fun ActividadCardDetalle(actividad: ActividadDTO, navController: NavController) {
    val context = LocalContext.current
    val baseUrl = "https://social-me-tfg.onrender.com"

    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val authToken = "Bearer $token"

    val tieneImagenes = actividad.fotosCarruselIds.isNotEmpty()
    val imagenUrl = if (tieneImagenes)
        "$baseUrl/files/download/${actividad.fotosCarruselIds[0]}"
    else ""

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

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
                .directory(context.cacheDir.resolve("actividad_images"))
                .maxSizeBytes(50 * 1024 * 1024)
                .build()
        }
        .okHttpClient(okHttpClient)
        .build()

    val fechaInicio = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(actividad.fechaInicio)
    val fechaFinalizacion = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(actividad.fechaFinalizacion)

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(250.dp)
            .clickable {
                navController.navigate("actividadDetalle/${actividad._id}")
            },
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = colorResource(R.color.white)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de actividad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorResource(R.color.cyanSecundario)),
                contentAlignment = Alignment.Center
            ) {
                if (tieneImagenes) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imagenUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.app_icon)
                            .error(R.drawable.app_icon)
                            .setHeader("Authorization", authToken)
                            .memoryCacheKey("actividad_${actividad.fotosCarruselIds[0]}")
                            .diskCacheKey("actividad_${actividad.fotosCarruselIds[0]}")
                            .build(),
                        contentDescription = "Foto de ${actividad.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        imageLoader = imageLoader
                    )
                } else {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "Imagen por defecto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre de la actividad
            Text(
                text = actividad.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Creador
            Text(
                text = "Por: @${actividad.creador}",
                fontSize = 12.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Descripción
            Text(
                text = actividad.descripcion,
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fechas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = "Fechas",
                    tint = colorResource(R.color.textoSecundario),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$fechaInicio - $fechaFinalizacion",
                    fontSize = 10.sp,
                    color = colorResource(R.color.textoSecundario)
                )
            }
        }
    }
}