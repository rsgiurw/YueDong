package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.BodyMetricDao
import com.yuedong.fitness.data.db.entity.BodyMetricEntity
import kotlinx.coroutines.flow.Flow

/** 身体数据仓库：今日页速记、记录页趋势、我的页进度都从这里走。 */
interface BodyMetricRepository {
    fun observeAll(): Flow<List<BodyMetricEntity>>
    fun observeLatest(): Flow<BodyMetricEntity?>
    suspend fun getByDate(date: String): BodyMetricEntity?
    suspend fun save(metric: BodyMetricEntity)

    /**
     * 保存"当前体重"：先清理晚于该记录日期的条目（日期快进测试可能残留未来记录），
     * 保证"最新体重 = 刚保存的这条"，全 APP 联动立即生效。
     */
    suspend fun saveCurrent(metric: BodyMetricEntity)
    suspend fun saveAll(metrics: List<BodyMetricEntity>)
    suspend fun clearAllWeights()
}

class BodyMetricRepositoryImpl(private val dao: BodyMetricDao) : BodyMetricRepository {
    override fun observeAll(): Flow<List<BodyMetricEntity>> = dao.observeAll()

    override fun observeLatest(): Flow<BodyMetricEntity?> = dao.observeLatest()

    override suspend fun getByDate(date: String): BodyMetricEntity? = dao.getByDate(date)

    override suspend fun save(metric: BodyMetricEntity) {
        dao.save(metric)
    }

    override suspend fun saveCurrent(metric: BodyMetricEntity) {
        dao.deleteAfter(metric.date)
        dao.save(metric)
    }

    override suspend fun saveAll(metrics: List<BodyMetricEntity>) {
        metrics.forEach { dao.save(it) }
    }

    override suspend fun clearAllWeights() {
        dao.deleteAllWeights()
    }
}
