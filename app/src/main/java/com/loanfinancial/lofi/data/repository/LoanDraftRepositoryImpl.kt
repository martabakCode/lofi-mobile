package com.loanfinancial.lofi.data.repository

import com.google.gson.Gson
import com.loanfinancial.lofi.data.local.dao.LoanDraftDao
import com.loanfinancial.lofi.data.model.entity.LoanDraftEntity
import com.loanfinancial.lofi.domain.model.DraftStatus
import com.loanfinancial.lofi.domain.model.DraftStep
import com.loanfinancial.lofi.domain.model.LoanDraft
import com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class LoanDraftRepositoryImpl @Inject constructor(
    private val loanDraftDao: LoanDraftDao
) : ILoanDraftRepository {

    override suspend fun saveDraft(draft: LoanDraft): Result<String> {
        return try {
            val draftId = draft.id.ifEmpty { UUID.randomUUID().toString() }
            val entity = LoanDraftEntity(
                id = draftId,
                amount = draft.amount,
                tenor = draft.tenor,
                purpose = draft.purpose,
                downPayment = draft.downPayment,
                latitude = draft.latitude,
                longitude = draft.longitude,
                isBiometricVerified = draft.isBiometricVerified,
                
                // Employment Info
                jobType = draft.jobType,
                companyName = draft.companyName,
                jobPosition = draft.jobPosition,
                workDurationMonths = draft.workDurationMonths,
                workAddress = draft.workAddress,
                officePhoneNumber = draft.officePhoneNumber,
                declaredIncome = draft.declaredIncome,
                additionalIncome = draft.additionalIncome,
                npwpNumber = draft.npwpNumber,

                // Emergency Contact
                emergencyContactName = draft.emergencyContactName,
                emergencyContactRelation = draft.emergencyContactRelation,
                emergencyContactPhone = draft.emergencyContactPhone,
                emergencyContactAddress = draft.emergencyContactAddress,

                // Bank Info
                bankName = draft.bankName,
                bankBranch = draft.bankBranch,
                accountNumber = draft.accountNumber,
                accountHolderName = draft.accountHolderName,

                documentPaths = draft.documentPaths?.let { Gson().toJson(it) },
                interestRate = draft.interestRate,
                adminFee = draft.adminFee,
                isAgreementChecked = draft.isAgreementChecked,
                currentStep = draft.currentStep.name,
                status = draft.status.name,
                isSynced = draft.isSynced,
                serverLoanId = draft.serverLoanId,
                documentUploadStatus = draft.documentUploadStatus?.let { Gson().toJson(it) },
                uploadQueueIds = draft.uploadQueueIds?.let { Gson().toJson(it) },
                createdAt = if (draft.id.isEmpty()) System.currentTimeMillis() else draft.createdAt,
                updatedAt = System.currentTimeMillis()
            )
            loanDraftDao.insertDraft(entity)
            Result.success(draftId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDraftById(draftId: String): LoanDraft? {
        return try {
            loanDraftDao.getDraftById(draftId)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override fun getAllActiveDrafts(): Flow<List<LoanDraft>> {
        return loanDraftDao.getAllActiveDrafts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDraftsByStep(step: DraftStep): Flow<List<LoanDraft>> {
        return loanDraftDao.getDraftsByStep(step.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateBasicInfo(
        draftId: String,
        amount: Long?,
        tenor: Int?,
        purpose: String?,
        downPayment: Long?,
        latitude: Double?,
        longitude: Double?,
        isBiometricVerified: Boolean
    ): Result<Unit> {
        return try {
            loanDraftDao.updateBasicInfo(
                draftId = draftId,
                amount = amount,
                tenor = tenor,
                purpose = purpose,
                downPayment = downPayment,
                latitude = latitude,
                longitude = longitude,
                isBiometricVerified = isBiometricVerified,
                currentStep = DraftStep.BASIC_INFO.name
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmploymentInfo(
        draftId: String,
        jobType: String?,
        companyName: String?,
        jobPosition: String?,
        workDurationMonths: Int?,
        workAddress: String?,
        officePhoneNumber: String?,
        declaredIncome: Long?,
        additionalIncome: Long?,
        npwpNumber: String?
    ): Result<Unit> {
        return try {
            loanDraftDao.updateEmploymentInfo(
                draftId = draftId,
                jobType = jobType,
                companyName = companyName,
                jobPosition = jobPosition,
                workDurationMonths = workDurationMonths,
                workAddress = workAddress,
                officePhoneNumber = officePhoneNumber,
                declaredIncome = declaredIncome,
                additionalIncome = additionalIncome,
                npwpNumber = npwpNumber,
                currentStep = DraftStep.EMPLOYMENT_INFO.name
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmergencyContact(
        draftId: String,
        name: String?,
        relation: String?,
        phone: String?,
        address: String?
    ): Result<Unit> {
        return try {
            loanDraftDao.updateEmergencyContact(
                draftId = draftId,
                name = name,
                relation = relation,
                phone = phone,
                address = address,
                currentStep = DraftStep.EMERGENCY_CONTACT.name
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBankInfo(
        draftId: String,
        bankName: String?,
        bankBranch: String?,
        accountNumber: String?,
        accountHolderName: String?
    ): Result<Unit> {
        return try {
            loanDraftDao.updateBankInfo(
                draftId = draftId,
                bankName = bankName,
                bankBranch = bankBranch,
                accountNumber = accountNumber,
                accountHolderName = accountHolderName,
                currentStep = DraftStep.BANK_INFO.name
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDocumentPaths(
        draftId: String,
        documentPaths: Map<String, String>
    ): Result<Unit> {
        return try {
            loanDraftDao.updateDocumentPaths(
                draftId = draftId,
                documentPaths = Gson().toJson(documentPaths),
                currentStep = DraftStep.DOCUMENTS.name
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeDraft(
        draftId: String,
        isAgreementChecked: Boolean
    ): Result<Unit> {
        return try {
            loanDraftDao.updateTncAndComplete(
                draftId = draftId,
                isAgreementChecked = isAgreementChecked,
                status = DraftStatus.COMPLETED.name,
                currentStep = DraftStep.COMPLETED.name
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDraft(draftId: String) {
        loanDraftDao.deleteDraft(draftId)
    }

    override suspend fun deleteAllDrafts() {
        loanDraftDao.deleteAllDrafts()
    }

    override suspend fun updateDraftStep(draftId: String, step: DraftStep) {
        loanDraftDao.updateDraftStep(draftId, step.name, null)
    }

    override suspend fun getUnsyncedCompletedDrafts(): List<LoanDraft> {
        return loanDraftDao.getUnsyncedCompletedDrafts().map { it.toDomain() }
    }

    override suspend fun markAsSynced(draftId: String, serverLoanId: String?) {
        loanDraftDao.markAsSynced(draftId, serverLoanId)
        loanDraftDao.updateDraftStatus(draftId, DraftStatus.SYNCED.name)
    }

    private fun LoanDraftEntity.toDomain(): LoanDraft {
        return LoanDraft(
            id = id,
            amount = amount,
            tenor = tenor,
            purpose = purpose,
            downPayment = downPayment,
            latitude = latitude,
            longitude = longitude,
            isBiometricVerified = isBiometricVerified,
            
            // Employment Info
            jobType = jobType,
            companyName = companyName,
            jobPosition = jobPosition,
            workDurationMonths = workDurationMonths,
            workAddress = workAddress,
            officePhoneNumber = officePhoneNumber,
            declaredIncome = declaredIncome,
            additionalIncome = additionalIncome,
            npwpNumber = npwpNumber,

            // Emergency Contact
            emergencyContactName = emergencyContactName,
            emergencyContactRelation = emergencyContactRelation,
            emergencyContactPhone = emergencyContactPhone,
            emergencyContactAddress = emergencyContactAddress,

            // Bank Info
            bankName = bankName,
            bankBranch = bankBranch,
            accountNumber = accountNumber,
            accountHolderName = accountHolderName,

            documentPaths = documentPaths?.let {
                try {
                    val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                    Gson().fromJson(it, type)
                } catch (e: Exception) {
                    emptyMap()
                }
            },
            interestRate = interestRate,
            adminFee = adminFee,
            isAgreementChecked = isAgreementChecked,
            currentStep = try {
                DraftStep.valueOf(currentStep)
            } catch (e: IllegalArgumentException) {
                DraftStep.BASIC_INFO
            },
            status = try {
                DraftStatus.valueOf(status)
            } catch (e: IllegalArgumentException) {
                DraftStatus.DRAFT
            },
            isSynced = isSynced,
            serverLoanId = serverLoanId,
            documentUploadStatus = documentUploadStatus?.let {
                try {
                    val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                    Gson().fromJson(it, type)
                } catch (e: Exception) {
                    emptyMap()
                }
            },
            uploadQueueIds = uploadQueueIds?.let {
                try {
                    val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                    Gson().fromJson(it, type)
                } catch (e: Exception) {
                    emptyList()
                }
            },
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
