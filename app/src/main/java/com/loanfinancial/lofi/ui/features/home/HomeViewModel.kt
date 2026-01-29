package com.loanfinancial.lofi.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.usecase.GetMyLoansUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val loans: List<Loan> = emptyList(),
    val userProfile: UserUpdateData? = null,
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val getMyLoansUseCase: GetMyLoansUseCase,
        private val getUserProfileUseCase: GetUserProfileUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        init {
            fetchLoans()
            fetchUserProfile()
        }

        fun refreshLoans() {
            fetchLoans(isRefreshing = true)
            fetchUserProfile()
        }

        private fun fetchUserProfile() {
            getUserProfileUseCase()
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(userProfile = result.data)
                        }
                        is Resource.Error -> {
                            // Optionally handle error, e.g. show toast or log
                        }
                        is Resource.Loading -> {
                            // handled by pull refresh or initial load if needed
                        }
                    }
                }.launchIn(viewModelScope)
        }

        fun fetchLoans(isRefreshing: Boolean = false) {
            getMyLoansUseCase()
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            if (isRefreshing) {
                                _uiState.value = _uiState.value.copy(isRefreshing = true)
                            } else {
                                _uiState.value = _uiState.value.copy(isLoading = true)
                            }
                        }
                        is Resource.Success -> {
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    loans = result.data,
                                    error = null,
                                )
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    error = result.message,
                                )
                        }
                    }
                }.launchIn(viewModelScope)
        }
    }
