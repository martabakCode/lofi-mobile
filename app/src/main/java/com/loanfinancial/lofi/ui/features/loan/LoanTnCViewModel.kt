package com.loanfinancial.lofi.ui.features.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Loan TnC Screen
 */
data class LoanTnCUiState(
    val isAgreementChecked: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class LoanTnCViewModel
    @Inject
    constructor() : ViewModel() {
        private val _uiState = MutableStateFlow(LoanTnCUiState())
        val uiState: StateFlow<LoanTnCUiState> = _uiState.asStateFlow()

        /**
         * Handle agreement checkbox change
         */
        fun onAgreementCheckedChange(checked: Boolean) {
            _uiState.update { it.copy(isAgreementChecked = checked) }
        }

        /**
         * Handle submit button click
         */
        fun onSubmitClicked() {
            if (!_uiState.value.isAgreementChecked) {
                _uiState.update { it.copy(errorMessage = "Anda harus menyetujui syarat dan ketentuan untuk melanjutkan") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isSubmitting = true) }

                try {
                    // Simulate API call for loan submission
                    // In real implementation, this should call the actual submission use case
                    delay(2000)

                    // Simulate success
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSuccess = true,
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = e.message ?: "Terjadi kesalahan saat mengirim pengajuan",
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
        }
    }
