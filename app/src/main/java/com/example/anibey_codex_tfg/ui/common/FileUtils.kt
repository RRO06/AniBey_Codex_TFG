package com.example.anibey_codex_tfg.ui.common

/**
 * Utilidades para el manejo de archivos y URLs en la capa de UI.
 */
object FileUtils {
    
    /**
     * Convierte una URL de Google Drive estándar en un enlace de descarga directa
     * para que librerías como Coil puedan cargar la imagen.
     */
    fun formatDriveUrl(url: String): String {
        return if (url.contains("drive.google.com")) {
            try {
                // Extraer el ID del archivo de la URL (/d/ID/view o similares)
                val id = url.split("/d/")[1].split("/")[0]
                "https://drive.google.com/uc?export=view&id=$id"
            } catch (e: Exception) {
                url // Si falla el parseo, devolvemos la original
            }
        } else {
            url
        }
    }
}
