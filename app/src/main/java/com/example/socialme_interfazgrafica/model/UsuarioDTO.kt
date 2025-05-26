package com.example.socialme_interfazgrafica.model

data class UsuarioDTO(
    val username: String,
    val email: String,
    val intereses: List<String>,
    val nombre: String,
    val apellido: String,
    val fotoPerfilId: String?,
    val direccion: Direccion?,
    val descripcion: String,
    val premium:Boolean,
    val privacidadComunidades: String,
    val privacidadActividades: String,
    val radarDistancia:String
)