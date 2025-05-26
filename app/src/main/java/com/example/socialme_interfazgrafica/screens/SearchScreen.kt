package com.example.socialme_interfazgrafica.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.ParticipantesComunidadDTO
import com.example.socialme_interfazgrafica.model.UsuarioDTO
import com.example.socialme_interfazgrafica.navigation.AppScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class SearchType {
    ACTIVITIES,
    COMMUNITIES,
    USERS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusquedaScreen(
    navController: NavController,
) {
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var actividades by remember { mutableStateOf<List<ActividadDTO>>(emptyList()) }
    var comunidades by remember { mutableStateOf<List<ComunidadDTO>>(emptyList()) }
    var usuarios by remember { mutableStateOf<List<UsuarioDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }
    var urlcomunidad by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("TOKEN", "") ?: ""
    val username = sharedPreferences.getString("USERNAME", "") ?: ""
    val token = "Bearer $authToken"

    val tabs = listOf(
        SearchType.ACTIVITIES,
        SearchType.COMMUNITIES,
        SearchType.USERS
    )

    // Función para cargar datos según el tipo seleccionado
    fun loadData(searchType: SearchType) {
        coroutineScope.launch {
            isLoading = true
            try {
                when (searchType) {
                    SearchType.ACTIVITIES -> {
                        val response = retrofitService.verTodasActividadesPublicas(token)
                        if (response.isSuccessful) {
                            actividades = response.body() ?: emptyList()
                        }
                    }

                    SearchType.COMMUNITIES -> {
                        val response = retrofitService.verTodasComunidadesPublicas(token)
                        if (response.isSuccessful) {
                            comunidades = response.body() ?: emptyList()
                        }
                    }

                    SearchType.USERS -> {
                        val response = retrofitService.verTodosLosUsuarios(token = token, username = username)
                        if (response.isSuccessful) {
                            usuarios = response.body() ?: emptyList()
                        }
                    }
                }
            } catch (e: Exception) {
                // Manejar errores
                errorMessage = "Error al cargar datos: ${e.message}"
                showError = true
            } finally {
                isLoading = false
            }
        }
    }

    // Diálogo para introducir código de unión
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = {
                Text(
                    "Unirse a comunidad",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = colorResource(R.color.cyanSecundario),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Primer campo - URL de la comunidad
                        Column {
                            Text(
                                "Inserte url de la comunidad",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = urlcomunidad,
                                onValueChange = { urlcomunidad = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = colorResource(R.color.cyanSecundario),
                                    unfocusedBorderColor = colorResource(R.color.cyanSecundario)
                                ),
                                singleLine = true
                            )
                        }

                        // Segundo campo - Código de unión
                        Column {
                            Text(
                                "Inserte código de unión a la comunidad",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = joinCode,
                                onValueChange = { joinCode = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = colorResource(R.color.cyanSecundario),
                                    unfocusedBorderColor = colorResource(R.color.cyanSecundario)
                                ),
                                singleLine = true
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // CORREGIDO: Lógica para unirse con el código
                        coroutineScope.launch {
                            try {
                                // Cerrar el diálogo de unión
                                showJoinDialog = false

                                // Mostrar un indicador de carga mientras se procesa la solicitud
                                isLoading = true

                                // Creamos el objeto ParticipantesComunidadDTO
                                val participante = ParticipantesComunidadDTO(
                                    username = username,
                                    comunidad = urlcomunidad
                                )

                                val response = retrofitService.unirseComunidadPorCodigo(
                                    participantesComunidadDTO = participante,
                                    codigo = joinCode,
                                    token = token
                                )

                                // Ocultar indicador de carga
                                isLoading = false

                                if (response.isSuccessful) {
                                    // CORREGIDO: Si la unión fue exitosa, la respuesta es ParticipantesComunidadDTO
                                    val responseBody = response.body()
                                    if (responseBody != null) {
                                        // La unión fue exitosa, navegar a la comunidad
                                        val comunidadResponse = retrofitService.verComunidadPorUrl(token, urlcomunidad)
                                        if (comunidadResponse.isSuccessful) {
                                            val comunidad = comunidadResponse.body()
                                            if (comunidad != null) {
                                                joinCode = ""
                                                urlcomunidad = ""
                                                // Navegamos a la pantalla de detalle de la comunidad
                                                navController.navigate("comunidadDetalle/${comunidad.url}")
                                            } else {
                                                // Error al obtener detalles de la comunidad
                                                errorMessage = "No se pudo obtener la información de la comunidad"
                                                showError = true
                                            }
                                        } else {
                                            // Error en la respuesta del endpoint
                                            errorMessage = when (comunidadResponse.code()) {
                                                404 -> "Comunidad no encontrada"
                                                403 -> "No tienes permisos para acceder a esta comunidad"
                                                else -> "Error al obtener la comunidad: ${comunidadResponse.message()}"
                                            }
                                            showError = true
                                        }
                                    } else {
                                        // El cuerpo de la respuesta es nulo
                                        errorMessage = "Error inesperado al unirse a la comunidad"
                                        showError = true
                                    }
                                } else {
                                    // CORREGIDO: Manejo de errores de la API
                                    errorMessage = when (response.code()) {
                                        400 -> "Datos incorrectos. Verifica la URL y el código"
                                        404 -> "Comunidad no encontrada o código incorrecto"
                                        403 -> "No tienes permisos para unirte a esta comunidad"
                                        409 -> "Ya estás unido a esta comunidad"
                                        else -> "Error al unirse a la comunidad: ${response.message()}"
                                    }
                                    showError = true
                                }
                            } catch (e: Exception) {
                                // Error de conexión o excepción
                                isLoading = false
                                errorMessage = "Error de conexión: ${e.message}"
                                showError = true
                            } finally {
                                // Limpiar campos
                                joinCode = ""
                                urlcomunidad = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.cyanSecundario)
                    )
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showJoinDialog = false
                        joinCode = ""
                        urlcomunidad = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Mostrar mensaje de error si es necesario
    if (showError && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = {
                Text(
                    "Error",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = errorMessage ?: "Ha ocurrido un error desconocido",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { showError = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.cyanSecundario)
                    )
                ) {
                    Text("Aceptar")
                }
            },
            containerColor = Color.White
        )
    }

    // Cargar datos iniciales
    LaunchedEffect(selectedTabIndex) {
        loadData(tabs[selectedTabIndex])
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
        ) {
            Text(
                text = "Búsqueda",
                color = colorResource(R.color.cyanSecundario),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = colorResource(R.color.cyanSecundario)
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(R.color.cyanSecundario),
                    unfocusedBorderColor = colorResource(R.color.cyanSecundario)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de filtro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, tab ->
                    Button(
                        onClick = { selectedTabIndex = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTabIndex == index)
                                colorResource(R.color.cyanSecundario)
                            else
                                colorResource(R.color.cyanSecundario).copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = when (tab) {
                                SearchType.ACTIVITIES -> "Actividades"
                                SearchType.COMMUNITIES -> "Comunidades"
                                SearchType.USERS -> "Usuarios"
                            },
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Área de resultados
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(R.color.cyanSecundario))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (tabs[selectedTabIndex]) {
                        SearchType.ACTIVITIES -> {
                            val filteredActivities = if (searchQuery.isEmpty()) {
                                actividades
                            } else {
                                actividades.filter {
                                    it.nombre.contains(searchQuery, ignoreCase = true) ||
                                            it.descripcion.contains(searchQuery, ignoreCase = true)
                                }
                            }
                            items(filteredActivities) { actividad ->
                                ActividadItem(
                                    actividad = actividad,
                                    authToken = authToken,
                                    navController = navController
                                )
                            }
                        }

                        SearchType.COMMUNITIES -> {
                            val filteredCommunities = if (searchQuery.isEmpty()) {
                                comunidades
                            } else {
                                comunidades.filter {
                                    it.nombre.contains(searchQuery, ignoreCase = true) ||
                                            it.descripcion.contains(searchQuery, ignoreCase = true)
                                }
                            }
                            items(filteredCommunities) { comunidad ->
                                ComunidadItem(
                                    comunidad = comunidad,
                                    authToken = authToken,
                                    navController = navController
                                )
                            }
                        }

                        SearchType.USERS -> {
                            val filteredUsers = if (searchQuery.isEmpty()) {
                                usuarios
                            } else {
                                usuarios.filter {
                                    it.nombre.contains(searchQuery, ignoreCase = true) ||
                                            it.apellido.contains(searchQuery, ignoreCase = true) ||
                                            it.username.contains(searchQuery, ignoreCase = true)
                                }
                            }
                            items(filteredUsers) { usuario ->
                                UsuarioSearchItem(
                                    usuario = usuario,
                                    authToken = authToken,
                                    navController = navController
                                )
                            }
                        }
                    }
                }

                // Bottom Navigation Bar
                BottomNavBar(
                    navController = navController,
                )
            }
        }

        // Botón flotante para introducir código de unión
        FloatingActionButton(
            onClick = { showJoinDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    bottom = 80.dp,
                    end = 16.dp
                ),
            containerColor = colorResource(R.color.cyanSecundario),
            contentColor = Color.White
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = "Código de unión",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ComunidadItem(
    comunidad: ComunidadDTO,
    authToken: String,
    navController: NavController
) {
    val baseUrl = "https://social-me-tfg.onrender.com"
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(AppScreen.ComunidadDetalleScreen.createRoute(comunidad.url))
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colorResource(R.color.azulPrimario).copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de la comunidad
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.azulPrimario).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (comunidad.fotoPerfilId.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("$baseUrl/files/download/${comunidad.fotoPerfilId}")
                            .crossfade(true)
                            .placeholder(R.drawable.app_icon)
                            .error(R.drawable.app_icon)
                            .setHeader("Authorization", "Bearer $authToken")
                            .build(),
                        contentDescription = "Foto de comunidad ${comunidad.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "Comunidad por defecto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información de la comunidad
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = comunidad.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = comunidad.descripcion,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (comunidad.intereses.isNotEmpty()) {
                    Text(
                        text = comunidad.intereses.take(3).joinToString(", "),
                        fontSize = 12.sp,
                        color = colorResource(R.color.azulPrimario)
                    )
                }
            }

            // Indicador de privacidad
            if (comunidad.privada) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Comunidad privada",
                    tint = colorResource(R.color.azulPrimario),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ActividadItem(
    actividad: ActividadDTO,
    authToken: String,
    navController: NavController
) {
    val baseUrl = "https://social-me-tfg.onrender.com"
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(AppScreen.ActividadDetalleScreen.createRoute(actividad._id))
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colorResource(R.color.azulPrimario).copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Imagen principal si existe
            if (actividad.fotosCarruselIds.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("$baseUrl/files/download/${actividad.fotosCarruselIds.first()}")
                            .crossfade(true)
                            .placeholder(R.drawable.app_icon)
                            .error(R.drawable.app_icon)
                            .setHeader("Authorization", "Bearer $authToken")
                            .build(),
                        contentDescription = "Imagen de actividad",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Información de la actividad
            Text(
                text = actividad.nombre,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = actividad.descripcion,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Lugar: ${actividad.lugar}",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = dateFormatter.format(actividad.fechaInicio),
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun UsuarioSearchItem(
    usuario: UsuarioDTO,
    authToken: String,
    navController: NavController
) {
    val baseUrl = "https://social-me-tfg.onrender.com"
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(AppScreen.UsuarioDetalleScreen.createRoute(usuario.username))
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colorResource(R.color.azulPrimario).copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de perfil del usuario
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.azulPrimario).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (usuario.fotoPerfilId != null && usuario.fotoPerfilId.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("$baseUrl/files/download/${usuario.fotoPerfilId}")
                            .crossfade(true)
                            .placeholder(R.drawable.app_icon)
                            .error(R.drawable.app_icon)
                            .setHeader("Authorization", "Bearer $authToken")
                            .build(),
                        contentDescription = "Foto de perfil de ${usuario.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "Usuario por defecto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del usuario
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${usuario.nombre} ${usuario.apellido}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "@${usuario.username}",
                    fontSize = 14.sp,
                    color = colorResource(R.color.cyanSecundario),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (usuario.intereses.isNotEmpty()) {
                    Text(
                        text = usuario.intereses.take(3).joinToString(", "),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}