package com.example.socialme_interfazgrafica.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.socialme_interfazgrafica.utils.ErrorUtils
import com.paypal.android.sdk.payments.PayPalConfiguration
import com.paypal.android.sdk.payments.PayPalPayment
import com.paypal.android.sdk.payments.PayPalService
import com.paypal.android.sdk.payments.PaymentActivity
import com.paypal.android.sdk.payments.PaymentConfirmation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprarPremiumScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPremium by remember { mutableStateOf(false) }

    // Obtener información del usuario
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val username = sharedPreferences.getString("USERNAME", "") ?: ""

    // Verificar si el usuario ya es premium
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
                    // Manejar error silenciosamente
                }
            }
        }
    }

    // Configuración de PayPal
    val paypalConfig = PayPalConfiguration()
        .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) // Cambiar a PRODUCTION en producción
        .clientId("YOUR_PAYPAL_CLIENT_ID") // Reemplazar con tu Client ID real

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        // App Bar
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

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de estrella
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Premium",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Text(
                text = "¡Hazte Premium!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.azulPrimario),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo
            Text(
                text = "Desbloquea todas las funciones especiales",
                fontSize = 16.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Beneficios
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

                    BenefitItem(
                        text = "Crea hasta 10 comunidades (en lugar de 3)",
                        icon = "✨"
                    )
                    BenefitItem(
                        text = "Prioridad en las búsquedas",
                        icon = "🔍"
                    )
                    BenefitItem(
                        text = "Acceso a funciones exclusivas",
                        icon = "⭐"
                    )
                    BenefitItem(
                        text = "Soporte prioritario",
                        icon = "🛟"
                    )
                    BenefitItem(
                        text = "Sin anuncios",
                        icon = "🚫"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Precio
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.cyanSecundario).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Precio especial",
                        fontSize = 16.sp,
                        color = colorResource(R.color.textoSecundario)
                    )
                    Text(
                        text = "€9.99",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.azulPrimario)
                    )
                    Text(
                        text = "Acceso de por vida",
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)),
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
                    onClick = { showConfirmDialog = true },
                    enabled = !isLoading,
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
                            text = "Comprar Premium con PayPal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información de seguridad
            Text(
                text = "Pago seguro mediante PayPal\nNo almacenamos tu información de pago",
                fontSize = 12.sp,
                color = colorResource(R.color.textoSecundario),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }

    // Diálogo de confirmación
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar compra") },
            text = {
                Text("¿Estás seguro de que deseas comprar SocialMe Premium por €9.99?\n\nSerás redirigido a PayPal para completar el pago.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        procesarCompraPremium(
                            context = context,
                            username = username,
                            token = token,
                            onLoading = { isLoading = it },
                            onSuccess = { showSuccessDialog = true },
                            onError = { errorMessage = it }
                        )
                    }
                ) {
                    Text("Confirmar", color = colorResource(R.color.azulPrimario))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                isPremium = true
            },
            title = { Text("¡Compra exitosa!") },
            text = {
                Text("¡Bienvenido a SocialMe Premium! Ya puedes disfrutar de todas las funciones exclusivas.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        isPremium = true
                        navController.popBackStack()
                    }
                ) {
                    Text("Entendido", color = colorResource(R.color.azulPrimario))
                }
            }
        )
    }

    // Mostrar error si existe
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            errorMessage = null
        }
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

// Función para procesar la compra premium (simulada)
private fun procesarCompraPremium(
    context: Context,
    username: String,
    token: String,
    onLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    // En un proyecto real, aquí implementarías la integración con PayPal
    // Por ahora, simularemos el proceso

    onLoading(true)

    // Simular proceso de pago
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            // Aquí iría la lógica real de PayPal
            kotlinx.coroutines.delay(2000) // Simular tiempo de procesamiento

            // Actualizar el estado premium en el servidor
            val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
            val response = apiService.actualizarPremium("Bearer $token", username)

            withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (response.isSuccessful) {
                    // Actualizar SharedPreferences
                    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putBoolean("PREMIUM", true)
                        apply()
                    }
                    onSuccess()
                } else {
                    onError("Error al procesar la compra: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                onError(ErrorUtils.parseErrorMessage(e.message ?: "Error de conexión"))
            }
        } finally {
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                onLoading(false)
            }
        }
    }
}