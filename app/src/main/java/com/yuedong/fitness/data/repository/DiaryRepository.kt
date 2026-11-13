package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.DiaryEntryDao
import com.yuedong.fitness.data.db.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

/** 日记仓库：每日一条，覆盖保存。 */
interface DiaryRepository {
    fun observeAll(): Flow<List<DiaryEntryEntity>>
    fun observeByDate(date: String): Flow<DiaryEntryEntity?>
    suspend fun getByDate(date: String): DiaryEntryEntity?
    suspend fun save(entry: DiaryEntryEntity)
}

class DiaryRepositoryImpl(private val dao: DiaryEntryDao) : DiaryRepository {
    override fun observeAll(): Flow<List<DiaryEntryEntity>> = dao.observeAll()

    override fun observeByDate(date: String): Flow<DiaryEntryEntity?> = dao.observeByDate(date)

    override suspend fun getByDate(date: String): DiaryEntryEntity? = dao.getByDate(date)

    override suspend fun save(entry: DiaryEntryEntity) {
        dao.save(entry)
    }
}
