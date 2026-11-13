package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 打卡记录表：每天至多一条（date 唯一索引，重复打卡按同日覆盖，保证幂等）。
 * 说明：planId 故意不设外键——即使未来删除计划，历史打卡记录也应保留。
 * date 格式：ISO yyyy-MM-dd。
 */
@Entity(tableName = "check_in_record", indices = [Index(value = ["date"], unique = true)])
data class CheckInRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val planId: Long,
    val note: String? = null,
    val createdAt: Long
)
