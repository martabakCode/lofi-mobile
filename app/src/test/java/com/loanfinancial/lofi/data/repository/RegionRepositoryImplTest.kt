package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.data.local.dao.RegionDao
import com.loanfinancial.lofi.data.model.entity.ProvinceEntity
import com.loanfinancial.lofi.data.model.entity.CityEntity
import com.loanfinancial.lofi.data.model.entity.DistrictEntity
import com.loanfinancial.lofi.data.model.entity.VillageEntity
import com.loanfinancial.lofi.data.remote.api.RegionApi
import com.loanfinancial.lofi.data.remote.api.CityResponse
import com.loanfinancial.lofi.data.remote.api.DistrictResponse
import com.loanfinancial.lofi.data.remote.api.ProvinceResponse
import com.loanfinancial.lofi.data.remote.api.VillageResponse
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class RegionRepositoryImplTest {
    @MockK
    private lateinit var regionApi: RegionApi

    @MockK
    private lateinit var regionDao: RegionDao

    private lateinit var repository: RegionRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = RegionRepositoryImpl(regionApi, regionDao)
    }

    @Test
    fun `getProvinces returns provinces from API`() =
        runTest {
            val response = ProvinceResponse(
                success = true,
                data = listOf(
                    com.loanfinancial.lofi.data.remote.api.ProvinceData(
                        id = "1",
                        name = "DKI Jakarta"
                    )
                )
            )

            coEvery { regionApi.getProvinces() } returns Response.success(response)
            coEvery { regionDao.insertProvinces(any()) } just runs

            val result = repository.getProvinces()

            assertEquals(1, result.size)
            assertEquals("DKI Jakarta", result[0].name)
        }

    @Test
    fun `getCities returns cities for province`() =
        runTest {
            val response = CityResponse(
                success = true,
                data = listOf(
                    com.loanfinancial.lofi.data.remote.api.CityData(
                        id = "1",
                        provinceId = "1",
                        name = "Jakarta Pusat"
                    )
                )
            )

            coEvery { regionApi.getCities("1") } returns Response.success(response)
            coEvery { regionDao.insertCities(any()) } just runs

            val result = repository.getCities("1")

            assertEquals(1, result.size)
            assertEquals("Jakarta Pusat", result[0].name)
        }

    @Test
    fun `getDistricts returns districts for city`() =
        runTest {
            val response = DistrictResponse(
                success = true,
                data = listOf(
                    com.loanfinancial.lofi.data.remote.api.DistrictData(
                        id = "1",
                        cityId = "1",
                        name = "Cempaka Putih"
                    )
                )
            )

            coEvery { regionApi.getDistricts("1") } returns Response.success(response)
            coEvery { regionDao.insertDistricts(any()) } just runs

            val result = repository.getDistricts("1")

            assertEquals(1, result.size)
            assertEquals("Cempaka Putih", result[0].name)
        }

    @Test
    fun `getVillages returns villages for district`() =
        runTest {
            val response = VillageResponse(
                success = true,
                data = listOf(
                    com.loanfinancial.lofi.data.remote.api.VillageData(
                        id = "1",
                        districtId = "1",
                        name = "Cempaka Putih Timur"
                    )
                )
            )

            coEvery { regionApi.getVillages("1") } returns Response.success(response)
            coEvery { regionDao.insertVillages(any()) } just runs

            val result = repository.getVillages("1")

            assertEquals(1, result.size)
            assertEquals("Cempaka Putih Timur", result[0].name)
        }

    @Test
    fun `getCachedProvinces returns from database`() =
        runTest {
            val provinces = listOf(
                ProvinceEntity(
                    id = "1",
                    name = "Cached Province"
                )
            )

            coEvery { regionDao.getAllProvinces() } returns provinces

            val result = repository.getCachedProvinces()

            assertEquals(1, result.size)
            assertEquals("Cached Province", result[0].name)
        }

    @Test
    fun `getCachedCities returns from database`() =
        runTest {
            val cities = listOf(
                CityEntity(
                    id = "1",
                    provinceId = "1",
                    name = "Cached City"
                )
            )

            coEvery { regionDao.getCitiesForProvince("1") } returns cities

            val result = repository.getCachedCities("1")

            assertEquals(1, result.size)
            assertEquals("Cached City", result[0].name)
        }

    @Test
    fun `clearCache removes all region data`() =
        runTest {
            coEvery { regionDao.clearAll() } just runs

            repository.clearCache()

            coVerify { regionDao.clearAll() }
        }
}
