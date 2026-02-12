package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.remote.api.AuthSourceResponse
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
class GetAuthSourceUseCaseTest {

    private lateinit var repository: IAuthRepository
    private lateinit var useCase: GetAuthSourceUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetAuthSourceUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository getAuthSource`() = runTest {
        // Arrange
        val expectedResponse = AuthSourceResponse(authSource = "google", googleUser = true)
        
        coEvery { repository.getAuthSource() } returns Result.success(expectedResponse)

        // Act
        val result = useCase()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        coVerify(exactly = 1) { repository.getAuthSource() }
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Arrange
        val expectedException = Exception("Network error")
        
        coEvery { repository.getAuthSource() } returns Result.failure(expectedException)

        // Act
        val result = useCase()

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.getAuthSource() }
    }
}
