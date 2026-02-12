package com.loanfinancial.lofi.data.remote.api

import com.loanfinancial.lofi.core.network.BaseResponse
import com.loanfinancial.lofi.data.model.dto.ChangePinRequest
import com.loanfinancial.lofi.data.model.dto.SetPinRequest
import com.loanfinancial.lofi.data.model.dto.UserUpdateRequest
import com.loanfinancial.lofi.data.model.dto.UserUpdateResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface UserApi {
    @PUT("users/me")
    suspend fun updateProfile(
        @Body request: UserUpdateRequest,
    ): Response<UserUpdateResponse>

    @Multipart
    @PUT("users/me/photo")
    suspend fun updateProfilePicture(
        @Part file: MultipartBody.Part,
    ): Response<UserUpdateResponse>

    @GET("users/me")
    suspend fun getUserProfile(): Response<UserUpdateResponse>

    @GET("users/me/photo")
    suspend fun getProfilePhoto(): Response<okhttp3.ResponseBody>

    @POST("users/set-pin")
    suspend fun setPin(
        @Body request: SetPinRequest,
    ): Response<BaseResponse<Any>>

    @PUT("users/me/pin")
    suspend fun changePin(
        @Body request: ChangePinRequest,
    ): Response<BaseResponse<Any>>
}
