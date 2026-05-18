package com.example.anibey_codex_tfg.domain.model

import com.google.firebase.firestore.DocumentId

data class Lugar(
    @DocumentId
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val tipo: String = "",
    val region: String = "",
    val imagenURL: String = "",
    val personajes: List<String> = emptyList(),
    val monstruos: List<String> = emptyList()
)

