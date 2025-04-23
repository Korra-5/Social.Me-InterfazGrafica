package com.example.socialme_interfazgrafica.model

import java.util.Date

data class ActividadCreateDTO (
    val nombre: String,
    val descripcion: String,
    val comunidad: String,
    val creador: String,
    val lugar: String,
    val fechaInicio: Date,
    val fechaFinalizacion: Date,
    val fotosCarruselBase64: List<String>? = null,  // Used for receiving base64 images
    val fotosCarruselIds: List<String>? = null,     // Used if files already uploaded
    val privada: Boolean
){

}