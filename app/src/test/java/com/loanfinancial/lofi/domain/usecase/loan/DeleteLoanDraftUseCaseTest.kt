package com.loanfinancial.lofi.domain.usecase.loan

import com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DeleteLoanDraftUseCaseTest {
    @MockK
    private lateinit var repository: ILoanDraftRepository

    private lateinit var useCase: DeleteLoanDraftUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = DeleteLoanDraftUseCase(repository)
    }

    @Test
    fun `invoke should delete draft`() =
        runTest {
            // Arrange
            val draftId = "draft_123"
            coEvery { repository.deleteDraft(draftId) } just Runs

            // Act
            useCase(draftId)

            // Assert
            coVerify { repository.deleteDraft(draftId) }
        }
}
