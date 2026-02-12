package com.loanfinancial.lofi.ui.features.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.CreateLoanRequest
import com.loanfinancial.lofi.domain.manager.LoanSubmissionManager
import com.loanfinancial.lofi.domain.model.LoanSubmissionData
import com.loanfinancial.lofi.domain.usecase.loan.CreateLoanUseCase
import com.loanfinancial.lofi.domain.usecase.loan.SubmitLoanUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

/**
 * UI State for Loan TnC Screen
 */
data class LoanTnCUiState(
    val isAgreementChecked: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val createdLoanId: String? = null,
)

/**
 * Data class containing loan form data passed from ApplyLoanViewModel
 */
data class LoanFormData(
    val amount: String = "",
    val tenor: String = "",
    val purpose: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val documents: Map<String, String> = emptyMap(),
)

@HiltViewModel
class LoanTnCViewModel
    @Inject
    constructor(
        private val createLoanUseCase: CreateLoanUseCase,
        private val submitLoanUseCase: SubmitLoanUseCase,
        private val loanSubmissionManager: LoanSubmissionManager,
        private val getUserProfileUseCase: GetUserProfileUseCase,
    ) : ViewModel() {
        /**
         * Round coordinate to 6 decimal places to avoid DB arithmetic overflow.
         * 6 decimal places provides ~11cm precision which is more than enough for GPS.
         */
        private fun roundCoordinate(value: Double): Double = round(value * 1_000_000) / 1_000_000

        private val _uiState = MutableStateFlow(LoanTnCUiState())
        val uiState: StateFlow<LoanTnCUiState> = _uiState.asStateFlow()

        // Store loan form data temporarily
        private var loanFormData: LoanFormData? = null

        /**
         * Set loan form data from previous screen
         */
        fun setLoanFormData(data: LoanFormData) {
            loanFormData = data
        }

        /**
         * Handle agreement checkbox change
         */
        fun onAgreementCheckedChange(checked: Boolean) {
            _uiState.update { it.copy(isAgreementChecked = checked) }
        }

        /**
         * Handle submit button click
         * Flow: Create Loan (POST /loans) → Submit Loan (POST /loans/{id}/submit)
         * Fallback: Submit Loan Offline (Save to DB, Schedule Worker)
         */
        fun onSubmitClicked() {
            if (!_uiState.value.isAgreementChecked) {
                _uiState.update { it.copy(errorMessage = "Anda harus menyetujui syarat dan ketentuan untuk melanjutkan") }
                return
            }

            val formData = loanFormData
            if (formData == null) {
                _uiState.update { it.copy(errorMessage = "Data pengajuan tidak ditemukan") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isSubmitting = true) }

                try {
                    // Step 1: Create Loan (POST /loans)
                    val createRequest =
                        CreateLoanRequest(
                            loanAmount = formData.amount.replace(",", "").toLongOrNull() ?: 0L,
                            tenor = formData.tenor.toIntOrNull() ?: 0,
                            longitude = roundCoordinate(formData.longitude ?: 0.0),
                            latitude = roundCoordinate(formData.latitude ?: 0.0),
                            purpose = formData.purpose,
                        )

                    val createResult =
                        createLoanUseCase(createRequest)
                            .first { it !is Resource.Loading }

                    when (createResult) {
                        is Resource.Success -> {
                            val loan = createResult.data
                            val loanId = loan?.id
                            if (loanId != null) {
                                // Step 2: Submit Loan (POST /loans/{id}/submit)
                                // If ApplyLoanUseCase (POST /loans) already returns SUBMITTED status, skip explicit submit
                                if (loan.loanStatus.equals("SUBMITTED", ignoreCase = true)) {
                                    _uiState.update {
                                        it.copy(
                                            isSubmitting = false,
                                            isSuccess = true,
                                            createdLoanId = loanId,
                                        )
                                    }
                                } else {
                                    submitLoan(loanId)
                                }
                            } else {
                                // Fallback to offline if ID is null (unlikely but possible)
                                submitLoanOffline(formData)
                            }
                        }
                        is Resource.Error -> {
                            // Check if it's a network error (e.g. timeout, connection failed)
                            if (isNetworkError(createResult.message)) {
                                // Network error - try offline submission
                                submitLoanOffline(formData)
                            } else {
                                // Business logic error - don't fallback to offline
                                _uiState.update {
                                    it.copy(
                                        isSubmitting = false,
                                        errorMessage = createResult.message ?: "Gagal membuat pengajuan pinjaman",
                                    )
                                }
                            }
                        }
                        else -> {
                            // Loading or other state - don't fallback automatically
                            _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    errorMessage = "Terjadi kesalahan. Silakan coba lagi.",
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Only fallback to offline for network-related exceptions
                    if (isNetworkError(e.message)) {
                        submitLoanOffline(formData)
                    } else {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = e.message ?: "Terjadi kesalahan",
                            )
                        }
                    }
                }
            }
        }

        private fun isNetworkError(message: String?): Boolean =
            message?.contains("connect", true) == true ||
                message?.contains("timeout", true) == true ||
                message?.contains("network", true) == true ||
                message?.contains("Unknown Error", true) == true ||
                message == null

        private suspend fun submitLoanOffline(formData: LoanFormData) {
            try {
                // Get customer name from cache fallback
                val customerName =
                    try {
                        val cachedProfile =
                            getUserProfileUseCase()
                                .first { it !is Resource.Loading }
                        when (cachedProfile) {
                            is Resource.Success -> cachedProfile.data?.fullName ?: "User"
                            else -> "User"
                        }
                    } catch (e: Exception) {
                        "User"
                    }

                // Validate required data before offline submission
                val loanAmount =
                    try {
                        formData.amount.replace(",", "").toLong()
                    } catch (e: Exception) {
                        0L
                    }
                val loanTenor =
                    try {
                        formData.tenor.toInt()
                    } catch (e: Exception) {
                        0
                    }

                if (loanAmount <= 0) {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = "Jumlah pinjaman tidak valid",
                        )
                    }
                    return
                }

                if (loanTenor <= 0) {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = "Tenor tidak valid",
                        )
                    }
                    return
                }

                val loanData =
                    LoanSubmissionData(
                        customerName = customerName,
                        productCode = "CASH_LOAN",
                        productName = "Pinjaman Tunai",
                        interestRate = 0.05,
                        loanAmount = loanAmount,
                        tenor = loanTenor,
                        purpose = formData.purpose,
                        latitude = roundCoordinate(formData.latitude ?: 0.0),
                        longitude = roundCoordinate(formData.longitude ?: 0.0),
                        documentPaths = formData.documents,
                    )

                val result = loanSubmissionManager.submitLoanOffline(loanData)

                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSuccess = true,
                            createdLoanId = result.getOrNull(),
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Submission Failed",
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.message ?: "Offline submission failed",
                    )
                }
            }
        }

        /**
         * Submit the created loan
         */
        private suspend fun submitLoan(loanId: String) {
            val submitResult =
                submitLoanUseCase(loanId)
                    .first { it !is Resource.Loading }

            when (submitResult) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSuccess = true,
                            createdLoanId = loanId,
                        )
                    }
                }
                is Resource.Error -> {
                    // Also try offline fallback if simple submit fails due to network?
                    // But here the loan is already created on server.
                    // If submit fails, the status on server might be DRAFT or similar.
                    // We probably should just show error here, as "offline submission" logic creates a NEW pending submission in local DB.
                    // If we use offline submission here, we might duplicate the loan (one created on server, one pending local).
                    // So just error is safer, or better retry logic.

                    if (isNetworkError(submitResult.message)) {
                        // Ideally we should mark the EXISTING loanId as pending sync?
                        // But for now, let's just show error or we risk dupes.
                        // User asked to "make it online first but if there is internet post it" -> implied fallback.
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = submitResult.message ?: "Gagal submit pengajuan pinjaman",
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = submitResult.message ?: "Gagal submit pengajuan pinjaman",
                            )
                        }
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = "Terjadi kesalahan saat submit pengajuan",
                        )
                    }
                }
            }
        }

        /**
         * Dismiss error dialog
         */
        fun onDismissError() {
            _uiState.update { it.copy(errorMessage = null) }
        }

        /**
         * Dismiss success dialog and reset state
         */
        fun onDismissSuccess() {
            _uiState.update {
                LoanTnCUiState()
            }
        }

        /**
         * Reset the view model state
         */
        fun resetState() {
            _uiState.value = LoanTnCUiState()
            loanFormData = null
        }
    }
