package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.SetPinRequest
import com.loanfinancial.lofi.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetPinUseCase
    @Inject
    constructor(
        private val userRepository: IUserRepository,
    ) {
        operator fun invoke(request: SetPinRequest): Flow<Resource<Unit>> = userRepository.setPin(request)
    }
