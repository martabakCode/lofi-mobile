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
class GetLoanDetailUseCaseTest {
    @MockK
    private lateinit var repository: ILoanRepository

    private lateinit var useCase: GetLoanDetailUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetLoanDetailUseCase(repository)
    }

    @Test
    fun `invoke should return loan detail`() =
        runTest {
            // Arrange
            val loanId = "loan_123"
            val loan =
                Loan(
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
                    loanStatusDisplay = "Menunggu Review",
                    slaDurationHours = 24,
                )

            every { repository.getLoanDetail(loanId) } returns
                flowOf(
                    Resource.Loading,
                    Resource.Success(loan),
                )

            // Act & Assert
            useCase(loanId).test {
                assertEquals(Resource.Loading, awaitItem())
                val success = awaitItem() as Resource.Success
                assertEquals(loanId, success.data?.id)
                assertEquals("Test Customer", success.data?.customerName)
                awaitComplete()
            }
        }

    @Test
    fun `invoke should return error when repository fails`() =
        runTest {
            // Arrange
            val loanId = "loan_123"
            every { repository.getLoanDetail(loanId) } returns
                flowOf(
                    Resource.Loading,
                    Resource.Error("Network error"),
                )

            // Act & Assert
            useCase(loanId).test {
                assertEquals(Resource.Loading, awaitItem())
                val error = awaitItem() as Resource.Error
                assertEquals("Network error", error.message)
                awaitComplete()
            }
        }
}
