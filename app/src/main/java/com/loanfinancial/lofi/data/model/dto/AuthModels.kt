package com.loanfinancial.lofi.data.model.dto

data class LoginRequest(
    val email: String,
    val password: String,
    val fcmToken: String,
)

/**
 * Common auth token response data used by login, register, and Google auth endpoints
 */
data class AuthTokenData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String,
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val code: String? = null,
    val data: AuthTokenData? = null,
    val errors: Any? = null,
)

/**
 * Request body for Google Sign-In authentication
 */
data class GoogleAuthRequest(
    val idToken: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

/**
 * Response for Google authentication - same structure as login/register
 */
data class GoogleAuthResponse(
    val success: Boolean,
    val message: String,
    val code: String? = null,
    val data: AuthTokenData? = null,
    val errors: Any? = null,
)

data class UserResponse(
    val success: Boolean,
    val message: String,
    val data: UserData? = null,
)

data class UserData(
    val id: String,
    val email: String,
    val username: String,
    val branchId: String?,
    val branchName: String?, // Nullable because branchName wasn't in LoginData, best to be safe
    val roles: List<String>,
    val permissions: List<String>,
)

data class RegisterRequest(
    val fullName: String,
    val username: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val code: String? = null,
    val data: AuthTokenData? = null,
    val errors: Any? = null,
)

data class LogoutResponse(
    val success: Boolean,
    val message: String,
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
)

data class ChangePasswordResponse(
    val success: Boolean,
    val message: String,
    val code: String? = null,
    val data: Any? = null,
    val errors: Any? = null,
)
