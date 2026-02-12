package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.model.dto.LoginRequest
import com.loanfinancial.lofi.data.model.dto.LoginResponse
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
class LoginUseCaseTest {
    private lateinit var repository: IAuthRepository
    private lateinit var useCase: LoginUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = LoginUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository login`() =
        runTest {
            // Arrange
            val request =
                LoginRequest(
                    email = "test@example.com",
                    password = "password123",
                    fcmToken = "token",
                )
            val expectedResponse =
                LoginResponse(
                    success = true,
                    message = "Success",
                    data =
                        com.loanfinancial.lofi.data.model.dto.AuthTokenData(
                            accessToken = "token",
                            refreshToken = "refresh",
                            tokenType = "Bearer",
                            expiresIn = 3600,
                        ),
                )

            coEvery { repository.login(request) } returns Result.success(expectedResponse)

            // Act
            val result = useCase(request)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(expectedResponse, result.getOrNull())
            coVerify(exactly = 1) { repository.login(request) }
        }

    @Test
    fun `invoke should return error when repository fails`() =
        runTest {
            // Arrange
            val request =
                LoginRequest(
                    email = "test@example.com",
                    password = "password123",
                    fcmToken = "token",
                )
            val expectedException = Exception("Network error")

            coEvery { repository.login(request) } returns Result.failure(expectedException)

            // Act
            val result = useCase(request)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedException, result.exceptionOrNull())
            coVerify(exactly = 1) { repository.login(request) }
        }
}
