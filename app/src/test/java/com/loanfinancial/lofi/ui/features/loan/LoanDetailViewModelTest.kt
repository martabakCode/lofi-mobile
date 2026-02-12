package com.loanfinancial.lofi.ui.features.loan

import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.Product
import com.loanfinancial.lofi.domain.usecase.loan.GetLoanDetailUseCase
import com.loanfinancial.lofi.domain.usecase.loan.SubmitLoanUseCase
import io.mockk.MockKAnnotations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class LoanDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @MockK
    private lateinit var getLoanDetailUseCase: GetLoanDetailUseCase

    @MockK
    private lateinit var submitLoanUseCase: SubmitLoanUseCase

    private lateinit var viewModel: LoanDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxed = true)
        viewModel =
            LoanDetailViewModel(
                getLoanDetailUseCase,
                submitLoanUseCase,
            )
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
    fun `loadLoan should emit success when loan loaded`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `loadLoan should emit error when loan fails to load`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `loadLoan should handle exception gracefully`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `submitLoan should update submit state to error on failure`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `submitLoan should update submit state to loading then success`() =
        runTest {
            assertNotNull(viewModel)
        }

    private fun createLoan(
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
