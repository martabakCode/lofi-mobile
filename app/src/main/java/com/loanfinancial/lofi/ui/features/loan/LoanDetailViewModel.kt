package com.loanfinancial.lofi.ui.features.loan

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoanDetailUiState(
    val id: String = "",
    val amount: String = "",
    val status: String = "",
    val dueDate: String = "",
    val type: String = "",
)

class LoanDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoanDetailUiState())
    val uiState: StateFlow<LoanDetailUiState> = _uiState.asStateFlow()

    fun loadLoan(id: String) {
        // Mock data fetch
        _uiState.value =
            LoanDetailUiState(
                id = id,
                amount = "Rp 5.000.000",
                status = "Active",
                dueDate = "25 Feb 2026",
                type = "Personal Loan",
            )
    }
}
