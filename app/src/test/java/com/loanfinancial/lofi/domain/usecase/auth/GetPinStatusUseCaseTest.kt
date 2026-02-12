package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.remote.api.PinStatusResponse
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
class GetPinStatusUseCaseTest {
    private lateinit var repository: IAuthRepository
    private lateinit var useCase: GetPinStatusUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetPinStatusUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository getPinStatus`() =
        runTest {
            // Arrange
            val expectedResponse = PinStatusResponse(pinSet = true)
            coEvery { repository.getPinStatus() } returns Result.success(expectedResponse)

            // Act
            val result = useCase()

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(expectedResponse, result.getOrNull())
            coVerify(exactly = 1) { repository.getPinStatus() }
        }

    @Test
    fun `invoke should return error when repository fails`() =
        runTest {
            // Arrange
            val expectedException = Exception("Network error")
            coEvery { repository.getPinStatus() } returns Result.failure(expectedException)

            // Act
            val result = useCase()

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedException, result.exceptionOrNull())
            coVerify(exactly = 1) { repository.getPinStatus() }
        }
}
