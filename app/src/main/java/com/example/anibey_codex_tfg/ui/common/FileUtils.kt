package com.example.anibey_codex_tfg.ui.common

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import coil.imageLoader
import coil.request.ImageRequest

/**
 * Utilidades para el manejo de archivos y URLs en la capa de UI.
 */
object FileUtils {
    
    /**
     * Convierte una URL de Google Drive en un enlace de miniatura.
     */
    fun formatDriveUrl(url: String, sz: Int = 1000): String {
        return if (url.contains("drive.google.com")) {
            try {
                val id = url.split("/d/")[1].split("/")[0]
                // Usamos el endpoint /thumbnail que es mucho más rápido y permite compresión
                "https://drive.google.com/thumbnail?id=$id&sz=w$sz"
            } catch (e: Exception) {
                Log.e("FileUtils", "Error al formatear la URL de Google Drive: ${e.message}")
                url
            }
        } else {
            url
        }
    }

    /**
     * Pre-carga una lista de imágenes en la caché de Coil.
     */
    fun preloadImages(context: Context, urls: List<String>) {
        val imageLoader = context.imageLoader
        urls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(formatDriveUrl(url, 400)) // Pre-carga versiones pequeñas para la lista
                .build()
            imageLoader.enqueue(request)
        }
    }

    fun getRamaColor(rama: String): Color {
        return when (rama.trim().uppercase()) {
            "FUEGO" -> Color(0xFFFF5722)
            "AGUA" -> Color(0xFF03A9F4)
            "AIRE" -> Color(0xFF00BCD4)
            "TIERRA" -> Color(0xFF795548)
            "LUZ" -> Color(0xFFFFD600)
            "OSCURIDAD" -> Color(0xFF673AB7)
            "CREACION" -> Color(0xFFE0E0E0)
            "DESTRUCCION" -> Color(0xFFB71C1C)
            "ESENCIA" -> Color(0xFFE91E63)
            "ILUSION" -> Color(0xFF009688)
            "NIGROMANCIA" -> Color(0xFF4CAF50)
            else -> Color(0xFF90A4AE)
        }
    }
}
