package com.example.socialme_interfazgrafica.model

class DenunciaCreateDTO(
    val motivo: String,
    val cuerpo: String,
    val nombreItemDenunciado:String,
    val tipoItemDenunciado:String,
    val usuarioDenunciante:String
) {
}