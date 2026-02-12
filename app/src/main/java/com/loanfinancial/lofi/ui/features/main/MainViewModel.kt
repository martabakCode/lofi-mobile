package com.loanfinancial.lofi.ui.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.domain.usecase.notification.GetUnreadNotificationsCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val getUnreadNotificationsCountUseCase: GetUnreadNotificationsCountUseCase,
    ) : ViewModel() {
        val unreadCount: StateFlow<Int> =
            getUnreadNotificationsCountUseCase()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = 0,
                )
    }
