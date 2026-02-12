package com.loanfinancial.lofi.ui.features.loan.draft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.domain.model.LoanDraft
import com.loanfinancial.lofi.domain.usecase.loan.DeleteLoanDraftUseCase
import com.loanfinancial.lofi.domain.usecase.loan.GetAllLoanDraftsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DraftListUiState(
    val drafts: List<LoanDraft> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val deletingDraftId: String? = null,
)

@HiltViewModel
class DraftListViewModel @Inject constructor(
    private val getAllLoanDraftsUseCase: GetAllLoanDraftsUseCase,
    private val deleteLoanDraftUseCase: DeleteLoanDraftUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DraftListUiState())
    val uiState: StateFlow<DraftListUiState> = _uiState.asStateFlow()

    init {
        loadDrafts()
    }

    private fun loadDrafts() {
        _uiState.update { it.copy(isLoading = true) }
        getAllLoanDraftsUseCase()
            .onEach { drafts ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        drafts = drafts.sortedByDescending { draft -> draft.updatedAt },
                        error = null,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun deleteDraft(draftId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deletingDraftId = draftId) }
            try {
                deleteLoanDraftUseCase(draftId)
                _uiState.update { it.copy(isDeleting = false, deletingDraftId = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        deletingDraftId = null,
                        error = e.message ?: "Failed to delete draft"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
