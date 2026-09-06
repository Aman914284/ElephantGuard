package org.forestdept.seemsai.ui.screens.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.RectF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.forestdept.seemsai.audio.SirenPlayer
import org.forestdept.seemsai.config.ForestConfig
import org.forestdept.seemsai.engine.TemporalValidationEngine
import org.forestdept.seemsai.engine.ValidationResult
import org.forestdept.seemsai.location.LocationProvider
import org.forestdept.seemsai.model.RawDetection
import org.forestdept.seemsai.network.AlertNetworkManager
import org.forestdept.seemsai.notification.NotificationHelper
import org.forestdept.seemsai.risk.DmrsRiskEngine
import org.forestdept.seemsai.risk.RiskEvaluation
import org.forestdept.seemsai.risk.ThreatLevel
import org.forestdept.seemsai.ui.components.ForestPrimaryButton
import org.forestdept.seemsai.ui.theme.AlertRed
import org.forestdept.seemsai.ui.theme.AlertRedBg
import org.forestdept.seemsai.ui.theme.AlertRedBorder
import org.forestdept.seemsai.ui.theme.ForestCard
import org.forestdept.seemsai.ui.theme.ForestCardBorder
import org.forestdept.seemsai.ui.theme.ForestDark850
import org.forestdept.seemsai.ui.theme.ForestDark900
import org.forestdept.seemsai.ui.theme.ForestDark950
import org.forestdept.seemsai.ui.theme.ForestGreenLight
import org.forestdept.seemsai.ui.theme.SuccessGreen
import org.forestdept.seemsai.ui.theme.TextMutedSage
import org.forestdept.seemsai.ui.theme.TextSubtle
import org.forestdept.seemsai.ui.theme.TextWhite
import org.forestdept.seemsai.vision.ElephantDetectionOutput
import org.forestdept.seemsai.vision.TfLiteElephantDetector
import java.util.concurrent.Executors

