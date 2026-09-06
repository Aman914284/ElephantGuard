package org.forestdept.seemsai.engine

import org.forestdept.seemsai.model.DefenseAlertLevel
import org.forestdept.seemsai.model.DmrsBreakdown
import org.forestdept.seemsai.model.TrackedEntity

/**
 * Autonomous Response Matrix Engine.
 * Converts real-time DMRS risk score into automated multi-tier spatial defenses.
 */
class AutonomousResponseMatrix {

    data class MatrixActionState(
        val level: DefenseAlertLevel,
        val vmsSpeedLimitKmh: Int?,
        val vmsMessage: String,
        val sirenTriggerRecommended: Boolean,
        val voiceAlertRecommended: Boolean,
        val qrtDispatchRecommended: Boolean,
        val smsBroadcastSimulated: Boolean,
        val actionsList: List<String>
    )

    fun evaluateActions(breakdown: DmrsBreakdown, entity: TrackedEntity): MatrixActionState {
        return when (breakdown.alertLevel) {
            DefenseAlertLevel.GREEN -> MatrixActionState(
                level = DefenseAlertLevel.GREEN,
                vmsSpeedLimitKmh = null,
                vmsMessage = "NORMAL TRAFFIC FLOW — CORRIDOR SECURE",
                sirenTriggerRecommended = false,
                voiceAlertRecommended = false,
                qrtDispatchRecommended = false,
                smsBroadcastSimulated = false,
                actionsList = listOf(
                    "Passive bio-spatial sensor monitoring",
                    "Edge-AI camera background telemetry active",
                    "No highway speed restrictions"
                )
            )

            DefenseAlertLevel.YELLOW -> MatrixActionState(
                level = DefenseAlertLevel.YELLOW,
                vmsSpeedLimitKmh = 40,
                vmsMessage = "CAUTION: WILDLIFE NEAR DALMA BUFFER — SPEED 40 KM/H",
                sirenTriggerRecommended = false,
                voiceAlertRecommended = false,
                qrtDispatchRecommended = false,
                smsBroadcastSimulated = false,
                actionsList = listOf(
                    "Dynamic VMS highway signboard warning active",
                    "Advisory telemetry logged to central matrix",
                    "Patrol units placed on background alert"
                )
            )

            DefenseAlertLevel.AMBER -> MatrixActionState(
                level = DefenseAlertLevel.AMBER,
                vmsSpeedLimitKmh = 30,
                vmsMessage = "WARNING: ELEPHANT APPROACHING NH-33 — SPEED 30 KM/H",
                sirenTriggerRecommended = false,
                voiceAlertRecommended = true,
                qrtDispatchRecommended = false,
                smsBroadcastSimulated = true,
                actionsList = listOf(
                    "Forest Patrol Pre-Alert issued to Sector 3",
                    "Community advisory broadcast queued",
                    "Highway Variable Message Signs updated to 30 km/h",
                    "Kinematic vector tracking intensified"
                )
            )

            DefenseAlertLevel.RED -> MatrixActionState(
                level = DefenseAlertLevel.RED,
                vmsSpeedLimitKmh = 20,
                vmsMessage = "🚨 CRITICAL: ELEPHANT ON HIGHWAY AXIS — SPEED 20 KM/H",
                sirenTriggerRecommended = true,
                voiceAlertRecommended = true,
                qrtDispatchRecommended = true,
                smsBroadcastSimulated = true,
                actionsList = listOf(
                    "🚨 20 km/h emergency speed restriction engaged",
                    "🔊 Tactical acoustic warning siren triggered",
                    "🗣 Bilingual voice warning broadcast (EN/HI)",
                    "📱 Village SMS advisory simulation triggered",
                    "🚒 Quick Response Team (QRT) intercept dispatched",
                    "🗺 Command map auto-locked onto target centroid",
                    "💾 Critical bio-telemetry incident persisted"
                )
            )
        }
    }
}
