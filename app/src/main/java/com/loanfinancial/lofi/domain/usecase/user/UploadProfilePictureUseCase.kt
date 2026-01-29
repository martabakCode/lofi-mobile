package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class UploadProfilePictureUseCase
    @Inject
    constructor(
        private val repository: IUserRepository,
    ) {
        operator fun invoke(file: File): Flow<Resource<UserUpdateData>> = repository.updateProfilePicture(file)
    }
