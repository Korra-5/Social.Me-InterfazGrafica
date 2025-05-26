package com.example.socialme_interfazgrafica.model

import java.util.Date

class DenunciaDTO(
    val motivo: String,
    val cuerpo: String,
    val nombreItemDenunciado: String,
    val tipoItemDenunciado: String,
    val fechaCreacion: Date,
    val solucionado: Boolean = false
) {
}