@Composable
fun LiveCameraScreen(
    locationProvider: LocationProvider,
    sirenPlayer: SirenPlayer,
    alertNetworkManager: AlertNetworkManager,
    onNavigateToMap: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required for live AI detection", Toast.LENGTH_LONG).show()
        }
    }

    // Default to REAR camera for wildlife monitoring
    var currentLensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }

    // AI Detector & 3-of-5 Temporal Validation Engine
    val tfLiteDetector = remember { TfLiteElephantDetector(context) }
    val temporalEngine = remember { TemporalValidationEngine(windowSize = 5, requiredThreshold = 3, confidenceThreshold = 0.20f) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var detectedElephants by remember { mutableStateOf<List<RawDetection>>(emptyList()) }
    var maxConfidence by remember { mutableStateOf(0f) }
    var frameWidth by remember { mutableIntStateOf(480) }
    var frameHeight by remember { mutableIntStateOf(640) }

    var validationResult by remember {
        mutableStateOf(
            ValidationResult(
                isConfirmed = false,
                rawElephant = false,
                last5 = emptyList(),
                positiveCount = 0,
                totalFramesInWindow = 0,
                statusDescription = "No Elephant Detected",
                rawDetection = null
            )
        )
    }

    var inferenceLatencyMs by remember { mutableLongStateOf(0L) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    val isSirenActive by sirenPlayer.isSirenPlaying.collectAsState()
    val sirenCountdown by sirenPlayer.sirenCountdown.collectAsState()
    val gpsCoordinates by locationProvider.currentLocation.collectAsState()

    var latestRiskEvaluation by remember {
        mutableStateOf(
            DmrsRiskEngine.calculateRisk(confidence = 0f, elephantCount = 0)
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            sirenPlayer.stopSiren()
            cameraExecutor.shutdown()
            tfLiteDetector.close()
        }
    }

    // Emergency chain: REAL ELEPHANT -> 3/5 VALIDATION -> BIOLOGICAL LIVENESS -> DMRS RISK -> CRITICAL (80-100) -> SIREN (10s) + ALERT
    LaunchedEffect(validationResult.isConfirmed, validationResult.livenessResult.isPhotoSpoofSuspected, maxConfidence, detectedElephants.size) {
        if (validationResult.isConfirmed && detectedElephants.isNotEmpty() && !validationResult.livenessResult.isPhotoSpoofSuspected) {
            val eval = DmrsRiskEngine.calculateRisk(
                confidence = maxConfidence,
                elephantCount = detectedElephants.size.coerceAtLeast(1)
            )
            latestRiskEvaluation = eval

            // Strictly require validated elephant detection AND verified live motion AND CRITICAL risk (>= 80)
            if (eval.threatLevel == ThreatLevel.CRITICAL) {
                // 1. Play Emergency Siren automatically for 10 seconds (with instant user mute option)
                sirenPlayer.startSiren(durationSeconds = 10)

                // 2. Post Emergency Notification
                val locStr = if (gpsCoordinates.isGpsActive) {
                    String.format("%.4f°N, %.4f°E", gpsCoordinates.latitude, gpsCoordinates.longitude)
                } else {
                    "GPS UNAVAILABLE"
                }
                NotificationHelper.showEmergencyNotification(
                    context = context,
                    elephantCount = detectedElephants.size.coerceAtLeast(1),
                    confidencePercent = (maxConfidence * 100).toInt(),
                    riskScore = eval.riskScore,
                    locationStr = locStr
                )

                // 3. Broadcast Alert to FastAPI Backend
                alertNetworkManager.broadcastAlert(
                    count = detectedElephants.size.coerceAtLeast(1),
                    confidence = maxConfidence,
                    risk = eval,
                    location = gpsCoordinates
                )
            }
        }
    }

    // Bind camera lifecycle with optimized 640x480 resolution
    fun bindCameraToLifecycle(previewView: PreviewView, lensFacing: Int) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(android.util.Size(640, 480))
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val output: ElephantDetectionOutput = tfLiteDetector.analyzeFrame(imageProxy)
                    val vResult = temporalEngine.processDetections(output.elephants)

                    mainHandler.post {
                        inferenceLatencyMs = output.inferenceLatencyMs
                        detectedElephants = output.elephants
                        maxConfidence = output.maxConfidence
                        validationResult = vResult
                        if (output.frameWidth > 0 && output.frameHeight > 0) {
                            frameWidth = output.frameWidth
                            frameHeight = output.frameHeight
                        }
                    }
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Camera initialization error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Rebind camera when lens facing changes
    LaunchedEffect(currentLensFacing, hasCameraPermission) {
        previewViewRef?.let { pView ->
            if (hasCameraPermission) {
                bindCameraToLifecycle(pView, currentLensFacing)
            }
        }
    }

    // Forest Helpline Dialer Action
    fun openForestDialer() {
        val number = ForestConfig.FOREST_DEPT_HELPLINE
        if (number.isBlank()) {
            Toast.makeText(context, "No Forest Helpline number configured.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to launch dialer", Toast.LENGTH_LONG).show()
        }
    }

    val isElephantPresent = detectedElephants.isNotEmpty()
    val isConfirmed = validationResult.isConfirmed

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForestDark950)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==========================================
            // TOP BAR: [ ← ] LIVE CAMERA [ 📞 ] [ 🔄 ]
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ForestDark900)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    sirenPlayer.stopSiren()
                    onBack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = TextWhite
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LIVE CAMERA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = TextWhite
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isConfirmed) AlertRed else if (isElephantPresent) Color(0xFFF59E0B) else ForestGreenLight)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isConfirmed) "CRITICAL ALERT ACTIVE" else if (isElephantPresent) "VALIDATING ELEPHANT" else "AI MONITORING ACTIVE",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isConfirmed) AlertRed else if (isElephantPresent) Color(0xFFF59E0B) else ForestGreenLight
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { openForestDialer() }) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Forest Department Helpline",
                            tint = ForestGreenLight
                        )
                    }

                    IconButton(
                        onClick = {
                            currentLensFacing = if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                            val lensName = if (currentLensFacing == CameraSelector.LENS_FACING_FRONT) "Front Camera" else "Rear Camera"
                            Toast.makeText(context, "Switched to $lensName", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Switch Camera",
                            tint = TextWhite
                        )
                    }
                }
            }

            // ==========================================
            // MAIN LIVE CAMERA & REAL AI OVERLAY
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                previewViewRef = this
                                bindCameraToLifecycle(this, currentLensFacing)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    val boxColor = if (isConfirmed) AlertRed else if (isElephantPresent) Color(0xFFF59E0B) else SuccessGreen
                    val textPaint = remember {
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 28f
                            isFakeBoldText = true
                            isAntiAlias = true
                        }
                    }
                    val bgTagPaint = remember(isConfirmed, isElephantPresent) {
                        android.graphics.Paint().apply {
                            color = if (isConfirmed) android.graphics.Color.argb(220, 220, 38, 38)
                            else android.graphics.Color.argb(220, 217, 119, 6)
                            style = android.graphics.Paint.Style.FILL
                            isAntiAlias = true
                        }
                    }

                    // Pixel-perfect aspect-ratio & rotation canvas mapping
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        val imgW = if (frameWidth > 0) frameWidth.toFloat() else 480f
                        val imgH = if (frameHeight > 0) frameHeight.toFloat() else 640f
                        val isFront = (currentLensFacing == CameraSelector.LENS_FACING_FRONT)

                        val viewAspect = w / h
                        val imgAspect = imgW / imgH
                        val scale = if (viewAspect > imgAspect) (w / imgW) else (h / imgH)
                        val scaledW = imgW * scale
                        val scaledH = imgH * scale
                        val dx = (w - scaledW) / 2f
                        val dy = (h - scaledH) / 2f

                        if (isElephantPresent) {
                            for (elephant in detectedElephants) {
                                val normBox = elephant.boundingBox
                                val normL = if (isFront) (1f - normBox.right) else normBox.left
                                val normR = if (isFront) (1f - normBox.left) else normBox.right
                                val left = (dx + (normL * scaledW)).coerceIn(0f, w)
                                val right = (dx + (normR * scaledW)).coerceIn(0f, w)
                                val top = (dy + (normBox.top * scaledH)).coerceIn(0f, h)
                                val bottom = (dy + (normBox.bottom * scaledH)).coerceIn(0f, h)

                                val boxWidth = (right - left).coerceAtLeast(10f)
                                val boxHeight = (bottom - top).coerceAtLeast(10f)

                                // Main Bounding Box
                                drawRoundRect(
                                    color = boxColor,
                                    topLeft = Offset(left, top),
                                    size = Size(boxWidth, boxHeight),
                                    cornerRadius = CornerRadius(10f, 10f),
                                    style = Stroke(width = 4f)
                                )

                                // Tactical Corner Brackets
                                val cornerLen = 22f
                                drawLine(boxColor, Offset(left - 2f, top), Offset(left + cornerLen, top), strokeWidth = 8f)
                                drawLine(boxColor, Offset(left, top - 2f), Offset(left, top + cornerLen), strokeWidth = 8f)
                                drawLine(boxColor, Offset(right - cornerLen, top), Offset(right + 2f, top), strokeWidth = 8f)
                                drawLine(boxColor, Offset(right, top - 2f), Offset(right + cornerLen, top), strokeWidth = 8f)
                                drawLine(boxColor, Offset(left - 2f, bottom), Offset(left + cornerLen, bottom), strokeWidth = 8f)
                                drawLine(boxColor, Offset(left, bottom - cornerLen), Offset(left, bottom + 2f), strokeWidth = 8f)
                                drawLine(boxColor, Offset(right - cornerLen, bottom), Offset(right + 2f, bottom), strokeWidth = 8f)
                                drawLine(boxColor, Offset(right, bottom - cornerLen), Offset(right + 2f, bottom), strokeWidth = 8f)

                                // Individual Confidence Tag
                                val confPercent = (elephant.confidence * 100).toInt()
                                val labelText = "ELEPHANT $confPercent%"
                                val textWidth = textPaint.measureText(labelText)
                                val tagTop = (top - 36f).coerceAtLeast(4f)
                                val tagBottom = tagTop + 32f
                                val tagLeft = left
                                val tagRight = left + textWidth + 18f

                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawRoundRect(
                                        tagLeft, tagTop, tagRight, tagBottom, 8f, 8f, bgTagPaint
                                    )
                                    canvas.nativeCanvas.drawText(
                                        labelText, tagLeft + 8f, tagBottom - 9f, textPaint
                                    )
                                }
                            }
                        } else {
                            val cx = w / 2f
                            val cy = h / 2f
                            val guideSize = 70f
                            drawRoundRect(
                                color = Color(0x28438A56),
                                topLeft = Offset(cx - guideSize, cy - guideSize),
                                size = Size(guideSize * 2, guideSize * 2),
                                cornerRadius = CornerRadius(8f, 8f),
                                style = Stroke(width = 2f)
                            )
                        }
                    }

                    // Floating Measurable Debug Box (Top Overlay)
                    val last5Formatted = if (validationResult.last5.isNotEmpty()) {
                        validationResult.last5.joinToString(", ") { if (it) "YES" else "NO" }
                    } else {
                        "NO, NO, NO, NO, NO"
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ForestDark950.copy(alpha = 0.90f))
                            .border(1.dp, if (isConfirmed) AlertRed else ForestCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RAW ELEPHANT: ${if (validationResult.rawElephant) "YES" else "NO"} • VALIDATED: ${if (isConfirmed) "YES" else "NO"}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isConfirmed) AlertRed else if (validationResult.rawElephant) Color(0xFFF59E0B) else TextWhite
                        )
                        Text(
                            text = "LAST 5: [$last5Formatted] (${validationResult.positiveCount}/5)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMutedSage
                        )
                    }

                    // Floating Map Shortcut Button
                    Button(
                        onClick = onNavigateToMap,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestDark900.copy(alpha = 0.9f),
                            contentColor = ForestGreenLight
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp)
                            .border(1.dp, ForestGreenLight.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "LIVE MAP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = ForestGreenLight,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Permission Required",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Elephant Guard requires camera access for real-time edge AI wildlife monitoring.",
                            fontSize = 13.sp,
                            color = TextMutedSage,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        ForestPrimaryButton(
                            text = "GRANT CAMERA PERMISSION",
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            icon = Icons.Default.CameraAlt
                        )
                    }
                }
            }

            // ==========================================
            // BOTTOM TELEMETRY & PHASE 3F EMERGENCY CARD
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = ForestCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    val isSpoof = validationResult.livenessResult.isPhotoSpoofSuspected
                    // Status Banner [ AI Monitoring Active / Elephant Detected / Anti-Prank Photo Spoof ]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSpoof) Color(0x33F59E0B)
                                else if (isConfirmed) AlertRedBg
                                else if (isElephantPresent) Color(0x2BF59E0B)
                                else Color(0x1F22C55E)
                            )
                            .border(
                                1.dp,
                                if (isSpoof) Color(0xFFF59E0B)
                                else if (isConfirmed) AlertRedBorder
                                else if (isElephantPresent) Color(0x66F59E0B)
                                else Color(0x4422C55E),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSpoof) Icons.Default.Info else if (isConfirmed) Icons.Default.Warning else if (isElephantPresent) Icons.Default.Info else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isSpoof) Color(0xFFF59E0B) else if (isConfirmed) AlertRed else if (isElephantPresent) Color(0xFFF59E0B) else SuccessGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isSpoof) "🛡️ ANTI-PRANK: STATIC 2D PHOTO DETECTED"
                                    else if (isConfirmed) "🚨 ELEPHANT DETECTED • ${latestRiskEvaluation.threatLevel.name} RISK"
                                    else if (isElephantPresent) "ELEPHANT DETECTED (VALIDATING ${validationResult.positiveCount}/5)"
                                    else "No Elephant Detected",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSpoof) Color(0xFFF59E0B) else if (isConfirmed) AlertRed else if (isElephantPresent) Color(0xFFF59E0B) else SuccessGreen
                                )
                                Text(
                                    text = if (isSpoof) "Static image on screen/paper • Sirens & Community Alerts Locked"
                                    else if (isConfirmed) "DMRS Score: ${latestRiskEvaluation.riskScore}/100 • Liveness Verified • ${(maxConfidence * 100).toInt()}% Conf • ${detectedElephants.size.coerceAtLeast(1)} Target(s)"
                                    else if (isElephantPresent) "Temporal & liveness validation in progress (${validationResult.positiveCount}/5 frames)"
                                    else "Corridor Status: Clear • Dalma AI Monitoring Active",
                                    fontSize = 11.sp,
                                    color = TextMutedSage
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Emergency Controls: Manual Siren Test Toggle + Live Map + QRT Call
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Siren Action Button (Shows 10s countdown timer and instant Off Siren option when active)
                        Button(
                            onClick = {
                                if (isSirenActive) sirenPlayer.stopSiren() else sirenPlayer.startSiren(10)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSirenActive) AlertRed else ForestDark900,
                                contentColor = if (isSirenActive) TextWhite else ForestGreenLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(42.dp)
                                .border(1.dp, if (isSirenActive) AlertRed else ForestGreenLight.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = if (isSirenActive) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSirenActive) "OFF SIREN (${sirenCountdown}s)" else "SIREN TEST (10s)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Spatial Map Shortcut
                        Button(
                            onClick = onNavigateToMap,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestDark900,
                                contentColor = ForestGreenLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .border(1.dp, ForestGreenLight.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "VIEW MAP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Forest Helpline Call Button
                        Button(
                            onClick = { openForestDialer() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreenLight,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "CALL 1926", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Diagnostic info footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "AI: EfficientDet-Lite0 • Latency: ${inferenceLatencyMs}ms",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSubtle
                        )
                        Text(
                            text = if (gpsCoordinates.isGpsActive) {
                                "GPS: ${String.format("%.4f, %.4f", gpsCoordinates.latitude, gpsCoordinates.longitude)} (±${gpsCoordinates.accuracyMeters.toInt()}m)"
                            } else {
                                "GPS: LOCATION UNAVAILABLE"
                            },
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (gpsCoordinates.isGpsActive) ForestGreenLight else Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }
    }
}
