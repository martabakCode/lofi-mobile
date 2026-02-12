package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.remote.api.PinVerificationResponse
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
class VerifyPinUseCaseTest {

    private lateinit var repository: IAuthRepository
    private lateinit var useCase: VerifyPinUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = VerifyPinUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository verifyPin`() = runTest {
        // Arrange
        val pin = "123456"
        val purpose = "LOGIN"
        val expectedResponse = PinVerificationResponse(
            isValid = true,
            remainingAttempts = 3,
            isLocked = false,
            lockedUntil = null
        )
        
        coEvery { repository.verifyPin(pin, purpose) } returns Result.success(expectedResponse)

        // Act
        val result = useCase(pin, purpose)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        coVerify(exactly = 1) { repository.verifyPin(pin, purpose) }
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Arrange
        val pin = "123456"
        val purpose = "LOGIN"
        val expectedException = Exception("Verification failed")
        
        coEvery { repository.verifyPin(pin, purpose) } returns Result.failure(expectedException)

        // Act
        val result = useCase(pin, purpose)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.verifyPin(pin, purpose) }
    }
}
