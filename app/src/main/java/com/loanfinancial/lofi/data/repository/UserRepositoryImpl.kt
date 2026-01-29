package com.loanfinancial.lofi.data.repository

import com.google.gson.Gson
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.dao.ProfileDraftDao
import com.loanfinancial.lofi.data.local.dao.UserDao
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
import com.loanfinancial.lofi.data.model.dto.UserUpdateRequest
import com.loanfinancial.lofi.data.model.entity.ProfileDraftEntity
import com.loanfinancial.lofi.data.model.entity.UserProfileEntity
import com.loanfinancial.lofi.data.remote.api.UserApi
import com.loanfinancial.lofi.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class UserRepositoryImpl
    @Inject
    constructor(
        private val userApi: UserApi,
        private val profileDraftDao: ProfileDraftDao,
        private val userDao: UserDao,
    ) : IUserRepository {
        override fun updateProfile(request: UserUpdateRequest): Flow<Resource<UserUpdateData>> =
            flow {
                emit(Resource.Loading)
                val response = userApi.updateProfile(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        emit(Resource.Success(body.data))
                    } else {
                        emit(Resource.Error(body?.message ?: "Unknown Error"))
                    }
                } else {
                    emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
                }
            }.catch { e ->
                emit(Resource.Error(e.message ?: "Unknown error"))
            }

        override fun updateProfilePicture(file: File): Flow<Resource<UserUpdateData>> =
            flow {
                emit(Resource.Loading)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

                val response = userApi.updateProfilePicture(body)
                if (response.isSuccessful) {
                    val resBody = response.body()
                    if (resBody?.success == true && resBody.data != null) {
                        emit(Resource.Success(resBody.data))
                    } else {
                        emit(Resource.Error(resBody?.message ?: "Unknown Error"))
                    }
                } else {
                    emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
                }
            }.catch { e ->
                emit(Resource.Error(e.message ?: "Unknown error"))
            }

        override fun getUserProfile(): Flow<Resource<UserUpdateData>> =
            flow {
                emit(Resource.Loading)
                val localProfile = userDao.getUserProfile().first()
                if (localProfile != null) {
                    try {
                        val data = Gson().fromJson(localProfile.dataJson, UserUpdateData::class.java)
                        emit(Resource.Success(data))
                    } catch (e: Exception) {
                        // Ignore parse error
                    }
                }

                try {
                    val response = userApi.getUserProfile()
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true && body.data != null) {
                            val profileEntity = UserProfileEntity(body.data.id, Gson().toJson(body.data))
                            userDao.clearUserProfile() // simple clear approach
                            userDao.insertUserProfile(profileEntity)
                            emit(Resource.Success(body.data))
                        } else {
                            if (localProfile == null) {
                                emit(Resource.Error(body?.message ?: "Unknown Error"))
                            }
                        }
                    } else {
                        if (localProfile == null) {
                            emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
                        }
                    }
                } catch (e: Exception) {
                    if (localProfile == null) {
                        emit(Resource.Error(e.message ?: "Unknown error"))
                    }
                }
            }

        override fun getProfileDraft(userId: String): Flow<ProfileDraftEntity?> = profileDraftDao.getDraft(userId)

        override suspend fun saveProfileDraft(draft: ProfileDraftEntity) {
            profileDraftDao.saveDraft(draft)
        }

        override suspend fun clearProfileDraft(userId: String) {
            profileDraftDao.deleteDraft(userId)
        }
    }
