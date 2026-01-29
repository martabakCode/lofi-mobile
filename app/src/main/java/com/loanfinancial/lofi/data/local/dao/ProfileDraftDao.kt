package com.loanfinancial.lofi.data.local.dao

import androidx.room.*
import com.loanfinancial.lofi.data.model.entity.ProfileDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDraftDao {
    @Query("SELECT * FROM profile_drafts WHERE userId = :userId")
    fun getDraft(userId: String): Flow<ProfileDraftEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: ProfileDraftEntity)

    @Query("DELETE FROM profile_drafts WHERE userId = :userId")
    suspend fun deleteDraft(userId: String)
}
