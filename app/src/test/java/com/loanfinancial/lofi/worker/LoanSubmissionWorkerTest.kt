package com.loanfinancial.lofi.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.loanfinancial.lofi.core.notification.LoanSubmissionNotificationManager
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.dao.PendingLoanSubmissionDao
import com.loanfinancial.lofi.data.model.dto.CreateLoanRequest
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
        MockKAnnotations.init(this)

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

    @Test
    fun `doWork should return failure when no loanId provided`() =
        runTest {
            // Arrange
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

            // Act
            val result = testWorker.doWork()

            // Assert
            assertEquals(ListenableWorker.Result.failure(), result)
        }

    @Test
    fun `doWork should create loan and submit successfully`() =
        runTest {
            // Arrange
            val pendingSubmission = createPendingSubmission()
            val createdLoan = createLoan("server_loan_123", "DRAFT")
            val submittedLoan = createLoan("server_loan_123", "SUBMITTED")

            coEvery { pendingSubmissionDao.getById("test_loan_id") } returns pendingSubmission
            coEvery {
                pendingSubmissionDao.updateSubmissionStatus(any(), any(), any(), any())
            } just Runs

            // Mock create loan
            coEvery {
                loanRepository.createLoan(any<CreateLoanRequest>())
            } returns flowOf(Resource.Success(createdLoan))

            // Mock get pending uploads (empty - no documents)
            coEvery { documentRepository.getPendingUploads("server_loan_123") } returns emptyList()

            // Mock get loan detail
            coEvery { loanRepository.getLoanDetail("server_loan_123") } returns
                flowOf(Resource.Success(createdLoan))

            // Mock submit loan
            coEvery { loanRepository.submitLoan("server_loan_123") } returns
                flowOf(Resource.Success(submittedLoan))

            coEvery { notificationManager.showSuccessNotification("test_loan_id") } just Runs
            coEvery { pendingSubmissionDao.update(any()) } just Runs

            // Act
            val result = worker.doWork()

            // Assert
            assertEquals(ListenableWorker.Result.success(), result)
            coVerify { loanRepository.createLoan(any<CreateLoanRequest>()) }
            coVerify { loanRepository.submitLoan("server_loan_123") }
            coVerify { notificationManager.showSuccessNotification("test_loan_id") }
        }

    @Test
    fun `doWork should handle loan creation failure and retry`() =
        runTest {
            // Arrange
            val pendingSubmission = createPendingSubmission()

            coEvery { pendingSubmissionDao.getById("test_loan_id") } returns pendingSubmission
            coEvery {
                pendingSubmissionDao.updateSubmissionStatus(any(), any(), any(), any())
            } just Runs

            // Mock create loan failure
            coEvery {
                loanRepository.createLoan(any<CreateLoanRequest>())
            } returns flowOf(Resource.Error("Network error"))

            // Act
            val result = worker.doWork()

            // Assert - Should retry on network error
            assertEquals(ListenableWorker.Result.retry(), result)
        }

    @Test
    fun `doWork should update existing server loan when serverLoanId exists`() =
        runTest {
            // Arrange
            val pendingSubmission =
                createPendingSubmission().copy(
                    serverLoanId = "existing_server_id",
                )
            val loan = createLoan("existing_server_id", "DRAFT")
            val submittedLoan = createLoan("existing_server_id", "SUBMITTED")

            coEvery { pendingSubmissionDao.getById("test_loan_id") } returns pendingSubmission
            coEvery {
                pendingSubmissionDao.updateSubmissionStatus(any(), any(), any(), any())
            } just Runs

            // Should NOT create new loan, use existing
            coEvery { documentRepository.getPendingUploads("existing_server_id") } returns emptyList()
            coEvery { loanRepository.getLoanDetail("existing_server_id") } returns
                flowOf(Resource.Success(loan))
            coEvery { loanRepository.submitLoan("existing_server_id") } returns
                flowOf(Resource.Success(submittedLoan))
            coEvery { notificationManager.showSuccessNotification("test_loan_id") } just Runs

            // Act
            val result = worker.doWork()

            // Assert
            assertEquals(ListenableWorker.Result.success(), result)
            coVerify(exactly = 0) { loanRepository.createLoan(any<CreateLoanRequest>()) }
            coVerify { loanRepository.submitLoan("existing_server_id") }
        }

    @Test
    fun `doWork should skip submit if loan already submitted`() =
        runTest {
            // Arrange
            val pendingSubmission = createPendingSubmission()
            val alreadySubmittedLoan = createLoan("server_loan_123", "SUBMITTED")

            coEvery { pendingSubmissionDao.getById("test_loan_id") } returns pendingSubmission
            coEvery {
                pendingSubmissionDao.updateSubmissionStatus(any(), any(), any(), any())
            } just Runs

            coEvery {
                loanRepository.createLoan(any<CreateLoanRequest>())
            } returns flowOf(Resource.Success(alreadySubmittedLoan))

            coEvery { documentRepository.getPendingUploads("server_loan_123") } returns emptyList()
            coEvery { loanRepository.getLoanDetail("server_loan_123") } returns
                flowOf(Resource.Success(alreadySubmittedLoan))

            // Should NOT call submit because already SUBMITTED
            coEvery { notificationManager.showSuccessNotification("test_loan_id") } just Runs

            // Act
            val result = worker.doWork()

            // Assert
            assertEquals(ListenableWorker.Result.success(), result)
            coVerify(exactly = 0) { loanRepository.submitLoan(any()) }
        }

    @Test
    fun `doWork should handle max retry count and fail`() =
        runTest {
            // Arrange
            val pendingSubmission =
                createPendingSubmission().copy(
                    retryCount = LoanSubmissionWorker.MAX_RETRY_COUNT,
                )

            coEvery { pendingSubmissionDao.getById("test_loan_id") } returns pendingSubmission
            coEvery {
                pendingSubmissionDao.updateSubmissionStatus(any(), any(), any(), any())
            } just Runs

            coEvery {
                loanRepository.createLoan(any<CreateLoanRequest>())
            } returns flowOf(Resource.Error("Server error"))

            coEvery { notificationManager.showFailureNotification(any(), any()) } just Runs

            // Act
            val result = worker.doWork()

            // Assert - Should fail after max retries
            assertEquals(ListenableWorker.Result.failure(), result)
            coVerify { notificationManager.showFailureNotification(any(), any()) }
        }

    @Test
    fun `doWork should queue documents for upload`() =
        runTest {
            // Arrange
            val pendingSubmission = createPendingSubmission()
            val createdLoan = createLoan("server_loan_123", "DRAFT")

            coEvery { pendingSubmissionDao.getById("test_loan_id") } returns pendingSubmission
            coEvery {
                pendingSubmissionDao.updateSubmissionStatus(any(), any(), any(), any())
            } just Runs
            coEvery {
                loanRepository.createLoan(any<CreateLoanRequest>())
            } returns flowOf(Resource.Success(createdLoan))

            // Has documents to upload
            coEvery { documentRepository.getPendingUploads("server_loan_123") } returns emptyList()
            coEvery { pendingSubmissionDao.update(any()) } just Runs

            // Act
            val result = worker.doWork()

            // Assert - Should queue documents and wait
            assertEquals(ListenableWorker.Result.retry(), result)
        }

    @Test
    fun `doWork should handle exception gracefully`() =
        runTest {
            // Arrange
            coEvery { pendingSubmissionDao.getById("test_loan_id") } throws
                RuntimeException("Database error")

            // Act
            val result = worker.doWork()

            // Assert
            assertTrue(result is ListenableWorker.Result.Retry)
        }

    private fun createPendingSubmission() =
        PendingLoanSubmissionEntity(
            loanId = "test_loan_id",
            userId = "user_123",
            customerName = "Test Customer",
            productCode = "CASH_LOAN",
            productName = "Pinjaman Tunai",
            interestRate = 0.05,
            loanAmount = 5000000,
            tenor = 12,
            loanStatus = "DRAFT",
            currentStage = "SUBMISSION",
            submittedAt = null,
            loanStatusDisplay = "Draft",
            slaDurationHours = null,
            purpose = "Business",
            latitude = -6.2088,
            longitude = 106.8456,
            documentPaths = "{}",
            pendingStatus = "PENDING",
            retryCount = 0,
            lastRetryTime = null,
            failureReason = null,
            createdAt = System.currentTimeMillis(),
        )

    private fun createLoan(
        id: String,
        status: String,
    ) =
        Loan(
            id = id,
            customerName = "Test Customer",
            product =
                Product(
                    productCode = "CASH_LOAN",
                    productName = "Pinjaman Tunai",
                    interestRate = 0.05,
                ),
            loanAmount = 5000000,
            tenor = 12,
            loanStatus = status,
            currentStage = "SUBMISSION",
            submittedAt = "2024-01-15T10:30:00Z",
            reviewedAt = null,
            approvedAt = if (status == "APPROVED" || status == "DISBURSED") "2024-01-16T10:30:00Z" else null,
            rejectedAt = null,
            disbursedAt = if (status == "DISBURSED") "2024-01-17T10:30:00Z" else null,
            loanStatusDisplay = status,
            slaDurationHours = 24,
        )
}
