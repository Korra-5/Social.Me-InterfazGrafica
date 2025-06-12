package com.example.socialme_interfazgrafica.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.Coordenadas
import com.example.socialme_interfazgrafica.model.Direccion
import com.example.socialme_interfazgrafica.model.LoginResponse
import com.example.socialme_interfazgrafica.model.UsuarioLoginDTO
import com.example.socialme_interfazgrafica.model.UsuarioRegisterDTO
import com.example.socialme_interfazgrafica.model.VerificacionDTO
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.nio.charset.StandardCharsets

sealed class RegistroState {
    object Initial : RegistroState()
    object Loading : RegistroState()
    data class Success(val message: String, val email: String) : RegistroState()
    data class Error(val code: Int, val message: String) : RegistroState()
}

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    data class Success(val token: String, val role: String?) : LoginState()
    data class Error(val code: Int, val message: String) : LoginState()
}

sealed class VerificacionState {
    object Initial : VerificacionState()
    object Loading : VerificacionState()
    data class Success(val message: String) : VerificacionState()
    data class Error(val message: String) : VerificacionState()
}

class UserViewModel : ViewModel() {
    private val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    private val _registroState = MutableLiveData<RegistroState>(RegistroState.Initial)
    val registroState: LiveData<RegistroState> = _registroState

    private val _loginState = MutableLiveData<LoginState>(LoginState.Initial)
    val loginState: LiveData<LoginState> = _loginState

    private val _verificacionState = MutableLiveData<VerificacionState>(VerificacionState.Initial)
    val verificacionState: LiveData<VerificacionState> = _verificacionState

    private val _registroExitoso = MutableLiveData<Boolean>()
    val registroExitoso: LiveData<Boolean> = _registroExitoso

    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> = _mensajeError

    private val _tokenLogin = MutableLiveData<String>()
    val tokenLogin: LiveData<String> = _tokenLogin

    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> = _userRole

    private val _ubicacionActual = MutableLiveData<Coordenadas?>()
    val ubicacionActual: LiveData<Coordenadas?> = _ubicacionActual

    private fun decodeJwt(token: String): Pair<String, String?> {
        try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("JWT_DECODE", "Token inválido: no tiene 3 partes")
                return Pair("", null)
            }

            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedPayload = String(decodedBytes, StandardCharsets.UTF_8)

            val jsonPayload = JSONObject(decodedPayload)
            val username = jsonPayload.optString("sub", "")
            val roles = jsonPayload.optString("roles", "")

            val role = when {
                roles.contains("ROLE_ADMIN") -> "ADMIN"
                roles.contains("ADMIN") -> "ADMIN"
                roles.contains("ROLE_USER") -> "USER"
                roles.contains("USER") -> "USER"
                else -> null
            }

