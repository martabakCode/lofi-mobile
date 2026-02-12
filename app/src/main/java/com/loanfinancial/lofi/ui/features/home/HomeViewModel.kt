package com.loanfinancial.lofi.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.data.model.dto.AvailableProductDto
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.usecase.GetMyLoansUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetAvailableProductUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetProductsUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val loans: List<Loan> = emptyList(),
    val userProfile: UserUpdateData? = null,
    val products: List<com.loanfinancial.lofi.data.model.dto.ProductDto> = emptyList(),
    val availableProduct: AvailableProductDto? = null,
    val error: String? = null,
    val hasCompletedFirstSession: Boolean = false,
    val isProfileCompleted: Boolean = false,
)

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val getMyLoansUseCase: GetMyLoansUseCase,
        private val getUserProfileUseCase: GetUserProfileUseCase,
        private val getProductsUseCase: GetProductsUseCase,
        private val getAvailableProductUseCase: GetAvailableProductUseCase,
        private val loanSubmissionManager: com.loanfinancial.lofi.domain.manager.LoanSubmissionManager,
        private val dataStoreManager: DataStoreManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        private var loansJob: Job? = null
        private var profileJob: Job? = null
        private var productsJob: Job? = null
        private var availableProductJob: Job? = null

        init {
            markFirstSessionComplete()
            fetchLoans()
            fetchUserProfile()
            fetchProducts()
            fetchAvailableProduct()
        }

        private fun markFirstSessionComplete() {
            viewModelScope.launch {
                val hasCompleted = dataStoreManager.hasCompletedFirstSessionFlow.first()
                val isProfileCompleted = dataStoreManager.isProfileCompletedFlow.first()
                _uiState.value = _uiState.value.copy(
                    hasCompletedFirstSession = true,
                    isProfileCompleted = isProfileCompleted
                )
                if (!hasCompleted) {
                    dataStoreManager.setHasCompletedFirstSession(true)
                }
            }
        }

        fun refreshLoans() {
            viewModelScope.launch {
                loanSubmissionManager.triggerPendingSubmissions()
            }
            fetchLoans(isRefreshing = true)
            fetchUserProfile()
            fetchProducts()
            fetchAvailableProduct()
        }

        private fun fetchUserProfile() {
            profileJob?.cancel()
            profileJob = getUserProfileUseCase()
                .onEach { result ->
                    when (result) {
                        is Resource.Success<UserUpdateData> -> {
                            _uiState.value = _uiState.value.copy(userProfile = result.data)
                        }
                        is Resource.Error -> {
                            // Optionally handle error
                        }
                        is Resource.Loading -> {
                            // handled by pull refresh or initial load
                        }
                    }
                }.launchIn(viewModelScope)
        }

        fun fetchLoans(isRefreshing: Boolean = false) {
            loansJob?.cancel()
            loansJob = getMyLoansUseCase()
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            if (isRefreshing) {
                                _uiState.value = _uiState.value.copy(isRefreshing = true)
                            } else {
                                _uiState.value = _uiState.value.copy(isLoading = true)
                            }
                        }
                        is Resource.Success<List<Loan>> -> {
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

        private fun fetchProducts() {
            productsJob?.cancel()
            productsJob = getProductsUseCase()
                .onEach { result ->
                    when (result) {
                        is Resource.Success<List<com.loanfinancial.lofi.data.model.dto.ProductDto>> -> {
                            _uiState.update { it.copy(products = result.data) }
                        }
                        else -> {}
                    }
                }.launchIn(viewModelScope)
        }

        private fun fetchAvailableProduct() {
            availableProductJob?.cancel()
            availableProductJob = getAvailableProductUseCase()
                .onEach { result ->
                    when (result) {
                        is Resource.Success<AvailableProductDto> -> {
                            _uiState.update { it.copy(availableProduct = result.data) }
                        }
                        is Resource.Error -> {
                            // Optionally handle error
                        }
                        is Resource.Loading -> {
                            // handled by pull refresh or initial load
                        }
                    }
                }.launchIn(viewModelScope)
        }
    }
