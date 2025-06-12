package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val PREMIUM_PRICE_EUR = "1.99"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprarPremiumScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isPremium by remember { mutableStateOf(false) }
    var connectionOk by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(true) }
    val TAG = "ComprarPremiumScreen"

    // Obtener información del usuario
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val username = sharedPreferences.getString("USERNAME", "") ?: ""

    // Función para verificar conexión
    fun testConnection() {
        scope.launch {
            try {
                val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                val healthResponse = apiService.healthCheck()
                connectionOk = healthResponse.isSuccessful
                Log.d(TAG, "Conexión: ${if (connectionOk) "OK" else "Error ${healthResponse.code()}"}")
            } catch (e: Exception) {
                connectionOk = false
                Log.e(TAG, "Error de conexión", e)
            } finally {
                isConnecting = false
            }
        }
    }

    // Verificar si el usuario ya es premium y probar conexión
    LaunchedEffect(Unit) {
        if (token.isNotEmpty() && username.isNotEmpty()) {
            scope.launch {
                try {
                    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                    val response = apiService.verUsuarioPorUsername("Bearer $token", username)
                    if (response.isSuccessful) {
                        isPremium = response.body()?.premium ?: false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error verificando usuario", e)
                }
            }
        }

        delay(1000)
        testConnection()
    }

    // Función para simular el pago
    fun processPremiumPurchase() {
        scope.launch {
            isLoading = true
            try {
                val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                val purchaseResponse = apiService.simulatePremiumPurchase(
                    token = "Bearer $token",
                    username = username
                )

                if (purchaseResponse.isSuccessful) {
                    val result = purchaseResponse.body()
                    if (result?.success == true) {
                        isPremium = true
                        Toast.makeText(
                            context,
                            "¡Pago exitoso! Ya eres Premium 🌟",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Error en el procesamiento del pago",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Error del servidor. Inténtalo más tarde",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error de conexión. Verifica tu internet",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isLoading = false
                showConfirmDialog = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        TopAppBar(
            title = { Text(text = "SocialMe Premium") },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Premium",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¡Hazte Premium!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Expande tu capacidad de crear comunidades",
                fontSize = 16.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Comparación Premium vs Básico
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "¿Qué obtienes con Premium?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.azulPrimario),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Comparación visual
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Plan Básico
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Gray.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "👤",
                                        fontSize = 32.sp
                                    )
                                    Text(
                                        text = "Plan Básico",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "3",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "comunidades",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Vs
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(0.3f)
                        ) {
                            Text(
                                text = "VS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.azulPrimario)
                            )
                        }

                        // Plan Premium
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "⭐",
                                        fontSize = 32.sp
                                    )
                                    Text(
                                        text = "Plan Premium",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB8860B)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "10",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB8860B)
                                    )
                                    Text(
                                        text = "comunidades",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB8860B),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Descripción del beneficio
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(R.color.cyanSecundario).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🚀",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = "¡Más de 3 veces la capacidad!",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.azulPrimario)
                                )
                                Text(
                                    text = "Crea hasta 10 comunidades y lidera más grupos",
                                    fontSize = 14.sp,
                                    color = colorResource(R.color.textoSecundario)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Precio
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.cyanSecundario).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Precio especial de lanzamiento",
                        fontSize = 16.sp,
                        color = colorResource(R.color.textoSecundario)
                    )

                    Text(
                        text = "€$PREMIUM_PRICE_EUR",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.azulPrimario)
                    )

                    Text(
                        text = "Pago único • Procesado con PayPal",
                        fontSize = 14.sp,
                        color = colorResource(R.color.textoSecundario)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de compra
            if (isPremium) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "¡Ya eres Premium!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            } else if (isConnecting) {
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Conectando...",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            } else if (!connectionOk) {
                Button(
                    onClick = {
                        isConnecting = true
                        testConnection()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Error de conexión - Reintentar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Button(
                    onClick = { showConfirmDialog = true },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario),
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Procesando...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "💳 Comprar Premium €$PREMIUM_PRICE_EUR",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    "Confirmar compra Premium",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "¿Confirmas la compra de Premium por €$PREMIUM_PRICE_EUR?",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Podrás crear hasta 10 comunidades en lugar de 3.",
                        fontSize = 14.sp,
                        color = colorResource(R.color.textoSecundario)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "(Esto es una simulacion)",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        style = androidx.compose.ui.text.TextStyle(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { processPremiumPurchase() },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario)
                    )
                ) {
                    Text(
                        text = if (isLoading) "Procesando..." else "Confirmar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false },
                    enabled = !isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}