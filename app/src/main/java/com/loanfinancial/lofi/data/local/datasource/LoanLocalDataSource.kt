package com.loanfinancial.lofi.data.local.datasource

import com.loanfinancial.lofi.data.local.dao.LoanDao
import com.loanfinancial.lofi.data.model.entity.LoanEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface LoanLocalDataSource {
    fun getAllLoans(): Flow<List<LoanEntity>>

    fun getLoansByUser(userId: String): Flow<List<LoanEntity>>

    suspend fun insertLoans(loans: List<LoanEntity>)

    suspend fun deleteAllLoans()

    suspend fun deleteByUser(userId: String)

    suspend fun clearAndInsertByUser(
        userId: String,
        loans: List<LoanEntity>,
    )
}

class LoanLocalDataSourceImpl
    @Inject
    constructor(
        private val loanDao: LoanDao,
    ) : LoanLocalDataSource {
        override fun getAllLoans(): Flow<List<LoanEntity>> = loanDao.getAllLoans()

        override fun getLoansByUser(userId: String): Flow<List<LoanEntity>> = loanDao.getLoansByUser(userId)

        override suspend fun insertLoans(loans: List<LoanEntity>) {
            loanDao.insertLoans(loans)
        }

        override suspend fun deleteAllLoans() {
            loanDao.deleteAllLoans()
        }

        override suspend fun deleteByUser(userId: String) {
            loanDao.deleteByUser(userId)
        }

        override suspend fun clearAndInsertByUser(
            userId: String,
            loans: List<LoanEntity>,
        ) {
            loanDao.clearAndInsertByUser(userId, loans)
        }
    }
