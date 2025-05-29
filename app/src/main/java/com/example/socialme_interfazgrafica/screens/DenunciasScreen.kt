package com.example.socialme_interfazgrafica.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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

    // Estados
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var denuncias by remember { mutableStateOf<List<DenunciaDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }

    // Obtener token de autenticación
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("TOKEN", "") ?: ""
    val token = "Bearer $authToken"

    val tabs = listOf(
        DenunciaType.TODAS,
        DenunciaType.NO_COMPLETADAS
    )

    // Función para cargar denuncias
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

    // Función para completar/descompletar denuncia
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

    // Cargar datos iniciales
    LaunchedEffect(selectedTabIndex) {
        loadDenuncias(tabs[selectedTabIndex])
    }

    // Diálogo de error
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Panel de Denuncias",
            color = colorResource(R.color.cyanSecundario),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de búsqueda por usuario denunciante
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

        // Pestañas
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

        // Lista de denuncias
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
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DenunciaItem(
    denuncia: DenunciaDTO,
    onToggleStatus: (String, Boolean) -> Unit
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
            // Cabecera con motivo y botón de estado
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

                // Botón de estado
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

            // Información de la denuncia
            Text(
                text = "Denunciante: ${denuncia.usuarioDenunciante ?: "Desconocido"}",
                fontSize = 14.sp,
                color = colorResource(R.color.cyanSecundario),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Item denunciado: ${denuncia.nombreItemDenunciado}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tipo: ${denuncia.tipoItemDenunciado}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cuerpo de la denuncia
            Text(
                text = denuncia.cuerpo,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fecha y estado
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