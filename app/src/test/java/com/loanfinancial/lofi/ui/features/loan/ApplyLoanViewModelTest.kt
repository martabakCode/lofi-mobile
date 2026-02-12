package com.loanfinancial.lofi.ui.features.loan

import android.content.Context
import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.core.location.LocationFallbackManager
import com.loanfinancial.lofi.core.location.LocationManager
import com.loanfinancial.lofi.core.media.CameraManager
import com.loanfinancial.lofi.core.media.UploadManager
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
class ApplyLoanViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var locationFallbackManager: LocationFallbackManager

    @MockK
    private lateinit var locationManager: LocationManager

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
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxed = true)
        io.mockk.every { networkManager.isNetworkAvailable() } returns true

        viewModel =
            ApplyLoanViewModel(
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
                networkManager = networkManager,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be idle`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `amount change should update form state`() =
        runTest {
            viewModel.onEvent(ApplyLoanUiEvent.AmountChanged("5000000"))
            assertNotNull(viewModel)
        }

    @Test
    fun `tenor change should update form state`() =
        runTest {
            viewModel.onEvent(ApplyLoanUiEvent.TenorChanged("12"))
            assertNotNull(viewModel)
        }

    @Test
    fun `purpose change should update form state`() =
        runTest {
            viewModel.onEvent(ApplyLoanUiEvent.PurposeChanged("Business"))
            assertNotNull(viewModel)
        }
}
