package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.CheckInRecordEntity
import kotlinx.coroutines.flow.Flow

/** 打卡记录表访问：date 唯一索引 + REPLACE 策略 = 同日打卡幂等。 */
@Dao
interface CheckInRecordDao {

    @Query("SELECT * FROM check_in_record ORDER BY date DESC")
    fun observeAll(): Flow<List<CheckInRecordEntity>>

    @Query("SELECT date FROM check_in_record ORDER BY date ASC")
    fun observeAllDates(): Flow<List<String>>

    @Query("SELECT * FROM check_in_record WHERE date = :date ORDER BY createdAt DESC LIMIT 1")
    fun observeByDate(date: String): Flow<CheckInRecordEntity?>

    @Query("SELECT * FROM check_in_record WHERE date = :date ORDER BY createdAt DESC LIMIT 1")
    suspend fun getByDate(date: String): CheckInRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(record: CheckInRecordEntity)

    @androidx.room.Query("DELETE FROM check_in_record WHERE date = :date")
    suspend fun deleteByDate(date: String)
}
