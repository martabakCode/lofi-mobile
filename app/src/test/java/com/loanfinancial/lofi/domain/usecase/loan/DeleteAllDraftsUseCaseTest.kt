package com.loanfinancial.lofi.domain.usecase.loan

import com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
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
class DeleteAllDraftsUseCaseTest {
    private lateinit var repository: ILoanDraftRepository
    private lateinit var useCase: DeleteAllDraftsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DeleteAllDraftsUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository deleteAllDrafts`() =
        runTest {
            // Arrange
            coEvery { repository.deleteAllDrafts() } just Runs

            // Act
            useCase()

            // Assert
            coVerify(exactly = 1) { repository.deleteAllDrafts() }
        }
}
