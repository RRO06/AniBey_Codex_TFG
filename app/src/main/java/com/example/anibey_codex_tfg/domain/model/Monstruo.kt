package com.example.anibey_codex_tfg.domain.model

import com.google.firebase.firestore.DocumentId

data class Monstruo(
    @DocumentId
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val nivelPeligro: String = "",
    val habitat: String = "",
    val imagenURL: String = "",
    val habilidades: List<String> = emptyList(),
    val debilidades: List<String> = emptyList()
)
