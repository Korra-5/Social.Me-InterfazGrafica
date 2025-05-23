package com.example.socialme_interfazgrafica.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import java.math.BigDecimal
import java.util.Properties

// Constante para el precio
const val PREMIUM_PRICE_EUR = "1.99"
const val PREMIUM_PRICE_DECIMAL = 1.99

object PayPalConfig {
    private const val TAG = "PayPalConfig"
    private var properties: Properties? = null
    private var isInitialized = false
    private var initError: String? = null

    fun init(context: Context) {
        Log.d(TAG, "Iniciando configuración de PayPal...")
        if (!isInitialized) {
            properties = Properties()
            try {
                Log.d(TAG, "Intentando cargar paypal-config.properties desde assets...")
                val inputStream = context.assets.open("paypal-config.properties")
                properties!!.load(inputStream)
                inputStream.close()
                Log.d(TAG, "Archivo cargado exitosamente")

                // Verificar que el ClientID no sea el placeholder
                val clientId = properties!!.getProperty("PAYPAL_APIKEY_PUBLICA", "").trim()
                Log.d(TAG, "ClientID encontrado: ${clientId.take(20)}... (length: ${clientId.length})")

                if (clientId.contains("Your_PayPal_Client_ID_Here") || clientId.isEmpty()) {
                    initError = "ClientID de PayPal no configurado. Usa un ClientID real de PayPal Sandbox."
                    Log.e(TAG, "Error: $initError")
                } else if (clientId.length < 75) {
                    initError = "ClientID parece ser inválido (muy corto: ${clientId.length} caracteres). Verifica que sea correcto."
                    Log.e(TAG, "Error: $initError")
                } else {
                    Log.d(TAG, "ClientID parece válido (${clientId.length} caracteres)")
                    // Verificar que empiece con A (formato típico de PayPal)
                    if (!clientId.startsWith("A")) {
                        Log.w(TAG, "Advertencia: ClientID no empieza con 'A', podría ser inválido")
                    }
                }

                val mode = properties!!.getProperty("PAYPAL_MODE", "sandbox")
                Log.d(TAG, "Modo PayPal: $mode")

                isInitialized = true
                Log.d(TAG, "Configuración de PayPal completada exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando configuración PayPal", e)
                initError = "Error cargando configuración PayPal: ${e.message}"
                // Valores por defecto para evitar crash
                properties = Properties().apply {
                    setProperty("PAYPAL_APIKEY_PUBLICA", "demo_client_id_for_testing")
                    setProperty("PAYPAL_MODE", "sandbox")
                }
                isInitialized = true
            }
        } else {
            Log.d(TAG, "PayPal ya está inicializado")
        }
    }

    fun getClientId(): String {
        return properties?.getProperty("PAYPAL_APIKEY_PUBLICA", "")?.trim() ?: "demo_client_id_for_testing"
    }

    fun getEnvironment(): String {
        return properties?.getProperty("PAYPAL_MODE") ?: "sandbox"
    }

    fun isSandbox(): Boolean = getEnvironment() == "sandbox"

    fun hasError(): Boolean = initError != null

    fun getError(): String? = initError

