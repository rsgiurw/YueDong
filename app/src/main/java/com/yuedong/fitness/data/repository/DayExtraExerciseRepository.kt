package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.DayExtraExerciseDao
import com.yuedong.fitness.data.db.entity.DayExtraExerciseEntity
import kotlinx.coroutines.flow.Flow

interface DayExtraExerciseRepository {
    fun observeByDate(date: String): Flow<List<DayExtraExerciseEntity>>
    suspend fun add(date: String, name: String, setsText: String)
    suspend fun delete(id: Long)
}

class DayExtraExerciseRepositoryImpl(
    private val dao: DayExtraExerciseDao
) : DayExtraExerciseRepository {

    override fun observeByDate(date: String): Flow<List<DayExtraExerciseEntity>> =
        dao.observeByDate(date)

    override suspend fun add(date: String, name: String, setsText: String) {
        dao.insert(
            DayExtraExerciseEntity(
                date = date,
                name = name,
                setsText = setsText,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun delete(id: Long) = dao.delete(id)
}
