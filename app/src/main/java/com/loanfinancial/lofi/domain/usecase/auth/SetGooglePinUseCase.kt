package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.domain.repository.IAuthRepository
import javax.inject.Inject

class SetGooglePinUseCase
    @Inject
    constructor(
        private val repository: IAuthRepository,
    ) {
        suspend operator fun invoke(pin: String): Result<Unit> = repository.setGooglePin(pin)
    }
