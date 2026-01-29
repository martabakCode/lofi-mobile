package com.loanfinancial.lofi.domain.repository

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.remote.api.DistrictResponse
import com.loanfinancial.lofi.data.remote.api.ProvinceResponse
import com.loanfinancial.lofi.data.remote.api.RegencyResponse
import com.loanfinancial.lofi.data.remote.api.VillageResponse
import kotlinx.coroutines.flow.Flow

interface IRegionRepository {
    fun getProvinces(): Flow<Resource<List<ProvinceResponse>>>

    fun getRegencies(provinceId: String): Flow<Resource<List<RegencyResponse>>>

    fun getDistricts(regencyId: String): Flow<Resource<List<DistrictResponse>>>

    fun getVillages(districtId: String): Flow<Resource<List<VillageResponse>>>
}
