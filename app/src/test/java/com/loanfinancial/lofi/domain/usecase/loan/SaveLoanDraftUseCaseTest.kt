package com.loanfinancial.lofi.domain.usecase.loan

import com.loanfinancial.lofi.domain.model.DraftStatus
import com.loanfinancial.lofi.domain.model.DraftStep
import com.loanfinancial.lofi.domain.model.LoanDraft
import com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SaveLoanDraftUseCaseTest {
    @MockK
    private lateinit var repository: ILoanDraftRepository

    @MockK
    private lateinit var deleteAllDraftsUseCase: DeleteAllDraftsUseCase

    private lateinit var useCase: SaveLoanDraftUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = SaveLoanDraftUseCase(repository, deleteAllDraftsUseCase)
    }

    @Test
    fun `invoke should create new draft successfully`() =
        runTest {
            // Arrange
            coEvery { repository.saveDraft(any()) } returns Result.success("draft_123")

            // Act
            val result =
                useCase(
                    amount = 5000000,
                    tenor = 12,
                    purpose = "Business",
                    currentStep = DraftStep.BASIC_INFO,
                    status = DraftStatus.DRAFT,
                )

            // Assert
            assertTrue(result.isSuccess)
            assertEquals("draft_123", result.getOrNull())
            coVerify { repository.saveDraft(any()) }
        }

    @Test
    fun `invoke should update existing draft`() =
        runTest {
            // Arrange
            val existingDraft = createLoanDraft("draft_123")
            coEvery { repository.getDraftById("draft_123") } returns existingDraft
            coEvery { repository.saveDraft(any()) } returns Result.success("draft_123")

            // Act
            val result =
                useCase(
                    id = "draft_123",
                    amount = 10000000, // Updated amount
                    tenor = 24,
                    purpose = "Updated Purpose",
                    currentStep = DraftStep.BASIC_INFO,
                )

            // Assert
            assertTrue(result.isSuccess)
            coVerify {
                repository.saveDraft(
                    match {
                        it.id == "draft_123" && it.amount == 10000000L
                    },
                )
            }
        }

    @Test
    fun `saveOrUpdateBasicInfo should update existing draft when draftId provided`() =
        runTest {
            // Arrange
            coEvery {
                repository.updateBasicInfo(any(), any(), any(), any(), any(), any(), any(), any())
            } returns Result.success(Unit)

            // Act
            val result =
                useCase.saveOrUpdateBasicInfo(
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
            assertEquals("draft_123", result.getOrNull())
            coVerify {
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
            }
        }

    @Test
    fun `saveOrUpdateBasicInfo should create new draft when no draftId`() =
        runTest {
            // Arrange
            coEvery { repository.getAllActiveDrafts() } returns flowOf(emptyList())
            coEvery { repository.saveDraft(any()) } returns Result.success("new_draft_123")

            // Act
            val result =
                useCase.saveOrUpdateBasicInfo(
                    draftId = null,
                    amount = 5000000,
                    tenor = 12,
                    purpose = "Business",
                    downPayment = null,
                    latitude = null,
                    longitude = null,
                    isBiometricVerified = false,
                )

            // Assert
            assertTrue(result.isSuccess)
            assertEquals("new_draft_123", result.getOrNull())
        }

    @Test
    fun `saveOrUpdateBasicInfo should reuse existing active draft`() =
        runTest {
            // Arrange
            val existingDraft = createLoanDraft("existing_draft")
            coEvery { repository.getAllActiveDrafts() } returns flowOf(listOf(existingDraft))
            coEvery {
                repository.updateBasicInfo(any(), any(), any(), any(), any(), any(), any(), any())
            } returns Result.success(Unit)

            // Act
            val result =
                useCase.saveOrUpdateBasicInfo(
                    draftId = null,
                    amount = 5000000,
                    tenor = 12,
                    purpose = "Business",
                    downPayment = null,
                    latitude = null,
                    longitude = null,
                    isBiometricVerified = false,
                )

            // Assert
            assertTrue(result.isSuccess)
            assertEquals("existing_draft", result.getOrNull())
            // Should update existing, not create new
            coVerify {
                repository.updateBasicInfo(
                    draftId = "existing_draft",
                    amount = 5000000,
                    tenor = 12,
                    purpose = "Business",
                    downPayment = null,
                    latitude = null,
                    longitude = null,
                    isBiometricVerified = false,
                )
            }
        }

    @Test
    fun `updateEmploymentInfo should update repository and step`() =
        runTest {
            // Arrange
            coEvery {
                repository.updateEmploymentInfo(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            } returns Result.success(Unit)
            coEvery { repository.updateDraftStep(any(), any()) } just Runs

            // Act
            val result =
                useCase.updateEmploymentInfo(
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
            coVerify { repository.updateDraftStep("draft_123", DraftStep.EMPLOYMENT_INFO) }
        }

    @Test
    fun `updateEmergencyContact should update repository and step`() =
        runTest {
            // Arrange
            coEvery { repository.updateEmergencyContact(any(), any(), any(), any(), any()) } returns
                Result.success(Unit)
            coEvery { repository.updateDraftStep(any(), any()) } just Runs

            // Act
            val result =
                useCase.updateEmergencyContact(
                    draftId = "draft_123",
                    name = "Emergency Contact",
                    relation = "Spouse",
                    phone = "+6281234567890",
                    address = "Jakarta",
                )

            // Assert
            assertTrue(result.isSuccess)
            coVerify { repository.updateDraftStep("draft_123", DraftStep.EMERGENCY_CONTACT) }
        }

    @Test
    fun `updateBankInfo should update repository and step`() =
        runTest {
            // Arrange
            coEvery { repository.updateBankInfo(any(), any(), any(), any(), any()) } returns
                Result.success(Unit)
            coEvery { repository.updateDraftStep(any(), any()) } just Runs

            // Act
            val result =
                useCase.updateBankInfo(
                    draftId = "draft_123",
                    bankName = "BCA",
                    bankBranch = "Jakarta",
                    accountNumber = "1234567890",
                    accountHolderName = "Test User",
                )

            // Assert
            assertTrue(result.isSuccess)
            coVerify { repository.updateDraftStep("draft_123", DraftStep.BANK_INFO) }
        }

    @Test
    fun `updateDocumentPaths should call repository`() =
        runTest {
            // Arrange
            val documentPaths =
                mapOf(
                    "KTP" to "/path/to/ktp.jpg",
                    "NPWP" to "/path/to/npwp.jpg",
                )
            coEvery { repository.updateDocumentPaths(any(), any()) } returns Result.success(Unit)

            // Act
            val result = useCase.updateDocumentPaths("draft_123", documentPaths)

            // Assert
            assertTrue(result.isSuccess)
            coVerify { repository.updateDocumentPaths("draft_123", documentPaths) }
        }

    @Test
    fun `should propagate error from repository`() =
        runTest {
            // Arrange
            val error = Exception("Database error")
            coEvery { repository.saveDraft(any()) } returns Result.failure(error)

            // Act
            val result =
                useCase(
                    amount = 5000000,
                    tenor = 12,
                    purpose = "Business",
                )

            // Assert
            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
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
}
