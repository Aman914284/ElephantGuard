package org.forestdept.seemsai.engine

import java.util.Locale

/**
 * TTC (Time-To-Collision) Engine with Highway Axis.
 * Estimates crossing time based on distance and perpendicular velocity.
 */
class TtcEngine {

    fun formatTtc(ttcSeconds: Long?): String {
        if (ttcSeconds == null || ttcSeconds <= 0) {
            return "N/A"
        }
        val minutes = ttcSeconds / 60
        val seconds = ttcSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun getTtcUrgencyDescription(ttcSeconds: Long?): String {
        return when {
            ttcSeconds == null -> "Stationary / Moving Parallel"
            ttcSeconds < 180 -> "IMMINENT CROSSING (< 3 min)"
            ttcSeconds < 600 -> "MODERATE APPROACH (< 10 min)"
            else -> "DISTANT VECTOR (> 10 min)"
        }
    }
}
