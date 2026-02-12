package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.domain.repository.IAuthRepository
import javax.inject.Inject

class HasPinUseCase
    @Inject
    constructor(
        private val repository: IAuthRepository,
    ) {
        suspend operator fun invoke(): Result<Boolean> = repository.hasPin()
    }
