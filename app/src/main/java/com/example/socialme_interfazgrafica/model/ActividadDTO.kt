package com.example.socialme_interfazgrafica.model

import java.util.Date

class ActividadDTO(
    val nombre: String,
    val descripcion: String,
    val privada: Boolean,
    val creador: String,
    val fechaInicio: Date,
    val fechaFinalizacion: Date,
    val fotosCarruselIds: List<String>,
    val lugar: String,
){
}