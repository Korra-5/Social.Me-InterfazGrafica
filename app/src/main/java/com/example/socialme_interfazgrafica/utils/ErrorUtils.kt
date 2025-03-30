package com.example.socialme_interfazgrafica.utils

//Objeto que traduce los errores de la API
object ErrorUtils {
    fun parseErrorMessage(errorMsg: String): String {
        return when {
            // Error de credenciales incorrectas
            errorMsg.contains("Tiempo de espera agotado")->
                "Conexión fallida, inténtelo de nuevo"


            // Error de credenciales incorrectas
            errorMsg.contains("401") ||
                    errorMsg.contains("Unauthorized") ||
                    errorMsg.contains("Credenciales") ->
                "Usuario o contraseñas incorrectos"

            // Error de usuario duplicado
            errorMsg.contains("400") && errorMsg.contains("ya está registrado") ->
                "Usuario ya registrado"

            // Validación de municipio
            errorMsg.lowercase().contains("400") &&
                    errorMsg.lowercase().contains("municipio") &&
                    errorMsg.lowercase().contains("incorrecto") ->
                "El municipio ingresado no es válido"

            // Validación de provincia
            errorMsg.lowercase().contains("400") &&
                    errorMsg.lowercase().contains("provincia") &&
                    errorMsg.lowercase().contains("no encontrada") ->
                "La provincia ingresada no es válida"

            // Error de municipio o provincia no encontrados (errores de la Geo API)
            errorMsg.contains("502") && errorMsg.contains("municipio") ->
                "Municipio no encontrado"

            errorMsg.contains("502") && errorMsg.contains("provincia") ->
                "Provincia no encontrada"

            errorMsg.contains("502") ->
                "Error de validación geográfica"

            // Error de timeout
            errorMsg.contains("timeout") || errorMsg.contains("timed out") ->
                "Conexión fallida, inténtelo de nuevo"

            // Errores de conexión
            errorMsg.contains("Unable to resolve host") ||
                    errorMsg.contains("Failed to connect") ||
                    errorMsg.contains("No address associated") ->
                "Error de conexión, compruebe su internet"

            // Otros errores HTTP
            errorMsg.contains("500") ->
                "Error del servidor, inténtelo más tarde"

            errorMsg.contains("403") ->
                "Acceso denegado"

            errorMsg.contains("404") ->
                "Servicio no disponible"

            // Mensaje por defecto para otros errores
            else -> "Error: " + (if (errorMsg.length > 80) errorMsg.substring(0, 80) + "..." else errorMsg)
        }
    }
}