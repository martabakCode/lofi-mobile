package com.loanfinancial.lofi.domain.usecase.loan

import com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
import javax.inject.Inject

/**
 * Use case to delete a specific loan draft by ID.
 * Used when user wants to discard a saved draft.
 */
class DeleteLoanDraftUseCase @Inject constructor(
    private val repository: ILoanDraftRepository
) {
    suspend operator fun invoke(draftId: String) {
        repository.deleteDraft(draftId)
    }
}
