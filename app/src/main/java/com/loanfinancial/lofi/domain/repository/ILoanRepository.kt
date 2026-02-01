package com.loanfinancial.lofi.domain.repository

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.model.Loan
import kotlinx.coroutines.flow.Flow

interface ILoanRepository {
    fun getMyLoans(
        page: Int,
        size: Int,
        sort: String,
    ): Flow<Resource<List<Loan>>>

    fun getLoanDetail(id: String): Flow<Resource<Loan>>

    fun submitLoan(id: String): Flow<Resource<Loan>>
}
