package com.loanfinancial.lofi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.loanfinancial.lofi.data.model.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Query("DELETE FROM users")
    suspend fun clearUser()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: com.loanfinancial.lofi.data.model.entity.UserProfileEntity)

    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getUserProfile(): Flow<com.loanfinancial.lofi.data.model.entity.UserProfileEntity?>

    @Query("DELETE FROM user_profiles")
    suspend fun clearUserProfile()
}
