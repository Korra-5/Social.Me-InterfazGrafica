package com.example.socialme_interfazgrafica.model

import java.util.Date

data class SolicitudAmistadDTO(
    val _id: String,
    val remitente: String,
    val destinatario: String,
    val fechaEnviada: Date? = null,
    val aceptada: Boolean = false
)