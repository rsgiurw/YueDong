package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.PlanDayEntity
import kotlinx.coroutines.flow.Flow

/** 计划日安排表访问。 */
@Dao
interface PlanDayDao {

    @Query("SELECT * FROM plan_day WHERE planId = :planId ORDER BY orderIndex ASC")
    fun observeByPlan(planId: Long): Flow<List<PlanDayEntity>>

    @Query("SELECT * FROM plan_day WHERE planId = :planId ORDER BY orderIndex ASC")
    suspend fun getByPlan(planId: Long): List<PlanDayEntity>

    @Insert
    suspend fun insert(day: PlanDayEntity): Long

    @Query("DELETE FROM plan_day WHERE planId = :planId")
    suspend fun deleteByPlan(planId: Long)
}
