package com.example.anibey_codex_tfg.ui.screens.profile

import android.net.Uri

data class ProfileState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val currentPassword: String = "",
    val photoUrl: String? = null, // URL o Base64 actual (mostrada en UI)
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val currentPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val updateSuccess: Boolean = false,
    val isDiscardDialogOpen: Boolean = false // Control del diálogo de confirmación
)

data class ProfileActions(
    val onUsernameChange: (String) -> Unit = {},
    val onEmailChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onCurrentPasswordChange: (String) -> Unit = {},
    val onPhotoChange: (String?) -> Unit = {},
    val onSave: () -> Unit = {},
    val onBack: () -> Unit = {},
    val uploadPhoto: (Uri) -> Unit = {},
    val onDismissDiscardDialog: () -> Unit = {},
    val onConfirmDiscard: () -> Unit = {}
)