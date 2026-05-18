package com.example.anibey_codex_tfg.domain.model

import com.google.firebase.firestore.DocumentId

data class Hechizo(
    @DocumentId
    val id: String = "",
    val nombre: String = "",
    val ramaMagia: String = "",
    val nivel: Int = 0,
    val tiempoCasteo: String = "",
    val zeonCost: Int = 0,
    val rango: String = "",
    val descripcion: String = "",
    val efecto: String = ""
)
