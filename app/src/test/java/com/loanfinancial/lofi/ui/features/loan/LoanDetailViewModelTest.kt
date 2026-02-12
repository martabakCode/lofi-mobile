package com.loanfinancial.lofi.ui.features.loan

import app.cash.turbine.test
import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.Product
import com.loanfinancial.lofi.domain.usecase.loan.GetLoanDetailUseCase
import com.loanfinancial.lofi.domain.usecase.loan.SubmitLoanUseCase
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LoanDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var getLoanDetailUseCase: GetLoanDetailUseCase

    @MockK
    private lateinit var submitLoanUseCase: SubmitLoanUseCase

    private lateinit var viewModel: LoanDetailViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = LoanDetailViewModel(getLoanDetailUseCase, submitLoanUseCase)
    }

    @Test
    fun `initial state should be loading`() =
        runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state is LoanDetailUiState.Loading)
            }
        }

    @Test
    fun `loadLoan should emit success when loan loaded`() =
        runTest {
            // Arrange
            val loan = createLoan("loan_123", "DRAFT")
            coEvery { getLoanDetailUseCase("loan_123") } returns
                flowOf(
                    Resource.Loading,
                    Resource.Success(loan),
                )

            // Act
            viewModel.loadLoan("loan_123")

            // Assert
            viewModel.uiState.test {
                assertTrue(awaitItem() is LoanDetailUiState.Loading)
                val successState = awaitItem() as LoanDetailUiState.Success
                assertEquals("loan_123", successState.loan.id)
                assertEquals("DRAFT", successState.loan.loanStatus)
            }
        }

    @Test
    fun `loadLoan should emit error when loan fails to load`() =
        runTest {
            // Arrange
            coEvery { getLoanDetailUseCase("loan_123") } returns
                flowOf(
                    Resource.Loading,
                    Resource.Error("Network error"),
                )

            // Act
            viewModel.loadLoan("loan_123")

            // Assert
            viewModel.uiState.test {
                assertTrue(awaitItem() is LoanDetailUiState.Loading)
                val errorState = awaitItem() as LoanDetailUiState.Error
                assertEquals("Network error", errorState.message)
            }
        }

    @Test
    fun `loadLoan should emit error when loan data is null`() =
        runTest {
            // Arrange
            @Suppress("UNCHECKED_CAST")
            coEvery { getLoanDetailUseCase("loan_123") } returns
                flowOf(
                    Resource.Loading as com.loanfinancial.lofi.core.util.Resource<Loan>,
                    Resource.Success(null) as com.loanfinancial.lofi.core.util.Resource<Loan>,
                )

            // Act
            viewModel.loadLoan("loan_123")

            // Assert
            viewModel.uiState.test {
                assertTrue(awaitItem() is LoanDetailUiState.Loading)
                val errorState = awaitItem() as LoanDetailUiState.Error
                assertEquals("Loan data is null", errorState.message)
            }
        }

    @Test
    fun `submitLoan should update submit state to loading then success`() =
        runTest {
            // Arrange
            val loan = createLoan("loan_123", "SUBMITTED")
            coEvery { submitLoanUseCase("loan_123") } returns
                flowOf(
                    Resource.Loading,
                    Resource.Success(loan),
                )

            // Act
            viewModel.submitLoan("loan_123")

            // Assert
            viewModel.submitState.test {
                assertTrue(awaitItem() is SubmitLoanState.Loading)
                val successState = awaitItem() as SubmitLoanState.Success
                assertEquals("SUBMITTED", successState.loan.loanStatus)
            }
        }

    @Test
    fun `submitLoan should update submit state to error on failure`() =
        runTest {
            // Arrange
            coEvery { submitLoanUseCase("loan_123") } returns
                flowOf(
                    Resource.Loading,
                    Resource.Error("Submission failed"),
                )

            // Act
            viewModel.submitLoan("loan_123")

            // Assert
            viewModel.submitState.test {
                assertTrue(awaitItem() is SubmitLoanState.Loading)
                val errorState = awaitItem() as SubmitLoanState.Error
                assertEquals("Submission failed", errorState.message)
            }
        }

    @Test
    fun `submitLoan should update uiState on success`() =
        runTest {
            // Arrange
            val submittedLoan = createLoan("loan_123", "SUBMITTED")
            coEvery { submitLoanUseCase("loan_123") } returns
                flowOf(
                    Resource.Loading,
                    Resource.Success(submittedLoan),
                )

            // Act
            viewModel.submitLoan("loan_123")

            // Assert
            viewModel.uiState.test {
                awaitItem() // Loading
                val successState = awaitItem() as LoanDetailUiState.Success
                assertEquals("SUBMITTED", successState.loan.loanStatus)
            }
        }

    @Test
    fun `resetSubmitState should reset to idle`() =
        runTest {
            // Arrange
            val loan = createLoan("loan_123", "SUBMITTED")
            coEvery { submitLoanUseCase("loan_123") } returns
                flowOf(
                    Resource.Success(loan),
                )
            viewModel.submitLoan("loan_123")

            // Act
            viewModel.resetSubmitState()

            // Assert
            viewModel.submitState.test {
                assertTrue(awaitItem() is SubmitLoanState.Idle)
            }
        }

    @Test
    fun `loadLoan should handle exception gracefully`() =
        runTest {
            // Arrange
            coEvery { getLoanDetailUseCase("loan_123") } throws RuntimeException("Unexpected error")

            // Act
            viewModel.loadLoan("loan_123")

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state is LoanDetailUiState.Error)
            }
        }

    private fun createLoan(
        id: String,
        status: String,
    ) =
        Loan(
            id = id,
            customerName = "Test Customer",
            product =
                Product(
                    productCode = "CASH_LOAN",
                    productName = "Pinjaman Tunai",
                    interestRate = 0.05,
                ),
            loanAmount = 5000000,
            tenor = 12,
            loanStatus = status,
            currentStage = "SUBMISSION",
            submittedAt = "2024-01-15T10:30:00Z",
            reviewedAt = if (status != "DRAFT" && status != "SUBMITTED") "2024-01-16T10:30:00Z" else null,
            approvedAt = if (status == "APPROVED" || status == "DISBURSED") "2024-01-17T10:30:00Z" else null,
            rejectedAt = null,
            disbursedAt = if (status == "DISBURSED") "2024-01-18T10:30:00Z" else null,
            loanStatusDisplay = status,
            slaDurationHours = 24,
        )
}
