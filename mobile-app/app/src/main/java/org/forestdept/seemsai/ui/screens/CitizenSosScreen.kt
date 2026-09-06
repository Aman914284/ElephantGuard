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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.forestdept.seemsai.model.CitizenSosReport
import org.forestdept.seemsai.ui.components.TacticalCard
import org.forestdept.seemsai.ui.theme.DefenseDark900
import org.forestdept.seemsai.ui.theme.DefenseDark950
import org.forestdept.seemsai.ui.theme.RadarCrimson
import org.forestdept.seemsai.ui.theme.RadarCyan
import org.forestdept.seemsai.ui.theme.RadarEmerald
import org.forestdept.seemsai.ui.theme.SlateTextMuted
import org.forestdept.seemsai.ui.theme.SlateTextPrimary
import org.forestdept.seemsai.ui.theme.SlateTextSecondary

@Composable
fun CitizenSosScreen(
    currentLat: Double,
    currentLng: Double,
    onSubmitReport: (CitizenSosReport) -> Unit,
    onDispatchQrt: () -> Unit
) {
    val scrollState = rememberScrollState()

    var elephantCount by remember { mutableIntStateOf(2) }
    var direction by remember { mutableStateOf("South toward NH-33") }
    var notes by remember { mutableStateOf("Two adult tuskers spotted near Asanbani boundary trees.") }
    var showReportSuccess by remember { mutableStateOf(false) }
    var isQrtDispatched by remember { mutableStateOf(false) }

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
                text = "CITIZEN SOS & QRT DISPATCH",
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = RadarCyan
            )
            Text(
                text = "COMMUNITY REPORTING & FOREST RANGER OPERATIONS",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = SlateTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // FOREST QRT DISPATCH CONSOLE
        TacticalCard(borderColor = if (isQrtDispatched) RadarCrimson else Color(0xFF0284C7)) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "QUICK RESPONSE TEAM (QRT)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = if (isQrtDispatched) "STATUS: EN ROUTE TO NH-33 (ETA 3m)" else "STATUS: PATROL READY / ASANBANI",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isQrtDispatched) RadarCrimson else RadarEmerald
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isQrtDispatched) Color(0xFF7F1D1D) else Color(0xFF064E3B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isQrtDispatched) "DISPATCHED" else "READY",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isQrtDispatched) RadarCrimson else RadarEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        isQrtDispatched = true
                        onDispatchQrt()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isQrtDispatched) Color(0xFF991B1B) else Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        if (isQrtDispatched) Icons.Default.Check else Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isQrtDispatched) "QRT PATROL UNIT DISPATCHED (SIMULATED)" else "DISPATCH QRT INTERCEPT (SIMULATE)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CITIZEN REPORTING FORM
        TacticalCard {
            Column {
                Text(
                    text = "CITIZEN WILDLIFE SIGHTING REPORT",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RadarCyan
                )
                Spacer(modifier = Modifier.height(8.dp))

                // GPS Coordinate Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DefenseDark900)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "GPS: ${String.format("%.4f° N, %.4f° E", currentLat, currentLng)} (Dalma Sector)",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Elephant count selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Elephant Count:",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateTextPrimary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 2, 3, 5, 8).forEach { count ->
                            val isSelected = elephantCount == count
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) RadarCyan else DefenseDark900)
                                    .border(1.dp, if (isSelected) RadarCyan else Color(0xFF334479), RoundedCornerShape(4.dp))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else SlateTextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Direction
                OutlinedTextField(
                    value = direction,
                    onValueChange = { direction = it },
                    label = { Text("Observed Movement Vector", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SlateTextPrimary,
                        unfocusedTextColor = SlateTextPrimary,
                        focusedBorderColor = RadarCyan,
                        unfocusedBorderColor = Color(0xFF334479)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Incident Description / Landmarks", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SlateTextPrimary,
                        unfocusedTextColor = SlateTextPrimary,
                        focusedBorderColor = RadarCyan,
                        unfocusedBorderColor = Color(0xFF334479)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val report = CitizenSosReport(
                            latitude = currentLat,
                            longitude = currentLng,
                            elephantCount = elephantCount,
                            movementDirection = direction,
                            notes = notes
                        )
                        onSubmitReport(report)
                        showReportSuccess = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RadarEmerald),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SUBMIT CITIZEN SOS REPORT",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                if (showReportSuccess) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ Report successfully recorded and mapped to Forest Defense Matrix.",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = RadarEmerald
                    )
                }
            }
        }
    }
}
