package org.forestdept.seemsai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.forestdept.seemsai.model.DefenseAlertLevel
import org.forestdept.seemsai.model.TrackedEntity
import org.forestdept.seemsai.ui.theme.DefenseCard
import org.forestdept.seemsai.ui.theme.DefenseCardBorder
import org.forestdept.seemsai.ui.theme.DefenseDark950
import org.forestdept.seemsai.ui.theme.RadarAmber
import org.forestdept.seemsai.ui.theme.RadarCrimson
import org.forestdept.seemsai.ui.theme.RadarCyan
import org.forestdept.seemsai.ui.theme.RadarEmerald
import org.forestdept.seemsai.ui.theme.SlateTextMuted
import org.forestdept.seemsai.ui.theme.SlateTextPrimary
import org.forestdept.seemsai.ui.theme.SlateTextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TacticalCard(
    modifier: Modifier = Modifier,
    borderColor: Color = DefenseCardBorder,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DefenseCard)
    ) {
        Box(modifier = Modifier.padding(14.dp)) {
            content()
        }
    }
}

@Composable
fun TelemetryItem(
    label: String,
    value: String,
    unit: String = "",
    color: Color = RadarCyan,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = SlateTextMuted,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = color
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun VmsSpeedLimitSign(
    speedLimit: Int?,
    modifier: Modifier = Modifier
) {
    if (speedLimit == null) {
        Box(
            modifier = modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F172A))
                .border(3.dp, RadarEmerald, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CLEAR",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RadarEmerald
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .border(4.dp, RadarCrimson, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$speedLimit",
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "KM/H",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RadarCrimson
                )
            }
        }
    }
}

@Composable
fun RiskProgressBar(
    riskScore: Int,
    alertLevel: DefenseAlertLevel,
    modifier: Modifier = Modifier
) {
    val barColor = when (alertLevel) {
        DefenseAlertLevel.GREEN -> RadarEmerald
        DefenseAlertLevel.YELLOW -> Color(0xFFEAB308)
        DefenseAlertLevel.AMBER -> RadarAmber
        DefenseAlertLevel.RED -> RadarCrimson
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DYNAMIC MULTI-PARAMETRIC RISK (DMRS)",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = SlateTextSecondary
            )
            Text(
                text = "$riskScore / 100",
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (riskScore / 100f).coerceIn(0.02f, 1.0f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(barColor)
            )
        }
    }
}

/**
 * RadarSpatialCanvas: Vector GIS spatial display centered on Dalma Elephant Corridor.
 */
@Composable
fun RadarSpatialCanvas(
    trackedEntity: TrackedEntity,
    alertLevel: DefenseAlertLevel,
    modifier: Modifier = Modifier
) {
    val ringColor = when (alertLevel) {
        DefenseAlertLevel.RED -> RadarCrimson
        DefenseAlertLevel.AMBER -> RadarAmber
        DefenseAlertLevel.YELLOW -> Color(0xFFEAB308)
        DefenseAlertLevel.GREEN -> RadarCyan
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(DefenseDark950)
            .border(1.dp, DefenseCardBorder, RoundedCornerShape(8.dp))
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = minOf(cx, cy) * 0.90f

        // Draw tactical radar grid circles
        drawCircle(
            color = Color(0x30334479),
            radius = maxR,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f)
        )
        drawCircle(
            color = Color(0x25334479),
            radius = maxR * 0.65f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.2f)
        )
        drawCircle(
            color = Color(0x20334479),
            radius = maxR * 0.35f,
            center = Offset(cx, cy),
            style = Stroke(width = 1f)
        )

        // Radar crosshairs
        drawLine(
            color = Color(0x20334479),
            start = Offset(cx, 0f),
            end = Offset(cx, size.height),
            strokeWidth = 1f
        )
        drawLine(
            color = Color(0x20334479),
            start = Offset(0f, cy),
            end = Offset(size.width, cy),
            strokeWidth = 1f
        )

        // Amber Geofence Zone (2.5 km)
        drawCircle(
            color = Color(0x30F59E0B),
            radius = maxR * 0.75f,
            center = Offset(cx, cy),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f))
        )

        // Red Geofence Zone (1.0 km)
        drawCircle(
            color = Color(0x45EF4444),
            radius = maxR * 0.38f,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5f)
        )

        // NH-33 Highway Corridor Axis Line (Angled corridor)
        drawLine(
            color = Color(0xFF94A3B8),
            start = Offset(cx - maxR * 0.85f, cy + maxR * 0.70f),
            end = Offset(cx + maxR * 0.85f, cy - maxR * 0.70f),
            strokeWidth = 3.5f
        )

        // QRT Patrol Unit Location
        val qrtX = cx + maxR * 0.40f
        val qrtY = cy + maxR * 0.20f
        drawCircle(
            color = Color(0xFF38BDF8),
            radius = 6f,
            center = Offset(qrtX, qrtY)
        )

        // Elephant Target Position relative to center
        val distRatio = (trackedEntity.distanceToHighwayMeters / 1500.0).coerceIn(0.05, 1.2).toFloat()
        val headingRad = Math.toRadians(trackedEntity.headingDegrees - 90.0)
        val elephantX = cx + (cos(headingRad) * maxR * 0.55f * distRatio).toFloat()
        val elephantY = cy + (sin(headingRad) * maxR * 0.55f * distRatio).toFloat()

        // Elephant Warning Rings
        drawCircle(
            color = ringColor.copy(alpha = 0.3f),
            radius = 18f,
            center = Offset(elephantX, elephantY)
        )
        drawCircle(
            color = ringColor,
            radius = 7f,
            center = Offset(elephantX, elephantY)
        )

        // Velocity Heading Vector Arrow
        val arrowLength = (trackedEntity.velocityKmh * 3.5).coerceIn(15.0, 50.0).toFloat()
        val vectorEndX = elephantX + (cos(headingRad) * arrowLength).toFloat()
        val vectorEndY = elephantY + (sin(headingRad) * arrowLength).toFloat()
        drawLine(
            color = ringColor,
            start = Offset(elephantX, elephantY),
            end = Offset(vectorEndX, vectorEndY),
            strokeWidth = 2.5f
        )
    }
}
