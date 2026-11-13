package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 训练备忘录:自由笔记,删除进入回收站(软删) */
@Entity(tableName = "memo")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val updatedAt: Long,
    val deletedAt: Long? = null
)
