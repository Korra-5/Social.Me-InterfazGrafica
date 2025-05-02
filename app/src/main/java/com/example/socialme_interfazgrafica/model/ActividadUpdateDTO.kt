package com.example.socialme_interfazgrafica.model

import java.util.Date

class ActividadUpdateDTO(
    val _id: String,
    val nombre: String,
    val descripcion: String,
    val fotosCarruselBase64: List<String>? = null,  // Used for receiving base64 images
    val fotosCarruselIds: List<String>? = null,     // Used if files already uploaded
    val fechaInicio: Date,
    val fechaFinalizacion: Date,
    val coordenadas: Coordenadas?,
    var lugar: String
) {
}