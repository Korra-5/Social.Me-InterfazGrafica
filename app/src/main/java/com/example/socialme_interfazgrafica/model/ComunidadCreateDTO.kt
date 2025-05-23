package com.example.socialme_interfazgrafica.model

class ComunidadCreateDTO(
    val url: String,
    val nombre: String,
    val descripcion: String,
    val intereses: List<String>,
    val fotoPerfilBase64: String? = null,
    val fotoPerfilId: String? = null,
    val creador: String,
    val comunidadGlobal: Boolean,
    val privada: Boolean,
    val coordenadas: Coordenadas?,  // Coordenadas geográficas
    val codigoUnion:String?
) {

}