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
class SetPinUseCaseTest {

    private lateinit var repository: IAuthRepository
    private lateinit var useCase: SetPinUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SetPinUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository setPin`() = runTest {
        // Arrange
        val pin = "123456"
        coEvery { repository.setPin(pin) } returns Result.success(Unit)

        // Act
        val result = useCase(pin)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.setPin(pin) }
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Arrange
        val pin = "123456"
        val expectedException = Exception("Network error")
        coEvery { repository.setPin(pin) } returns Result.failure(expectedException)

        // Act
        val result = useCase(pin)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.setPin(pin) }
    }
}
