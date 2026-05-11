package com.example.anibey_codex_tfg.domain.model

data class Monstruo(
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
