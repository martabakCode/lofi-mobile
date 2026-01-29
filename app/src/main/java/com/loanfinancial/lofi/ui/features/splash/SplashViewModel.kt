package com.loanfinancial.lofi.ui.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.data.local.datastore.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val preferencesManager: PreferencesManager,
    ) : ViewModel() {
        private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
        val isUserLoggedIn: StateFlow<Boolean?> = _isUserLoggedIn.asStateFlow()

        init {
            checkLoginState()
        }

        private fun checkLoginState() {
            preferencesManager.tokenFlow
                .distinctUntilChanged()
                .onEach { token ->
                    _isUserLoggedIn.value = !token.isNullOrBlank()
                }.launchIn(viewModelScope)
        }
    }
