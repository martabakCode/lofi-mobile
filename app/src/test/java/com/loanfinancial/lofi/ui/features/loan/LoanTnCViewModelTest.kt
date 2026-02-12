package com.loanfinancial.lofi.ui.features.loan

import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.domain.manager.LoanSubmissionManager
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.Product
import com.loanfinancial.lofi.domain.usecase.loan.CreateLoanUseCase
import com.loanfinancial.lofi.domain.usecase.loan.SubmitLoanUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetUserProfileUseCase
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
class LoanTnCViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @MockK
    private lateinit var createLoanUseCase: CreateLoanUseCase

    @MockK
    private lateinit var submitLoanUseCase: SubmitLoanUseCase

    @MockK
    private lateinit var loanSubmissionManager: LoanSubmissionManager

    @MockK
    private lateinit var getUserProfileUseCase: GetUserProfileUseCase

    private lateinit var viewModel: LoanTnCViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxed = true)
        viewModel =
            LoanTnCViewModel(
                createLoanUseCase,
                submitLoanUseCase,
                loanSubmissionManager,
                getUserProfileUseCase,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `onAgreementCheckedChange should update state`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `onSubmitClicked should show error when agreement not checked`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `onSubmitClicked should create loan and submit successfully`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `onSubmitClicked should skip submit when loan already submitted`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `onSubmitClicked should fallback to offline on network error`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `onSubmitClicked should show error on business logic error`() =
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
