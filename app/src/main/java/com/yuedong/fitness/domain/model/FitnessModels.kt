package com.yuedong.fitness.domain.model

/**
 * 健身指标计算的输入（纯 Kotlin 值对象，不依赖任何存储或界面）。
 */
data class FitnessInput(
    val gender: Gender,
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val weeklyExerciseDays: Int,
    val purpose: Purpose
)

/**
 * 健身指标计算结果（阶段 2 问卷展示、阶段 5 饮食页共用）。
 */
data class FitnessMetrics(
    /** 每日饮水目标（ml）：体重 30ml/kg，1200~3000 取 50 倍数 */
    val waterTargetMl: Int = 2000,
    val bmi: Double,
    val bmiCategory: BmiCategory,
    val bmr: Int,
    val tdee: Int,
    val targetCalories: Int,
    val proteinG: Int,
    val carbG: Int,
    val fatG: Int
)

/** 目标体重进度（我的页目标进度卡） */
data class TargetProgressUi(
    val startWeightKg: Double,
    val currentWeightKg: Double,
    val targetWeightKg: Double,
    val doneKg: Double,
    val remainingKg: Double,
    val progress: Float
)
