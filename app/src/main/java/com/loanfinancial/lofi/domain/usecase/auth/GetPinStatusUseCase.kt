package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.remote.api.PinStatusResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import javax.inject.Inject

class GetPinStatusUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(): Result<PinStatusResponse> = repository.getPinStatus()
}
