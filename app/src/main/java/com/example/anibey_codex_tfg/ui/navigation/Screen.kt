package com.example.anibey_codex_tfg.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Welcome : Screen

    @Serializable data object Login: Screen
    @Serializable data object Register: Screen
    @Serializable
    data object Home : Screen
    @Serializable
    data object Profile : Screen
    @Serializable
    data object Lugares : Screen
    @Serializable
    data object Bestiario : Screen
    @Serializable
    data object Grimorio : Screen // Nueva sección

    @Serializable
    data class LugarDetail(val lugarId: String) : Screen

    @Serializable
    data class MonstruoDetail(val monstruoId: String) : Screen

    @Serializable
    data class HechizoDetail(val spellId: String) : Screen // Detalle de hechizo
}