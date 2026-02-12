package com.loanfinancial.lofi.domain.usecase

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetMyLoansUseCase
    @Inject
    constructor(
        private val repository: ILoanRepository,
        private val dataStoreManager: DataStoreManager,
    ) {
        operator fun invoke(
            page: Int = 0,
            size: Int = 10,
            sort: String = "createdAt",
        ): Flow<Resource<List<Loan>>> = flow {
            val userId = dataStoreManager.getUserId()
            if (userId != null) {
                emitAll(repository.getMyLoans(userId, page, size, sort))
            } else {
                emit(Resource.Error("User not logged in"))
            }
        }
    }
