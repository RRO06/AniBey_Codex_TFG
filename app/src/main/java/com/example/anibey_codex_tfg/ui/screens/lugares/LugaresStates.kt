package com.example.anibey_codex_tfg.ui.screens.lugares

import com.example.anibey_codex_tfg.domain.model.Lugar

sealed class LugaresStates {
    data object Loading : LugaresStates()
    data class Error(val message: String) : LugaresStates()
    data object Empty : LugaresStates()
    data class Success(val lugares: List<Lugar>) : LugaresStates()
}





