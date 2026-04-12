package com.example.anibey_codex_tfg.ui.screens.profile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.domain.model.UserProfile
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    var state by mutableStateOf(ProfileState())
        private set

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(user.uid).get().await()
                val profile = doc.toObject(UserProfile::class.java)
                if (profile != null) {
                    state = state.copy(
                        username = profile.username,
                        email = user.email ?: "",
                        photoUrl = profile.photoUrl
                    )
                }
            } catch (e: Exception) {
                state = state.copy(generalError = "Error al cargar perfil: ${e.localizedMessage}")
            }
        }
    }

    fun onUsernameChange(newValue: String) {
        state = state.copy(username = newValue, usernameError = null, generalError = null)
    }

    fun onEmailChange(newValue: String) {
        state = state.copy(email = newValue, emailError = null, generalError = null)
    }

    fun onPasswordChange(newValue: String) {
        state = state.copy(password = newValue, passwordError = null, generalError = null)
    }

    fun onCurrentPasswordChange(newValue: String) {
        state = state.copy(currentPassword = newValue, currentPasswordError = null, generalError = null)
    }

    fun onPhotoChange(newValue: String?) {
        state = state.copy(photoUrl = newValue, generalError = null)
    }

    fun uploadPhoto(imageUri: Uri) {
        val user = auth.currentUser ?: return
        // Aseguramos que la ruta sea correcta y el archivo tenga extensión
        val photoRef = storage.reference.child("profiles/${user.uid}.jpg")

        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true, generalError = null)
                
                // Subir archivo
                photoRef.putFile(imageUri).await()
                
                // Obtener URL de descarga
                val downloadUrl = photoRef.downloadUrl.await()
                
                state = state.copy(
                    photoUrl = downloadUrl.toString(), 
                    isLoading = false
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    generalError = "Error en Storage: ${e.localizedMessage}"
                )
            }
        }
    }

    fun saveProfile() {
        if (state.username.isBlank()) {
            state = state.copy(usernameError = "El apodo no puede estar vacío")
            return
        }

        state = state.copy(isLoading = true, generalError = null)

        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: throw Exception("Usuario no autenticado")

                // Reautenticación para cambios críticos
                val needsReauth = state.email != user.email || state.password.isNotBlank()
                if (needsReauth) {
                    if (state.currentPassword.isBlank()) {
                        state = state.copy(
                            isLoading = false,
                            currentPasswordError = "Introduce tu contraseña actual para confirmar cambios"
                        )
                        return@launch
                    }
                    val credential = EmailAuthProvider.getCredential(user.email!!, state.currentPassword)
                    user.reauthenticate(credential).await()
                }

                // Actualizar Email
                if (state.email != user.email) {
                    user.updateEmail(state.email).await()
                }

                // Actualizar Password
                if (state.password.isNotBlank()) {
                    user.updatePassword(state.password).await()
                }

                // Actualizar Firestore
                val profile = UserProfile(
                    email = state.email,
                    username = state.username,
                    photoUrl = state.photoUrl
                )
                db.collection("users").document(user.uid).set(profile).await()

                // Sincronizar DataStore local
                sessionDataStore.saveSession(profile)

                state = state.copy(
                    isLoading = false, 
                    updateSuccess = true, 
                    currentPassword = "",
                    password = ""
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    generalError = "Error al guardar: ${e.localizedMessage}"
                )
            }
        }
    }
}