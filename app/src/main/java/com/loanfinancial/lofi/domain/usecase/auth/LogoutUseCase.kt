package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.model.dto.LogoutResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import javax.inject.Inject

class LogoutUseCase
    @Inject
    constructor(
        private val repository: IAuthRepository,
    ) {
        suspend operator fun invoke(): Result<LogoutResponse> = repository.logout()
    }
