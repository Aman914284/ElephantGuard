package org.forestdept.seemsai.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Geodesic Proximity Engine for Elephant Guard.
 * Calculates exact distance between patrol units and wildlife detections using the Haversine formula.
 */
object GeoProximityEngine {

    private const val EARTH_RADIUS_METERS = 6371000.0 // Mean Earth radius in meters
    const val DEFAULT_ALERT_RADIUS_METERS = 5000.0 // 5.0 km alert radius

    /**
     * Calculates distance in meters between two GPS coordinate pairs.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        if (lat1 == 0.0 || lon1 == 0.0 || lat2 == 0.0 || lon2 == 0.0) {
            // Fallback for sector proximity when GPS unacquired
            return 1500.0
        }

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculates distance in kilometers between two GPS coordinate pairs.
     */
    fun calculateDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        return calculateDistanceMeters(lat1, lon1, lat2, lon2) / 1000.0
    }

    /**
     * Checks if a target location is within the specified radius (default 5.0 km).
     */
    fun isWithinRadius(
        myLat: Double,
        myLon: Double,
        targetLat: Double,
        targetLon: Double,
        radiusMeters: Double = DEFAULT_ALERT_RADIUS_METERS
    ): Boolean {
        val distance = calculateDistanceMeters(myLat, myLon, targetLat, targetLon)
        return distance <= radiusMeters
    }
}
