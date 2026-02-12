package com.loanfinancial.lofi.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.data.model.dto.SetPinRequest
import com.loanfinancial.lofi.domain.usecase.user.SetPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetPinUiState(
    val password: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val step: Int = 0 // 0 for password, 1 for enter pin, 2 for confirm pin
)

@HiltViewModel
class SetPinViewModel @Inject constructor(
    private val setPinUseCase: SetPinUseCase,
    private val dataStoreManager: com.loanfinancial.lofi.data.local.datastore.DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetPinUiState())
    val uiState: StateFlow<SetPinUiState> = _uiState.asStateFlow()

    fun onPasswordInput(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onSubmitPassword() {
        if (_uiState.value.password.isEmpty()) {
            _uiState.update { it.copy(error = "Password is required") }
            return
        }
        _uiState.update { it.copy(step = 1, error = null) }
    }

    fun updatePin(newValue: String) {
        if (newValue.length > 6) return
        
        if (_uiState.value.step == 1) {
            _uiState.update { it.copy(pin = newValue, error = null) }
            if (newValue.length == 6) {
                _uiState.update { it.copy(step = 2) }
            }
        } else if (_uiState.value.step == 2) {
             _uiState.update { it.copy(confirmPin = newValue, error = null) }
            if (newValue.length == 6) {
                submitPin() // Check logic inside submitPin to ensure it uses the latest value
            }
        }
    }

    private fun submitPin() {
        val state = _uiState.value
        if (state.pin != state.confirmPin) {
            _uiState.update { it.copy(error = "PIN does not match", confirmPin = "", step = 1, pin = "") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            setPinUseCase(SetPinRequest(state.pin, state.password)).collect { result ->
                 when (result) {
                    is com.loanfinancial.lofi.core.util.Resource.Success -> {
                        dataStoreManager.setPinSet(true)
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                    is com.loanfinancial.lofi.core.util.Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = result.message ?: "Failed to set PIN", 
                                pin = "", 
                                confirmPin = "", 
                                step = 1,
                                password = ""
                            ) 
                        }
                    }
                    is com.loanfinancial.lofi.core.util.Resource.Loading -> {}
                }
            }
        }
    }

    fun onBack() {
        if (_uiState.value.step == 2) {
            _uiState.update { it.copy(step = 1, confirmPin = "") }
        } else if (_uiState.value.step == 1) {
             _uiState.update { it.copy(step = 0, pin = "") }
        }
    }
}
