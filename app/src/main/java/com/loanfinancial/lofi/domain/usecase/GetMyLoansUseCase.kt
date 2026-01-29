package com.loanfinancial.lofi.domain.usecase

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyLoansUseCase
    @Inject
    constructor(
        private val repository: ILoanRepository,
    ) {
        operator fun invoke(
            page: Int = 0,
            size: Int = 10,
            sort: String = "createdAt",
        ): Flow<Resource<List<Loan>>> = repository.getMyLoans(page, size, sort)
    }
