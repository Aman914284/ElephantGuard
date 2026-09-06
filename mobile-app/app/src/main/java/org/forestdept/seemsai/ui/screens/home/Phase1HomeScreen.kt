package org.forestdept.seemsai.ui.screens.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.forestdept.seemsai.auth.AuthViewModel
import org.forestdept.seemsai.ui.components.ElephantGuardHeader
import org.forestdept.seemsai.ui.components.ForestPrimaryButton
import org.forestdept.seemsai.ui.components.ForestSecondaryButton
import org.forestdept.seemsai.ui.components.PoweredBySeemsAiBadge
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
fun Phase1HomeScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.currentUser
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForestDark950)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Logo
            ElephantGuardHeader(
                title = "ELEPHANT GUARD",
                subtitle = "Protect Wildlife. Protect Lives."
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Phase 1 Status Banner
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(ForestGreenPrimary.copy(alpha = 0.18f))
                    .border(1.dp, ForestGreenLight.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ForestGreenLight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Phase 1 Complete",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenLight
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Session Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForestCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
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
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = user?.fullName ?: "Wildlife Patrol Officer",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = user?.role ?: "Forest Defense Matrix",
                                fontSize = 12.sp,
                                color = TextMutedSage
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    androidx.compose.material3.HorizontalDivider(
                        color = ForestCardBorder,
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "EMAIL / CONTACT",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSubtle,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = user?.email ?: "demo@elephantguard.org",
                                fontSize = 13.sp,
                                color = TextMutedSage,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ACCOUNT TYPE",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSubtle,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (user?.isDemo == true) "Local Demo Account" else "Registered Local Account",
                                fontSize = 13.sp,
                                color = ForestGreenLight,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Project Roadmap / Modular Architecture Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForestDark900),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PROJECT ROADMAP & ARCHITECTURE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = TextSubtle
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PhaseRoadmapItem(
                        phaseNumber = "PHASE 1",
                        title = "Authentication & Demo Accounts",
                        status = "Active / Complete",
                        icon = Icons.Default.CheckCircle,
                        isComplete = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PhaseRoadmapItem(
                        phaseNumber = "PHASE 2",
                        title = "Live Camera & AI Detection",
                        status = "Next Up (Modular Handoff)",
                        icon = Icons.Default.Videocam,
                        isComplete = false
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PhaseRoadmapItem(
                        phaseNumber = "PHASE 3",
                        title = "Live Map & Geofence Zones",
                        status = "Planned",
                        icon = Icons.Default.LocationOn,
                        isComplete = false
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PhaseRoadmapItem(
                        phaseNumber = "PHASE 4",
                        title = "Real-Time Alerts & Notification Engine",
                        status = "Planned",
                        icon = Icons.Default.Notifications,
                        isComplete = false
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PhaseRoadmapItem(
                        phaseNumber = "PHASE 5",
                        title = "Emergency & Citizen SOS Response",
                        status = "Planned",
                        icon = Icons.Default.Emergency,
                        isComplete = false
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PhaseRoadmapItem(
                        phaseNumber = "PHASE 6",
                        title = "Reports & Telemetry Logs",
                        status = "Planned",
                        icon = Icons.Default.QueryStats,
                        isComplete = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            ForestSecondaryButton(
                text = "LOGOUT",
                onClick = {
                    authViewModel.logout(onLogout)
                },
                icon = Icons.AutoMirrored.Filled.ExitToApp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PhaseRoadmapItem(
    phaseNumber: String,
    title: String,
    status: String,
    icon: ImageVector,
    isComplete: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isComplete) ForestCard else ForestDark850)
            .border(
                1.dp,
                if (isComplete) ForestGreenLight.copy(alpha = 0.4f) else ForestCardBorder,
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isComplete) ForestGreenPrimary.copy(alpha = 0.25f) else ForestDark950),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isComplete) ForestGreenLight else TextSubtle,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phaseNumber,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isComplete) ForestGreenLight else TextSubtle
                )
                Text(
                    text = status,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isComplete) SuccessGreen else TextSubtle,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isComplete) TextWhite else TextMutedSage
            )
        }
    }
}
