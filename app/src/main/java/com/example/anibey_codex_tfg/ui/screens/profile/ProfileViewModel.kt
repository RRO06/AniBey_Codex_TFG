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
                // Handle error
            }
        }
    }

    fun onUsernameChange(newValue: String) {
        state = state.copy(username = newValue, usernameError = null)
    }

    fun onEmailChange(newValue: String) {
        state = state.copy(email = newValue, emailError = null)
    }

    fun onPasswordChange(newValue: String) {
        state = state.copy(password = newValue, passwordError = null)
    }

    fun onCurrentPasswordChange(newValue: String) {
        state = state.copy(currentPassword = newValue, currentPasswordError = null)
    }

    fun onPhotoChange(newValue: String?) {
        state = state.copy(photoUrl = newValue)
    }

    fun uploadPhoto(imageUri: Uri) {
        val user = auth.currentUser ?: return
        val photoRef = storage.reference.child("users/${user.uid}/profile.jpg")

        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                photoRef.putFile(imageUri).await()
                val downloadUrl = photoRef.downloadUrl.await()
                state = state.copy(photoUrl = downloadUrl.toString(), isLoading = false)
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    emailError = "Error al subir imagen: ${e.localizedMessage}"
                )
            }
        }
    }

    fun saveProfile() {
        if (state.username.isBlank()) {
            state = state.copy(usernameError = "El apodo no puede estar vacío")
            return
        }

        state = state.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: return@launch

                // Reauthenticate if email or password is changing
                val needsReauth = state.email != user.email || state.password.isNotBlank()
                if (needsReauth) {
                    if (state.currentPassword.isBlank()) {
                        state = state.copy(
                            isLoading = false,
                            currentPasswordError = "Contraseña actual requerida para cambios sensibles"
                        )
                        return@launch
                    }
                    val credential = EmailAuthProvider.getCredential(user.email!!, state.currentPassword)
                    user.reauthenticate(credential).await()
                }

                // Update email if changed
                if (state.email != user.email) {
                    user.updateEmail(state.email).await()
                }

                // Update password if provided
                if (state.password.isNotBlank()) {
                    user.updatePassword(state.password).await()
                }

                // Update Firestore
                val profile = UserProfile(
                    email = state.email,
                    username = state.username,
                    photoUrl = state.photoUrl
                )
                db.collection("users").document(user.uid).set(profile).await()

                // Update session
                sessionDataStore.saveSession(profile)

                state = state.copy(isLoading = false, updateSuccess = true)
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    emailError = "Error al actualizar: ${e.localizedMessage}"
                )
            }
        }
    }
}
