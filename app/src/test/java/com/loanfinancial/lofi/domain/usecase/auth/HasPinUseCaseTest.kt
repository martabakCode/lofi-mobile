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
class HasPinUseCaseTest {

    private lateinit var repository: IAuthRepository
    private lateinit var useCase: HasPinUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = HasPinUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository hasPin`() = runTest {
        // Arrange
        coEvery { repository.hasPin() } returns Result.success(true)

        // Act
        val result = useCase()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { repository.hasPin() }
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Arrange
        val expectedException = Exception("Network error")
        coEvery { repository.hasPin() } returns Result.failure(expectedException)

        // Act
        val result = useCase()

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.hasPin() }
    }
}
