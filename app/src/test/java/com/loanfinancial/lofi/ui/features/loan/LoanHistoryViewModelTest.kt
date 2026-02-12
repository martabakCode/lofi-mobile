package com.loanfinancial.lofi.ui.features.loan

import app.cash.turbine.test
import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.manager.LoanSubmissionManager
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.PendingLoanSubmission
import com.loanfinancial.lofi.domain.model.PendingSubmissionStatus
import com.loanfinancial.lofi.domain.model.Product
import com.loanfinancial.lofi.domain.usecase.GetMyLoansUseCase
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
class LoanHistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var getMyLoansUseCase: GetMyLoansUseCase

    @MockK
    private lateinit var loanSubmissionManager: LoanSubmissionManager

    private lateinit var viewModel: LoanHistoryViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        // Default mock behavior
        every { getMyLoansUseCase(any(), any(), any()) } returns
            flowOf(Resource.Success(emptyList()))
        every { loanSubmissionManager.getPendingSubmissions() } returns
            flowOf(emptyList())

        viewModel = LoanHistoryViewModel(getMyLoansUseCase, loanSubmissionManager)
    }

    @Test
    fun `initial state should be loading`() =
        runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state.isLoading)
                assertTrue(state.loans.isEmpty())
            }
        }

    @Test
    fun `loadHistory should update state with loans`() =
        runTest {
            // Arrange
            val loans =
                listOf(
                    createLoan("loan_1", "SUBMITTED"),
                    createLoan("loan_2", "APPROVED"),
                )

            every { getMyLoansUseCase(0, 10, any()) } returns
                flowOf(Resource.Success(loans))
            every { loanSubmissionManager.getPendingSubmissions() } returns
                flowOf(emptyList())

            // Act
            viewModel.loadHistory()

            // Assert
            viewModel.uiState.test {
                skipItems(1) // Skip loading state
                val state = awaitItem()
                assertFalse(state.isLoading)
                assertEquals(2, state.loans.size)
                assertNull(state.error)
            }
        }

    @Test
    fun `loadHistory should include pending submissions`() =
        runTest {
            // Arrange
            val remoteLoans = listOf(createLoan("loan_1", "SUBMITTED"))
            val pendingSubmissions =
                listOf(
                    createPendingSubmission("pending_1", PendingSubmissionStatus.PENDING),
                    createPendingSubmission("pending_2", PendingSubmissionStatus.SUBMITTING),
                )

            every { getMyLoansUseCase(0, 10, any()) } returns
                flowOf(Resource.Success(remoteLoans))
            every { loanSubmissionManager.getPendingSubmissions() } returns
                flowOf(pendingSubmissions)

            // Act
            viewModel.loadHistory()

            // Assert
            viewModel.uiState.test {
                skipItems(1)
                val state = awaitItem()
                assertEquals(3, state.loans.size) // 1 remote + 2 pending
            }
        }

    @Test
    fun `loadHistory should handle error state`() =
        runTest {
            // Arrange
            every { getMyLoansUseCase(0, 10, any()) } returns
                flowOf(Resource.Error("Network error"))
            every { loanSubmissionManager.getPendingSubmissions() } returns
                flowOf(emptyList())

            // Act
            viewModel.loadHistory()

            // Assert
            viewModel.uiState.test {
                skipItems(1)
                val state = awaitItem()
                assertFalse(state.isLoading)
                assertEquals("Network error", state.error)
            }
        }

    @Test
    fun `loadMore should increment page and append loans`() =
        runTest {
            // Arrange
            val initialLoans = listOf(createLoan("loan_1", "SUBMITTED"))
            val moreLoans = listOf(createLoan("loan_2", "APPROVED"))

            every { getMyLoansUseCase(0, 10, any()) } returns
                flowOf(Resource.Success(initialLoans))
            every { getMyLoansUseCase(1, 10, any()) } returns
                flowOf(Resource.Success(moreLoans))
            every { loanSubmissionManager.getPendingSubmissions() } returns
                flowOf(emptyList())

            // Act
            viewModel.loadHistory() // Load first page
            viewModel.loadMore() // Load second page

            // Assert
            viewModel.uiState.test {
                skipItems(2) // Skip loading and first result
                val state = awaitItem()
                assertEquals(2, state.loans.size)
                assertEquals(1, state.page)
            }
        }

    @Test
    fun `refresh should reset page and reload`() =
        runTest {
            // Arrange
            val loans = listOf(createLoan("loan_1", "SUBMITTED"))

            every { getMyLoansUseCase(0, 10, any()) } returns
                flowOf(Resource.Success(loans))
            every { loanSubmissionManager.getPendingSubmissions() } returns
                flowOf(emptyList())

            // Act
            viewModel.refresh()

            // Assert
            viewModel.uiState.test {
                val loadingState = awaitItem()
                assertTrue(loadingState.isRefreshing)

                val successState = awaitItem()
                assertFalse(successState.isRefreshing)
                assertEquals(0, successState.page)
            }
        }

    @Test
    fun `should set hasMore to false when no more items`() =
        runTest {
            // Arrange
            val loans = listOf(createLoan("loan_1", "SUBMITTED"))

            every { getMyLoansUseCase(0, 10, any()) } returns
                flowOf(Resource.Success(loans))
            every { loanSubmissionManager.getPendingSubmissions() } returns
                flowOf(emptyList())

            // Act
            viewModel.loadHistory()

            // Assert
            viewModel.uiState.test {
                skipItems(1)
                val state = awaitItem()
                assertFalse(state.hasMore) // Only 1 item returned, less than page size
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
            reviewedAt = null,
            approvedAt = if (status == "APPROVED") "2024-01-16T10:30:00Z" else null,
            rejectedAt = null,
            disbursedAt = null,
            loanStatusDisplay = status,
            slaDurationHours = 24,
        )

    private fun createPendingSubmission(
        id: String,
        status: PendingSubmissionStatus,
    ) =
        PendingLoanSubmission(
            loanId = id,
            loanAmount = 5000000,
            tenor = 12,
            productName = "Pinjaman Tunai",
            status = status,
            retryCount = 0,
            lastRetryTime = null,
            failureReason = null,
            createdAt = java.util.Date(),
        )
}
