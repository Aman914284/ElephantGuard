package org.forestdept.seemsai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import org.forestdept.seemsai.audio.SirenPlayer
import org.forestdept.seemsai.auth.AuthRepository
import org.forestdept.seemsai.auth.AuthViewModel
import org.forestdept.seemsai.location.LocationProvider
import org.forestdept.seemsai.network.AlertNetworkManager
import org.forestdept.seemsai.notification.NotificationHelper
import org.forestdept.seemsai.ui.screens.auth.CreateDemoAccountScreen
import org.forestdept.seemsai.ui.screens.auth.LoginScreen
import org.forestdept.seemsai.ui.screens.camera.LiveCameraScreen
import org.forestdept.seemsai.ui.screens.home.HomeScreen
import org.forestdept.seemsai.ui.screens.map.RealTimeMapScreen
import org.forestdept.seemsai.ui.theme.AlertRed
import org.forestdept.seemsai.ui.theme.ForestDark950
import org.forestdept.seemsai.ui.theme.SEEMSAITheme

/**
 * Screen routes for Elephant Guard.
 */
sealed class AppScreen {
    object Login : AppScreen()
    object CreateAccount : AppScreen()
    object Home : AppScreen()
    object LiveCamera : AppScreen()
    object RealTimeMap : AppScreen()
}

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var authViewModel: AuthViewModel
    private lateinit var locationProvider: LocationProvider
    private lateinit var sirenPlayer: SirenPlayer
    private lateinit var alertNetworkManager: AlertNetworkManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val camera = permissions[Manifest.permission.CAMERA] ?: false

        if (fine || coarse) {
            locationProvider.startLocationUpdates()
        }
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        } else {
            locationProvider.startLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(applicationContext)

        authRepository = AuthRepository(applicationContext)
        authViewModel = AuthViewModel(authRepository)
        locationProvider = LocationProvider(applicationContext)
        sirenPlayer = SirenPlayer()
        alertNetworkManager = AlertNetworkManager(applicationContext, sirenPlayer, locationProvider)

        // Request core permissions immediately on app launch
        requestAppPermissions()

        setContent {
            SEEMSAITheme {
                ElephantGuardApp(
                    authViewModel = authViewModel,
                    locationProvider = locationProvider,
                    sirenPlayer = sirenPlayer,
                    alertNetworkManager = alertNetworkManager,
                    onRequestPermissions = { requestAppPermissions() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sirenPlayer.stopSiren()
        locationProvider.stopLocationUpdates()
    }

    @Composable
    private fun ElephantGuardApp(
        authViewModel: AuthViewModel,
        locationProvider: LocationProvider,
        sirenPlayer: SirenPlayer,
        alertNetworkManager: AlertNetworkManager,
        onRequestPermissions: () -> Unit = {}
    ) {
        // Persistent Login: Automatically open Home if session exists; else open Login
        var currentScreen by remember {
            mutableStateOf<AppScreen>(
                if (authRepository.getCurrentUser() != null) AppScreen.Home else AppScreen.Login
            )
        }

        val isSirenPlaying by sirenPlayer.isSirenPlaying.collectAsState()
        val sirenCountdown by sirenPlayer.sirenCountdown.collectAsState()

        // Handle hardware/gesture back press safely
        when (currentScreen) {
            AppScreen.CreateAccount -> {
                BackHandler {
                    currentScreen = AppScreen.Login
                }
            }
            AppScreen.Home -> {
                BackHandler {
                    Toast.makeText(this@MainActivity, "Press Logout to return to Login screen", Toast.LENGTH_SHORT).show()
                }
            }
            AppScreen.LiveCamera -> {
                BackHandler {
                    sirenPlayer.stopSiren()
                    currentScreen = AppScreen.Home
                }
            }
            AppScreen.RealTimeMap -> {
                BackHandler {
                    currentScreen = AppScreen.Home
                }
            }
            AppScreen.Login -> {
                // Default Android back behavior (exit app)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ForestDark950)
        ) {
            when (currentScreen) {
                AppScreen.Login -> {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onNavigateToCreateAccount = {
                            authViewModel.clearMessages()
                            currentScreen = AppScreen.CreateAccount
                        },
                        onLoginSuccess = {
                            currentScreen = AppScreen.Home
                        }
                    )
                }

                AppScreen.CreateAccount -> {
                    CreateDemoAccountScreen(
                        authViewModel = authViewModel,
                        onNavigateBackToLogin = {
                            authViewModel.clearMessages()
                            currentScreen = AppScreen.Login
                        },
                        onAccountCreatedSuccess = {
                            currentScreen = AppScreen.Home
                        }
                    )
                }

                AppScreen.Home -> {
                    HomeScreen(
                        authViewModel = authViewModel,
                        alertNetworkManager = alertNetworkManager,
                        locationProvider = locationProvider,
                        onRequestLocationPermission = onRequestPermissions,
                        onNavigateToCamera = {
                            currentScreen = AppScreen.LiveCamera
                        },
                        onNavigateToMap = {
                            currentScreen = AppScreen.RealTimeMap
                        },
                        onLogout = {
                            currentScreen = AppScreen.Login
                            Toast.makeText(this@MainActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                AppScreen.LiveCamera -> {
                    LiveCameraScreen(
                        locationProvider = locationProvider,
                        sirenPlayer = sirenPlayer,
                        alertNetworkManager = alertNetworkManager,
                        onNavigateToMap = {
                            currentScreen = AppScreen.RealTimeMap
                        },
                        onBack = {
                            sirenPlayer.stopSiren()
                            currentScreen = AppScreen.Home
                        }
                    )
                }

                AppScreen.RealTimeMap -> {
                    RealTimeMapScreen(
                        locationProvider = locationProvider,
                        alertNetworkManager = alertNetworkManager,
                        onBack = {
                            currentScreen = AppScreen.Home
                        }
                    )
                }
            }

            // Global High-Priority Siren Control Bar (Visible across all screens when active)
            if (isSirenPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 42.dp, start = 16.dp, end = 16.dp)
                        .zIndex(99f)
                        .align(Alignment.TopCenter)
                ) {
                    androidx.compose.material3.Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = AlertRed,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🚨 EMERGENCY SIREN ACTIVE (${sirenCountdown}s)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Acoustic deterrent sounding (10s auto-stop)",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Button(
                                onClick = { sirenPlayer.stopSiren() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = AlertRed
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = AlertRed
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "OFF SIREN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
