package com.loanfinancial.lofi.ui.features.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.usecase.loan.GetLoanDetailUseCase
import com.loanfinancial.lofi.domain.usecase.loan.SubmitLoanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoanDetailUiState {
    data object Loading : LoanDetailUiState()

    data class Success(
        val loan: Loan,
    ) : LoanDetailUiState()

    data class Error(
        val message: String,
    ) : LoanDetailUiState()
}

@HiltViewModel
class LoanDetailViewModel
    @Inject
    constructor(
        private val getLoanDetailUseCase: GetLoanDetailUseCase,
        private val submitLoanUseCase: SubmitLoanUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<LoanDetailUiState>(LoanDetailUiState.Loading)
        val uiState: StateFlow<LoanDetailUiState> = _uiState.asStateFlow()

        private val _submitState = MutableStateFlow<SubmitLoanState>(SubmitLoanState.Idle)
        val submitState: StateFlow<SubmitLoanState> = _submitState.asStateFlow()

        fun loadLoan(id: String) {
            viewModelScope.launch {
                getLoanDetailUseCase(id).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = LoanDetailUiState.Loading
                        }
                        is Resource.Success -> {
                            result.data?.let { loan ->
                                _uiState.value = LoanDetailUiState.Success(loan)
                            } ?: run {
                                _uiState.value = LoanDetailUiState.Error("Loan data is null")
                            }
                        }
                        is Resource.Error -> {
                            _uiState.value = LoanDetailUiState.Error(result.message ?: "Unknown error")
                        }
                    }
                }
            }
        }

        fun submitLoan(id: String) {
            viewModelScope.launch {
                submitLoanUseCase(id).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _submitState.value = SubmitLoanState.Loading
                        }
                        is Resource.Success -> {
                            result.data?.let { loan ->
                                _submitState.value = SubmitLoanState.Success(loan)
                                // Refresh loan detail after successful submission
                                _uiState.value = LoanDetailUiState.Success(loan)
                            } ?: run {
                                _submitState.value = SubmitLoanState.Error("Loan data is null")
                            }
                        }
                        is Resource.Error -> {
                            _submitState.value = SubmitLoanState.Error(result.message ?: "Unknown error")
                        }
                    }
                }
            }
        }

        fun resetSubmitState() {
            _submitState.value = SubmitLoanState.Idle
        }
    }

sealed class SubmitLoanState {
    data object Idle : SubmitLoanState()

    data object Loading : SubmitLoanState()

    data class Success(
        val loan: Loan,
    ) : SubmitLoanState()

    data class Error(
        val message: String,
    ) : SubmitLoanState()
}
