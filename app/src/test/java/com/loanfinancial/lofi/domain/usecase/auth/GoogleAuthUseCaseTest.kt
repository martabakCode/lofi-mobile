package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.model.dto.AuthTokenData
import com.loanfinancial.lofi.data.model.dto.GoogleAuthRequest
import com.loanfinancial.lofi.data.model.dto.GoogleAuthResponse
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
class GoogleAuthUseCaseTest {

    private lateinit var repository: IAuthRepository
    private lateinit var useCase: GoogleAuthUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GoogleAuthUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository googleAuth`() = runTest {
        // Arrange
        val request = GoogleAuthRequest(idToken = "google-id-token")
        val expectedTokenData = AuthTokenData(
            accessToken = "access",
            refreshToken = "refresh",
            expiresIn = 3600,
            tokenType = "Bearer"
        )
        val expectedResponse = GoogleAuthResponse(
            success = true,
            message = "Success",
            data = expectedTokenData
        )
        
        coEvery { repository.googleAuth(request) } returns Result.success(expectedResponse)

        // Act
        val result = useCase(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        coVerify(exactly = 1) { repository.googleAuth(request) }
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Arrange
        val request = GoogleAuthRequest(idToken = "google-id-token")
        val expectedException = Exception("Auth failed")
        
        coEvery { repository.googleAuth(request) } returns Result.failure(expectedException)

        // Act
        val result = useCase(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.googleAuth(request) }
    }
}
