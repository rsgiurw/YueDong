package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 健身日记表：每天至多一条（date 唯一索引，同日重写按覆盖更新）。
 * mood 取值 1~5，对应 😞 😟 😐 🙂 😄。
 */
@Entity(tableName = "diary_entry", indices = [Index(value = ["date"], unique = true)])
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val mood: Int,
    val content: String,
    val updatedAt: Long
)
