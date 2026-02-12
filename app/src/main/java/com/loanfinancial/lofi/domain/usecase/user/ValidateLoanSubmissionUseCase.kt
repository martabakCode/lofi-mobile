package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.repository.IUserRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.last
import javax.inject.Inject

data class LoanSubmissionEligibility(
    val isProfileComplete: Boolean,
    val isPinSet: Boolean,
    val missingProfileFields: List<String> = emptyList(),
)

class ValidateLoanSubmissionUseCase
    @Inject
    constructor(
        private val userRepository: IUserRepository,
    ) {
        suspend operator fun invoke(): Result<LoanSubmissionEligibility> =
            try {
                val resource =
                    userRepository
                        .getUserProfile()
                        .filter { it !is Resource.Loading }
                        .last()

                if (resource is Resource.Success) {
                    val data = resource.data
                    if (data != null) {
                        Result.success(
                            LoanSubmissionEligibility(
                                isProfileComplete = data.profileCompleted ?: false,
                                isPinSet = data.pinSet ?: false,
                            ),
                        )
                    } else {
                        Result.failure(Exception("User profile data is null"))
                    }
                } else if (resource is Resource.Error) {
                    Result.failure(Exception(resource.message ?: "Failed to fetch user profile"))
                } else {
                    Result.failure(Exception("Loading or unknown state"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
