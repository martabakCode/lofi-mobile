package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.ChangePinRequest
import com.loanfinancial.lofi.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChangePinUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    operator fun invoke(request: ChangePinRequest): Flow<Resource<Unit>> {
        return userRepository.changePin(request)
    }
}
