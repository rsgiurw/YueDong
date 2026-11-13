package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 饮水记录：每次 +N ml 一行，按日汇总展示。 */
@Entity(
    tableName = "water_log",
    indices = [Index("date")]
)
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val amountMl: Int,
    val createdAt: Long
)
