package org.forestdept.seemsai.model

import android.graphics.RectF

/**
 * Target species and objects recognizable by SEEMS-AI Edge CV.
 */
enum class DetectedTarget(val displayName: String, val isPrimaryHazard: Boolean) {
    ELEPHANT("Elephant (Elephas maximus)", true),
    PERSON("Human / Pedestrian", false),
    DOG("Domestic Dog / Canine", false),
    CAT("Feline / Small Animal", false),
    UNKNOWN("Unclassified Bio-Object", false)
}

/**
 * Performance Modes for edge optimization across device tiers.
 */
enum class PerformanceMode(
    val title: String,
    val targetFps: Int,
    val resolutionLabel: String,
    val description: String
) {
    PERFORMANCE("PERFORMANCE MODE", 12, "480p (640x480)", "Optimized for budget devices (Low RAM & Thermal throttling safe)"),
    BALANCED("BALANCED MODE", 18, "720p (1280x720)", "Standard field operation (Balanced power & telemetry)"),
    ACCURACY("ACCURACY MODE", 25, "1080p (1920x1080)", "Maximum spatial precision & edge model fidelity")
}

/**
 * Autonomous Defense Alert Levels.
 */
enum class DefenseAlertLevel(
    val levelName: String,
    val colorHex: Long,
    val description: String,
    val vmsSpeedLimit: Int?
) {
    GREEN("GREEN (NORMAL)", 0xFF10B981, "Passive spatial monitoring. Normal traffic flow.", null),
    YELLOW("YELLOW (CAUTION)", 0xFFEAB308, "Wildlife movement near corridor boundary. VMS advisory.", 40),
    AMBER("AMBER (ELEVATED)", 0xFFF59E0B, "Elephant approaching NH-33 corridor. Patrol pre-alerted.", 30),
    RED("RED (CRITICAL HAZARD)", 0xFFEF4444, "Imminent highway crossing detected. Siren active. 20 km/h limit.", 20)
}

/**
 * Raw detection output from inference frame.
 */
data class RawDetection(
    val target: DetectedTarget,
    val confidence: Float, // 0.0 to 1.0
    val boundingBox: RectF, // Normalized 0..1
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Kinematically tracked wildlife entity.
 */
data class TrackedEntity(
    val trackId: String = "ELP-DALMA-01",
    val target: DetectedTarget = DetectedTarget.ELEPHANT,
    val confidence: Float = 0.95f,
    val normalizedX: Float = 0.5f,
    val normalizedY: Float = 0.5f,
    val headingDegrees: Double = 218.0, // 0 - 360 deg
    val velocityKmh: Double = 8.5,
    val latitude: Double = 22.8950,
    val longitude: Double = 86.2075,
    val distanceToHighwayMeters: Double = 340.0,
    val ttcSeconds: Long? = 222, // null if moving away or stationary
    val isConfirmed: Boolean = true,
    val validationWindowCount: Int = 4, // out of 5
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

/**
 * DMRS (Dynamic Multi-Parametric Risk Score) breakdown.
 * Formula: Risk = min(100, round(0.20*C + 0.30*P + 0.25*V + 0.15*T + 0.10*H))
 */
data class DmrsBreakdown(
    val confidenceScore: Int,      // C (0-100)
    val proximityScore: Int,       // P (0-100)
    val kinematicsScore: Int,      // V (0-100)
    val timeRiskScore: Int,        // T (0-100)
    val hotspotBiasScore: Int,     // H (0-100, default 85)
    val finalRiskScore: Int,       // Final combined (0-100)
    val alertLevel: DefenseAlertLevel
)

/**
 * Citizen SOS report item.
 */
data class CitizenSosReport(
    val id: String = "SOS-" + (1000..9999).random(),
    val reporterName: String = "Citizen Patrol",
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val elephantCount: Int,
    val movementDirection: String,
    val notes: String,
    val isVerified: Boolean = false
)

/**
 * Forest Quick Response Team (QRT) Unit.
 */
data class QrtPatrolUnit(
    val unitId: String,
    val name: String,
    val status: String, // "PATROLLING", "DISPATCHED", "ON_SCENE", "STANDBY"
    val baseStation: String,
    val etaMinutes: Int,
    val currentLat: Double,
    val currentLng: Double
)

/**
 * Recorded Incident item for persistence.
 */
data class IncidentRecord(
    val id: String,
    val timestamp: Long,
    val formattedTime: String,
    val targetName: String,
    val confidence: Int,
    val riskScore: Int,
    val alertLevel: String,
    val ttcString: String,
    val speedKmh: Double,
    val heading: Double,
    val distanceToHwy: Double,
    val latitude: Double,
    val longitude: Double,
    val qrtDispatched: Boolean
)
