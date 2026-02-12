package com.loanfinancial.lofi.data.remote.api

import com.google.gson.annotations.SerializedName
import com.loanfinancial.lofi.core.network.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PinApi {
    @POST("auth/verify-pin")
    suspend fun verifyPin(
        @Body request: PinVerificationRequest,
    ): Response<BaseResponse<PinVerificationResponse>>

    @GET("auth/pin-policy")
    suspend fun getPinPolicy(): Response<BaseResponse<PinPolicyResponse>>

    @POST("auth/set-pin")
    suspend fun setPin(
        @Body request: SetPinRequest,
    ): Response<BaseResponse<Unit>>

    @GET("auth/has-pin")
    suspend fun hasPin(): Response<BaseResponse<Boolean>>

    @GET("users/me/auth-source")
    suspend fun getAuthSource(): Response<BaseResponse<AuthSourceResponse>>

    @POST("users/set-google-pin")
    suspend fun setGooglePin(
        @Body request: SetGooglePinRequest,
    ): Response<BaseResponse<Unit>>

    @POST("users/me/google-pin")
    suspend fun updateGooglePin(
        @Body request: UpdateGooglePinRequest,
    ): Response<BaseResponse<Unit>>

    @GET("users/me/pin/status")
    suspend fun getPinStatus(): Response<BaseResponse<PinStatusResponse>>
}

data class SetPinRequest(
    @SerializedName("pin") val pin: String,
)

data class PinVerificationRequest(
    @SerializedName("pin") val pin: String,
    @SerializedName("purpose") val purpose: String,
)

data class PinVerificationResponse(
    @SerializedName("isValid") val isValid: Boolean,
    @SerializedName("remainingAttempts") val remainingAttempts: Int,
    @SerializedName("isLocked") val isLocked: Boolean,
    @SerializedName("lockedUntil") val lockedUntil: String?,
)

data class PinPolicyResponse(
    @SerializedName("minLength") val minLength: Int,
    @SerializedName("maxLength") val maxLength: Int,
    @SerializedName("maxAttempts") val maxAttempts: Int,
    @SerializedName("expiryHours") val expiryHours: Int,
)

data class SetGooglePinRequest(
    @SerializedName("pin") val pin: String,
)

data class UpdateGooglePinRequest(
    @SerializedName("oldPin") val oldPin: String,
    @SerializedName("newPin") val newPin: String,
)

data class AuthSourceResponse(
    @SerializedName("authSource") val authSource: String,
    @SerializedName("googleUser") val googleUser: Boolean,
)

data class PinStatusResponse(
    @SerializedName("pinSet") val pinSet: Boolean,
)
