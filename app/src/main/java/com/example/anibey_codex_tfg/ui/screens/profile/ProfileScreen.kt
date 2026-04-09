package com.example.anibey_codex_tfg.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.ui.common.component.AnimaTextField
import com.example.anibey_codex_tfg.ui.common.theme.AniBey_Codex_TFGTheme
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = viewModel.state
    val actions = ProfileActions(
        onUsernameChange = viewModel::onUsernameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
        onPhotoChange = viewModel::onPhotoChange,
        onSave = viewModel::saveProfile,
        onBack = onNavigateBack,
        uploadPhoto = viewModel::uploadPhoto
    )

    ProfileContent(state = state, actions = actions, modifier = modifier)
}

@Composable
fun ProfileContent(
    state: ProfileState,
    actions: ProfileActions,
    modifier: Modifier
) {
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            actions.uploadPhoto(uri)
        }
    }

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
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
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

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Picture
            ProfilePhotoSelector(
                photoUrl = state.photoUrl,
                onClick = { imageLauncher.launch("image/*") },
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form
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
                    label = "Contraseña Actual (para cambios sensibles)",
                    isPassword = true,
                    errorMessage = state.currentPasswordError
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
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
                    Text("Guardar Cambios", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun ProfilePreview() {
    AniBey_Codex_TFGTheme {
        ProfileContent(
            state = ProfileState(username = "Viajero", email = "test@example.com"),
            actions = ProfileActions(),
            modifier = Modifier
        )
    }
}
