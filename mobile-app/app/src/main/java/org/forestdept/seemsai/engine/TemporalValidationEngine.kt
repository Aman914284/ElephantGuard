package org.forestdept.seemsai.engine

import org.forestdept.seemsai.model.DetectedTarget
import org.forestdept.seemsai.model.RawDetection
import java.util.LinkedList

/**
 * 3-of-5 Temporal Validation & Anti-Spoofing Liveness Engine.
 * Requirement: At least 3 positive elephant detections within the last 5 consecutive inference frames
 * PLUS verified biological liveness / non-spoofing before escalating into an emergency wildlife encounter.
 * 
 * Suppresses:
 * - Single-frame flickers, shadows, passing vehicles, and transient sensor noise.
 * - Static 2D photographs, printed paper pictures, posters, and digital phone screen pranks.
 */
class TemporalValidationEngine(
    private val windowSize: Int = 5,
    private val requiredThreshold: Int = 3,
    var confidenceThreshold: Float = 0.30f
) {

    private val frameHistory = LinkedList<Boolean>()
    private val livenessEngine = AntiSpoofLivenessEngine()

    @Synchronized
    fun processFrame(detection: RawDetection?): ValidationResult {
        val isElephant = detection != null && 
                         detection.target == DetectedTarget.ELEPHANT && 
                         detection.confidence >= confidenceThreshold

        if (frameHistory.size >= windowSize) {
            frameHistory.removeFirst()
        }
        frameHistory.addLast(isElephant)

        val liveness = livenessEngine.processFrame(if (isElephant) detection else null)

        val positiveCount = frameHistory.count { it }
        // A confirmed encounter requires temporal threshold AND must NOT be a detected static photo spoof
        val isTemporalConfirmed = positiveCount >= requiredThreshold
        val isConfirmed = isTemporalConfirmed && !liveness.isPhotoSpoofSuspected
        val last5Snapshot = frameHistory.toList()

        val statusText = when {
            liveness.isPhotoSpoofSuspected -> "⚠️ STATIC PHOTO / SCREEN SPOOF DETECTED (Alarm Locked)"
            isConfirmed -> "CONFIRMED ELEPHANT EVENT ($positiveCount/$windowSize Frames • Live Motion)"
            positiveCount > 0 -> "VALIDATING ($positiveCount/$windowSize Frames)"
            else -> "NO HAZARD DETECTED"
        }

        return ValidationResult(
            isConfirmed = isConfirmed,
            rawElephant = isElephant,
            last5 = last5Snapshot,
            positiveCount = positiveCount,
            totalFramesInWindow = frameHistory.size,
            statusDescription = statusText,
            rawDetection = detection,
            livenessResult = liveness
        )
    }

    @Synchronized
    fun processDetections(detections: List<RawDetection>): ValidationResult {
        val validElephant = detections.firstOrNull { 
            it.target == DetectedTarget.ELEPHANT && it.confidence >= confidenceThreshold 
        }
        return processFrame(validElephant)
    }

    @Synchronized
    fun reset() {
        frameHistory.clear()
        livenessEngine.reset()
    }

    @Synchronized
    fun forceState(positiveCount: Int) {
        frameHistory.clear()
        val count = positiveCount.coerceIn(0, windowSize)
        for (i in 0 until count) frameHistory.add(true)
        for (i in count until windowSize) frameHistory.add(false)
    }
}

data class ValidationResult(
    val isConfirmed: Boolean,
    val rawElephant: Boolean = false,
    val last5: List<Boolean> = emptyList(),
    val positiveCount: Int,
    val totalFramesInWindow: Int,
    val statusDescription: String,
    val rawDetection: RawDetection?,
    val livenessResult: LivenessResult = LivenessResult(
        state = LivenessState.ANALYZING,
        livenessScore = 0f,
        organicMotionVariance = 0f,
        isPhotoSpoofSuspected = false,
        isLiveConfirmed = false,
        description = "Ready"
    )
)

