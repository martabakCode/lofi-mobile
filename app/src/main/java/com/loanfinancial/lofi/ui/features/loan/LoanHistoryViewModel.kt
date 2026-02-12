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
import kotlinx.coroutines.launch
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
        private val loanSubmissionManager: com.loanfinancial.lofi.domain.manager.LoanSubmissionManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoanHistoryUiState())
        val uiState: StateFlow<LoanHistoryUiState> = _uiState.asStateFlow()

        private var currentPendingLoans: List<Loan> = emptyList()
        private var currentRemoteLoans: List<Loan> = emptyList()

        init {
            observePendingSubmissions()
            loadHistory()
        }

        private fun observePendingSubmissions() {
            loanSubmissionManager
                .getPendingSubmissions()
                .onEach { submissions ->
                    currentPendingLoans = submissions.map { it.toLoan() }
                    updateUiState()
                }.launchIn(viewModelScope)
        }

        private fun updateUiState() {
            _uiState.update { it.copy(loans = currentPendingLoans + currentRemoteLoans) }
        }

        private fun com.loanfinancial.lofi.domain.model.PendingLoanSubmission.toLoan(): Loan =
            Loan(
                id = loanId,
                customerName = "", // Not available in pending submission properly, or needed?
                // Wait, PendingLoanSubmission doesn't have customerName. PendingLoanSubmissionEntity has.
                // I should probably add customerName to PendingLoanSubmission domain model or just ignore it for list item.
                product =
                    com.loanfinancial.lofi.domain.model.Product(
                        productCode = "", // Not in domain model
                        productName = productName,
                        interestRate = 0.0, // Not in domain model
                    ),
                loanAmount = loanAmount,
                tenor = tenor,
                loanStatus = status.name,
                currentStage = "SUBMISSION",
                submittedAt = null,
                reviewedAt = null,
                approvedAt = null,
                rejectedAt = null,
                disbursedAt = null,
                loanStatusDisplay =
                    when (status) {
                        com.loanfinancial.lofi.domain.model.PendingSubmissionStatus.PENDING -> "Menunggu Jaringan"
                        com.loanfinancial.lofi.domain.model.PendingSubmissionStatus.SUBMITTING -> "Mengirim..."
                        com.loanfinancial.lofi.domain.model.PendingSubmissionStatus.SUCCESS -> "Terkirim"
                        com.loanfinancial.lofi.domain.model.PendingSubmissionStatus.FAILED -> "Gagal"
                        com.loanfinancial.lofi.domain.model.PendingSubmissionStatus.CANCELLED -> "Dibatalkan"
                    },
                slaDurationHours = null,
                pendingStatus = status,
                failureReason = failureReason,
            )

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
                                currentRemoteLoans = if (state.page == 0) newLoans else currentRemoteLoans + newLoans
                                val mergedLoans = currentPendingLoans + currentRemoteLoans
                                state.copy(
                                    loans = mergedLoans,
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
            viewModelScope.launch {
                loanSubmissionManager.triggerPendingSubmissions()
            }
            loadHistory(isRefreshing = true)
        }

        fun loadMore() {
            if (!_uiState.value.isLoading && !_uiState.value.isLoadingMore && _uiState.value.hasMore) {
                loadHistory()
            }
        }

        fun retrySubmission(loanId: String) {
            viewModelScope.launch {
                loanSubmissionManager.retrySubmission(loanId)
            }
        }

        fun cancelSubmission(loanId: String) {
            viewModelScope.launch {
                loanSubmissionManager.cancelSubmission(loanId)
            }
        }
    }
