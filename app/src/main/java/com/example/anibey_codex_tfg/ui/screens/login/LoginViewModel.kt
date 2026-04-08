package com.example.anibey_codex_tfg.ui.screens.login

import LoginState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    // --- MANEJO DE INPUTS ---

    fun onEmailChange(newValue: String) {
        state = state.copy(email = newValue, emailError = null)
    }

    fun onPasswordChange(newValue: String) {
        state = state.copy(password = newValue, passwordError = null)
    }

    fun toggleRecoveryMode(enabled: Boolean) {
        state = state.copy(
            isRecoveryMode = enabled,
            isRecoveryMailSent = false,
            emailError = null
        )
    }

    // --- LÓGICA DE AUTENTICACIÓN ---

    /**
     * Intenta iniciar sesión con Email y Contraseña.
     * Si tiene éxito, busca el perfil en Firestore y lo guarda en DataStore.
     */
    fun onLoginSubmit(onSuccess: () -> Unit) {
        if (!validateLoginForm()) return

        state = state.copy(isLoading = true)

        auth.signInWithEmailAndPassword(state.email, state.password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    fetchUserProfileAndNavigate(uid, onSuccess)
                } else {
                    state = state.copy(isLoading = false, emailError = "Error de identidad")
                }
            }
            .addOnFailureListener {
                state = state.copy(
                    isLoading = false,
                    emailError = "El vínculo ha fallado: credenciales incorrectas"
                )
            }
    }

    /**
     * Envía un correo de recuperación de contraseña a través de Firebase.
     */
    fun sendRecoveryEmail() {
        if (state.email.isBlank()) {
            state = state.copy(emailError = "Escribe tu e-mail para el susurro")
            return
        }

        state = state.copy(isLoading = true)
        auth.sendPasswordResetEmail(state.email)
            .addOnSuccessListener {
                state = state.copy(isLoading = false, isRecoveryMailSent = true)
            }
            .addOnFailureListener {
                state = state.copy(isLoading = false, emailError = "El vacío no responde a este e-mail")
            }
    }

    // --- FUNCIONES DE APOYO ---

    private fun validateLoginForm(): Boolean {
        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
        val isPasswordValid = state.password.length >= 6

        state = state.copy(
            emailError = if (!isEmailValid) "E-mail no válido" else null,
            passwordError = if (!isPasswordValid) "La contraseña es demasiado corta" else null
        )

        return isEmailValid && isPasswordValid
    }

    private fun fetchUserProfileAndNavigate(uid: String, onSuccess: () -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java)
                if (profile != null) {
                    viewModelScope.launch {
                        sessionDataStore.saveSession(profile)
                        state = state.copy(isLoading = false)
                        onSuccess()
                    }
                } else {
                    state = state.copy(isLoading = false, emailError = "Perfil no encontrado en Gaia")
                }
            }
            .addOnFailureListener {
                state = state.copy(isLoading = false, emailError = "Error al conectar con el Códice")
            }
    }
}