package com.loanfinancial.lofi.domain.usecase.loan

import com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
import javax.inject.Inject

/**
 * Use case to delete all loan drafts.
 * Used for single-draft enforcement - ensures only one draft exists at a time.
 */
class DeleteAllDraftsUseCase @Inject constructor(
    private val repository: ILoanDraftRepository
) {
    suspend operator fun invoke() {
        repository.deleteAllDrafts()
    }
}
