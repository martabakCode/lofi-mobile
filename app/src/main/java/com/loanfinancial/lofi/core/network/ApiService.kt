package com.loanfinancial.lofi.core.network

import com.loanfinancial.lofi.data.model.dto.ChangePasswordRequest
import com.loanfinancial.lofi.data.model.dto.ChangePasswordResponse
import com.loanfinancial.lofi.data.model.dto.GoogleAuthRequest
import com.loanfinancial.lofi.data.model.dto.GoogleAuthResponse
import com.loanfinancial.lofi.data.model.dto.LoginRequest
import com.loanfinancial.lofi.data.model.dto.LoginResponse
import com.loanfinancial.lofi.data.model.dto.LogoutResponse
import com.loanfinancial.lofi.data.model.dto.RegisterRequest
import com.loanfinancial.lofi.data.model.dto.RegisterResponse
import com.loanfinancial.lofi.data.model.dto.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getUserInfo(): Response<UserResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): Response<RegisterResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<LogoutResponse>

    @POST("auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest,
    ): Response<ChangePasswordResponse>

    @POST("auth/google")
    suspend fun googleAuth(
        @Body request: GoogleAuthRequest,
    ): Response<GoogleAuthResponse>
}
