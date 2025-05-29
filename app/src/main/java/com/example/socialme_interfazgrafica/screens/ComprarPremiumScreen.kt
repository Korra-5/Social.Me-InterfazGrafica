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
import com.example.socialme_interfazgrafica.services.PayPalSimulationService
import com.example.socialme_interfazgrafica.services.PurchaseResult
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
    var connectionTested by remember { mutableStateOf(false) }
    var connectionOk by remember { mutableStateOf(false) }
    val TAG = "ComprarPremiumScreen"

    // Obtener información del usuario
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val username = sharedPreferences.getString("USERNAME", "") ?: ""

    val simulationService = remember { PayPalSimulationService() }

    // Verificar si el usuario ya es premium
    LaunchedEffect(Unit) {
        if (token.isNotEmpty() && username.isNotEmpty()) {
            scope.launch {
                try {
                    Log.d(TAG, "Verificando estado premium del usuario...")
                    val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
                    val response = apiService.verUsuarioPorUsername("Bearer $token", username)
                    if (response.isSuccessful) {
                        isPremium = response.body()?.premium ?: false
                        Log.d(TAG, "Usuario es premium: $isPremium")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error verificando usuario", e)
                }
            }
        }
    }

    // Probar conexión con el backend
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                connectionOk = simulationService.testConnection()
                connectionTested = true
                Log.d(TAG, "Conexión con backend: $connectionOk")
            } catch (e: Exception) {
                Log.e(TAG, "Error probando conexión", e)
                connectionTested = true
                connectionOk = false
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
                text = "Desbloquea todas las funciones especiales",
                fontSize = 16.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Beneficios Premium
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
                        text = "Beneficios Premium:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.azulPrimario)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BenefitItem(text = "Crea hasta 10 comunidades (en lugar de 3)", icon = "✨")
                    BenefitItem(text = "Prioridad en las búsquedas", icon = "🔍")
                    BenefitItem(text = "Soporte prioritario", icon = "🛟")
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
                        text = "Precio especial",
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
                        text = "Simulación de pago",
                        fontSize = 14.sp,
                        color = colorResource(R.color.textoSecundario)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Estado de conexión
            if (!connectionTested) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                ) {
                    Text(
                        text = "Probando conexión...",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (!connectionOk) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8D7DA))
                ) {
                    Text(
                        text = "Error de conexión con el servidor",
                        fontSize = 14.sp,
                        color = Color(0xFF721C24),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de compra
            if (isPremium) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "¡Ya eres Premium!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Button(
                    onClick = {
                        showConfirmDialog = true
                    },
                    enabled = !isLoading && connectionOk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.azulPrimario)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Simular Compra Premium",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Simulación de pago - No se cobra dinero real",
                fontSize = 12.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center
            )
        }
    }

    // Diálogo de confirmación
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar compra simulada") },
            text = {
                Text("¿Deseas simular la compra de Premium por €$PREMIUM_PRICE_EUR?\n\nEsto es solo una simulación - no se cobrará dinero real.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        scope.launch {
                            isLoading = true
                            when (val result = simulationService.simulatePremiumPurchase(
                                username = username,
                                amount = PREMIUM_PRICE_EUR,
                                token = token
                            )) {
                                is PurchaseResult.Success -> {
                                    Log.d(TAG, "✅ Compra simulada exitosa: ${result.orderId}")
                                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                    isPremium = true
                                    // Actualizar SharedPreferences
                                    with(sharedPreferences.edit()) {
                                        putBoolean("PREMIUM", true)
                                        apply()
                                    }
                                }
                                is PurchaseResult.Error -> {
                                    Log.e(TAG, "❌ Error en compra: ${result.message}")
                                    Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                            isLoading = false
                        }
                    }
                ) {
                    Text("Simular", color = colorResource(R.color.azulPrimario))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun BenefitItem(text: String, icon: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}