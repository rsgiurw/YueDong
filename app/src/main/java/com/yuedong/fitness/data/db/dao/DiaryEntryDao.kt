package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

/** 健身日记表访问：date 唯一索引 + REPLACE 策略 = 每日一条、可覆盖修改。 */
@Dao
interface DiaryEntryDao {

    @Query("SELECT * FROM diary_entry ORDER BY date DESC")
    fun observeAll(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entry WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<DiaryEntryEntity?>

    @Query("SELECT * FROM diary_entry WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DiaryEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entry: DiaryEntryEntity)
}
