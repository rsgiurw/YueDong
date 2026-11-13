package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 训练计划表。
 * source 取值：BUILTIN（内置）/ CUSTOM（用户自建）；
 * purposeTag 取值同 purpose：FAT_LOSS / MUSCLE_GAIN / TONE / HEALTH；
 * isActive 表示当前激活计划（全局至多一个，由 PlanRepository 统一维护，其他模块不得直接改写）。
 */
@Entity(tableName = "workout_plan")
data class WorkoutPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val source: String,
    val purposeTag: String,
    val daysPerWeek: Int,
    /** 训练周期天数：0 = 按模板天数循环；>0 = 虚拟周期（模板按日轮换） */
    val cycleDays: Int = 0,
    val isActive: Boolean = false,
    val isFavorite: Boolean = false,
    /** 置顶：长按菜单设置，置顶的计划排列表最前 */
    val isPinned: Boolean = false,
    /** 软删除时间;null = 未删除,删除后进回收站可恢复 */
    val deletedAt: Long? = null
)
