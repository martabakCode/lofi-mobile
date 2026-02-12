package com.loanfinancial.lofi.domain.usecase.loan

import com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
import com.loanfinancial.lofi.domain.model.LoanDraft
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all active loan drafts.
 * Returns a Flow for real-time updates when drafts are added, modified, or deleted.
 * Only returns drafts with DRAFT or IN_PROGRESS status.
 */
class GetAllLoanDraftsUseCase @Inject constructor(
    private val repository: ILoanDraftRepository
) {
    operator fun invoke(): Flow<List<LoanDraft>> {
        return repository.getAllActiveDrafts()
    }
}
