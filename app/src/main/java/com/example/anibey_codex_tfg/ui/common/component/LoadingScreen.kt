package com.example.anibey_codex_tfg.ui.common.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

@Composable
fun LoadingScreen(
    message: String = "Cargando...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = PrimaryRed)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            message,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 1.sp)
        )
    }
}