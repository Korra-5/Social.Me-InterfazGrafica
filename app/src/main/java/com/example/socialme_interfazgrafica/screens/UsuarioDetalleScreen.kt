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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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

    var usuario by remember { mutableStateOf<UsuarioDTO?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val showMenu = remember { mutableStateOf(false) }

    var esAmigo by remember { mutableStateOf(false) }
    var hayPendienteEnviada by remember { mutableStateOf(false) }
    var hayPendienteRecibida by remember { mutableStateOf(false) }
    var solicitudRecibidaId by remember { mutableStateOf<String?>(null) }
    var solicitudEnviadaId by remember { mutableStateOf<String?>(null) }
    var seSolicitoAmistad by remember { mutableStateOf(false) }
    var amigosDelUsuario by remember { mutableStateOf<List<UsuarioDTO>>(emptyList()) }
    var cargandoAmigos by remember { mutableStateOf(false) }

    var privacidadComunidades by remember { mutableStateOf("AMIGOS") }
    var privacidadActividades by remember { mutableStateOf("TODOS") }

    val showReportDialog = remember { mutableStateOf(false) }
    val reportReason = remember { mutableStateOf("") }
    val reportBody = remember { mutableStateOf("") }
    val isReportLoading = remember { mutableStateOf(false) }

    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val currentUsername = sharedPreferences.getString("USERNAME", "") ?: ""
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val authToken = "Bearer $token"

    val isOwnProfile = username == currentUsername
    val utils = FunctionUtils
    val baseUrl = "https://social-me-tfg.onrender.com"

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
                .directory(context.cacheDir.resolve("user_profile_images"))
                .maxSizeBytes(50 * 1024 * 1024)
                .build()
        }
        .okHttpClient(okHttpClient)
        .build()

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

    LaunchedEffect(username) {
        try {
            val response = apiService.verUsuarioPorUsername(authToken, username)
            if (response.isSuccessful) {
                val usuarioData = response.body()
                usuario = usuarioData

                if (usuarioData != null) {
                    privacidadComunidades = usuarioData.privacidadComunidades
                    privacidadActividades = usuarioData.privacidadActividades
                }

                isLoading = false

                cargandoAmigos = true
                try {
                    val amigosResponse = apiService.verAmigos(authToken, username)
                    if (amigosResponse.isSuccessful) {
                        amigosDelUsuario = amigosResponse.body() ?: emptyList()
                        if (!isOwnProfile) {
                            esAmigo = amigosDelUsuario.any { it.username == currentUsername }
                        }
                    } else {
                        Log.e("UsuarioDetalles", "Error al cargar amigos: ${amigosResponse.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("UsuarioDetalles", "Error: ${e.message}")
                } finally {
                    cargandoAmigos = false
                }

                if (!isOwnProfile) {
                    try {
                        val responseEnviada = apiService.verificarSolicitudPendiente(authToken, currentUsername, username)
                        if (responseEnviada.isSuccessful) {
                            hayPendienteEnviada = responseEnviada.body() ?: false
                        }

                        val responseRecibida = apiService.verificarSolicitudPendiente(authToken, username, currentUsername)
                        if (responseRecibida.isSuccessful) {
                            hayPendienteRecibida = responseRecibida.body() ?: false
                        }

                        if (hayPendienteRecibida) {
                            val solicitudesResponse = apiService.verSolicitudesAmistad(authToken, currentUsername)
                            if (solicitudesResponse.isSuccessful) {
                                val solicitudes = solicitudesResponse.body() ?: emptyList()
                                val solicitudDeEsteUsuario = solicitudes.find { it.remitente == username }
                                solicitudRecibidaId = solicitudDeEsteUsuario?._id
                            }
                        }

                        if (hayPendienteEnviada) {
                            val todasSolicitudesResponse = apiService.verSolicitudesAmistad(authToken, username)
                            if (todasSolicitudesResponse.isSuccessful) {
                                val todasSolicitudes = todasSolicitudesResponse.body() ?: emptyList()
                                val solicitudEnviada = todasSolicitudes.find {
                                    it.remitente == currentUsername && it.destinatario == username && !it.aceptada
                                }
                                solicitudEnviadaId = solicitudEnviada?._id
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("UsuarioDetalles", "Error al verificar solicitudes: ${e.message}")
                    }
                }
            } else {
                errorMessage = "Error al cargar el usuario: ${response.code()}"
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = "Error de conexion: ${e.message}"
            isLoading = false
        }
    }

    fun enviarSolicitudAmistad() {
        scope.launch {
            try {
                val solicitudDTO = SolicitudAmistadDTO(
                    _id = "",
                    remitente = currentUsername,
                    destinatario = username
                )

                val response = apiService.enviarSolicitudAmistad(authToken, solicitudDTO)

                if (response.isSuccessful) {
                    val solicitudCreada = response.body()
                    Toast.makeText(context, "Solicitud de amistad enviada", Toast.LENGTH_SHORT).show()
                    seSolicitoAmistad = true
                    hayPendienteEnviada = true
                    solicitudEnviadaId = solicitudCreada?._id
                } else {
                    Toast.makeText(context, "Error al enviar solicitud de amistad", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("UsuarioDetalles", "Error al enviar solicitud: ${e.message}")
            }
        }
    }

    fun cancelarSolicitudAmistad() {
        if (solicitudEnviadaId == null) {
            Toast.makeText(context, "Error: No se encontro la solicitud enviada", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            try {
                val response = apiService.cancelarSolicitudAmistad(authToken, solicitudEnviadaId!!)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Solicitud de amistad cancelada", Toast.LENGTH_SHORT).show()
                    hayPendienteEnviada = false
                    seSolicitoAmistad = false
                    solicitudEnviadaId = null
                } else {
                    Toast.makeText(context, "Error al cancelar solicitud", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("UsuarioDetalles", "Error al cancelar solicitud: ${e.message}")
            }
        }
    }

    fun aceptarSolicitudAmistad() {
        if (solicitudRecibidaId == null) {
            Toast.makeText(context, "Error: No se encontro la solicitud", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            try {
                val response = apiService.aceptarSolicitud(authToken, solicitudRecibidaId!!)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Solicitud de amistad aceptada", Toast.LENGTH_SHORT).show()
                    esAmigo = true
                    hayPendienteRecibida = false
                    solicitudRecibidaId = null

                    cargandoAmigos = true
                    try {
                        val amigosResponse = apiService.verAmigos(authToken, username)
                        if (amigosResponse.isSuccessful) {
                            amigosDelUsuario = amigosResponse.body() ?: emptyList()
                        }
                    } finally {
                        cargandoAmigos = false
                    }
                } else {
                    Toast.makeText(context, "Error al aceptar solicitud", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("UsuarioDetalles", "Error al aceptar solicitud: ${e.message}")
            }
        }
    }

    fun rechazarSolicitudAmistad() {
        if (solicitudRecibidaId == null) {
            Toast.makeText(context, "Error: No se encontro la solicitud", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            try {
                val response = apiService.rechazarSolicitud(authToken, solicitudRecibidaId!!)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Solicitud de amistad rechazada", Toast.LENGTH_SHORT).show()
                    hayPendienteRecibida = false
                    solicitudRecibidaId = null
                } else {
                    Toast.makeText(context, "Error al rechazar solicitud", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("UsuarioDetalles", "Error al rechazar solicitud: ${e.message}")
            }
        }
    }

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

    if (showReportDialog.value) {
        utils.ReportDialog(
            onDismiss = { showReportDialog.value = false },
            onConfirm = { motivo, cuerpo ->
                scope.launch {
                    isReportLoading.value = true
                    try {
                        val denunciaDTO = DenunciaCreateDTO(
                            motivo = motivo,
                            cuerpo = cuerpo,
                            nombreItemDenunciado = username,
                            tipoItemDenunciado = "Usuario",
                            usuarioDenunciante = currentUsername
                        )

                        val response = withContext(Dispatchers.IO) {
                            RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                                .crearDenuncia(authToken, denunciaDTO)
                        }

                        if (response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "Denuncia enviada correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showReportDialog.value = false
                            }
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                            withContext(Dispatchers.Main) {
                                try {
                                    val jsonObject = JSONObject(errorBody)
                                    val errorMsg = jsonObject.optString("error", "")
                                    if (errorMsg.isNotEmpty()) {
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG)
                                            .show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            ErrorUtils.parseErrorMessage(errorBody),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        ErrorUtils.parseErrorMessage(errorBody),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                ErrorUtils.parseErrorMessage(e.message ?: "Error de conexion"),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } finally {
                        isReportLoading.value = false
                    }
                }
            },
            isLoading = isReportLoading.value,
            reportReason = reportReason,
            reportBody = reportBody
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(text = "Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
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

                        if (isOwnProfile) {
                            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
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

                                Text(
                                    text = "${usuario!!.nombre} ${usuario!!.apellido}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.azulPrimario),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "@${usuario!!.username}",
                                    fontSize = 16.sp,
                                    color = colorResource(R.color.textoSecundario),
                                    textAlign = TextAlign.Center
                                )

                                if (!isOwnProfile && !esAmigo) {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    when {
                                        hayPendienteRecibida -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(0.8f),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { aceptarSolicitudAmistad() },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colorResource(R.color.azulPrimario)
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(40.dp),
                                                    shape = RoundedCornerShape(20.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Check,
                                                        contentDescription = "Aceptar",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Aceptar", fontSize = 12.sp)
                                                }

                                                Button(
                                                    onClick = { rechazarSolicitudAmistad() },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colorResource(R.color.error)
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(40.dp),
                                                    shape = RoundedCornerShape(20.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Close,
                                                        contentDescription = "Rechazar",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Rechazar", fontSize = 12.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = "Te ha enviado una solicitud de amistad",
                                                fontSize = 12.sp,
                                                color = colorResource(R.color.textoSecundario),
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        hayPendienteEnviada || seSolicitoAmistad -> {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.fillMaxWidth(0.7f)
                                            ) {
                                                Button(
                                                    onClick = { },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color.Gray.copy(alpha = 0.5f)
                                                    ),
                                                    enabled = false,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(40.dp),
                                                    shape = RoundedCornerShape(20.dp)
                                                ) {
                                                    Text(
                                                        text = "Solicitud pendiente",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Button(
                                                    onClick = { cancelarSolicitudAmistad() },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colorResource(R.color.error).copy(alpha = 0.8f)
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(35.dp),
                                                    shape = RoundedCornerShape(17.dp)
                                                ) {
                                                    Text(
                                                        text = "Cancelar solicitud",
                                                        fontSize = 12.sp,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        else -> {
                                            Button(
                                                onClick = { enviarSolicitudAmistad() },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colorResource(R.color.azulPrimario)
                                                ),
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
                                                        text = "Anadir amigo",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

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

                                if (usuario!!.direccion?.municipio?.isNotEmpty() == true || usuario!!.direccion?.provincia?.isNotEmpty() == true) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_location),
                                            contentDescription = "Ubicacion",
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

                                Spacer(modifier = Modifier.height(24.dp))

                                if (usuario!!.intereses.isNotEmpty()) {
                                    Text(
                                        text = "Intereses",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorResource(R.color.azulPrimario),
                                        modifier = Modifier.align(Alignment.Start)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

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
                                            text = "Sin amigos todavia",
                                            color = colorResource(R.color.textoSecundario),
                                            fontSize = 14.sp
                                        )
                                    }
                                } else {
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

                        item {
                            SeccionPrivacidad(
                                titulo = "Actividades Próximas",
                                privacidad = privacidadActividades,
                                esAmigo = esAmigo,
                                isOwnProfile = isOwnProfile,
                                username = username,
                                navController = navController,
                                obtenerMensajePrivacidad = ::obtenerMensajePrivacidad,
                                tipoContenido = "actividades"
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

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
    val shouldShow = when (privacidad.uppercase()) {
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

        if (shouldShow) {
            when (tipoContenido) {
                "comunidades" -> ComunidadCarouselUsuario(username = username, navController = navController)
                "actividades" -> ActividadCarouselUsuario(username = username, navController = navController)
            }
        } else {
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

@Composable
fun ComunidadCarouselUsuario(username: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var comunidades by remember { mutableStateOf<List<ComunidadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val currentUsername = sharedPreferences.getString("USERNAME", "") ?: ""

    LaunchedEffect(username) {
        isLoading = true
        errorMsg = null

        try {
            val token = sharedPreferences.getString("TOKEN", "") ?: ""

            if (token.isEmpty()) {
                errorMsg = "No se ha encontrado un token de autenticacion"
                isLoading = false
                return@LaunchedEffect
            }

            val authToken = "Bearer $token"
            val response = apiService.verComunidadPorUsuario(authToken, username, currentUsername)

            if (response.isSuccessful) {
                comunidades = response.body() ?: emptyList()
            } else {
                if (response.code() == 500) {
                    comunidades = emptyList()
                } else {
                    errorMsg = when (response.code()) {
                        401 -> "No autorizado. Por favor, inicie sesion nuevamente."
                        403 -> "No tienes permisos para ver las comunidades de este usuario."
                        404 -> "No se encontraron comunidades para este usuario."
                        else -> "Error al cargar comunidades: ${response.message()}"
                    }
                }
            }
        } catch (e: Exception) {
            errorMsg = "Error de conexion: ${e.message ?: "No se pudo conectar al servidor"}"
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    fun cargarComunidades() {
        scope.launch {
            isLoading = true
            errorMsg = null

            try {
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    errorMsg = "No se ha encontrado un token de autenticacion"
                    isLoading = false
                    return@launch
                }

                val authToken = "Bearer $token"
                val response = apiService.verComunidadPorUsuario(authToken, username, currentUsername)

                if (response.isSuccessful) {
                    comunidades = response.body() ?: emptyList()
                } else {
                    if (response.code() == 500) {
                        comunidades = emptyList()
                    } else {
                        errorMsg = when (response.code()) {
                            401 -> "No autorizado. Por favor, inicie sesion nuevamente."
                            403 -> "No tienes permisos para ver las comunidades de este usuario."
                            404 -> "No se encontraron comunidades para este usuario."
                            else -> "Error al cargar comunidades: ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                errorMsg = "Error de conexion: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

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
        errorMsg != null -> {
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
                        text = errorMsg!!,
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

@Composable
fun ActividadCarouselUsuario(username: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var actividades by remember { mutableStateOf<List<ActividadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val currentUsername = sharedPreferences.getString("USERNAME", "") ?: ""

    LaunchedEffect(username) {
        isLoading = true
        errorMsg = null

        try {
            val token = sharedPreferences.getString("TOKEN", "") ?: ""

            if (token.isEmpty()) {
                errorMsg = "No se ha encontrado un token de autenticación"
                isLoading = false
                return@LaunchedEffect
            }

            val authToken = "Bearer $token"
            val response = apiService.verActividadPorUsernameFechaSuperior(authToken, username, currentUsername)

            if (response.isSuccessful) {
                actividades = response.body() ?: emptyList()
            } else {
                errorMsg = when (response.code()) {
                    401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                    403 -> "No tienes permisos para ver las actividades de este usuario."
                    404 -> "No se encontraron actividades para este usuario."
                    500 -> {
                        actividades = emptyList()
                        null
                    }
                    else -> "Error al cargar actividades: ${response.message()}"
                }

                if (response.code() == 500) {
                    actividades = emptyList()
                    errorMsg = null
                }
            }
        } catch (e: Exception) {
            errorMsg = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    fun cargarActividades() {
        scope.launch {
            isLoading = true
            errorMsg = null

            try {
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    errorMsg = "No se ha encontrado un token de autenticación"
                    isLoading = false
                    return@launch
                }

                val authToken = "Bearer $token"
                val response = apiService.verActividadPorUsernameFechaSuperior(authToken, username, currentUsername)

                if (response.isSuccessful) {
                    actividades = response.body() ?: emptyList()
                } else {
                    errorMsg = when (response.code()) {
                        401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                        403 -> "No tienes permisos para ver las actividades de este usuario."
                        404 -> "No se encontraron actividades para este usuario."
                        500 -> {
                            actividades = emptyList()
                            null
                        }
                        else -> "Error al cargar actividades: ${response.message()}"
                    }

                    if (response.code() == 500) {
                        actividades = emptyList()
                        errorMsg = null
                    }
                }
            } catch (e: Exception) {
                errorMsg = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

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
        errorMsg != null -> {
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
                        text = errorMsg!!,
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
                    text = "No participa en ninguna actividad próxima",
                    color = colorResource(R.color.textoSecundario),
                    textAlign = TextAlign.Center
                )
            }
        }
        else -> {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = actividades.sortedBy { it.fechaInicio },
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

            Text(
                text = comunidad.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "@${comunidad.url}",
                fontSize = 12.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comunidad.descripcion,
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

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

            Text(
                text = actividad.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Por: @${actividad.creador}",
                fontSize = 12.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = actividad.descripcion,
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

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