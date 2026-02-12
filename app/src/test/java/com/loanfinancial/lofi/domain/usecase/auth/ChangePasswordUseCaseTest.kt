package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.model.dto.ChangePasswordRequest
import com.loanfinancial.lofi.data.model.dto.ChangePasswordResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ChangePasswordUseCaseTest {
    private lateinit var repository: IAuthRepository
    private lateinit var useCase: ChangePasswordUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ChangePasswordUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository changePassword`() =
        runTest {
            // Arrange
            val request =
                ChangePasswordRequest(
                    currentPassword = "oldPassword",
                    newPassword = "newPassword",
                    newPasswordConfirmation = "newPassword",
                )
            val expectedResponse = ChangePasswordResponse(message = "Password changed successfully")

            coEvery { repository.changePassword(request) } returns Result.success(expectedResponse)

            // Act
            val result = useCase(request)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(expectedResponse, result.getOrNull())
            coVerify(exactly = 1) { repository.changePassword(request) }
        }

    @Test
    fun `invoke should return error when repository fails`() =
        runTest {
            // Arrange
            val request =
                ChangePasswordRequest(
                    currentPassword = "oldPassword",
                    newPassword = "newPassword",
                    newPasswordConfirmation = "newPassword",
                )
            val expectedException = Exception("Password mismatch")

            coEvery { repository.changePassword(request) } returns Result.failure(expectedException)

            // Act
            val result = useCase(request)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedException, result.exceptionOrNull())
            coVerify(exactly = 1) { repository.changePassword(request) }
        }
}
