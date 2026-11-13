package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.ExerciseProgressEntity
import kotlinx.coroutines.flow.Flow

/** 某天某动作的完成明细（投影行） */
data class ExerciseDoneInfo(
    val name: String,
    val sets: Int,
    val repsOrDuration: String,
    val done: Boolean
)

/** 动作完成进度 DAO。 */
@Dao
interface ExerciseProgressDao {

    @Query("SELECT * FROM exercise_progress")
    fun observeAll(): Flow<List<ExerciseProgressEntity>>

    @Query("SELECT * FROM exercise_progress WHERE date = :date")
    fun observeByDate(date: String): Flow<List<ExerciseProgressEntity>>

    /** 该日期的训练进度归属哪个计划（progress → plan_exercise → plan_day 反查） */
    @Query(
        "SELECT pd.planId FROM exercise_progress ep " +
            "JOIN plan_exercise pe ON ep.planExerciseId = pe.id " +
            "JOIN plan_day pd ON pe.planDayId = pd.id " +
            "WHERE ep.date = :date LIMIT 1"
    )
    fun observePlanIdForDate(date: String): Flow<Long?>

    /** 清理未来日期的进度残留（调试日期快进/回退后产生的脏数据） */
    @Query("DELETE FROM exercise_progress WHERE date > :today")
    suspend fun deleteAfter(today: String)

    /** 某天的动作明细（名称 + 组次 + 完成态，按勾选顺序） */
    @Query(
        "SELECT pe.name AS name, pe.sets AS sets, pe.repsOrDuration AS repsOrDuration, ep.done AS done " +
            "FROM exercise_progress ep " +
            "JOIN plan_exercise pe ON ep.planExerciseId = pe.id " +
            "WHERE ep.date = :date ORDER BY ep.id"
    )
    suspend fun exerciseDetailsForDate(date: String): List<ExerciseDoneInfo>

    /** 某天实际完成的动作名（按勾选顺序） */
    @Query(
        "SELECT pe.name FROM exercise_progress ep " +
            "JOIN plan_exercise pe ON ep.planExerciseId = pe.id " +
            "WHERE ep.date = :date AND ep.done = 1 ORDER BY ep.id"
    )
    suspend fun doneExerciseNamesForDate(date: String): List<String>

    @Query("DELETE FROM exercise_progress WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ExerciseProgressEntity)
}
