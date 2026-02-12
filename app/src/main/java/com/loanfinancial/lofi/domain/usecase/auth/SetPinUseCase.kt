package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.domain.repository.IAuthRepository
import javax.inject.Inject

class SetPinUseCase @Inject constructor(
    private val repository: IAuthRepository,
) {
    suspend operator fun invoke(pin: String): Result<Unit> = repository.setPin(pin)
}
