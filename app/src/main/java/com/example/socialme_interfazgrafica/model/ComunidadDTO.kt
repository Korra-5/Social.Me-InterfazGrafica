package com.example.socialme_interfazgrafica.model

import java.util.Date

data class ComunidadDTO(
    val url: String,
    val nombre: String,
    val descripcion: String,
    val intereses: List<String>,
    val fotoPerfilId: String, // Changed from fotoPerfil
    val fotoCarruselIds: List<String>?, // Changed from fotoCarrusel
    val creador: String,
    val administradores: List<String>?,
    val fechaCreacion: Date,
    val comunidadGlobal: Boolean,
    val privada: Boolean,
    val coordenadas: Coordenadas?,  // Coordenadas geográficas

)