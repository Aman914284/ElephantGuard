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
import androidx.compose.foundation.rememberScrollState
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
import org.forestdept.seemsai.model.DefenseAlertLevel
import org.forestdept.seemsai.model.DmrsBreakdown
import org.forestdept.seemsai.ui.components.RiskProgressBar
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
fun RiskTelemetryScreen(
    dmrsBreakdown: DmrsBreakdown
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
        // HEADER
        Column {
            Text(
                text = "DMRS RISK ENGINE",
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = RadarCyan
            )
            Text(
                text = "DYNAMIC MULTI-PARAMETRIC RISK SCORE BREAKDOWN",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = SlateTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // FINAL RISK SCORE SUMMARY CARD
        TacticalCard(borderColor = levelColor) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMPUTED RISK SCORE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SlateTextSecondary
                        )
                        Text(
                            text = "${dmrsBreakdown.finalRiskScore} / 100",
                            fontSize = 28.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = levelColor
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(levelColor.copy(alpha = 0.2f))
                            .border(1.dp, levelColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = dmrsBreakdown.alertLevel.levelName,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = levelColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                RiskProgressBar(
                    riskScore = dmrsBreakdown.finalRiskScore,
                    alertLevel = dmrsBreakdown.alertLevel
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // MATHEMATICAL EQUATION DISPLAY
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DefenseDark900)
                .border(1.dp, Color(0x60334479), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = "DMRS FORMULA",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RadarCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Risk = min(100, round(0.20·C + 0.30·P + 0.25·V + 0.15·T + 0.10·H))",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5 PARAMETER BREAKDOWN CARDS
        DmrsParameterRow(
            symbol = "C",
            name = "Confidence Score",
            weight = "20%",
            value = dmrsBreakdown.confidenceScore,
            weightedVal = Math.round(0.20 * dmrsBreakdown.confidenceScore * 10.0) / 10.0,
            description = "Edge CV model detection confidence (0-100)"
        )

        DmrsParameterRow(
            symbol = "P",
            name = "Highway Proximity",
            weight = "30%",
            value = dmrsBreakdown.proximityScore,
            weightedVal = Math.round(0.30 * dmrsBreakdown.proximityScore * 10.0) / 10.0,
            description = "d ≤ 200m: 100 | 200-1500m: linear decay | >1500m: 0"
        )

        DmrsParameterRow(
            symbol = "V",
            name = "Kinematic Risk (TTC)",
            weight = "25%",
            value = dmrsBreakdown.kinematicsScore,
            weightedVal = Math.round(0.25 * dmrsBreakdown.kinematicsScore * 10.0) / 10.0,
            description = "TTC < 5 min: 100 | TTC 5-15 min: 60 | Parallel: 20"
        )

        DmrsParameterRow(
            symbol = "T",
            name = "Time Risk Index",
            weight = "15%",
            value = dmrsBreakdown.timeRiskScore,
            weightedVal = Math.round(0.15 * dmrsBreakdown.timeRiskScore * 10.0) / 10.0,
            description = "Night (18:00-06:00): 100 | Twilight: 50 | Day: 15"
        )

        DmrsParameterRow(
            symbol = "H",
            name = "Historical Hotspot Bias",
            weight = "10%",
            value = dmrsBreakdown.hotspotBiasScore,
            weightedVal = Math.round(0.10 * dmrsBreakdown.hotspotBiasScore * 10.0) / 10.0,
            description = "Dalma corridor historical conflict weighting (Fixed 85)"
        )
    }
}

@Composable
fun DmrsParameterRow(
    symbol: String,
    name: String,
    weight: String,
    value: Int,
    weightedVal: Double,
    description: String
) {
    TacticalCard(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, RadarCyan, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = symbol,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = RadarCyan
                    )
                }
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Column {
                    Text(
                        text = name,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                    Text(
                        text = description,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateTextMuted
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$value/100",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RadarCyan
                )
                Text(
                    text = "+$weightedVal ($weight)",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextSecondary
                )
            }
        }
    }
}
