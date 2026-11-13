package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.ListItemFlagDao
import com.yuedong.fitness.data.db.entity.ListItemFlagEntity
import kotlinx.coroutines.flow.Flow

/** 标记常量：置顶 / 收藏 / 隐藏（隐藏 = 从列表里删除内置条目的实现方式）。 */
object ItemFlags {
    const val PIN = "pin"
    const val FAVORITE = "favorite"
    const val HIDDEN = "hidden"
}

interface ItemFlagRepository {
    fun observeByType(itemType: String): Flow<List<ListItemFlagEntity>>
    suspend fun setFlag(itemType: String, itemName: String, flag: String)
    suspend fun clearFlag(itemType: String, itemName: String, flag: String)
    suspend fun hasFlag(itemType: String, itemName: String, flag: String): Boolean
}

class ItemFlagRepositoryImpl(private val dao: ListItemFlagDao) : ItemFlagRepository {
    override fun observeByType(itemType: String): Flow<List<ListItemFlagEntity>> = dao.observeByType(itemType)

    override suspend fun setFlag(itemType: String, itemName: String, flag: String) =
        dao.upsert(ListItemFlagEntity(itemType = itemType, itemName = itemName, flag = flag, createdAt = System.currentTimeMillis()))

    override suspend fun clearFlag(itemType: String, itemName: String, flag: String) =
        dao.delete(itemType = itemType, itemName = itemName, flag = flag)

    override suspend fun hasFlag(itemType: String, itemName: String, flag: String): Boolean =
        dao.has(itemType, itemName, flag)
}
