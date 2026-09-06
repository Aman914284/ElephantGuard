package org.forestdept.seemsai.ui.screens

import android.graphics.RectF
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.forestdept.seemsai.model.DefenseAlertLevel
import org.forestdept.seemsai.model.DmrsBreakdown
import org.forestdept.seemsai.model.PerformanceMode
import org.forestdept.seemsai.model.RawDetection
import org.forestdept.seemsai.model.TrackedEntity
import org.forestdept.seemsai.simulation.SimulationEngine
import org.forestdept.seemsai.ui.components.RiskProgressBar
import org.forestdept.seemsai.ui.components.TacticalCard
import org.forestdept.seemsai.ui.components.TelemetryItem
import org.forestdept.seemsai.ui.components.VmsSpeedLimitSign
import org.forestdept.seemsai.ui.theme.DefenseCard
import org.forestdept.seemsai.ui.theme.DefenseCardBorder
import org.forestdept.seemsai.ui.theme.DefenseDark900
import org.forestdept.seemsai.ui.theme.DefenseDark950
import org.forestdept.seemsai.ui.theme.RadarAmber
import org.forestdept.seemsai.ui.theme.RadarCrimson
import org.forestdept.seemsai.ui.theme.RadarCyan
import org.forestdept.seemsai.ui.theme.RadarEmerald
import org.forestdept.seemsai.ui.theme.SlateTextMuted
import org.forestdept.seemsai.ui.theme.SlateTextPrimary
import org.forestdept.seemsai.ui.theme.SlateTextSecondary

@Composable
fun LiveAiScreen(
    trackedEntity: TrackedEntity,
    dmrsBreakdown: DmrsBreakdown,
    currentDetection: RawDetection?,
    ttcString: String,
    validationStatus: String,
    fps: Int,
    latencyMs: Long,
    isCameraMode: Boolean,
    performanceMode: PerformanceMode,
    demoPhaseMessage: String?,
    previewViewFactory: (() -> PreviewView)?,
    onToggleCameraMode: (Boolean) -> Unit,
    onSimulateScenario: (SimulationEngine.SimulationScenario) -> Unit,
    onRunCommanderDemo: () -> Unit
) {
    val scrollState = rememberScrollState()

    val levelColor = when (dmrsBreakdown.alertLevel) {
        DefenseAlertLevel.GREEN -> RadarEmerald
        DefenseAlertLevel.YELLOW -> Color(0xFFEAB308)
        DefenseAlertLevel.AMBER -> RadarAmber
        DefenseAlertLevel.RED -> RadarCrimson
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefenseDark950)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .verticalScroll(scrollState)
    ) {
        // TOP SYSTEM STATUS BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SEEMS-AI COMMAND",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = RadarCyan
                )
                Text(
                    text = "DALMA CORRIDOR NH-33",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextSecondary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(levelColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = dmrsBreakdown.alertLevel.levelName.take(15),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = levelColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // COMMANDER DEMO BANNER (IF ACTIVE)
        if (!demoPhaseMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF7C2D12))
                    .border(1.dp, RadarAmber, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "🎖 $demoPhaseMessage",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // CAMERA / SIMULATED FEED BOX WITH HUD BOUNDING BOX
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DefenseDark900)
                .border(1.5.dp, levelColor.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
        ) {
            if (isCameraMode && previewViewFactory != null) {
                AndroidView(
                    factory = { previewViewFactory() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Simulated Edge AI Video Surface
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF070B19)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (trackedEntity.isConfirmed) "🐘 SIMULATED FIELD TELEMETRY ACTIVE" else "PASSIVE SENSOR SURVEILLANCE",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateTextMuted
                    )
                }
            }

            // Bounding Box & Target Overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val box = currentDetection?.boundingBox ?: RectF(0.25f, 0.30f, 0.75f, 0.75f)
                val left = box.left * size.width
                val top = box.top * size.height
                val boxWidth = box.width() * size.width
                val boxHeight = box.height() * size.height

                if (trackedEntity.isConfirmed || currentDetection != null) {
                    drawRect(
                        color = levelColor,
                        topLeft = Offset(left, top),
                        size = Size(boxWidth, boxHeight),
                        style = Stroke(width = 3f)
                    )
                    // Centroid cross
                    val cx = left + boxWidth / 2f
                    val cy = top + boxHeight / 2f
                    drawCircle(color = levelColor, radius = 4f, center = Offset(cx, cy))
                }
            }

            // Target Overlay Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC020617))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (trackedEntity.isConfirmed) "🐘 ELEPHANT ${(trackedEntity.confidence * 100).toInt()}%" else "SCANNING FOR HAZARDS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = levelColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "[ID: ${trackedEntity.trackId.takeLast(6)}]",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateTextMuted
                    )
                }
            }

            // FPS & AI Latency Telemetry
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC020617))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$fps FPS | ${latencyMs}ms",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RadarCyan
                )
            }

            // Validation Engine Status at bottom of preview
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color(0xCC020617))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = validationStatus,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (trackedEntity.isConfirmed) RadarEmerald else RadarAmber
                    )
                    Text(
                        text = performanceMode.resolutionLabel.take(4),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // TELEMETRY MATRIX HUD
        TacticalCard(borderColor = levelColor.copy(alpha = 0.5f)) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TelemetryItem(
                        label = "Risk Score",
                        value = "${dmrsBreakdown.finalRiskScore}",
                        unit = "/100",
                        color = levelColor,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryItem(
                        label = "TTC (Highway)",
                        value = ttcString,
                        color = if (ttcString != "N/A") RadarCrimson else RadarCyan,
                        modifier = Modifier.weight(1f)
                    )
                    VmsSpeedLimitSign(
                        speedLimit = dmrsBreakdown.alertLevel.vmsSpeedLimit,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryItem(
                        label = "Velocity (v)",
                        value = "${trackedEntity.velocityKmh}",
                        unit = "km/h",
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryItem(
                        label = "Heading (θ)",
                        value = "${trackedEntity.headingDegrees.toInt()}°",
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryItem(
                        label = "Highway Dist",
                        value = "${trackedEntity.distanceToHighwayMeters.toInt()}",
                        unit = "m",
                        color = if (trackedEntity.distanceToHighwayMeters < 200) RadarCrimson else RadarCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                RiskProgressBar(
                    riskScore = dmrsBreakdown.finalRiskScore,
                    alertLevel = dmrsBreakdown.alertLevel
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // QUICK ACTION BUTTONS: 1-TAP DEMO & THREAT SIMULATION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onRunCommanderDemo() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = RadarCrimson),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "1-TAP FULL DEMO",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { onSimulateScenario(SimulationEngine.SimulationScenario.SCENARIO_3_CRITICAL) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = RadarAmber, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SIMULATE THREAT",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RadarAmber
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // FEED TOGGLE & SCENARIOS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onToggleCameraMode(!isCameraMode) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isCameraMode) "MODE: LIVE CAMERA" else "MODE: SIMULATION",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = RadarCyan
                )
            }

            OutlinedButton(
                onClick = { onSimulateScenario(SimulationEngine.SimulationScenario.SCENARIO_4_FALSE_POSITIVE) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "TEST FALSE+ FILTER",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextSecondary
                )
            }
        }
    }
}
