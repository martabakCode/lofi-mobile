package com.loanfinancial.lofi.data.repository

import app.cash.turbine.test
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.dao.ProfileDraftDao
import com.loanfinancial.lofi.data.local.dao.UserDao
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.data.remote.api.UserApi
import com.loanfinancial.lofi.domain.model.User
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
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
    fun `getUserProfile should return user data on success`() = runTest {
        // Arrange
        val userData = UserUpdateData(
            id = "user_123",
            fullName = "Test User",
            email = "test@test.com",
            phoneNumber = "+6281234567890",
            profilePictureUrl = null,
            biodata = null,
            branch = null,
            product = null,
            pinSet = true,
            profileCompleted = true
        )
        
        coEvery { userApi.getUserProfile() } returns Response.success(
            com.loanfinancial.lofi.data.model.dto.UserUpdateResponse(
                success = true,
                message = "Success",
                data = userData
            )
        )

        // Act & Assert
        repository.getUserProfile().test {
            assertTrue(awaitItem() is Resource.Loading)
            val success = awaitItem() as Resource.Success
            assertEquals("Test User", success.data?.fullName)
            awaitComplete()
        }
    }

    @Test
    fun `getUserProfile should return error on failure`() = runTest {
        // Arrange
        coEvery { userApi.getUserProfile() } returns Response.error(
            401,
            ResponseBody.create(null, "Unauthorized")
        )

        // Act & Assert
        repository.getUserProfile().test {
            assertTrue(awaitItem() is Resource.Loading)
            val error = awaitItem() as Resource.Error
            assertNotNull(error.message)
            awaitComplete()
        }
    }

    @Test
    fun `updateProfile should return success on valid data`() = runTest {
        // Arrange
        val request = com.loanfinancial.lofi.data.model.dto.UserUpdateRequest(
            fullName = "Updated Name",
            phoneNumber = "+6281234567890",
            incomeSource = "Salary",
            incomeType = "Permanent",
            monthlyIncome = 10000000.0,
            nik = "1234567890123456",
            dateOfBirth = "1990-01-01",
            gender = "MALE",
            address = "Jakarta",
            city = "Jakarta",
            province = "DKI Jakarta",
            postalCode = "12345"
        )
        
        coEvery { userApi.updateProfile(request) } returns Response.success(
            com.loanfinancial.lofi.data.model.dto.UserUpdateResponse(
                success = true,
                message = "Profile updated",
                data = null
            )
        )

        // Act
        val result = repository.updateProfile(request)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateProfile should return error on failure`() = runTest {
        // Arrange
        val request = com.loanfinancial.lofi.data.model.dto.UserUpdateRequest(
            fullName = "Test",
            phoneNumber = "+6281234567890",
            incomeSource = "Salary",
            incomeType = "Permanent",
            monthlyIncome = 10000000.0,
            nik = "1234567890123456",
            dateOfBirth = "1990-01-01",
            gender = "MALE",
            address = "Jakarta",
            city = "Jakarta",
            province = "DKI Jakarta",
            postalCode = "12345"
        )
        
        coEvery { userApi.updateProfile(request) } returns Response.error(
            400,
            ResponseBody.create(null, "Bad Request")
        )

        // Act
        val result = repository.updateProfile(request)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `uploadProfilePicture should return success`() = runTest {
        // Arrange
        val filePath = "/path/to/photo.jpg"
        coEvery { userApi.uploadProfilePicture(any()) } returns Response.success(
            com.loanfinancial.lofi.data.model.dto.UserUpdateResponse(
                success = true,
                message = "Photo uploaded",
                data = null
            )
        )

        // Act
        val result = repository.uploadProfilePicture(filePath)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getCachedUser should return user from dao`() = runTest {
        // Arrange
        val userEntity = com.loanfinancial.lofi.data.model.entity.UserEntity(
            id = "user_123",
            fullName = "Test User",
            username = "testuser",
            email = "test@test.com",
            phoneNumber = "+6281234567890",
            profilePictureUrl = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { userDao.getUserById("user_123") } returns userEntity

        // Act
        val result = repository.getCachedUser("user_123")

        // Assert
        assertNotNull(result)
        assertEquals("Test User", result?.fullName)
    }

    @Test
    fun `saveProfileDraft should call dao`() = runTest {
        // Arrange
        val draft = com.loanfinancial.lofi.data.model.entity.ProfileDraftEntity(
            userId = "user_123",
            draftData = "{}",
            updatedAt = System.currentTimeMillis()
        )
        coEvery { profileDraftDao.insertOrUpdate(draft) } just Runs

        // Act
        repository.saveProfileDraft(draft)

        // Assert
        coVerify { profileDraftDao.insertOrUpdate(draft) }
    }

    @Test
    fun `getProfileDraft should return draft from dao`() = runTest {
        // Arrange
        val draft = com.loanfinancial.lofi.data.model.entity.ProfileDraftEntity(
            userId = "user_123",
            draftData = "{}",
            updatedAt = System.currentTimeMillis()
        )
        coEvery { profileDraftDao.getDraft("user_123") } returns draft

        // Act
        val result = repository.getProfileDraft("user_123")

        // Assert
        assertNotNull(result)
        assertEquals("user_123", result?.userId)
    }

    @Test
    fun `clearProfileDraft should call dao`() = runTest {
        // Arrange
        coEvery { profileDraftDao.deleteDraft("user_123") } just Runs

        // Act
        repository.clearProfileDraft("user_123")

        // Assert
        coVerify { profileDraftDao.deleteDraft("user_123") }
    }

    @Test
    fun `getUser should return user from dao`() = runTest {
        // Arrange
        val userEntity = com.loanfinancial.lofi.data.model.entity.UserEntity(
            id = "user_123",
            fullName = "Test User",
            username = "testuser",
            email = "test@test.com",
            phoneNumber = "+6281234567890",
            profilePictureUrl = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        every { userDao.getUserById("user_123") } returns flowOf(userEntity)

        // Act & Assert
        repository.getUser("user_123").test {
            val user = awaitItem()
            assertNotNull(user)
            assertEquals("Test User", user?.fullName)
            awaitComplete()
        }
    }
}
