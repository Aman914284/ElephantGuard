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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import org.forestdept.seemsai.model.IncidentRecord
import org.forestdept.seemsai.ui.components.TacticalCard
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
fun IncidentHistoryScreen(
    incidents: List<IncidentRecord>,
    onClearAll: () -> Unit,
    onExportJson: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefenseDark950)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "INCIDENT TELEMETRY LOGS",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = RadarCyan
                )
                Text(
                    text = "TOTAL EVENTS RECORDED: ${incidents.size}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextSecondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { onExportJson() },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EXPORT", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                OutlinedButton(
                    onClick = { onClearAll() },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = RadarCrimson)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (incidents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO RECENT INCIDENTS LOGGED",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(incidents, key = { it.id }) { incident ->
                    IncidentItemCard(incident = incident)
                }
            }
        }
    }
}

@Composable
fun IncidentItemCard(incident: IncidentRecord) {
    val levelColor = when (incident.alertLevel) {
        "RED" -> RadarCrimson
        "AMBER" -> RadarAmber
        "YELLOW" -> Color(0xFFEAB308)
        else -> RadarEmerald
    }

    TacticalCard(borderColor = levelColor.copy(alpha = 0.5f)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(levelColor.copy(alpha = 0.2f))
                            .border(1.dp, levelColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = incident.alertLevel,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = levelColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = incident.id,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                }
                Text(
                    text = incident.formattedTime.takeLast(8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "🐘 ${incident.targetName} (${incident.confidence}% Conf)",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = SlateTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Risk: ${incident.riskScore}/100",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = levelColor
                )
                Text(
                    text = "TTC: ${incident.ttcString}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = RadarCyan
                )
                Text(
                    text = "Speed: ${incident.speedKmh} km/h",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextSecondary
                )
                Text(
                    text = "Hwy: ${incident.distanceToHwy.toInt()}m",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextSecondary
                )
            }
        }
    }
}