            return Pair(username, role)
        } catch (e: Exception) {
            Log.e("JWT_DECODE", "Error decodificando token: ${e.message}")
            return Pair("", null)
        }
    }

    fun obtenerUbicacionActual(context: Context, onComplete: (Boolean) -> Unit) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val coordenadas = Coordenadas(
                            latitud = location.latitude.toString(),
                            longitud = location.longitude.toString()
                        )
                        _ubicacionActual.value = coordenadas
                        Log.d("UserViewModel", "Ubicación obtenida: Lat=${location.latitude}, Long=${location.longitude}")
                        onComplete(true)
                    } else {
                        Log.e("UserViewModel", "No se pudo obtener la ubicación actual")
                        _ubicacionActual.value = null
                        onComplete(false)
                    }
                }.addOnFailureListener { exception ->
                    Log.e("UserViewModel", "Error al obtener ubicación: ${exception.message}", exception)
                    _ubicacionActual.value = null
                    onComplete(false)
                }
            } else {
                Log.d("UserViewModel", "No hay permisos de ubicación")
                _ubicacionActual.value = null
                onComplete(false)
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Excepción al obtener ubicación: ${e.message}", e)
            _ubicacionActual.value = null
            onComplete(false)
        }
    }

    private suspend fun convertirUriABase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error convirtiendo URI a Base64: ${e.message}")
            null
        }
    }

    fun registrarUsuario(
        context: Context,
        username: String,
        email: String,
        password: String,
        passwordRepeat: String,
        nombre: String,
        apellidos: String,
        descripcion: String,
        intereses: List<String>,
        fotoPerfil: String,
        municipio: String,
        provincia: String,
        rol: String? = "USER"
    ) {
        _registroState.value = RegistroState.Loading

        val direccion = Direccion(
            municipio = municipio,
            provincia = provincia
        )

        viewModelScope.launch {
            val fotoPerfilBase64 = if (fotoPerfil.isNotEmpty()) {
                try {
                    val uri = Uri.parse(fotoPerfil)
                    convertirUriABase64(context, uri)
                } catch (e: Exception) {
                    Log.e("RegistroVM", "Error procesando imagen: ${e.message}")
                    null
                }
            } else null

            val usuario = UsuarioRegisterDTO(
                username = username,
                email = email,
                password = password,
                passwordRepeat = passwordRepeat,
                rol = rol,
                direccion = direccion,
                nombre = nombre,
                apellidos = apellidos,
                descripcion = descripcion,
                intereses = intereses,
                fotoPerfil = "",
                fotoPerfilBase64 = fotoPerfilBase64
            )

            Log.d("RegistroVM", "Intentando iniciar registro para usuario: $usuario")

            try {
                val response = apiService.iniciarRegistro(usuario)

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        Log.d("RegistroVM", "Registro iniciado exitosamente: $result")
                        _registroState.value = RegistroState.Success(
                            result["message"] ?: "Código enviado",
                            result["email"] ?: email
                        )
                    } ?: run {
                        _registroState.value = RegistroState.Error(-1, "Respuesta vacía del servidor")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("RegistroVM", "Error en inicio de registro: Código ${response.code()}, Mensaje: $errorBody")
                    _registroState.value = RegistroState.Error(response.code(), errorBody)
                }
            } catch (e: Exception) {
                Log.e("RegistroVM", "Excepción en inicio de registro: ${e.message}", e)
                _registroState.value = RegistroState.Error(-1, e.message ?: "Error desconocido")
            }
        }
    }

    fun login(context: Context, username: String, password: String) {
        _loginState.value = LoginState.Loading

        obtenerUbicacionActual(context) { success ->
            val coordenadas = if (success) {
                _ubicacionActual.value
            } else {
                Coordenadas("40.41", "-3.7") //Coordenas de Madrid Centro
            }

            realizarLogin(username, password, coordenadas)
        }
    }

    private fun realizarLogin(username: String, password: String, coordenadas: Coordenadas?) {
        viewModelScope.launch {
            try {
                val usuario = UsuarioLoginDTO(
                    username = username,
                    password = password,
                    coordenadas = coordenadas
                )

                val response = apiService.loginUser(usuario)

                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        val token = loginResponse.token
                        val (_, role) = decodeJwt(token)

                        _userRole.value = role ?: "USER"
                        _loginState.value = LoginState.Success(token, role)
                        _tokenLogin.value = token

                        Log.d("UserViewModel", "Login exitoso. Token: $token, Rol: $role")
                        if (coordenadas != null) {
                            Log.d("UserViewModel", "Coordenadas enviadas: Lat=${coordenadas.latitud}, Long=${coordenadas.longitud}")
                        }
                    } ?: run {
                        val errorMsg = "Respuesta vacía del servidor"
                        _loginState.value = LoginState.Error(response.code(), errorMsg)
                        _tokenLogin.value = ""
                        _userRole.value = ""
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                        404 -> "Usuario no encontrado"
                        500 -> "Error del servidor"
                        else -> response.errorBody()?.string() ?: "Error de autenticación"
                    }

                    _loginState.value = LoginState.Error(response.code(), errorMsg)
                    _tokenLogin.value = ""
                    _userRole.value = ""
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("timeout") == true -> "Tiempo de espera agotado"
                    e.message?.contains("Unable to resolve host") == true -> "Sin conexión a internet"
                    else -> e.message ?: "Error de conexión"
                }

                _loginState.value = LoginState.Error(-1, errorMsg)
                _tokenLogin.value = ""
                _userRole.value = ""
            }
        }
    }

    fun verificarCodigo(email: String, codigo: String, onComplete: (Boolean) -> Unit) {
        _verificacionState.value = VerificacionState.Loading

        viewModelScope.launch {
            try {
                val verificacionDTO = VerificacionDTO(email = email, codigo = codigo)
                val response = apiService.completarRegistro(verificacionDTO)

                if (response.isSuccessful) {
                    response.body()?.let { usuario ->
                        _verificacionState.value = VerificacionState.Success("¡Usuario creado exitosamente!")
                        Log.d("UserViewModel", "Usuario creado exitosamente: ${usuario.username}")
                        onComplete(true)
                    } ?: run {
                        _verificacionState.value = VerificacionState.Error("Respuesta vacía del servidor")
                        onComplete(false)
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error al verificar código"
                    _verificacionState.value = VerificacionState.Error(errorMsg)
                    Log.e("UserViewModel", "Error al verificar código: $errorMsg")
                    onComplete(false)
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Error de conexión"
                _verificacionState.value = VerificacionState.Error(errorMsg)
                Log.e("UserViewModel", "Error al verificar código: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun verificarCodigoModificacion(context: Context, email: String, codigo: String, onComplete: (Boolean) -> Unit) {
        _verificacionState.value = VerificacionState.Loading

        viewModelScope.launch {
            try {
                val verificacionDTO = VerificacionDTO(email = email, codigo = codigo)

                val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val authToken = sharedPreferences.getString("TOKEN", "") ?: ""

                if (authToken.isEmpty()) {
                    _verificacionState.value = VerificacionState.Error("No se encontró token de autenticación. Inicia sesión nuevamente.")
                    onComplete(false)
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    apiService.completarModificacionUsuario(
                        token = "Bearer $authToken",
                        verificacionDTO = verificacionDTO
                    )
                }

                if (response.isSuccessful) {
                    response.body()?.let { usuario ->
                        val currentUsername = sharedPreferences.getString("USERNAME", "")
                        if (usuario.username != currentUsername) {
                            val editor = sharedPreferences.edit()
                            editor.putString("USERNAME", usuario.username)
                            editor.apply()
                            Log.d("UserViewModel", "Username actualizado en SharedPreferences: ${usuario.username}")
                        }

                        _verificacionState.value = VerificacionState.Success("Perfil actualizado correctamente")
                        Log.d("UserViewModel", "Usuario modificado exitosamente: ${usuario.username}")
                        onComplete(true)
                    } ?: run {
                        _verificacionState.value = VerificacionState.Error("Respuesta vacía del servidor")
                        onComplete(false)
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Código de verificación incorrecto o expirado"
                        401 -> "Token de autenticación inválido. Inicia sesión nuevamente."
                        404 -> "No se encontraron datos de modificación"
                        else -> "Error al verificar el código: ${response.code()}"
                    }
                    _verificacionState.value = VerificacionState.Error(errorMsg)
                    Log.e("UserViewModel", "Error al verificar código modificación: $errorMsg")
                    onComplete(false)
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("timeout") == true -> "Tiempo de espera agotado. Inténtalo de nuevo."
                    e.message?.contains("network") == true -> "Error de conexión. Verifica tu internet."
                    else -> "Error al verificar el código: ${e.message}"
                }
                _verificacionState.value = VerificacionState.Error(errorMsg)
                Log.e("UserViewModel", "Error al verificar código modificación: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun reenviarCodigo(email: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.reenviarCodigo(email)
                onComplete(response.isSuccessful && response.body() == true)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error al reenviar código: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Initial
        _tokenLogin.value = ""
        _userRole.value = ""
        _ubicacionActual.value = null
        _mensajeError.value = ""
        Log.d("UserViewModel", "Estado de login reseteado correctamente")
    }

    fun resetRegistroState() {
        _registroState.value = RegistroState.Initial
        _verificacionState.value = VerificacionState.Initial
        Log.d("UserViewModel", "Estado de registro reseteado correctamente")
    }
}