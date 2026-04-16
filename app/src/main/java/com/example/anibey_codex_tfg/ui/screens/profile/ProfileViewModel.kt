package com.example.anibey_codex_tfg.ui.screens.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.domain.model.UserProfile
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
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
                profile?.let {
                    originalUsername = it.username
                    originalEmail = user.email ?: ""
                    originalPhotoUrl = it.photoUrl

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

    /**
     * Procesa la foto de la galería y la guarda en memoria (Base64)
     */
    fun uploadPhoto(imageUri: Uri) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                val base64Image = processImageToBase64(imageUri)
                state = state.copy(photoUrl = base64Image, isLoading = false)
                Log.d("ProfileViewModel", "Imagen procesada con éxito")
            } catch (e: Exception) {
                state = state.copy(isLoading = false, generalError = "Error al procesar foto: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun processImageToBase64(uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (bitmap == null) throw Exception("No se pudo leer la imagen")

        val resizedBitmap = bitmap.scale(300, 300)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        
        Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun saveProfile() {
        if (!isUsernameValid()) return

        state = state.copy(isLoading = true, generalError = null)

        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
                val emailChanged = state.email != user.email
                val passwordChanged = state.password.isNotBlank()

                // 1. Seguridad: Re-autenticar si hay cambios críticos
                if (emailChanged || passwordChanged) {
                    performReauthentication(user)
                }

                // 2. Procesar cambio de Email (enviar verificación pero NO guardar aún)
                if (emailChanged) {
                    verifyAndPrepareNewEmail(user)
                    finalizeSaveProcess(emailChanged = true)
                    return@launch
                }

                // 3. Procesar cambio de Contraseña
                if (passwordChanged) {
                    performPasswordUpdate(user)
                }

                // 4. Persistir datos en Firestore y Local (solo si email NO cambió)
                persistAllChanges(user)

                // 5. Finalizar flujo
                finalizeSaveProcess(emailChanged = false)

            } catch (_: FirebaseAuthUserCollisionException) {
                state = state.copy(isLoading = false, emailError = "Este correo ya está vinculado a otra cuenta")
            } catch (e: Exception) {
                state = state.copy(isLoading = false, generalError = e.localizedMessage)
            }
        }
    }

    private fun isUsernameValid(): Boolean {
        return if (state.username.isBlank()) {
            state = state.copy(usernameError = "El apodo no puede estar vacío")
            false
        } else true
    }

    private suspend fun performReauthentication(user: FirebaseUser) {
        if (state.currentPassword.isBlank()) {
            state = state.copy(isLoading = false, currentPasswordError = "Contraseña actual obligatoria")
            throw Exception("Re-autenticación cancelada: falta contraseña")
        }
        val credential = EmailAuthProvider.getCredential(user.email!!, state.currentPassword)
        user.reauthenticate(credential).await()
    }

    private suspend fun verifyAndPrepareNewEmail(user: FirebaseUser) {
        if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            state = state.copy(isLoading = false, emailError = "Formato no válido")
            throw Exception("Email inválido")
        }

        // Comprobar si el email ya está en uso
        try {
            val providers = auth.fetchSignInMethodsForEmail(state.email).await()
            if (providers.signInMethods?.isNotEmpty() == true) {
                state = state.copy(isLoading = false, emailError = "Correo ya en uso")
                throw Exception("Email en uso")
            }
        } catch (_: Exception) {}

        // Enviar email de verificación pero NO actualizar aún
        user.verifyBeforeUpdateEmail(state.email).await()
    }

    private suspend fun performPasswordUpdate(user: FirebaseUser) {
        if (state.password.length < 6) {
            state = state.copy(isLoading = false, passwordError = "Mínimo 6 caracteres")
            throw Exception("Contraseña demasiado corta")
        }
        user.updatePassword(state.password).await()
    }

    private suspend fun persistAllChanges(user: FirebaseUser) {
        val updatedProfile = UserProfile(
            email = state.email,
            username = state.username,
            photoUrl = state.photoUrl
        )
        // Nube
        db.collection("users").document(user.uid).set(updatedProfile).await()
        // Local
        sessionDataStore.saveSession(updatedProfile)
    }


    private fun finalizeSaveProcess(emailChanged: Boolean) {
        if (emailChanged) {
            state = state.copy(
                isLoading = false,
                showEmailVerificationDialog = true,
                isCheckingEmailVerification = true
            )
        } else {
            originalUsername = state.username
            originalEmail = state.email
            originalPhotoUrl = state.photoUrl

            state = state.copy(
                isLoading = false,
                updateSuccess = true,
                currentPassword = "",
                password = ""
            )
        }
    }

    fun hasUnsavedChanges(): Boolean = 
        state.username != originalUsername || 
        state.email != originalEmail || 
        state.photoUrl != originalPhotoUrl ||
        state.password.isNotEmpty()

    fun onBackRequested(onConfirmBack: () -> Unit) {
        if (hasUnsavedChanges()) state = state.copy(isDiscardDialogOpen = true)
        else onConfirmBack()
    }

    fun onDismissDiscardDialog() {
        state = state.copy(isDiscardDialogOpen = false)
    }

    fun onDismissVerificationDialog() {
        state = state.copy(
            showEmailVerificationDialog = false,
            isCheckingEmailVerification = false,
            emailVerificationCountdown = 0,
            email = originalEmail
        )
    }

    fun onConfirmEmailVerification(onSuccess: () -> Unit) {
        state = state.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: run {
                    sessionDataStore.clearSession()
                    state = state.copy(isLoading = false)
                    onSuccess()
                    return@launch
                }

                user.reload().await()

                if (user.email == state.email) {
                    Log.d("ProfileViewModel", "Email verificado: ${user.email}")
                    persistAllChanges(user)
                    sessionDataStore.clearSession()
                    auth.signOut()
                    state = state.copy(
                        isLoading = false,
                        showEmailVerificationDialog = false,
                        updateSuccess = true,
                        currentPassword = "",
                        password = ""
                    )
                    onSuccess()
                } else {
                    state = state.copy(
                        isLoading = false,
                        generalError = "El correo aún no ha sido verificado. Intenta de nuevo."
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al verificar email: ${e.message}")
                state = state.copy(isLoading = false, generalError = "Error: ${e.localizedMessage}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                auth.signOut()
                sessionDataStore.clearSession()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al hacer logout", e)
                sessionDataStore.clearSession()
            }
        }
    }
}
