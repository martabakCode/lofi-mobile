package com.loanfinancial.lofi.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface RegionApi {
    @GET
    suspend fun getProvinces(
        @Url url: String = "https://www.emsifa.com/api-wilayah-indonesia/api/provinces.json",
    ): List<ProvinceResponse>

    @GET("https://www.emsifa.com/api-wilayah-indonesia/api/regencies/{provinceId}.json")
    suspend fun getRegencies(
        @Path("provinceId") provinceId: String,
    ): List<RegencyResponse>

    @GET("https://www.emsifa.com/api-wilayah-indonesia/api/districts/{regencyId}.json")
    suspend fun getDistricts(
        @Path("regencyId") regencyId: String,
    ): List<DistrictResponse>

    @GET("https://www.emsifa.com/api-wilayah-indonesia/api/villages/{districtId}.json")
    suspend fun getVillages(
        @Path("districtId") districtId: String,
    ): List<VillageResponse>
}
