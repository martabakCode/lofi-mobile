package com.loanfinancial.lofi.domain.usecase.loan

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.CreateLoanRequest
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateLoanUseCase
    @Inject
    constructor(
        private val loanRepository: ILoanRepository,
    ) {
        operator fun invoke(request: CreateLoanRequest): Flow<Resource<Loan>> = loanRepository.createLoan(request)
    }
