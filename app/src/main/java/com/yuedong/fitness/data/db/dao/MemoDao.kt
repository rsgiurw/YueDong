package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yuedong.fitness.data.db.entity.MemoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {

    @Query("SELECT * FROM memo WHERE deletedAt IS NULL ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memo WHERE deletedAt IS NOT NULL ORDER BY updatedAt DESC")
    fun observeDeleted(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memo WHERE id = :id")
    suspend fun byId(id: Long): MemoEntity?

    @Insert
    suspend fun insert(memo: MemoEntity): Long

    @Update
    suspend fun update(memo: MemoEntity)

    @Query("UPDATE memo SET deletedAt = :ts WHERE id = :id")
    suspend fun softDelete(id: Long, ts: Long)

    @Query("UPDATE memo SET deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM memo WHERE id = :id")
    suspend fun purge(id: Long)
}
