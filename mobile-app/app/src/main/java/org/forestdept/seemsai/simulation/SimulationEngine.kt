package org.forestdept.seemsai.simulation

import android.graphics.RectF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.forestdept.seemsai.engine.TrackingEngine
import org.forestdept.seemsai.model.DetectedTarget
import org.forestdept.seemsai.model.RawDetection

/**
 * Offline Simulation Engine.
 * Generates realistic wildlife kinematics, bounding boxes, and scenarios
 * to guarantee 100% dependable live hackathon demonstrations.
 */
class SimulationEngine(
    private val onFrameGenerated: (RawDetection?, Double, Double, Double, Double, String) -> Unit
) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var simulationJob: Job? = null

    enum class SimulationScenario(val title: String, val description: String) {
        SCENARIO_1_SAFE("Scenario 1: Safe Distance", "Elephant deep in Dalma forest (> 2 km from NH-33). Risk: 15 / Green."),
        SCENARIO_2_APPROACHING("Scenario 2: Approaching Corridor", "Elephant moving at 7 km/h toward highway. Risk: 65 / Amber."),
        SCENARIO_3_CRITICAL("Scenario 3: Critical Threat Vector", "Elephant 120m from highway axis. Imminent crossing. Risk: 92+ / Red."),
        SCENARIO_4_FALSE_POSITIVE("Scenario 4: False Positive Filter", "Single-frame cow/shadow anomaly filtered by sliding window."),
        SCENARIO_5_MULTI_DETECTION("Scenario 5: Multi-Frame Tracking", "Consistent multi-frame trajectory with duplicate alert suppression.")
    }

    fun isSimulating(): Boolean = simulationJob?.isActive == true

    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    fun runScenario(scenario: SimulationScenario) {
        stopSimulation()
        simulationJob = scope.launch {
            when (scenario) {
                SimulationScenario.SCENARIO_1_SAFE -> {
                    // Far away, moving parallel
                    val lat = TrackingEngine.ANCHOR_LAT + 0.0200
                    val lng = TrackingEngine.ANCHOR_LNG + 0.0150
                    val raw = RawDetection(
                        target = DetectedTarget.ELEPHANT,
                        confidence = 0.94f,
                        boundingBox = RectF(0.35f, 0.40f, 0.65f, 0.70f)
                    )
                    // Fire 5 consistent frames
                    for (i in 1..5) {
                        onFrameGenerated(raw, lat, lng, 90.0, 3.2, "SCENARIO 1: SAFE DISTANCE (d > 2.2 km)")
                        delay(200)
                    }
                }

                SimulationScenario.SCENARIO_2_APPROACHING -> {
                    // Approaching at 650m
                    var lat = TrackingEngine.ANCHOR_LAT + 0.0070
                    var lng = TrackingEngine.ANCHOR_LNG - 0.0040
                    for (step in 1..10) {
                        lat -= 0.0002
                        lng += 0.0001
                        val raw = RawDetection(
                            target = DetectedTarget.ELEPHANT,
                            confidence = 0.96f,
                            boundingBox = RectF(0.30f, 0.35f, 0.70f, 0.75f)
                        )
                        onFrameGenerated(raw, lat, lng, 215.0, 7.4, "SCENARIO 2: APPROACHING CORRIDOR (650m)")
                        delay(250)
                    }
                }

                SimulationScenario.SCENARIO_3_CRITICAL -> {
                    // Imminent highway collision vector
                    var lat = TrackingEngine.ANCHOR_LAT + 0.0020
                    var lng = TrackingEngine.ANCHOR_LNG - 0.0010
                    for (step in 1..12) {
                        lat -= 0.00015
                        lng += 0.0001
                        val raw = RawDetection(
                            target = DetectedTarget.ELEPHANT,
                            confidence = 0.98f,
                            boundingBox = RectF(0.20f, 0.25f, 0.80f, 0.85f)
                        )
                        onFrameGenerated(raw, lat, lng, 218.0, 9.8, "SCENARIO 3: CRITICAL HIGHWAY CROSSING (< 150m)")
                        delay(250)
                    }
                }

                SimulationScenario.SCENARIO_4_FALSE_POSITIVE -> {
                    // Single-frame spike then null
                    val rawDog = RawDetection(
                        target = DetectedTarget.DOG,
                        confidence = 0.82f,
                        boundingBox = RectF(0.40f, 0.50f, 0.60f, 0.70f)
                    )
                    onFrameGenerated(rawDog, TrackingEngine.ANCHOR_LAT, TrackingEngine.ANCHOR_LNG, 0.0, 0.0, "ANOMALY FRAME 1/5: DOG DETECTED")
                    delay(300)
                    // Next 4 frames empty
                    for (i in 2..5) {
                        onFrameGenerated(null, TrackingEngine.ANCHOR_LAT, TrackingEngine.ANCHOR_LNG, 0.0, 0.0, "VALIDATION FRAME $i/5: NULL (FALSE ALARM REJECTED)")
                        delay(250)
                    }
                }

                SimulationScenario.SCENARIO_5_MULTI_DETECTION -> {
                    // Sustained track across multiple frames
                    var lat = TrackingEngine.ANCHOR_LAT + 0.0040
                    var lng = TrackingEngine.ANCHOR_LNG - 0.0020
                    for (i in 1..8) {
                        lat -= 0.0001
                        val raw = RawDetection(
                            target = DetectedTarget.ELEPHANT,
                            confidence = 0.95f,
                            boundingBox = RectF(0.25f, 0.30f, 0.75f, 0.80f)
                        )
                        onFrameGenerated(raw, lat, lng, 210.0, 6.5, "MULTI-FRAME TRACK: CONTINUOUS TRAJECTORY FRAME $i")
                        delay(250)
                    }
                }
            }
        }
    }

    /**
     * One-Tap 25-Second Commander Demo.
     * Automatically transitions through the entire defense lifecycle:
     * 1. Safe Distance (5s)
     * 2. Approach Vector & Sliding-Window Escalation (6s)
     * 3. Red Zone Collision Alert + 20km/h VMS + Siren + Voice Warning (8s)
     * 4. QRT Unit Intercept & Safe Corridor Restoration (6s)
     */
    fun startCommanderOneTapDemo(onPhaseUpdate: (String) -> Unit) {
        stopSimulation()
        simulationJob = scope.launch {
            try {
                // Phase 1: Passive Safe State
                onPhaseUpdate("PHASE 1/4: PASSIVE MONITORING (Corridor Clear)")
                var lat = TrackingEngine.ANCHOR_LAT + 0.0180
                var lng = TrackingEngine.ANCHOR_LNG + 0.0120
                for (i in 1..10) {
                    if (!isActive) return@launch
                    val raw = RawDetection(
                        target = DetectedTarget.ELEPHANT,
                        confidence = 0.92f,
                        boundingBox = RectF(0.40f, 0.45f, 0.60f, 0.65f)
                    )
                    onFrameGenerated(raw, lat, lng, 85.0, 3.5, "PHASE 1: SAFE DISTANCE (> 2000m)")
                    delay(500)
                }

                // Phase 2: Approach & Temporal Escalation
                onPhaseUpdate("PHASE 2/4: VECTOR APPROACH & TEMPORAL VALIDATION")
                for (i in 1..12) {
                    if (!isActive) return@launch
                    lat -= 0.0008
                    lng -= 0.0005
                    val raw = RawDetection(
                        target = DetectedTarget.ELEPHANT,
                        confidence = 0.95f,
                        boundingBox = RectF(0.30f, 0.35f, 0.70f, 0.75f)
                    )
                    onFrameGenerated(raw, lat, lng, 215.0, 7.8, "PHASE 2: APPROACHING NH-33 (600m)")
                    delay(500)
                }

                // Phase 3: Critical Hazard & Full Autonomous Defense
                onPhaseUpdate("PHASE 3/4: CRITICAL GEOFENCE BREACH (Siren / VMS 20 km/h / Voice / QRT)")
                for (i in 1..16) {
                    if (!isActive) return@launch
                    lat -= 0.0003
                    lng -= 0.0002
                    val raw = RawDetection(
                        target = DetectedTarget.ELEPHANT,
                        confidence = 0.98f,
                        boundingBox = RectF(0.18f, 0.22f, 0.82f, 0.86f)
                    )
                    onFrameGenerated(raw, lat, lng, 218.0, 9.2, "PHASE 3: RED ZONE BREACH (< 150m) — TTC: 01:24")
                    delay(500)
                }

                // Phase 4: Resolution & Safe Passage
                onPhaseUpdate("PHASE 4/4: QRT INTERCEPTED & HERD GUIDED TO SANCTUARY")
                for (i in 1..8) {
                    if (!isActive) return@launch
                    lat += 0.0010
                    lng += 0.0008
                    val raw = RawDetection(
                        target = DetectedTarget.ELEPHANT,
                        confidence = 0.93f,
                        boundingBox = RectF(0.42f, 0.44f, 0.58f, 0.62f)
                    )
                    onFrameGenerated(raw, lat, lng, 45.0, 4.0, "PHASE 4: HERD RETREATING NORTH INTO DALMA CORE")
                    delay(500)
                }

                onPhaseUpdate("COMMANDER DEMO COMPLETED — ALL DEFENSE SYSTEMS OPERATIONAL")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
