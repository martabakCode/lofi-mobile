package com.loanfinancial.lofi.ui.features.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.biometric.BiometricResult
import com.loanfinancial.lofi.core.common.result.ErrorType
import com.loanfinancial.lofi.core.location.LocationManager
import com.loanfinancial.lofi.core.location.LofiLocationResult
import com.loanfinancial.lofi.core.media.CameraManager
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.core.media.UploadManager
import com.loanfinancial.lofi.core.media.UploadResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplyLoanViewModel
    @Inject
    constructor(
        private val locationManager: LocationManager,
        private val cameraManager: CameraManager,
        private val uploadManager: UploadManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<ApplyLoanUiState>(ApplyLoanUiState.Idle)
        val uiState: StateFlow<ApplyLoanUiState> = _uiState.asStateFlow()

        private val _formState = MutableStateFlow(ApplyLoanFormState())
        val formState: StateFlow<ApplyLoanFormState> = _formState.asStateFlow()

        fun onEvent(event: ApplyLoanUiEvent) {
            when (event) {
                is ApplyLoanUiEvent.AmountChanged -> {
                    _formState.update { it.copy(amount = event.amount) }
                }
                is ApplyLoanUiEvent.TenorChanged -> {
                    _formState.update { it.copy(tenor = event.tenor) }
                }
                is ApplyLoanUiEvent.PurposeChanged -> {
                    _formState.update { it.copy(purpose = event.purpose) }
                }
                is ApplyLoanUiEvent.DocumentSelected -> {
                    handleDocumentSelected(event.documentType, event.filePath)
                }
                is ApplyLoanUiEvent.DocumentUploadStarted -> {
                    uploadDocument(event.documentType)
                }
                is ApplyLoanUiEvent.DocumentRemoved -> {
                    removeDocument(event.documentType)
                }
                is ApplyLoanUiEvent.CaptureDocument -> {
                    // Handled by UI
                }
                is ApplyLoanUiEvent.SelectDocumentFromGallery -> {
                    // Handled by UI
                }
                is ApplyLoanUiEvent.BiometricAuthenticate -> {
                    // Biometric authentication is handled at the UI layer
                    // The result will be passed back via BiometricResult event
                }
                ApplyLoanUiEvent.CaptureLocation -> {
                    captureLocation()
                }
                ApplyLoanUiEvent.SubmitClicked -> {
                    submitLoan()
                }
                ApplyLoanUiEvent.CancelClicked -> {
                    resetState()
                }
                ApplyLoanUiEvent.RetryClicked -> {
                    _uiState.value = ApplyLoanUiState.Idle
                }
                ApplyLoanUiEvent.ResetClicked -> {
                    resetState()
                }
                else -> { }
            }
        }

        private fun handleDocumentSelected(
            documentType: DocumentType,
            filePath: String,
        ) {
            val currentDocuments = _formState.value.documents.toMutableMap()
            currentDocuments[documentType] =
                DocumentUploadState(
                    documentType = documentType,
                    filePath = filePath,
                    isUploading = false,
                    isUploaded = false,
                )
            _formState.update { it.copy(documents = currentDocuments) }

            uploadDocument(documentType)
        }

        private fun uploadDocument(documentType: DocumentType) {
            val filePath = _formState.value.documents[documentType]?.filePath ?: return

            viewModelScope.launch {
                val currentDocuments = _formState.value.documents.toMutableMap()
                currentDocuments[documentType] =
                    DocumentUploadState(
                        documentType = documentType,
                        filePath = filePath,
                        isUploading = true,
                        uploadProgress = 0,
                    )
                _formState.update { it.copy(documents = currentDocuments) }

                uploadManager.uploadDocument(filePath, documentType).collect { result ->
                    when (result) {
                        is UploadResult.Progress -> {
                            currentDocuments[documentType] =
                                DocumentUploadState(
                                    documentType = documentType,
                                    filePath = filePath,
                                    isUploading = true,
                                    uploadProgress = result.percentage,
                                )
                            _formState.update { it.copy(documents = currentDocuments) }
                        }
                        is UploadResult.Success -> {
                            currentDocuments[documentType] =
                                DocumentUploadState(
                                    documentType = documentType,
                                    filePath = filePath,
                                    fileUrl = result.fileUrl,
                                    isUploading = false,
                                    isUploaded = true,
                                    uploadProgress = 100,
                                )
                            _formState.update { it.copy(documents = currentDocuments) }
                        }
                        is UploadResult.Error -> {
                            currentDocuments[documentType] =
                                DocumentUploadState(
                                    documentType = documentType,
                                    filePath = filePath,
                                    isUploading = false,
                                    error = result.message,
                                )
                            _formState.update { it.copy(documents = currentDocuments) }
                        }
                        else -> { }
                    }
                }
            }
        }

        private fun removeDocument(documentType: DocumentType) {
            val currentDocuments = _formState.value.documents.toMutableMap()
            currentDocuments.remove(documentType)
            _formState.update { it.copy(documents = currentDocuments) }
        }

        fun onBiometricResult(result: BiometricResult) {
            when (result) {
                is BiometricResult.Success -> {
                    _formState.update { it.copy(isBiometricVerified = true) }
                    _uiState.value = ApplyLoanUiState.Idle
                }
                is BiometricResult.Error -> {
                    _uiState.value =
                        ApplyLoanUiState.Error(
                            ErrorType.BiometricError(result.errorCode, result.errorMessage),
                        )
                }
                is BiometricResult.Cancelled -> {
                    _uiState.value = ApplyLoanUiState.Idle
                }
                is BiometricResult.NotAvailable -> {
                    _uiState.value =
                        ApplyLoanUiState.Error(
                            ErrorType.HardwareError("Biometric authentication not available"),
                        )
                }
                is BiometricResult.NotEnrolled -> {
                    _uiState.value =
                        ApplyLoanUiState.Error(
                            ErrorType.HardwareError("No biometric credentials enrolled"),
                        )
                }
            }
        }

        fun startBiometricAuthentication() {
            _uiState.value = ApplyLoanUiState.BiometricAuthenticating
        }

        private fun captureLocation() {
            viewModelScope.launch {
                _uiState.value = ApplyLoanUiState.CapturingLocation

                when (val result = locationManager.getCurrentLocation()) {
                    is LofiLocationResult.Success -> {
                        _formState.update {
                            it.copy(
                                latitude = result.latitude,
                                longitude = result.longitude,
                            )
                        }
                        _uiState.value = ApplyLoanUiState.Idle
                    }
                    is LofiLocationResult.Error -> {
                        _uiState.value =
                            ApplyLoanUiState.Error(
                                ErrorType.LocationError(result.message),
                            )
                    }
                    is LofiLocationResult.PermissionDenied -> {
                        _uiState.value =
                            ApplyLoanUiState.Error(
                                ErrorType.PermissionDenied("Location"),
                            )
                    }
                    is LofiLocationResult.LocationDisabled -> {
                        _uiState.value =
                            ApplyLoanUiState.Error(
                                ErrorType.HardwareError("Location services are disabled"),
                            )
                    }
                }
            }
        }

        private fun submitLoan() {
            val validationErrors = _formState.value.getValidationErrors()
            if (validationErrors.isNotEmpty()) {
                _uiState.value = ApplyLoanUiState.ValidationError(validationErrors)
                return
            }

            viewModelScope.launch {
                _uiState.value = ApplyLoanUiState.Loading

                try {
                    val formState = _formState.value

                    if (formState.latitude == null || formState.longitude == null) {
                        captureLocation()
                        if (_uiState.value is ApplyLoanUiState.Error) return@launch
                    }

                    // Biometric verification should be completed before calling submitLoan
                    // The UI layer should handle biometric authentication and call onBiometricResult
                    if (!formState.isBiometricVerified) {
                        _uiState.value =
                            ApplyLoanUiState.Error(
                                ErrorType.BiometricError(-1, "Biometric verification required"),
                            )
                        return@launch
                    }

                    val pendingDocuments = formState.documents.filter { !it.value.isUploaded }
                    if (pendingDocuments.isNotEmpty()) {
                        pendingDocuments.forEach { (type, _) ->
                            uploadDocument(type)
                        }
                    }

                    kotlinx.coroutines.delay(1000)

                    _uiState.value = ApplyLoanUiState.Success("loan_${System.currentTimeMillis()}")
                } catch (e: Exception) {
                    _uiState.value =
                        ApplyLoanUiState.Error(
                            ErrorType.UnknownError(e.message ?: "Unknown error occurred"),
                        )
                }
            }
        }

        private fun resetState() {
            _uiState.value = ApplyLoanUiState.Idle
            _formState.value = ApplyLoanFormState()
        }
    }
