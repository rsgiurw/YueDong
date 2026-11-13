package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.ExerciseFavoriteEntity
import kotlinx.coroutines.flow.Flow

/** 动作收藏 DAO。 */
@Dao
interface ExerciseFavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: ExerciseFavoriteEntity)

    @Query("DELETE FROM exercise_favorite WHERE exerciseName = :name")
    suspend fun deleteByName(name: String)

    @Query("SELECT EXISTS(SELECT 1 FROM exercise_favorite WHERE exerciseName = :name)")
    fun observeIsFavorite(name: String): Flow<Boolean>

    @Query("SELECT * FROM exercise_favorite ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ExerciseFavoriteEntity>>
}
