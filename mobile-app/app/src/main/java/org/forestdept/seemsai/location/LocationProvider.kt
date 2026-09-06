package org.forestdept.seemsai.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GpsCoordinates(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracyMeters: Float = 0f,
    val altitudeMeters: Double = 0.0,
    val timestamp: Long = 0L,
    val isGpsActive: Boolean = false,
    val locationName: String = "GPS LOCATION UNAVAILABLE"
)

/**
 * High-precision GPS Location Provider for Dalma Wildlife Sanctuary / NH-33 Corridor.
 * Does NOT substitute fake coordinates if GPS is unavailable.
 */
class LocationProvider(private val context: Context) {

    private val defaultCoordinates = GpsCoordinates(
        latitude = 0.0,
        longitude = 0.0,
        accuracyMeters = 0f,
        altitudeMeters = 0.0,
        timestamp = 0L,
        isGpsActive = false,
        locationName = "GPS LOCATION UNAVAILABLE"
    )

    private val _isPermissionGranted = MutableStateFlow(hasLocationPermission())
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _currentLocation = MutableStateFlow(defaultCoordinates)
    val currentLocation: StateFlow<GpsCoordinates> = _currentLocation.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    init {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            startLocationUpdates()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshPermissionState(): Boolean {
        val granted = hasLocationPermission()
        _isPermissionGranted.value = granted
        if (granted) {
            startLocationUpdates()
        }
        return granted
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            _isPermissionGranted.value = false
            return
        }
        _isPermissionGranted.value = true

        // 1. Immediately fetch cached last known location from all available providers
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                val gpsLoc = try { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: Exception) { null }
                val netLoc = try { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { null }
                val passiveLoc = try { locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) } catch (e: Exception) { null }
                
                val bestCached = gpsLoc ?: netLoc ?: passiveLoc
                if (bestCached != null) {
                    updateLocation(bestCached)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Fused Location Client
        try {
            fusedLocationClient?.lastLocation?.addOnSuccessListener { loc ->
                if (loc != null) {
                    updateLocation(loc)
                }
            }

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
                .setMinUpdateIntervalMillis(1000L)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { updateLocation(it) }
                }
            }

            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Fallback LocationManager continuous listener
        fallbackToLocationManager()
    }

    @SuppressLint("MissingPermission")
    private fun fallbackToLocationManager() {
        if (!hasLocationPermission()) return
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    updateLocation(location)
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 2f, listener, Looper.getMainLooper())
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 2f, listener, Looper.getMainLooper())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateLocation(loc: Location) {
        _currentLocation.value = GpsCoordinates(
            latitude = loc.latitude,
            longitude = loc.longitude,
            accuracyMeters = loc.accuracy,
            altitudeMeters = loc.altitude,
            timestamp = loc.time,
            isGpsActive = true,
            locationName = "Dalma / NH-33 (${String.format("%.4f°N, %.4f°E", loc.latitude, loc.longitude)})"
        )
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
        }
    }
}
