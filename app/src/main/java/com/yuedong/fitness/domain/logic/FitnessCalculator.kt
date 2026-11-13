package com.yuedong.fitness.domain.logic

import com.yuedong.fitness.domain.model.BmiCategory
import com.yuedong.fitness.domain.model.FitnessInput
import com.yuedong.fitness.domain.model.FitnessMetrics
import com.yuedong.fitness.domain.model.Gender
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * 健身指标计算器（模块契约见设计包 §8 FitnessCalculator）。
 * 职责：BMI 及分类、BMR（Mifflin-St Jeor）、TDEE（活动系数）、目标热量（含下限保护）、
 *       三大营养素克数。
 * 非职责：不做 IO、不依赖 Android 类、不持有状态（全部纯函数，可单元测试）。
 * 隐藏内部：活动系数表、营养素比例等常量集中在 AppConfig。
 */
object FitnessCalculator {

    /** BMI = 体重(kg) ÷ 身高²(m)。 */
    fun bmi(weightKg: Double, heightCm: Int): Double {
        if (heightCm <= 0) return 0.0
        val heightM = heightCm / 100.0
        return weightKg / (heightM * heightM)
    }

    /** BMI 分类边界：偏瘦 <18.5，正常 <24，超重 <28，肥胖 ≥28。 */
    fun bmiCategory(bmi: Double): BmiCategory = when {
        bmi < 18.5 -> BmiCategory.UNDERWEIGHT
        bmi < 24.0 -> BmiCategory.NORMAL
        bmi < 28.0 -> BmiCategory.OVERWEIGHT
        else -> BmiCategory.OBESE
    }

    /** 基础代谢（Mifflin-St Jeor）：男 = 10w+6.25h−5age+5；女同式 −161。 */
    fun bmr(gender: Gender, weightKg: Double, heightCm: Int, age: Int): Int {
        val base = 10 * weightKg + 6.25 * heightCm - 5 * age
        val offset = if (gender == Gender.MALE) 5.0 else -161.0
        return (base + offset).roundToInt()
    }

    /** 每周运动天数 → 活动系数。 */
    fun activityFactor(weeklyExerciseDays: Int): Double = when {
        weeklyExerciseDays <= 0 -> 1.2
        weeklyExerciseDays <= 2 -> 1.375
        weeklyExerciseDays <= 4 -> 1.55
        else -> 1.725
    }

    /** 汇总计算全部指标。 */
    fun metrics(input: FitnessInput): FitnessMetrics {
        val bmiValue = bmi(input.weightKg, input.heightCm)
        val bmrValue = bmr(input.gender, input.weightKg, input.heightCm, input.age)
        val tdee = (bmrValue * activityFactor(input.weeklyExerciseDays)).roundToInt()

        val floor = if (input.gender == Gender.MALE) {
            AppConfig.maleCalorieFloor
        } else {
            AppConfig.femaleCalorieFloor
        }
        val targetCalories = (tdee + AppConfig.calorieAdjustment(input.purpose)).coerceAtLeast(floor)

        val protein = roundTo5(input.weightKg * AppConfig.proteinPerKg(input.purpose))
        val fat = roundTo5(targetCalories * AppConfig.fatCalorieShare / 9.0)
        val carb = roundTo5((targetCalories - protein * 4 - fat * 9) / 4.0).coerceAtLeast(0)

        val waterTargetMl = ((input.weightKg * 30).coerceIn(1200.0, 3000.0) / 50).roundToInt() * 50

        return FitnessMetrics(
            waterTargetMl = waterTargetMl,
            bmi = bmiValue,
            bmiCategory = bmiCategory(bmiValue),
            bmr = bmrValue,
            tdee = tdee,
            targetCalories = targetCalories,
            proteinG = protein,
            carbG = carb,
            fatG = fat
        )
    }

    /** 克数取 5 的倍数，便于用户阅读与执行。 */
    private fun roundTo5(value: Double): Int = (value / 5.0).roundToInt() * 5

    /**
     * 打卡连击天数：从今天往回数连续打卡的天数。
     * 今天还没打卡不断签——连击保留到昨天为止（避免"今天还没练"的焦虑）。
     */
    fun streak(dates: List<LocalDate>, today: LocalDate): Int {
        val set = dates.toSet()
        if (set.isEmpty()) return 0
        var cursor = if (today in set) today else today.minusDays(1)
        var count = 0
        while (cursor in set) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    /**
     * 每日缺口/盈余的进度反馈（v2.3 采纳用户建议）。
     * 按"每公斤脂肪约 7700 千卡"估算每周体重变化方向与幅度；estimate 仅用于参考展示。
     */
    fun calorieFeedback(tdee: Int, targetCalories: Int): CalorieFeedback {
        val deficit = tdee - targetCalories
        val weeklyKg = deficit * 7.0 / 7700.0
        val direction = when {
            weeklyKg > 0.02 -> "减重"
            weeklyKg < -0.02 -> "增重"
            else -> "持平"
        }
        return CalorieFeedback(
            deficit = deficit,
            weeklyWeightChangeKg = kotlin.math.abs((weeklyKg * 10).roundToInt()) / 10.0,
            direction = direction
        )
    }
}

/**
 * 进度反馈结果：deficit>0 为缺口（减重方向），<0 为盈余（增重方向）。
 */
data class CalorieFeedback(
    val deficit: Int,
    val weeklyWeightChangeKg: Double,
    val direction: String
)
