package com.example.anibey_codex_tfg.ui.common.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

@Composable
fun AnimaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    errorMessage: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val isError = errorMessage != null
    val mainColor = if (isError) Color.Red else PrimaryRed

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Etiqueta superior
        AnimaLabel(label, mainColor)

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp
            ),
            // 2. Transformación visual (Ocultar/Mostrar)
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            // 3. Icono de ojo
            trailingIcon = {
                if (isPassword) {
                    PasswordVisibilityIcon(
                        isVisible = passwordVisible,
                        color = mainColor,
                        onToggle = { passwordVisible = !passwordVisible }
                    )
                }
            },
            // 4. Texto de error
            supportingText = {
                if (isError) ErrorSupportingText(errorMessage)
            },
            colors = animaTextFieldColors()
        )
    }
}

// --- Sub-composables para mejorar la legibilidad ---

@Composable
private fun AnimaLabel(text: String, color: Color) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 2.sp,
            color = color,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun PasswordVisibilityIcon(
    isVisible: Boolean,
    color: Color,
    onToggle: () -> Unit
) {
    val icon = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
    val description = if (isVisible) "Ocultar contraseña" else "Mostrar contraseña"

    IconButton(onClick = onToggle) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = color
        )
    }
}

@Composable
private fun ErrorSupportingText(message: String) {
    Text(
        text = message,
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun animaTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
    focusedIndicatorColor = PrimaryRed,
    unfocusedIndicatorColor = Color.Black.copy(alpha = 0.5f),
    errorIndicatorColor = Color.Red,
    cursorColor = PrimaryRed,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black
)

// --- Previews ---

@Preview(showBackground = true)
@Composable
fun AnimaTextFieldErrorPreview() {
    AnimaTextField(
        value = "usuario_erroneo",
        onValueChange = {},
        label = "Nombre de Usuario",
        errorMessage = "El nombre no puede estar vacío"
    )
}