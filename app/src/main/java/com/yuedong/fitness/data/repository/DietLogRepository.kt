package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.CustomFoodDao
import com.yuedong.fitness.data.db.dao.DietLogDao
import com.yuedong.fitness.data.db.entity.CustomFoodEntity
import com.yuedong.fitness.data.db.entity.DietLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * 饮食记录仓库（阶段 5.5 新增）：饮食日志 + 自定义食物的统一入口。
 */
interface DietLogRepository {
    fun observeLogsByDate(date: String): Flow<List<DietLogEntity>>
    fun observeLogsBetween(start: String, end: String): Flow<List<DietLogEntity>>
    suspend fun addLog(entry: DietLogEntity)
    suspend fun deleteLog(id: Long)

    fun observeCustomFoods(): Flow<List<CustomFoodEntity>>
    fun observeDeletedCustomFoods(): Flow<List<CustomFoodEntity>>
    suspend fun upsertCustomFood(food: CustomFoodEntity)
    suspend fun deleteCustomFood(name: String)
    suspend fun restoreCustomFood(name: String)
    suspend fun purgeCustomFood(name: String)
}

class DietLogRepositoryImpl(
    private val dietLogDao: DietLogDao,
    private val customFoodDao: CustomFoodDao
) : DietLogRepository {

    override fun observeLogsByDate(date: String): Flow<List<DietLogEntity>> =
        dietLogDao.observeByDate(date)

    override fun observeLogsBetween(start: String, end: String): Flow<List<DietLogEntity>> =
        dietLogDao.observeBetween(start, end)

    override suspend fun addLog(entry: DietLogEntity) {
        dietLogDao.insert(entry)
    }

    override suspend fun deleteLog(id: Long) {
        dietLogDao.deleteById(id)
    }

    override fun observeDeletedCustomFoods(): Flow<List<CustomFoodEntity>> =
        customFoodDao.observeDeleted()

    override fun observeCustomFoods(): Flow<List<CustomFoodEntity>> =
        customFoodDao.observeAll()

    override suspend fun upsertCustomFood(food: CustomFoodEntity) {
        // 同名编辑：保留原 id 更新，避免 REPLACE + 自增 id 在唯一索引下静默失败；
        // 老调用方不携带图片（null）时保留已有图片，避免被静默清掉
        val existing = customFoodDao.getByName(food.name)
        val merged = if (existing != null) {
            food.copy(id = existing.id, imagePath = food.imagePath ?: existing.imagePath)
        } else {
            food
        }
        customFoodDao.upsert(merged)
    }

    override suspend fun deleteCustomFood(name: String) {
        customFoodDao.softDeleteByName(name, System.currentTimeMillis())
    }

    override suspend fun restoreCustomFood(name: String) {
        customFoodDao.restoreByName(name)
    }

    override suspend fun purgeCustomFood(name: String) {
        customFoodDao.deleteByName(name)
    }
}
