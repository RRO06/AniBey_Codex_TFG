package com.example.anibey_codex_tfg.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    // Escuchamos cambios de invitado localmente
    val isGuest: Flow<Boolean> = sessionDataStore.isGuest

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    init {
        observeUserProfile()
    }

    /**
     * Escucha cambios en Firestore en tiempo real para reflejar cambios 
     * hechos desde este u otros dispositivos.
     */
    private fun observeUserProfile() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                val docRef = db.collection("users").document(user.uid)
                docRef.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && snapshot.exists()) {
                        val profile = snapshot.toObject(UserProfile::class.java)
                        _userProfile.value = profile
                        
                        profile?.let {
                            viewModelScope.launch { sessionDataStore.saveSession(it) }
                        }
                    }
                }
            } else {
                sessionDataStore.userData.collectLatest {
                    _userProfile.value = it 
                }
            }
        }
    }


    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            auth.signOut()
            sessionDataStore.clearSession()
            onSuccess()
        }
    }
}