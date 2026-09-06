package org.forestdept.seemsai.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.forestdept.seemsai.model.IncidentRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local Incident Database & Telemetry Store.
 */
class IncidentDatabase(context: Context) {

    private val prefs = context.getSharedPreferences("seems_ai_incidents_db", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private val cachedList = mutableListOf<IncidentRecord>()

    init {
        loadFromPrefs()
        if (cachedList.isEmpty()) {
            populateInitialHistoricalEvents()
        }
    }

    private fun loadFromPrefs() {
        val json = prefs.getString("incidents_json", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<IncidentRecord>>() {}.type
                val items: List<IncidentRecord> = gson.fromJson(json, type)
                cachedList.clear()
                cachedList.addAll(items)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveToPrefs() {
        try {
            val json = gson.toJson(cachedList)
            prefs.edit().putString("incidents_json", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun getAllIncidents(): List<IncidentRecord> {
        return cachedList.toList().reversed()
    }

    @Synchronized
    fun logIncident(
        targetName: String,
        confidence: Int,
        riskScore: Int,
        alertLevel: String,
        ttcString: String,
        speedKmh: Double,
        heading: Double,
        distanceToHwy: Double,
        lat: Double,
        lng: Double,
        qrtDispatched: Boolean = false
    ): IncidentRecord {
        val now = System.currentTimeMillis()
        val record = IncidentRecord(
            id = "INC-DLM-" + (1000..9999).random(),
            timestamp = now,
            formattedTime = dateFormat.format(Date(now)),
            targetName = targetName,
            confidence = confidence,
            riskScore = riskScore,
            alertLevel = alertLevel,
            ttcString = ttcString,
            speedKmh = speedKmh,
            heading = heading,
            distanceToHwy = distanceToHwy,
            latitude = lat,
            longitude = lng,
            qrtDispatched = qrtDispatched
        )
        cachedList.add(record)
        // Keep last 100 entries
        if (cachedList.size > 100) {
            cachedList.removeAt(0)
        }
        saveToPrefs()
        return record
    }

    @Synchronized
    fun clearAll() {
        cachedList.clear()
        saveToPrefs()
    }

    fun exportAsJson(): String {
        return gson.toJson(cachedList)
    }

    private fun populateInitialHistoricalEvents() {
        val now = System.currentTimeMillis()
        cachedList.addAll(
            listOf(
                IncidentRecord(
                    id = "INC-DLM-8821",
                    timestamp = now - 3600000 * 5,
                    formattedTime = dateFormat.format(Date(now - 3600000 * 5)),
                    targetName = "Elephant (Tusked Bull)",
                    confidence = 96,
                    riskScore = 88,
                    alertLevel = "RED",
                    ttcString = "04:12",
                    speedKmh = 9.2,
                    heading = 210.0,
                    distanceToHwy = 180.0,
                    latitude = 22.8955,
                    longitude = 86.2078,
                    qrtDispatched = true
                ),
                IncidentRecord(
                    id = "INC-DLM-8794",
                    timestamp = now - 3600000 * 18,
                    formattedTime = dateFormat.format(Date(now - 3600000 * 18)),
                    targetName = "Elephant Herd (3 Adults, 1 Calf)",
                    confidence = 94,
                    riskScore = 72,
                    alertLevel = "AMBER",
                    ttcString = "09:45",
                    speedKmh = 6.4,
                    heading = 195.0,
                    distanceToHwy = 620.0,
                    latitude = 22.8990,
                    longitude = 86.2110,
                    qrtDispatched = false
                ),
                IncidentRecord(
                    id = "INC-DLM-8750",
                    timestamp = now - 3600000 * 30,
                    formattedTime = dateFormat.format(Date(now - 3600000 * 30)),
                    targetName = "Canine (Domestic Dog)",
                    confidence = 82,
                    riskScore = 18,
                    alertLevel = "GREEN",
                    ttcString = "N/A",
                    speedKmh = 14.0,
                    heading = 90.0,
                    distanceToHwy = 1400.0,
                    latitude = 22.8800,
                    longitude = 86.2150,
                    qrtDispatched = false
                )
            )
        )
        saveToPrefs()
    }
}
