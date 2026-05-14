package com.example.anibey_codex_tfg.ui.screens.profile

import android.net.Uri

data class ProfileState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val currentPassword: String = "",
    val photoUrl: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val currentPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val updateSuccess: Boolean = false,
    val isDiscardDialogOpen: Boolean = false,
    val showEmailVerificationDialog: Boolean = false,
    val emailVerificationCountdown: Int = 0,
    val isCheckingEmailVerification: Boolean = false
)