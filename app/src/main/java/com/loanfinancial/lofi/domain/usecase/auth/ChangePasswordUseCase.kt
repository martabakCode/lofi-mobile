package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.model.dto.ChangePasswordRequest
import com.loanfinancial.lofi.data.model.dto.ChangePasswordResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import javax.inject.Inject

class ChangePasswordUseCase
    @Inject
    constructor(
        private val repository: IAuthRepository,
    ) {
        suspend operator fun invoke(request: ChangePasswordRequest): Result<ChangePasswordResponse> = repository.changePassword(request)
    }
