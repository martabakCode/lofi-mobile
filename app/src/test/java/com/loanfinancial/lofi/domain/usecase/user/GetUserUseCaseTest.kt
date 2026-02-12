package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.data.model.dto.UserData
import com.loanfinancial.lofi.data.model.dto.UserResponse
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
class GetUserUseCaseTest {
    private lateinit var repository: IAuthRepository
    private lateinit var useCase: GetUserUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetUserUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository getUserInfo`() =
        runTest {
            // Arrange
            val userData =
                UserData(
                    id = "user_123",
                    email = "john@example.com",
                    username = "john",
                    roles = listOf("USER"),
                    permissions = emptyList(),
                    branchId = null,
                    branchName = null,
                )
            val expectedResponse = UserResponse(success = true, message = "Success", data = userData)

            coEvery { repository.getUserInfo() } returns Result.success(expectedResponse)

            // Act
            val result = useCase()

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(expectedResponse, result.getOrNull())
            coVerify(exactly = 1) { repository.getUserInfo() }
        }

    @Test
    fun `invoke should return error when repository fails`() =
        runTest {
            // Arrange
            val expectedException = Exception("Network error")

            coEvery { repository.getUserInfo() } returns Result.failure(expectedException)

            // Act
            val result = useCase()

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedException, result.exceptionOrNull())
            coVerify(exactly = 1) { repository.getUserInfo() }
        }
}
