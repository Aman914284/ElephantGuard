package org.forestdept.seemsai.engine

import java.util.Locale

/**
 * Anti-Fatigue Alert System.
 * Locks duplicate acoustic sirens and SMS notifications for 10 minutes
 * after a critical alert, while continuing real-time spatial tracking.
 */
class AntiFatigueAlertSystem(private val cooldownDurationMs: Long = 10 * 60 * 1000L) {

    private var lastCriticalAlertTimestamp: Long = 0L

    @Synchronized
    fun canTriggerLoudAlert(): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastCriticalAlertTimestamp) >= cooldownDurationMs
    }

    @Synchronized
    fun recordAlertTriggered() {
        lastCriticalAlertTimestamp = System.currentTimeMillis()
    }

    @Synchronized
    fun getRemainingCooldownSeconds(): Long {
        val now = System.currentTimeMillis()
        val elapsed = now - lastCriticalAlertTimestamp
        val remaining = cooldownDurationMs - elapsed
        return if (remaining > 0) remaining / 1000 else 0L
    }

    @Synchronized
    fun formatCooldown(): String {
        val seconds = getRemainingCooldownSeconds()
        if (seconds <= 0) return "READY / NO COOLDOWN"
        val min = seconds / 60
        val sec = seconds % 60
        return String.format(Locale.US, "COOLDOWN: %02d:%02d", min, sec)
    }

    @Synchronized
    fun resetCooldown() {
        lastCriticalAlertTimestamp = 0L
    }
}
