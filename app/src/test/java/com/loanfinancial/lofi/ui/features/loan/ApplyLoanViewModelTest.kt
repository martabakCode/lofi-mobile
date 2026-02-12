package com.loanfinancial.lofi.ui.features.loan

import android.content.Context
import app.cash.turbine.test
import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.core.location.LocationFallbackManager
import com.loanfinancial.lofi.core.location.LocationResult
import com.loanfinancial.lofi.core.media.CameraManager
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.core.media.UploadManager
import com.loanfinancial.lofi.core.media.UploadResult
import com.loanfinancial.lofi.core.network.NetworkManager
import com.loanfinancial.lofi.data.remote.api.PinApi
import com.loanfinancial.lofi.domain.manager.LoanSubmissionManager
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import com.loanfinancial.lofi.domain.usecase.loan.GetAllLoanDraftsUseCase
import com.loanfinancial.lofi.domain.usecase.loan.GetLoanDraftByIdUseCase
import com.loanfinancial.lofi.domain.usecase.loan.SaveLoanDraftUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetUserProfileUseCase
import com.loanfinancial.lofi.domain.usecase.user.ValidateLoanSubmissionUseCase
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
class ApplyLoanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var locationFallbackManager: LocationFallbackManager

    @MockK
    private lateinit var locationManager: com.loanfinancial.lofi.core.location.LocationManager

    @MockK
    private lateinit var cameraManager: CameraManager

    @MockK
    private lateinit var uploadManager: UploadManager

    @MockK
    private lateinit var getUserProfileUseCase: GetUserProfileUseCase

    @MockK
    private lateinit var loanSubmissionManager: LoanSubmissionManager

    @MockK
    private lateinit var loanRepository: ILoanRepository

    @MockK
    private lateinit var documentRepository: IDocumentRepository

    @MockK
    private lateinit var saveLoanDraftUseCase: SaveLoanDraftUseCase

    @MockK
    private lateinit var getLoanDraftByIdUseCase: GetLoanDraftByIdUseCase

    @MockK
    private lateinit var getAllLoanDraftsUseCase: GetAllLoanDraftsUseCase

    @MockK
    private lateinit var validateLoanSubmissionUseCase: ValidateLoanSubmissionUseCase

    @MockK
    private lateinit var pinApi: PinApi

    @MockK
    private lateinit var networkManager: NetworkManager

    private lateinit var viewModel: ApplyLoanViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Default mocks
        every { getUserProfileUseCase() } returns flowOf(com.loanfinancial.lofi.core.util.Resource.Loading)
        every { networkManager.isNetworkAvailable() } returns true
        
        viewModel = ApplyLoanViewModel(
            context = context,
            locationFallbackManager = locationFallbackManager,
            locationManager = locationManager,
            cameraManager = cameraManager,
            uploadManager = uploadManager,
            getUserProfileUseCase = getUserProfileUseCase,
            loanSubmissionManager = loanSubmissionManager,
            loanRepository = loanRepository,
            documentRepository = documentRepository,
            saveLoanDraftUseCase = saveLoanDraftUseCase,
            getLoanDraftByIdUseCase = getLoanDraftByIdUseCase,
            getAllLoanDraftsUseCase = getAllLoanDraftsUseCase,
            validateLoanSubmissionUseCase = validateLoanSubmissionUseCase,
            pinApi = pinApi,
            networkManager = networkManager
        )
    }

    @Test
    fun `initial state should be idle`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ApplyLoanUiState.Idle || state is ApplyLoanUiState.Loading)
        }
    }

    @Test
    fun `amount change should update form state`() = runTest {
        // Act
        viewModel.onEvent(ApplyLoanUiEvent.AmountChanged("5000000"))

        // Assert
        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("5000000", state.amount)
        }
    }

    @Test
    fun `tenor change should update form state`() = runTest {
        // Act
        viewModel.onEvent(ApplyLoanUiEvent.TenorChanged("12"))

        // Assert
        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("12", state.tenor)
        }
    }

    @Test
    fun `purpose change should update form state`() = runTest {
        // Act
        viewModel.onEvent(ApplyLoanUiEvent.PurposeChanged("Business"))

        // Assert
        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("Business", state.purpose)
        }
    }

    @Test
    fun `form validation should fail with empty fields`() = runTest {
        // Assert
        viewModel.formState.test {
            val state = awaitItem()
            assertFalse(state.isValid())
            assertTrue(state.getValidationErrors().isNotEmpty())
        }
    }

    @Test
    fun `location capture success should update form state`() = runTest {
        // Arrange
        coEvery { locationFallbackManager.getLocation() } returns LocationResult.Success(-6.2088, 106.8456, "GPS")

        // Act
        viewModel.onEvent(ApplyLoanUiEvent.CaptureLocation)

        // Assert
        viewModel.formState.test {
            skipItems(1) // Skip initial
            val state = awaitItem()
            assertEquals(-6.2088, state.latitude ?: 0.0, 0.0001)
            assertEquals(106.8456, state.longitude ?: 0.0, 0.0001)
        }
    }

    @Test
    fun `location capture error should show error state`() = runTest {
        // Arrange
        coEvery { locationFallbackManager.getLocation() } returns LocationResult.Error("Permission denied")

        // Act
        viewModel.onEvent(ApplyLoanUiEvent.CaptureLocation)

        // Assert
        viewModel.uiState.test {
            skipItems(1) // Skip loading
            val state = awaitItem()
            assertTrue(state is ApplyLoanUiState.Error)
        }
    }

    @Test
    fun `document upload success should update document state`() = runTest {
        // Arrange
        val filePath = "/path/to/ktp.jpg"
        coEvery { 
            uploadManager.uploadDocument(filePath, DocumentType.KTP, any()) 
        } returns flowOf(
            UploadResult.Progress(50),
            UploadResult.Success(DocumentType.KTP, "https://example.com/ktp.jpg", "ktp.jpg")
        )

        // Act
        viewModel.onEvent(ApplyLoanUiEvent.DocumentSelected(DocumentType.KTP, filePath))

        // Assert
        viewModel.formState.test {
            skipItems(1) // Skip initial adding
            val state = awaitItem()
            val ktpState = state.documents[DocumentType.KTP]
            assertTrue(ktpState?.isUploaded == true)
            assertEquals("https://example.com/ktp.jpg", ktpState?.fileUrl)
        }
    }

    @Test
    fun `reset should clear all state`() = runTest {
        // Arrange
        viewModel.onEvent(ApplyLoanUiEvent.AmountChanged("5000000"))

        // Act
        viewModel.onEvent(ApplyLoanUiEvent.ResetClicked)

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ApplyLoanUiState.Idle)
        }

        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("", state.amount)
            assertEquals("", state.tenor)
            assertEquals("", state.purpose)
            assertTrue(state.documents.isEmpty())
            assertFalse(state.isBiometricVerified)
            assertNull(state.latitude)
            assertNull(state.longitude)
        }
    }

    @Test
    fun `cancel should reset state`() = runTest {
        // Arrange
        viewModel.onEvent(ApplyLoanUiEvent.AmountChanged("5000000"))

        // Act
        viewModel.onEvent(ApplyLoanUiEvent.CancelClicked)

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ApplyLoanUiState.Idle)
        }
    }

    @Test
    fun `next step should advance current step`() = runTest {
        // Arrange - fill basic info first
        viewModel.onEvent(ApplyLoanUiEvent.AmountChanged("5000000"))
        viewModel.onEvent(ApplyLoanUiEvent.TenorChanged("12"))
        viewModel.onEvent(ApplyLoanUiEvent.PurposeChanged("Business"))

        // Act
        viewModel.onEvent(ApplyLoanUiEvent.NextStepClicked)

        // Assert
        viewModel.formState.test {
            skipItems(3) // Skip field updates
            val state = awaitItem()
            assertEquals(2, state.currentStep)
        }
    }

    @Test
    fun `previous step should go back`() = runTest {
        // Arrange - go to step 2 first
        viewModel.onEvent(ApplyLoanUiEvent.AmountChanged("5000000"))
        viewModel.onEvent(ApplyLoanUiEvent.TenorChanged("12"))
        viewModel.onEvent(ApplyLoanUiEvent.PurposeChanged("Business"))
        viewModel.onEvent(ApplyLoanUiEvent.NextStepClicked)

        // Act
        viewModel.onEvent(ApplyLoanUiEvent.PreviousStepClicked)

        // Assert
        viewModel.formState.test {
            skipItems(4) // Skip updates
            val state = awaitItem()
            assertEquals(1, state.currentStep)
        }
    }

    @Test
    fun `document removal should remove from state`() = runTest {
        // Arrange - add document first
        val filePath = "/path/to/ktp.jpg"
        coEvery { 
            uploadManager.uploadDocument(filePath, DocumentType.KTP, any()) 
        } returns flowOf(UploadResult.Success(DocumentType.KTP, "url", "name"))
        
        viewModel.onEvent(ApplyLoanUiEvent.DocumentSelected(DocumentType.KTP, filePath))

        // Act
        viewModel.onEvent(ApplyLoanUiEvent.DocumentRemoved(DocumentType.KTP))

        // Assert
        viewModel.formState.test {
            skipItems(2) // Skip add and remove
            val state = awaitItem()
            assertNull(state.documents[DocumentType.KTP])
        }
    }
}
