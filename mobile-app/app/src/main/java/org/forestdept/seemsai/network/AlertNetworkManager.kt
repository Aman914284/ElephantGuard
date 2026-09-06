package org.forestdept.seemsai.network

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.forestdept.seemsai.audio.SirenPlayer
import org.forestdept.seemsai.location.GeoProximityEngine
import org.forestdept.seemsai.location.GpsCoordinates
import org.forestdept.seemsai.notification.NotificationHelper
import org.forestdept.seemsai.risk.RiskEvaluation
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ElephantSightingMarker(
    val id: String,
    val reporterName: String,
    val elephantCount: Int,
    val threatLevel: String,
    val confidence: Float,
    val riskScore: Int,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val notes: String = "",
    val isSosAlert: Boolean = true,
    val radiusKm: Double = 5.0
)

data class WildlifeAlertPayload(
    val alertId: String,
    val target: String,
    val count: Int,
    val confidence: Float,
    val riskScore: Int,
    val threatLevel: String,
    val latitude: Double,
    val longitude: Double,
    val isGpsActive: Boolean,
    val timestamp: String,
    val sourceDeviceId: String,
    val sourceDeviceName: String,
    val notes: String = "",
    val isSos: Boolean = false
)

data class RemoteAlertEvent(
    val payload: WildlifeAlertPayload,
    val distanceKm: Double,
    val isWithin5KmGeofence: Boolean
)

/**
 * Manages Elephant Guard Alert Network (Phone A <-> Local Wi-Fi / Hotspot LAN Mesh <-> Phone B <-> Backend).
 * Provides 5 km proximity geofencing, P2P SOS broadcasts, and sighting zone mapping.
 */
