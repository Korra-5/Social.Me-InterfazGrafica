package com.example.socialme_interfazgrafica.model

//Plantilla para login exitoso
data class LoginResponse(
    val token: String,
    // Otros posibles campos si los hay
    val message: String? = null
)