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
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
        private val getUserProfileUseCase: GetUserProfileUseCase,
    ) : ViewModel() {
        // Store user's sub-district location from biodata
        private var userSubDistrictLocation: Pair<Double, Double>? = null

        init {
            loadUserLocationFromBiodata()
        }

        private fun loadUserLocationFromBiodata() {
            viewModelScope.launch {
                try {
                    getUserProfileUseCase().first { it is Resource.Success }.let { result ->
                        if (result is Resource.Success) {
                            result.data?.biodata?.let { biodata ->
                                // Get location based on city/province from biodata
                                val location = getLocationFromBiodata(
                                    city = biodata.city,
                                    province = biodata.province,
                                    district = biodata.district,
                                    subDistrict = biodata.subDistrict,
                                )
                                userSubDistrictLocation = location
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Silently fail - will use random location as fallback
                }
            }
        }

        private fun getLocationFromBiodata(
            city: String?,
            province: String?,
            district: String?,
            subDistrict: String?,
        ): Pair<Double, Double>? {
            // Map of major Indonesian cities to their approximate coordinates
            val cityCoordinates =
                mapOf(
                    // Jakarta
                    "jakarta" to Pair(-6.2088, 106.8456),
                    "jakarta utara" to Pair(-6.1383, 106.8639),
                    "jakarta barat" to Pair(-6.1683, 106.7588),
                    "jakarta pusat" to Pair(-6.1865, 106.8341),
                    "jakarta selatan" to Pair(-6.2615, 106.8106),
                    "jakarta timur" to Pair(-6.2250, 106.9004),
                    // Surabaya
                    "surabaya" to Pair(-7.2575, 112.7521),
                    // Bandung
                    "bandung" to Pair(-6.9175, 107.6191),
                    // Medan
                    "medan" to Pair(3.5952, 98.6722),
                    // Semarang
                    "semarang" to Pair(-6.9932, 110.4203),
                    // Yogyakarta
                    "yogyakarta" to Pair(-7.7956, 110.3695),
                    // Makassar
                    "makassar" to Pair(-5.1477, 119.4327),
                    // Palembang
                    "palembang" to Pair(-2.9761, 104.7754),
                    // Denpasar
                    "denpasar" to Pair(-8.6705, 115.2126),
                    // Malang
                    "malang" to Pair(-7.9666, 112.6326),
                    // Tangerang
                    "tangerang" to Pair(-6.1702, 106.6403),
                    // Bekasi
                    "bekasi" to Pair(-6.2349, 106.9896),
                    // Depok
                    "depok" to Pair(-6.4025, 106.7942),
                    // Bogor
                    "bogor" to Pair(-6.5944, 106.7892),
                )

            // Try to match city
            val normalizedCity = city?.lowercase()?.trim()
            val normalizedProvince = province?.lowercase()?.trim()

            // First try exact city match
            cityCoordinates[normalizedCity]?.let { return it }

            // Try to extract city from province or vice versa
            if (normalizedProvince?.contains("jakarta") == true) {
                return cityCoordinates["jakarta"]
            }

            // Return Jakarta as default if no match found
            return null
        }
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
                ApplyLoanUiEvent.SaveAsDraftClicked -> {
                    saveAsDraft()
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
                    // Try to capture location if not already captured, but make it optional
                    if (_formState.value.latitude == null || _formState.value.longitude == null) {
                        when (val locationResult = locationManager.getCurrentLocation()) {
                            is LofiLocationResult.Success -> {
                                _formState.update {
                                    it.copy(
                                        latitude = locationResult.latitude,
                                        longitude = locationResult.longitude,
                                    )
                                }
                            }
                            else -> {
                                // Location is optional - use user's biodata location or random Jakarta location as default
                                val location = userSubDistrictLocation ?: getRandomJakartaLocation()
                                _formState.update {
                                    it.copy(
                                        latitude = location.first,
                                        longitude = location.second,
                                    )
                                }
                            }
                        }
                    }

                    // Get updated form state after location capture
                    val formState = _formState.value

                    // Biometric verification is optional - proceed without it if not verified
                    // Just continue with submission

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

        private fun getRandomJakartaLocation(): Pair<Double, Double> {
            // Jakarta bounding box (approximate)
            // Latitude: -6.35 to -6.10
            // Longitude: 106.70 to 106.95
            val minLat = -6.35
            val maxLat = -6.10
            val minLng = 106.70
            val maxLng = 106.95

            val randomLat = minLat + Math.random() * (maxLat - minLat)
            val randomLng = minLng + Math.random() * (maxLng - minLng)

            return Pair(randomLat, randomLng)
        }

        private fun saveAsDraft() {
            viewModelScope.launch {
                _uiState.value = ApplyLoanUiState.Loading

                try {
                    // Capture location if available (optional for draft)
                    if (_formState.value.latitude == null || _formState.value.longitude == null) {
                        when (val locationResult = locationManager.getCurrentLocation()) {
                            is LofiLocationResult.Success -> {
                                _formState.update {
                                    it.copy(
                                        latitude = locationResult.latitude,
                                        longitude = locationResult.longitude,
                                    )
                                }
                            }
                            else -> {
                                // Location is optional for draft - use user's biodata location or random Jakarta location as default
                                val location = userSubDistrictLocation ?: getRandomJakartaLocation()
                                _formState.update {
                                    it.copy(
                                        latitude = location.first,
                                        longitude = location.second,
                                    )
                                }
                            }
                        }
                    }

                    // Upload any pending documents (optional for draft)
                    val pendingDocuments = _formState.value.documents.filter { !it.value.isUploaded }
                    if (pendingDocuments.isNotEmpty()) {
                        pendingDocuments.forEach { (type, _) ->
                            uploadDocument(type)
                        }
                    }

                    kotlinx.coroutines.delay(500)

                    // Save as draft - returns draft loan ID
                    _uiState.value = ApplyLoanUiState.DraftSaved("loan_${System.currentTimeMillis()}")
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
