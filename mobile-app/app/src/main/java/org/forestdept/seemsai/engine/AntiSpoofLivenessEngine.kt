package org.forestdept.seemsai.engine

import android.graphics.RectF
import org.forestdept.seemsai.model.RawDetection
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Liveness & Anti-Spoofing classification state.
 */
enum class LivenessState {
    ANALYZING,               // Gathering frame dynamics (1-3 frames)
    LIVE_ORGANIC_WILDLIFE,   // Confirmed real biological movement & temporal dynamics
    STATIC_PHOTO_SPOOF       // Detected static 2D photograph, printed picture, or still phone screen prank
}

/**
 * Result data holder for Anti-Spoofing & Liveness verification.
 */
data class LivenessResult(
    val state: LivenessState,
    val livenessScore: Float,            // 0.0 (Pure static photo) to 1.0 (Active live biological motion)
    val organicMotionVariance: Float,
    val isPhotoSpoofSuspected: Boolean,
    val isLiveConfirmed: Boolean,
    val description: String
)

/**
 * Advanced Edge Anti-Spoofing & Anti-Prank Liveness Engine for SEEMS-AI Elephant Guard.
 * 
 * Prevents malicious users or pranksters from triggering emergency sirens or false SOS alerts
 * by pointing their camera at printed photographs, still pictures, posters, or digital phone screens.
 * 
 * Analyzes multi-frame bounding box deformation, aspect ratio variance, centroid micro-dynamics,
 * and temporal motion vectors to distinguish real 3D biological elephants from rigid 2D photos.
 */
class AntiSpoofLivenessEngine(
    private val windowSize: Int = 8,
    private val staticVarianceThreshold: Float = 0.0035f, // Below this = rigid static photo
    private val minLiveVarianceThreshold: Float = 0.0075f   // Above this = genuine live movement
) {

    private data class BoundingSnapshot(
        val rect: RectF,
        val timestampMs: Long,
        val confidence: Float
    )

    private val snapshotHistory = LinkedList<BoundingSnapshot>()

    /**
     * Evaluates a stream of raw detection frames for biological liveness vs static photo spoofing.
     */
    @Synchronized
    fun processFrame(detection: RawDetection?): LivenessResult {
        if (detection == null) {
            if (snapshotHistory.size > 0) {
                snapshotHistory.removeFirst()
            }
            return LivenessResult(
                state = LivenessState.ANALYZING,
                livenessScore = 0f,
                organicMotionVariance = 0f,
                isPhotoSpoofSuspected = false,
                isLiveConfirmed = false,
                description = "Monitoring Corridor • No Target in View"
            )
        }

        val now = System.currentTimeMillis()
        val snapshot = BoundingSnapshot(
            rect = RectF(detection.boundingBox),
            timestampMs = now,
            confidence = detection.confidence
        )

        if (snapshotHistory.size >= windowSize) {
            snapshotHistory.removeFirst()
        }
        snapshotHistory.addLast(snapshot)

        // Need at least 4 frames to reliably calculate motion dynamics
        if (snapshotHistory.size < 4) {
            return LivenessResult(
                state = LivenessState.ANALYZING,
                livenessScore = 0.5f,
                organicMotionVariance = 0.005f,
                isPhotoSpoofSuspected = false,
                isLiveConfirmed = false,
                description = "Liveness Engine: Analyzing target dynamics (${snapshotHistory.size}/$windowSize frames)..."
            )
        }

        // 1. Calculate Centroid Shifts across consecutive frames
        var totalCentroidShift = 0f
        var totalAspectRatioShift = 0f
        var totalScaleShift = 0f

        val list = snapshotHistory.toList()
        for (i in 1 until list.size) {
            val prev = list[i - 1].rect
            val curr = list[i].rect

            val prevCx = (prev.left + prev.right) / 2f
            val prevCy = (prev.top + prev.bottom) / 2f
            val currCx = (curr.left + curr.right) / 2f
            val currCy = (curr.top + curr.bottom) / 2f

            val dx = currCx - prevCx
            val dy = currCy - prevCy
            val dist = sqrt(dx * dx + dy * dy)
            totalCentroidShift += dist

            val prevW = (prev.right - prev.left).coerceAtLeast(0.01f)
            val prevH = (prev.bottom - prev.top).coerceAtLeast(0.01f)
            val currW = (curr.right - curr.left).coerceAtLeast(0.01f)
            val currH = (curr.bottom - curr.top).coerceAtLeast(0.01f)

            val prevAspect = prevW / prevH
            val currAspect = currW / currH
            totalAspectRatioShift += abs(currAspect - prevAspect)

            val prevArea = prevW * prevH
            val currArea = currW * currH
            totalScaleShift += abs(currArea - prevArea)
        }

        val count = (list.size - 1).toFloat()
        val avgCentroidShift = totalCentroidShift / count
        val avgAspectShift = totalAspectRatioShift / count
        val avgScaleShift = totalScaleShift / count

        // Composite Organic Motion Metric
        // Real biological animals undergo non-rigid deformation (aspect ratio & centroid micro-movements)
        // Static photos held still or on screens exhibit near-zero variance (< staticVarianceThreshold)
        val organicMotionVariance = (avgCentroidShift * 0.45f) + (avgAspectShift * 0.35f) + (avgScaleShift * 0.20f)

        val livenessScore = (organicMotionVariance / (minLiveVarianceThreshold * 1.5f)).coerceIn(0f, 1f)

        return when {
            // Static Photo Spoofing detected: Rigid bounding box with minimal organic deformation
            organicMotionVariance < staticVarianceThreshold -> {
                LivenessResult(
                    state = LivenessState.STATIC_PHOTO_SPOOF,
                    livenessScore = livenessScore,
                    organicMotionVariance = organicMotionVariance,
                    isPhotoSpoofSuspected = true,
                    isLiveConfirmed = false,
                    description = "⚠️ STATIC PHOTO / SCREEN SPOOF DETECTED (Motion: ${(organicMotionVariance * 1000).toInt()}mVar • Sirens Locked)"
                )
            }
            // Genuine live biological elephant with active natural movement
            organicMotionVariance >= minLiveVarianceThreshold -> {
                LivenessResult(
                    state = LivenessState.LIVE_ORGANIC_WILDLIFE,
                    livenessScore = livenessScore,
                    organicMotionVariance = organicMotionVariance,
                    isPhotoSpoofSuspected = false,
                    isLiveConfirmed = true,
                    description = "✅ LIVE BIOLOGICAL ELEPHANT (Liveness ${(livenessScore * 100).toInt()}% • Verified Motion)"
                )
            }
            else -> {
                LivenessResult(
                    state = LivenessState.ANALYZING,
                    livenessScore = livenessScore,
                    organicMotionVariance = organicMotionVariance,
                    isPhotoSpoofSuspected = false,
                    isLiveConfirmed = false,
                    description = "Verifying Liveness Dynamics (${(livenessScore * 100).toInt()}%) • Hold Steady..."
                )
            }
        }
    }

    @Synchronized
    fun reset() {
        snapshotHistory.clear()
    }
}
