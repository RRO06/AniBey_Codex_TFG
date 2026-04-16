package com.example.anibey_codex_tfg.domain.model

/**
 * Evento de seguridad que se registra en Firestore para notificar
 * cambios críticos (email, contraseña) a otros dispositivos.
 */
data class SecurityEvent(
    val eventType: String = "", // "email_changed", "password_changed"
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String = "", // Para saber qué dispositivo provocó el cambio
    val oldValue: String = "",
    val newValue: String = ""
)

