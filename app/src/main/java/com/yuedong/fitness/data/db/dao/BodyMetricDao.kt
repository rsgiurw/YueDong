package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.BodyMetricEntity
import kotlinx.coroutines.flow.Flow

/** 身体数据表访问：date 唯一索引 + REPLACE 策略 = 同日记录覆盖更新。 */
@Dao
interface BodyMetricDao {

    @Query("SELECT * FROM body_metric ORDER BY date ASC")
    fun observeAll(): Flow<List<BodyMetricEntity>>

    @Query("SELECT * FROM body_metric ORDER BY date DESC LIMIT 1")
    fun observeLatest(): Flow<BodyMetricEntity?>

    @Query("SELECT * FROM body_metric WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): BodyMetricEntity?

    @Query("DELETE FROM body_metric WHERE date > :date")
    suspend fun deleteAfter(date: String)

    @Query("DELETE FROM body_metric")
    suspend fun deleteAllWeights()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(metric: BodyMetricEntity)
}
