package com.example.socialme_interfazgrafica.model

//Plantilla para login exitoso
data class LoginResponse(
    val token: String,
    val message: String? = null
)