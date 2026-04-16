package com.example.anibey_codex_tfg.ui.screens.lugares.lugares_detail

import com.example.anibey_codex_tfg.domain.model.Lugar

sealed interface LugarDetailState {
    data object Loading : LugarDetailState
    data class Success(val lugar: Lugar) : LugarDetailState
    data class Error(val message: String) : LugarDetailState
}