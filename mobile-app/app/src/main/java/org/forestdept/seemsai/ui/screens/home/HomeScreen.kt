package org.forestdept.seemsai.ui.screens.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.forestdept.seemsai.auth.AuthViewModel
import org.forestdept.seemsai.config.ForestConfig
import org.forestdept.seemsai.location.GeoProximityEngine
import org.forestdept.seemsai.location.LocationProvider
import org.forestdept.seemsai.network.AlertNetworkManager
import org.forestdept.seemsai.ui.components.ElephantGuardHeader
import org.forestdept.seemsai.ui.components.ForestPrimaryButton
import org.forestdept.seemsai.ui.components.ForestSecondaryButton
import org.forestdept.seemsai.ui.theme.AlertRed
import org.forestdept.seemsai.ui.theme.AlertRedBg
import org.forestdept.seemsai.ui.theme.AlertRedBorder
import org.forestdept.seemsai.ui.theme.ForestCard
import org.forestdept.seemsai.ui.theme.ForestCardBorder
import org.forestdept.seemsai.ui.theme.ForestDark850
import org.forestdept.seemsai.ui.theme.ForestDark900
import org.forestdept.seemsai.ui.theme.ForestDark950
import org.forestdept.seemsai.ui.theme.ForestGreenLight
import org.forestdept.seemsai.ui.theme.ForestGreenPrimary
import org.forestdept.seemsai.ui.theme.SuccessGreen
import org.forestdept.seemsai.ui.theme.TextMutedSage
import org.forestdept.seemsai.ui.theme.TextSubtle
import org.forestdept.seemsai.ui.theme.TextWhite

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    alertNetworkManager: AlertNetworkManager,
    locationProvider: LocationProvider,
    onNavigateToCamera: () -> Unit,
    onNavigateToMap: () -> Unit,
    onLogout: () -> Unit,
    onRequestLocationPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val user = authState.currentUser
    val scrollState = rememberScrollState()

    val activeSightings by alertNetworkManager.activeSightings.collectAsState()
    val userLocation by locationProvider.currentLocation.collectAsState()
    val isLocationPermissionGranted by locationProvider.isPermissionGranted.collectAsState()

    var showSosDialog by remember { mutableStateOf(false) }

    val curLat = if (userLocation.isGpsActive) userLocation.latitude else 22.8950
    val curLon = if (userLocation.isGpsActive) userLocation.longitude else 86.2075

    val nearestSighting = activeSightings.minByOrNull { sighting ->
        GeoProximityEngine.calculateDistanceKm(curLat, curLon, sighting.latitude, sighting.longitude)
    }
    val nearestDistanceKm = nearestSighting?.let {
        GeoProximityEngine.calculateDistanceKm(curLat, curLon, it.latitude, it.longitude)
    }
    val isWithin5Km = nearestDistanceKm != null && nearestDistanceKm <= 5.0

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
            Toast.makeText(context, "Unable to launch dialer: ${e.localizedMessage ?: "Error"}", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForestDark950)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Logo
            ElephantGuardHeader(
                title = "ELEPHANT GUARD",
                subtitle = "Protect Wildlife. Protect Lives."
            )

            Spacer(modifier = Modifier.height(14.dp))

            // User Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ForestCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ForestDark850)
                            .border(1.dp, ForestGreenLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = ForestGreenLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.fullName ?: "Patrol Officer",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = user?.email ?: "officer@elephantguard.org",
                            fontSize = 11.sp,
                            color = TextMutedSage
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ForestDark900)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ONLINE",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // GPS Location & Network Connectivity Status Card
            if (!isLocationPermissionGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x33F59E0B)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("LOCATION PERMISSION REQUIRED", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                            Text("Required to calculate 5 km distance to other users' elephant sightings.", fontSize = 10.sp, color = TextWhite, lineHeight = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                onRequestLocationPermission()
                                locationProvider.refreshPermissionState()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("GRANT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = ForestDark900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreenLight.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (userLocation.isGpsActive) ForestGreenLight else Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (userLocation.isGpsActive) "GPS: ${String.format("%.4f°N, %.4f°E", userLocation.latitude, userLocation.longitude)}" else "GPS: ACQUIRING FIX...",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (userLocation.isGpsActive) ForestGreenLight else Color(0xFFF59E0B)
                            )
                        }
                        Text(
                            text = "CLOUD + P2P ACTIVE",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 5 KM SOS & ELEPHANT SIGHTING BROADCAST CARD
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isWithin5Km) AlertRedBg else ForestCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isWithin5Km) AlertRed else Color(0xFFEAB308).copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isWithin5Km) AlertRed else Color(0x33EAB308))
                                .border(1.dp, if (isWithin5Km) AlertRed else Color(0xFFEAB308), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isWithin5Km) TextWhite else Color(0xFFEAB308),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "5 KM SOS BROADCAST",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = if (isWithin5Km) AlertRed else TextWhite
                            )
                            Text(
                                text = if (isWithin5Km) {
                                    "🚨 ${nearestSighting?.elephantCount ?: 1} Elephant(s) within ${String.format("%.2f km", nearestDistanceKm!!)}!"
                                } else {
                                    "Alert all users within 5 km radius & mark map area"
                                },
                                fontSize = 11.sp,
                                color = if (isWithin5Km) TextWhite else TextMutedSage
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showSosDialog = true },
                            modifier = Modifier.weight(1.3f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlertRed,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "BROADCAST 5KM SOS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNavigateToMap,
                            modifier = Modifier.weight(0.9f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestDark900,
                                contentColor = ForestGreenLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreenLight.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "MAP (${activeSightings.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // PRIMARY FEATURE: LIVE AI CAMERA BUTTON
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCamera() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForestCard),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ForestGreenLight.copy(alpha = 0.8f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ForestGreenPrimary.copy(alpha = 0.25f))
                                .border(1.dp, ForestGreenLight, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = ForestGreenLight,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Live AI Camera",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(ForestGreenLight)
                                )
                            }
                            Text(
                                text = "Real-time edge elephant vision & auto 5km SOS",
                                fontSize = 12.sp,
                                color = TextMutedSage,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ForestPrimaryButton(
                        text = "OPEN CAMERA",
                        onClick = onNavigateToCamera,
                        icon = Icons.Default.Videocam
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // REAL-TIME SPATIAL MAP BUTTON
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMap() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForestCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ForestDark850)
                            .border(1.dp, ForestGreenLight, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = ForestGreenLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Real-Time Spatial Map",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Sighting markers, 5km SOS radius, 1km/2.5km danger circles",
                            fontSize = 11.sp,
                            color = TextMutedSage
                        )
                    }
                    Button(
                        onClick = onNavigateToMap,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestDark900,
                            contentColor = ForestGreenLight
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, ForestGreenLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Text(text = "VIEW MAP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Forest Department Emergency Quick Contact
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openForestDialer() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ForestCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ForestDark850),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = ForestGreenLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wildlife Emergency Helpline",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Control Room: ${ForestConfig.FOREST_DEPT_HELPLINE}",
                            fontSize = 11.sp,
                            color = TextMutedSage
                        )
                    }
                    Button(
                        onClick = { openForestDialer() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestDark900,
                            contentColor = ForestGreenLight
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, ForestGreenLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "CALL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logout Button
            ForestSecondaryButton(
                text = "LOGOUT",
                onClick = {
                    authViewModel.logout(onLogout)
                },
                icon = Icons.AutoMirrored.Filled.ExitToApp
            )

            Spacer(modifier = Modifier.height(20.dp))
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


