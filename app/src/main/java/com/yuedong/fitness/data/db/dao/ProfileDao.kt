package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.yuedong.fitness.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/** 用户资料表访问：单行读取与覆盖保存。 */
@Dao
interface ProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Upsert
    suspend fun upsert(profile: UserProfileEntity)
}
