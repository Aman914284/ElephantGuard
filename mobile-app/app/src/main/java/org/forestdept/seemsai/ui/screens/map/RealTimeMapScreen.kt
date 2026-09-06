package org.forestdept.seemsai.ui.screens.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.forestdept.seemsai.config.ForestConfig
import org.forestdept.seemsai.location.GeoProximityEngine
import org.forestdept.seemsai.location.GpsCoordinates
import org.forestdept.seemsai.location.LocationProvider
import org.forestdept.seemsai.network.AlertNetworkManager
import org.forestdept.seemsai.network.ElephantSightingMarker
import org.forestdept.seemsai.ui.theme.AlertRed
import org.forestdept.seemsai.ui.theme.AlertRedBg
import org.forestdept.seemsai.ui.theme.AlertRedBorder
import org.forestdept.seemsai.ui.theme.ForestCard
import org.forestdept.seemsai.ui.theme.ForestCardBorder
import org.forestdept.seemsai.ui.theme.ForestDark850
import org.forestdept.seemsai.ui.theme.ForestDark900
import org.forestdept.seemsai.ui.theme.ForestDark950
import org.forestdept.seemsai.ui.theme.ForestGreenLight
import org.forestdept.seemsai.ui.theme.SuccessGreen
import org.forestdept.seemsai.ui.theme.TextMutedSage
import org.forestdept.seemsai.ui.theme.TextSubtle
import org.forestdept.seemsai.ui.theme.TextWhite
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@Composable
fun RealTimeMapScreen(
    locationProvider: LocationProvider,
    alertNetworkManager: AlertNetworkManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userLocation by locationProvider.currentLocation.collectAsState()
    val latestAlert by alertNetworkManager.latestBroadcastAlert.collectAsState()
    val activeSightings by alertNetworkManager.activeSightings.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(locationProvider.hasLocationPermission())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasLocationPermission = fine || coarse
        if (hasLocationPermission) {
            locationProvider.startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var selectedMarkerInfo by remember { mutableStateOf<String?>(null) }
    var selectedSighting by remember { mutableStateOf<ElephantSightingMarker?>(null) }
    var showSosDialog by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    fun openForestDialer() {
        val number = ForestConfig.FOREST_DEPT_HELPLINE
        if (number.isBlank()) {
            Toast.makeText(context, "No Forest Helpline number configured.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to launch dialer", Toast.LENGTH_LONG).show()
        }
    }

    // Default reference coordinates for Dalma Wildlife Sanctuary / NH-33
    val dalmaRefLat = 22.8950
    val dalmaRefLon = 86.2075
    val currentLat = if (userLocation.isGpsActive) userLocation.latitude else dalmaRefLat
    val currentLon = if (userLocation.isGpsActive) userLocation.longitude else dalmaRefLon

    // Calculate nearest sighting distance
    val nearestSighting = activeSightings.minByOrNull { sighting ->
        GeoProximityEngine.calculateDistanceKm(currentLat, currentLon, sighting.latitude, sighting.longitude)
    }
    val nearestDistanceKm = nearestSighting?.let {
        GeoProximityEngine.calculateDistanceKm(currentLat, currentLon, it.latitude, it.longitude)
    }
    val isWithin5KmZone = nearestDistanceKm != null && nearestDistanceKm <= 5.0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForestDark950)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==========================================
            // TOP BAR: [ ← ] REAL-TIME SPATIAL MAP [ 📞 ]
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ForestDark900)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "5 KM SPATIAL RADAR",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = TextWhite
                    )
                    Text(
                        text = "DALMA CORRIDOR • ${activeSightings.size} ACTIVE SIGHTING(S)",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isWithin5KmZone) AlertRed else ForestGreenLight
                    )
                }

                IconButton(onClick = { openForestDialer() }) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Helpline",
                        tint = ForestGreenLight
                    )
                }
            }

            // ==========================================
            // MAIN MAP VIEW (OpenStreetMap + Vector Overlays)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { ctx ->
                        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(14.0)

                            val centerPoint = GeoPoint(currentLat, currentLon)
                            controller.setCenter(centerPoint)
                            mapViewRef = this
                        }
                    },
                    update = { map ->
                        map.overlays.clear()

                        // 1. User / Officer Location Marker
                        val centerPoint = GeoPoint(currentLat, currentLon)
                        val userMarker = Marker(map).apply {
                            position = centerPoint
                            title = if (userLocation.isGpsActive) "Patrol Officer (You)" else "Dalma Sector Reference"
                            snippet = if (userLocation.isGpsActive) {
                                "Lat: ${String.format("%.4f", userLocation.latitude)}, Lon: ${String.format("%.4f", userLocation.longitude)} (±${userLocation.accuracyMeters.toInt()}m)"
                            } else {
                                "GPS Location Unavailable • Dalma Wildlife Sanctuary Sector"
                            }
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                selectedSighting = null
                                selectedMarkerInfo = if (userLocation.isGpsActive) {
                                    "👤 YOUR PATROL LOCATION\n• Coordinates: ${String.format("%.4f°N, %.4f°E", userLocation.latitude, userLocation.longitude)}\n• Accuracy: ±${userLocation.accuracyMeters.toInt()}m\n• Nearest Sighting: ${nearestDistanceKm?.let { String.format("%.2f km", it) } ?: "None in range"}"
                                } else {
                                    "👤 DALMA REFERENCE SECTOR\n• Lat: 22.8950°N\n• Lon: 86.2075°E\n• GPS: Simulation Mode Active"
                                }
                                true
                            }
                        }
                        map.overlays.add(userMarker)

                        // 2. Render all active Elephant Sighting Zones
                        val sightingsToRender = activeSightings.ifEmpty {
                            if (latestAlert != null && latestAlert!!.latitude != 0.0) {
                                listOf(
                                    ElephantSightingMarker(
                                        id = latestAlert!!.alertId,
                                        reporterName = latestAlert!!.sourceDeviceName,
                                        elephantCount = latestAlert!!.count,
                                        threatLevel = latestAlert!!.threatLevel,
                                        confidence = latestAlert!!.confidence,
                                        riskScore = latestAlert!!.riskScore,
                                        latitude = latestAlert!!.latitude,
                                        longitude = latestAlert!!.longitude,
                                        timestamp = latestAlert!!.timestamp,
                                        notes = latestAlert!!.notes,
                                        isSosAlert = true,
                                        radiusKm = 5.0
                                    )
                                )
                            } else {
                                emptyList()
                            }
                        }

                        for (sighting in sightingsToRender) {
                            val sightingPoint = GeoPoint(sighting.latitude, sighting.longitude)
                            val distToUser = GeoProximityEngine.calculateDistanceKm(currentLat, currentLon, sighting.latitude, sighting.longitude)

                            // 1.0 km Red Critical Hazard Zone
                            val redCircle = Polygon.pointsAsCircle(sightingPoint, 1000.0)
                            val redPolygon = Polygon(map).apply {
                                points = redCircle
                                fillColor = android.graphics.Color.argb(45, 239, 68, 68)
                                strokeColor = android.graphics.Color.argb(220, 239, 68, 68)
                                strokeWidth = 3f
                                title = "1.0 km Core Danger Perimeter"
                            }
                            map.overlays.add(redPolygon)

                            // 2.5 km Amber Warning Buffer Zone
                            val amberCircle = Polygon.pointsAsCircle(sightingPoint, 2500.0)
                            val amberPolygon = Polygon(map).apply {
                                points = amberCircle
                                fillColor = android.graphics.Color.argb(25, 245, 158, 11)
                                strokeColor = android.graphics.Color.argb(180, 245, 158, 11)
                                strokeWidth = 2f
                                title = "2.5 km Warning Buffer Zone"
                            }
                            map.overlays.add(amberPolygon)

                            // 5.0 km Yellow / Golden SOS Perimeter Zone
                            val yellowCircle = Polygon.pointsAsCircle(sightingPoint, 5000.0)
                            val yellowPolygon = Polygon(map).apply {
                                points = yellowCircle
                                fillColor = android.graphics.Color.argb(16, 234, 179, 8)
                                strokeColor = android.graphics.Color.argb(160, 234, 179, 8)
                                strokeWidth = 2.5f
                                title = "5.0 km SOS Broadcast & Relay Perimeter"
                            }
                            map.overlays.add(yellowPolygon)

                            // Elephant Sighting Marker Pin
                            val elephantMarker = Marker(map).apply {
                                position = sightingPoint
                                title = "🐘 ELEPHANT SIGHTING (${sighting.elephantCount}x)"
                                snippet = "Reported by ${sighting.reporterName} • ${String.format("%.2f km away", distToUser)}"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                setOnMarkerClickListener { _, _ ->
                                    selectedSighting = sighting
                                    selectedMarkerInfo = "🐘 ELEPHANT SIGHTING ZONE\n• Target: ${sighting.elephantCount} Elephant(s)\n• Danger Level: ${sighting.threatLevel} (DMRS ${sighting.riskScore}/100)\n• Distance from You: ${String.format("%.2f km", distToUser)} (${if (distToUser <= 5.0) "INSIDE 5KM ZONE" else "OUTSIDE 5KM ZONE"})\n• Reported By: ${sighting.reporterName}\n• Coordinates: ${String.format("%.4f°N, %.4f°E", sighting.latitude, sighting.longitude)}\n• Sighting Time: ${sighting.timestamp}\n• Notes: ${sighting.notes.ifBlank { "Corridor sighting broadcast" }}"
                                    true
                                }
                            }
                            map.overlays.add(elephantMarker)
                        }

                        map.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Top Heads-Up Distance Banner (if within 5 km)
                if (isWithin5KmZone && nearestSighting != null && nearestDistanceKm != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp, start = 12.dp, end = 12.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AlertRedBg.copy(alpha = 0.95f))
                            .border(1.5.dp, AlertRed, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AlertRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚠️ ELEPHANT IN 5KM PROXIMITY!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                            Text(
                                text = "Spotted ${String.format("%.2f km", nearestDistanceKm)} from you (${nearestSighting.elephantCount} elephant(s)) • Exercise extreme caution",
                                fontSize = 10.sp,
                                color = TextWhite
                            )
                        }
                        Button(
                            onClick = {
                                mapViewRef?.controller?.animateTo(GeoPoint(nearestSighting.latitude, nearestSighting.longitude))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlertRed,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(text = "FOCUS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Floating Re-Center Button
                IconButton(
                    onClick = {
                        mapViewRef?.controller?.animateTo(GeoPoint(currentLat, currentLon))
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = if (isWithin5KmZone) 64.dp else 14.dp, end = 14.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ForestDark900)
                        .border(1.dp, ForestGreenLight, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Re-Center GPS",
                        tint = ForestGreenLight,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Map Legend Overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = if (isWithin5KmZone) 64.dp else 14.dp, start = 14.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ForestDark950.copy(alpha = 0.90f))
                        .border(1.dp, ForestCardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AlertRed))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "1km Hazard", fontSize = 8.sp, color = TextWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "2.5km Buffer", fontSize = 8.sp, color = TextWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEAB308)))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "5km SOS", fontSize = 8.sp, color = TextWhite)
                }

                // Floating Action: Quick "🚨 SEND 5KM SOS" Button on Map
                Button(
                    onClick = { showSosDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertRed,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp)
                        .height(44.dp)
                        .border(1.dp, TextWhite.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "SEND 5KM SOS", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }

            // ==========================================
            // BOTTOM DETAIL & TELEMETRY CARD
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = ForestCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Sighting Status / Danger Card
                    if (activeSightings.isNotEmpty() || (latestAlert != null && latestAlert!!.latitude != 0.0)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isWithin5KmZone) AlertRedBg else Color(0x2BF59E0B))
                                .border(1.dp, if (isWithin5KmZone) AlertRedBorder else Color(0x66F59E0B), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isWithin5KmZone) AlertRed else Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isWithin5KmZone) "🚨 ELEPHANT WITHIN 5.0 KM RADIUS" else "WILDLIFE SIGHTING RECORDED",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWithin5KmZone) AlertRed else Color(0xFFF59E0B)
                                )
                                Text(
                                    text = nearestDistanceKm?.let {
                                        "Closest Target: ${String.format("%.2f km away", it)} • ${nearestSighting?.elephantCount ?: 1} Elephant(s)"
                                    } ?: "Active Elephant Sighting marked on spatial grid",
                                    fontSize = 11.sp,
                                    color = TextMutedSage
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x1F22C55E))
                                .border(1.dp, Color(0x4422C55E), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CORRIDOR CLEAR • 5 KM ZONE ACTIVE",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "No active sightings within 5.0 km • Tap 'SEND 5KM SOS' if spotted",
                                    fontSize = 11.sp,
                                    color = TextMutedSage
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selected Marker Details if tapped
                    if (selectedMarkerInfo != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ForestDark900)
                                .border(1.dp, ForestGreenLight.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SELECTED POINT TELEMETRY",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenLight
                                    )
                                    IconButton(
                                        onClick = {
                                            selectedMarkerInfo = null
                                            selectedSighting = null
                                        },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedSage, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text(
                                    text = selectedMarkerInfo!!,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextWhite,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // GPS Readout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (userLocation.isGpsActive) {
                                "GPS: ${String.format("%.4f°N, %.4f°E", userLocation.latitude, userLocation.longitude)}"
                            } else {
                                "GPS: DALMA SANCTUARY (SIMULATED)"
                            },
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (userLocation.isGpsActive) TextWhite else Color(0xFFF59E0B)
                        )
                        Text(
                            text = if (userLocation.isGpsActive) "● GPS LOCKED" else "○ SIMULATION",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (userLocation.isGpsActive) ForestGreenLight else Color(0xFFF59E0B)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons: Broadcast SOS & Call 1926
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { openForestDialer() },
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestDark900,
                                contentColor = ForestGreenLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreenLight.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "CALL 1926", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showSosDialog = true },
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlertRed,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "BROADCAST SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 5 KM SOS BROADCAST DIALOG
    // ==========================================
    if (showSosDialog) {
        var elephantCount by remember { mutableIntStateOf(2) }
        var threatLevel by remember { mutableStateOf("CRITICAL") }
        var notes by remember { mutableStateOf("Elephant sighting spotted near corridor path. 5km alert dispatched.") }

        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            containerColor = ForestDark900,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BROADCAST 5KM SOS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "This will immediately mark the elephant sighting on the map and send an emergency 50s siren alarm to all users within 5.0 km.",
                        fontSize = 11.sp,
                        color = TextMutedSage,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Elephant Count:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 3, 5, 8).forEach { count ->
                            val isSelected = elephantCount == count
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AlertRed else ForestDark850)
                                    .border(1.dp, if (isSelected) AlertRed else ForestCardBorder, RoundedCornerShape(6.dp))
                                    .clickable { elephantCount = count },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TextWhite else TextMutedSage
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Landmark / Sighting Notes", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ForestGreenLight,
                            unfocusedBorderColor = ForestCardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        alertNetworkManager.broadcastManualSos(
                            count = elephantCount,
                            threatLevel = threatLevel,
                            notes = notes,
                            location = userLocation
                        )
                        showSosDialog = false
                        Toast.makeText(context, "🚨 5KM SOS Broadcast Sent & Sighting Zone Marked!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SEND 5KM ALERT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showSosDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestDark850, contentColor = TextMutedSage)
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}

