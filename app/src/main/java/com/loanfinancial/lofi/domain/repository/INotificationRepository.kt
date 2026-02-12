package com.loanfinancial.lofi.domain.repository

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.NotificationResponse
import kotlinx.coroutines.flow.Flow

interface INotificationRepository {
    fun getNotifications(): Flow<Resource<List<NotificationResponse>>>
    fun getUnreadCount(): Flow<Int>
    suspend fun syncNotifications()
}
