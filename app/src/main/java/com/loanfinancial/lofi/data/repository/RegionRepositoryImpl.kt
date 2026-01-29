package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.database.AppDatabase
import com.loanfinancial.lofi.data.model.entity.toEntity
import com.loanfinancial.lofi.data.remote.api.DistrictResponse
import com.loanfinancial.lofi.data.remote.api.ProvinceResponse
import com.loanfinancial.lofi.data.remote.api.RegencyResponse
import com.loanfinancial.lofi.data.remote.api.RegionApi
import com.loanfinancial.lofi.data.remote.api.VillageResponse
import com.loanfinancial.lofi.domain.repository.IRegionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RegionRepositoryImpl
    @Inject
    constructor(
        private val regionApi: RegionApi,
        private val database: AppDatabase,
    ) : IRegionRepository {
        override fun getProvinces(): Flow<Resource<List<ProvinceResponse>>> =
            flow {
                emit(Resource.Loading)
                val dao = database.regionDao()
                val localData = dao.getProvinces().first()
                if (localData.isNotEmpty()) {
                    emit(Resource.Success(localData.map { it.toResponse() }))
                }

                try {
                    val response = regionApi.getProvinces()
                    dao.insertProvinces(response.map { it.toEntity() })
                    val updatedLocal = dao.getProvinces().first()
                    emit(Resource.Success(updatedLocal.map { it.toResponse() }))
                } catch (e: Exception) {
                    if (localData.isEmpty()) {
                        emit(Resource.Error(e.message ?: "An unknown error occurred"))
                    }
                }
            }

        override fun getRegencies(provinceId: String): Flow<Resource<List<RegencyResponse>>> =
            flow {
                emit(Resource.Loading)
                val dao = database.regionDao()
                val localData = dao.getRegencies(provinceId).first()
                if (localData.isNotEmpty()) {
                    emit(Resource.Success(localData.map { it.toResponse() }))
                }

                try {
                    val response = regionApi.getRegencies(provinceId)
                    dao.insertRegencies(response.map { it.toEntity() })
                    val updatedLocal = dao.getRegencies(provinceId).first()
                    emit(Resource.Success(updatedLocal.map { it.toResponse() }))
                } catch (e: Exception) {
                    if (localData.isEmpty()) {
                        emit(Resource.Error(e.message ?: "An unknown error occurred"))
                    }
                }
            }

        override fun getDistricts(regencyId: String): Flow<Resource<List<DistrictResponse>>> =
            flow {
                emit(Resource.Loading)
                val dao = database.regionDao()
                val localData = dao.getDistricts(regencyId).first()
                if (localData.isNotEmpty()) {
                    emit(Resource.Success(localData.map { it.toResponse() }))
                }

                try {
                    val response = regionApi.getDistricts(regencyId)
                    dao.insertDistricts(response.map { it.toEntity() })
                    val updatedLocal = dao.getDistricts(regencyId).first()
                    emit(Resource.Success(updatedLocal.map { it.toResponse() }))
                } catch (e: Exception) {
                    if (localData.isEmpty()) {
                        emit(Resource.Error(e.message ?: "An unknown error occurred"))
                    }
                }
            }

        override fun getVillages(districtId: String): Flow<Resource<List<VillageResponse>>> =
            flow {
                emit(Resource.Loading)
                val dao = database.regionDao()
                val localData = dao.getVillages(districtId).first()
                if (localData.isNotEmpty()) {
                    emit(Resource.Success(localData.map { it.toResponse() }))
                }

                try {
                    val response = regionApi.getVillages(districtId)
                    dao.insertVillages(response.map { it.toEntity() })
                    val updatedLocal = dao.getVillages(districtId).first()
                    emit(Resource.Success(updatedLocal.map { it.toResponse() }))
                } catch (e: Exception) {
                    if (localData.isEmpty()) {
                        emit(Resource.Error(e.message ?: "An unknown error occurred"))
                    }
                }
            }
    }
