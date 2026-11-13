package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.MemoDao
import com.yuedong.fitness.data.db.entity.MemoEntity
import kotlinx.coroutines.flow.Flow

interface MemoRepository {
    fun observeAll(): Flow<List<MemoEntity>>
    fun observeDeleted(): Flow<List<MemoEntity>>
    suspend fun byId(id: Long): MemoEntity?
    suspend fun add(title: String, content: String)
    suspend fun update(id: Long, title: String, content: String)
    suspend fun softDelete(id: Long)
    suspend fun restore(id: Long)
    suspend fun purge(id: Long)
}

class MemoRepositoryImpl(
    private val dao: MemoDao
) : MemoRepository {

    override fun observeAll(): Flow<List<MemoEntity>> = dao.observeAll()
    override fun observeDeleted(): Flow<List<MemoEntity>> = dao.observeDeleted()
    override suspend fun byId(id: Long): MemoEntity? = dao.byId(id)

    override suspend fun add(title: String, content: String) {
        dao.insert(
            MemoEntity(title = title, content = content, updatedAt = System.currentTimeMillis())
        )
    }

    override suspend fun update(id: Long, title: String, content: String) {
        dao.byId(id)?.let {
            dao.update(it.copy(title = title, content = content, updatedAt = System.currentTimeMillis()))
        }
    }

    override suspend fun softDelete(id: Long) = dao.softDelete(id, System.currentTimeMillis())
    override suspend fun restore(id: Long) = dao.restore(id)
    override suspend fun purge(id: Long) = dao.purge(id)
}
