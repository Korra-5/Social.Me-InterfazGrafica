package com.example.socialme_interfazgrafica.model

import java.util.Date

data class MensajeDTO(
    val id: String?,
    val comunidadUrl: String,
    val username: String,
    val contenido: String,
    val fechaEnvio: Date,
    val leido: Boolean = false
)