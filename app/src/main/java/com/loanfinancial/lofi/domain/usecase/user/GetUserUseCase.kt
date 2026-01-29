package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.data.model.dto.UserResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository

class GetUserUseCase(
    private val repository: IAuthRepository,
) {
    suspend operator fun invoke(): Result<UserResponse> = repository.getUserInfo()
}
