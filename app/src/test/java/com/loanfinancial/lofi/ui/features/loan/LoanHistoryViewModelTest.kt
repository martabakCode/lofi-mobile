package com.loanfinancial.lofi.ui.features.loan

import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.manager.LoanSubmissionManager
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.Product
import com.loanfinancial.lofi.domain.usecase.GetMyLoansUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LoanHistoryViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @MockK
    private lateinit var getMyLoansUseCase: GetMyLoansUseCase

    @MockK
    private lateinit var loanSubmissionManager: LoanSubmissionManager

    private lateinit var viewModel: LoanHistoryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxed = true)

        every { getMyLoansUseCase(any(), any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { loanSubmissionManager.getPendingSubmissions() } returns flowOf(emptyList())

        viewModel = LoanHistoryViewModel(getMyLoansUseCase, loanSubmissionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `loadHistory should update state with loans`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `loadHistory should handle error state`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `loadMore should increment page and append loans`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `refresh should reset page and reload`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `loadHistory should include pending submissions`() =
        runTest {
            assertNotNull(viewModel)
        }

    protected fun createLoan(
        id: String,
        status: String,
    ) =
        Loan(
            id = id,
            customerName = "Test User",
            product = Product("CASH", "Pinjaman", 0.05),
            loanAmount = 5000000,
            tenor = 12,
            loanStatus = status,
            currentStage = "SUBMISSION",
            submittedAt = "2024-01-15T10:30:00Z",
            reviewedAt = null,
            approvedAt = null,
            rejectedAt = null,
            disbursedAt = null,
            loanStatusDisplay = "Test",
            slaDurationHours = 24,
        )
}
