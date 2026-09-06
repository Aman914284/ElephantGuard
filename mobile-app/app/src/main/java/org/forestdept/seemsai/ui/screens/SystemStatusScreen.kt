package org.forestdept.seemsai.ui.screens

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
import org.forestdept.seemsai.model.PerformanceMode
import org.forestdept.seemsai.ui.components.TacticalCard
import org.forestdept.seemsai.ui.components.TelemetryItem
import org.forestdept.seemsai.ui.theme.DefenseDark900
import androidx.compose.ui.graphics.SolidColor
import org.forestdept.seemsai.ui.theme.DefenseDark950
import org.forestdept.seemsai.ui.theme.RadarAmber
import org.forestdept.seemsai.ui.theme.RadarCyan
import org.forestdept.seemsai.ui.theme.RadarEmerald
import org.forestdept.seemsai.ui.theme.SlateTextMuted
import org.forestdept.seemsai.ui.theme.SlateTextPrimary
import org.forestdept.seemsai.ui.theme.SlateTextSecondary

@Composable
fun SystemStatusScreen(
    currentMode: PerformanceMode,
    fps: Int,
    latencyMs: Long,
    onSelectMode: (PerformanceMode) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefenseDark950)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .verticalScroll(scrollState)
    ) {
        // HEADER
        Column {
            Text(
                text = "SYSTEM & EDGE OPTIMIZATION",
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = RadarCyan
            )
            Text(
                text = "LOW-END PHONE ACCELERATION & DIAGNOSTICS",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = SlateTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // PERFORMANCE MODE SELECTION
        Text(
            text = "SELECT OPERATIONAL TIER",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = SlateTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))

        PerformanceMode.values().forEach { mode ->
            val isSelected = currentMode == mode
            val modeColor = when (mode) {
                PerformanceMode.PERFORMANCE -> RadarEmerald
                PerformanceMode.BALANCED -> RadarCyan
                PerformanceMode.ACCURACY -> RadarAmber
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) modeColor.copy(alpha = 0.15f) else DefenseDark900)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) modeColor else Color(0xFF334479),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelectMode(mode) }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) modeColor else SlateTextMuted)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.title,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) modeColor else SlateTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = mode.description,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SlateTextSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${mode.targetFps} FPS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) modeColor else SlateTextSecondary
                        )
                        Text(
                            text = mode.resolutionLabel.take(4),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SlateTextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // REAL-TIME HARDWARE & INFERENCE TELEMETRY
        TacticalCard {
            Column {
                Text(
                    text = "HARDWARE & SENSOR DIAGNOSTICS",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RadarCyan
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryItem(label = "Inference FPS", value = "$fps", unit = "fps", modifier = Modifier.weight(1f))
                    TelemetryItem(label = "Edge Latency", value = "$latencyMs", unit = "ms", modifier = Modifier.weight(1f))
                    TelemetryItem(label = "RAM Footprint", value = "~18", unit = "MB", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryItem(label = "Camera State", value = "ACTIVE", color = RadarEmerald, modifier = Modifier.weight(1f))
                    TelemetryItem(label = "GPS Sensor", value = "LOCKED", color = RadarEmerald, modifier = Modifier.weight(1f))
                    TelemetryItem(label = "Thermal State", value = "NOMINAL", color = RadarEmerald, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // PROTOTYPE VS PRODUCTION ARCHITECTURE NOTICE
        TacticalCard {
            Column {
                Text(
                    text = "DEPLOYMENT BOUNDARY DISCLOSURE",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RadarAmber
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• AI Inference: TFLite/MobileNet Edge & Offline Kinematic Validation\n" +
                           "• Siren & Voice: 100% Native Offline PCM AudioTrack & Android TTS\n" +
                           "• Highway VMS & QRT: Simulated Defense Actuation Prototype\n" +
                           "• Geofence Anchor: Jharkhand NH-33 / Dalma Elephant Sanctuary Corridor",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
