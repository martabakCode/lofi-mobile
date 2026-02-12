package com.loanfinancial.lofi.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.loanfinancial.lofi.core.notification.LoanSubmissionNotificationManager
import com.loanfinancial.lofi.core.util.Logger
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.dao.PendingLoanSubmissionDao
import com.loanfinancial.lofi.data.model.entity.PendingLoanSubmissionEntity
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.Product
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LoanSubmissionWorkerTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var params: WorkerParameters

    @MockK
    private lateinit var loanRepository: ILoanRepository

    @MockK
    private lateinit var documentRepository: IDocumentRepository

    @MockK
    private lateinit var pendingSubmissionDao: PendingLoanSubmissionDao

    @MockK
    private lateinit var notificationManager: LoanSubmissionNotificationManager

    private lateinit var worker: LoanSubmissionWorker

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        mockkObject(Logger)
        every { Logger.d(any(), any()) } just Runs
        every { Logger.e(any(), any()) } just Runs
        every { Logger.e(any(), any(), any()) } just Runs
        every { Logger.w(any(), any()) } just Runs

        every { params.inputData.getString(LoanSubmissionWorker.KEY_LOAN_ID) } returns "test_loan_id"

        worker =
            LoanSubmissionWorker(
                context = context,
                params = params,
                loanRepository = loanRepository,
                documentRepository = documentRepository,
                pendingSubmissionDao = pendingSubmissionDao,
                notificationManager = notificationManager,
            )
    }

    @org.junit.After
    fun tearDown() {
        unmockkObject(Logger)
    }


    @Test
    fun `doWork should return failure when no loanId provided`() =
        runTest {
            every { params.inputData.getString(LoanSubmissionWorker.KEY_LOAN_ID) } returns null

            val testWorker =
                LoanSubmissionWorker(
                    context = context,
                    params = params,
                    loanRepository = loanRepository,
                    documentRepository = documentRepository,
                    pendingSubmissionDao = pendingSubmissionDao,
                    notificationManager = notificationManager,
                )

            val result = testWorker.doWork()
            assertEquals(ListenableWorker.Result.failure(), result)
        }

    @Test
    fun `doWork should create loan and submit successfully`() =
        runTest {
            val pendingSubmission = createPendingSubmission()
            val createdLoan = createLoan("server_loan_123", "DRAFT")
            val submittedLoan = createLoan("server_loan_123", "SUBMITTED")

            // Mock 4-arg update (SUBMITTING and SUCCESS)
            coEvery { pendingSubmissionDao.updateSubmissionStatus(any(), any(), any(), any()) } just Runs
            coEvery { pendingSubmissionDao.getById("test_loan_id") } returns pendingSubmission
            
            // Mock createLoan to return success
            coEvery { loanRepository.createLoan(any()) } returns flowOf(Resource.Success(createdLoan))
            // Mock document upload check
            coEvery { documentRepository.getPendingUploads("server_loan_123") } returns emptyList()
            // Mock getLoanDetail before submit
            coEvery { loanRepository.getLoanDetail("server_loan_123") } returns flowOf(Resource.Success(createdLoan))
            
            coEvery { loanRepository.submitLoan("server_loan_123") } returns flowOf(Resource.Success(submittedLoan))
            coEvery { notificationManager.showSuccessNotification("test_loan_id") } just Runs
            
            val result = worker.doWork()
            assertEquals(ListenableWorker.Result.success(), result)
        }

    @Test
    fun `doWork should delete submission on non-retriable error`() =
        runTest {
            val pendingSubmission = createPendingSubmission()
            
            coEvery { params.inputData.getString(LoanSubmissionWorker.KEY_LOAN_ID) } returns "test_loan_id"
            // Mock 4-arg update (SUBMITTING)
            coEvery { pendingSubmissionDao.updateSubmissionStatus(any(), any(), any(), any()) } just Runs
            coEvery { pendingSubmissionDao.getById("test_loan_id") } returns pendingSubmission
            
            // Mock createLoan to return 400 Error
            coEvery { loanRepository.createLoan(any()) } returns flowOf(Resource.Error("Network Error: 400"))
            
            // Expect deletion and notification
            coEvery { pendingSubmissionDao.delete("test_loan_id") } just Runs
            coEvery { notificationManager.showFailureNotification("test_loan_id", any()) } just Runs

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.failure(), result)
            coVerify { pendingSubmissionDao.delete("test_loan_id") }
            coVerify { notificationManager.showFailureNotification("test_loan_id", match { it?.contains("400") == true }) }
        }

    private fun createPendingSubmission() =
        PendingLoanSubmissionEntity(
            loanId = "test_loan_id",
            userId = "user_123",
            customerName = "Test User",
            productCode = "CASH",
            productName = "Pinjaman",
            interestRate = 0.05,
            loanAmount = 5000000L,
            tenor = 12,
            loanStatus = "DRAFT",
            currentStage = "SUBMISSION",
            submittedAt = null,
            loanStatusDisplay = "Draft",
            slaDurationHours = 24,
            purpose = "Business",
            latitude = null,
            longitude = null,
            serverLoanId = null,
            documentPaths = "{}",
            pendingStatus = "PENDING",
            retryCount = 0,
        )

    private fun createLoan(
        id: String,
        status: String,
    ) =
        Loan(
            id = id,
            customerName = "Test User",
            product = Product("CASH", "Pinjaman", 0.05),
            loanAmount = 5000000,
            tenor = 12,
            loanStatus = status,
            currentStage = "SUBMISSION",
            submittedAt = "2024-01-15T10:30:00Z",
            reviewedAt = null,
            approvedAt = null,
            rejectedAt = null,
            disbursedAt = null,
            loanStatusDisplay = "Test",
            slaDurationHours = 24,
        )
}
