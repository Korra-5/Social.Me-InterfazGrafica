// model/NotificacionDTO.kt
package com.example.socialme_interfazgrafica.model

import java.util.Date

data class NotificacionDTO(
    val _id: String?,
    val tipo: String, // "ACTIVIDAD_PROXIMA", "ACTIVIDAD_INICIANDO", etc.
    val titulo: String,
    val mensaje: String,
    val entidadId: String?,
    val entidadNombre: String?,
    val fechaCreacion: Date,
    val leida: Boolean
)