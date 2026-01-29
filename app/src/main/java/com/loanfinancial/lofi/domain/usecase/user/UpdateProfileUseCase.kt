package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.data.model.dto.UserUpdateRequest
import com.loanfinancial.lofi.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateProfileUseCase
    @Inject
    constructor(
        private val repository: IUserRepository,
    ) {
        operator fun invoke(request: UserUpdateRequest): Flow<Resource<UserUpdateData>> = repository.updateProfile(request)
    }
