package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.data.model.dto.UserUpdateRequest
import com.loanfinancial.lofi.domain.repository.IUserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import app.cash.turbine.test

@ExperimentalCoroutinesApi
class UpdateProfileUseCaseTest {

    private lateinit var repository: IUserRepository
    private lateinit var useCase: UpdateProfileUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = UpdateProfileUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository updateProfile`() = runTest {
        // Arrange
        val request = UserUpdateRequest(
            fullName = "John Doe",
            phoneNumber = "08123456789",
            incomeSource = "Salary",
            incomeType = "Monthly",
            monthlyIncome = 10000000.0,
            nik = "1234567890123456",
            dateOfBirth = "1990-01-01",
            placeOfBirth = "Jakarta",
            city = "Jakarta Selatan",
            address = "Jl. Sudirman",
            province = "DKI Jakarta",
            district = "Kebayoran Baru",
            subDistrict = "Senayan",
            postalCode = "12190",
            gender = "Male",
            maritalStatus = "Single",
            occupation = "Employee"
        )
        val expectedData = mockk<UserUpdateData>(relaxed = true)
        
        every { repository.updateProfile(request) } returns flowOf(
            Resource.Loading,
            Resource.Success(expectedData)
        )

        // Act
        useCase(request).test {
            assertEquals(Resource.Loading, awaitItem())
            val success = awaitItem() as Resource.Success
            assertEquals(expectedData, success.data)
            awaitComplete()
        }
        
        verify(exactly = 1) { repository.updateProfile(request) }
    }
}
