package com.loanfinancial.lofi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.loanfinancial.lofi.data.model.entity.LoanDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDraftDao {
    @Query("SELECT * FROM loan_drafts WHERE status = :status ORDER BY updatedAt DESC")
    fun getDraftsByStatus(status: String): Flow<List<LoanDraftEntity>>

    @Query("SELECT * FROM loan_drafts WHERE id = :draftId")
    suspend fun getDraftById(draftId: String): LoanDraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: LoanDraftEntity): Long

    @Update
    suspend fun updateDraft(draft: LoanDraftEntity)

    @Query("UPDATE loan_drafts SET currentStep = :step, stepData = :stepData, updatedAt = :timestamp WHERE id = :draftId")
    suspend fun updateDraftStep(draftId: String, step: String, stepData: String?, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE loan_drafts SET status = :status, updatedAt = :timestamp WHERE id = :draftId")
    suspend fun updateDraftStatus(draftId: String, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM loan_drafts WHERE id = :draftId")
    suspend fun deleteDraft(draftId: String)

    @Query("DELETE FROM loan_drafts")
    suspend fun deleteAllDrafts()

    @Query("SELECT * FROM loan_drafts WHERE status IN ('DRAFT', 'IN_PROGRESS') ORDER BY updatedAt DESC")
    fun getAllActiveDrafts(): Flow<List<LoanDraftEntity>>

    @Query("SELECT * FROM loan_drafts WHERE isSynced = 0 AND status = 'COMPLETED'")
    suspend fun getUnsyncedCompletedDrafts(): List<LoanDraftEntity>

    @Query("UPDATE loan_drafts SET isSynced = 1, serverLoanId = :serverLoanId WHERE id = :draftId")
    suspend fun markAsSynced(draftId: String, serverLoanId: String?)

    @Query("SELECT * FROM loan_drafts WHERE currentStep = :step AND status IN ('DRAFT', 'IN_PROGRESS') ORDER BY updatedAt DESC")
    fun getDraftsByStep(step: String): Flow<List<LoanDraftEntity>>
    
    @Query("UPDATE loan_drafts SET amount = :amount, tenor = :tenor, purpose = :purpose, downPayment = :downPayment, latitude = :latitude, longitude = :longitude, isBiometricVerified = :isBiometricVerified, currentStep = :currentStep, updatedAt = :timestamp WHERE id = :draftId")
    suspend fun updateBasicInfo(
        draftId: String,
        amount: Long?,
        tenor: Int?,
        purpose: String?,
        downPayment: Long?,
        latitude: Double?,
        longitude: Double?,
        isBiometricVerified: Boolean,
        currentStep: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE loan_drafts SET jobType = :jobType, companyName = :companyName, jobPosition = :jobPosition, workDurationMonths = :workDurationMonths, workAddress = :workAddress, officePhoneNumber = :officePhoneNumber, declaredIncome = :declaredIncome, additionalIncome = :additionalIncome, npwpNumber = :npwpNumber, currentStep = :currentStep, updatedAt = :timestamp WHERE id = :draftId")
    suspend fun updateEmploymentInfo(
        draftId: String,
        jobType: String?,
        companyName: String?,
        jobPosition: String?,
        workDurationMonths: Int?,
        workAddress: String?,
        officePhoneNumber: String?,
        declaredIncome: Long?,
        additionalIncome: Long?,
        npwpNumber: String?,
        currentStep: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE loan_drafts SET emergencyContactName = :name, emergencyContactRelation = :relation, emergencyContactPhone = :phone, emergencyContactAddress = :address, currentStep = :currentStep, updatedAt = :timestamp WHERE id = :draftId")
    suspend fun updateEmergencyContact(
        draftId: String,
        name: String?,
        relation: String?,
        phone: String?,
        address: String?,
        currentStep: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE loan_drafts SET bankName = :bankName, bankBranch = :bankBranch, accountNumber = :accountNumber, accountHolderName = :accountHolderName, currentStep = :currentStep, updatedAt = :timestamp WHERE id = :draftId")
    suspend fun updateBankInfo(
        draftId: String,
        bankName: String?,
        bankBranch: String?,
        accountNumber: String?,
        accountHolderName: String?,
        currentStep: String,
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE loan_drafts SET documentPaths = :documentPaths, currentStep = :currentStep, updatedAt = :timestamp WHERE id = :draftId")
    suspend fun updateDocumentPaths(
        draftId: String,
        documentPaths: String?,
        currentStep: String,
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE loan_drafts SET isAgreementChecked = :isAgreementChecked, status = :status, currentStep = :currentStep, updatedAt = :timestamp WHERE id = :draftId")
    suspend fun updateTncAndComplete(
        draftId: String,
        isAgreementChecked: Boolean,
        status: String,
        currentStep: String,
        timestamp: Long = System.currentTimeMillis()
    )
}
