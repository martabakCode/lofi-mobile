package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.remote.api.AuthSourceResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import javax.inject.Inject

class GetAuthSourceUseCase
    @Inject
    constructor(
        private val repository: IAuthRepository,
    ) {
        suspend operator fun invoke(): Result<AuthSourceResponse> = repository.getAuthSource()
    }
