package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
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
import com.example.socialme_interfazgrafica.BuildConfig
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.ActividadDTO
import com.example.socialme_interfazgrafica.model.ComunidadDTO
import com.example.socialme_interfazgrafica.model.DenunciaCreateDTO
import com.example.socialme_interfazgrafica.model.ParticipantesComunidadDTO
import com.example.socialme_interfazgrafica.navigation.AppScreen
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.example.socialme_interfazgrafica.utils.FunctionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComunidadDetalleScreen(comunidad: ComunidadDTO, authToken: String, navController: NavController) {
    val baseUrl = BuildConfig.URL_API
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val utils = FunctionUtils

    val isUserParticipating = remember { mutableStateOf(false) }

    val isLoading = remember { mutableStateOf(false) }
    val username = remember { mutableStateOf("") }

    val cantidadUsuarios = remember { mutableStateOf(0) }
    val isLoadingUsuarios = remember { mutableStateOf(true) }

    val isCreadorOAdmin = remember { mutableStateOf(false) }
    val isLoadingVerificacion = remember { mutableStateOf(true) }

    val showCodigoUnionDialog = remember { mutableStateOf(false) }

    val showMenu = remember { mutableStateOf(false) }

    val showReportDialog = remember { mutableStateOf(false) }
    val reportReason = remember { mutableStateOf("") }
    val reportBody = remember { mutableStateOf("") }
    val isReportLoading = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username.value = sharedPreferences.getString("USERNAME", "") ?: ""
    }

    val retrofitService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    LaunchedEffect(username.value) {
        if (username.value.isEmpty()) return@LaunchedEffect

        isLoading.value = true
        try {
            supervisorScope {
                val participantesComunidadDTO = ParticipantesComunidadDTO(
                    username = username.value,
                    comunidad = comunidad.url,
                )

                val participacionResponseDeferred = async(Dispatchers.IO) {
                    retrofitService.booleanUsuarioApuntadoComunidad(
                        participantesComunidadDTO = participantesComunidadDTO,
                        token = authToken
                    )
                }

                val participacionResponse =
                    withTimeout(8000) { participacionResponseDeferred.await() }
                isUserParticipating.value = participacionResponse.isSuccessful &&
                        participacionResponse.body() == true
            }
        } catch (e: Exception) {
            Log.e("ComunidadDetalle", "Error verificando participación: ${e.message}")
        } finally {
            isLoading.value = false
        }
    }

    LaunchedEffect(comunidad.url) {
        isLoadingUsuarios.value = true
        try {
            val response = withContext(Dispatchers.IO) {
                withTimeout(5000) {
                    retrofitService.contarUsuariosEnUnaComunidad(
                        token = authToken,
                        comunidad = comunidad.url
                    )
                }
            }

            if (response.isSuccessful) {
                cantidadUsuarios.value = response.body() ?: 0
                Log.d("ComunidadDetalle", "Usuarios en la comunidad: ${cantidadUsuarios.value}")
            } else {
                Log.e("ComunidadDetalle", "Error al contar usuarios: ${response.message()}")
                cantidadUsuarios.value = 0
            }
        } catch (e: Exception) {
            Log.e("ComunidadDetalle", "Excepción al contar usuarios: ${e.message}")
            cantidadUsuarios.value = 0
        } finally {
            isLoadingUsuarios.value = false
        }
    }

    LaunchedEffect(username.value, comunidad.url) {
        if (username.value.isEmpty()) return@LaunchedEffect

        isLoadingVerificacion.value = true
        try {
            supervisorScope {
                withContext(Dispatchers.IO) {
                    withTimeout(5000) {
                        val response = retrofitService.verificarCreadorAdministradorComunidad(
                            token = authToken,
                            username = username.value,
                            comunidadUrl = comunidad.url
                        )
                        isCreadorOAdmin.value = response.isSuccessful && response.body() == true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ComunidadDetalle", "Error verificando permisos: ${e.message}")
        } finally {
            isLoadingVerificacion.value = false
        }
    }

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val imageLoader = ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .okHttpClient(okHttpClient)
        .build()

    val fotoPerfilUrl = if (comunidad.fotoPerfilId.isNotEmpty())
        "$baseUrl/files/download/${comunidad.fotoPerfilId}"
    else ""

    val fechaCreacion =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(comunidad.fechaCreacion)

    if (showCodigoUnionDialog.value && comunidad.codigoUnion != null) {
        AlertDialog(
            onDismissRequest = { showCodigoUnionDialog.value = false },
            title = { Text("Código de unión") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Comparte este código para que otros usuarios puedan unirse a esta comunidad privada:",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(R.color.cyanSecundario).copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(1.dp, colorResource(R.color.azulPrimario))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = comunidad.codigoUnion,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.azulPrimario),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(comunidad.codigoUnion))
                        Toast.makeText(
                            context,
                            "Código copiado al portapapeles",
                            Toast.LENGTH_SHORT
                        ).show()
                        showCodigoUnionDialog.value = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario)
                    )
                ) {
                    Text("Copiar código")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCodigoUnionDialog.value = false }
                ) {
                    Text("Cerrar")
                }
            }
        )
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
                            nombreItemDenunciado = comunidad.url,
                            tipoItemDenunciado = "comunidad",
                            usuarioDenunciante = username.value
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
                                    val errorMessage = jsonObject.optString("error", "")
                                    if (errorMessage.isNotEmpty()) {
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG)
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
                                ErrorUtils.parseErrorMessage(e.message ?: "Error de conexión"),
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
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (fotoPerfilUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(fotoPerfilUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.app_icon)
                            .error(R.drawable.app_icon)
                            .setHeader("Authorization", authToken)
                            .build(),
                        contentDescription = "Foto de perfil de ${comunidad.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        imageLoader = imageLoader
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorResource(R.color.azulPrimario))
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = comunidad.nombre,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "@${comunidad.url}",
                    fontSize = 16.sp,
                    color = colorResource(R.color.textoSecundario)
                )

                if (comunidad.privada) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = "Comunidad privada",
                            tint = colorResource(R.color.textoSecundario),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Privada",
                            fontSize = 14.sp,
                            color = colorResource(R.color.textoSecundario)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            val nombreComunidadEncoded = URLEncoder.encode(
                                comunidad.nombre,
                                StandardCharsets.UTF_8.toString()
                            )
                            navController.navigate(
                                AppScreen.VerUsuariosPorComunidadScreen.createRoute(
                                    comunidadId = comunidad.url,
                                    nombreComunidad = nombreComunidadEncoded
                                )
                            )
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colorResource(R.color.cyanSecundario)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cantidadUsuarios.value.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "miembros se han unido ya",
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )
                }

                Divider(color = Color.LightGray, thickness = 1.dp)

                Text(
                    text = "Descripción",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.azulPrimario),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Text(
                    text = comunidad.descripcion,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Divider(color = Color.LightGray, thickness = 1.dp)

                Text(
                    text = "Intereses",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.azulPrimario),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    comunidad.intereses.forEach { interes ->
                        Surface(
                            modifier = Modifier.padding(bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = colorResource(R.color.cyanSecundario)
                        ) {
                            Text(
                                text = interes,
                                color = colorResource(R.color.azulPrimario),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_user),
                        contentDescription = "Creador",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gestionada por: @${comunidad.creador}",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = "Fecha de creación",
                        tint = colorResource(R.color.azulPrimario),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fecha de creación: $fechaCreacion",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }

                if (!comunidad.administradores.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Administradores:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.azulPrimario)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    comunidad.administradores.take(3).forEach { admin ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colorResource(R.color.cyanSecundario)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = admin.first().toString().uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.azulPrimario)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "@$admin",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    }

                    if ((comunidad.administradores.size) > 3) {
                        Text(
                            text = "Y ${comunidad.administradores.size - 3} más...",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                if (!comunidad.fotoCarruselIds.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)

                    Text(
                        text = "Galería",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.azulPrimario),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    CarruselImagenes(
                        imagenIds = comunidad.fotoCarruselIds,
                        baseUrl = baseUrl,
                        authToken = authToken,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)

                Button(
                    onClick = {
                        if (!isLoading.value && username.value.isNotEmpty()) {
                            isLoading.value = true
                            scope.launch {
                                try {
                                    val participantesComunidadDTO = ParticipantesComunidadDTO(
                                        username = username.value,
                                        comunidad = comunidad.url,
                                    )

                                    withContext(Dispatchers.IO) {
                                        if (!isUserParticipating.value) {
                                            val response = withTimeout(5000) {
                                                retrofitService.unirseComunidad(
                                                    participantesComunidadDTO = participantesComunidadDTO,
                                                    token = authToken
                                                )
                                            }

                                            withContext(Dispatchers.Main) {
                                                if (response.isSuccessful) {
                                                    isUserParticipating.value = true
                                                    Toast.makeText(
                                                        context,
                                                        "Te has unido a la comunidad",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    cantidadUsuarios.value += 1
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Error al unirse: ${response.message()}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } else {
                                            if (comunidad.creador == username.value) {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Como creador, debes designar un nuevo creador antes de abandonar la comunidad",
                                                        Toast.LENGTH_LONG
                                                    ).show()

                                                    val nombreComunidadEncoded = URLEncoder.encode(
                                                        comunidad.nombre,
                                                        StandardCharsets.UTF_8.toString()
                                                    )
                                                    navController.navigate(
                                                        AppScreen.VerUsuariosPorComunidadScreen.createRoute(
                                                            comunidadId = comunidad.url,
                                                            nombreComunidad = nombreComunidadEncoded,
                                                            modoSeleccion = "cambiar_creador"
                                                        )
                                                    )
                                                }
                                            } else {
                                                val response = withTimeout(5000) {
                                                    retrofitService.salirComunidad(
                                                        participantesComunidadDTO = participantesComunidadDTO,
                                                        token = authToken
                                                    )
                                                }

                                                withContext(Dispatchers.Main) {
                                                    if (response.isSuccessful) {
                                                        isUserParticipating.value = false
                                                        Toast.makeText(
                                                            context,
                                                            "Has abandonado la comunidad",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        if (cantidadUsuarios.value > 0) {
                                                            cantidadUsuarios.value -= 1
                                                        }
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Error al abandonar: ${response.message()}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Error: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.e("ComunidadDetalle", "Error: $e")
                                    }
                                } finally {
                                    isLoading.value = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("joinComunidadButton")
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUserParticipating.value)
                            Color.Gray
                        else
                            colorResource(R.color.azulPrimario)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isUserParticipating.value) "ABANDONAR" else "UNIRSE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUserParticipating.value) Color.DarkGray else Color.White
                        )
                    }
                }

                if (isUserParticipating.value) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            navController.navigate(
                                AppScreen.ChatComunidadScreen.createRoute(
                                    comunidadUrl = comunidad.url,
                                    comunidadNombre = comunidad.nombre
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorResource(R.color.azulPrimario),
                        ),
                        border = BorderStroke(1.dp, colorResource(R.color.azulPrimario)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_email),
                                contentDescription = "Chat",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CHAT DE LA COMUNIDAD",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)

                CarruselActividadesComunidad(
                    comunidadUrl = comunidad.url,
                    navController = navController
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isCreadorOAdmin.value) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            navController.navigate(
                                AppScreen.CrearActividadScreen.createRoute(
                                    comunidadUrl = comunidad.url
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.cyanSecundario)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Text(
                            text = "CREAR ACTIVIDAD",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.azulPrimario)
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = colorResource(R.color.azulPrimario),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { showMenu.value = true },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Opciones",
                    tint = colorResource(R.color.azulPrimario),
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
                offset = DpOffset(x = (-160).dp, y = 0.dp),
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = "Reportar",
                                tint = colorResource(R.color.error),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reportar comunidad",
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

                if (comunidad.creador == username.value) {
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_description),
                                    contentDescription = "Modificar",
                                    tint = colorResource(R.color.azulPrimario),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Modificar comunidad",
                                    color = Color.Black
                                )
                            }
                        },
                        onClick = {
                            showMenu.value = false
                            navController.navigate(
                                AppScreen.ModificarComunidadScreen.createRoute(
                                    comunidad.url
                                )
                            )
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (comunidad.privada && isCreadorOAdmin.value && comunidad.codigoUnion != null) {
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Share,
                                    contentDescription = "Código de unión",
                                    tint = colorResource(R.color.azulPrimario),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Codigo de unión",
                                    color = Color.Black
                                )
                            }
                        },
                        onClick = {
                            showMenu.value = false
                            showCodigoUnionDialog.value = true
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CarruselImagenes(
    imagenIds: List<String>,
    baseUrl: String,
    authToken: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { imagenIds.size })

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
                .directory(context.cacheDir.resolve("carrusel_images"))
                .maxSizeBytes(50 * 1024 * 1024)
                .build()
        }
        .okHttpClient(okHttpClient)
        .build()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = modifier
        ) { page ->
            val imagenUrl = "$baseUrl/files/download/${imagenIds[page]}"

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imagenUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.app_icon)
                        .error(R.drawable.app_icon)
                        .setHeader("Authorization", authToken)
                        .memoryCacheKey("carrusel_${imagenIds[page]}")
                        .diskCacheKey("carrusel_${imagenIds[page]}")
                        .build(),
                    contentDescription = "Imagen ${page + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    imageLoader = imageLoader
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(imagenIds.size) { iteration ->
                val color = if (pagerState.currentPage == iteration)
                    colorResource(R.color.azulPrimario) else colorResource(R.color.cyanSecundario)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}
@Composable
fun CarruselActividadesComunidad(comunidadUrl: String, navController: NavController) {
    val context = LocalContext.current
    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    var actividades by remember { mutableStateOf<List<ActividadDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mostrarSoloProximas by remember { mutableStateOf(true) }

    LaunchedEffect(comunidadUrl, mostrarSoloProximas) {
        Log.d("CarruselActividadesComunidad", "LaunchedEffect iniciado para comunidad: $comunidadUrl")
        isLoading = true
        errorMessage = null
        try {
            val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("TOKEN", "") ?: ""

            if (token.isEmpty()) {
                Log.e("CarruselActividadesComunidad", "Token vacío, no se puede proceder")
                errorMessage = "No se ha encontrado un token de autenticación"
                isLoading = false
                return@LaunchedEffect
            }

            val authToken = "Bearer $token"
            Log.d("CarruselActividadesComunidad", "Realizando petición API con token: ${token.take(5)}... para comunidad: $comunidadUrl")

            val response = if (mostrarSoloProximas) {
                apiService.verActividadesPorComunidadFechaSuperior(authToken, comunidadUrl)
            } else {
                apiService.verActividadesPorComunidadCualquierFecha(authToken, comunidadUrl)
            }

            if (response.isSuccessful) {
                val actividadesRecibidas = response.body() ?: emptyList()
                Log.d("CarruselActividadesComunidad", "Actividades recibidas correctamente: ${actividadesRecibidas.size}")
                actividades = actividadesRecibidas.sortedBy { it.fechaInicio }
            } else {
                if (response.code() == 500) {
                    Log.w("CarruselActividadesComunidad", "Código 500 recibido, asumiendo lista vacía")
                    actividades = emptyList()
                } else {
                    val errorCode = response.code()
                    Log.e("CarruselActividadesComunidad", "Error al cargar actividades. Código: $errorCode, Mensaje: ${response.message()}")
                    errorMessage = when (errorCode) {
                        401 -> "No autorizado. Por favor, inicie sesión nuevamente."
                        404 -> "No se encontraron actividades en esta comunidad."
                        else -> "Error al cargar actividades: ${response.message()}"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CarruselActividadesComunidad", "Excepción al cargar actividades", e)
            errorMessage = "Error de conexión: ${e.message ?: "No se pudo conectar al servidor"}"
            e.printStackTrace()
        } finally {
            isLoading = false
            Log.d("CarruselActividadesComunidad", "Finalizada carga de actividades. isLoading: $isLoading, errorMessage: $errorMessage")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Actividades de esta comunidad",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtrar actividades:",
                    fontSize = 14.sp,
                    color = colorResource(R.color.textoSecundario)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Próximas",
                        fontSize = 12.sp,
                        color = if (mostrarSoloProximas)
                            colorResource(R.color.azulPrimario)
                        else
                            colorResource(R.color.textoSecundario),
                        fontWeight = if (mostrarSoloProximas) FontWeight.Bold else FontWeight.Normal
                    )

                    Switch(
                        checked = !mostrarSoloProximas,
                        onCheckedChange = { mostrarSoloProximas = !it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorResource(R.color.azulPrimario),
                            checkedTrackColor = colorResource(R.color.cyanSecundario),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        ),
                        modifier = Modifier.scale(0.8f)
                    )

                    Text(
                        text = "Todas",
                        fontSize = 12.sp,
                        color = if (!mostrarSoloProximas)
                            colorResource(R.color.azulPrimario)
                        else
                            colorResource(R.color.textoSecundario),
                        fontWeight = if (!mostrarSoloProximas) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> {
                Log.d("CarruselActividadesComunidad", "Mostrando indicador de carga")
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
                Log.d("CarruselActividadesComunidad", "Mostrando mensaje de error: $errorMessage")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage!!,
                        color = colorResource(R.color.error),
                        textAlign = TextAlign.Center
                    )
                }
            }
            actividades.isEmpty() -> {
                Log.d("CarruselActividadesComunidad", "Mostrando mensaje de lista vacía")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (mostrarSoloProximas) "No hay actividades próximas en esta comunidad" else "No hay actividades en esta comunidad",
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
                        items = actividades,
                        key = { it.nombre }
                    ) { actividad ->
                        Log.d("CarruselActividadesComunidad", "Cargando actividad: ${actividad.nombre}")
                        ActividadCard(actividad = actividad, navController = navController)
                    }
                }
            }
        }
    }
}