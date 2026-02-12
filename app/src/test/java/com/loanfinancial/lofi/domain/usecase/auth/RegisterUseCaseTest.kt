package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.model.dto.RegisterRequest
import com.loanfinancial.lofi.data.model.dto.RegisterResponse
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
class RegisterUseCaseTest {
    private lateinit var repository: IAuthRepository
    private lateinit var useCase: RegisterUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = RegisterUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository register`() =
        runTest {
            // Arrange
            val request =
                RegisterRequest(
                    fullName = "John Doe",
                    username = "johndoe",
                    email = "test@example.com",
                    password = "password123",
                    phoneNumber = "08123456789",
                )
            val expectedResponse =
                RegisterResponse(
                    success = true,
                    message = "Success",
                    data = com.loanfinancial.lofi.data.model.dto.AuthTokenData(
                        accessToken = "token",
                        refreshToken = "refresh",
                        tokenType = "Bearer",
                        expiresIn = 3600
                    )
                )

            coEvery { repository.register(request) } returns Result.success(expectedResponse)

            // Act
            val result = useCase(request)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(expectedResponse, result.getOrNull())
            coVerify(exactly = 1) { repository.register(request) }
        }

    @Test
    fun `invoke should return error when repository fails`() =
        runTest {
            // Arrange
            val request =
                RegisterRequest(
                    fullName = "John Doe",
                    username = "johndoe",
                    email = "test@example.com",
                    password = "password123",
                    phoneNumber = "08123456789",
                )
            val expectedException = Exception("Email already taken")

            coEvery { repository.register(request) } returns Result.failure(expectedException)

            // Act
            val result = useCase(request)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedException, result.exceptionOrNull())
            coVerify(exactly = 1) { repository.register(request) }
        }
}
