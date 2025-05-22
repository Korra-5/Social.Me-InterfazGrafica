package com.example.socialme_interfazgrafica.model

data class ConfirmarCambioEmailDTO(
    val username: String,
    val nuevoEmail: String,
    val codigo: String
)