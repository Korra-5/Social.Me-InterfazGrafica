package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.ParticipantesComunidadDTO
import com.example.socialme_interfazgrafica.model.UsuarioDTO
import com.example.socialme_interfazgrafica.navigation.AppScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerUsuariosPorComunidadScreen(
    navController: NavController,
    comunidadId: String,
    nombreComunidad: String,
    modoSeleccion: String = "" // Nuevo parámetro
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    val usuarios = remember { mutableStateOf<List<UsuarioDTO>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }

    // Estados para el diálogo de confirmación
    val showConfirmDialog = remember { mutableStateOf(false) }
    val selectedUser = remember { mutableStateOf<UsuarioDTO?>(null) }
    val checkboxConfirmed = remember { mutableStateOf(false) }
    val isChangingCreator = remember { mutableStateOf(false) }

    // Estados existentes
    val isCreador = remember { mutableStateOf(false) }
    val isAdmin = remember { mutableStateOf(false) }
    val isVerifyingPermissions = remember { mutableStateOf(true) }
    val comunidadData = remember { mutableStateOf<ComunidadDTO?>(null) }

    // Determinar si estamos en modo "cambiar creador"
    val esModoSeleccionCreador = modoSeleccion == "cambiar_creador"

    // Obtener el token de autenticación
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val username = sharedPreferences.getString("USERNAME", "") ?: ""
    val authToken = "Bearer $token"

    // Configurar el cargador de imágenes
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
                .directory(context.cacheDir.resolve("user_images"))
                .maxSizeBytes(50 * 1024 * 1024)
                .build()
        }
        .build()

    // Verificar si el usuario es creador o administrador
    LaunchedEffect(comunidadId, username) {
        isVerifyingPermissions.value = true

        scope.launch {
            try {
                // Primero obtenemos los datos de la comunidad
                val comunidadResponse = withContext(Dispatchers.IO) {
                    withTimeout(8000) {
                        retrofitService.verComunidadPorUrl(authToken, comunidadId)
                    }
                }

                if (comunidadResponse.isSuccessful && comunidadResponse.body() != null) {
                    val comunidad = comunidadResponse.body()!!
                    comunidadData.value = comunidad

                    // Verificar si el usuario actual es el creador
                    isCreador.value = comunidad.creador == username

                    // Verificar si el usuario actual es administrador
                    isAdmin.value = comunidad.administradores?.contains(username) ?: false
                } else {
                    // Si no podemos obtener datos de la comunidad, asumimos que no es creador ni admin
                    isCreador.value = false
                    isAdmin.value = false
                }
            } catch (e: Exception) {
                // En caso de error, asumimos que no es creador ni admin
                isCreador.value = false
                isAdmin.value = false
            } finally {
                isVerifyingPermissions.value = false
            }
        }
    }

    // Cargar los usuarios de la comunidad
    LaunchedEffect(comunidadId) {
        isLoading.value = true
        error.value = null

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    withTimeout(10000) {
                        retrofitService.verUsuariosPorComunidad(
                            token = authToken,
                            comunidad = comunidadId,
                            usuarioActual = username
                        )
                    }
                }

                if (response.isSuccessful && response.body() != null) {
                    usuarios.value = response.body()!!
                } else {
                    error.value = "No se pudieron cargar los usuarios: ${response.message()}"
                }
            } catch (e: Exception) {
                error.value = "Error de conexión: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // Función para cambiar creador
    fun cambiarCreadorComunidad(nuevoCreador: String) {
        scope.launch {
            isChangingCreator.value = true
            try {
                val response = withContext(Dispatchers.IO) {
                    withTimeout(8000) {
                        retrofitService.cambiarCreadorComunidad(
                            token = authToken,
                            comunidadUrl = comunidadId,
                            creadorActual = username,
                            nuevoCreador = nuevoCreador
                        )
                    }
                }

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Creador cambiado exitosamente. Ahora puedes abandonar la comunidad.",
                            Toast.LENGTH_LONG
                        ).show()

                        // Volver a la pantalla anterior
                        navController.popBackStack()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Error al cambiar creador: ${response.message()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                isChangingCreator.value = false
                showConfirmDialog.value = false
                checkboxConfirmed.value = false
            }
        }
    }

    // Función para eliminar un usuario de la comunidad
    fun eliminarUsuarioDeComunidad(usuarioUsername: String) {
        scope.launch {
            try {
                // Mostrar diálogo de confirmación
                val confirmacion = android.app.AlertDialog.Builder(context)
                    .setTitle("Eliminar miembro")
                    .setMessage("¿Estás seguro que quieres eliminar a este miembro de la comunidad?")
                    .setPositiveButton("Eliminar") { dialog, _ ->
                        dialog.dismiss()

                        // Realizar la eliminación
                        scope.launch {
                            try {
                                val participantesComunidadDTO = ParticipantesComunidadDTO(
                                    username = usuarioUsername,
                                    comunidad = comunidadId
                                )

                                val response = withContext(Dispatchers.IO) {
                                    withTimeout(5000) {
                                        retrofitService.eliminarUsuarioDeComunidad(
                                            token = authToken,
                                            participantesComunidadDTO = participantesComunidadDTO,
                                            usuarioSolicitante = username
                                        )
                                    }
                                }

                                if (response.isSuccessful) {
                                    // Actualizar la lista de usuarios removiendo el eliminado
                                    usuarios.value = usuarios.value.filter { it.username != usuarioUsername }
                                    Toast.makeText(context, "Usuario eliminado de la comunidad", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error al eliminar: ${response.message()}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()

                confirmacion.show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Diálogo de confirmación para cambiar creador
    if (showConfirmDialog.value && selectedUser.value != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog.value = false
                checkboxConfirmed.value = false
            },
            title = {
                Text(
                    text = "Cambiar creador",
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.azulPrimario)
                )
            },
            text = {
                Column {
                    Text(
                        text = "¿Estás seguro que quieres transferir la propiedad de la comunidad a:",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(R.color.cyanSecundario).copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${selectedUser.value!!.nombre} ${selectedUser.value!!.apellido}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "@${selectedUser.value!!.username}",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Text(
                        text = "Esta acción NO se puede deshacer. El usuario seleccionado se convertirá en el nuevo creador y tú pasarás a ser administrador.",
                        color = colorResource(R.color.error),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            checkboxConfirmed.value = !checkboxConfirmed.value
                        }
                    ) {
                        Checkbox(
                            checked = checkboxConfirmed.value,
                            onCheckedChange = { checkboxConfirmed.value = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colorResource(R.color.azulPrimario)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirmo que entiendo que esta acción no se puede deshacer",
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        cambiarCreadorComunidad(selectedUser.value!!.username)
                    },
                    enabled = checkboxConfirmed.value && !isChangingCreator.value,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario),
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    if (isChangingCreator.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Cambiar creador")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog.value = false
                        checkboxConfirmed.value = false
                    },
                    enabled = !isChangingCreator.value
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (esModoSeleccionCreador) "Seleccionar nuevo creador" else "Miembros",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (esModoSeleccionCreador)
                        colorResource(R.color.azulPrimario)
                    else
                        colorResource(R.color.cyanSecundario)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when {
                isLoading.value -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colorResource(R.color.cyanSecundario)
                    )
                }
                error.value != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Error",
                            tint = colorResource(R.color.cyanSecundario),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error.value ?: "Error desconocido",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                usuarios.value.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Sin usuarios",
                            tint = colorResource(R.color.cyanSecundario),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay miembros en esta comunidad",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¡Sé el primero en unirte!",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Mensaje especial para modo selección de creador
                        if (esModoSeleccionCreador) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colorResource(R.color.azulPrimario).copy(alpha = 0.1f)
                                ),
                                border = BorderStroke(1.dp, colorResource(R.color.azulPrimario))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_user),
                                            contentDescription = "Info",
                                            tint = colorResource(R.color.azulPrimario),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Selecciona nuevo creador",
                                            fontWeight = FontWeight.Bold,
                                            color = colorResource(R.color.azulPrimario)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Para abandonar la comunidad, primero debes designar a un nuevo creador. Selecciona un miembro de la lista.",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }

                        // Título de la comunidad
                        Text(
                            text = if (esModoSeleccionCreador)
                                "Miembros de:"
                            else
                                "Miembros de la comunidad:",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = nombreComunidad,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (esModoSeleccionCreador)
                                colorResource(R.color.azulPrimario)
                            else
                                colorResource(R.color.cyanSecundario),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Contador de miembros
                        Text(
                            text = "${usuarios.value.size} ${if (usuarios.value.size == 1) "miembro" else "miembros"}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Lista de usuarios
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(usuarios.value) { usuario ->
                                // En modo selección de creador, excluir al usuario actual
                                if (esModoSeleccionCreador && usuario.username == username) {
                                    // No mostrar al usuario actual en modo selección
                                } else {
                                    val puedeEliminar = if (!esModoSeleccionCreador) {
                                        when {
                                            isCreador.value -> usuario.username != username
                                            isAdmin.value -> {
                                                usuario.username != comunidadData.value?.creador &&
                                                        !(comunidadData.value?.administradores?.contains(usuario.username) ?: false)
                                            }
                                            else -> false
                                        }
                                    } else false

                                    UsuarioItem(
                                        usuario = usuario,
                                        authToken = authToken,
                                        imageLoader = imageLoader,
                                        onClick = {
                                            if (esModoSeleccionCreador) {
                                                // En modo selección, mostrar diálogo de confirmación
                                                selectedUser.value = usuario
                                                showConfirmDialog.value = true
                                            } else {
                                                // Navegación normal al perfil
                                                navController.navigate(AppScreen.UsuarioDetalleScreen.createRoute(usuario.username))
                                            }
                                        },
                                        color = if (esModoSeleccionCreador)
                                            colorResource(R.color.azulPrimario)
                                        else
                                            colorResource(R.color.cyanSecundario),
                                        mostrarEliminar = puedeEliminar,
                                        onEliminar = {
                                            eliminarUsuarioDeComunidad(usuario.username)
                                        },
                                        // Nuevos parámetros para modo selección
                                        esSeleccionable = esModoSeleccionCreador,
                                        textoAccion = if (esModoSeleccionCreador) "hacer creador" else null
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsuarioItem(
    usuario: UsuarioDTO,
    authToken: String,
    onClick: () -> Unit,
    color: Color = colorResource(id = R.color.azulPrimario),
    imageLoader: ImageLoader? = null,
    mostrarEliminar: Boolean = false,
    onEliminar: () -> Unit = {},
    esSeleccionable: Boolean = false, // Nuevo parámetro
    textoAccion: String? = null // Nuevo parámetro
) {
    val baseUrl = "https://social-me-tfg.onrender.com"
    val context = LocalContext.current
    val actualImageLoader = imageLoader ?: ImageLoader.Builder(context).build()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (esSeleccionable)
                color.copy(alpha = 0.05f)
            else
                Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            if (esSeleccionable)
                color.copy(alpha = 0.3f)
            else
                color.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esSeleccionable) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de perfil
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (usuario.fotoPerfilId != null && usuario.fotoPerfilId.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("$baseUrl/files/download/${usuario.fotoPerfilId}")
                            .crossfade(true)
                            .placeholder(R.drawable.app_icon)
                            .error(R.drawable.app_icon)
                            .setHeader("Authorization", authToken)
                            .build(),
                        contentDescription = "Foto de perfil de ${usuario.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        imageLoader = actualImageLoader
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "Perfil por defecto",
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
                    color = Color.Gray
                )

                // Mostrar texto de acción si está disponible
                if (textoAccion != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toca para $textoAccion",
                        fontSize = 12.sp,
                        color = color,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // Icono de acción
            if (esSeleccionable) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "Seleccionar",
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            } else if (mostrarEliminar) {
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar usuario",
                        tint = colorResource(R.color.error),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}