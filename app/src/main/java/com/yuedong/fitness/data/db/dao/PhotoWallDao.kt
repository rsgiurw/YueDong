package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.PhotoWallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoWallDao {

    @Query("SELECT * FROM photo_wall WHERE deletedAt IS NULL ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<PhotoWallEntity>>

    @Query("SELECT * FROM photo_wall WHERE deletedAt IS NOT NULL ORDER BY date DESC, createdAt DESC")
    fun observeDeleted(): Flow<List<PhotoWallEntity>>

    @Insert
    suspend fun insert(entry: PhotoWallEntity): Long

    @Query("SELECT * FROM photo_wall WHERE id = :id")
    suspend fun byId(id: Long): PhotoWallEntity?

    @Query("UPDATE photo_wall SET deletedAt = :ts WHERE id = :id")
    suspend fun softDelete(id: Long, ts: Long)

    @Query("UPDATE photo_wall SET deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM photo_wall WHERE id = :id")
    suspend fun delete(id: Long)
}
