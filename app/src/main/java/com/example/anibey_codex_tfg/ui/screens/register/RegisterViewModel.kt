package com.example.anibey_codex_tfg.ui.screens.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.data.local.datastore.SessionDataStore
import com.example.anibey_codex_tfg.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    var state by mutableStateOf(RegisterState())
        private set

    // --- MANEJO DE INPUTS ---

    fun onEmailChange(newValue: String) {
        state = state.copy(email = newValue, emailError = null)
    }

    fun onUsernameChange(newValue: String) {
        state = state.copy(username = newValue, usernameError = null)
    }

    fun onPasswordChange(newValue: String) {
        state = state.copy(password = newValue, passwordError = null)
    }

    // --- FASE 1: VERIFICACIÓN DEL EMAIL ---

    // --- FASE 1: VERIFICACIÓN DEL EMAIL ---
    fun sendVerificationEmail() {
        val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
        if (!emailValid) {
            state = state.copy(emailError = "El e-mail no es reconocido por Gaia")
            return
        }

        state = state.copy(isLoading = true)

        // Intentamos crear el usuario
        auth.createUserWithEmailAndPassword(state.email, "temp_pass_anima_123")
            .addOnSuccessListener { result ->
                // Caso A: Usuario nuevo creado con éxito
                sendEmailAndProgress(result.user)
            }
            .addOnFailureListener { exception ->
                if (exception is FirebaseAuthUserCollisionException) {
                    // Caso B: El usuario ya existe en Auth.
                    // Intentamos loguearlo con la pass temporal para ver si podemos reenviar
                    auth.signInWithEmailAndPassword(state.email, "temp_pass_anima_123")
                        .addOnSuccessListener { result ->
                            val user = result.user
                            if (user != null && !user.isEmailVerified) {
                                // El usuario existe pero NO está verificado: Reenviamos y avanzamos
                                sendEmailAndProgress(user)
                            } else {
                                // El usuario existe y SÍ está verificado: Debe ir a Login
                                state = state.copy(
                                    isLoading = false,
                                    emailError = "Este alma ya está vinculada. Ve al inicio de sesión."
                                )
                            }
                        }
                        .addOnFailureListener {
                            // Si falla el login temporal, es que el usuario existe con OTRA contraseña real
                            state = state.copy(
                                isLoading = false,
                                emailError = "Este alma ya pertenece a otro viajero."
                            )
                        }
                } else {
                    state = state.copy(isLoading = false, emailError = "Error en el vínculo: ${exception.localizedMessage}")
                }
            }
    }

    // Función auxiliar para no repetir código
    private fun sendEmailAndProgress(user: com.google.firebase.auth.FirebaseUser?) {
        user?.sendEmailVerification()?.addOnSuccessListener {
            state = state.copy(
                isLoading = false,
                currentStep = RegisterStep.VERIFY_PENDING,
                emailError = null
            )
            startResendTimer()
        }
    }
    // --- FASE 2: COMPROBACIÓN DEL ESTADO ---

    fun checkVerificationStatus() {
        val user = auth.currentUser
        if (user == null) {
            state = state.copy(emailError = "Sesión perdida. Reintenta.")
            return
        }

        state = state.copy(isLoading = true)

        // 1. Recargamos al usuario desde el servidor
        user.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 2. Tras recargar, comprobamos el booleano
                if (user.isEmailVerified) {
                    state = state.copy(
                        isLoading = false,
                        isVerified = true,
                        currentStep = RegisterStep.FINALIZE_PACT,
                        emailError = null
                    )
                } else {
                    state = state.copy(
                        isLoading = false,
                        emailError = "El lazo aún no ha sido confirmado en tu correo."
                    )
                }
            } else {
                state = state.copy(isLoading = false, emailError = "Error al conectar con el vacío.")
            }
        }
    }

    private var timerJob: Job? = null

    fun startResendTimer() {
        state = state.copy(canResend = false, resendCountdown = 60)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (state.resendCountdown > 0) {
                delay(1000)
                state = state.copy(resendCountdown = state.resendCountdown - 1)
            }
            state = state.copy(canResend = true)
        }
    }
    // Función para cuando el usuario se equivoca de email
    fun editEmail() {
        auth.currentUser?.delete()?.addOnCompleteListener {
            state = state.copy(
                currentStep = RegisterStep.EMAIL_ENTRY,
                isVerified = false,
                resendCountdown = 0,
                canResend = true,
                emailError = null
            )
            timerJob?.cancel()
        }
    }

    // --- FASE 3: PASSWORD Y FIRESTORE ---

    fun finalizeRegistration(onSuccess: () -> Unit) {
        if (state.password.length < 6 || state.username.isBlank()) {
            state = state.copy(
                passwordError = "La clave debe tener al menos 6 carácteres.",
                usernameError = "Todo viajero necesita un apodo."
            )
            return
        }

        state = state.copy(isLoading = true)
        val user = auth.currentUser

        // 1. Actualizamos a la contraseña real elegida por el usuario
        user?.updatePassword(state.password)?.addOnSuccessListener {

            // 2. Creamos el perfil definitivo en la base de datos
            val profile = UserProfile(
                email = state.email,
                username = state.username
            )

            db.collection("users").document(user.uid).set(profile)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        sessionDataStore.saveSession(profile)
                        state = state.copy(isLoading = false)
                        onSuccess()
                    }
                }
        }?.addOnFailureListener {
            state =
                state.copy(isLoading = false, passwordError = "Error al sellar el pacto místico.")
        }
    }
}