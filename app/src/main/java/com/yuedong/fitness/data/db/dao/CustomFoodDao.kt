package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.CustomFoodEntity
import kotlinx.coroutines.flow.Flow

/** 自定义食物表访问:同名覆盖保存(幂等);删除为软删,进回收站可恢复。 */
@Dao
interface CustomFoodDao {

    @Query("SELECT * FROM custom_food WHERE deletedAt IS NULL ORDER BY name ASC")
    fun observeAll(): Flow<List<CustomFoodEntity>>

    @Query("SELECT * FROM custom_food WHERE deletedAt IS NOT NULL ORDER BY name ASC")
    fun observeDeleted(): Flow<List<CustomFoodEntity>>

    @Query("SELECT * FROM custom_food WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): CustomFoodEntity?

    @Query("UPDATE custom_food SET deletedAt = :ts WHERE name = :name")
    suspend fun softDeleteByName(name: String, ts: Long)

    @Query("UPDATE custom_food SET deletedAt = NULL WHERE name = :name")
    suspend fun restoreByName(name: String)

    @Query("DELETE FROM custom_food WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(food: CustomFoodEntity)
}
