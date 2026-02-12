package com.loanfinancial.lofi.ui.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.domain.usecase.auth.GetAuthSourceUseCase
import com.loanfinancial.lofi.domain.usecase.auth.GetPinStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val getAuthSourceUseCase: GetAuthSourceUseCase,
    private val getPinStatusUseCase: GetPinStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState

    fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                val isFirstInstall = dataStoreManager.isFirstInstallFlow.first()
                if (isFirstInstall) {
                    // On first install, show onboarding
                    _uiState.value = SplashUiState.NavigateToOnboarding
                    return@launch
                }

                val token = dataStoreManager.tokenFlow.first()
                val isLoggedIn = !token.isNullOrBlank()

                if (isLoggedIn) {
                    val isProfileCompleted = dataStoreManager.isProfileCompletedFlow.first()
                    
                    // Sync PIN status from server to handle cases where PIN was set on another device
                    // or local data was cleared
                    val pinStatusResult = runCatching { getPinStatusUseCase() }.getOrNull()
                    val isPinSetFromServer = pinStatusResult?.getOrNull()?.pinSet ?: false
                    
                    // Update local cache if different from server
                    val isPinSetFromLocal = dataStoreManager.isPinSetFlow.first()
                    if (isPinSetFromServer != isPinSetFromLocal) {
                        dataStoreManager.setPinSet(isPinSetFromServer)
                    }
                    
                    // Check auth source to determine if user is Google user
                    val authSourceResult = runCatching { getAuthSourceUseCase() }.getOrNull()
                    val isGoogleUser = authSourceResult?.getOrNull()?.googleUser == true
                    
                    when {
                        !isProfileCompleted -> {
                            _uiState.value = SplashUiState.NavigateToCompleteProfile
                        }
                        !isPinSetFromServer -> {
                            if (isGoogleUser) {
                                _uiState.value = SplashUiState.NavigateToSetGooglePin
                            } else {
                                _uiState.value = SplashUiState.NavigateToSetPin
                            }
                        }
                        else -> {
                            _uiState.value = SplashUiState.NavigateToDashboard(isGuest = false)
                        }
                    }
                } else {
                    // If not logged in, navigate to Dashboard as Guest
                    _uiState.value = SplashUiState.NavigateToDashboard(isGuest = true)
                }
            } catch (e: Exception) {
                // On error, navigate to login for safety
                _uiState.value = SplashUiState.NavigateToLogin
            }
        }
    }
}

sealed class SplashUiState {
    data object Loading : SplashUiState()
    data object NavigateToOnboarding : SplashUiState()
    data object NavigateToLogin : SplashUiState()
    data class NavigateToDashboard(val isGuest: Boolean) : SplashUiState()
    data object NavigateToCompleteProfile : SplashUiState()
    data object NavigateToSetPin : SplashUiState()
    data object NavigateToSetGooglePin : SplashUiState()
}
