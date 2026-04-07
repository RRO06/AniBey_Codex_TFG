package com.example.anibey_codex_tfg.ui.screens.login
enum class RegistrationStep {
    CREDENTIALS, // Email y Password
    CHARACTER_INFO, // Nombre de usuario / Apodo
    FINALIZING // Confirmación
}

data class LoginActions(
    val onEmailChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onUsernameChange: (String) -> Unit = {},
    val onNextStep: () -> Unit = {},
    val onBackStep: () -> Unit = {},
    val onLoginSubmit: () -> Unit = {}
)
data class LoginState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val username: String = "",
    val usernameError: String? = null,
    val currentStep: RegistrationStep = RegistrationStep.CREDENTIALS,
    val isLoading: Boolean = false
)