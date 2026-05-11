package com.example.anibey_codex_tfg.ui.screens.bestiario

import com.example.anibey_codex_tfg.domain.model.Monstruo

sealed interface BestiarioStates {
    data object Loading : BestiarioStates
    data class Success(val monstruos: List<Monstruo>) : BestiarioStates
    data class Error(val message: String) : BestiarioStates
    data object Empty : BestiarioStates
}
