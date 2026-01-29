package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.model.dto.LoginRequest
import com.loanfinancial.lofi.data.model.dto.LoginResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository

class LoginUseCase(
    private val repository: IAuthRepository,
) {
    suspend operator fun invoke(request: LoginRequest): Result<LoginResponse> = repository.login(request)
}
