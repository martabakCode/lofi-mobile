package com.loanfinancial.lofi.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.data.model.dto.ChangePasswordRequest
import com.loanfinancial.lofi.domain.usecase.auth.ChangePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordUiState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
)

@HiltViewModel
class ChangePasswordViewModel
    @Inject
    constructor(
        private val changePasswordUseCase: ChangePasswordUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ChangePasswordUiState())
        val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

        fun onOldPasswordChange(value: String) {
            _uiState.update { it.copy(oldPassword = value, error = null) }
        }

        fun onNewPasswordChange(value: String) {
            _uiState.update { it.copy(newPassword = value, error = null) }
        }

        fun onConfirmPasswordChange(value: String) {
            _uiState.update { it.copy(confirmPassword = value, error = null) }
        }

        fun submit() {
            if (!validate()) return

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val request =
                    ChangePasswordRequest(
                        oldPassword = _uiState.value.oldPassword,
                        newPassword = _uiState.value.newPassword,
                    )

                val result = changePasswordUseCase(request)

                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to change password",
                        )
                    }
                }
            }
        }

        private fun validate(): Boolean {
            val errors = mutableMapOf<String, String>()
            val state = _uiState.value

            if (state.oldPassword.isBlank()) errors["oldPassword"] = "Old password is required"
            if (state.newPassword.isBlank()) errors["newPassword"] = "New password is required"
            if (state.newPassword.length < 6) errors["newPassword"] = "Password must be at least 6 characters"
            if (state.confirmPassword != state.newPassword) errors["confirmPassword"] = "Passwords do not match"

            _uiState.update { it.copy(validationErrors = errors) }
            return errors.isEmpty()
        }
    }
