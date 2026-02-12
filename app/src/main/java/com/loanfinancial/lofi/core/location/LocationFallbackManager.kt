package com.loanfinancial.lofi.core.location

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.repository.RegionRepositoryImpl
import com.loanfinancial.lofi.domain.repository.IUserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

interface LocationFallbackManager {
    suspend fun getLocation(): LocationResult
}

@Singleton
class LocationFallbackManagerImpl @Inject constructor(
    private val locationManager: LocationManager,
    private val regionRepository: RegionRepositoryImpl, // Use implementation if interface doesn't expose methods effectively or use usecases. Assuming repository for now.
    private val userRepository: IUserRepository,
) : LocationFallbackManager {
    override suspend fun getLocation(): LocationResult {
        // Step 1: Try GPS first (high accuracy)
        when (val gpsResult = locationManager.getCurrentLocation()) {
            is LofiLocationResult.Success -> {
                return LocationResult.Success(
                    latitude = gpsResult.latitude,
                    longitude = gpsResult.longitude,
                    source = "GPS"
                )
            }
            else -> {
                // GPS failed or permission denied, continue to fallback
            }
        }

        // Step 2: Try user's biodata location from profile
        val biodataLocation = getBiodataLocation()
        if (biodataLocation != null) {
            return LocationResult.Success(
                latitude = biodataLocation.first,
                longitude = biodataLocation.second,
                source = "USER_BIODATA"
            )
        }

        // Step 3: Use default Jakarta coordinates
        // We could add subdistrict lookup here if regionRepository supports it
        // For now, simpler fallback to "Jakarta" or major city logic
        
        return LocationResult.Success(
            latitude = -6.2088,
            longitude = 106.8456,
            source = "DEFAULT_JAKARTA"
        )
    }

    private suspend fun getBiodataLocation(): Pair<Double, Double>? {
        return try {
            val profile = userRepository.getUserProfile().first {
                it !is Resource.Loading
            }
            if (profile is Resource.Success && profile.data?.biodata != null) {
                val biodata = profile.data!!.biodata!!
                // Use district/city/province to find coordinates
                // Since we don't have a geocoder here, we rely on a static map
                getDistrictCoordinates(biodata.city) ?: getDistrictCoordinates("jakarta")
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getDistrictCoordinates(cityName: String?): Pair<Double, Double>? {
        if (cityName == null) return null
        val normalizedCity = cityName.lowercase().trim()
        val cityCoordinates = mapOf(
            "jakarta" to Pair(-6.2088, 106.8456),
            "jakarta utara" to Pair(-6.1383, 106.8639),
            "jakarta barat" to Pair(-6.1683, 106.7588),
            "jakarta pusat" to Pair(-6.1865, 106.8341),
            "jakarta selatan" to Pair(-6.2615, 106.8106),
            "jakarta timur" to Pair(-6.2250, 106.9004),
            "surabaya" to Pair(-7.2575, 112.7521),
            "bandung" to Pair(-6.9175, 107.6191),
            "medan" to Pair(3.5952, 98.6722),
            "semarang" to Pair(-6.9932, 110.4203),
            "yogyakarta" to Pair(-7.7956, 110.3695),
            "makassar" to Pair(-5.1477, 119.4327),
            "palembang" to Pair(-2.9761, 104.7754),
            "denpasar" to Pair(-8.6705, 115.2126),
            "malang" to Pair(-7.9666, 112.6326),
            "tangerang" to Pair(-6.1702, 106.6403),
            "bekasi" to Pair(-6.2349, 106.9896),
            "depok" to Pair(-6.4025, 106.7942),
            "bogor" to Pair(-6.5944, 106.7892),
        )
        return cityCoordinates[normalizedCity]
    }
}

sealed class LocationResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val source: String
    ) : LocationResult()
    
    data class Error(val message: String) : LocationResult()
}
