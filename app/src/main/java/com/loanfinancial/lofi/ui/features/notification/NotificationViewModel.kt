package com.loanfinancial.lofi.ui.features.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.NotificationResponse
import com.loanfinancial.lofi.domain.usecase.notification.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

// Defined in Playbook Rule 10
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()

    data class Success<T>(
        val data: T,
    ) : UiState<T>()

    data class Error(
        val message: String,
    ) : UiState<Nothing>()
}

@HiltViewModel
class NotificationViewModel
    @Inject
    constructor(
        private val getNotificationsUseCase: GetNotificationsUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<List<NotificationResponse>>>(UiState.Loading)
        val uiState: StateFlow<UiState<List<NotificationResponse>>> = _uiState.asStateFlow()

        init {
            fetchNotifications()
        }

        fun fetchNotifications() {
            getNotificationsUseCase()
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = UiState.Loading
                        }
                        is Resource.Success -> {
                            _uiState.value = UiState.Success(result.data)
                        }
                        is Resource.Error -> {
                            _uiState.value = UiState.Error(result.message)
                        }
                    }
                }.launchIn(viewModelScope)
        }

        fun getNotification(id: String): NotificationResponse? {
            val state = _uiState.value
            return if (state is UiState.Success) {
                state.data.find { it.id == id }
            } else {
                null
            }
        }
    }
