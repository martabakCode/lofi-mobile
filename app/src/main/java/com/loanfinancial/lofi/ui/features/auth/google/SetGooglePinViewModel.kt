package com.loanfinancial.lofi.ui.features.auth.google

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.domain.usecase.auth.SetGooglePinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetGooglePinUiState(
    val pin: String = "",
    val confirmPin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    // 0 = enter pin, 1 = confirm pin
    val step: Int = 0,
    // When PIN is already set on server, redirect to change PIN
    val shouldNavigateToChangePin: Boolean = false,
)

@HiltViewModel
class SetGooglePinViewModel @Inject constructor(
    private val setGooglePinUseCase: SetGooglePinUseCase,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SetGooglePinUiState())
    val uiState: StateFlow<SetGooglePinUiState> = _uiState.asStateFlow()
    
    fun updatePin(newValue: String) {
        if (newValue.length > 6) return
        
        if (_uiState.value.step == 0) {
            _uiState.update { it.copy(pin = newValue, error = null) }
            if (newValue.length == 6) {
                _uiState.update { it.copy(step = 1) }
            }
        } else if (_uiState.value.step == 1) {
            _uiState.update { it.copy(confirmPin = newValue, error = null) }
            if (newValue.length == 6) {
                submitPin()
            }
        }
    }

    fun onBackClick() {
        if (_uiState.value.step == 1) {
            _uiState.update { it.copy(step = 0, confirmPin = "") }
        }
    }
    
    private fun submitPin() {
        val state = _uiState.value
        if (state.pin != state.confirmPin) {
            _uiState.update { 
                it.copy(
                    error = "PIN does not match",
                    confirmPin = "",
                    step = 0,
                    pin = ""
                ) 
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            setGooglePinUseCase(state.pin)
                .onSuccess {
                    dataStoreManager.setPinSet(true)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to set PIN"
                    // Check if PIN is already set on server
                    val isPinAlreadySet = errorMessage.contains("already set", ignoreCase = true) ||
                                         errorMessage.contains("update PIN", ignoreCase = true)
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = if (isPinAlreadySet) "PIN already set. Please use Change PIN instead." else errorMessage,
                            pin = "",
                            confirmPin = "",
                            step = 0,
                            shouldNavigateToChangePin = isPinAlreadySet
                        ) 
                    }
                }
        }
    }
    
    fun onNavigateToChangePinHandled() {
        _uiState.update { it.copy(shouldNavigateToChangePin = false) }
    }
}
