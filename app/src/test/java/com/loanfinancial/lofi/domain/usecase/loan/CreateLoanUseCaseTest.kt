package com.loanfinancial.lofi.domain.usecase.loan

import app.cash.turbine.test
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.CreateLoanRequest
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
class CreateLoanUseCaseTest {

    @MockK
    private lateinit var repository: ILoanRepository

    private lateinit var useCase: CreateLoanUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = CreateLoanUseCase(repository)
    }

    @Test
    fun `invoke should call repository createLoan`() = runTest {
        // Arrange
        val request = CreateLoanRequest(
            loanAmount = 5000000,
            tenor = 12,
            purpose = "Business",
            longitude = 106.8456,
            latitude = -6.2088
        )
        
        val loan = Loan(
            id = "loan_123",
            customerName = "Test Customer",
            product = Product("CASH_LOAN", "Pinjaman Tunai", 0.05),
            loanAmount = 5000000,
            tenor = 12,
            loanStatus = "DRAFT",
            currentStage = "SUBMISSION",
            submittedAt = null,
            reviewedAt = null,
            approvedAt = null,
            rejectedAt = null,
            disbursedAt = null,
            loanStatusDisplay = "Draft",
            slaDurationHours = 24
        )
        
        every { repository.createLoan(request) } returns flowOf(
            Resource.Loading,
            Resource.Success(loan)
        )

        // Act & Assert
        useCase(request).test {
            assertEquals(Resource.Loading, awaitItem())
            val success = awaitItem() as Resource.Success
            assertEquals("loan_123", success.data?.id)
            awaitComplete()
        }
    }

    @Test
    fun `invoke should propagate error from repository`() = runTest {
        // Arrange
        val request = CreateLoanRequest(
            loanAmount = 5000000,
            tenor = 12,
            purpose = "Business",
            longitude = 106.8456,
            latitude = -6.2088
        )
        
        every { repository.createLoan(request) } returns flowOf(
            Resource.Loading,
            Resource.Error("Network error")
        )

        // Act & Assert
        useCase(request).test {
            assertEquals(Resource.Loading, awaitItem())
            val error = awaitItem() as Resource.Error
            assertEquals("Network error", error.message)
            awaitComplete()
        }
    }
}
