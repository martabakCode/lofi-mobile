package com.loanfinancial.lofi.ui.features.loan

import app.cash.turbine.test
import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.core.biometric.BiometricAuthenticator
import com.loanfinancial.lofi.core.biometric.BiometricResult
import com.loanfinancial.lofi.core.location.LocationManager
import com.loanfinancial.lofi.core.location.LocationResult
import com.loanfinancial.lofi.core.media.CameraManager
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.core.media.UploadManager
import com.loanfinancial.lofi.core.media.UploadResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ApplyLoanViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var biometricAuthenticator: BiometricAuthenticator

    @MockK
    private lateinit var locationManager: LocationManager

    @MockK
    private lateinit var cameraManager: CameraManager

    @MockK
    private lateinit var uploadManager: UploadManager

    private lateinit var viewModel: ApplyLoanViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel =
            ApplyLoanViewModel(
                biometricAuthenticator = biometricAuthenticator,
                locationManager = locationManager,
                cameraManager = cameraManager,
                uploadManager = uploadManager,
            )
    }

    @Test
    fun `initial state should be idle`() =
        runTest {
            viewModel.uiState.test {
                assertEquals(ApplyLoanUiState.Idle, awaitItem())
            }
        }

    @Test
    fun `amount change should update form state`() =
        runTest {
            viewModel.onEvent(ApplyLoanUiEvent.AmountChanged("5000000"))

            viewModel.formState.test {
                val state = awaitItem()
                assertEquals("5000000", state.amount)
            }
        }

    @Test
    fun `tenor change should update form state`() =
        runTest {
            viewModel.onEvent(ApplyLoanUiEvent.TenorChanged("12"))

            viewModel.formState.test {
                val state = awaitItem()
                assertEquals("12", state.tenor)
            }
        }

    @Test
    fun `purpose change should update form state`() =
        runTest {
            viewModel.onEvent(ApplyLoanUiEvent.PurposeChanged("Business"))

            viewModel.formState.test {
                val state = awaitItem()
                assertEquals("Business", state.purpose)
            }
        }

    @Test
    fun `form validation should fail with empty fields`() =
        runTest {
            viewModel.formState.test {
                val state = awaitItem()
                assertFalse(state.isValid())
                assertTrue(state.getValidationErrors().isNotEmpty())
            }
        }

    @Test
    fun `form validation should pass with all required fields`() =
        runTest {
            viewModel.onEvent(ApplyLoanUiEvent.AmountChanged("5000000"))
            viewModel.onEvent(ApplyLoanUiEvent.TenorChanged("12"))
            viewModel.onEvent(ApplyLoanUiEvent.PurposeChanged("Business"))

            viewModel.formState.test {
                skipItems(3)
                val state = awaitItem()
                assertFalse(state.isValid())
            }
        }

    @Test
    fun `biometric authentication success should update state`() =
        runTest {
            coEvery {
                biometricAuthenticator.authenticate(any(), any(), any(), any())
            } returns flowOf(BiometricResult.Success)

            viewModel.onEvent(ApplyLoanUiEvent.BiometricAuthenticate)

            viewModel.uiState.test {
                assertEquals(ApplyLoanUiState.BiometricAuthenticating, awaitItem())
                assertEquals(ApplyLoanUiState.Idle, awaitItem())
            }

            viewModel.formState.test {
                val state = awaitItem()
                assertTrue(state.isBiometricVerified)
            }
        }

    @Test
    fun `biometric authentication error should show error state`() =
        runTest {
            coEvery {
                biometricAuthenticator.authenticate(any(), any(), any(), any())
            } returns flowOf(BiometricResult.Error(1, "Authentication failed"))

            viewModel.onEvent(ApplyLoanUiEvent.BiometricAuthenticate)

            viewModel.uiState.test {
                assertEquals(ApplyLoanUiState.BiometricAuthenticating, awaitItem())
                val errorState = awaitItem() as ApplyLoanUiState.Error
                assertTrue(errorState.error is com.loanfinancial.lofi.core.common.result.ErrorType.BiometricError)
            }
        }

    @Test
    fun `location capture success should update form state`() =
        runTest {
            coEvery { locationManager.getCurrentLocation() } returns LocationResult.Success(-6.2088, 106.8456)

            viewModel.onEvent(ApplyLoanUiEvent.CaptureLocation)

            viewModel.uiState.test {
                assertEquals(ApplyLoanUiState.CapturingLocation, awaitItem())
                assertEquals(ApplyLoanUiState.Idle, awaitItem())
            }

            viewModel.formState.test {
                val state = awaitItem()
                assertEquals(-6.2088, state.latitude, 0.0001)
                assertEquals(106.8456, state.longitude, 0.0001)
            }
        }

    @Test
    fun `location capture permission denied should show error`() =
        runTest {
            coEvery { locationManager.getCurrentLocation() } returns LocationResult.PermissionDenied

            viewModel.onEvent(ApplyLoanUiEvent.CaptureLocation)

            viewModel.uiState.test {
                assertEquals(ApplyLoanUiState.CapturingLocation, awaitItem())
                val errorState = awaitItem() as ApplyLoanUiState.Error
                assertTrue(errorState.error is com.loanfinancial.lofi.core.common.result.ErrorType.PermissionDenied)
            }
        }

    @Test
    fun `document upload success should update document state`() =
        runTest {
            val filePath = "/path/to/ktp.jpg"
            coEvery {
                uploadManager.uploadDocument(filePath, DocumentType.KTP, any())
            } returns
                flowOf(
                    UploadResult.Progress(50),
                    UploadResult.Success(DocumentType.KTP, "https://example.com/ktp.jpg", "ktp.jpg"),
                )

            viewModel.onEvent(ApplyLoanUiEvent.DocumentSelected(DocumentType.KTP, filePath))

            viewModel.formState.test {
                skipItems(1)
                val state = awaitItem()
                val ktpState = state.documents[DocumentType.KTP]
                assertTrue(ktpState?.isUploaded == true)
                assertEquals("https://example.com/ktp.jpg", ktpState?.fileUrl)
            }
        }

    @Test
    fun `reset should clear all state`() =
        runTest {
            viewModel.onEvent(ApplyLoanUiEvent.AmountChanged("5000000"))
            viewModel.onEvent(ApplyLoanUiEvent.ResetClicked)

            viewModel.uiState.test {
                assertEquals(ApplyLoanUiState.Idle, awaitItem())
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
    fun `cancel should reset state`() =
        runTest {
            viewModel.onEvent(ApplyLoanUiEvent.AmountChanged("5000000"))
            viewModel.onEvent(ApplyLoanUiEvent.CancelClicked)

            viewModel.uiState.test {
                assertEquals(ApplyLoanUiState.Idle, awaitItem())
            }

            viewModel.formState.test {
                val state = awaitItem()
                assertEquals("", state.amount)
            }
        }
}
