package com.example.anibey_codex_tfg.ui.screens.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import androidx.core.graphics.scale

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    var state by mutableStateOf(ProfileState())
        private set

    // Guardamos el estado inicial para comparar cambios
    private var originalUsername = ""
    private var originalEmail = ""
    private var originalPhotoUrl: String? = null

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
                    originalUsername = profile.username
                    originalEmail = user.email ?: ""
                    originalPhotoUrl = profile.photoUrl

                    state = state.copy(
                        username = originalUsername,
                        email = originalEmail,
                        photoUrl = originalPhotoUrl
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

    /**
     * Procesa la foto localmente y actualiza el estado, PERO NO GUARDA EN FIREBASE TODAVÍA.
     */
    fun uploadPhoto(imageUri: Uri) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true, generalError = null)
                
                val base64Image = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    
                    if (bitmap == null) throw Exception("Error al leer imagen")

                    val resizedBitmap = bitmap.scale(300, 300)
                    val outputStream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                    val byteArray = outputStream.toByteArray()
                    
                    Base64.encodeToString(byteArray, Base64.NO_WRAP)
                }

                // Solo actualizamos el estado visual
                state = state.copy(
                    photoUrl = base64Image,
                    isLoading = false
                )
                
                Log.d("ProfileViewModel", "Imagen cargada en memoria, pendiente de guardar")

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al procesar foto", e)
                state = state.copy(
                    isLoading = false,
                    generalError = "Error al procesar la foto: ${e.localizedMessage}"
                )
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        return state.username != originalUsername || 
               state.email != originalEmail || 
               state.photoUrl != originalPhotoUrl ||
               state.password.isNotEmpty()
    }

    fun onBackRequested(onConfirmBack: () -> Unit) {
        if (hasUnsavedChanges()) {
            state = state.copy(isDiscardDialogOpen = true)
        } else {
            onConfirmBack()
        }
    }

    fun onDismissDiscardDialog() {
        state = state.copy(isDiscardDialogOpen = false)
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

                if (state.email != user.email) {
                    user.updateEmail(state.email).await()
                }

                if (state.password.isNotBlank()) {
                    user.updatePassword(state.password).await()
                }

                // Guardamos en Firestore (incluyendo la foto si cambió)
                val profile = UserProfile(
                    email = state.email,
                    username = state.username,
                    photoUrl = state.photoUrl
                )
                db.collection("users").document(user.uid).set(profile).await()

                // Actualizamos sesión local
                sessionDataStore.saveSession(profile)

                // Actualizamos originales para que ya no detecte cambios
                originalUsername = state.username
                originalEmail = state.email
                originalPhotoUrl = state.photoUrl

                state = state.copy(
                    isLoading = false, 
                    updateSuccess = true, 
                    currentPassword = "",
                    password = ""
                )
                Log.d("ProfileViewModel", "Perfil guardado correctamente en Firebase y local")
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    generalError = "Error al guardar: ${e.localizedMessage}"
                )
            }
        }
    }
}