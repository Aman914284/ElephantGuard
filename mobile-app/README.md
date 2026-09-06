# Elephant Guard — Mobile Edge AI Application

Native Android application built with **Kotlin**, **Jetpack Compose**, **CameraX**, and **TensorFlow Lite** for zero-latency, on-device wildlife detection and automated deterrence.

## Architecture Highlights

- 🧠 **On-Device Edge AI Vision**: Real-time bounding box inference on low-power devices without needing continuous internet connectivity.
- 📡 **Offline-First Mesh Alerting**: Stores detection timestamps, GPS coordinates, and telemetry locally; syncs via WebSocket/REST when connectivity is restored.
- 🔊 **Automated Bio-Acoustic Defense**: Automatically triggers ultrasonic frequency patterns upon continuous elephant verification within high-risk zones.
- 📍 **Precision Geofencing**: Dual-band GPS tracking calculating real-time distance vectors relative to vulnerable agricultural perimeters.

## Building & Running

### Requirements
- Android Studio Hedgehog (2023.1.1+) or newer
- JDK 17 / 21
- Android SDK 34 (UpsideDownCake)

### Build Debug APK via Gradle
```bash
# On Linux/macOS
./gradlew assembleDebug

# On Windows
gradlew.bat assembleDebug
```

The compiled APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.
