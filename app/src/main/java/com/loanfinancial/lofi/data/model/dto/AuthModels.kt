package com.loanfinancial.lofi.data.model.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("fcmToken") val fcmToken: String,
)

/**
 * Common auth token response data used by login, register, and Google auth endpoints
 */
data class AuthTokenData(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Long,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("pinSet") val pinSet: Boolean = false,
    @SerializedName("profileCompleted") val profileCompleted: Boolean = false,
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("code") val code: String? = null,
    @SerializedName("data") val data: AuthTokenData? = null,
    @SerializedName("errors") val errors: Any? = null,
)

/**
 * Request body for Google Sign-In authentication
 */
data class GoogleAuthRequest(
    @SerializedName("idToken") val idToken: String,
    @SerializedName("fcmToken") val fcmToken: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
)

/**
 * Response for Google authentication - same structure as login/register
 */
data class GoogleAuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("code") val code: String? = null,
    @SerializedName("data") val data: AuthTokenData? = null,
    @SerializedName("errors") val errors: Any? = null,
)

data class UserResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserData? = null,
)

data class UserData(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("branchId") val branchId: String?,
    // Nullable because branchName wasn't in LoginData, best to be safe
    @SerializedName("branchName") val branchName: String?,
    @SerializedName("roles") val roles: List<String>,
    @SerializedName("permissions") val permissions: List<String>,
)

data class RegisterRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
)

data class RegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("code") val code: String? = null,
    @SerializedName("data") val data: AuthTokenData? = null,
    @SerializedName("errors") val errors: Any? = null,
)

data class LogoutResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
)

data class ChangePasswordRequest(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String,
)

data class ChangePasswordResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("code") val code: String? = null,
    @SerializedName("data") val data: Any? = null,
    @SerializedName("errors") val errors: Any? = null,
)

data class SetPinRequest(
    @SerializedName("pin") val pin: String,
    @SerializedName("password") val password: String? = null,
)

data class ChangePinRequest(
    @SerializedName("oldPin") val oldPin: String,
    @SerializedName("newPin") val newPin: String,
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String,
)

typealias RefreshTokenResponse = LoginResponse
