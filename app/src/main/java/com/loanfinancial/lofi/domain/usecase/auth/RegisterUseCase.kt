package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.model.dto.RegisterRequest
import com.loanfinancial.lofi.data.model.dto.RegisterResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository

class RegisterUseCase(
    private val repository: IAuthRepository,
) {
    suspend operator fun invoke(request: RegisterRequest): Result<RegisterResponse> = repository.register(request)
}
