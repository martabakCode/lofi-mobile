package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.remote.api.PinVerificationResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import javax.inject.Inject

class VerifyPinUseCase @Inject constructor(
    private val repository: IAuthRepository,
) {
    suspend operator fun invoke(pin: String, purpose: String): Result<PinVerificationResponse> = 
        repository.verifyPin(pin, purpose)
}
