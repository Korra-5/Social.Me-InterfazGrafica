package com.example.socialme_interfazgrafica.model

import java.util.Date

class ComunidadDTO(
    val url: String,
    val nombre: String,
    val descripcion: String,
    val intereses:List<String>,
    val fotoPerfil:String,
    val fotoCarrusel: List<String>?,
    val creador:String,
    val administradores: List<String>?,
    val fechaCreacion: Date,
    val comunidadGlobal:Boolean,
    val privada:Boolean
) {
}