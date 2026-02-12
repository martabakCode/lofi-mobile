/*
package com.loanfinancial.lofi.data.manager

import android.content.Context
import com.loanfinancial.lofi.data.local.dao.PendingLoanSubmissionDao
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.data.model.entity.PendingLoanSubmissionEntity
import com.loanfinancial.lofi.domain.model.LoanSubmissionData
import com.loanfinancial.lofi.domain.model.PendingSubmissionStatus
import com.loanfinancial.lofi.worker.LoanSubmissionWorker
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LoanSubmissionManagerImplTest {
    private lateinit var context: Context
    private lateinit var pendingSubmissionDao: PendingLoanSubmissionDao
    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var manager: LoanSubmissionManagerImpl

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        pendingSubmissionDao = mockk(relaxed = true)
        dataStoreManager = mockk(relaxed = true)

        mockkObject(LoanSubmissionWorker)
        every { LoanSubmissionWorker.schedule(any(), any()) } just Runs
        every { LoanSubmissionWorker.scheduleImmediate(any(), any()) } just Runs
        manager = LoanSubmissionManagerImpl(context, pendingSubmissionDao, dataStoreManager)
    }

    @After
    fun tearDown() {
        unmockkObject(LoanSubmissionWorker)
    }

    @Test
    fun `submitLoanOffline should create pending submission successfully`() =
        runTest {
            // Arrange
            val loanData = createLoanSubmissionData()
            coEvery { dataStoreManager.getUserId() } returns "user_123"
            coEvery { pendingSubmissionDao.insert(any()) } just Runs

            // Act
            val result = manager.submitLoanOffline(loanData)

            // Assert
            assertTrue(result.isSuccess)
            assertNotNull(result.getOrNull())
            coVerify { pendingSubmissionDao.insert(any()) }
        }

    @Test
    fun `submitLoanOffline should fail when user not logged in`() =
        runTest {
            // Arrange
            val loanData = createLoanSubmissionData()
            coEvery { dataStoreManager.getUserId() } returns null

            // Act
            val result = manager.submitLoanOffline(loanData)

            // Assert
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalStateException)
        }

    @Test
    fun `getPendingSubmissions should return list of pending submissions`() =
        runTest {
            // Arrange
            val entities = listOf(createPendingEntity())
            coEvery { dataStoreManager.getUserId() } returns "user_123"
            coEvery { pendingSubmissionDao.getPendingSubmissionsByUser("user_123") } returns
                flowOf(entities)

            // Act
            val result = manager.getPendingSubmissions().first()

            // Assert
            assertEquals(1, result.size)
            assertEquals("test_loan_id", result[0].loanId)
            assertEquals(PendingSubmissionStatus.PENDING, result[0].status)
        }

    @Test
    fun `getPendingSubmissions should return empty list when user not logged in`() =
        runTest {
            // Arrange
            coEvery { dataStoreManager.getUserId() } returns null

            // Act
            val result = manager.getPendingSubmissions().first()

            // Assert
            assertTrue(result.isEmpty())
        }

    @Test
    fun `retrySubmission should update status and schedule worker`() =
        runTest {
            // Arrange
            coEvery {
                pendingSubmissionDao.updateSubmissionStatus(any(), any(), any(), any(), any())
            } just Runs

            // Act
            val result = manager.retrySubmission("test_loan_id")

            // Assert
            assertTrue(result.isSuccess)
            coVerify {
                pendingSubmissionDao.updateSubmissionStatus(
                    loanId = "test_loan_id",
                    status = "PENDING",
                    retryCount = 0,
                    timestamp = any(),
                    reason = "Manual retry",
                )
            }
        }

    @Test
    fun `cancelSubmission should cancel pending submission`() =
        runTest {
            // Arrange
            coEvery { pendingSubmissionDao.cancelSubmission("test_loan_id") } returns 1

            // Act
            manager.cancelSubmission("test_loan_id")

            // Assert
            coVerify { pendingSubmissionDao.cancelSubmission("test_loan_id") }
        }

    @Test
    fun `triggerPendingSubmissions should reset failed submissions`() =
        runTest {
            // Arrange
            val pendingList =
                listOf(
                    createPendingEntity().copy(pendingStatus = "FAILED"),
                    createPendingEntity().copy(pendingStatus = "PENDING"),
                )
            coEvery { pendingSubmissionDao.getAllPendingSubmissionsSync() } returns pendingList
            coEvery {
                pendingSubmissionDao.updateSubmissionStatus(any(), any(), any(), any(), any())
            } just Runs

            // Act
            manager.triggerPendingSubmissions()

            // Assert - Should reset FAILED to PENDING
            coVerify(atLeast = 1) {
                pendingSubmissionDao.updateSubmissionStatus(
                    loanId = any(),
                    status = "PENDING",
                    timestamp = any(),
                    reason = "Network Trigger",
                )
            }
        }

    private fun createLoanSubmissionData() =
        LoanSubmissionData(
            customerName = "Test Customer",
            productCode = "CASH_LOAN",
            productName = "Pinjaman Tunai",
            interestRate = 0.05,
            loanAmount = 5000000,
            tenor = 12,
            purpose = "Business",
            latitude = -6.2088,
            longitude = 106.8456,
            documentPaths = emptyMap(),
        )

    private fun createPendingEntity() =
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
}
*/
