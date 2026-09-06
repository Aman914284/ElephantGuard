package org.forestdept.seemsai.risk

import java.util.Calendar
import kotlin.math.roundToInt

enum class ThreatLevel {
    SAFE,
    CAUTION,
    HIGH,
    CRITICAL
}

data class RiskEvaluation(
    val riskScore: Int,
    val threatLevel: ThreatLevel,
    val confidenceFactor: Float,
    val proximityFactor: Float,
    val velocityFactor: Float,
    val timeFactor: Float,
    val hotspotFactor: Float,
    val summary: String
)

/**
 * Dynamic Multi-factor Risk Scoring (DMRS) Engine for Elephant Guard.
 * Formula: RiskScore = min(100, round(0.20*C + 0.30*P + 0.25*V + 0.15*T + 0.10*H))
 */
object DmrsRiskEngine {

    fun calculateRisk(
        confidence: Float, // 0.0 to 1.0
        boundingBoxAreaRatio: Float = 0.25f, // Approx proximity proxy
        elephantCount: Int = 1,
        isCorridorHotspot: Boolean = true
    ): RiskEvaluation {
        if (confidence <= 0f || elephantCount <= 0) {
            return RiskEvaluation(
                riskScore = 0,
                threatLevel = ThreatLevel.SAFE,
                confidenceFactor = 0f,
                proximityFactor = 0f,
                velocityFactor = 0f,
                timeFactor = 0f,
                hotspotFactor = 0f,
                summary = "SAFE: Sector Clear • No Wildlife Hazard"
            )
        }

        // 1. Confidence Weight (0.20 * C)
        val c = (confidence * 100f).coerceIn(0f, 100f)

        // 2. Proximity Weight (0.30 * P) - Bounding box size / count
        val p = ((boundingBoxAreaRatio * 200f) + 60f + (elephantCount - 1) * 15f).coerceIn(60f, 100f)

        // 3. Velocity / Herd Dynamics Weight (0.25 * V)
        val v = if (elephantCount > 1) 90f else 80f

        // 4. Time-of-Day Weight (0.15 * T) - Dusk/Night (18:00 - 06:00) is peak elephant movement
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val t = if (hour in 18..23 || hour in 0..6) 95f else 75f

        // 5. Historical Hotspot Weight (0.10 * H)
        val h = if (isCorridorHotspot) 90f else 60f

        // Formula Calculation
        val rawScore = (0.20f * c) + (0.30f * p) + (0.25f * v) + (0.15f * t) + (0.10f * h)
        val finalScore = (rawScore.roundToInt()).coerceIn(0, 100)

        val threatLevel = when {
            finalScore >= 80 -> ThreatLevel.CRITICAL
            finalScore >= 60 -> ThreatLevel.HIGH
            finalScore >= 30 -> ThreatLevel.CAUTION
            else -> ThreatLevel.SAFE
        }

        val summary = when (threatLevel) {
            ThreatLevel.CRITICAL -> "CRITICAL: Imminent Elephant Encounter in Corridor"
            ThreatLevel.HIGH -> "HIGH: Elephant Herd Activity in Immediate Zone"
            ThreatLevel.CAUTION -> "CAUTION: Wildlife Movement Monitored"
            ThreatLevel.SAFE -> "SAFE: Sector Clear"
        }

        return RiskEvaluation(
            riskScore = finalScore,
            threatLevel = threatLevel,
            confidenceFactor = c,
            proximityFactor = p,
            velocityFactor = v,
            timeFactor = t,
            hotspotFactor = h,
            summary = summary
        )
    }
}
