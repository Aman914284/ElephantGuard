package org.forestdept.seemsai.engine

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Spatial Geofence Engine anchored at Dalma Elephant Corridor NH-33.
 */
class GeofenceEngine {

    companion object {
        const val ANCHOR_LAT = 22.8942
        const val ANCHOR_LNG = 86.2081

        const val RED_ZONE_RADIUS_METERS = 1000.0  // 1.0 km - Immediate Collision Danger
        const val AMBER_ZONE_RADIUS_METERS = 2500.0 // 2.5 km - Community & Patrol Advisory

        val VILLAGES = listOf(
            VillagePoint("Pardih Village", 22.8750, 86.1950, 2400),
            VillagePoint("Asanbani Corridor Point", 22.9050, 86.2300, 1100),
            VillagePoint("Moharda Foothills", 22.8850, 86.2400, 3100),
            VillagePoint("Dimna Dam Buffer", 22.8600, 86.2300, 4200),
            VillagePoint("Pipla Rural Belt", 22.9200, 86.1900, 3500)
        )
    }

    enum class GeofenceStatus(val label: String, val colorHex: Long) {
        RED_ZONE_BREACH("RED ZONE (IMMEDIATE DANGER)", 0xFFEF4444),
        AMBER_ZONE_ACTIVE("AMBER ZONE (PATROL ADVISORY)", 0xFFF59E0B),
        OUTSIDE_GEOFENCE("OUTSIDE BUFFER (SAFE RANGE)", 0xFF10B981)
    }

    fun evaluateGeofence(latitude: Double, longitude: Double): GeofenceEvaluation {
        val distToAnchor = calculateHaversineDistance(latitude, longitude, ANCHOR_LAT, ANCHOR_LNG)

        val status = when {
            distToAnchor <= RED_ZONE_RADIUS_METERS -> GeofenceStatus.RED_ZONE_BREACH
            distToAnchor <= AMBER_ZONE_RADIUS_METERS -> GeofenceStatus.AMBER_ZONE_ACTIVE
            else -> GeofenceStatus.OUTSIDE_GEOFENCE
        }

        val nearestVillage = VILLAGES.minByOrNull {
            calculateHaversineDistance(latitude, longitude, it.lat, it.lng)
        }

        val distToNearestVillage = nearestVillage?.let {
            calculateHaversineDistance(latitude, longitude, it.lat, it.lng)
        } ?: 0.0

        return GeofenceEvaluation(
            status = status,
            distanceToAnchorMeters = distToAnchor,
            nearestVillageName = nearestVillage?.name ?: "Pardih",
            distanceToVillageMeters = distToNearestVillage
        )
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

data class VillagePoint(
    val name: String,
    val lat: Double,
    val lng: Double,
    val populationApprox: Int
)

data class GeofenceEvaluation(
    val status: GeofenceEngine.GeofenceStatus,
    val distanceToAnchorMeters: Double,
    val nearestVillageName: String,
    val distanceToVillageMeters: Double
)
