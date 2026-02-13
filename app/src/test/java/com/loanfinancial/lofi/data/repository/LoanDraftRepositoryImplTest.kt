package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.data.local.dao.LoanDraftDao
import com.loanfinancial.lofi.data.model.entity.LoanDraftEntity
import com.loanfinancial.lofi.domain.model.DraftStatus
import com.loanfinancial.lofi.domain.model.DraftStep
import com.loanfinancial.lofi.domain.model.LoanDraft
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
class LoanDraftRepositoryImplTest {
    @MockK
    private lateinit var loanDraftDao: LoanDraftDao

    private lateinit var repository: LoanDraftRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        repository = LoanDraftRepositoryImpl(loanDraftDao)
    }

    @Test
    fun `saveDraft should create new draft when id is empty`() =
        runTest {
            // Arrange
            val draft = createLoanDraft("")
            coEvery { loanDraftDao.insertDraft(any()) } returns 1L

            // Act
            val result = repository.saveDraft(draft)

            // Assert
            assertTrue(result.isSuccess)
            assertNotNull(result.getOrNull())
            coVerify { loanDraftDao.insertDraft(any()) }
        }

    @Test
    fun `saveDraft should update existing draft when id provided`() =
        runTest {
            // Arrange
            val draft = createLoanDraft("draft_123")
            coEvery { loanDraftDao.insertDraft(any()) } returns 1L

            // Act
            val result = repository.saveDraft(draft)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals("draft_123", result.getOrNull())
        }

    @Test
    fun `getDraftById should return draft when found`() =
        runTest {
            // Arrange
            val entity = createLoanDraftEntity("draft_123")
            coEvery { loanDraftDao.getDraftById("draft_123") } returns entity

            // Act
            val result = repository.getDraftById("draft_123")

            // Assert
            assertNotNull(result)
            assertEquals("draft_123", result?.id)
        }

    @Test
    fun `getDraftById should return null when not found`() =
        runTest {
            // Arrange
            coEvery { loanDraftDao.getDraftById("draft_123") } returns null

            // Act
            val result = repository.getDraftById("draft_123")

            // Assert
            assertNull(result)
        }

    @Test
    fun `getAllActiveDrafts should return list of drafts`() =
        runTest {
            // Arrange
            val entities =
                listOf(
                    createLoanDraftEntity("draft_1"),
                    createLoanDraftEntity("draft_2"),
                )
            every { loanDraftDao.getAllActiveDrafts() } returns flowOf(entities)

            // Act
            val result = repository.getAllActiveDrafts().first()

            // Assert
            assertEquals(2, result.size)
        }

    @Test
    fun `updateBasicInfo should call dao with correct parameters`() =
        runTest {
            // Arrange
            coEvery {
                loanDraftDao.updateBasicInfo(any(), any(), any(), any(), any(), any(), any(), any(), any())
            } just Runs

            // Act
            val result =
                repository.updateBasicInfo(
                    draftId = "draft_123",
                    amount = 5000000,
                    tenor = 12,
                    purpose = "Business",
                    downPayment = 1000000,
                    latitude = -6.2088,
                    longitude = 106.8456,
                    isBiometricVerified = true,
                )

            // Assert
            assertTrue(result.isSuccess)
            coVerify {
                loanDraftDao.updateBasicInfo(
                    draftId = any(),
                    amount = any(),
                    tenor = any(),
                    purpose = any(),
                    downPayment = any(),
                    latitude = any(),
                    longitude = any(),
                    isBiometricVerified = any(),
                    currentStep = any(),
                )
            }
        }

    @Test
    fun `updateEmploymentInfo should call dao`() =
        runTest {
            // Arrange
            coEvery {
                loanDraftDao.updateEmploymentInfo(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            } just Runs

            // Act
            val result =
                repository.updateEmploymentInfo(
                    draftId = "draft_123",
                    jobType = "Permanent",
                    companyName = "Test Company",
                    jobPosition = "Manager",
                    workDurationMonths = 24,
                    workAddress = "Jakarta",
                    officePhoneNumber = "021123456",
                    declaredIncome = 10000000,
                    additionalIncome = 2000000,
                    npwpNumber = "1234567890",
                )

            // Assert
            assertTrue(result.isSuccess)
        }

    @Test
    fun `updateEmergencyContact should call dao`() =
        runTest {
            // Arrange
            coEvery { loanDraftDao.updateEmergencyContact(any(), any(), any(), any(), any(), any()) } just Runs

            // Act
            val result =
                repository.updateEmergencyContact(
                    draftId = "draft_123",
                    name = "Emergency Contact",
                    relation = "Spouse",
                    phone = "+6281234567890",
                    address = "Jakarta",
                )

            // Assert
            assertTrue(result.isSuccess)
        }

    @Test
    fun `updateBankInfo should call dao`() =
        runTest {
            // Arrange
            coEvery { loanDraftDao.updateBankInfo(any(), any(), any(), any(), any(), any()) } just Runs

            // Act
            val result =
                repository.updateBankInfo(
                    draftId = "draft_123",
                    bankName = "BCA",
                    bankBranch = "Jakarta",
                    accountNumber = "1234567890",
                    accountHolderName = "Test User",
                )

            // Assert
            assertTrue(result.isSuccess)
        }

    @Test
    fun `updateDocumentPaths should call dao`() =
        runTest {
            // Arrange
            coEvery { loanDraftDao.updateDocumentPaths(any(), any(), any(), any()) } just Runs

            // Act
            val result =
                repository.updateDocumentPaths(
                    draftId = "draft_123",
                    documentPaths = mapOf("KTP" to "/path/to/ktp.jpg"),
                )

            // Assert
            assertTrue(result.isSuccess)
        }

    @Test
    fun `deleteDraft should call dao`() =
        runTest {
            // Arrange
            coEvery { loanDraftDao.deleteDraft("draft_123") } just Runs

            // Act
            repository.deleteDraft("draft_123")

            // Assert
            coVerify { loanDraftDao.deleteDraft("draft_123") }
        }

    @Test
    fun `deleteAllDrafts should call dao`() =
        runTest {
            // Arrange
            coEvery { loanDraftDao.deleteAllDrafts() } just Runs

            // Act
            repository.deleteAllDrafts()

            // Assert
            coVerify { loanDraftDao.deleteAllDrafts() }
        }

    @Test
    fun `completeDraft should call dao`() =
        runTest {
            // Arrange
            coEvery { loanDraftDao.updateTncAndComplete(any(), any(), any(), any()) } just Runs

            // Act
            val result = repository.completeDraft("draft_123", true)

            // Assert
            assertTrue(result.isSuccess)
        }

    private fun createLoanDraft(id: String) =
        LoanDraft(
            id = id,
            amount = 5000000,
            tenor = 12,
            purpose = "Business",
            downPayment = null,
            latitude = null,
            longitude = null,
            isBiometricVerified = false,
            documentPaths = null,
            interestRate = null,
            adminFee = null,
            isAgreementChecked = false,
            currentStep = DraftStep.BASIC_INFO,
            status = DraftStatus.DRAFT,
            isSynced = false,
            serverLoanId = null,
            documentUploadStatus = null,
            uploadQueueIds = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )

    private fun createLoanDraftEntity(id: String) =
        LoanDraftEntity(
            id = id,
            amount = 5000000,
            tenor = 12,
            purpose = "Business",
            downPayment = null,
            latitude = null,
            longitude = null,
            isBiometricVerified = false,
            documentPaths = null,
            interestRate = null,
            adminFee = null,
            isAgreementChecked = false,
            currentStep = DraftStep.BASIC_INFO.name,
            status = DraftStatus.DRAFT.name,
            isSynced = false,
            serverLoanId = null,
            documentUploadStatus = null,
            uploadQueueIds = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            // Employment fields
            jobType = null,
            companyName = null,
            jobPosition = null,
            workDurationMonths = null,
            workAddress = null,
            officePhoneNumber = null,
            declaredIncome = null,
            additionalIncome = null,
            npwpNumber = null,
            // Emergency contact fields
            emergencyContactName = null,
            emergencyContactRelation = null,
            emergencyContactPhone = null,
            emergencyContactAddress = null,
            // Bank fields
            bankName = null,
            bankBranch = null,
            accountNumber = null,
            accountHolderName = null,
        )
}
