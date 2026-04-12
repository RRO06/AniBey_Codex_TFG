package com.example.anibey_codex_tfg.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.anibey_codex_tfg.R

@Composable
fun ProfilePhotoSelector(
    photoUrl: String?,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Surface(
        shape = CircleShape,
        border = BorderStroke(2.dp, Color.White),
        modifier = Modifier.size(120.dp)
    ) {
        Box(
            modifier = Modifier.clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                // Muestra avatar por defecto como placeholder y si no hay URL
                painter = rememberAsyncImagePainter(
                    model = photoUrl ?: R.drawable.default_avatar,
                    placeholder = painterResource(R.drawable.default_avatar)
                ),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(120.dp)
            )
        }
    }

    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, Color.White),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        enabled = !isLoading
    ) {
        Text("Cambiar Foto")
    }
}
