package com.yuedong.fitness.domain

import com.yuedong.fitness.domain.logic.FitnessCalculator
import com.yuedong.fitness.domain.model.BmiCategory
import com.yuedong.fitness.domain.model.FitnessInput
import com.yuedong.fitness.domain.model.Gender
import com.yuedong.fitness.domain.model.Purpose
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 健身指标计算器单元测试：男女各一组完整案例 + 热量下限保护 + BMI 边界。
 */
class FitnessCalculatorTest {

    @Test
    fun bmi_formula() {
        assertEquals(22.857, FitnessCalculator.bmi(70.0, 175), 0.001)
        assertEquals(21.484, FitnessCalculator.bmi(55.0, 160), 0.001)
    }

    @Test
    fun bmiCategory_boundaries() {
        assertEquals(BmiCategory.UNDERWEIGHT, FitnessCalculator.bmiCategory(18.4))
        assertEquals(BmiCategory.NORMAL, FitnessCalculator.bmiCategory(18.5))
        assertEquals(BmiCategory.NORMAL, FitnessCalculator.bmiCategory(23.9))
        assertEquals(BmiCategory.OVERWEIGHT, FitnessCalculator.bmiCategory(24.0))
        assertEquals(BmiCategory.OVERWEIGHT, FitnessCalculator.bmiCategory(27.9))
        assertEquals(BmiCategory.OBESE, FitnessCalculator.bmiCategory(28.0))
    }

    @Test
    fun activityFactor_boundaries() {
        assertEquals(1.2, FitnessCalculator.activityFactor(0), 0.0001)
        assertEquals(1.375, FitnessCalculator.activityFactor(2), 0.0001)
        assertEquals(1.55, FitnessCalculator.activityFactor(4), 0.0001)
        assertEquals(1.725, FitnessCalculator.activityFactor(7), 0.0001)
    }

    @Test
    fun metrics_male_fatLoss() {
        // 男，25 岁，175cm，70kg，每周 3 天，减脂
        val metrics = FitnessCalculator.metrics(
            FitnessInput(Gender.MALE, 25, 175, 70.0, 3, Purpose.FAT_LOSS)
        )
        assertEquals(1674, metrics.bmr)          // 10*70+6.25*175-5*25+5 = 1673.75
        assertEquals(2595, metrics.tdee)         // 1674*1.55 = 2594.7
        assertEquals(2195, metrics.targetCalories) // 2595-400
        assertEquals(125, metrics.proteinG)      // 70*1.8 = 126 → 取 5 的倍数
        assertEquals(60, metrics.fatG)           // 2195*0.25/9 = 60.97
        assertEquals(290, metrics.carbG)         // (2195-500-540)/4 = 288.75
        assertEquals(BmiCategory.NORMAL, metrics.bmiCategory)
    }

    @Test
    fun metrics_female_muscleGain() {
        // 女，30 岁，160cm，55kg，每周 5 天，增肌
        val metrics = FitnessCalculator.metrics(
            FitnessInput(Gender.FEMALE, 30, 160, 55.0, 5, Purpose.MUSCLE_GAIN)
        )
        assertEquals(1239, metrics.bmr)          // 10*55+6.25*160-5*30-161 = 1239
        assertEquals(2137, metrics.tdee)         // 1239*1.725 = 2137.275
        assertEquals(2437, metrics.targetCalories) // 2137+300
        assertEquals(100, metrics.proteinG)      // 55*1.8 = 99 → 100
        assertEquals(70, metrics.fatG)           // 2437*0.25/9 = 67.69
        assertEquals(350, metrics.carbG)         // (2437-400-630)/4 = 351.75
        assertEquals(BmiCategory.NORMAL, metrics.bmiCategory)
    }

    @Test
    fun targetCalories_floorProtection() {
        // 女，80 岁，150cm，40kg，几乎不动，减脂：原始目标 532 千卡，必须被抬到下限 1200
        val metrics = FitnessCalculator.metrics(
            FitnessInput(Gender.FEMALE, 80, 150, 40.0, 0, Purpose.FAT_LOSS)
        )
        assertEquals(1200, metrics.targetCalories)
        assertEquals(70, metrics.proteinG) // 40*1.8 = 72 → 70
    }

    @Test
    fun maleFloor_higherThanFemale() {
        // 男性下限 1500：构造一个原始目标低于 1500 的男性案例
        val metrics = FitnessCalculator.metrics(
            FitnessInput(Gender.MALE, 80, 150, 40.0, 0, Purpose.FAT_LOSS)
        )
        assertEquals(1500, metrics.targetCalories)
    }

    // ---- 打卡连击 ----

    @Test
    fun streak_emptyList_isZero() {
        val today = java.time.LocalDate.of(2026, 9, 5)
        assertEquals(0, FitnessCalculator.streak(emptyList(), today))
    }

    @Test
    fun streak_countsConsecutiveDaysIncludingToday() {
        val today = java.time.LocalDate.of(2026, 9, 5)
        val dates = listOf(
            java.time.LocalDate.of(2026, 9, 5),
            java.time.LocalDate.of(2026, 9, 4),
            java.time.LocalDate.of(2026, 9, 3),
            java.time.LocalDate.of(2026, 8, 30) // 中断，不计入
        )
        assertEquals(3, FitnessCalculator.streak(dates, today))
    }

    @Test
    fun streak_withoutToday_keepsYesterdayChain() {
        // 今天还没练，不断签：连击保留到昨天
        val today = java.time.LocalDate.of(2026, 9, 5)
        val dates = listOf(
            java.time.LocalDate.of(2026, 9, 4),
            java.time.LocalDate.of(2026, 9, 3)
        )
        assertEquals(2, FitnessCalculator.streak(dates, today))
    }

    @Test
    fun streak_brokenChain_isZero() {
        // 今天和昨天都没打卡 → 连击归零
        val today = java.time.LocalDate.of(2026, 9, 5)
        val dates = listOf(java.time.LocalDate.of(2026, 9, 3))
        assertEquals(0, FitnessCalculator.streak(dates, today))
    }

    // ---- 热量缺口进度反馈 ----

    @Test
    fun calorieFeedback_deficitMeansWeightLoss() {
        // TDEE 2640 - 目标 2240 = 400 缺口 → 每周约 0.4 公斤减重
        val feedback = FitnessCalculator.calorieFeedback(2640, 2240)
        assertEquals(400, feedback.deficit)
        assertEquals(0.4, feedback.weeklyWeightChangeKg, 0.05)
        assertEquals("减重", feedback.direction)
    }

    @Test
    fun calorieFeedback_surplusMeansWeightGain() {
        // 增肌：TDEE 2000 - 目标 2300 = -300 盈余 → 每周约 0.3 公斤增重
        val feedback = FitnessCalculator.calorieFeedback(2000, 2300)
        assertEquals(-300, feedback.deficit)
        assertEquals(0.3, feedback.weeklyWeightChangeKg, 0.05)
        assertEquals("增重", feedback.direction)
    }

    @Test
    fun calorieFeedback_balancedMeansMaintain() {
        val feedback = FitnessCalculator.calorieFeedback(2200, 2200)
        assertEquals(0, feedback.deficit)
        assertEquals(0.0, feedback.weeklyWeightChangeKg, 0.001)
        assertEquals("持平", feedback.direction)
    }
}
