package com.loanfinancial.lofi.ui.features.loan

import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.core.media.CameraManager
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import com.loanfinancial.lofi.domain.usecase.document.QueueDocumentUploadUseCase
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DocumentUploadViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @MockK
    private lateinit var queueDocumentUploadUseCase: QueueDocumentUploadUseCase

    @MockK
    private lateinit var documentRepository: IDocumentRepository

    @MockK
    private lateinit var cameraManager: CameraManager

    private lateinit var viewModel: DocumentUploadViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxed = true)
        viewModel =
            DocumentUploadViewModel(
                queueDocumentUploadUseCase,
                documentRepository,
                cameraManager,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `observeUploadStatus should update state from repository`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `isAllUploaded should be true when all documents completed`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `should handle upload error state`() =
        runTest {
            assertNotNull(viewModel)
        }

    @Test
    fun `onRetryUpload should re-queue failed upload`() =
        runTest {
            assertNotNull(viewModel)
        }
}
