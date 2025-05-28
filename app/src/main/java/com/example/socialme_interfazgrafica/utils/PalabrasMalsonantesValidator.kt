package com.example.socialme_interfazgrafica.utils

object PalabrasMalsonantesValidator {

    private val palabrasMalsonantes = listOf(
        "cabron", "cabrones", "cabrona", "cabronas", "pendejo", "pendejos", "pendeja", "pendejas",
        "imbecil", "imbeciles", "gilipollas", "capullo", "capullos", "mamada", "mamadas", "pinche",
        "pinches", "chingar", "joder", "jodido", "jodidos", "jodida", "jodidas", "coño", "coños",
        "cojones", "hostia", "hostias", "puta", "putas", "puto", "putos", "zorra", "zorras",
        "zorro", "zorros", "maricon", "maricones", "marica", "maricas", "bolludo", "bolludos",
        "boluda", "bolludas", "pelotudo", "pelotudos", "pelotuda", "pelotudas", "tarado", "tarados",
        "tarada", "taradas", "subnormal", "subnormales", "mogolico", "mogolicos", "mogolica",
        "mogolicas", "hdp", "hijo de puta", "hijos de puta", "hija de puta", "hijas de puta",
        "que te jodan", "vete a la mierda", "mierda", "mierdas", "cagada", "cagadas", "cagar",
        "cagado", "cagados", "ojete", "ojetes", "culero", "culeros", "culera", "culeras",
        "verga", "vergas", "polla", "pollas", "chichi", "chichis", "teta", "tetas", "culo",
        "culos", "concha", "conchas", "chocho", "chochos", "coger", "follar", "culear", "garchar",
        "cachondo", "cachondos", "cachonda", "cachondas", "masturbacion", "masturbaciones",
        "masturbar", "prostituta", "prostitutas", "prostituto", "prostitutos", "fuck", "fucking",
        "fucked", "fucker", "fuckers", "shit", "shits", "shitty", "bitch", "bitches", "asshole",
        "assholes", "bastard", "bastards", "damn", "hell", "crap", "piss", "dickhead", "dickheads",
        "moron", "morons", "fag", "fags", "faggot", "faggots", "slut", "sluts", "whore", "whores",
        "dick", "dicks", "cock", "cocks", "pussy", "pussies", "boobs", "tits", "ass", "asses",
        "butt", "butts", "nude", "nudes", "naked", "porn", "pornography", "marihuana", "marijuana",
        "hierba", "hierbas", "porro", "porros", "canuto", "canutos", "coca", "cocaina", "heroina",
        "extasis", "perico", "pericos", "crack", "lsd", "acido", "acidos", "meta", "metanfetamina",
        "metanfetaminas", "anfetamina", "anfetaminas", "mdma", "ketamina", "ketaminas", "drogadicto",
        "drogadictos", "drogadicta", "drogadictas", "yonki", "yonkis", "junkie", "junkies", "dealer",
        "dealers", "narcotraficante", "narcotraficantes", "nazi", "nazis", "fascista", "fascistas",
        "terrorista", "terroristas", "bomba", "bombas", "explotar", "matar", "asesinar", "violacion",
        "violaciones", "violar", "violador", "violadores", "violadora", "violadoras", "maltrato",
        "maltratos", "maltratar", "suicidio", "suicidios", "suicidarse", "cortarse", "autolesion",
        "autolesiones", "gore", "snuff", "zoofilia", "pedofilo", "pedofilos", "pedofila", "pedofilas",
        "incesto", "incestos", "necrofilia"
    )

    /**
     * Valida si el texto contiene palabras malsonantes
     * @param texto El texto a validar
     * @return true si contiene palabras malsonantes, false si no
     */
    fun contienepalabrasmalsonantes(texto: String): Boolean {
        val textoLower = texto.lowercase().trim()

        return palabrasMalsonantes.any { palabra ->
            textoLower.contains(palabra)
        }
    }

    /**
     * Obtiene las palabras malsonantes encontradas en el texto
     * @param texto El texto a validar
     * @return Lista de palabras malsonantes encontradas
     */
    fun obtenerPalabrasMalsonantesEncontradas(texto: String): List<String> {
        val textoLower = texto.lowercase().trim()

        return palabrasMalsonantes.filter { palabra ->
            textoLower.contains(palabra)
        }
    }

    /**
     * Valida una lista de textos
     * @param textos Lista de textos a validar
     * @return true si alguno contiene palabras malsonantes
     */
    fun validarLista(textos: List<String>): Boolean {
        return textos.any { contienepalabrasmalsonantes(it) }
    }
}