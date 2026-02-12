package com.loanfinancial.lofi.ui.features.auth.google

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.domain.usecase.auth.ChangeGooglePinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangeGooglePinUiState(
    val oldPin: String = "",
    val newPin: String = "",
    val confirmPin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    // 0 = old pin, 1 = new pin, 2 = confirm pin
    val step: Int = 0,
)

@HiltViewModel
class ChangeGooglePinViewModel @Inject constructor(
    private val changeGooglePinUseCase: ChangeGooglePinUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChangeGooglePinUiState())
    val uiState: StateFlow<ChangeGooglePinUiState> = _uiState.asStateFlow()
    
    fun updatePin(newValue: String) {
        if (newValue.length > 6) return
        
        when (_uiState.value.step) {
            0 -> {
                _uiState.update { it.copy(oldPin = newValue, error = null) }
                if (newValue.length == 6) {
                    _uiState.update { it.copy(step = 1) }
                }
            }
            1 -> {
                _uiState.update { it.copy(newPin = newValue, error = null) }
                if (newValue.length == 6) {
                    _uiState.update { it.copy(step = 2) }
                }
            }
            2 -> {
                _uiState.update { it.copy(confirmPin = newValue, error = null) }
                if (newValue.length == 6) {
                    submitPin()
                }
            }
        }
    }

    fun onBackClick() {
        when (_uiState.value.step) {
            1 -> _uiState.update { it.copy(step = 0, newPin = "") }
            2 -> _uiState.update { it.copy(step = 1, confirmPin = "") }
        }
    }
    
    private fun submitPin() {
        val state = _uiState.value
        if (state.newPin != state.confirmPin) {
            _uiState.update { 
                it.copy(
                    error = "New PIN confirmation does not match",
                    confirmPin = "",
                    step = 1,
                    newPin = ""
                ) 
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            changeGooglePinUseCase(state.oldPin, state.newPin)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to change PIN",
                            oldPin = if (error.message?.contains("mismatch", ignoreCase = true) == true || 
                                         error.message?.contains("invalid", ignoreCase = true) == true) "" else state.oldPin,
                            step = if (error.message?.contains("mismatch", ignoreCase = true) == true || 
                                       error.message?.contains("invalid", ignoreCase = true) == true) 0 else state.step,
                            confirmPin = ""
                        ) 
                    }
                }
        }
    }
}
