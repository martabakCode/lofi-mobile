package com.loanfinancial.lofi.ui.features.auth.biometric

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.biometric.BiometricAuthenticator
import com.loanfinancial.lofi.core.biometric.BiometricResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BiometricLoginUiState(
    val isLoading: Boolean = false,
    val biometricSuccess: Boolean = false,
    val error: String? = null,
    val isBiometricAvailable: Boolean = true,
)

@HiltViewModel
class BiometricLoginViewModel @Inject constructor(
    private val biometricAuthenticator: BiometricAuthenticator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BiometricLoginUiState())
    val uiState: StateFlow<BiometricLoginUiState> = _uiState.asStateFlow()

    init {
        checkBiometricAvailability()
    }

    private fun checkBiometricAvailability() {
        val isAvailable = biometricAuthenticator.isBiometricAvailable() &&
                         biometricAuthenticator.isBiometricEnrolled()
        _uiState.update { it.copy(isBiometricAvailable = isAvailable) }
    }

    fun authenticate() {
        if (!_uiState.value.isBiometricAvailable) {
            _uiState.update {
                it.copy(error = "Biometric authentication is not available")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            biometricAuthenticator.authenticate(
                title = "Login to LOFI",
                subtitle = "Authenticate to continue",
                description = "Use your fingerprint or face to login",
                negativeButtonText = "Use Password",
            ).collect { result ->
                when (result) {
                    is BiometricResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                biometricSuccess = true,
                            )
                        }
                    }
                    is BiometricResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.errorMessage,
                            )
                        }
                    }
                    is BiometricResult.Cancelled -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    is BiometricResult.NotAvailable -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Biometric authentication is not available on this device",
                            )
                        }
                    }
                    is BiometricResult.NotEnrolled -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "No biometric credentials enrolled. Please set up fingerprint or face recognition in device settings.",
                            )
                        }
                    }
                }
            }
        }
    }

    fun onBiometricSuccessHandled() {
        _uiState.update { it.copy(biometricSuccess = false) }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }
}
