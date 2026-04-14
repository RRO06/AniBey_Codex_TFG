package com.example.anibey_codex_tfg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _startDestination = MutableStateFlow<Screen?>(null)
    val startDestination: StateFlow<Screen?> = _startDestination

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // Limpiamos el modo invitado al arrancar
            sessionDataStore.clearGuestMode()
            
            // Obtenemos el primer valor para decidir la pantalla de inicio y paramos
            val userProfile = sessionDataStore.userData.first()
            
            _startDestination.value = if (userProfile != null) {
                Screen.Home
            } else {
                Screen.Welcome
            }
        }
    }
}