    fun isValidConfig(): Boolean {
        val clientId = getClientId()
        val isValid = !clientId.contains("demo_client_id") &&
                !clientId.contains("Your_PayPal_Client_ID_Here") &&
                clientId.isNotEmpty() &&
                clientId.length >= 75 &&
                clientId.startsWith("A")
        Log.d(TAG, "Config es válido: $isValid (ClientID: ${clientId.take(20)}..., length: ${clientId.length})")
        return isValid
    }
}

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
    var isPayPalConfigured by remember { mutableStateOf(false) }
    val TAG = "ComprarPremiumScreen"

    // Inicializar PayPal Config
    LaunchedEffect(Unit) {
        Log.d(TAG, "Iniciando LaunchedEffect...")
        PayPalConfig.init(context)
        isPayPalConfigured = PayPalConfig.isValidConfig()

        Log.d(TAG, "PayPal configurado: $isPayPalConfigured")

        if (PayPalConfig.hasError()) {
            val error = PayPalConfig.getError()
            Log.e(TAG, "Error de PayPal: $error")
            errorMessage = error
        }
    }

    // Obtener información del usuario
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("TOKEN", "") ?: ""
    val username = sharedPreferences.getString("USERNAME", "") ?: ""

    // Configuración de PayPal usando las credenciales del archivo
    val payPalConfig = remember {
        Log.d(TAG, "Creando configuración de PayPal...")
        try {
            val config = PayPalConfiguration()
                .environment(
                    if (PayPalConfig.isSandbox()) {
                        Log.d(TAG, "Usando ambiente SANDBOX")
                        PayPalConfiguration.ENVIRONMENT_SANDBOX
                    } else {
                        Log.d(TAG, "Usando ambiente PRODUCTION")
                        PayPalConfiguration.ENVIRONMENT_PRODUCTION
                    }
                )
                .clientId(PayPalConfig.getClientId())
                .acceptCreditCards(true)
                .merchantName("SocialMe")
                .merchantPrivacyPolicyUri(Uri.parse("https://example.com/privacy"))
                .merchantUserAgreementUri(Uri.parse("https://example.com/terms"))
            Log.d(TAG, "Configuración de PayPal creada exitosamente")
            config
        } catch (e: Exception) {
            Log.e(TAG, "Error creando configuración de PayPal", e)
            throw e
        }
    }

    val payPalLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Resultado de PayPal recibido: ${result.resultCode}")

        when (result.resultCode) {
            Activity.RESULT_OK -> {
                Log.d(TAG, "Pago exitoso")
                val confirm = result.data?.getParcelableExtra<PaymentConfirmation>(PaymentActivity.EXTRA_RESULT_CONFIRMATION)
                if (confirm != null) {
                    try {
                        val paymentDetails = confirm.toJSONObject().getJSONObject("response")
                        val paymentId = paymentDetails.getString("id")
                        Log.d(TAG, "PaymentID: $paymentId")

                        // Pago exitoso, actualizar premium
                        actualizarPremiumEnServidor(
                            context = context,
                            username = username,
                            token = token,
                            paymentId = paymentId,
                            onSuccess = {
                                Log.d(TAG, "Premium actualizado exitosamente")
                                showSuccessDialog = true
                            },
                            onError = {
                                Log.e(TAG, "Error actualizando premium: $it")
                                errorMessage = it
                            }
                        )
                    } catch (e: JSONException) {
                        Log.e(TAG, "Error procesando respuesta de PayPal", e)
                        errorMessage = "Error procesando el pago: ${e.message}"
                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                Log.d(TAG, "Pago cancelado por el usuario")
                Toast.makeText(context, "Pago cancelado", Toast.LENGTH_SHORT).show()
            }
            PaymentActivity.RESULT_EXTRAS_INVALID -> {
                Log.e(TAG, "Configuración de pago inválida")
                errorMessage = "Configuración de pago inválida"
            }
            else -> {
                Log.w(TAG, "Resultado desconocido: ${result.resultCode}")
            }
        }
        isLoading = false
    }

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
                    } else {
                        Log.w(TAG, "Error verificando usuario: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error verificando usuario", e)
                }
            }
        }
    }

    // Iniciar servicio de PayPal solo si está configurado
    LaunchedEffect(isPayPalConfigured) {
        if (isPayPalConfigured) {
            try {
                Log.d(TAG, "Iniciando servicio de PayPal...")
                val intent = Intent(context, PayPalService::class.java)
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfig)
                context.startService(intent)
                Log.d(TAG, "Servicio de PayPal iniciado exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "Error iniciando servicio de PayPal", e)
                errorMessage = "Error iniciando PayPal: ${e.message}"
            }
        } else {
            Log.d(TAG, "PayPal no configurado, saltando inicio de servicio")
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

        // Contenido principal
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
                        text = "Soporte prioritario",
                        icon = "🛟"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.cyanSecundario).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        color = colorResource(R.color.textoSecundario),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Precio centrado
                    Text(
                        text = "€$PREMIUM_PRICE_EUR",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.azulPrimario),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = if (isPayPalConfigured) "Pago seguro con PayPal Sandbox" else "PayPal no configurado - Solo demostración",
                        fontSize = 14.sp,
                        color = colorResource(R.color.textoSecundario),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
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
                    onClick = {
                        if (isPayPalConfigured) {
                            Log.d(TAG, "Iniciando proceso de pago...")
                            showConfirmDialog = true
                        } else {
                            val error = "PayPal no está configurado correctamente. Verifica el archivo paypal-config.properties"
                            Log.w(TAG, error)
                            errorMessage = error
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPayPalConfigured) colorResource(R.color.azulPrimario) else Color.Gray
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
                            text = if (isPayPalConfigured) "Comprar Premium con PayPal" else "PayPal no configurado",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información de seguridad
            Text(
                text = if (isPayPalConfigured) {
                    "Pago 100% ficticio con PayPal Sandbox\nDinero de prueba - No se cobrará dinero real\nCredenciales de prueba necesarias"
                } else {
                    "Para habilitar PayPal:\n1. Ve a developer.paypal.com\n2. Crea una app Sandbox\n3. Copia el ClientID a paypal-config.properties"
                },
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
                Text("¿Estás seguro de que deseas comprar SocialMe Premium por €$PREMIUM_PRICE_EUR?\n\nSerás redirigido a PayPal Sandbox (dinero ficticio).")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        Log.d(TAG, "Usuario confirmó compra, iniciando PayPal...")
                        iniciarPagoPayPal(
                            payPalLauncher = payPalLauncher,
                            payPalConfig = payPalConfig,
                            context = context,
                            onLoading = { isLoading = it }
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
                Text("¡Bienvenido a SocialMe Premium!\nYa puedes disfrutar de todas las funciones exclusivas.")
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
            Log.e(TAG, "Mostrando error: $message")
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

// Función para iniciar el pago de PayPal con logs detallados
private fun iniciarPagoPayPal(
    payPalLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    payPalConfig: PayPalConfiguration,
    context: Context,
    onLoading: (Boolean) -> Unit
) {
    val TAG = "iniciarPagoPayPal"
    onLoading(true)
    Log.d(TAG, "Iniciando proceso de pago con PayPal...")

    try {
        // Crear el pago
        Log.d(TAG, "Creando objeto de pago...")
        val payment = PayPalPayment(
            BigDecimal(PREMIUM_PRICE_EUR),
            "EUR",
            "SocialMe Premium - Upgrade",
            PayPalPayment.PAYMENT_INTENT_SALE
        )
        Log.d(TAG, "Pago creado: €$PREMIUM_PRICE_EUR EUR")

        // Crear intent para PayPal
        Log.d(TAG, "Creando intent para PayPal...")
        val intent = Intent(context, PaymentActivity::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfig)
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment)

        // Lanzar actividad de PayPal
        Log.d(TAG, "Lanzando actividad de PayPal...")
        payPalLauncher.launch(intent)
        Log.d(TAG, "PayPal lanzado exitosamente")
    } catch (e: Exception) {
        Log.e(TAG, "Error iniciando PayPal", e)
        onLoading(false)
        Toast.makeText(context, "Error iniciando PayPal: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// Función para actualizar premium en el servidor
private fun actualizarPremiumEnServidor(
    context: Context,
    username: String,
    token: String,
    paymentId: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val TAG = "actualizarPremiumEnServidor"
    Log.d(TAG, "Actualizando estado premium en servidor...")

    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()
            Log.d(TAG, "Enviando petición al servidor...")
            val response = apiService.actualizarPremium("Bearer $token", username)

            withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Premium actualizado exitosamente")
                    // Actualizar SharedPreferences
                    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putBoolean("PREMIUM", true)
                        apply()
                    }
                    onSuccess()
                } else {
                    Log.e(TAG, "Error del servidor: ${response.code()}")
                    onError("Error al procesar la compra: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error de conexión", e)
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                onError(ErrorUtils.parseErrorMessage(e.message ?: "Error de conexión"))
            }
        }
    }
}