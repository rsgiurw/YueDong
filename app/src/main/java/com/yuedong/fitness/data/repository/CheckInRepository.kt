package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.CheckInRecordDao
import com.yuedong.fitness.data.db.entity.CheckInRecordEntity
import kotlinx.coroutines.flow.Flow

/** 打卡仓库：今日页写入、记录页统计都从这里走。 */
interface CheckInRepository {
    fun observeAll(): Flow<List<CheckInRecordEntity>>
    fun observeAllDates(): Flow<List<String>>
    fun observeByDate(date: String): Flow<CheckInRecordEntity?>
    suspend fun getByDate(date: String): CheckInRecordEntity?
    suspend fun save(record: CheckInRecordEntity)
    suspend fun deleteByDate(date: String)
}

class CheckInRepositoryImpl(private val dao: CheckInRecordDao) : CheckInRepository {
    override fun observeAll(): Flow<List<CheckInRecordEntity>> = dao.observeAll()

    override fun observeAllDates(): Flow<List<String>> = dao.observeAllDates()

    override fun observeByDate(date: String): Flow<CheckInRecordEntity?> = dao.observeByDate(date)

    override suspend fun getByDate(date: String): CheckInRecordEntity? = dao.getByDate(date)

    override suspend fun deleteByDate(date: String) = dao.deleteByDate(date)

    override suspend fun save(record: CheckInRecordEntity) {
        dao.save(record)
    }
}
