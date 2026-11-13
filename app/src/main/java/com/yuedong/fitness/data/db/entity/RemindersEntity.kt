package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 训练提醒:可添加多条,每条每日定时本地通知(当天已打卡则跳过)。 */
@Entity(tableName = "reminders")
data class RemindersEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true,
    val createdAt: Long
)
