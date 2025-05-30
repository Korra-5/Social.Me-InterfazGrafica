package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.DenunciaDTO
import com.example.socialme_interfazgrafica.navigation.AppScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class DenunciaType {
    TODAS,
    NO_COMPLETADAS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DenunciasScreen(navController: NavController) {
    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var denuncias by remember { mutableStateOf<List<DenunciaDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }

    // Dialog de cerrar sesión
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
    var confirmarCerrarSesion by remember { mutableStateOf(false) }
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("TOKEN", "") ?: ""
    val token = "Bearer $authToken"

    val tabs = listOf(
        DenunciaType.TODAS,
        DenunciaType.NO_COMPLETADAS
    )

    // Cargar denuncias
    fun loadDenuncias(type: DenunciaType) {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = when (type) {
                    DenunciaType.TODAS -> retrofitService.verTodasLasDenuncias(token)
                    DenunciaType.NO_COMPLETADAS -> retrofitService.verDenunciasNoCompletadas(token)
                }

                if (response.isSuccessful) {
                    denuncias = response.body() ?: emptyList()
                } else {
                    errorMessage = "Error al cargar denuncias: ${response.code()}"
                    showError = true
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                showError = true
            } finally {
                isLoading = false
            }
        }
    }

    // Toggle estado denuncia
    fun toggleDenunciaStatus(denunciaId: String, completado: Boolean) {
        coroutineScope.launch {
            try {
                val response = retrofitService.completarDenuncia(token, denunciaId, completado)
                if (response.isSuccessful) {
                    // Recargar la lista
                    loadDenuncias(tabs[selectedTabIndex])
                } else {
                    errorMessage = "Error al actualizar denuncia: ${response.code()}"
                    showError = true
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                showError = true
            }
        }
    }

    // Logout
    fun cerrarSesion() {
        // Limpiar datos
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }

        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()

        // Volver al login usando la ruta correcta
        navController.navigate(AppScreen.InicioSesionScreen.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    // Cargar al inicio
    LaunchedEffect(selectedTabIndex) {
        loadDenuncias(tabs[selectedTabIndex])
    }

    // Error dialog
    if (showError && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = {
                Text("Error", style = MaterialTheme.typography.titleLarge, color = Color.Red)
            },
            text = {
                Text(text = errorMessage ?: "Ha ocurrido un error desconocido")
            },
            confirmButton = {
                Button(
                    onClick = { showError = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.cyanSecundario))
                ) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Dialog cerrar sesión
    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoCerrarSesion = false
                confirmarCerrarSesion = false
            },
            title = {
                Text(
                    "Cerrar sesión",
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.cyanSecundario)
                )
            },
            text = {
                Column {
                    Text(
                        text = "¿Estás seguro de que deseas cerrar sesión?",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Tendrás que volver a iniciar sesión para acceder al panel de administración.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { confirmarCerrarSesion = !confirmarCerrarSesion }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = confirmarCerrarSesion,
                            onCheckedChange = { confirmarCerrarSesion = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colorResource(R.color.cyanSecundario)
                            )
                        )
                        Text(
                            text = "Sí, quiero cerrar mi sesión",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCerrarSesion = false
                        confirmarCerrarSesion = false
                        cerrarSesion()
                    },
                    enabled = confirmarCerrarSesion
                ) {
                    Text(
                        "Cerrar sesión",
                        color = if (confirmarCerrarSesion) colorResource(R.color.cyanSecundario) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCerrarSesion = false
                        confirmarCerrarSesion = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .padding(16.dp)
    ) {
        // Header con título y logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Panel de Denuncias",
                color = colorResource(R.color.cyanSecundario),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // Botón logout
            IconButton(
                onClick = { mostrarDialogoCerrarSesion = true },
                modifier = Modifier
                    .background(
                        colorResource(R.color.cyanSecundario).copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    tint = colorResource(R.color.cyanSecundario),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por usuario denunciante...") },
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

        // Tabs
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
                            DenunciaType.TODAS -> "Ver Todas"
                            DenunciaType.NO_COMPLETADAS -> "No Completadas"
                        },
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorResource(R.color.cyanSecundario))
            }
        } else {
            val filteredDenuncias = if (searchQuery.isEmpty()) {
                denuncias
            } else {
                denuncias.filter { denuncia ->
                    denuncia.usuarioDenunciante?.contains(searchQuery, ignoreCase = true) == true
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredDenuncias) { denuncia ->
                    DenunciaItem(
                        denuncia = denuncia,
                        onToggleStatus = { denunciaId, completado ->
                            toggleDenunciaStatus(denunciaId, completado)
                        },
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun DenunciaItem(
    denuncia: DenunciaDTO,
    onToggleStatus: (String, Boolean) -> Unit,
    navController: NavController
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colorResource(R.color.azulPrimario).copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header con motivo y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = denuncia.motivo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                // Estado
                IconButton(
                    onClick = {
                        denuncia._id?.let { id ->
                            onToggleStatus(id, !denuncia.solucionado)
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (denuncia.solucionado) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = if (denuncia.solucionado) "Completada" else "No completada",
                        tint = if (denuncia.solucionado) Color.Green else Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info denuncia
            Text(
                text = "Denunciante: ${denuncia.usuarioDenunciante ?: "Desconocido"}",
                fontSize = 14.sp,
                color = colorResource(R.color.cyanSecundario),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Item + lupa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Item denunciado: ${denuncia.nombreItemDenunciado}",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1f)
                )

                // Lupa
                IconButton(
                    onClick = {
                        when (denuncia.tipoItemDenunciado.lowercase()) {
                            "usuario" -> {
                                navController.navigate(
                                    AppScreen.UsuarioDetalleScreen.createRoute(denuncia.nombreItemDenunciado)
                                )
                            }
                            "actividad" -> {
                                navController.navigate(
                                    AppScreen.ActividadDetalleScreen.createRoute(denuncia.nombreItemDenunciado)
                                )
                            }
                            "comunidad" -> {
                                navController.navigate(
                                    AppScreen.ComunidadDetalleScreen.createRoute(denuncia.nombreItemDenunciado)
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ver ${denuncia.tipoItemDenunciado}",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tipo: ${denuncia.tipoItemDenunciado}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contenido
            Text(
                text = denuncia.cuerpo,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatter.format(denuncia.fechaCreacion),
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                Text(
                    text = if (denuncia.solucionado) "COMPLETADA" else "PENDIENTE",
                    fontSize = 12.sp,
                    color = if (denuncia.solucionado) Color.Green else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}