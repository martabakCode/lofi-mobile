package com.loanfinancial.lofi.domain.usecase.loan

import app.cash.turbine.test
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.Product
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SubmitLoanUseCaseTest {

    @MockK
    private lateinit var repository: ILoanRepository

    private lateinit var useCase: SubmitLoanUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = SubmitLoanUseCase(repository)
    }

    @Test
    fun `invoke should call repository submitLoan`() = runTest {
        // Arrange
        val loanId = "loan_123"
        val loan = Loan(
            id = loanId,
            customerName = "Test Customer",
            product = Product("CASH_LOAN", "Pinjaman Tunai", 0.05),
            loanAmount = 5000000,
            tenor = 12,
            loanStatus = "SUBMITTED",
            currentStage = "SUBMISSION",
            submittedAt = "2024-01-15T10:30:00Z",
            reviewedAt = null,
            approvedAt = null,
            rejectedAt = null,
            disbursedAt = null,
            loanStatusDisplay = "Submitted",
            slaDurationHours = 24
        )
        
        every { repository.submitLoan(loanId) } returns flowOf(
            Resource.Loading,
            Resource.Success(loan)
        )

        // Act & Assert
        useCase(loanId).test {
            assertEquals(Resource.Loading, awaitItem())
            val success = awaitItem() as Resource.Success
            assertEquals("SUBMITTED", success.data?.loanStatus)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should propagate error`() = runTest {
        // Arrange
        val loanId = "loan_123"
        
        every { repository.submitLoan(loanId) } returns flowOf(
            Resource.Loading,
            Resource.Error("Submission failed")
        )

        // Act & Assert
        useCase(loanId).test {
            assertEquals(Resource.Loading, awaitItem())
            val error = awaitItem() as Resource.Error
            assertEquals("Submission failed", error.message)
            awaitComplete()
        }
    }
}
