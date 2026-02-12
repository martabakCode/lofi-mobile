package com.loanfinancial.lofi.ui.features.loan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.biometric.BiometricResult

import com.loanfinancial.lofi.core.network.NetworkManager
import com.loanfinancial.lofi.data.remote.api.PinApi
import com.loanfinancial.lofi.data.remote.api.PinVerificationRequest
import com.loanfinancial.lofi.core.common.result.ErrorType
import com.loanfinancial.lofi.core.location.LocationManager
import com.loanfinancial.lofi.core.location.LofiLocationResult
import com.loanfinancial.lofi.core.media.CameraManager
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.core.media.UploadManager
import com.loanfinancial.lofi.core.media.UploadResult
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import com.loanfinancial.lofi.core.location.LocationFallbackManager
import com.loanfinancial.lofi.core.location.LocationResult
import com.loanfinancial.lofi.domain.usecase.user.GetUserProfileUseCase
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.domain.usecase.loan.GetAllLoanDraftsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplyLoanViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val locationFallbackManager: LocationFallbackManager,
        private val locationManager: LocationManager,
        private val cameraManager: CameraManager,
        private val uploadManager: UploadManager,
        private val getUserProfileUseCase: GetUserProfileUseCase,
        private val loanSubmissionManager: com.loanfinancial.lofi.domain.manager.LoanSubmissionManager,
        private val loanRepository: com.loanfinancial.lofi.domain.repository.ILoanRepository,
        private val documentRepository: IDocumentRepository,
        private val saveLoanDraftUseCase: com.loanfinancial.lofi.domain.usecase.loan.SaveLoanDraftUseCase,
        private val getLoanDraftByIdUseCase: com.loanfinancial.lofi.domain.usecase.loan.GetLoanDraftByIdUseCase,
        private val getAllLoanDraftsUseCase: GetAllLoanDraftsUseCase,
        private val validateLoanSubmissionUseCase: com.loanfinancial.lofi.domain.usecase.user.ValidateLoanSubmissionUseCase,
        private val pinApi: PinApi,
        private val networkManager: NetworkManager,
    ) : ViewModel() {
        init {
            preFillFromProfile()
        }

        private fun preFillFromProfile() {
            getUserProfileUseCase()
                .onEach { result ->
                    if (result is Resource.Success<UserUpdateData>) {
                        val profile = result.data
                        val biodata = profile?.biodata
                        
                        _formState.update { state ->
                            // Only pre-fill if the field is currently empty to avoid overwriting user input or draft data
                            state.copy(
                                accountHolderName = state.accountHolderName.ifBlank { profile?.fullName ?: "" },
                                jobPosition = state.jobPosition.ifBlank { biodata?.occupation ?: "" },
                                jobType = state.jobType.ifBlank { biodata?.incomeType ?: "" },
                                declaredIncome = state.declaredIncome.ifBlank { 
                                    biodata?.monthlyIncome?.let { 
                                        if (it > 0) String.format("%.0f", it) else "" 
                                    } ?: ""
                                }
                            )
                        }
                    }
                }.launchIn(viewModelScope)
        }

        /**
         * Load an existing draft by ID and populate the form
         */
        fun loadDraft(draftId: String) {
            viewModelScope.launch {
                try {
                    _uiState.value = ApplyLoanUiState.Loading
                    
                    val draft = getLoanDraftByIdUseCase(draftId)
                    if (draft != null) {
                        // Convert document paths from draft to document upload states
                        val documentsMap = draft.documentPaths?.let { paths ->
                            paths.mapNotNull { (key, path) ->
                                try {
                                    val docType = com.loanfinancial.lofi.core.media.DocumentType.valueOf(key)
                                    docType to DocumentUploadState(
                                        documentType = docType,
                                        filePath = path,
                                        isUploading = false,
                                        isUploaded = false
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }.toMap()
                        } ?: emptyMap()
                        
                        _formState.update {
                            it.copy(
                                amount = draft.amount?.toString() ?: "",
                                tenor = draft.tenor?.toString() ?: "",
                                purpose = draft.purpose ?: "",
                                downPayment = draft.downPayment?.toString() ?: "",
                                jobType = draft.jobType ?: "",
                                companyName = draft.companyName ?: "",
                                jobPosition = draft.jobPosition ?: "",
                                workDurationMonths = draft.workDurationMonths?.toString() ?: "",
                                workAddress = draft.workAddress ?: "",
                                officePhoneNumber = draft.officePhoneNumber ?: "",
                                declaredIncome = draft.declaredIncome?.toString() ?: "",
                                additionalIncome = draft.additionalIncome?.toString() ?: "",
                                npwpNumber = draft.npwpNumber ?: "",
                                emergencyContactName = draft.emergencyContactName ?: "",
                                emergencyContactRelation = draft.emergencyContactRelation ?: "",
                                emergencyContactPhone = draft.emergencyContactPhone ?: "",
                                emergencyContactAddress = draft.emergencyContactAddress ?: "",
                                bankName = draft.bankName ?: "",
                                bankBranch = draft.bankBranch ?: "",
                                accountNumber = draft.accountNumber ?: "",
                                accountHolderName = draft.accountHolderName ?: "",
                                latitude = draft.latitude,
                                longitude = draft.longitude,
                                isBiometricVerified = draft.isBiometricVerified,
                                draftId = draft.id,
                                documents = documentsMap,
                                currentStep = when(draft.currentStep) {
                                    com.loanfinancial.lofi.domain.model.DraftStep.BASIC_INFO -> 1
                                    com.loanfinancial.lofi.domain.model.DraftStep.EMPLOYMENT_INFO -> 2
                                    com.loanfinancial.lofi.domain.model.DraftStep.EMERGENCY_CONTACT -> 3
                                    com.loanfinancial.lofi.domain.model.DraftStep.BANK_INFO -> 4
                                    com.loanfinancial.lofi.domain.model.DraftStep.DOCUMENTS -> 4
                                    else -> 1
                                }
                            )
                        }
                        _uiState.value = ApplyLoanUiState.DraftLoaded(draftId)
                    } else {
                        _uiState.value = ApplyLoanUiState.Error(
                            ErrorType.UnknownError("Draft not found")
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = ApplyLoanUiState.Error(
                        ErrorType.UnknownError(e.message ?: "Failed to load draft")
                    )
                }
            }
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
                is ApplyLoanUiEvent.DownPaymentChanged -> {
                    _formState.update { it.copy(downPayment = event.downPayment) }
                }
                is ApplyLoanUiEvent.JobTypeChanged -> {
                    _formState.update { it.copy(jobType = event.jobType) }
                }
                is ApplyLoanUiEvent.CompanyNameChanged -> {
                    _formState.update { it.copy(companyName = event.companyName) }
                }
                is ApplyLoanUiEvent.JobPositionChanged -> {
                    _formState.update { it.copy(jobPosition = event.jobPosition) }
                }
                is ApplyLoanUiEvent.WorkDurationMonthsChanged -> {
                    _formState.update { it.copy(workDurationMonths = event.months) }
                }
                is ApplyLoanUiEvent.WorkAddressChanged -> {
                    _formState.update { it.copy(workAddress = event.address) }
                }
                is ApplyLoanUiEvent.OfficePhoneNumberChanged -> {
                    _formState.update { it.copy(officePhoneNumber = event.phone) }
                }
                is ApplyLoanUiEvent.DeclaredIncomeChanged -> {
                    _formState.update { it.copy(declaredIncome = event.income) }
                }
                is ApplyLoanUiEvent.AdditionalIncomeChanged -> {
                    _formState.update { it.copy(additionalIncome = event.income) }
                }
                is ApplyLoanUiEvent.NpwpNumberChanged -> {
                    _formState.update { it.copy(npwpNumber = event.npwp) }
                }
                is ApplyLoanUiEvent.EmergencyContactNameChanged -> {
                    _formState.update { it.copy(emergencyContactName = event.name) }
                }
                is ApplyLoanUiEvent.EmergencyContactRelationChanged -> {
                    _formState.update { it.copy(emergencyContactRelation = event.relation) }
                }
                is ApplyLoanUiEvent.EmergencyContactPhoneChanged -> {
                    _formState.update { it.copy(emergencyContactPhone = event.phone) }
                }
                is ApplyLoanUiEvent.EmergencyContactAddressChanged -> {
                    _formState.update { it.copy(emergencyContactAddress = event.address) }
                }
                is ApplyLoanUiEvent.BankNameChanged -> {
                    _formState.update { it.copy(bankName = event.bank) }
                }
                is ApplyLoanUiEvent.BankBranchChanged -> {
                    _formState.update { it.copy(bankBranch = event.branch) }
                }
                is ApplyLoanUiEvent.AccountNumberChanged -> {
                    _formState.update { it.copy(accountNumber = event.number) }
                }
                is ApplyLoanUiEvent.AccountHolderNameChanged -> {
                    _formState.update { it.copy(accountHolderName = event.name) }
                }
                is ApplyLoanUiEvent.NextStepClicked -> {
                    handleNextStep()
                }
                is ApplyLoanUiEvent.PreviousStepClicked -> {
                    _formState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1)) }
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
                is ApplyLoanUiEvent.AutoBiometricAuthenticate -> {
                     // Handled by UI same as BiometricAuthenticate
                     // This event is just a trigger to separate the intent
                     // potentially setting a specific UI state
                     _uiState.value = ApplyLoanUiState.AutoCapturingBiometric
                }
                is ApplyLoanUiEvent.CaptureLocation -> {
                    captureLocation()
                }
                is ApplyLoanUiEvent.AutoCaptureLocation -> {
                    captureLocation(isAuto = true)
                }
                is ApplyLoanUiEvent.PinSubmitted -> {
                    verifyPin(event.pin)
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
                ApplyLoanUiEvent.ProceedToDocumentUpload -> {
                    proceedToDocumentUpload()
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
                    _uiState.value = ApplyLoanUiState.PinRequired
                }
                is BiometricResult.NotEnrolled -> {
                    _uiState.value = ApplyLoanUiState.PinRequired
                }
            }
        }

        fun startBiometricAuthentication() {
             _uiState.value = ApplyLoanUiState.BiometricAuthenticating
        }

        fun verifyPin(pin: String) {
            viewModelScope.launch {
                _uiState.value = ApplyLoanUiState.Loading

                if (!networkManager.isNetworkAvailable()) {
                    _uiState.value = ApplyLoanUiState.Error(ErrorType.NetworkError("No internet connection"))
                    return@launch
                }

                try {
                    val request = PinVerificationRequest(pin = pin, purpose = "LOAN_SUBMISSION")
                    val response = pinApi.verifyPin(request)

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true && body.data != null) {
                            val data = body.data
                            if (data.isValid) {
                                _formState.update { it.copy(isBiometricVerified = true) }
                                _uiState.value = ApplyLoanUiState.Idle
                            } else {
                                if (data.isLocked) {
                                    _uiState.value = ApplyLoanUiState.PinLocked("Locked until ${data.lockedUntil}")
                                } else {
                                    _uiState.value = ApplyLoanUiState.PinError("Invalid PIN. ${data.remainingAttempts} attempts remaining.")
                                }
                            }
                        } else {
                            _uiState.value = ApplyLoanUiState.Error(ErrorType.BusinessError("AUTH_ERROR", body?.message ?: "Verification failed"))
                        }
                    } else {
                         _uiState.value = ApplyLoanUiState.Error(ErrorType.ServerError(response.code(), response.message()))
                    }
                } catch (e: Exception) {
                    _uiState.value = ApplyLoanUiState.Error(ErrorType.UnknownError(e.message ?: "Network error"))
                }
            }
        }

        private fun captureLocation(isAuto: Boolean = false) {
            viewModelScope.launch {
                _uiState.value = if (isAuto) ApplyLoanUiState.AutoCapturingLocation else ApplyLoanUiState.CapturingLocation

                when (val result = locationFallbackManager.getLocation()) {
                    is LocationResult.Success -> {
                        _formState.update {
                            it.copy(
                                latitude = result.latitude,
                                longitude = result.longitude,
                            )
                        }
                        _uiState.value = ApplyLoanUiState.Idle
                    }
                    is LocationResult.Error -> {
                        _uiState.value = ApplyLoanUiState.Error(
                            ErrorType.LocationError(result.message)
                        )
                    }
                }
            }
        }


        private fun saveAsDraft(showDialog: Boolean = true) {
            viewModelScope.launch {
                _uiState.value = ApplyLoanUiState.Loading

                try {
                    // Extract values with proper validation
                    val amount = _formState.value.amount.replace(",", "").toLongOrNull()
                    val tenor = _formState.value.tenor.toIntOrNull()
                    val purpose = _formState.value.purpose.ifBlank { null }
                    val downPayment = _formState.value.downPayment.replace(",", "").toLongOrNull()
                    
                    // Step 1: Always save/update basic info first to get/create draftId
                    val basicResult = saveLoanDraftUseCase.saveOrUpdateBasicInfo(
                        draftId = _formState.value.draftId,
                        amount = amount,
                        tenor = tenor,
                        purpose = purpose,
                        downPayment = downPayment,
                        latitude = _formState.value.latitude,
                        longitude = _formState.value.longitude,
                        isBiometricVerified = _formState.value.isBiometricVerified
                    )
                    
                    if (basicResult.isFailure) {
                        _uiState.value = ApplyLoanUiState.Error(
                            ErrorType.UnknownError(basicResult.exceptionOrNull()?.message ?: "Failed to save basic info")
                        )
                        return@launch
                    }
                    
                    val draftId = basicResult.getOrThrow()
                    
                    // Step 2: Save employment info if available (step >= 2)
                    if (_formState.value.currentStep >= 2 || 
                        _formState.value.jobType.isNotBlank() ||
                        _formState.value.companyName.isNotBlank()) {
                        
                        val employmentResult = saveLoanDraftUseCase.updateEmploymentInfo(
                            draftId = draftId,
                            jobType = _formState.value.jobType.ifBlank { null },
                            companyName = _formState.value.companyName.ifBlank { null },
                            jobPosition = _formState.value.jobPosition.ifBlank { null },
                            workDurationMonths = _formState.value.workDurationMonths.toIntOrNull(),
                            workAddress = _formState.value.workAddress.ifBlank { null },
                            officePhoneNumber = _formState.value.officePhoneNumber.ifBlank { null },
                            declaredIncome = _formState.value.declaredIncome.replace(",", "").toLongOrNull(),
                            additionalIncome = _formState.value.additionalIncome.replace(",", "").toLongOrNull(),
                            npwpNumber = _formState.value.npwpNumber.ifBlank { null }
                        )
                        
                        if (employmentResult.isFailure) {
                            // Log but continue - basic info is already saved
                            println("Warning: Failed to save employment info: ${employmentResult.exceptionOrNull()?.message}")
                        }
                    }
                    
                    // Step 3: Save emergency contact if available (step >= 3)
                    if (_formState.value.currentStep >= 3 ||
                        _formState.value.emergencyContactName.isNotBlank() ||
                        _formState.value.emergencyContactPhone.isNotBlank()) {
                        
                        val contactResult = saveLoanDraftUseCase.updateEmergencyContact(
                            draftId = draftId,
                            name = _formState.value.emergencyContactName.ifBlank { null },
                            relation = _formState.value.emergencyContactRelation.ifBlank { null },
                            phone = _formState.value.emergencyContactPhone.ifBlank { null },
                            address = _formState.value.emergencyContactAddress.ifBlank { null }
                        )
                        
                        if (contactResult.isFailure) {
                            println("Warning: Failed to save emergency contact: ${contactResult.exceptionOrNull()?.message}")
                        }
                    }
                    
                    // Step 4: Save bank info if available (step >= 4)
                    if (_formState.value.currentStep >= 4 ||
                        _formState.value.bankName.isNotBlank() ||
                        _formState.value.accountNumber.isNotBlank()) {
                        
                        val bankResult = saveLoanDraftUseCase.updateBankInfo(
                            draftId = draftId,
                            bankName = _formState.value.bankName.ifBlank { null },
                            bankBranch = _formState.value.bankBranch.ifBlank { null },
                            accountNumber = _formState.value.accountNumber.ifBlank { null },
                            accountHolderName = _formState.value.accountHolderName.ifBlank { null }
                        )
                        
                        if (bankResult.isFailure) {
                            println("Warning: Failed to save bank info: ${bankResult.exceptionOrNull()?.message}")
                        }
                    }
                    
                    // Update form state with draft ID
                    _formState.update { it.copy(draftId = draftId) }
                    _uiState.value = ApplyLoanUiState.DraftSaved(draftId, showDialog)
                    
                } catch (e: Exception) {
                    _uiState.value =
                        ApplyLoanUiState.Error(
                            ErrorType.UnknownError(e.message ?: "Unknown error occurred"),
                        )
                }
            }
        }

        private fun handleNextStep() {
            val validationErrors = _formState.value.getValidationErrors()
            if (validationErrors.isNotEmpty()) {
                _uiState.value = ApplyLoanUiState.ValidationError(validationErrors)
                return
            }

            // Don't auto-save draft when clicking "Lanjut" - proceed directly to next step
            // Draft is only saved when explicitly clicking "Save as Draft" button
            _formState.update { it.copy(currentStep = it.currentStep + 1) }
        }

        private fun submitLoan() {
            val validationErrors = _formState.value.getValidationErrors()
            if (validationErrors.isNotEmpty()) {
                _uiState.value = ApplyLoanUiState.ValidationError(validationErrors)
                return
            }

            viewModelScope.launch {
                try {
                    _uiState.value = ApplyLoanUiState.Loading

                    // Check eligibility first
                    val eligibility = validateLoanSubmissionUseCase()
                    if (eligibility.isFailure) {
                        _uiState.value = ApplyLoanUiState.Error(
                            ErrorType.UnknownError("Failed to validate submission: ${eligibility.exceptionOrNull()?.message}")
                        )
                        return@launch
                    }

                    val eligibilityResult = eligibility.getOrThrow()
                    if (!eligibilityResult.isProfileComplete) {
                        _uiState.value = ApplyLoanUiState.ProfileIncomplete(eligibilityResult.missingProfileFields)
                        return@launch
                    }
                    if (!eligibilityResult.isPinSet) {
                        _uiState.value = ApplyLoanUiState.PinNotSet
                        return@launch
                    }

                    // Ensure coordinates are available via fallback if not yet set
                    val (lat, lng) = if (_formState.value.latitude == null || _formState.value.longitude == null) {
                         when(val result = locationFallbackManager.getLocation()) {
                             is LocationResult.Success -> Pair(result.latitude, result.longitude)
                             else -> Pair(-6.2088, 106.8456)
                         }
                    } else {
                        Pair(_formState.value.latitude!!, _formState.value.longitude!!)
                    }
                    
                    // Get customer name from cache without blocking on network
                    val customerName = try {
                        val cachedProfile = getUserProfileUseCase()
                            .first { it !is com.loanfinancial.lofi.core.util.Resource.Loading }
                        when (cachedProfile) {
                            is com.loanfinancial.lofi.core.util.Resource.Success -> cachedProfile.data?.fullName ?: "User"
                            else -> "User"
                        }
                    } catch (e: Exception) {
                        "User"
                    }

                    // Map documents
                    val documents = _formState.value.documents.mapKeys { it.key.name }.mapValues { it.value.filePath ?: "" }
                        .filterValues { it.isNotEmpty() }

                    val loanData = com.loanfinancial.lofi.domain.model.LoanSubmissionData(
                        customerName = customerName,
                        productCode = "CASH_LOAN",
                        productName = "Pinjaman Tunai",
                        interestRate = 0.05,
                        loanAmount = try { _formState.value.amount.replace(",", "").toLong() } catch(e: Exception) { 0L },
                        tenor = try { _formState.value.tenor.toInt() } catch(e: Exception) { 0 },
                        purpose = _formState.value.purpose,
                        downPayment = _formState.value.downPayment.replace(",", "").toLongOrNull(),
                        latitude = lat,
                        longitude = lng,
                        
                        // Employment
                        jobType = _formState.value.jobType,
                        companyName = _formState.value.companyName,
                        jobPosition = _formState.value.jobPosition,
                        workDurationMonths = _formState.value.workDurationMonths.toIntOrNull(),
                        workAddress = _formState.value.workAddress,
                        officePhoneNumber = _formState.value.officePhoneNumber,
                        declaredIncome = _formState.value.declaredIncome.replace(",", "").toLongOrNull(),
                        additionalIncome = _formState.value.additionalIncome.replace(",", "").toLongOrNull(),
                        npwpNumber = _formState.value.npwpNumber,
                        
                        // Contact
                        emergencyContactName = _formState.value.emergencyContactName,
                        emergencyContactRelation = _formState.value.emergencyContactRelation,
                        emergencyContactPhone = _formState.value.emergencyContactPhone,
                        emergencyContactAddress = _formState.value.emergencyContactAddress,
                        
                        // Bank
                        bankName = _formState.value.bankName,
                        bankBranch = _formState.value.bankBranch,
                        accountNumber = _formState.value.accountNumber,
                        accountHolderName = _formState.value.accountHolderName,
                        
                        documentPaths = documents
                    )

                    // Submit based on network availability
                    val result = if (networkManager.isNetworkAvailable()) {
                        // Online: submit directly via API
                        val request = com.loanfinancial.lofi.data.model.dto.CreateLoanRequest(
                            loanAmount = loanData.loanAmount,
                            tenor = loanData.tenor,
                            purpose = loanData.purpose,
                            longitude = loanData.longitude,
                            latitude = loanData.latitude,
                            downPayment = loanData.downPayment,
                            jobType = loanData.jobType,
                            companyName = loanData.companyName,
                            jobPosition = loanData.jobPosition,
                            workDurationMonths = loanData.workDurationMonths,
                            workAddress = loanData.workAddress,
                            officePhoneNumber = loanData.officePhoneNumber,
                            declaredIncome = loanData.declaredIncome,
                            additionalIncome = loanData.additionalIncome,
                            npwpNumber = loanData.npwpNumber,
                            emergencyContactName = loanData.emergencyContactName,
                            emergencyContactRelation = loanData.emergencyContactRelation,
                            emergencyContactPhone = loanData.emergencyContactPhone,
                            emergencyContactAddress = loanData.emergencyContactAddress,
                            bankName = loanData.bankName,
                            bankBranch = loanData.bankBranch,
                            accountNumber = loanData.accountNumber,
                            accountHolderName = loanData.accountHolderName
                        )
                        
                        // Submit directly via repository
                        when (val submissionResult = loanRepository.createLoan(request).first { it !is com.loanfinancial.lofi.core.util.Resource.Loading }) {
                            is com.loanfinancial.lofi.core.util.Resource.Success -> {
                                val loanId = submissionResult.data?.id
                                if (loanId != null) {
                                    // Queue documents for upload if any
                                    if (documents.isNotEmpty()) {
                                        documents.forEach { (docType, filePath) ->
                                            try {
                                                val type = com.loanfinancial.lofi.core.media.DocumentType.valueOf(docType)
                                                documentRepository.queueDocumentUpload(loanId, filePath, type)
                                            } catch (e: Exception) {
                                                // Log but continue
                                                println("Failed to queue document $docType: ${e.message}")
                                            }
                                        }
                                        // Schedule document upload worker
                                        com.loanfinancial.lofi.worker.DocumentUploadWorker.scheduleForDraft(
                                            context,
                                            loanId
                                        )
                                    }
                                    Result.success(loanId)
                                } else {
                                    Result.failure(Exception("Loan ID is null"))
                                }
                            }
                            is com.loanfinancial.lofi.core.util.Resource.Error -> {
                                // Check if network error for fallback
                                val msg = submissionResult.message ?: ""
                                if (msg.contains("connect", true) || 
                                    msg.contains("timeout", true) ||
                                    msg.contains("network", true) ||
                                    msg.contains("unable to resolve host", true)) {
                                    // Network error - fallback to offline
                                    loanSubmissionManager.submitLoanOffline(loanData)
                                } else {
                                    Result.failure(Exception(submissionResult.message ?: "Submission failed"))
                                }
                            }
                            else -> Result.failure(Exception("Unknown error"))
                        }
                    } else {
                        // Offline: use offline submission manager
                        loanSubmissionManager.submitLoanOffline(loanData)
                    }
                    
                    if (result.isSuccess) {
                        _uiState.value = ApplyLoanUiState.Success(result.getOrNull() ?: "", isDraft = false)
                    } else {
                        _uiState.value = ApplyLoanUiState.Error(
                            ErrorType.UnknownError(result.exceptionOrNull()?.message ?: "Submission Failed")
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value =
                        ApplyLoanUiState.Error(
                            ErrorType.UnknownError(e.message ?: "Unknown error occurred"),
                        )
                }
            }
        }

        private fun proceedToDocumentUpload() {
            val validationErrors = _formState.value.getValidationErrors()
            if (validationErrors.isNotEmpty()) {
                _uiState.value = ApplyLoanUiState.ValidationError(validationErrors)
                return
            }

            val draftId = _formState.value.draftId
            
            if (!draftId.isNullOrEmpty()) {
                // Draft already exists, proceed to document upload
                _uiState.value = ApplyLoanUiState.ReadyForDocumentUpload(draftId)
            } else {
                // Auto-save draft if it doesn't exist
                autoSaveDraftForDocumentUpload()
            }
        }

        private fun autoSaveDraftForDocumentUpload() {
            viewModelScope.launch {
                _uiState.value = ApplyLoanUiState.Loading
                
                try {
                    val currentStep = _formState.value.currentStep
                    val existingDraftId = _formState.value.draftId
                    
                    // Prepare document paths map
                    val documentPaths = _formState.value.documents
                        .mapKeys { it.key.name }
                        .mapValues { it.value.filePath ?: "" }
                        .filterValues { it.isNotEmpty() }
                    
                    // First save basic info to create/update the draft
                    val basicInfoResult = saveLoanDraftUseCase.saveOrUpdateBasicInfo(
                        draftId = existingDraftId,
                        amount = try { _formState.value.amount.replace(",", "").toLongOrNull() } catch(e: Exception) { null },
                        tenor = try { _formState.value.tenor.toIntOrNull() } catch(e: Exception) { null },
                        purpose = _formState.value.purpose.ifBlank { null },
                        downPayment = try { _formState.value.downPayment.replace(",", "").toLongOrNull() } catch(e: Exception) { null },
                        latitude = _formState.value.latitude,
                        longitude = _formState.value.longitude,
                        isBiometricVerified = _formState.value.isBiometricVerified
                    )
                    
                    if (basicInfoResult.isFailure) {
                        _uiState.value = ApplyLoanUiState.Error(
                            ErrorType.UnknownError(basicInfoResult.exceptionOrNull()?.message ?: "Failed to save draft")
                        )
                        return@launch
                    }
                    
                    val newDraftId = basicInfoResult.getOrThrow()
                    
                    // Now save employment info if user has filled step 2
                    if (currentStep >= 2 || _formState.value.jobType.isNotBlank()) {
                        saveLoanDraftUseCase.updateEmploymentInfo(
                            draftId = newDraftId,
                            jobType = _formState.value.jobType.ifBlank { null },
                            companyName = _formState.value.companyName.ifBlank { null },
                            jobPosition = _formState.value.jobPosition.ifBlank { null },
                            workDurationMonths = _formState.value.workDurationMonths.toIntOrNull(),
                            workAddress = _formState.value.workAddress.ifBlank { null },
                            officePhoneNumber = _formState.value.officePhoneNumber.ifBlank { null },
                            declaredIncome = try { _formState.value.declaredIncome.replace(",", "").toLongOrNull() } catch(e: Exception) { null },
                            additionalIncome = try { _formState.value.additionalIncome.replace(",", "").toLongOrNull() } catch(e: Exception) { null },
                            npwpNumber = _formState.value.npwpNumber.ifBlank { null }
                        )
                    }
                    
                    // Now save emergency contact if user has filled step 3
                    if (currentStep >= 3 || _formState.value.emergencyContactName.isNotBlank()) {
                        saveLoanDraftUseCase.updateEmergencyContact(
                            draftId = newDraftId,
                            name = _formState.value.emergencyContactName.ifBlank { null },
                            relation = _formState.value.emergencyContactRelation.ifBlank { null },
                            phone = _formState.value.emergencyContactPhone.ifBlank { null },
                            address = _formState.value.emergencyContactAddress.ifBlank { null }
                        )
                    }
                    
                    // Now save bank info if user has filled step 4
                    if (currentStep >= 4 || _formState.value.bankName.isNotBlank()) {
                        saveLoanDraftUseCase.updateBankInfo(
                            draftId = newDraftId,
                            bankName = _formState.value.bankName.ifBlank { null },
                            bankBranch = _formState.value.bankBranch.ifBlank { null },
                            accountNumber = _formState.value.accountNumber.ifBlank { null },
                            accountHolderName = _formState.value.accountHolderName.ifBlank { null }
                        )
                    }
                    
                    // Update document paths if any
                    if (documentPaths.isNotEmpty()) {
                        saveLoanDraftUseCase.updateDocumentPaths(newDraftId, documentPaths)
                    }
                    
                    // Update form state with the new draft ID
                    _formState.update { it.copy(draftId = newDraftId) }
                    
                    // Proceed to document upload
                    _uiState.value = ApplyLoanUiState.ReadyForDocumentUpload(newDraftId)
                    
                } catch (e: Exception) {
                    _uiState.value = ApplyLoanUiState.Error(
                        ErrorType.UnknownError(e.message ?: "Failed to save draft")
                    )
                }
            }
        }

        private fun resetState() {
            _uiState.value = ApplyLoanUiState.Idle
            _formState.value = ApplyLoanFormState()
        }
    }
