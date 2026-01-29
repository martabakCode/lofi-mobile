package com.loanfinancial.lofi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.loanfinancial.lofi.data.remote.api.DistrictResponse
import com.loanfinancial.lofi.data.remote.api.ProvinceResponse
import com.loanfinancial.lofi.data.remote.api.RegencyResponse
import com.loanfinancial.lofi.data.remote.api.VillageResponse

@Entity(tableName = "provinces")
data class ProvinceEntity(
    @PrimaryKey val id: String,
    val name: String,
) {
    fun toResponse() = ProvinceResponse(id, name)
}

fun ProvinceResponse.toEntity() = ProvinceEntity(id, name)

@Entity(tableName = "regencies")
data class RegencyEntity(
    @PrimaryKey val id: String,
    val provinceId: String,
    val name: String,
) {
    fun toResponse() = RegencyResponse(id, provinceId, name)
}

fun RegencyResponse.toEntity() = RegencyEntity(id, province_id, name)

@Entity(tableName = "districts")
data class DistrictEntity(
    @PrimaryKey val id: String,
    val regencyId: String,
    val name: String,
) {
    fun toResponse() = DistrictResponse(id, regencyId, name)
}

fun DistrictResponse.toEntity() = DistrictEntity(id, regency_id, name)

@Entity(tableName = "villages")
data class VillageEntity(
    @PrimaryKey val id: String,
    val districtId: String,
    val name: String,
) {
    fun toResponse() = VillageResponse(id, districtId, name)
}

fun VillageResponse.toEntity() = VillageEntity(id, district_id, name)
