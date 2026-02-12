package com.loanfinancial.lofi.domain.usecase.loan

import com.loanfinancial.lofi.domain.model.LoanDraft
import com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
import javax.inject.Inject

/**
 * Use case to get a specific loan draft by ID.
 * Used for loading existing drafts in the loan application flow.
 */
class GetLoanDraftByIdUseCase
    @Inject
    constructor(
        private val repository: ILoanDraftRepository,
    ) {
        suspend operator fun invoke(draftId: String): LoanDraft? = repository.getDraftById(draftId)
    }
