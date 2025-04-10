package com.example.socialme_interfazgrafica.model

data class UsuarioDTO(
    val username: String,
    val email: String,
    val nombre: String,
    val apellido: String,
    val intereses: List<String>,
    val fotoPerfilId: String,
    val direccion: Direccion,
    val descripcion: String
)