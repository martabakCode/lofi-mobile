package com.loanfinancial.lofi.data.remote.api

data class ProvinceResponse(
    val id: String,
    val name: String,
)

data class RegencyResponse(
    val id: String,
    val province_id: String,
    val name: String,
)

data class DistrictResponse(
    val id: String,
    val regency_id: String,
    val name: String,
)

data class VillageResponse(
    val id: String,
    val district_id: String,
    val name: String,
)
