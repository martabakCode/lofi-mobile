package com.loanfinancial.lofi.ui.features.loan

import app.cash.turbine.test
import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.manager.LoanSubmissionManager
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.Product
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.domain.usecase.loan.CreateLoanUseCase
import com.loanfinancial.lofi.domain.usecase.loan.SubmitLoanUseCase
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
class LoanTnCViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
        MockKAnnotations.init(this)
        viewModel = LoanTnCViewModel(
            createLoanUseCase,
            submitLoanUseCase,
            loanSubmissionManager,
            getUserProfileUseCase
        )
    }

    @Test
    fun `initial state should be correct`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isAgreementChecked)
            assertFalse(state.isSubmitting)
            assertFalse(state.isSuccess)
            assertNull(state.errorMessage)
            assertNull(state.createdLoanId)
        }
    }

    @Test
    fun `onAgreementCheckedChange should update state`() = runTest {
        viewModel.onAgreementCheckedChange(true)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isAgreementChecked)
        }
    }

    @Test
    fun `onSubmitClicked should show error when agreement not checked`() = runTest {
        // Arrange - agreement not checked (default)
        viewModel.setLoanFormData(createLoanFormData())

        // Act
        viewModel.onSubmitClicked()

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.errorMessage)
            assertFalse(state.isSubmitting)
        }
    }

    @Test
    fun `onSubmitClicked should show error when form data is null`() = runTest {
        // Arrange
        viewModel.onAgreementCheckedChange(true)

        // Act - don't set form data
        viewModel.onSubmitClicked()

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.errorMessage)
            assertEquals("Data pengajuan tidak ditemukan", state.errorMessage)
        }
    }

    @Test
    fun `onSubmitClicked should create loan and submit successfully`() = runTest {
        // Arrange
        val formData = createLoanFormData()
        val createdLoan = createLoan("loan_123", "DRAFT")
        val submittedLoan = createLoan("loan_123", "SUBMITTED")

        viewModel.setLoanFormData(formData)
        viewModel.onAgreementCheckedChange(true)

        coEvery { 
            createLoanUseCase(any()) 
        } returns flowOf(Resource.Success(createdLoan))
        
        coEvery { 
            submitLoanUseCase("loan_123") 
        } returns flowOf(Resource.Success(submittedLoan))

        // Act
        viewModel.onSubmitClicked()

        // Assert
        viewModel.uiState.test {
            // Loading state
            assertTrue(awaitItem().isSubmitting)
            // Success state
            val successState = awaitItem()
            assertTrue(successState.isSuccess)
            assertEquals("loan_123", successState.createdLoanId)
        }
    }

    @Test
    fun `onSubmitClicked should skip submit when loan already submitted`() = runTest {
        // Arrange
        val formData = createLoanFormData()
        val alreadySubmittedLoan = createLoan("loan_123", "SUBMITTED")

        viewModel.setLoanFormData(formData)
        viewModel.onAgreementCheckedChange(true)

        coEvery { 
            createLoanUseCase(any()) 
        } returns flowOf(Resource.Success(alreadySubmittedLoan))

        // Act
        viewModel.onSubmitClicked()

        // Assert
        viewModel.uiState.test {
            assertTrue(awaitItem().isSubmitting)
            val successState = awaitItem()
            assertTrue(successState.isSuccess)
        }
        
        // Verify submitLoanUseCase is NOT called
        coVerify(exactly = 0) { submitLoanUseCase(any()) }
    }

    @Test
    fun `onSubmitClicked should fallback to offline on network error`() = runTest {
        // Arrange
        val formData = createLoanFormData()
        val user = UserUpdateData(
            id = "user_123",
            fullName = "Test User",
            email = "test@test.com",
            phoneNumber = "+6281234567890",
            profilePictureUrl = null,
            biodata = null,
            branch = null,
            product = null,
            pinSet = false,
            profileCompleted = false
        )

        viewModel.setLoanFormData(formData)
        viewModel.onAgreementCheckedChange(true)

        // Network error on create
        coEvery { 
            createLoanUseCase(any()) 
        } returns flowOf(Resource.Error("Unable to resolve host"))
        
        // Profile for offline submission
        coEvery { 
            getUserProfileUseCase() 
        } returns flowOf(com.loanfinancial.lofi.core.util.Resource.Success(user))
        
        // Offline submission success
        coEvery { 
            loanSubmissionManager.submitLoanOffline(any()) 
        } returns Result.success("offline_loan_id")

        // Act
        viewModel.onSubmitClicked()

        // Assert
        viewModel.uiState.test {
            assertTrue(awaitItem().isSubmitting)
            val successState = awaitItem()
            assertTrue(successState.isSuccess)
            assertEquals("offline_loan_id", successState.createdLoanId)
        }
    }

    @Test
    fun `onSubmitClicked should show error on business logic error`() = runTest {
        // Arrange
        val formData = createLoanFormData()

        viewModel.setLoanFormData(formData)
        viewModel.onAgreementCheckedChange(true)

        // Business error (not network)
        coEvery { 
            createLoanUseCase(any()) 
        } returns flowOf(Resource.Error("Invalid loan amount"))

        // Act
        viewModel.onSubmitClicked()

        // Assert
        viewModel.uiState.test {
            assertTrue(awaitItem().isSubmitting)
            val errorState = awaitItem()
            assertNotNull(errorState.errorMessage)
            assertFalse(errorState.isSuccess)
        }
        
        // Verify offline fallback is NOT called for business errors
        coVerify(exactly = 0) { loanSubmissionManager.submitLoanOffline(any()) }
    }

    @Test
    fun `onDismissError should clear error message`() = runTest {
        // Arrange - trigger an error
        viewModel.onAgreementCheckedChange(true)
        viewModel.onSubmitClicked() // No form data, will show error

        // Act
        viewModel.onDismissError()

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.errorMessage)
        }
    }

    @Test
    fun `onDismissSuccess should reset state`() = runTest {
        // Arrange
        viewModel.setLoanFormData(createLoanFormData())
        viewModel.onAgreementCheckedChange(true)

        val loan = createLoan("loan_123", "SUBMITTED")
        coEvery { createLoanUseCase(any()) } returns flowOf(Resource.Success(loan))

        viewModel.onSubmitClicked()

        // Act
        viewModel.onDismissSuccess()

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isSuccess)
            assertFalse(state.isSubmitting)
            assertNull(state.createdLoanId)
        }
    }

    @Test
    fun `resetState should clear all state`() = runTest {
        // Arrange
        viewModel.setLoanFormData(createLoanFormData())
        viewModel.onAgreementCheckedChange(true)

        // Act
        viewModel.resetState()

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isAgreementChecked)
            assertFalse(state.isSuccess)
            assertNull(state.errorMessage)
        }
    }

    private fun createLoanFormData() = LoanFormData(
        amount = "5000000",
        tenor = "12",
        purpose = "Business",
        latitude = -6.2088,
        longitude = 106.8456,
        documents = emptyMap()
    )

    private fun createLoan(id: String, status: String) = Loan(
        id = id,
        customerName = "Test Customer",
        product = Product(
            productCode = "CASH_LOAN",
            productName = "Pinjaman Tunai",
            interestRate = 0.05
        ),
        loanAmount = 5000000,
        tenor = 12,
        loanStatus = status,
        currentStage = "SUBMISSION",
        submittedAt = "2024-01-15T10:30:00Z",
        reviewedAt = null,
        approvedAt = null,
        rejectedAt = null,
        disbursedAt = null,
        loanStatusDisplay = status,
        slaDurationHours = 24
    )
}
