package com.loanfinancial.lofi.core.di

import android.content.Context
import androidx.room.Room
import com.loanfinancial.lofi.data.local.dao.LoanDao
import com.loanfinancial.lofi.data.local.dao.LoanDraftDao
import com.loanfinancial.lofi.data.local.dao.ProfileDraftDao
import com.loanfinancial.lofi.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private val MIGRATION_9_10 =
        object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    """
                CREATE TABLE IF NOT EXISTS pending_loan_submissions (
                    loanId TEXT PRIMARY KEY NOT NULL,
                    customerName TEXT NOT NULL,
                    productCode TEXT NOT NULL,
                    productName TEXT NOT NULL,
                    interestRate REAL NOT NULL,
                    loanAmount INTEGER NOT NULL,
                    tenor INTEGER NOT NULL,
                    loanStatus TEXT NOT NULL,
                    currentStage TEXT NOT NULL,
                    submittedAt TEXT,
                    loanStatusDisplay TEXT NOT NULL,
                    slaDurationHours INTEGER,
                    documentPaths TEXT NOT NULL,
                    pendingStatus TEXT NOT NULL,
                    retryCount INTEGER NOT NULL DEFAULT 0,
                    lastRetryTime INTEGER,
                    failureReason TEXT,
                    createdAt INTEGER NOT NULL
                )
            """,
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pending_status ON pending_loan_submissions(pendingStatus)")
            }
        }

    private val MIGRATION_10_11 =
        object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create loan_drafts table if it doesn't exist (for version 10 to 11)
                db.execSQL(
                    """
                CREATE TABLE IF NOT EXISTS loan_drafts (
                    id TEXT PRIMARY KEY NOT NULL,
                    amount INTEGER,
                    tenor INTEGER,
                    purpose TEXT,
                    latitude REAL,
                    longitude REAL,
                    isBiometricVerified INTEGER NOT NULL DEFAULT 0,
                    documentPaths TEXT,
                    interestRate REAL,
                    adminFee REAL,
                    isAgreementChecked INTEGER NOT NULL DEFAULT 0,
                    currentStep TEXT NOT NULL,
                    stepData TEXT,
                    status TEXT NOT NULL,
                    isSynced INTEGER NOT NULL DEFAULT 0,
                    serverLoanId TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """,
                )
            }
        }

    private val MIGRATION_11_12 =
        object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    """
                CREATE TABLE IF NOT EXISTS pending_document_uploads (
                    id TEXT PRIMARY KEY NOT NULL,
                    loanDraftId TEXT NOT NULL,
                    loanId TEXT,
                    documentType TEXT NOT NULL,
                    localFilePath TEXT NOT NULL,
                    fileName TEXT NOT NULL,
                    contentType TEXT NOT NULL,
                    documentId TEXT,
                    objectKey TEXT,
                    status TEXT NOT NULL,
                    retryCount INTEGER NOT NULL,
                    lastRetryTime INTEGER,
                    failureReason TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    compressedFilePath TEXT,
                    originalFileSize INTEGER NOT NULL,
                    compressedFileSize INTEGER NOT NULL,
                    isCompressed INTEGER NOT NULL,
                    cleanupScheduled INTEGER NOT NULL
                )
            """,
                )
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN documentUploadStatus TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN uploadQueueIds TEXT")
            }
        }

    private val MIGRATION_12_13 =
        object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pending_loan_submissions ADD COLUMN purpose TEXT")
                db.execSQL("ALTER TABLE pending_loan_submissions ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE pending_loan_submissions ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE pending_loan_submissions ADD COLUMN serverLoanId TEXT")
            }
        }

    private val MIGRATION_13_14 =
        object : androidx.room.migration.Migration(13, 14) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add missing columns to loan_drafts table
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN customerId TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN downPayment INTEGER")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN declaredIncome INTEGER")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN npwpNumber TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN jobType TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN companyName TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN jobPosition TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN workDurationMonths INTEGER")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN workAddress TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN officePhoneNumber TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN additionalIncome INTEGER")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN emergencyContactName TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN emergencyContactRelation TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN emergencyContactPhone TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN emergencyContactAddress TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN bankName TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN bankBranch TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN accountNumber TEXT")
                db.execSQL("ALTER TABLE loan_drafts ADD COLUMN accountHolderName TEXT")
            }
        }

    private val MIGRATION_14_15 =
        object : androidx.room.migration.Migration(14, 15) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add loan tracking date columns to loans table
                db.execSQL("ALTER TABLE loans ADD COLUMN reviewedAt TEXT")
                db.execSQL("ALTER TABLE loans ADD COLUMN approvedAt TEXT")
                db.execSQL("ALTER TABLE loans ADD COLUMN rejectedAt TEXT")
                db.execSQL("ALTER TABLE loans ADD COLUMN disbursedAt TEXT")
                db.execSQL("ALTER TABLE loans ADD COLUMN disbursementReference TEXT")
            }
        }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "lofi_db",
            ).addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideLoanDao(database: AppDatabase): LoanDao = database.loanDao()

    @Provides
    fun provideUserDao(database: AppDatabase): com.loanfinancial.lofi.data.local.dao.UserDao = database.userDao()

    @Provides
    fun provideProfileDraftDao(database: AppDatabase): ProfileDraftDao = database.profileDraftDao()

    @Provides
    fun providePendingLoanSubmissionDao(database: AppDatabase): com.loanfinancial.lofi.data.local.dao.PendingLoanSubmissionDao =
        database.pendingLoanSubmissionDao()

    @Provides
    fun provideLoanDraftDao(database: AppDatabase): LoanDraftDao = database.loanDraftDao()

    @Provides
    fun providePendingDocumentUploadDao(database: AppDatabase): com.loanfinancial.lofi.data.local.dao.PendingDocumentUploadDao =
        database.pendingDocumentUploadDao()
}
