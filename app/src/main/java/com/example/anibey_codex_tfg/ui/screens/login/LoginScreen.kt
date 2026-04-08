package com.example.anibey_codex_tfg.ui.login.ui

import LoginActions
import LoginState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.ui.common.component.AnimaTextField
import com.example.anibey_codex_tfg.ui.common.theme.AniBey_Codex_TFGTheme
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed
import com.example.anibey_codex_tfg.ui.screens.login.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = viewModel.state

    // Mapeo de acciones para limpiar la UI de lógica
    val actions = LoginActions(
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginSubmit = { viewModel.onLoginSubmit(onLoginSuccess) },
        onToggleRecoveryMode = { viewModel.toggleRecoveryMode(it) },
        onSendRecoveryEmail = viewModel::sendRecoveryEmail,
        onBackStep = {
            if (state.isRecoveryMode) viewModel.toggleRecoveryMode(false)
            else onNavigateBack()
        }
    )

    LoginScreenContent(
        state = state,
        actions = actions,
        modifier = modifier
    )
}

@Composable
fun LoginScreenContent(
    state: LoginState,
    actions: LoginActions,
    modifier: Modifier
) {
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
            // Cabecera con efecto de brillo
            LoginHeader(isRecoveryMode = state.isRecoveryMode)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.isRecoveryMailSent) {
                    // Vista cuando el correo de recuperación ya se envió
                    RecoverySuccessView(email = state.email)
                } else {
                    // Formulario de Login o Recuperación
                    LoginFieldsSection(state = state, actions = actions)
                }
            }

            // Botones de acción en la parte inferior
            LoginBottomBar(state = state, actions = actions)
        }
    }
}

@Composable
private fun LoginHeader(isRecoveryMode: Boolean) {
    Text(
        text = if (isRecoveryMode) "RECUPERACIÓN" else "ESENCIA",
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            shadow = Shadow(color = Color.White.copy(alpha = 0.7f), blurRadius = 12f)
        ),
        color = Color(0xFF1A1A1A),
        modifier = Modifier.padding(top = 80.dp)
    )
}

@Composable
private fun LoginFieldsSection(state: LoginState, actions: LoginActions) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        AnimaTextField(
            value = state.email,
            onValueChange = actions.onEmailChange,
            label = "E-mail de contacto",
            errorMessage = state.emailError
        )

        if (!state.isRecoveryMode) {
            // Campo de contraseña solo visible en Login Normal
            AnimaTextField(
                value = state.password,
                onValueChange = actions.onPasswordChange,
                label = "Contraseña de alma",
                errorMessage = state.passwordError,
                isPassword = true // Asegúrate que tu AnimaTextField use PasswordVisualTransformation
            )

            // Botón sutil para olvidar contraseña
            TextButton(
                onClick = { actions.onToggleRecoveryMode(true) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "¿HAS PERDIDO TU RUMBO?",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        color = PrimaryRed.copy(alpha = 0.8f)
                    )
                )
            }
        } else {
            // Texto explicativo en modo recuperación
            Text(
                text = "Escribe tu correo vinculado. Enviaremos un susurro para restaurar tu vínculo con Gaia.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif
                ),
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun LoginBottomBar(state: LoginState, actions: LoginActions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón Secundario (Desistir o Volver)
        TextButton(
            onClick = actions.onBackStep,
            enabled = !state.isLoading
        ) {
            Text(
                text = if (state.isRecoveryMode) "CANCELAR" else "DESISTIR",
                color = Color.Black.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp)
            )
        }

        // Botón Principal (Login o Enviar Mail)
        Button(
            onClick = {
                if (state.isRecoveryMode) actions.onSendRecoveryEmail()
                else actions.onLoginSubmit()
            },
            modifier = Modifier
                .height(50.dp)
                .width(160.dp),
            enabled = !state.isLoading && !state.isRecoveryMailSent,
            shape = RoundedCornerShape(2.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed,
                disabledContainerColor = PrimaryRed.copy(alpha = 0.5f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(
                    text = if (state.isRecoveryMode) "SUSURRAR" else "DESPERTAR",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp)
                )
            }
        }
    }
}

@Composable
private fun RecoverySuccessView(email: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google), // Usa un icono de un sobre o magia
            contentDescription = null,
            tint = PrimaryRed,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "EL LAZO HA SIDO ENVIADO",
            style = MaterialTheme.typography.titleMedium.copy(color = PrimaryRed, letterSpacing = 2.sp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Revisa tu bandeja en $email para restaurar tu esencia.",
            style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
            color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenContentPreview() {
    AniBey_Codex_TFGTheme {
        LoginScreenContent(
            state = LoginState(
                isRecoveryMode = true
            ),
            actions = LoginActions(),
            Modifier
        )
    }
}