package com.loanfinancial.lofi.domain.usecase.loan

import com.loanfinancial.lofi.domain.model.DraftStatus
import com.loanfinancial.lofi.domain.model.DraftStep
import com.loanfinancial.lofi.domain.model.LoanDraft
import com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class GetLoanDraftByIdUseCaseTest {
    @MockK
    private lateinit var repository: ILoanDraftRepository

    private lateinit var useCase: GetLoanDraftByIdUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GetLoanDraftByIdUseCase(repository)
    }

    @Test
    fun `invoke should return draft when found`() =
        runTest {
            // Arrange
            val draftId = "draft_123"
            val draft = createLoanDraft(draftId)
            coEvery { repository.getDraftById(any()) } returns draft

            // Act
            val result = useCase(draftId)

            // Assert
            assertNotNull(result)
            assertEquals(draftId, result?.id)
            assertEquals(5000000, result?.amount)
        }

    @Test
    fun `invoke should return null when not found`() =
        runTest {
            // Arrange
            val draftId = "draft_123"
            coEvery { repository.getDraftById(draftId) } returns null

            // Act
            val result = useCase(draftId)

            // Assert
            assertNull(result)
        }

    private fun createLoanDraft(id: String) =
        LoanDraft(
            id = id,
            amount = 5000000,
            tenor = 12,
            purpose = "Business",
            downPayment = null,
            latitude = null,
            longitude = null,
            isBiometricVerified = false,
            documentPaths = null,
            interestRate = null,
            adminFee = null,
            isAgreementChecked = false,
            currentStep = DraftStep.BASIC_INFO,
            status = DraftStatus.DRAFT,
            isSynced = false,
            serverLoanId = null,
            documentUploadStatus = null,
            uploadQueueIds = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
}
