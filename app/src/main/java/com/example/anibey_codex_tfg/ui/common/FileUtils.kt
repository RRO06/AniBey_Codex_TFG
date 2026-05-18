package com.example.anibey_codex_tfg.ui.common

import android.content.Context
import android.util.Log
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
}
