package com.example.anibey_codex_tfg.ui.screens.home

import android.util.Log
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

    private val _forceLogout = MutableStateFlow(false)
    val forceLogout: StateFlow<Boolean> = _forceLogout

    init {
        observeUserProfile()
        observeSecurityEvents()
    }

    /**
     * Escucha cambios en Firestore en tiempo real para reflejar cambios 
     * hechos desde este u otros dispositivos.
     */
    private fun observeUserProfile() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                // Suscripción en tiempo real a Firestore
                val docRef = db.collection("users").document(user.uid)
                docRef.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && snapshot.exists()) {
                        val profile = snapshot.toObject(UserProfile::class.java)
                        _userProfile.value = profile
                        
                        // Opcional: Sincronizamos el DataStore local para consistencia offline
                        profile?.let {
                            viewModelScope.launch { sessionDataStore.saveSession(it) }
                        }
                    }
                }
            } else {
                // Si no hay Firebase, intentamos cargar lo último del DataStore
                sessionDataStore.userData.collectLatest { 
                    _userProfile.value = it 
                }
            }
        }
    }

    /**
     * Escucha eventos de seguridad como cambios de email en otros dispositivos.
     * Si detecta un cambio de email, hace logout automáticamente.
     */
    private fun observeSecurityEvents() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                val eventsRef = db.collection("users")
                    .document(user.uid)
                    .collection("security_events")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)

                eventsRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("HomeViewModel", "Error escuchando eventos de seguridad: ${error.message}")
                        return@addSnapshotListener
                    }

                    snapshot?.documentChanges?.forEach { change ->
                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            val data = change.document.data
                            val eventType = data["eventType"] as? String
                            val deviceId = data["deviceId"] as? String
                            val currentDeviceId = android.os.Build.ID

                            Log.d("HomeViewModel", "Evento de seguridad detectado: $eventType desde dispositivo $deviceId")

                            // Si el cambio fue hecho desde OTRO dispositivo, hacer logout
                            if (eventType == "email_changed" && deviceId != currentDeviceId) {
                                Log.w("HomeViewModel", "Cambio de email detectado desde otro dispositivo. Haciendo logout...")
                                performForceLogout()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun performForceLogout() {
        viewModelScope.launch {
            try {
                auth.signOut()
                sessionDataStore.clearSession()
                _forceLogout.value = true
                Log.d("HomeViewModel", "Logout forzado completado")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error durante logout forzado: ${e.message}")
                sessionDataStore.clearSession()
                _forceLogout.value = true
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