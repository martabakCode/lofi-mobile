package com.loanfinancial.lofi.data.local.datasource

import com.loanfinancial.lofi.data.local.dao.LoanDao
import com.loanfinancial.lofi.data.model.entity.LoanEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface LoanLocalDataSource {
    fun getAllLoans(): Flow<List<LoanEntity>>

    suspend fun insertLoans(loans: List<LoanEntity>)

    suspend fun deleteAllLoans()
}

class LoanLocalDataSourceImpl
    @Inject
    constructor(
        private val loanDao: LoanDao,
    ) : LoanLocalDataSource {
        override fun getAllLoans(): Flow<List<LoanEntity>> = loanDao.getAllLoans()

        override suspend fun insertLoans(loans: List<LoanEntity>) {
            loanDao.insertLoans(loans)
        }

        override suspend fun deleteAllLoans() {
            loanDao.deleteAllLoans()
        }
    }
