package com.example.anibey_codex_tfg.ui.welcome.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.ui.common.theme.*

data class WelcomeActions(
    val onLoginClick: () -> Unit = {},
    val onGuestClick: () -> Unit = {}
)

@Composable
fun WelcomeScreen(
    modifier: Modifier,
    onLoginSelected: () -> Unit,
    onGuestSelected: () -> Unit
) {
    // Hoisting de las acciones
    val actions = WelcomeActions(
        onLoginClick = onLoginSelected,
        onGuestClick = onGuestSelected
    )
    WelcomeScreenContent(modifier = modifier, actions = actions)
}

@Composable
fun WelcomeScreenContent(
    modifier: Modifier = Modifier,
    actions: WelcomeActions
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.fondo_login),
                contentScale = ContentScale.Crop,
                // Oscurecemos el fondo un 15% para que los elementos blancos/rojos resalten
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.15f), BlendMode.Darken)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo con un ligero padding superior para que no toque la muesca del móvil
            Image(
                painter = painterResource(id = R.drawable.logo_anima_tfg),
                contentDescription = "Anima Codex Logo",
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(top = 100.dp)
            )

            Column(
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "EL CÓDICE DE GAIA",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF2C2C2C)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // BOTÓN LOGIN
                Button(
                    onClick = { actions.onLoginClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text("INICIAR SESIÓN", style = MaterialTheme.typography.titleMedium)
                }

                // BOTÓN INVITADO
                OutlinedButton(
                    onClick = { actions.onGuestClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.5.dp, PrimaryRed),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.4f) // Un toque de blanco traslúcido
                    )
                ) {
                    Text(
                        "MODO INVITADO",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryRed
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun WelcomePreview() {
    AniBey_Codex_TFGTheme {
        WelcomeScreenContent(actions = WelcomeActions())
    }
}