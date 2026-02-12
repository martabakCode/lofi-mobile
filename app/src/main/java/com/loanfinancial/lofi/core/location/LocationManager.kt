package com.loanfinancial.lofi.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

sealed class LofiLocationResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
    ) : LofiLocationResult()

    data class Error(
        val message: String,
    ) : LofiLocationResult()

    data object PermissionDenied : LofiLocationResult()

    data object LocationDisabled : LofiLocationResult()
}

interface LocationManager {
    suspend fun getCurrentLocation(): LofiLocationResult

    suspend fun getLastKnownLocation(): LofiLocationResult

    fun hasLocationPermission(): Boolean

    fun requestLocationUpdates(): Flow<LofiLocationResult>
}

@Singleton
class LocationManagerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : LocationManager {
        private val fusedLocationClient: FusedLocationProviderClient by lazy {
            LocationServices.getFusedLocationProviderClient(context)
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

        override suspend fun getCurrentLocation(): LofiLocationResult {
            if (!hasLocationPermission()) {
                return LofiLocationResult.PermissionDenied
            }

            return try {
                val locationRequest =
                    LocationRequest
                        .Builder(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            10000L,
                        ).apply {
                            setWaitForAccurateLocation(true)
                            setMinUpdateIntervalMillis(5000L)
                        }.build()

                val location =
                    suspendCancellableCoroutine<Location?> { continuation ->
                        val locationCallback =
                            object : LocationCallback() {
                                override fun onLocationResult(result: LocationResult) {
                                    continuation.resume(result.lastLocation)
                                    fusedLocationClient.removeLocationUpdates(this)
                                }
                            }

                        try {
                            fusedLocationClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.getMainLooper(),
                            )
                        } catch (e: SecurityException) {
                            continuation.resume(null)
                        }

                        continuation.invokeOnCancellation {
                            fusedLocationClient.removeLocationUpdates(locationCallback)
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
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    LofiLocationResult.Success(it.latitude, it.longitude)
                } ?: LofiLocationResult.Error("No last known location available")
            } catch (e: SecurityException) {
                LofiLocationResult.PermissionDenied
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

                val locationRequest =
                    LocationRequest
                        .Builder(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            10000L,
                        ).apply {
                            setMinUpdateIntervalMillis(5000L)
                        }.build()

                val locationCallback =
                    object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            result.lastLocation?.let { location ->
                                trySend(
                                    LofiLocationResult.Success(location.latitude, location.longitude),
                                )
                            }
                        }
                    }

                try {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper(),
                    )
                } catch (e: SecurityException) {
                    trySend(LofiLocationResult.PermissionDenied)
                    close()
                    return@callbackFlow
                }

                awaitClose {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
    }
