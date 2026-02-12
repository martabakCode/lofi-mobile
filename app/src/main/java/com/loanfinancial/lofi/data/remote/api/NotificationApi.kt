package com.loanfinancial.lofi.data.remote.api

import com.loanfinancial.lofi.core.network.BaseResponse
import com.loanfinancial.lofi.data.model.dto.NotificationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT

interface NotificationApi {
    @GET("notifications")
    suspend fun getNotifications(): Response<BaseResponse<List<NotificationResponse>>>

    @PUT("notifications/{id}/read")
    suspend fun markAsRead(
        @retrofit2.http.Path("id") id: String,
    ): Response<BaseResponse<Unit>>

    @PUT("notifications/mark-all-read")
    suspend fun markAllAsRead(): Response<BaseResponse<Unit>>
}
