package com.example.socialme_interfazgrafica.model

import java.util.Date

data class ActividadCreateDTO(
    val nombre: String,
    val descripcion: String,
    val comunidad: String,
    val creador: String,
    val fechaInicio: Date,
    val fechaFinalizacion: Date,
    val fotosCarruselBase64: List<String>? = null,
    val fotosCarruselIds: List<String>? = null,
    val privada: Boolean,
    val coordenadas: Coordenadas?,
    var lugar: String
){

}