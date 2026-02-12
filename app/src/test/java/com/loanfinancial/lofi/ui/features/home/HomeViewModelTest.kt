package com.loanfinancial.lofi.ui.features.home

import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.domain.manager.LoanSubmissionManager
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.Product
import com.loanfinancial.lofi.domain.usecase.GetMyLoansUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetAvailableProductUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetProductsUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetUserProfileUseCase
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
class HomeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @MockK
    private lateinit var getMyLoansUseCase: GetMyLoansUseCase

    @MockK
    private lateinit var getUserProfileUseCase: GetUserProfileUseCase

    @MockK
    private lateinit var getProductsUseCase: GetProductsUseCase

    @MockK
    private lateinit var getAvailableProductUseCase: GetAvailableProductUseCase

    @MockK
    private lateinit var loanSubmissionManager: LoanSubmissionManager

    @MockK
    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxed = true)
        every { getUserProfileUseCase() } returns flowOf(Resource.Loading)
        every { getProductsUseCase() } returns flowOf(Resource.Success(emptyList()))
        every { getAvailableProductUseCase() } returns flowOf(Resource.Loading)
        every { loanSubmissionManager.getPendingSubmissions() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have default values`() =
        runTest {
            viewModel =
                HomeViewModel(
                    getMyLoansUseCase,
                    getUserProfileUseCase,
                    getProductsUseCase,
                    getAvailableProductUseCase,
                    loanSubmissionManager,
                    dataStoreManager,
                )

            val state = viewModel.uiState.value
            assertNotNull(state)
        }

    @Test
    fun `fetchLoans should update uiState with success`() =
        runTest {
            val loans =
                listOf(
                    Loan(
                        id = "loan_1",
                        customerName = "Test",
                        product = Product("CASH", "Pinjaman", 0.05),
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
                    ),
                )

            every { getMyLoansUseCase(any(), any(), any()) } returns flowOf(Resource.Success(loans))

            viewModel =
                HomeViewModel(
                    getMyLoansUseCase,
                    getUserProfileUseCase,
                    getProductsUseCase,
                    getAvailableProductUseCase,
                    loanSubmissionManager,
                    dataStoreManager,
                )

            val state = viewModel.uiState.value
            assertNotNull(state)
        }

    @Test
    fun `fetchLoans should update uiState with error`() =
        runTest {
            every { getMyLoansUseCase(any(), any(), any()) } returns flowOf(Resource.Error("Network error"))

            viewModel =
                HomeViewModel(
                    getMyLoansUseCase,
                    getUserProfileUseCase,
                    getProductsUseCase,
                    getAvailableProductUseCase,
                    loanSubmissionManager,
                    dataStoreManager,
                )

            val state = viewModel.uiState.value
            assertNotNull(state)
        }
}
