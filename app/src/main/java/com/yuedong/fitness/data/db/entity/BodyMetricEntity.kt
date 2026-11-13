package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 身体数据表：每天至多一条（date 唯一索引，同日录入按覆盖更新）。
 * waistCm 为 V2 预留列（腰围），当前恒为 null，避免将来数据库迁移。
 */
@Entity(tableName = "body_metric", indices = [Index(value = ["date"], unique = true)])
data class BodyMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val weightKg: Double,
    val waistCm: Double? = null,
    val createdAt: Long,
    val updatedAt: Long
)
