package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.core.network.BaseResponse
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.dao.ProfileDraftDao
import com.loanfinancial.lofi.data.local.dao.UserDao
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.data.model.dto.UserUpdateRequest
import com.loanfinancial.lofi.data.model.entity.ProfileDraftEntity
import com.loanfinancial.lofi.data.model.entity.UserProfileEntity
import com.loanfinancial.lofi.data.remote.api.UserApi
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class UserRepositoryImplTest {
    @MockK
    private lateinit var userApi: UserApi

    @MockK
    private lateinit var profileDraftDao: ProfileDraftDao

    @MockK
    private lateinit var userDao: UserDao

    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = UserRepositoryImpl(userApi, profileDraftDao, userDao)
    }

    @Test
    fun `updateProfile should emit loading then success`() =
        runTest {
            // Arrange
            val request = UserUpdateRequest(
                fullName = "Test User",
                phoneNumber = "08123456789",
                incomeSource = "WAGES",
                incomeType = "MONTHLY",
                monthlyIncome = 5000000.0,
                nik = "1234567890123456",
                dateOfBirth = "1990-01-01",
                placeOfBirth = "Jakarta",
                city = "Jakarta",
                address = "Jl. Test No. 1",
                province = "DKI Jakarta",
                district = "Test District",
                subDistrict = "Test SubDistrict",
                postalCode = "12345",
                gender = "MALE",
                maritalStatus = "SINGLE",
                occupation = "Software Engineer"
            )
            val updateData = UserUpdateData(
                id = "user123",
                fullName = "Test User",
                email = "test@example.com",
                phoneNumber = "08123456789",
                profilePictureUrl = null,
                branch = null,
                biodata = null,
                product = null,
                pinSet = true,
                profileCompleted = true
            )
            val baseResponse = BaseResponse(
                success = true,
                message = "Success",
                data = updateData
            )

            coEvery { userApi.updateProfile(request) } returns Response.success(baseResponse)

            // Act
            val results = repository.updateProfile(request).toList()

            // Assert
            assertTrue(results[0] is Resource.Loading)
            assertTrue(results[1] is Resource.Success)
            assertEquals("user123", (results[1] as Resource.Success).data.id)
        }

    @Test
    fun `getUserProfile should emit local then remote success`() =
        runTest {
            // Arrange
            val localProfile = UserProfileEntity("user123", "{\"id\":\"user123\",\"fullName\":\"Local\"}")
            val updateData = UserUpdateData(
                id = "user123",
                fullName = "Remote",
                email = "remote@example.com",
                phoneNumber = "08123456789",
                profilePictureUrl = null,
                branch = null,
                biodata = null,
                product = null,
                pinSet = true,
                profileCompleted = true
            )
            val baseResponse = BaseResponse(
                success = true,
                message = "Success",
                data = updateData
            )

            coEvery { userDao.getUserProfile() } returns flowOf(localProfile)
            coEvery { userApi.getUserProfile() } returns Response.success(baseResponse)
            coEvery { userDao.clearUserProfile() } just Runs
            coEvery { userDao.insertUserProfile(any()) } just Runs

            // Act
            val results = repository.getUserProfile().toList()

            // Assert
            assertTrue(results[0] is Resource.Loading)
            assertTrue(results[1] is Resource.Success)
            assertEquals("Local", (results[1] as Resource.Success).data.fullName)
            assertTrue(results[2] is Resource.Success)
            assertEquals("Remote", (results[2] as Resource.Success).data.fullName)
        }
}
