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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import org.forestdept.seemsai.engine.AutonomousResponseMatrix
import org.forestdept.seemsai.model.DefenseAlertLevel
import org.forestdept.seemsai.ui.components.TacticalCard
import org.forestdept.seemsai.ui.components.VmsSpeedLimitSign
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
fun AlertsResponseScreen(
    matrixActionState: AutonomousResponseMatrix.MatrixActionState,
    antiFatigueCooldownString: String,
    voiceLanguageMode: String,
    onTestSiren: () -> Unit,
    onTestVoice: () -> Unit,
    onSetVoiceLanguage: (String) -> Unit,
    onResetCooldown: () -> Unit,
    onTriggerNotification: () -> Unit
) {
    val scrollState = rememberScrollState()

    val levelColor = when (matrixActionState.level) {
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
        // TOP HEADER
        Column {
            Text(
                text = "AUTONOMOUS RESPONSE MATRIX",
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = RadarCyan
            )
            Text(
                text = "MULTI-TIER DEFENSE & CORRIDOR ACTUATION",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = SlateTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // DYNAMIC VARIABLE MESSAGE SIGN (VMS) HIGHWAY SIGNBOARD
        TacticalCard(borderColor = levelColor) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HIGHWAY VMS ADVISORY SIGN",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateTextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = matrixActionState.vmsMessage,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                VmsSpeedLimitSign(speedLimit = matrixActionState.vmsSpeedLimitKmh)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ANTI-FATIGUE COOLDOWN CONTROLLER
        TacticalCard {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ANTI-FATIGUE COOLDOWN CONTROLLER",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RadarCyan
                        )
                        Text(
                            text = antiFatigueCooldownString,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (antiFatigueCooldownString.contains("COOLDOWN")) RadarAmber else RadarEmerald
                        )
                    }
                    OutlinedButton(
                        onClick = { onResetCooldown() },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RESET", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Locks duplicate loud sirens/SMS for 10 min while maintaining continuous tracking.",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // AUDIO & VOICE TEST INTERFACE
        TacticalCard {
            Column {
                Text(
                    text = "ACOUSTIC SIREN & BILINGUAL VOICE CONTROLS",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RadarCyan
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Voice Language Selection Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("AUTO", "ENGLISH", "HINDI").forEach { lang ->
                        val isSelected = voiceLanguageMode.equals(lang, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) RadarCyan else DefenseDark900)
                                .border(1.dp, if (isSelected) RadarCyan else Color(0xFF334479), RoundedCornerShape(6.dp))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lang,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else SlateTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onTestSiren() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RadarCrimson),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TEST SIREN", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onTestVoice() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TEST VOICE", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { onTriggerNotification() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TRIGGER TEST NOTIFICATION", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // RESPONSE ACTIONS LIST
        TacticalCard {
            Column {
                Text(
                    text = "ACTIVE RESPONSE PROTOCOLS (${matrixActionState.level.levelName})",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = levelColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                matrixActionState.actionsList.forEach { action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "▶",
                            fontSize = 10.sp,
                            color = levelColor,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = action,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SlateTextPrimary
                        )
                    }
                }
            }
        }
    }
}
