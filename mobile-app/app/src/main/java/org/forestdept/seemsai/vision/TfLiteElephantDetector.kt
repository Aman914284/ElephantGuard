package org.forestdept.seemsai.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.ImageProxy
import org.forestdept.seemsai.model.DetectedTarget
import org.forestdept.seemsai.model.RawDetection
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Result data holder for multi-elephant on-device inference.
 */
data class ElephantDetectionOutput(
    val elephants: List<RawDetection>,
    val otherDetections: List<RawDetection>,
    val maxConfidence: Float,
    val isElephantDetected: Boolean,
    val inferenceLatencyMs: Long,
    val frameWidth: Int = 0,
    val frameHeight: Int = 0
)

/**
 * Real on-device AI Object Detection Engine using TensorFlow Lite (EfficientDet-Lite0 / COCO SSD).
 * Genuinely detects elephants from live CameraX preview frames and extracts normalized bounding boxes & confidence scores.
 */
class TfLiteElephantDetector(private val context: Context) {

    private var tfliteDetector: ObjectDetector? = null
    var isModelLoaded: Boolean = false
        private set
    var modelLoadError: String? = null
        private set
    var lastInferenceLatencyMs: Long = 0L
        private set

    // Concurrency guard: Ensure only one frame is processed at a time
    private val isAnalyzing = AtomicBoolean(false)

    init {
        initializeDetector()
    }

    private fun initializeDetector() {
        try {
            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setMaxResults(10)
                .setScoreThreshold(0.20f)
                .build()

            try {
                tfliteDetector = ObjectDetector.createFromFileAndOptions(
                    context,
                    "models/efficientdet_lite0.tflite",
                    options
                )
                isModelLoaded = true
                modelLoadError = null
                Log.e("ElephantDetector", "TFLite ObjectDetector initialized successfully with efficientdet_lite0.tflite (scoreThreshold=0.20f)")
            } catch (e1: Exception) {
                Log.w("ElephantDetector", "efficientdet_lite0.tflite init error: ${e1.message}, trying ssd_mobilenet_v1.tflite...")
                tfliteDetector = ObjectDetector.createFromFileAndOptions(
                    context,
                    "models/ssd_mobilenet_v1.tflite",
                    options
                )
                isModelLoaded = true
                modelLoadError = null
                Log.e("ElephantDetector", "TFLite ObjectDetector initialized successfully with ssd_mobilenet_v1.tflite (scoreThreshold=0.20f)")
            }
        } catch (e: Exception) {
            Log.e("ElephantDetector", "Failed to initialize TFLite ObjectDetector: ${e.message}", e)
            isModelLoaded = false
            modelLoadError = "TFLite Model Load Warning: ${e.localizedMessage ?: e.message}"
        }
    }

    /**
     * Runs genuine on-device AI inference on CameraX ImageProxy frame off the UI thread.
     * Extracts all detected elephants with normalized bounding boxes and individual confidence scores.
     */
    fun analyzeFrame(imageProxy: ImageProxy): ElephantDetectionOutput {
        // Concurrency guard: drop frame if busy
        if (!isAnalyzing.compareAndSet(false, true)) {
            imageProxy.close()
            return ElephantDetectionOutput(
                elephants = emptyList(),
                otherDetections = emptyList(),
                maxConfidence = 0f,
                isElephantDetected = false,
                inferenceLatencyMs = lastInferenceLatencyMs
            )
        }

        val startTime = System.currentTimeMillis()
        val elephantList = mutableListOf<RawDetection>()
        val otherList = mutableListOf<RawDetection>()

        try {
            if (tfliteDetector == null || !isModelLoaded) {
                Log.e("ElephantDetector", "analyzeFrame: detector is null or model not loaded!")
                return ElephantDetectionOutput(
                    elephants = emptyList(),
                    otherDetections = emptyList(),
                    maxConfidence = 0f,
                    isElephantDetected = false,
                    inferenceLatencyMs = 0L
                )
            }

            // Convert CameraX ImageProxy (RGBA / YUV) to Bitmap
            val bitmap = imageProxy.toBitmap()
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            // Rotate Bitmap to match display orientation
            val rotatedBitmap = if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            val imgW = rotatedBitmap.width.toFloat()
            val imgH = rotatedBitmap.height.toFloat()

            val tensorImage = TensorImage.fromBitmap(rotatedBitmap)
            val results = tfliteDetector?.detect(tensorImage) ?: emptyList()
            lastInferenceLatencyMs = System.currentTimeMillis() - startTime

            if (results.isNotEmpty()) {
                Log.e("ElephantDetector", "Frame analyzed in ${lastInferenceLatencyMs}ms: detected ${results.size} objects")
            }

            // Inspect and log all detections for debugging
            for (detection in results) {
                for (category in detection.categories) {
                    val rawLabel = category.label ?: ""
                    val rawDisplayName = category.displayName ?: ""
                    val normLabel = rawLabel.lowercase().trim()
                    val normDisplayName = rawDisplayName.lowercase().trim()
                    val score = category.score
                    val box = detection.boundingBox

                    // Log exact runtime output from ObjectDetector
                    Log.e("ElephantDetector", "DETECTION: label='$rawLabel' displayName='$rawDisplayName' score=$score bbox=[${box.left}, ${box.top}, ${box.right}, ${box.bottom}]")

                    // Normalize bounding box coordinates to 0.0 - 1.0 range based on rotated image dimensions
                    val normBox = RectF(
                        (box.left / imgW).coerceIn(0f, 1f),
                        (box.top / imgH).coerceIn(0f, 1f),
                        (box.right / imgW).coerceIn(0f, 1f),
                        (box.bottom / imgH).coerceIn(0f, 1f)
                    )

                    // Check both label and displayName for elephant matching
                    val isElephant = normLabel == "elephant" || normDisplayName == "elephant" ||
                            normLabel.contains("elephant") || normDisplayName.contains("elephant") ||
                            normLabel.contains("elephas") || normDisplayName.contains("elephas")

                    if (isElephant) {
                        elephantList.add(
                            RawDetection(
                                target = DetectedTarget.ELEPHANT,
                                confidence = score,
                                boundingBox = normBox,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        Log.e("ElephantDetector", "🐘 ELEPHANT DETECTED! label='$rawLabel' displayName='$rawDisplayName' score=${(score * 100).toInt()}% box=$normBox")
                    } else if (normLabel.contains("person") || normDisplayName.contains("person") ||
                        normLabel.contains("human") || normDisplayName.contains("human")) {
                        otherList.add(
                            RawDetection(
                                target = DetectedTarget.PERSON,
                                confidence = score,
                                boundingBox = normBox,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }

            val maxConf = if (elephantList.isNotEmpty()) elephantList.maxOf { it.confidence } else 0f

            return ElephantDetectionOutput(
                elephants = elephantList,
                otherDetections = otherList,
                maxConfidence = maxConf,
                isElephantDetected = elephantList.isNotEmpty(),
                inferenceLatencyMs = lastInferenceLatencyMs,
                frameWidth = rotatedBitmap.width,
                frameHeight = rotatedBitmap.height
            )
        } catch (e: Exception) {
            Log.e("ElephantDetector", "Error analyzing frame: ${e.message}", e)
            return ElephantDetectionOutput(
                elephants = emptyList(),
                otherDetections = emptyList(),
                maxConfidence = 0f,
                isElephantDetected = false,
                inferenceLatencyMs = 0L
            )
        } finally {
            imageProxy.close()
            isAnalyzing.set(false)
        }
    }

    fun close() {
        try {
            tfliteDetector = null
            isModelLoaded = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
