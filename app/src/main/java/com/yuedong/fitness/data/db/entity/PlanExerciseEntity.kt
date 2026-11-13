package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 动作明细表，从属于某个计划日（planDayId 外键，计划日删除时级联删除）。
 * repsOrDuration 示例："12次" / "30秒"；tip 为一句动作要点。
 */
@Entity(
    tableName = "plan_exercise",
    foreignKeys = [
        ForeignKey(
            entity = PlanDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["planDayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planDayId")]
)
data class PlanExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planDayId: Long,
    val name: String,
    val sets: Int,
    val repsOrDuration: String,
    val tip: String
)
