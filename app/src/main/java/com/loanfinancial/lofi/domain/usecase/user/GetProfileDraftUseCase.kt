package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.data.model.entity.ProfileDraftEntity
import com.loanfinancial.lofi.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfileDraftUseCase
    @Inject
    constructor(
        private val repository: IUserRepository,
    ) {
        operator fun invoke(userId: String): Flow<ProfileDraftEntity?> = repository.getProfileDraft(userId)
    }
