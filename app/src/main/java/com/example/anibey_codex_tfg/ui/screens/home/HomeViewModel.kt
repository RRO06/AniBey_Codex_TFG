package com.example.anibey_codex_tfg.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val auth: FirebaseAuth
) : ViewModel() {

    val userProfile: Flow<UserProfile?> = sessionDataStore.userData
    val isGuest: Flow<Boolean> = sessionDataStore.isGuest

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Limpia Firebase
            auth.signOut()
            // Limpia DataStore local (nombre, foto, isGuest flag)
            sessionDataStore.clearSession()
            onSuccess()
        }
    }
}
