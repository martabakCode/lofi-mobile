package com.loanfinancial.lofi.ui.features.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.usecase.GetMyLoansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LoanHistoryUiState(
    val loans: List<Loan> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val page: Int = 0,
    val hasMore: Boolean = true,
    val isLoadingMore: Boolean = false,
)

@HiltViewModel
class LoanHistoryViewModel
    @Inject
    constructor(
        private val getMyLoansUseCase: GetMyLoansUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoanHistoryUiState())
        val uiState: StateFlow<LoanHistoryUiState> = _uiState.asStateFlow()

        init {
            loadHistory()
        }

        fun loadHistory(isRefreshing: Boolean = false) {
            if (isRefreshing) {
                _uiState.update { it.copy(page = 0, hasMore = true, isRefreshing = true) }
            } else if (_uiState.value.page == 0) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            getMyLoansUseCase(page = _uiState.value.page, size = 10)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            // Handled above
                        }
                        is Resource.Success -> {
                            val newLoans = result.data
                            _uiState.update { state ->
                                state.copy(
                                    loans = if (state.page == 0) newLoans else state.loans + newLoans,
                                    isLoading = false,
                                    isRefreshing = false,
                                    isLoadingMore = false,
                                    hasMore = newLoans.isNotEmpty(),
                                    error = null,
                                    page = state.page + 1,
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    isLoadingMore = false,
                                    error = result.message,
                                )
                            }
                        }
                    }
                }.launchIn(viewModelScope)
        }

        fun refresh() {
            loadHistory(isRefreshing = true)
        }

        fun loadMore() {
            if (!_uiState.value.isLoading && !_uiState.value.isLoadingMore && _uiState.value.hasMore) {
                loadHistory()
            }
        }
    }
