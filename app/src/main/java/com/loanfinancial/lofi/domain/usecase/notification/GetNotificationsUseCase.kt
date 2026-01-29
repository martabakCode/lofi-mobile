package com.loanfinancial.lofi.domain.usecase.notification

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.NotificationResponse
import com.loanfinancial.lofi.domain.repository.INotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase
    @Inject
    constructor(
        private val repository: INotificationRepository,
    ) {
        operator fun invoke(): Flow<Resource<List<NotificationResponse>>> = repository.getNotifications()
    }
