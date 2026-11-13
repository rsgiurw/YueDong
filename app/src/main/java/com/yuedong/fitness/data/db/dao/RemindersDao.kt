package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yuedong.fitness.data.db.entity.RemindersEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RemindersDao {

    @Query("SELECT * FROM reminders ORDER BY hour ASC, minute ASC")
    fun observeAll(): Flow<List<RemindersEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun byId(id: Long): RemindersEntity?

    @Insert
    suspend fun insert(entry: RemindersEntity): Long

    @Update
    suspend fun update(entry: RemindersEntity)

    @Query("UPDATE reminders SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: Long)
}
