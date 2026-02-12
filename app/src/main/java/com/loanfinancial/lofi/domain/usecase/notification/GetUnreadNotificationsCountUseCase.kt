package com.loanfinancial.lofi.domain.usecase.notification

import com.loanfinancial.lofi.domain.repository.INotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnreadNotificationsCountUseCase
    @Inject
    constructor(
        private val repository: INotificationRepository,
    ) {
        operator fun invoke(): Flow<Int> = repository.getUnreadCount()
    }
