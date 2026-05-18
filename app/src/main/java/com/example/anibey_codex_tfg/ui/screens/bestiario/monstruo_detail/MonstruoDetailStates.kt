package com.example.anibey_codex_tfg.ui.screens.bestiario.monstruo_detail

import com.example.anibey_codex_tfg.domain.model.Monstruo

sealed interface MonstruoDetailStates {
    data object Loading : MonstruoDetailStates
    data class Success(val monstruo: Monstruo) : MonstruoDetailStates
    data class Error(val message: String) : MonstruoDetailStates
}
