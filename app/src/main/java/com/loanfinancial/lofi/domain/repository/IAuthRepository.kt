package com.loanfinancial.lofi.domain.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
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
import com.loanfinancial.lofi.domain.model.User
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>

    suspend fun getUserInfo(): Result<UserResponse>

    suspend fun register(request: RegisterRequest): Result<RegisterResponse>

    suspend fun logout(): Result<LogoutResponse>

    suspend fun changePassword(request: ChangePasswordRequest): Result<ChangePasswordResponse>

    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser>

    /**
     * Authenticate with Google using idToken from Google Sign-In
     * This sends the idToken to the backend for verification and token generation
     */
    suspend fun googleAuth(request: GoogleAuthRequest): Result<GoogleAuthResponse>

    suspend fun getFirebaseIdToken(): Result<String>

    fun getUser(): Flow<User?>
}
