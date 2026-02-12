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
class GetFirebaseIdTokenUseCaseTest {

    private lateinit var repository: IAuthRepository
    private lateinit var useCase: GetFirebaseIdTokenUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetFirebaseIdTokenUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository getFirebaseIdToken`() = runTest {
        // Arrange
        val expectedToken = "firebase_id_token_123"
        coEvery { repository.getFirebaseIdToken() } returns Result.success(expectedToken)

        // Act
        val result = useCase()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedToken, result.getOrNull())
        coVerify(exactly = 1) { repository.getFirebaseIdToken() }
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Arrange
        val expectedException = Exception("Token error")
        coEvery { repository.getFirebaseIdToken() } returns Result.failure(expectedException)

        // Act
        val result = useCase()

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.getFirebaseIdToken() }
    }
}
