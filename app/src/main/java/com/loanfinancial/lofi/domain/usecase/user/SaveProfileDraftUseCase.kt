package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.data.model.entity.ProfileDraftEntity
import com.loanfinancial.lofi.domain.repository.IUserRepository
import javax.inject.Inject

class SaveProfileDraftUseCase
    @Inject
    constructor(
        private val repository: IUserRepository,
    ) {
        suspend operator fun invoke(draft: ProfileDraftEntity) {
            repository.saveProfileDraft(draft)
        }
    }
