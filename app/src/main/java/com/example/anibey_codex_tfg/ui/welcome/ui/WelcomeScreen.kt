package com.example.anibey_codex_tfg.ui.welcome.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.ui.theme.AniBey_Codex_TFGTheme
import com.example.anibey_codex_tfg.ui.theme.DarkGray
import com.example.anibey_codex_tfg.ui.theme.LightGray
import com.example.anibey_codex_tfg.ui.theme.SecondaryRed

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier){
    WelcomeScreenContent()
}
@Composable
fun WelcomeScreenContent(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.fondo_login),
                contentScale = ContentScale.FillBounds
            )
            .background(Color.White.copy(alpha = 0.1f))
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_anima_tfg),
            contentDescription = "Logo Aplicación",
            modifier = Modifier
                .size(size = 400.dp)
                .align(Alignment.TopCenter)
                .offset(y = 50.dp, x = (-15).dp)
                .scale(scale = 1.5f)
        )
        Spacer(modifier = Modifier.padding(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 100.dp)
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .background(
                    color = LightGray.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {

            Text(
                text = "Accede al códice de Anima",
                color = DarkGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Iniciar sesión")
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp),
                border = BorderStroke(2.dp, SecondaryRed)
            ) {
                Text("Continuar como invitado", color = SecondaryRed)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeContentPreview(){
    AniBey_Codex_TFGTheme{ WelcomeScreenContent() }
}