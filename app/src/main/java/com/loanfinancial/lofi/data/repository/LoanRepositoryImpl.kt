package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.datasource.LoanLocalDataSource
import com.loanfinancial.lofi.data.model.dto.CreateLoanRequest
import com.loanfinancial.lofi.data.model.entity.toDomain
import com.loanfinancial.lofi.data.model.entity.toEntity
import com.loanfinancial.lofi.data.remote.datasource.LoanRemoteDataSource
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.toDomain
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoanRepositoryImpl
    @Inject
    constructor(
        private val remoteDataSource: LoanRemoteDataSource,
        private val localDataSource: LoanLocalDataSource,
    ) : ILoanRepository {
        override fun getMyLoans(
            userId: String,
            page: Int,
            size: Int,
            sort: String,
        ): Flow<Resource<List<Loan>>> =
            flow {
                emit(Resource.Loading)

                try {
                    // Step 1: Fetch from remote
                    val response = remoteDataSource.getMyLoans(page, size, sort)
                    if (response.isSuccessful) {
                        val baseResponse = response.body()
                        if (baseResponse?.success == true) {
                            val remoteItems = baseResponse.data?.items ?: emptyList()
                            val domainLoans = remoteItems.map { it.toDomain() }

                            // Step 2: Update local DB atomically using Transaction
                            localDataSource.clearAndInsertByUser(userId, domainLoans.map { it.toEntity(userId) })
                        }
                    }
                } catch (e: Exception) {
                    // Handled gracefully - Room will continue showing cached data
                }

                // Step 3: Continuous emission from local DB (source of truth)
                emitAll(
                    localDataSource.getLoansByUser(userId)
                        .map { entities ->
                            Resource.Success(entities.map { it.toDomain() })
                        }
                )
            }.distinctUntilChanged()

        override fun getLoanDetail(id: String): Flow<Resource<Loan>> =
            flow {
                emit(Resource.Loading)

                try {
                    val response = remoteDataSource.getLoanDetail(id)
                    if (response.isSuccessful) {
                        val baseResponse = response.body()
                        if (baseResponse?.success == true) {
                            val loan = baseResponse.data?.toDomain()
                            if (loan != null) {
                                emit(Resource.Success(loan))
                            } else {
                                emit(Resource.Error("Loan data is null"))
                            }
                        } else {
                            emit(Resource.Error(baseResponse?.message ?: "Unknown Error"))
                        }
                    } else {
                        emit(Resource.Error("Network Error: ${response.code()}"))
                    }
                } catch (e: Exception) {
                    emit(Resource.Error(e.localizedMessage ?: "Connection Error"))
                }
            }

        override fun createLoan(request: CreateLoanRequest): Flow<Resource<Loan>> =
            flow {
                emit(Resource.Loading)

                val response = remoteDataSource.createLoan(request)
                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    if (baseResponse?.success == true) {
                        val loan = baseResponse.data?.toDomain()
                        if (loan != null) {
                            emit(Resource.Success(loan))
                        } else {
                            emit(Resource.Error("Loan data is null"))
                        }
                    } else {
                        emit(Resource.Error(baseResponse?.message ?: "Unknown Error"))
                    }
                } else {
                    emit(Resource.Error("Network Error: ${response.code()}"))
                }
            }.catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "Connection Error"))
            }

        override fun submitLoan(id: String): Flow<Resource<Loan>> =
            flow {
                emit(Resource.Loading)

                val response = remoteDataSource.submitLoan(id)
                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    if (baseResponse?.success == true) {
                        val loan = baseResponse.data?.toDomain()
                        if (loan != null) {
                            emit(Resource.Success(loan))
                        } else {
                            emit(Resource.Error("Loan data is null"))
                        }
                    } else {
                        emit(Resource.Error(baseResponse?.message ?: "Unknown Error"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    emit(Resource.Error("Server Error ${response.code()}: ${errorBody ?: response.message()}"))
                }
            }.catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "Connection Error"))
            }
    }
