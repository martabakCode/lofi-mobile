package com.loanfinancial.lofi.ui.features.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.ProductDto
import com.loanfinancial.lofi.domain.usecase.user.GetProductsUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoanSimulationUiState(
    val loanAmount: Double = 0.0,
    val durationMonths: Int = 0,
    val selectedProduct: ProductDto? = null,
    val estimatedPayment: Double? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val minAmount: Double = 0.0,
    val maxAmount: Double = 100_000_000.0,
    val minTenor: Int = 1,
    val maxTenor: Int = 60,
)

@HiltViewModel
class LoanSimulationViewModel
    @Inject
    constructor(
        private val getProductsUseCase: GetProductsUseCase,
        private val getUserProfileUseCase: GetUserProfileUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoanSimulationUiState())
        val uiState: StateFlow<LoanSimulationUiState> = _uiState.asStateFlow()

        init {
            loadData()
        }

        private fun loadData() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                // 1. Try to get user's assigned product first
                getUserProfileUseCase().collect { result ->
                    if (result is Resource.Success) {
                        val userProduct = result.data?.product
                        if (userProduct != null) {
                            setProduct(userProduct)
                            _uiState.update { it.copy(isLoading = false) }
                        } else {
                            // 2. Fetch all products to find the "lowest" one
                            loadLowestProduct()
                        }
                    } else if (result is Resource.Error) {
                        loadLowestProduct()
                    }
                }
            }
        }

        private suspend fun loadLowestProduct() {
            getProductsUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val products = result.data ?: emptyList()
                        // "Lowest" defined as min maximum loan amount
                        val lowest = products.minByOrNull { it.maxLoanAmount ?: Double.MAX_VALUE }
                        if (lowest != null) {
                            setProduct(lowest)
                        }
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                    is Resource.Loading -> {}
                }
            }
        }

        private fun setProduct(product: ProductDto) {
            _uiState.update { state ->
                state.copy(
                    selectedProduct = product,
                    loanAmount = product.minLoanAmount ?: 1_000_000.0,
                    durationMonths = product.minTenor ?: 12,
                    minAmount = product.minLoanAmount ?: 1_000_000.0,
                    maxAmount = product.maxLoanAmount ?: 10_000_000.0,
                    minTenor = product.minTenor ?: 6,
                    maxTenor = product.maxTenor ?: 36,
                )
            }
            calculatePayment()
        }

        fun onLoanAmountChange(amount: Double) {
            _uiState.update { it.copy(loanAmount = amount) }
            calculatePayment()
        }

        fun onDurationChange(duration: Int) {
            _uiState.update { it.copy(durationMonths = duration) }
            calculatePayment()
        }

        private fun calculatePayment() {
            val state = _uiState.value
            val product = state.selectedProduct ?: return
            val amount = state.loanAmount
            val months = state.durationMonths
            if (months <= 0) return

            val annualRate = product.interestRate
            val monthlyRate = annualRate / 12 / 100 // Convert percentage to decimal and then to monthly

            val payment =
                if (monthlyRate > 0) {
                    amount * (monthlyRate * Math.pow(1 + monthlyRate, months.toDouble())) /
                        (Math.pow(1 + monthlyRate, months.toDouble()) - 1)
                } else {
                    amount / months
                }

            _uiState.update { it.copy(estimatedPayment = payment) }
        }
    }
