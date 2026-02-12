package com.loanfinancial.lofi.domain.repository

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.ChangePinRequest
import com.loanfinancial.lofi.data.model.dto.SetPinRequest
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.data.model.dto.UserUpdateRequest
import com.loanfinancial.lofi.data.model.entity.ProfileDraftEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

interface IUserRepository {
    fun updateProfile(request: UserUpdateRequest): Flow<Resource<UserUpdateData>>

    fun updateProfilePicture(file: File): Flow<Resource<UserUpdateData>>

    fun getUserProfile(): Flow<Resource<UserUpdateData>>

    fun getProfileDraft(userId: String): Flow<ProfileDraftEntity?>

    suspend fun saveProfileDraft(draft: ProfileDraftEntity)

    suspend fun clearProfileDraft(userId: String)

    fun setPin(request: SetPinRequest): Flow<Resource<Unit>>

    fun changePin(request: ChangePinRequest): Flow<Resource<Unit>>
}
