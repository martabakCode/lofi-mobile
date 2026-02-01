# Native Android Location Implementation Plan

## Overview
This document outlines the implementation of a native Android LocationManager that does NOT require Google Play Services. This is useful for devices without Google Play Services (e.g., Huawei devices) or when you want to avoid Google dependencies.

## Current Implementation

The project currently uses:
- `play-services-location` (Google Play Services Location)
- `FusedLocationProviderClient` for location updates

## Proposed Native Implementation

### 1. NativeLocationManager.kt

Create a new implementation using Android's native [`LocationManager`](https://developer.android.com/reference/android/location/LocationManager):

```kotlin
package com.loanfinancial.lofi.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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

/**
 * Native Android LocationManager implementation that does NOT require Google Play Services.
 * Uses the system LocationManager with GPS and Network providers.
 */
@Singleton
class NativeLocationManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : LocationManager {
    
    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    /**
     * Check if Network provider is available
     */
    fun isNetworkProviderEnabled(): Boolean =
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

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
            isGpsProviderEnabled() -> LocationManager.GPS_PROVIDER
            isNetworkProviderEnabled() -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

    override suspend fun getCurrentLocation(): LofiLocationResult {
        if (!hasLocationPermission()) {
            return LofiLocationResult.PermissionDenied
        }

        if (!isLocationEnabled()) {
            return LofiLocationResult.LocationDisabled
        }

        val provider = getBestProvider()
            ?: return LofiLocationResult.Error("No location provider available")

        return try {
            val location = suspendCancellableCoroutine<Location?> { continuation ->
                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        continuation.resume(location) {}
                        locationManager.removeUpdates(this)
                    }

                    override fun onProviderEnabled(provider: String) {}

                    override fun onProviderDisabled(provider: String) {
                        if (continuation.isActive) {
                            continuation.resume(null) {}
                            locationManager.removeUpdates(this)
                        }
                    }
                }

                try {
                    locationManager.requestSingleUpdate(
                        provider,
                        locationListener,
                        Looper.getMainLooper(),
                    )
                } catch (e: SecurityException) {
                    continuation.resume(null) {}
                }

                continuation.invokeOnCancellation {
                    locationManager.removeUpdates(locationListener)
                }

                // Timeout after 30 seconds
                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    if (continuation.isActive) {
                        continuation.resume(null) {}
                        locationManager.removeUpdates(locationListener)
                    }
                }, 30000)
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
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } catch (e: SecurityException) {
                    null
                } ?: try {
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
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
            if (isGpsProviderEnabled()) providers.add(LocationManager.GPS_PROVIDER)
            if (isNetworkProviderEnabled()) providers.add(LocationManager.NETWORK_PROVIDER)

            if (providers.isEmpty()) {
                trySend(LofiLocationResult.Error("No location provider available"))
                close()
                return@callbackFlow
            }

            val locationListener = object : LocationListener {
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
```

### 2. Update LocationManager Interface

The existing [`LocationManager`](app/src/main/java/com/loanfinancial/lofi/core/location/LocationManager.kt:40) interface already supports both implementations. No changes needed.

### 3. Update HardwareModule.kt

Modify [`HardwareModule.kt`](app/src/main/java/com/loanfinancial/lofi/core/di/HardwareModule.kt:26) to provide the native implementation:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {
    @Binds
    @Singleton
    abstract fun bindLocationManager(
        impl: NativeLocationManager,  // Change from LocationManagerImpl to NativeLocationManager
    ): LocationManager

    // ... other bindings
}
```

Or create a factory pattern to switch between implementations:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context,
        preferencesManager: PreferencesManager,
    ): LocationManager {
        return if (preferencesManager.useNativeLocation) {
            NativeLocationManager(context)
        } else {
            LocationManagerImpl(context)
        }
    }
}
```

### 4. Required Permissions

The existing permissions in [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml:7) are sufficient:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

### 5. Remove Google Play Services Dependency (Optional)

If you want to completely remove Google Play Services dependency, remove from [`app/build.gradle.kts`](app/build.gradle.kts:132):

```kotlin
// Remove this line
implementation(libs.play.services.location)
```

And from [`gradle/libs.versions.toml`](gradle/libs.versions.toml:99):

```toml
# Remove these lines
playServicesLocation = "21.1.0"
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "playServicesLocation" }
```

## Key Differences: Native vs Google Play Services

| Feature | Native LocationManager | Google Play Services |
|---------|----------------------|---------------------|
| Google Play Required | No | Yes |
| Fused Provider | No (manual GPS/Network) | Yes (automatic fusion) |
| Accuracy | Good (GPS) / Moderate (Network) | Excellent (fused) |
| Battery Efficiency | Moderate | Excellent |
| Indoor Accuracy | Poor | Good |
| Availability | All Android devices | Devices with Play Services |

## Usage Example

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val locationManager: LocationManager
) : ViewModel() {

    fun getLocation() {
        viewModelScope.launch {
            when (val result = locationManager.getCurrentLocation()) {
                is LofiLocationResult.Success -> {
                    val lat = result.latitude
                    val lng = result.longitude
                    // Use location
                }
                is LofiLocationResult.Error -> {
                    // Handle error
                }
                LofiLocationResult.PermissionDenied -> {
                    // Request permission
                }
                LofiLocationResult.LocationDisabled -> {
                    // Prompt user to enable location
                }
            }
        }
    }
}
```

## Migration Steps

1. Create `NativeLocationManager.kt` file
2. Update `HardwareModule.kt` to bind `NativeLocationManager` instead of `LocationManagerImpl`
3. Test on devices without Google Play Services
4. Optionally remove `play-services-location` dependency

## Testing Checklist

- [ ] Test on device with GPS only
- [ ] Test on device with Network only
- [ ] Test with both providers disabled
- [ ] Test permission denial
- [ ] Test location timeout
- [ ] Test on Huawei device (no Play Services)
