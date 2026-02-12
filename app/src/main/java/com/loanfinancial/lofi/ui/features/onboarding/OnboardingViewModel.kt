package com.loanfinancial.lofi.ui.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        val dataStoreManager: DataStoreManager,
    ) : ViewModel() {
        fun completeOnboarding() {
            viewModelScope.launch {
                dataStoreManager.setFirstInstall(false)
            }
        }
    }
