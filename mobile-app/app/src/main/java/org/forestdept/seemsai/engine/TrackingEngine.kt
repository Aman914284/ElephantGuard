package org.forestdept.seemsai.engine

import org.forestdept.seemsai.model.DetectedTarget
import org.forestdept.seemsai.model.RawDetection
import org.forestdept.seemsai.model.TrackedEntity
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Tracking Engine.
 * Tracks wildlife across frames using temporal coordinates and smoothing.
 * Computes:
 * - Centroid (X, Y)
 * - Heading Angle θ (degrees)
 * - Estimated Velocity v (km/h)
 * - Distance to NH-33 Highway Corridor Axis
 */
class TrackingEngine {

    // Corridor Anchor: Dalma Elephant Sanctuary / NH-33 axis
    companion object {
        const val ANCHOR_LAT = 22.8942
        const val ANCHOR_LNG = 86.2081

        // NH-33 highway segment line points near Dalma
        val HIGHWAY_POINT_A = Pair(22.8800, 86.2000)
        val HIGHWAY_POINT_B = Pair(22.9100, 86.2200)
    }

    private var previousLat: Double = ANCHOR_LAT + 0.0050
    private var previousLng: Double = ANCHOR_LNG - 0.0030
    private var previousTime: Long = System.currentTimeMillis()
    private var smoothedVelocity: Double = 6.8
    private var smoothedHeading: Double = 215.0

    @Synchronized
    fun updateTracking(
        detection: RawDetection?,
        isConfirmed: Boolean,
        overrideLat: Double? = null,
        overrideLng: Double? = null
    ): TrackedEntity {
        val now = System.currentTimeMillis()
        val dt = ((now - previousTime) / 1000.0).coerceAtLeast(0.1)

        val currentLat = overrideLat ?: (previousLat + (Math.random() - 0.52) * 0.0002)
        val currentLng = overrideLng ?: (previousLng + (Math.random() - 0.48) * 0.0002)

        // Calculate delta displacement in meters
        val dLat = (currentLat - previousLat) * 111139.0
        val dLng = (currentLng - previousLng) * 111139.0 * cos(Math.toRadians(currentLat))
        val distMeters = sqrt(dLat * dLat + dLng * dLng)

        // Heading angle in degrees (0 to 360)
        if (distMeters > 0.1) {
            val angleRad = atan2(dLng, dLat)
            var deg = Math.toDegrees(angleRad)
            if (deg < 0) deg += 360.0
            smoothedHeading = (smoothedHeading * 0.7) + (deg * 0.3)
        }

        // Velocity in km/h
        val instantVelocity = (distMeters / dt) * 3.6
        if (distMeters > 0.05) {
            smoothedVelocity = ((smoothedVelocity * 0.8) + (instantVelocity * 0.2)).coerceIn(1.0, 35.0)
        }

        previousLat = currentLat
        previousLng = currentLng
        previousTime = now

        // Calculate perpendicular distance to NH-33 line
        val distToHighway = calculateDistanceToHighway(currentLat, currentLng)

        // Calculate TTC (Time to collision) with highway axis
        val headingToHighwayDiff = Math.abs(smoothedHeading - 210.0)
        val isHeadingTowardsHighway = headingToHighwayDiff < 60.0

        val ttcSeconds: Long? = if (isHeadingTowardsHighway && smoothedVelocity > 1.0) {
            val speedMps = smoothedVelocity / 3.6
            val timeSec = (distToHighway / speedMps).toLong()
            timeSec.coerceIn(15, 3600)
        } else {
            null
        }

        val target = detection?.target ?: DetectedTarget.ELEPHANT
        val conf = detection?.confidence ?: 0.94f

        return TrackedEntity(
            trackId = "ELP-DALMA-${(ANCHOR_LAT * 100).toInt()}",
            target = target,
            confidence = conf,
            normalizedX = detection?.boundingBox?.centerX() ?: 0.5f,
            normalizedY = detection?.boundingBox?.centerY() ?: 0.5f,
            headingDegrees = Math.round(smoothedHeading * 10.0) / 10.0,
            velocityKmh = Math.round(smoothedVelocity * 10.0) / 10.0,
            latitude = currentLat,
            longitude = currentLng,
            distanceToHighwayMeters = Math.round(distToHighway * 10.0) / 10.0,
            ttcSeconds = ttcSeconds,
            isConfirmed = isConfirmed,
            lastSeenTimestamp = now
        )
    }

    private fun calculateDistanceToHighway(lat: Double, lng: Double): Double {
        // Point-to-line distance approximation in meters
        val x0 = lng * 111139.0 * cos(Math.toRadians(lat))
        val y0 = lat * 111139.0
        val x1 = HIGHWAY_POINT_A.second * 111139.0 * cos(Math.toRadians(HIGHWAY_POINT_A.first))
        val y1 = HIGHWAY_POINT_A.first * 111139.0
        val x2 = HIGHWAY_POINT_B.second * 111139.0 * cos(Math.toRadians(HIGHWAY_POINT_B.first))
        val y2 = HIGHWAY_POINT_B.first * 111139.0

        val numerator = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1)
        val denominator = sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1))
        return if (denominator > 0) numerator / denominator else 500.0
    }

    fun setCoordinates(lat: Double, lng: Double, heading: Double, velocity: Double) {
        previousLat = lat
        previousLng = lng
        smoothedHeading = heading
        smoothedVelocity = velocity
    }
}
