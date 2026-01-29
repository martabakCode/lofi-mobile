package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.model.dto.GoogleAuthRequest
import com.loanfinancial.lofi.data.model.dto.GoogleAuthResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository

/**
 * Use case for authenticating with Google via backend
 * Takes the Google idToken and optional location coordinates,
 * sends to backend for verification and token generation
 */
class GoogleAuthUseCase(
    private val repository: IAuthRepository,
) {
    suspend operator fun invoke(request: GoogleAuthRequest): Result<GoogleAuthResponse> =
        repository.googleAuth(request)
}
