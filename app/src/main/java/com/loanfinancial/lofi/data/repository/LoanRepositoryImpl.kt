package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.datasource.LoanLocalDataSource
import com.loanfinancial.lofi.data.model.entity.toDomain
import com.loanfinancial.lofi.data.model.entity.toEntity
import com.loanfinancial.lofi.data.remote.datasource.LoanRemoteDataSource
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.toDomain
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import kotlinx.coroutines.flow.Flow
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
            page: Int,
            size: Int,
            sort: String,
        ): Flow<Resource<List<Loan>>> =
            flow {
                emit(Resource.Loading)

                // Step 1: Emit cached data from local DB
                val cachedLoans = localDataSource.getAllLoans().first()
                if (cachedLoans.isNotEmpty()) {
                    emit(Resource.Success(cachedLoans.map { it.toDomain() }))
                }

                try {
                    // Step 2: Fetch from remote
                    val response = remoteDataSource.getMyLoans(page, size, sort)
                    if (response.isSuccessful) {
                        val baseResponse = response.body()
                        if (baseResponse?.success == true) {
                            val remoteItems = baseResponse.data?.items ?: emptyList()
                            val domainLoans = remoteItems.map { it.toDomain() }

                            // Step 3: Update local DB
                            localDataSource.deleteAllLoans()
                            localDataSource.insertLoans(domainLoans.map { it.toEntity() })
                        } else {
                            emit(Resource.Error(baseResponse?.message ?: "Unknown Error"))
                        }
                    } else {
                        emit(Resource.Error("Network Error: ${response.code()}"))
                    }
                } catch (e: Exception) {
                    emit(Resource.Error(e.localizedMessage ?: "Connection Error"))
                }

                // Step 4: Final emit from local DB (source of truth)
                emitAll(
                    localDataSource.getAllLoans().map { entities ->
                        Resource.Success(entities.map { it.toDomain() })
                    },
                )
            }
    }
