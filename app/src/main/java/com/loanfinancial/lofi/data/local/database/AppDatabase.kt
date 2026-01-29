package com.loanfinancial.lofi.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.loanfinancial.lofi.data.local.dao.LoanDao
import com.loanfinancial.lofi.data.local.dao.NotificationDao
import com.loanfinancial.lofi.data.local.dao.ProductDao
import com.loanfinancial.lofi.data.local.dao.ProfileDraftDao
import com.loanfinancial.lofi.data.local.dao.RegionDao
import com.loanfinancial.lofi.data.local.dao.UserDao
import com.loanfinancial.lofi.data.model.entity.DistrictEntity
import com.loanfinancial.lofi.data.model.entity.LoanEntity
import com.loanfinancial.lofi.data.model.entity.NotificationEntity
import com.loanfinancial.lofi.data.model.entity.ProductEntity
import com.loanfinancial.lofi.data.model.entity.ProfileDraftEntity
import com.loanfinancial.lofi.data.model.entity.ProvinceEntity
import com.loanfinancial.lofi.data.model.entity.RegencyEntity
import com.loanfinancial.lofi.data.model.entity.UserEntity
import com.loanfinancial.lofi.data.model.entity.UserProfileEntity
import com.loanfinancial.lofi.data.model.entity.VillageEntity

@Database(
    entities = [
        LoanEntity::class,
        UserEntity::class,
        ProfileDraftEntity::class,
        NotificationEntity::class,
        ProductEntity::class,
        ProvinceEntity::class,
        RegencyEntity::class,
        DistrictEntity::class,
        VillageEntity::class,
        UserProfileEntity::class,
    ],
    version = 8,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun loanDao(): LoanDao

    abstract fun userDao(): UserDao

    abstract fun profileDraftDao(): ProfileDraftDao

    abstract fun notificationDao(): NotificationDao

    abstract fun productDao(): ProductDao

    abstract fun regionDao(): RegionDao
}
