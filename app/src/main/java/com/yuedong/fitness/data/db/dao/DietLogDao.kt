package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.DietLogEntity
import kotlinx.coroutines.flow.Flow

/** 饮食记录表访问。 */
@Dao
interface DietLogDao {

    @Query("SELECT * FROM diet_log WHERE date = :date ORDER BY createdAt ASC")
    fun observeByDate(date: String): Flow<List<DietLogEntity>>

    @Query("SELECT * FROM diet_log WHERE date BETWEEN :start AND :end ORDER BY date ASC, createdAt ASC")
    fun observeBetween(start: String, end: String): Flow<List<DietLogEntity>>

    @Insert
    suspend fun insert(entry: DietLogEntity)

    @Query("DELETE FROM diet_log WHERE id = :id")
    suspend fun deleteById(id: Long)
}