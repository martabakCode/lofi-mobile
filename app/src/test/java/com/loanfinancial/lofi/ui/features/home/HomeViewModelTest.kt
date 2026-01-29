package com.loanfinancial.lofi.ui.features.home

import app.cash.turbine.test
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.usecase.GetMyLoansUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {
    private val getMyLoansUseCase: GetMyLoansUseCase = mockk()
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Mock init call
        every { getMyLoansUseCase() } returns flowOf(Resource.Loading)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchLoans should update uiState with success`() =
        runTest {
            // Arrange
            val loans = emptyList<com.loanfinancial.lofi.domain.model.Loan>()
            every { getMyLoansUseCase() } returns flowOf(Resource.Success(loans))

            // Act
            viewModel = HomeViewModel(getMyLoansUseCase)
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(false, state.isLoading)
                assertEquals(loans, state.loans)
                assertEquals(null, state.error)
            }
        }

    @Test
    fun `fetchLoans should update uiState with error`() =
        runTest {
            // Arrange
            val errorMessage = "Error Message"
            every { getMyLoansUseCase() } returns flowOf(Resource.Error(errorMessage))

            // Act
            viewModel = HomeViewModel(getMyLoansUseCase)
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(false, state.isLoading)
                assertEquals(errorMessage, state.error)
            }
        }
}
