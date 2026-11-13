package com.yuedong.fitness.data.db

import com.yuedong.fitness.data.db.entity.PlanDayEntity
import com.yuedong.fitness.data.db.entity.PlanExerciseEntity

/**
 * "一天安排 + 当天动作"的组合载体：用于整计划写入与整计划读取。
 */
data class PlanDayWithExercises(
    val day: PlanDayEntity,
    val exercises: List<PlanExerciseEntity>
)
