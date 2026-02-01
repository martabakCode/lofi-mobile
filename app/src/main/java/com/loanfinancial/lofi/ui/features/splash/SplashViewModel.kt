package com.loanfinancial.lofi.ui.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState

    fun checkAuthStatus() {
        viewModelScope.launch {
            val token = dataStoreManager.tokenFlow.first()
            val isLoggedIn = !token.isNullOrBlank()

            _uiState.value = if (isLoggedIn) {
                SplashUiState.NavigateToDashboard
            } else {
                SplashUiState.NavigateToLogin
            }
        }
    }
}

sealed class SplashUiState {
    data object Loading : SplashUiState()
    data object NavigateToLogin : SplashUiState()
    data object NavigateToDashboard : SplashUiState()
}
