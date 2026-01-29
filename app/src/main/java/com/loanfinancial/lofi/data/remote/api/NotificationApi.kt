package com.loanfinancial.lofi.data.remote.api

import com.loanfinancial.lofi.core.network.BaseResponse
import com.loanfinancial.lofi.data.model.dto.NotificationResponse
import retrofit2.Response
import retrofit2.http.GET

interface NotificationApi {
    @GET("notifications")
    suspend fun getNotifications(): Response<BaseResponse<List<NotificationResponse>>>
}
