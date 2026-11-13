package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 动作完成进度表：某天某个训练动作是否完成（今日页逐项勾选）。
 * 同一天同一动作只有一行（唯一索引 + REPLACE 幂等写入）。
 */
@Entity(
    tableName = "exercise_progress",
    indices = [
        Index("date"),
        Index(value = ["date", "planExerciseId"], unique = true)
    ]
)
data class ExerciseProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val planExerciseId: Long,
    val done: Boolean
)
