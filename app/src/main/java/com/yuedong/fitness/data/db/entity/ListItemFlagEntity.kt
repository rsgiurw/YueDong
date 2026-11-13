package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 列表条目标记表：给按名称索引的条目（内置/自建动作、内置/自建食物）打上
 * 置顶(pin)/收藏(favorite)/隐藏(hidden) 等标记，同一个条目可同时有多个标记。
 * itemType 取值："exercise" / "food"。
 */
@Entity(
    tableName = "list_item_flag",
    indices = [Index(value = ["itemType", "itemName", "flag"], unique = true)]
)
data class ListItemFlagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemType: String,
    val itemName: String,
    val flag: String,
    val createdAt: Long
)
