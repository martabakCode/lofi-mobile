package com.loanfinancial.lofi.ui.features.auth.biometric

import android.content.Context
import com.loanfinancial.lofi.R

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.biometric.BiometricAuthenticator
import com.loanfinancial.lofi.core.biometric.BiometricResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BiometricLoginUiState(
    val isLoading: Boolean = false,
    val biometricSuccess: Boolean = false,
    val error: String? = null,
    val isBiometricAvailable: Boolean = true,
)

class BiometricLoginViewModel(
    private val biometricAuthenticator: BiometricAuthenticator,
    private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BiometricLoginUiState())
    val uiState: StateFlow<BiometricLoginUiState> = _uiState.asStateFlow()

    init {
        checkBiometricAvailability()
    }

    private fun checkBiometricAvailability() {
        val isAvailable =
            biometricAuthenticator.isBiometricAvailable() &&
                biometricAuthenticator.isBiometricEnrolled()
        _uiState.update { it.copy(isBiometricAvailable = isAvailable) }
    }

    fun authenticate() {
        if (!_uiState.value.isBiometricAvailable) {
            _uiState.update {
                it.copy(error = context.getString(R.string.error_biometric_not_available))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            biometricAuthenticator
                .authenticate(
                    title = context.getString(R.string.biometric_prompt_title),
                    subtitle = context.getString(R.string.biometric_prompt_subtitle),
                    description = context.getString(R.string.biometric_prompt_description),
                    negativeButtonText = context.getString(R.string.biometric_prompt_negative),
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
                                    error = context.getString(R.string.error_biometric_not_available),
                                )
                            }
                        }
                        is BiometricResult.NotEnrolled -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = context.getString(R.string.error_biometric_not_enrolled),
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

    companion object {
        fun provideFactory(
            biometricAuthenticator: BiometricAuthenticator,
            context: Context,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(BiometricLoginViewModel::class.java)) {
                        return BiometricLoginViewModel(biometricAuthenticator, context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
