package com.example.socialme_interfazgrafica.model

class ComunidadCreateDTO(
    val url: String,
    val nombre: String,
    val descripcion: String,
    val intereses: List<String>,
    val fotoPerfilBase64: String? = null,  // Used for receiving base64 image
    val fotoPerfilId: String? = null,      // Used if file already uploaded
    val creador: String,
    val comunidadGlobal: Boolean,
    val privada: Boolean,
    val coordenadas: Coordenadas?,  // Coordenadas geográficas
    val codigoUnion:String?
) {

}