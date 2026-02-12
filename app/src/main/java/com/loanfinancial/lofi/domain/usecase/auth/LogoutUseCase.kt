package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.local.dao.LoanDao
import com.loanfinancial.lofi.data.local.dao.PendingDocumentUploadDao
import com.loanfinancial.lofi.data.local.dao.PendingLoanSubmissionDao
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.data.model.dto.LogoutResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import javax.inject.Inject

class LogoutUseCase
    @Inject
    constructor(

        private val repository: IAuthRepository,
        private val loanDao: LoanDao,
        private val documentUploadDao: PendingDocumentUploadDao,
        private val pendingSubmissionDao: PendingLoanSubmissionDao,
        private val dataStoreManager: DataStoreManager,
    ) {
        suspend operator fun invoke(): Result<LogoutResponse> {
            val userId = dataStoreManager.getUserId()
            if (userId != null) {
                // Clean up all user-specific data
                loanDao.deleteByUser(userId)
                documentUploadDao.deleteByUser(userId)
                pendingSubmissionDao.deleteByUser(userId)
            }
            return repository.logout()
        }
    }
