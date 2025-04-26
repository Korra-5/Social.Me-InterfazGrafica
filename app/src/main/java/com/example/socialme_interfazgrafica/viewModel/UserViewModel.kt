package com.example.socialme_interfazgrafica.viewModel

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialme_interfazgrafica.data.RetrofitService
import com.example.socialme_interfazgrafica.model.Direccion
import com.example.socialme_interfazgrafica.model.LoginResponse
import com.example.socialme_interfazgrafica.model.RegistroResponse
import com.example.socialme_interfazgrafica.model.UsuarioLoginDTO
import com.example.socialme_interfazgrafica.model.UsuarioRegisterDTO
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.StandardCharsets

sealed class RegistroState {
    object Initial : RegistroState()  // Nuevo estado inicial
    object Loading : RegistroState()
    data class Success(val data: RegistroResponse) : RegistroState()
    data class Error(val code: Int, val message: String) : RegistroState()
}

sealed class LoginState {
    object Initial : LoginState()  // Nuevo estado inicial
    object Loading : LoginState()
    data class Success(val token: String, val role: String?) : LoginState()
    data class Error(val code: Int, val message: String) : LoginState()
}

class UserViewModel : ViewModel() {
    private val apiService = RetrofitService.RetrofitServiceFactory.makeRetrofitService()

    // Estados LiveData que la UI puede observar
    private val _registroState = MutableLiveData<RegistroState>(RegistroState.Initial) // Estado inicial
    val registroState: LiveData<RegistroState> = _registroState

    private val _loginState = MutableLiveData<LoginState>(LoginState.Initial) // Estado inicial
    val loginState: LiveData<LoginState> = _loginState

    private val _registroExitoso = MutableLiveData<Boolean>()
    val registroExitoso: LiveData<Boolean> = _registroExitoso

    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> = _mensajeError

    private val _tokenLogin = MutableLiveData<String>()
    val tokenLogin: LiveData<String> = _tokenLogin

    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> = _userRole

    // Método para decodificar el JWT y extraer el username y el rol
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

    fun registrarUsuario(
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
            fotoPerfil = fotoPerfil
        )

        Log.d("RegistroVM", "Intentando registrar usuario: $usuario")

        viewModelScope.launch {
            try {
                val response = apiService.insertUser(usuario)

                if (response.isSuccessful) {
                    Log.d("RegistroVM", "Registro exitoso: ${response.body()}")
                    _registroState.value = RegistroState.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("RegistroVM", "Error en registro: Código ${response.code()}, Mensaje: $errorBody")
                    _registroState.value = RegistroState.Error(response.code(), errorBody)
                }
            } catch (e: Exception) {
                Log.e("RegistroVM", "Excepción en registro: ${e.message}", e)
                _registroState.value = RegistroState.Error(-1, e.message ?: "Error desconocido")
            }
        }
    }

    fun login(username: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val usuario = UsuarioLoginDTO(
                    username = username,
                    password = password
                )

                val response = apiService.loginUser(usuario)

                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        val token = loginResponse.token
                        val (_, role) = decodeJwt(token)

                        _userRole.value = role ?: "USER"
                        _loginState.value = LoginState.Success(token, role)
                        _tokenLogin.value = token
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

    /**
     * Resetea el estado de login para evitar inicios de sesión automáticos indeseados
     * cuando el usuario cierra sesión
     */
    fun resetLoginState() {
        _loginState.value = LoginState.Initial
        _tokenLogin.value = ""
        _userRole.value = ""

        // También podemos limpiar los mensajes de error si hay alguno
        _mensajeError.value = ""

        Log.d("UserViewModel", "Estado de login reseteado correctamente")
    }
}