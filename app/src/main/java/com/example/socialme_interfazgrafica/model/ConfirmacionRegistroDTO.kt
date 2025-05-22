package com.example.socialme_interfazgrafica.model

data class ConfirmacionRegistroDTO(
    val email: String,
    val codigo: String,
    val datosRegistro: UsuarioRegisterDTO
)