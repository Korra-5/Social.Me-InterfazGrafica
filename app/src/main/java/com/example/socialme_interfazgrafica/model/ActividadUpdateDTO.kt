package com.example.socialme_interfazgrafica.model

import java.util.Date

class ActividadUpdateDTO(
    val _id: String,
    val nombre: String,
    val descripcion: String,
    val fotosCarruselBase64: List<String>? = null,
    val fotosCarruselIds: List<String>? = null,
    val fechaInicio: Date,
    val fechaFinalizacion: Date,
    val coordenadas: Coordenadas?,
    var lugar: String
) {
}