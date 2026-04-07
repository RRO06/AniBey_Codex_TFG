package com.example.anibey_codex_tfg.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    private fun validateCurrentStep(): Boolean {
        return when (state.currentStep) {
            RegistrationStep.CREDENTIALS -> {
                val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
                val passValid = state.password.length >= 6

                state = state.copy(
                    emailError = if (!emailValid) "Email no válido" else null,
                    passwordError = if (!passValid) "Mínimo 6 caracteres" else null
                )
                emailValid && passValid
            }
            RegistrationStep.CHARACTER_INFO -> {
                val userValid = state.username.isNotBlank()
                state = state.copy(
                    usernameError = if (!userValid) "El nombre no puede estar vacío" else null
                )
                userValid
            }
            RegistrationStep.FINALIZING -> true
        }
    }
    fun onEmailChange(newValue: String) {
        state = state.copy(email = newValue, emailError = null)
    }

    fun onPasswordChange(newValue: String) {
        state = state.copy(password = newValue, passwordError = null)
    }

    fun onUsernameChange(newValue: String) {
        state = state.copy(username = newValue, usernameError = null)
    }
    fun onNextStep() {
        if (validateCurrentStep()) {
            val nextOrdinal = state.currentStep.ordinal + 1
            if (nextOrdinal < RegistrationStep.entries.size) {
                state = state.copy(currentStep = RegistrationStep.entries[nextOrdinal])
            }
        }
    }

    fun onBackStep() {
        val prevOrdinal = state.currentStep.ordinal - 1
        if (prevOrdinal >= 0) {
            state = state.copy(currentStep = RegistrationStep.entries[prevOrdinal])
        }
    }

    fun onLoginSubmit() {
        if (validateCurrentStep()) {
            state = state.copy(isLoading = true)
            // Lógica de Firebase...
        }
    }
}