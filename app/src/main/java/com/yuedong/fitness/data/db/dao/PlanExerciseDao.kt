package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.PlanExerciseEntity
import kotlinx.coroutines.flow.Flow

/** 动作明细表访问。 */
@Dao
interface PlanExerciseDao {

    @Query(
        "SELECT plan_exercise.* FROM plan_exercise " +
            "INNER JOIN plan_day ON plan_exercise.planDayId = plan_day.id " +
            "WHERE plan_day.planId = :planId " +
            "ORDER BY plan_day.orderIndex ASC, plan_exercise.id ASC"
    )
    fun observeByPlan(planId: Long): Flow<List<PlanExerciseEntity>>

    @Query("SELECT * FROM plan_exercise WHERE planDayId = :planDayId ORDER BY id ASC")
    fun observeByDay(planDayId: Long): Flow<List<PlanExerciseEntity>>

    @Query("SELECT * FROM plan_exercise WHERE planDayId = :planDayId ORDER BY id ASC")
    suspend fun getByDay(planDayId: Long): List<PlanExerciseEntity>

    @Query(
        "SELECT plan_exercise.* FROM plan_exercise " +
            "INNER JOIN plan_day ON plan_exercise.planDayId = plan_day.id " +
            "WHERE plan_day.planId = :planId " +
            "ORDER BY plan_day.orderIndex ASC, plan_exercise.id ASC"
    )
    suspend fun getByPlan(planId: Long): List<PlanExerciseEntity>

    @Insert
    suspend fun insertAll(exercises: List<PlanExerciseEntity>)
}
