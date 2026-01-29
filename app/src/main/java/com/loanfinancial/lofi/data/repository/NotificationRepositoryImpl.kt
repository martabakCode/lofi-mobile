package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.database.AppDatabase
import com.loanfinancial.lofi.data.model.dto.NotificationResponse
import com.loanfinancial.lofi.data.model.entity.toEntity
import com.loanfinancial.lofi.data.remote.datasource.NotificationRemoteDataSource
import com.loanfinancial.lofi.domain.repository.INotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NotificationRepositoryImpl
    @Inject
    constructor(
        private val remoteDataSource: NotificationRemoteDataSource,
        private val database: AppDatabase,
    ) : INotificationRepository {
        override fun getNotifications(): Flow<Resource<List<NotificationResponse>>> =
            flow {
                emit(Resource.Loading)
                val dao = database.notificationDao()

                // Emit local data first
                val localData = dao.getNotifications().first()
                if (localData.isNotEmpty()) {
                    emit(Resource.Success(localData.map { it.toDto() }))
                }

                try {
                    val response = remoteDataSource.getNotifications()
                    if (response.isSuccessful) {
                        val baseResponse = response.body()
                        if (baseResponse?.success == true) {
                            val data = baseResponse.data ?: emptyList()

                            // Save to local
                            dao.clearAll()
                            dao.insertAll(data.map { it.toEntity() })

                            // Fetch updated local data
                            val updatedLocal = dao.getNotifications().first()
                            emit(Resource.Success(updatedLocal.map { it.toDto() }))
                        } else {
                            if (localData.isEmpty()) {
                                emit(Resource.Error(baseResponse?.message ?: "Unknown Error"))
                            }
                        }
                    } else {
                        if (localData.isEmpty()) {
                            emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
                        }
                    }
                } catch (e: Exception) {
                    if (localData.isEmpty()) {
                        emit(Resource.Error(e.message ?: "Unknown Error"))
                    }
                }
            }
    }
