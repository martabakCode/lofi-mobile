package com.loanfinancial.lofi.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Looper
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import android.location.LocationManager as AndroidLocationManager

/**
 * Native Android LocationManager implementation that does NOT require Google Play Services.
 * Uses the system LocationManager with GPS and Network providers.
 */
@Singleton
class NativeLocationManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : com.loanfinancial.lofi.core.location.LocationManager {
        private val locationManager: AndroidLocationManager by lazy {
            context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
        }

        override fun hasLocationPermission(): Boolean =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED

        /**
         * Check if GPS provider is available
         */
        fun isGpsProviderEnabled(): Boolean =
            locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER)

        /**
         * Check if Network provider is available
         */
        fun isNetworkProviderEnabled(): Boolean =
            locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)

        /**
         * Check if any location provider is available
         */
        fun isLocationEnabled(): Boolean =
            isGpsProviderEnabled() || isNetworkProviderEnabled()

        /**
         * Get the best available provider (GPS preferred, fallback to Network)
         */
        private fun getBestProvider(): String? =
            when {
                isGpsProviderEnabled() -> AndroidLocationManager.GPS_PROVIDER
                isNetworkProviderEnabled() -> AndroidLocationManager.NETWORK_PROVIDER
                else -> null
            }

        override suspend fun getCurrentLocation(): LofiLocationResult {
            if (!hasLocationPermission()) {
                return LofiLocationResult.PermissionDenied
            }

            if (!isLocationEnabled()) {
                return LofiLocationResult.LocationDisabled
            }

            val provider =
                getBestProvider()
                    ?: return LofiLocationResult.Error("No location provider available")

            return try {
                var location: Location? =
                    suspendCancellableCoroutine<Location?> { continuation ->
                        val locationListener =
                            object : LocationListener {
                                override fun onLocationChanged(location: Location) {
                                    continuation.resume(location)
                                    locationManager.removeUpdates(this)
                                }

                                override fun onProviderEnabled(provider: String) {}

                                override fun onProviderDisabled(provider: String) {
                                    if (continuation.isActive) {
                                        continuation.resume(null)
                                        locationManager.removeUpdates(this)
                                    }
                                }
                            }

                        try {
                            locationManager.requestLocationUpdates(
                                provider,
                                0L,
                                0f,
                                locationListener,
                                Looper.getMainLooper(),
                            )
                        } catch (e: SecurityException) {
                            continuation.resume(null)
                        }

                        continuation.invokeOnCancellation {
                            locationManager.removeUpdates(locationListener)
                        }

                        // Timeout after 30 seconds
                        android.os.Handler(Looper.getMainLooper()).postDelayed({
                            if (continuation.isActive) {
                                continuation.resume(null)
                                locationManager.removeUpdates(locationListener)
                            }
                        }, 30000)
                    }

                // Fallback to last known location if requestSingleUpdate failed/timed out
                if (location == null) {
                    location =
                        try {
                            locationManager.getLastKnownLocation(provider)
                        } catch (e: SecurityException) {
                            null
                        }
                    // Fallback to other provider if primary provider had no last known location
                    if (location == null) {
                        val otherProvider =
                            if (provider == AndroidLocationManager.GPS_PROVIDER) {
                                AndroidLocationManager.NETWORK_PROVIDER
                            } else {
                                AndroidLocationManager.GPS_PROVIDER
                            }
                        location =
                            try {
                                locationManager.getLastKnownLocation(otherProvider)
                            } catch (e: SecurityException) {
                                null
                            }
                    }
                }

                location?.let {
                    LofiLocationResult.Success(it.latitude, it.longitude)
                } ?: LofiLocationResult.Error("Unable to get current location")
            } catch (e: Exception) {
                LofiLocationResult.Error(e.message ?: "Unknown error occurred")
            }
        }

        override suspend fun getLastKnownLocation(): LofiLocationResult {
            if (!hasLocationPermission()) {
                return LofiLocationResult.PermissionDenied
            }

            return try {
                // Try GPS first, then Network
                val location: Location? =
                    try {
                        locationManager.getLastKnownLocation(AndroidLocationManager.GPS_PROVIDER)
                    } catch (e: SecurityException) {
                        null
                    }
                        ?: try {
                            locationManager.getLastKnownLocation(AndroidLocationManager.NETWORK_PROVIDER)
                        } catch (e: SecurityException) {
                            null
                        }

                location?.let {
                    LofiLocationResult.Success(it.latitude, it.longitude)
                } ?: LofiLocationResult.Error("No last known location available")
            } catch (e: Exception) {
                LofiLocationResult.Error(e.message ?: "Unknown error occurred")
            }
        }

        override fun requestLocationUpdates(): Flow<LofiLocationResult> =
            callbackFlow {
                if (!hasLocationPermission()) {
                    trySend(LofiLocationResult.PermissionDenied)
                    close()
                    return@callbackFlow
                }

                if (!isLocationEnabled()) {
                    trySend(LofiLocationResult.LocationDisabled)
                    close()
                    return@callbackFlow
                }

                val providers = mutableListOf<String>()
                if (isGpsProviderEnabled()) providers.add(AndroidLocationManager.GPS_PROVIDER)
                if (isNetworkProviderEnabled()) providers.add(AndroidLocationManager.NETWORK_PROVIDER)

                if (providers.isEmpty()) {
                    trySend(LofiLocationResult.Error("No location provider available"))
                    close()
                    return@callbackFlow
                }

                val locationListener =
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            trySend(
                                LofiLocationResult.Success(location.latitude, location.longitude),
                            )
                        }

                        override fun onProviderEnabled(provider: String) {}

                        override fun onProviderDisabled(provider: String) {}
                    }

                try {
                    providers.forEach { provider ->
                        locationManager.requestLocationUpdates(
                            provider,
                            10000L, // 10 seconds
                            10f, // 10 meters
                            locationListener,
                            Looper.getMainLooper(),
                        )
                    }
                } catch (e: SecurityException) {
                    trySend(LofiLocationResult.PermissionDenied)
                    close()
                    return@callbackFlow
                }

                awaitClose {
                    locationManager.removeUpdates(locationListener)
                }
            }
    }
