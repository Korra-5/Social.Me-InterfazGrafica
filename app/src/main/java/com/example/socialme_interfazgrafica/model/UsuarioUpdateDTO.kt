package com.example.socialme_interfazgrafica.model

data class UsuarioUpdateDTO(
    val currentUsername: String,
    val newUsername: String?,
    val email: String,
    val nombre: String,
    val apellido: String,
    val descripcion: String,
    val intereses: List<String>,
    val fotoPerfilBase64: String?,
    val fotoPerfilId: String?,
    val direccion: Direccion
)