package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.DayExtraExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayExtraExerciseDao {

    @Query("SELECT * FROM day_extra_exercise WHERE date = :date ORDER BY createdAt ASC")
    fun observeByDate(date: String): Flow<List<DayExtraExerciseEntity>>

    @Insert
    suspend fun insert(entry: DayExtraExerciseEntity): Long

    @Query("DELETE FROM day_extra_exercise WHERE id = :id")
    suspend fun delete(id: Long)
}
