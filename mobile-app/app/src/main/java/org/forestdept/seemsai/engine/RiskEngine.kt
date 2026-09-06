package org.forestdept.seemsai.engine

import org.forestdept.seemsai.model.DefenseAlertLevel
import org.forestdept.seemsai.model.DmrsBreakdown
import org.forestdept.seemsai.model.TrackedEntity
import java.util.Calendar

/**
 * Dynamic Multi-Parametric Risk Score (DMRS) Engine.
 *
 * Implements:
 * RiskScore = min(100, Math.round(
 *   (0.20 * C) +
 *   (0.30 * P) +
 *   (0.25 * V) +
 *   (0.15 * T) +
 *   (0.10 * H)
 * ))
 */
class RiskEngine {

    companion object {
        const val DEFAULT_HOTSPOT_BIAS = 85
    }

    /**
     * Compute comprehensive DMRS breakdown.
     */
    fun calculateRisk(
        entity: TrackedEntity,
        forcedHour: Int? = null,
        hotspotBias: Int = DEFAULT_HOTSPOT_BIAS
    ): DmrsBreakdown {
        // If not confirmed or not elephant, return minimal baseline
        if (!entity.isConfirmed) {
            val c = (entity.confidence * 100).toInt().coerceIn(0, 100)
            return DmrsBreakdown(
                confidenceScore = c,
                proximityScore = 10,
                kinematicsScore = 0,
                timeRiskScore = calculateTimeRisk(forcedHour),
                hotspotBiasScore = hotspotBias,
                finalRiskScore = (c * 0.15).toInt(),
                alertLevel = DefenseAlertLevel.GREEN
            )
        }

        // C - Confidence (0 - 100)
        val c = (entity.confidence * 100).toInt().coerceIn(0, 100)

        // P - Highway Proximity
        // d <= 200m -> 100
        // 200m < d <= 1500m -> linear attenuation
        // d > 1500m -> 0
        val d = entity.distanceToHighwayMeters
        val p = when {
            d <= 200.0 -> 100
            d <= 1500.0 -> {
                val ratio = (d - 200.0) / 1300.0
                (100 - (ratio * 100.0)).toInt().coerceIn(0, 100)
            }
            else -> 0
        }

        // V - Kinematic Risk
        // TTC < 5 min and heading toward highway -> 100
        // TTC 5-15 min -> 60
        // Stationary / parallel -> 20
        // Moving away -> 0
        val ttc = entity.ttcSeconds
        val v = when {
            ttc != null && ttc < 300 -> 100 // < 5 mins
            ttc != null && ttc <= 900 -> 60  // 5-15 mins
            entity.velocityKmh < 2.0 -> 20    // Stationary / parallel
            ttc != null -> 30
            else -> 0                         // Moving away
        }

        // T - Time Risk
        // 18:00 - 06:00 -> 100
        // Dusk / twilight -> 50
        // Day -> 15
        val t = calculateTimeRisk(forcedHour)

        // H - Hotspot Bias
        val h = hotspotBias.coerceIn(0, 100)

        // Weighted DMRS calculation
        val rawScore = (0.20 * c) + (0.30 * p) + (0.25 * v) + (0.15 * t) + (0.10 * h)
        val finalScore = Math.min(100, Math.round(rawScore).toInt()).coerceAtLeast(0)

        // Map to Defense Alert Level
        val level = when {
            finalScore >= 80 -> DefenseAlertLevel.RED
            finalScore >= 60 -> DefenseAlertLevel.AMBER
            finalScore >= 30 -> DefenseAlertLevel.YELLOW
            else -> DefenseAlertLevel.GREEN
        }

        return DmrsBreakdown(
            confidenceScore = c,
            proximityScore = p,
            kinematicsScore = v,
            timeRiskScore = t,
            hotspotBiasScore = h,
            finalRiskScore = finalScore,
            alertLevel = level
        )
    }

    private fun calculateTimeRisk(forcedHour: Int?): Int {
        val hour = forcedHour ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..5, in 19..23 -> 100 // Night
            6, 17, 18 -> 50           // Dusk / Dawn
            else -> 15                // Day
        }
    }
}
