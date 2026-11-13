package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.ExerciseFavoriteDao
import com.yuedong.fitness.data.db.entity.ExerciseFavoriteEntity
import kotlinx.coroutines.flow.Flow

interface ExerciseFavoriteRepository {
    fun observeAll(): Flow<List<ExerciseFavoriteEntity>>
    fun observeIsFavorite(name: String): Flow<Boolean>
    suspend fun addFavorite(name: String)
    suspend fun removeFavorite(name: String)
}

class ExerciseFavoriteRepositoryImpl(private val dao: ExerciseFavoriteDao) : ExerciseFavoriteRepository {
    override fun observeAll(): Flow<List<ExerciseFavoriteEntity>> = dao.observeAll()
    override fun observeIsFavorite(name: String): Flow<Boolean> = dao.observeIsFavorite(name)
    override suspend fun addFavorite(name: String) =
        dao.insert(ExerciseFavoriteEntity(exerciseName = name, createdAt = System.currentTimeMillis()))

    override suspend fun removeFavorite(name: String) = dao.deleteByName(name)
}
