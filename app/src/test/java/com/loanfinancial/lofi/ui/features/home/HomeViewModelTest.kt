package com.loanfinancial.lofi.ui.features.home

import app.cash.turbine.test
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
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
        MockKAnnotations.init(this)

        // Default mocks
        every { getMyLoansUseCase(any(), any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { getUserProfileUseCase() } returns flowOf(Resource.Loading)
        every { getProductsUseCase() } returns flowOf(Resource.Success(emptyList<com.loanfinancial.lofi.data.model.dto.ProductDto>()))
        every { getAvailableProductUseCase() } returns flowOf(Resource.Loading)
        every { loanSubmissionManager.getPendingSubmissions() } returns flowOf(emptyList())
    }

    @Test
    fun `initial state should have default values`() =
        runTest {
            // Act
            viewModel =
                HomeViewModel(
                    getMyLoansUseCase,
                    getUserProfileUseCase,
                    getProductsUseCase,
                    getAvailableProductUseCase,
                    loanSubmissionManager,
                    dataStoreManager,
                )

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertFalse(state.isLoading)
                assertTrue(state.loans.isEmpty())
                assertNull(state.error)
            }
        }

    @Test
    fun `fetchLoans should update uiState with success`() =
        runTest {
            // Arrange
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

            // Act
            viewModel =
                HomeViewModel(
                    getMyLoansUseCase,
                    getUserProfileUseCase,
                    getProductsUseCase,
                    getAvailableProductUseCase,
                    loanSubmissionManager,
                    dataStoreManager,
                )

            // Assert
            viewModel.uiState.test {
                skipItems(1) // Skip initial state
                val state = awaitItem()
                assertEquals(1, state.loans.size)
                assertEquals("loan_1", state.loans[0].id)
            }
        }

    @Test
    fun `fetchLoans should update uiState with error`() =
        runTest {
            // Arrange
            val errorMessage = "Network error"
            every { getMyLoansUseCase(any(), any(), any()) } returns flowOf(Resource.Error(errorMessage))

            // Act
            viewModel =
                HomeViewModel(
                    getMyLoansUseCase,
                    getUserProfileUseCase,
                    getProductsUseCase,
                    getAvailableProductUseCase,
                    loanSubmissionManager,
                    dataStoreManager,
                )

            // Assert
            viewModel.uiState.test {
                skipItems(1)
                val state = awaitItem()
                assertEquals(errorMessage, state.error)
            }
        }
}
