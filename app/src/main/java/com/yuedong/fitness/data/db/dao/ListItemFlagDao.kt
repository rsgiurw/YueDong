package com.yuedong.fitness.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yuedong.fitness.data.db.entity.ListItemFlagEntity
import kotlinx.coroutines.flow.Flow

/** 列表条目标记 DAO（置顶/收藏/隐藏）。 */
@Dao
interface ListItemFlagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(flag: ListItemFlagEntity)

    @Query("DELETE FROM list_item_flag WHERE itemType = :itemType AND itemName = :itemName AND flag = :flag")
    suspend fun delete(itemType: String, itemName: String, flag: String)

    @Query("SELECT * FROM list_item_flag WHERE itemType = :itemType")
    fun observeByType(itemType: String): Flow<List<ListItemFlagEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM list_item_flag WHERE itemType = :itemType AND itemName = :itemName AND flag = :flag)")
    suspend fun has(itemType: String, itemName: String, flag: String): Boolean
}
