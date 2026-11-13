package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 当日额外动作:补录某天计划之外实际完成的动作(不进入计划模板)。 */
@Entity(tableName = "day_extra_exercise")
data class DayExtraExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 归属日期 yyyy-MM-dd */
    val date: String,
    val name: String,
    /** 如 "3 组 × 12 次" / "30 秒" */
    val setsText: String,
    val createdAt: Long
)
