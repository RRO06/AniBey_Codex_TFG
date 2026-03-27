package com.example.anibey_codex_tfg.ui.common.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.anibey_codex_tfg.R

// Definimos la familia de fuente personalizada
val AnimaFontFamily = FontFamily(
    Font(R.font.cinzel_regular, FontWeight.Normal),
    Font(R.font.cinzel_bold, FontWeight.Bold)
)

val Typography = Typography(
    // Títulos de secciones o botones grandes
    displayLarge = TextStyle(
        fontFamily = AnimaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = 2.sp
    ),

    // Texto de los botones principales
    titleMedium = TextStyle(
        fontFamily = AnimaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        letterSpacing = 1.2.sp
    ),

    // Subtítulos elegantes (como el de "Destino de Gaia")
    labelLarge = TextStyle(
        fontFamily = AnimaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        letterSpacing = 2.5.sp
    ),

    // Cuerpo de texto para leer mucho (hechizos, trasfondo)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Serif, // Para lectura larga, Serif estándar cansa menos la vista
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
)