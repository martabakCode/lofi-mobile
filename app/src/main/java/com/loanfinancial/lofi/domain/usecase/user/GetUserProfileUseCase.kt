package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase
    @Inject
    constructor(
        private val repository: IUserRepository,
    ) {
        operator fun invoke(): Flow<Resource<UserUpdateData>> = repository.getUserProfile()
    }
