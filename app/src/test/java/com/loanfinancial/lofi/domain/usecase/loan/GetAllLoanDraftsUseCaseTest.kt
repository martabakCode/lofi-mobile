package com.loanfinancial.lofi.domain.usecase.loan

import com.loanfinancial.lofi.domain.model.DraftStatus
import com.loanfinancial.lofi.domain.model.DraftStep
import com.loanfinancial.lofi.domain.model.LoanDraft
import com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class GetAllLoanDraftsUseCaseTest {

    @MockK
    private lateinit var repository: ILoanDraftRepository

    private lateinit var useCase: GetAllLoanDraftsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetAllLoanDraftsUseCase(repository)
    }

    @Test
    fun `invoke should return list of drafts`() = runTest {
        // Arrange
        val drafts = listOf(
            createLoanDraft("draft_1"),
            createLoanDraft("draft_2")
        )
        every { repository.getAllActiveDrafts() } returns flowOf(drafts)

        // Act
        val result = useCase().first()

        // Assert
        assertEquals(2, result.size)
        assertEquals("draft_1", result[0].id)
        assertEquals("draft_2", result[1].id)
    }

    @Test
    fun `invoke should return empty list when no drafts`() = runTest {
        // Arrange
        every { repository.getAllActiveDrafts() } returns flowOf(emptyList())

        // Act
        val result = useCase().first()

        // Assert
        assertTrue(result.isEmpty())
    }

    private fun createLoanDraft(id: String) = LoanDraft(
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
        updatedAt = System.currentTimeMillis()
    )
}
