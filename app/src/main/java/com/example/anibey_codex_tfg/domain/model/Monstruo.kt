package com.example.anibey_codex_tfg.domain.model

import com.google.firebase.firestore.DocumentId

data class Monstruo(
    @DocumentId
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val nivel: Int = 0,
    val habitat: String = "",
    val imagenURL: String = "",
    val habilidades: List<String> = emptyList(),
    val debilidades: List<String> = emptyList()
)

enum class NivelPeligro {
    BAJO,
    MEDIO,
    ALTO,
    LEGENDARIO
}

/**
 * Propiedad de extensión para obtener el NivelPeligro basado en el nivel numérico.
 * Se define fuera del data class para que Firebase no intente guardarlo en la base de datos.
 */
val Monstruo.nivelPeligro: NivelPeligro
    get() = when {
        nivel <= 3 -> NivelPeligro.BAJO
        nivel <= 5 -> NivelPeligro.MEDIO
        nivel <= 8 -> NivelPeligro.ALTO
        else -> NivelPeligro.LEGENDARIO
    }
