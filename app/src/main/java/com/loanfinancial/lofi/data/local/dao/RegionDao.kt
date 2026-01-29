package com.loanfinancial.lofi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.loanfinancial.lofi.data.model.entity.DistrictEntity
import com.loanfinancial.lofi.data.model.entity.ProvinceEntity
import com.loanfinancial.lofi.data.model.entity.RegencyEntity
import com.loanfinancial.lofi.data.model.entity.VillageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionDao {
    @Query("SELECT * FROM provinces")
    fun getProvinces(): Flow<List<ProvinceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvinces(provinces: List<ProvinceEntity>)

    @Query("SELECT * FROM regencies WHERE provinceId = :provinceId")
    fun getRegencies(provinceId: String): Flow<List<RegencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegencies(regencies: List<RegencyEntity>)

    @Query("SELECT * FROM districts WHERE regencyId = :regencyId")
    fun getDistricts(regencyId: String): Flow<List<DistrictEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDistricts(districts: List<DistrictEntity>)

    @Query("SELECT * FROM villages WHERE districtId = :districtId")
    fun getVillages(districtId: String): Flow<List<VillageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVillages(villages: List<VillageEntity>)
}
