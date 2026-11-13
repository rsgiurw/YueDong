package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.WaterLogDao
import com.yuedong.fitness.data.db.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow

/** 饮水记录仓库（模块契约见设计包 §8 DataLayer）。 */
interface WaterRepository {
    fun observeTotalForDate(date: String): Flow<Int>
    suspend fun add(date: String, amountMl: Int)
}

class WaterRepositoryImpl(private val dao: WaterLogDao) : WaterRepository {
    override fun observeTotalForDate(date: String): Flow<Int> = dao.observeTotalForDate(date)

    override suspend fun add(date: String, amountMl: Int) {
        dao.insert(
            WaterLogEntity(date = date, amountMl = amountMl, createdAt = System.currentTimeMillis())
        )
    }
}
