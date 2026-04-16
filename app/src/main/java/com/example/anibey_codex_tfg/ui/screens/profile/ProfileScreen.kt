package com.example.anibey_codex_tfg.ui.screens.profile

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.ui.common.component.AnimaTextField
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = viewModel.state
    
    BackHandler {
        viewModel.onBackRequested(onNavigateBack)
    }

    val actions = ProfileActions(
        onUsernameChange = viewModel::onUsernameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
        onPhotoChange = viewModel::onPhotoChange,
        onSave = { viewModel.saveProfile() },
        onBack = { viewModel.onBackRequested(onNavigateBack) },
        uploadPhoto = viewModel::uploadPhoto,
        onDismissDiscardDialog = viewModel::onDismissDiscardDialog,
        onConfirmDiscard = {
            viewModel.onDismissDiscardDialog()
            onNavigateBack()
        },
        onDismissVerificationDialog = {
            viewModel.onDismissVerificationDialog()
        },
        onConfirmVerification = {
            viewModel.onConfirmEmailVerification(
                onSuccess = {
                    // Logout después de que la UI se actualice
                    viewModel.logout()
                    onLogout()
                }
            )
        }
    )

    ProfileContent(state = state, actions = actions, modifier = modifier)
}

@Composable
fun ProfileContent(
    state: ProfileState,
    actions: ProfileActions,
    modifier: Modifier
) {
    val scrollState = rememberScrollState()
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            actions.uploadPhoto(uri)
        }
    }

    DiscardChangesDialog(state, actions)
    EmailVerificationDialog(state, actions)

    Box(
        modifier = modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.fondo_login),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.20f), BlendMode.Darken)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(actions)
            Spacer(modifier = Modifier.height(24.dp))

            ProfileMessages(state)
            Spacer(modifier = Modifier.height(32.dp))

            ProfilePhotoSelector(
                photoUrl = state.photoUrl,
                onClick = { imageLauncher.launch("image/*") },
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            ProfileFormFields(state, actions)
            Spacer(modifier = Modifier.height(48.dp))

            SaveButton(state, actions)
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun DiscardChangesDialog(state: ProfileState, actions: ProfileActions) {
    if (state.isDiscardDialogOpen) {
        AlertDialog(
            onDismissRequest = actions.onDismissDiscardDialog,
            title = { Text("¿DESCARTAR CAMBIOS?") },
            text = { Text("Tu esencia ha mutado pero no ha sido sellada. Si te retiras ahora, los cambios se desvanecerán en el vacío.") },
            confirmButton = {
                TextButton(onClick = actions.onConfirmDiscard) {
                    Text("DESCARTAR", color = PrimaryRed)
                }
            },
            dismissButton = {
                TextButton(onClick = actions.onDismissDiscardDialog) {
                    Text("CANCELAR", color = Color.Gray)
                }
            },
            containerColor = Color.DarkGray,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
private fun EmailVerificationDialog(state: ProfileState, actions: ProfileActions) {
    if (state.showEmailVerificationDialog) {
        AlertDialog(
            onDismissRequest = { actions.onDismissVerificationDialog() },
            title = { Text("VERIFICACIÓN DE CORREO REQUERIDA") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Se ha enviado un enlace de verificación a tu nuevo correo.",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Text(
                        "Por favor:",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        "1. Abre tu correo\n2. Haz clic en el enlace de verificación\n3. Vuelve a esta pantalla\n4. Haz clic en CONFIRMADO",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = Color.Yellow
                    )

                    // Mostrar mensaje de error si la verificación falló
                    state.generalError?.let { error ->
                        Text(
                            text = error,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = PrimaryRed,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { actions.onConfirmVerification() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("CONFIRMADO", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        actions.onDismissVerificationDialog()
                    },
                    enabled = !state.isLoading
                ) {
                    Text("CANCELAR", color = Color.Gray)
                }
            },
            containerColor = Color.Black.copy(alpha = 0.9f),
            titleContentColor = PrimaryRed,
            textContentColor = Color.White
        )
    }
}

@Composable
private fun ProfileHeader(actions: ProfileActions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = actions.onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Editar Perfil",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                shadow = Shadow(color = Color.Black, blurRadius = 4f)
            ),
            color = Color.White
        )
    }
}

@Composable
private fun ProfileMessages(state: ProfileState) {
    state.generalError?.let { error ->
        Text(
            text = error,
            color = PrimaryRed,
            style = MaterialTheme.typography.labelSmall.copy(textAlign = TextAlign.Center),
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }

    if (state.updateSuccess) {
        Text(
            text = "Esencia actualizada con éxito",
            color = Color.Green,
            style = MaterialTheme.typography.labelSmall.copy(textAlign = TextAlign.Center),
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun ProfileFormFields(state: ProfileState, actions: ProfileActions) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnimaTextField(
            value = state.username,
            onValueChange = actions.onUsernameChange,
            label = "Apodo del Viajero",
            errorMessage = state.usernameError
        )

        AnimaTextField(
            value = state.email,
            onValueChange = actions.onEmailChange,
            label = "Correo Vinculado",
            errorMessage = state.emailError
        )

        AnimaTextField(
            value = state.password,
            onValueChange = actions.onPasswordChange,
            label = "Nueva Contraseña (opcional)",
            isPassword = true,
            errorMessage = state.passwordError
        )

        AnimaTextField(
            value = state.currentPassword,
            onValueChange = actions.onCurrentPasswordChange,
            label = "Contraseña Actual (obligatoria para cambios)",
            isPassword = true,
            errorMessage = state.currentPasswordError
        )
    }
}

@Composable
private fun SaveButton(state: ProfileState, actions: ProfileActions) {
    Button(
        onClick = actions.onSave,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = !state.isLoading,
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
        } else {
            Text("GUARDAR CAMBIOS", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}