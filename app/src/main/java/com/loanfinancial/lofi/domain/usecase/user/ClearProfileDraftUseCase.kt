package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.domain.repository.IUserRepository
import javax.inject.Inject

class ClearProfileDraftUseCase
    @Inject
    constructor(
        private val repository: IUserRepository,
    ) {
        suspend operator fun invoke(userId: String) {
            repository.clearProfileDraft(userId)
        }
    }
