package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.domain.repository.IUserRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ValidateLoanSubmissionUseCaseTest {
    private lateinit var repository: IUserRepository
    private lateinit var useCase: ValidateLoanSubmissionUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ValidateLoanSubmissionUseCase(repository)
    }

    private fun createUserData(
        isProfileCompleted: Boolean = true,
        isPinSet: Boolean = true,
    ): UserUpdateData =
        UserUpdateData(
            id = "user123",
            fullName = "John Doe",
            email = "john@example.com",
            phoneNumber = "08123456789",
            profilePictureUrl = null,
            branch = null,
            biodata = null,
            product = null,
            pinSet = isPinSet,
            profileCompleted = isProfileCompleted,
        )

    @Test
    fun `invoke should return success when profile is valid`() =
        runTest {
            // Arrange
            val userData = createUserData(isProfileCompleted = true, isPinSet = true)

            every { repository.getUserProfile() } returns
                flowOf(
                    Resource.Loading,
                    Resource.Success(userData),
                )

            // Act
            val result = useCase()

            // Assert
            assertTrue(result.isSuccess)
            val eligibility = result.getOrNull()
            assertEquals(true, eligibility?.isProfileComplete)
            assertEquals(true, eligibility?.isPinSet)
        }

    @Test
    fun `invoke should return correct flags when profile is incomplete`() =
        runTest {
            // Arrange
            val userData = createUserData(isProfileCompleted = false, isPinSet = false)

            every { repository.getUserProfile() } returns
                flowOf(
                    Resource.Success(userData),
                )

            // Act
            val result = useCase()

            // Assert
            assertTrue(result.isSuccess)
            val eligibility = result.getOrNull()
            assertEquals(false, eligibility?.isProfileComplete)
            assertEquals(false, eligibility?.isPinSet)
        }

    @Test
    fun `invoke should return failure when repository returns error`() =
        runTest {
            // Arrange
            every { repository.getUserProfile() } returns
                flowOf(
                    Resource.Loading,
                    Resource.Error(message = "Network error"),
                )

            // Act
            val result = useCase()

            // Assert
            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }
}
