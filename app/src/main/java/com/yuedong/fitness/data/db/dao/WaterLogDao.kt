package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    /** 某日饮水总量（ml），无记录时为 0。 */
    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_log WHERE date = :date")
    fun observeTotalForDate(date: String): Flow<Int>

    @Insert
    suspend fun insert(entry: WaterLogEntity)
}
