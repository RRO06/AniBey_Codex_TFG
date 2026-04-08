data class LoginState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isRecoveryMode: Boolean = false, // Para mostrar la vista de "Recuperar Esencia"
    val isRecoveryMailSent: Boolean = false
)

data class LoginActions(
    val onEmailChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onLoginSubmit: () -> Unit = {},
    val onToggleRecoveryMode: (Boolean) -> Unit = {},
    val onSendRecoveryEmail: () -> Unit = {},
    val onBackStep: () -> Unit = {}
)