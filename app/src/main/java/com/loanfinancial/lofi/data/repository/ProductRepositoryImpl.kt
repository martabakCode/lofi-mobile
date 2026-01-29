package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.database.AppDatabase
import com.loanfinancial.lofi.data.model.dto.ProductDto
import com.loanfinancial.lofi.data.model.entity.toEntity
import com.loanfinancial.lofi.data.remote.api.LoanProductApi
import com.loanfinancial.lofi.domain.repository.IProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProductRepositoryImpl
    @Inject
    constructor(
        private val api: LoanProductApi,
        private val database: AppDatabase,
    ) : IProductRepository {
        override fun getProducts(): Flow<Resource<List<ProductDto>>> =
            flow {
                emit(Resource.Loading)
                val dao = database.productDao()

                // Local
                val localData = dao.getProducts().first()
                if (localData.isNotEmpty()) {
                    emit(Resource.Success(localData.map { it.toDto() }))
                }

                try {
                    val response = api.getProducts(true, 50)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.success) {
                            val remoteData = body.data?.items ?: emptyList()

                            dao.clearAll()
                            dao.insertAll(remoteData.map { it.toEntity() })

                            val updatedLocal = dao.getProducts().first()
                            emit(Resource.Success(updatedLocal.map { it.toDto() }))
                        } else {
                            if (localData.isEmpty()) {
                                emit(Resource.Error(body?.message ?: "Unknown error"))
                            }
                        }
                    } else {
                        if (localData.isEmpty()) {
                            emit(Resource.Error("Error ${response.code()}: ${response.message()}"))
                        }
                    }
                } catch (e: Exception) {
                    if (localData.isEmpty()) {
                        emit(Resource.Error(e.message ?: "Network error"))
                    }
                }
            }
    }
