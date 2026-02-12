package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.domain.repository.IUserRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ClearProfileDraftUseCaseTest {
    private lateinit var repository: IUserRepository
    private lateinit var useCase: ClearProfileDraftUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ClearProfileDraftUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository clearProfileDraft`() =
        runTest {
            // Arrange
            val userId = "user_123"
            coEvery { repository.clearProfileDraft(userId) } just Runs

            // Act
            useCase(userId)

            // Assert
            coVerify(exactly = 1) { repository.clearProfileDraft(userId) }
        }
}