class AlertNetworkManager(
    private val context: Context? = null,
    private val sirenPlayer: SirenPlayer? = null,
    private val locationProvider: org.forestdept.seemsai.location.LocationProvider? = null,
    private val backendBaseUrl: String = "http://10.0.2.2:8000"
) {

    val myDeviceId: String = "UNIT-${Build.MODEL.replace(" ", "_")}-${UUID.randomUUID().toString().take(4)}"
    val myDeviceName: String = "Dalma Patrol Unit (${Build.MODEL})"

    private val _latestBroadcastAlert = MutableStateFlow<WildlifeAlertPayload?>(null)
    val latestBroadcastAlert: StateFlow<WildlifeAlertPayload?> = _latestBroadcastAlert.asStateFlow()

    private val _remoteIncomingAlert = MutableStateFlow<RemoteAlertEvent?>(null)
    val remoteIncomingAlert: StateFlow<RemoteAlertEvent?> = _remoteIncomingAlert.asStateFlow()

    private val _activeSightings = MutableStateFlow<List<ElephantSightingMarker>>(emptyList())
    val activeSightings: StateFlow<List<ElephantSightingMarker>> = _activeSightings.asStateFlow()

    private val _backendStatus = MutableStateFlow("P2P Mesh + Cloud Network Active")
    val backendStatus: StateFlow<String> = _backendStatus.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var udpListenerJob: Job? = null
    private var cloudListenerJob: Job? = null
    private val udpPort = 8888
    private val cloudTopicUrl = "https://ntfy.sh/dalma_elephant_guard_sos_network_v1"

    init {
        startUdpListener()
        startCloudStreamListener()
    }

    private fun addOrUpdateSighting(marker: ElephantSightingMarker) {
        val currentList = _activeSightings.value.filter { it.id != marker.id }.toMutableList()
        currentList.add(0, marker)
        // Keep up to 15 recent sightings
        if (currentList.size > 15) {
            _activeSightings.value = currentList.take(15)
        } else {
            _activeSightings.value = currentList
        }
    }

    /**
     * Broadcasts a manual 5 KM SOS Elephant Sighting Alert from the user.
     */
    fun broadcastManualSos(
        count: Int,
        threatLevel: String = "CRITICAL",
        notes: String = "Elephant sighting confirmed by patrol officer. Immediate 5km safety corridor broadcast.",
        location: GpsCoordinates
    ) {
        val riskScore = when (threatLevel) {
            "CRITICAL" -> 95
            "HIGH" -> 80
            "ELEVATED" -> 60
            else -> 50
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val alertId = "SOS-${System.currentTimeMillis()}"
        val lat = if (location.isGpsActive) location.latitude else 22.8950
        val lon = if (location.isGpsActive) location.longitude else 86.2075
        val time = sdf.format(Date())

        val payload = WildlifeAlertPayload(
            alertId = alertId,
            target = "ELEPHANT",
            count = count,
            confidence = 1.0f,
            riskScore = riskScore,
            threatLevel = threatLevel,
            latitude = lat,
            longitude = lon,
            isGpsActive = location.isGpsActive,
            timestamp = time,
            sourceDeviceId = myDeviceId,
            sourceDeviceName = myDeviceName,
            notes = notes,
            isSos = true
        )

        val sighting = ElephantSightingMarker(
            id = alertId,
            reporterName = myDeviceName,
            elephantCount = count,
            threatLevel = threatLevel,
            confidence = 1.0f,
            riskScore = riskScore,
            latitude = lat,
            longitude = lon,
            timestamp = time,
            notes = notes,
            isSosAlert = true,
            radiusKm = 5.0
        )

        addOrUpdateSighting(sighting)
        _latestBroadcastAlert.value = payload
        Log.e("AlertNetwork", "🚨 BROADCASTING 5KM MANUAL SOS: $payload")

        // 1. Local Wi-Fi / Hotspot LAN UDP Broadcast (Phone A -> Phone B offline mesh)
        scope.launch {
            try {
                val jsonStr = createJsonFromPayload(payload)
                val socket = DatagramSocket()
                socket.broadcast = true
                val data = jsonStr.toByteArray()
                val packet = DatagramPacket(data, data.size, InetAddress.getByName("255.255.255.255"), udpPort)
                socket.send(packet)
                socket.close()
                Log.e("AlertNetwork", "UDP 5km SOS broadcast sent on port $udpPort")
            } catch (e: Exception) {
                Log.w("AlertNetwork", "UDP SOS broadcast error: ${e.message}")
            }
        }

        // 2. Global Cloud SOS Network Broadcast (Works on 4G/5G mobile internet across all devices)
        scope.launch {
            try {
                val url = URL(cloudTopicUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Title", "🚨 5KM ELEPHANT SOS ALERT")
                conn.setRequestProperty("Priority", "high")
                conn.setRequestProperty("Tags", "warning,elephant")
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                conn.doOutput = true

                val json = createJsonFromPayload(payload)
                OutputStreamWriter(conn.outputStream).use { it.write(json) }
                val code = conn.responseCode
                Log.e("AlertNetwork", "Cloud SOS posted to $cloudTopicUrl -> HTTP $code")
                if (code in 200..299) {
                    _backendStatus.value = "SOS Broadcast Live to All Connected Users (Cloud Active)"
                }
            } catch (e: Exception) {
                Log.w("AlertNetwork", "Cloud SOS broadcast notice: ${e.message}")
            }
        }

        // 3. Central Cloud backend telemetry sync
        scope.launch {
            try {
                val url = URL("$backendBaseUrl/api/v1/telemetry/report")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 2500
                conn.readTimeout = 2500
                conn.doOutput = true

                val json = createJsonFromPayload(payload)
                OutputStreamWriter(conn.outputStream).use { it.write(json) }
                val code = conn.responseCode
                if (code in 200..299) {
                    _backendStatus.value = "SOS Synced with Central Server (HTTP $code)"
                }
            } catch (e: Exception) {
                _backendStatus.value = "P2P Mesh + Cloud Relay Active"
            }
        }
    }

    /**
     * Broadcasts an automated AI elephant detection event over Local LAN & Central Cloud.
     */
    fun broadcastAlert(
        count: Int,
        confidence: Float,
        risk: RiskEvaluation,
        location: GpsCoordinates
    ) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val alertId = "ALERT-${System.currentTimeMillis()}"
        val lat = if (location.isGpsActive) location.latitude else 22.8950
        val lon = if (location.isGpsActive) location.longitude else 86.2075
        val time = sdf.format(Date())

        val payload = WildlifeAlertPayload(
            alertId = alertId,
            target = "ELEPHANT",
            count = count,
            confidence = confidence,
            riskScore = risk.riskScore,
            threatLevel = risk.threatLevel.name,
            latitude = lat,
            longitude = lon,
            isGpsActive = location.isGpsActive,
            timestamp = time,
            sourceDeviceId = myDeviceId,
            sourceDeviceName = myDeviceName,
            notes = "AI Edge Vision Detection (3/5 Temporal Validation Confirmed)",
            isSos = true
        )

        val sighting = ElephantSightingMarker(
            id = alertId,
            reporterName = "$myDeviceName (AI Edge)",
            elephantCount = count,
            threatLevel = risk.threatLevel.name,
            confidence = confidence,
            riskScore = risk.riskScore,
            latitude = lat,
            longitude = lon,
            timestamp = time,
            notes = "Edge AI Vision Detection",
            isSosAlert = true,
            radiusKm = 5.0
        )

        addOrUpdateSighting(sighting)
        _latestBroadcastAlert.value = payload
        Log.e("AlertNetwork", "📡 BROADCASTING AI ALERT: $payload")

        // 1. Local Wi-Fi / Hotspot LAN UDP Broadcast
        scope.launch {
            try {
                val jsonStr = createJsonFromPayload(payload)
                val socket = DatagramSocket()
                socket.broadcast = true
                val data = jsonStr.toByteArray()
                val packet = DatagramPacket(data, data.size, InetAddress.getByName("255.255.255.255"), udpPort)
                socket.send(packet)
                socket.close()
                Log.e("AlertNetwork", "UDP LAN broadcast sent on port $udpPort")
            } catch (e: Exception) {
                Log.w("AlertNetwork", "UDP broadcast error: ${e.message}")
            }
        }

        // 2. Global Cloud SOS Network Broadcast
        scope.launch {
            try {
                val url = URL(cloudTopicUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Title", "🚨 AI DETECTED ELEPHANT ALERT")
                conn.setRequestProperty("Priority", "high")
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                conn.doOutput = true

                val json = createJsonFromPayload(payload)
                OutputStreamWriter(conn.outputStream).use { it.write(json) }
                conn.responseCode
            } catch (e: Exception) {
                Log.w("AlertNetwork", "Cloud AI alert notice: ${e.message}")
            }
        }

        // 3. Central cloud backend sync
        scope.launch {
            try {
                val url = URL("$backendBaseUrl/api/v1/telemetry/report")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 2500
                conn.readTimeout = 2500
                conn.doOutput = true

                val json = createJsonFromPayload(payload)
                OutputStreamWriter(conn.outputStream).use { it.write(json) }
                val code = conn.responseCode
                if (code in 200..299) {
                    _backendStatus.value = "Synced with Central Server (HTTP $code)"
                }
            } catch (e: Exception) {
                _backendStatus.value = "P2P Mesh + Cloud Relay Active"
            }
        }
    }

    /**
     * Listens continuously on UDP port 8888 for incoming alerts from other patrol phones on the same Wi-Fi / Hotspot.
     */
    private fun startUdpListener() {
        udpListenerJob?.cancel()
        udpListenerJob = scope.launch {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket(udpPort)
                socket.broadcast = true
                val buffer = ByteArray(4096)

                while (isActive) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val message = String(packet.data, 0, packet.length)
                    handleIncomingAlertJson(message, locationProvider?.currentLocation?.value)
                }
            } catch (e: Exception) {
                Log.w("AlertNetwork", "UDP listener notice: ${e.message}")
            } finally {
                socket?.close()
            }
        }
    }

    /**
     * Listens in real-time to the Cloud SOS Event Stream so any phone anywhere on 4G/5G/Wi-Fi receives alerts.
     */
    private fun startCloudStreamListener() {
        cloudListenerJob?.cancel()
        cloudListenerJob = scope.launch {
            while (isActive) {
                var conn: HttpURLConnection? = null
                try {
                    val url = URL("$cloudTopicUrl/json")
                    conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 10000
                    conn.readTimeout = 0 // Keep stream open
                    conn.setRequestProperty("Accept", "application/json")

                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    var line: String? = null
                    while (isActive && reader.readLine().also { line = it } != null) {
                        val raw = line?.trim() ?: continue
                        if (raw.isEmpty()) continue
                        try {
                            val envelope = JSONObject(raw)
                            val event = envelope.optString("event", "")
                            if (event == "message") {
                                val messageBody = envelope.optString("message", "")
                                if (messageBody.isNotEmpty()) {
                                    handleIncomingAlertJson(messageBody, locationProvider?.currentLocation?.value)
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("AlertNetwork", "SSE line parse error: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.w("AlertNetwork", "Cloud SOS stream interrupted (${e.message}), reconnecting in 4s...")
                    delay(4000)
                } finally {
                    try { conn?.disconnect() } catch (e: Exception) {}
                }
            }
        }
    }

    /**
     * Processes an incoming alert packet received from another device (Phone B).
     */
    fun handleIncomingAlertJson(jsonStr: String, currentDeviceGps: GpsCoordinates? = null) {
        try {
            val obj = JSONObject(jsonStr)
            val incomingDeviceId = obj.optString("source_device_id", "")
            if (incomingDeviceId == myDeviceId && incomingDeviceId.isNotEmpty()) {
                // Ignore our own broadcast
                return
            }

            val alertLat = obj.optDouble("latitude", 22.8950)
            val alertLon = obj.optDouble("longitude", 86.2075)
            val count = obj.optInt("count", 1)
            val conf = obj.optDouble("confidence", 0.90).toFloat()
            val riskScore = obj.optInt("risk_score", 90)
            val threat = obj.optString("threat_level", "CRITICAL")
            val sourceName = obj.optString("source_device_name", "Dalma Remote Patrol Unit")
            val time = obj.optString("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            val alertId = obj.optString("alert_id", "REMOTE-${System.currentTimeMillis()}")
            val notes = obj.optString("notes", "5km Elephant Sighting SOS Broadcast")
            val isSos = obj.optBoolean("is_sos", true)

            val effectiveGps = currentDeviceGps ?: locationProvider?.currentLocation?.value
            val myLat = if (effectiveGps != null && effectiveGps.isGpsActive) effectiveGps.latitude else 22.8950
            val myLon = if (effectiveGps != null && effectiveGps.isGpsActive) effectiveGps.longitude else 86.2075

            // Compute Haversine distance
            val distanceKm = GeoProximityEngine.calculateDistanceKm(myLat, myLon, alertLat, alertLon)
            val isWithin5Km = distanceKm <= 5.0

            val payload = WildlifeAlertPayload(
                alertId = alertId,
                target = "ELEPHANT",
                count = count,
                confidence = conf,
                riskScore = riskScore,
                threatLevel = threat,
                latitude = alertLat,
                longitude = alertLon,
                isGpsActive = obj.optBoolean("is_gps_active", true),
                timestamp = time,
                sourceDeviceId = incomingDeviceId,
                sourceDeviceName = sourceName,
                notes = notes,
                isSos = isSos
            )

            val sighting = ElephantSightingMarker(
                id = alertId,
                reporterName = sourceName,
                elephantCount = count,
                threatLevel = threat,
                confidence = conf,
                riskScore = riskScore,
                latitude = alertLat,
                longitude = alertLon,
                timestamp = time,
                notes = notes,
                isSosAlert = isSos,
                radiusKm = 5.0
            )

            // Register sighting on map
            addOrUpdateSighting(sighting)

            val remoteEvent = RemoteAlertEvent(
                payload = payload,
                distanceKm = distanceKm,
                isWithin5KmGeofence = isWithin5Km
            )

            _remoteIncomingAlert.value = remoteEvent
            _latestBroadcastAlert.value = payload

            Log.e("AlertNetwork", "🚨 RECEIVED REMOTE SOS from $sourceName: distance=${String.format("%.2f", distanceKm)} km, within5km=$isWithin5Km")

            // If within 5 km: TRIGGER 10-SECOND EMERGENCY SIREN & HEADS-UP NOTIFICATION
            if (isWithin5Km) {
                sirenPlayer?.startSiren(durationSeconds = 10)

                context?.let { ctx ->
                    NotificationHelper.showSosEmergencyNotification(
                        context = ctx,
                        elephantCount = count,
                        threatLevel = threat,
                        reporter = sourceName,
                        distanceKm = distanceKm,
                        locationStr = "Dalma Corridor (${String.format("%.2f km away", distanceKm)})",
                        notes = notes
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AlertNetwork", "Error parsing remote alert JSON: ${e.message}")
        }
    }

    /**
     * Simulates receiving an elephant detection from Remote Unit B (~2.4 km away, IN 5KM ZONE).
     */
    fun simulateRemoteAlertFromUnitB(userGps: GpsCoordinates) {
        val remoteLat = if (userGps.isGpsActive) userGps.latitude + 0.018 else 22.9120 // ~2.4 km away
        val remoteLon = if (userGps.isGpsActive) userGps.longitude + 0.012 else 86.2200

        val fakeJson = """
            {
                "alert_id": "REMOTE-UNIT-B-${System.currentTimeMillis()}",
                "target": "ELEPHANT",
                "count": 2,
                "confidence": 0.95,
                "risk_score": 95,
                "threat_level": "CRITICAL",
                "latitude": $remoteLat,
                "longitude": $remoteLon,
                "is_gps_active": true,
                "timestamp": "${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}",
                "source_device_id": "REMOTE-UNIT-B-TEST",
                "source_device_name": "Dalma Field Guard Unit B",
                "notes": "2 Adult Tuskers sighted moving towards NH-33 crossing",
                "is_sos": true
            }
        """.trimIndent()

        handleIncomingAlertJson(fakeJson, userGps)
    }

    /**
     * Simulates receiving an SOS from Village Guard Unit C (~4.5 km away, IN 5KM ZONE).
     */
    fun simulateRemoteAlertFromVillageC(userGps: GpsCoordinates) {
        val remoteLat = if (userGps.isGpsActive) userGps.latitude - 0.032 else 22.8600 // ~4.5 km away
        val remoteLon = if (userGps.isGpsActive) userGps.longitude + 0.024 else 86.2350

        val fakeJson = """
            {
                "alert_id": "REMOTE-VILLAGE-C-${System.currentTimeMillis()}",
                "target": "ELEPHANT",
                "count": 4,
                "confidence": 0.98,
                "risk_score": 98,
                "threat_level": "CRITICAL",
                "latitude": $remoteLat,
                "longitude": $remoteLon,
                "is_gps_active": true,
                "timestamp": "${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}",
                "source_device_id": "REMOTE-VILLAGE-C-TEST",
                "source_device_name": "Asanbani Village Guard C",
                "notes": "Elephant herd (4 elephants) entering farm perimeter",
                "is_sos": true
            }
        """.trimIndent()

        handleIncomingAlertJson(fakeJson, userGps)
    }

    /**
     * Simulates receiving an alert from Out-of-Range Patrol D (~7.8 km away, OUT OF 5KM ZONE).
     */
    fun simulateRemoteAlertOutOfRange(userGps: GpsCoordinates) {
        val remoteLat = if (userGps.isGpsActive) userGps.latitude + 0.065 else 22.9700 // ~7.8 km away
        val remoteLon = if (userGps.isGpsActive) userGps.longitude + 0.045 else 86.2700

        val fakeJson = """
            {
                "alert_id": "REMOTE-OUTOFRANGE-D-${System.currentTimeMillis()}",
                "target": "ELEPHANT",
                "count": 1,
                "confidence": 0.88,
                "risk_score": 60,
                "threat_level": "HIGH",
                "latitude": $remoteLat,
                "longitude": $remoteLon,
                "is_gps_active": true,
                "timestamp": "${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}",
                "source_device_id": "REMOTE-UNIT-D-TEST",
                "source_device_name": "Chandil Sector Unit D",
                "notes": "Solitary bull spotted in deep forest ridge",
                "is_sos": false
            }
        """.trimIndent()

        handleIncomingAlertJson(fakeJson, userGps)
    }

    private fun createJsonFromPayload(payload: WildlifeAlertPayload): String {
        return """
            {
                "alert_id": "${payload.alertId}",
                "target": "${payload.target}",
                "count": ${payload.count},
                "confidence": ${payload.confidence},
                "risk_score": ${payload.riskScore},
                "threat_level": "${payload.threatLevel}",
                "latitude": ${payload.latitude},
                "longitude": ${payload.longitude},
                "is_gps_active": ${payload.isGpsActive},
                "timestamp": "${payload.timestamp}",
                "source_device_id": "${payload.sourceDeviceId}",
                "source_device_name": "${payload.sourceDeviceName}",
                "notes": "${payload.notes}",
                "is_sos": ${payload.isSos}
            }
        """.trimIndent()
    }
}

