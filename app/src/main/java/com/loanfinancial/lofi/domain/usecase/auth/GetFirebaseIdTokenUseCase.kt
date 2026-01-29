package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.domain.repository.IAuthRepository

class GetFirebaseIdTokenUseCase(
    private val repository: IAuthRepository,
) {
    suspend operator fun invoke(): Result<String> = repository.getFirebaseIdToken()
}
