package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.ExerciseProgressDao
import com.yuedong.fitness.data.db.entity.ExerciseProgressEntity
import kotlinx.coroutines.flow.Flow

interface ExerciseProgressRepository {
    fun observeByDate(date: String): Flow<List<ExerciseProgressEntity>>
    fun observeAll(): Flow<List<ExerciseProgressEntity>>
    suspend fun deleteFutureRows(realToday: String)
    suspend fun deleteByDate(date: String)
    fun observePlanIdForDate(date: String): Flow<Long?>
    suspend fun setDone(date: String, planExerciseId: Long, done: Boolean)
    suspend fun doneExerciseNamesForDate(date: String): List<String>
    suspend fun exerciseDetailsForDate(date: String): List<com.yuedong.fitness.data.db.dao.ExerciseDoneInfo>
}

class ExerciseProgressRepositoryImpl(
    private val dao: ExerciseProgressDao
) : ExerciseProgressRepository {

    override fun observePlanIdForDate(date: String): Flow<Long?> =
        dao.observePlanIdForDate(date)

    override fun observeAll(): Flow<List<ExerciseProgressEntity>> = dao.observeAll()

    override suspend fun deleteFutureRows(realToday: String) = dao.deleteAfter(realToday)

    override suspend fun deleteByDate(date: String) = dao.deleteByDate(date)

    override fun observeByDate(date: String): Flow<List<ExerciseProgressEntity>> =
        dao.observeByDate(date)

    override suspend fun doneExerciseNamesForDate(date: String): List<String> =
        dao.doneExerciseNamesForDate(date)

    override suspend fun exerciseDetailsForDate(date: String): List<com.yuedong.fitness.data.db.dao.ExerciseDoneInfo> =
        dao.exerciseDetailsForDate(date)

    override suspend fun setDone(date: String, planExerciseId: Long, done: Boolean) {
        dao.upsert(
            ExerciseProgressEntity(date = date, planExerciseId = planExerciseId, done = done)
        )
    }
}
