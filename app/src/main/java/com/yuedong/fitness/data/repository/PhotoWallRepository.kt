package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.PhotoWallDao
import com.yuedong.fitness.data.db.entity.PhotoWallEntity
import kotlinx.coroutines.flow.Flow

interface PhotoWallRepository {
    fun observeAll(): Flow<List<PhotoWallEntity>>
    fun observeDeleted(): Flow<List<PhotoWallEntity>>
    suspend fun add(date: String, path: String)
    suspend fun byId(id: Long): PhotoWallEntity?
    suspend fun delete(id: Long)
    suspend fun restore(id: Long)
    suspend fun purge(id: Long)
}

class PhotoWallRepositoryImpl(
    private val dao: PhotoWallDao
) : PhotoWallRepository {

    override fun observeAll(): Flow<List<PhotoWallEntity>> = dao.observeAll()
    override fun observeDeleted(): Flow<List<PhotoWallEntity>> = dao.observeDeleted()

    override suspend fun add(date: String, path: String) {
        dao.insert(PhotoWallEntity(date = date, path = path, createdAt = System.currentTimeMillis()))
    }

    override suspend fun byId(id: Long): PhotoWallEntity? = dao.byId(id)

    override suspend fun delete(id: Long) = dao.softDelete(id, System.currentTimeMillis())
    override suspend fun restore(id: Long) = dao.restore(id)
    override suspend fun purge(id: Long) = dao.delete(id)
}
