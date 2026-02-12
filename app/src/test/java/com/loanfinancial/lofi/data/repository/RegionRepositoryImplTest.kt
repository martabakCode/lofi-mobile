package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.dao.RegionDao
import com.loanfinancial.lofi.data.local.database.AppDatabase
import com.loanfinancial.lofi.data.model.entity.ProvinceEntity
import com.loanfinancial.lofi.data.model.entity.RegencyEntity
import com.loanfinancial.lofi.data.remote.api.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class RegionRepositoryImplTest {
    @MockK
    private lateinit var regionApi: RegionApi

    @MockK
    private lateinit var database: AppDatabase

    @MockK
    private lateinit var regionDao: RegionDao

    private lateinit var repository: RegionRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { database.regionDao() } returns regionDao
        repository = RegionRepositoryImpl(regionApi, database)
    }

    @Test
    fun `getProvinces should emit loading then local then remote`() =
        runTest {
            // Arrange
            val localProvinces = listOf(ProvinceEntity("1", "Local"))
            val remoteProvinces = listOf(ProvinceResponse("1", "Remote"))

            coEvery { regionDao.getProvinces() } returns flowOf(localProvinces) andThen
                flowOf(
                    remoteProvinces.map {
                        com.loanfinancial.lofi.data.model.entity
                            .ProvinceEntity(it.id, it.name)
                    },
                )
            coEvery { regionApi.getProvinces(any()) } returns remoteProvinces
            coEvery { regionDao.insertProvinces(any()) } just Runs

            // Act
            val results = repository.getProvinces().toList()

            // Assert
            assertTrue(results[0] is Resource.Loading)
            assertTrue(results[1] is Resource.Success)
            assertEquals("Local", (results[1] as Resource.Success).data[0].name)
            assertTrue(results[2] is Resource.Success)
            assertEquals("Remote", (results[2] as Resource.Success).data[0].name)
        }

    @Test
    fun `getRegencies should emit loading then local then remote`() =
        runTest {
            // Arrange
            val localRegencies = listOf(RegencyEntity("1", "1", "Local"))
            val remoteRegencies = listOf(RegencyResponse("1", "1", "Remote"))

            coEvery { regionDao.getRegencies("1") } returns flowOf(localRegencies) andThen
                flowOf(
                    remoteRegencies.map {
                        com.loanfinancial.lofi.data.model.entity
                            .RegencyEntity(it.id, it.province_id, it.name)
                    },
                )
            coEvery { regionApi.getRegencies("1") } returns remoteRegencies
            coEvery { regionDao.insertRegencies(any()) } just Runs

            // Act
            val results = repository.getRegencies("1").toList()

            // Assert
            assertTrue(results[0] is Resource.Loading)
            assertTrue(results[1] is Resource.Success)
            assertEquals("Local", (results[1] as Resource.Success).data[0].name)
            assertTrue(results[2] is Resource.Success)
            assertEquals("Remote", (results[2] as Resource.Success).data[0].name)
        }
}
