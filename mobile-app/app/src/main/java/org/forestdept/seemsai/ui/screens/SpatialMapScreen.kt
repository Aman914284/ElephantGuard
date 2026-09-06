package org.forestdept.seemsai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.forestdept.seemsai.engine.GeofenceEngine
import org.forestdept.seemsai.engine.GeofenceEvaluation
import org.forestdept.seemsai.model.DefenseAlertLevel
import org.forestdept.seemsai.model.TrackedEntity
import org.forestdept.seemsai.ui.components.RadarSpatialCanvas
import org.forestdept.seemsai.ui.components.TacticalCard
import org.forestdept.seemsai.ui.components.TelemetryItem
import org.forestdept.seemsai.ui.theme.DefenseDark950
import org.forestdept.seemsai.ui.theme.RadarAmber
import org.forestdept.seemsai.ui.theme.RadarCrimson
import org.forestdept.seemsai.ui.theme.RadarCyan
import org.forestdept.seemsai.ui.theme.RadarEmerald
import org.forestdept.seemsai.ui.theme.SlateTextMuted
import org.forestdept.seemsai.ui.theme.SlateTextPrimary
import org.forestdept.seemsai.ui.theme.SlateTextSecondary

@Composable
fun SpatialMapScreen(
    trackedEntity: TrackedEntity,
    alertLevel: DefenseAlertLevel,
    geofenceEvaluation: GeofenceEvaluation
) {
    val scrollState = rememberScrollState()

    val zoneColor = when (geofenceEvaluation.status) {
        GeofenceEngine.GeofenceStatus.RED_ZONE_BREACH -> RadarCrimson
        GeofenceEngine.GeofenceStatus.AMBER_ZONE_ACTIVE -> RadarAmber
        GeofenceEngine.GeofenceStatus.OUTSIDE_GEOFENCE -> RadarEmerald
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefenseDark950)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .verticalScroll(scrollState)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SPATIAL GIS & GEO-FENCING",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = RadarCyan
                )
                Text(
                    text = "DALMA ANCHOR (22.8942° N, 86.2081° E)",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // GEOFENCE STATUS ALERT BANNER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(zoneColor.copy(alpha = 0.15f))
                .border(1.dp, zoneColor, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(zoneColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = geofenceEvaluation.status.label,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = zoneColor
                    )
                }
                Text(
                    text = "${geofenceEvaluation.distanceToAnchorMeters.toInt()}m from Axis",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // RADAR SPATIAL CANVAS
        RadarSpatialCanvas(
            trackedEntity = trackedEntity,
            alertLevel = alertLevel
        )

        Spacer(modifier = Modifier.height(10.dp))

        // GPS TELEMETRY CARD
        TacticalCard {
            Column {
                Text(
                    text = "TARGET BIO-SPATIAL COORDINATES",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RadarCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryItem(
                        label = "Latitude",
                        value = String.format("%.5f° N", trackedEntity.latitude),
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryItem(
                        label = "Longitude",
                        value = String.format("%.5f° E", trackedEntity.longitude),
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryItem(
                        label = "Axis Dist",
                        value = "${trackedEntity.distanceToHighwayMeters.toInt()}m",
                        color = zoneColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CORRIDOR VILLAGES PROXIMITY TABLE
        TacticalCard {
            Column {
                Text(
                    text = "SURROUNDING CORRIDOR VILLAGES",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                GeofenceEngine.VILLAGES.forEach { village ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = village.name,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = "Pop: ~${village.populationApprox} citizens",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SlateTextMuted
                            )
                        }
                        Text(
                            text = "${village.lat.toString().take(6)}°N, ${village.lng.toString().take(6)}°E",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = RadarCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // FOREST QRT STATUS
        TacticalCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FOREST QRT PATROL UNIT 03",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "Base: Asanbani Checkpost | ETA: 4 mins",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateTextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0C4A6E))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "STANDBY",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }
    }
}
