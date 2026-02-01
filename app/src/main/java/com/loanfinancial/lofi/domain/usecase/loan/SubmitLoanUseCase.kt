package com.loanfinancial.lofi.domain.usecase.loan

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubmitLoanUseCase
    @Inject
    constructor(
        private val loanRepository: ILoanRepository,
    ) {
        operator fun invoke(id: String): Flow<Resource<Loan>> = loanRepository.submitLoan(id)
    }
