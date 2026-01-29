package com.loanfinancial.lofi.data.remote.datasource

import com.loanfinancial.lofi.core.network.BaseResponse
import com.loanfinancial.lofi.data.model.dto.NotificationResponse
import com.loanfinancial.lofi.data.remote.api.NotificationApi
import retrofit2.Response
import javax.inject.Inject

interface NotificationRemoteDataSource {
    suspend fun getNotifications(): Response<BaseResponse<List<NotificationResponse>>>
}

class NotificationRemoteDataSourceImpl
    @Inject
    constructor(
        private val api: NotificationApi,
    ) : NotificationRemoteDataSource {
        override suspend fun getNotifications(): Response<BaseResponse<List<NotificationResponse>>> = api.getNotifications()
    }
