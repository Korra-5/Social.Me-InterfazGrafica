package com.example.socialme_interfazgrafica.model

//Clase que loguea un usuario
data class UsuarioLoginDTO (
    val username:String,
    val password:String,
    val coordenadas: Coordenadas? = null
)
