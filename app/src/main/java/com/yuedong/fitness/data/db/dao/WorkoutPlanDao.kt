package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yuedong.fitness.data.db.entity.WorkoutPlanEntity
import kotlinx.coroutines.flow.Flow

/** 训练计划表访问：列表/激活计划/写入。 */
@Dao
interface WorkoutPlanDao {

    @Query("UPDATE workout_plan SET isFavorite = :fav WHERE id = :planId")
    suspend fun setFavorite(planId: Long, fav: Boolean)

    @Query("UPDATE workout_plan SET isPinned = :pinned WHERE id = :planId")
    suspend fun setPinned(planId: Long, pinned: Boolean)

    @Query("SELECT * FROM workout_plan WHERE deletedAt IS NULL ORDER BY source ASC, id ASC")
    fun observeAll(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plan WHERE deletedAt IS NOT NULL ORDER BY name ASC")
    fun observeDeleted(): Flow<List<WorkoutPlanEntity>>

    @Query("UPDATE workout_plan SET deletedAt = :ts WHERE id = :id")
    suspend fun softDeleteById(id: Long, ts: Long)

    @Query("UPDATE workout_plan SET deletedAt = NULL WHERE id = :id")
    suspend fun restoreById(id: Long)

    @Query("SELECT * FROM workout_plan WHERE deletedAt IS NULL AND isActive = 1 LIMIT 1")
    fun observeActive(): Flow<WorkoutPlanEntity?>

    @Query("SELECT * FROM workout_plan WHERE deletedAt IS NULL AND id = :planId")
    suspend fun getById(planId: Long): WorkoutPlanEntity?

    @Query("SELECT * FROM workout_plan WHERE deletedAt IS NULL AND id = :planId")
    fun observeById(planId: Long): Flow<WorkoutPlanEntity?>

    @Query("SELECT COUNT(*) FROM workout_plan WHERE source = 'BUILTIN'")
    suspend fun countBuiltin(): Int

    @Insert
    suspend fun insert(plan: WorkoutPlanEntity): Long

    @Update
    suspend fun update(plan: WorkoutPlanEntity)

    @Query("UPDATE workout_plan SET isActive = 0 WHERE isActive = 1")
    suspend fun clearAllActive()

    @Query("UPDATE workout_plan SET isActive = 1 WHERE id = :planId")
    suspend fun markActive(planId: Long)

    @Query("DELETE FROM workout_plan WHERE id = :planId")
    suspend fun deleteById(planId: Long)
}
