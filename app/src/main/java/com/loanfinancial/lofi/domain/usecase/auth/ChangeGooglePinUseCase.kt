package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.domain.repository.IAuthRepository
import javax.inject.Inject

class ChangeGooglePinUseCase
    @Inject
    constructor(
        private val repository: IAuthRepository,
    ) {
        suspend operator fun invoke(
            oldPin: String,
            newPin: String,
        ): Result<Unit> =
            repository.updateGooglePin(oldPin, newPin)
    }
