package com.loanfinancial.lofi.domain.usecase.auth

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
class ChangeGooglePinUseCaseTest {

    private lateinit var repository: IAuthRepository
    private lateinit var useCase: ChangeGooglePinUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ChangeGooglePinUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository updateGooglePin`() = runTest {
        // Arrange
        val oldPin = "123456"
        val newPin = "654321"
        
        coEvery { repository.updateGooglePin(oldPin, newPin) } returns Result.success(Unit)

        // Act
        val result = useCase(oldPin, newPin)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.updateGooglePin(oldPin, newPin) }
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Arrange
        val oldPin = "123456"
        val newPin = "654321"
        val expectedException = Exception("Network error")
        
        coEvery { repository.updateGooglePin(oldPin, newPin) } returns Result.failure(expectedException)

        // Act
        val result = useCase(oldPin, newPin)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.updateGooglePin(oldPin, newPin) }
    }
}
