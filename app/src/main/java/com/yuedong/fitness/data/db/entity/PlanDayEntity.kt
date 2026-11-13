package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 计划的单日安排表，从属于某个计划（planId 外键，计划删除时级联删除）。
 * isRestDay 为 true 表示该日是休息日，title 通常为"休息"。
 */
@Entity(
    tableName = "plan_day",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class PlanDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val orderIndex: Int,
    val title: String,
    val isRestDay: Boolean
)
