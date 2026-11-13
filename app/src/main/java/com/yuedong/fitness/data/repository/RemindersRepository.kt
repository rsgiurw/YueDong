package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.RemindersDao
import com.yuedong.fitness.data.db.entity.RemindersEntity
import kotlinx.coroutines.flow.Flow

interface RemindersRepository {
    fun observeAll(): Flow<List<RemindersEntity>>
    suspend fun byId(id: Long): RemindersEntity?
    suspend fun add(hour: Int, minute: Int): Long
    suspend fun update(id: Long, hour: Int, minute: Int)
    suspend fun setEnabled(id: Long, enabled: Boolean)
    suspend fun delete(id: Long)
}

class RemindersRepositoryImpl(
    private val dao: RemindersDao
) : RemindersRepository {

    override fun observeAll(): Flow<List<RemindersEntity>> = dao.observeAll()
    override suspend fun byId(id: Long): RemindersEntity? = dao.byId(id)

    override suspend fun add(hour: Int, minute: Int): Long =
        dao.insert(RemindersEntity(hour = hour, minute = minute, createdAt = System.currentTimeMillis()))

    override suspend fun update(id: Long, hour: Int, minute: Int) {
        dao.byId(id)?.let { dao.update(it.copy(hour = hour, minute = minute)) }
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) = dao.setEnabled(id, enabled)
    override suspend fun delete(id: Long) = dao.delete(id)
}
