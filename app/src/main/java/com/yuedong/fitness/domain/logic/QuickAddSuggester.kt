package com.yuedong.fitness.domain.logic

/**
 * 快捷补记推荐（纯函数，可单测）：
 * 对比今日实际摄入与目标，找出缺口最大的营养素（蛋白质/碳水/脂肪），
 * 优先推荐该营养素密度高的食物（必须真实补到短板，如缺口在蛋白则食物蛋白 ≥ 5g/100g）；
 * 热量余量不足 300 千卡时对高热量食物降权；已超过目标热量时只保留 ≤150 千卡的低热量选项。
 * 分类互不重复，保证一次补记的多样性。
 */
object QuickAddSuggester {

    /** 今日目标（来自问卷计算） */
    data class Goal(val proteinG: Double, val carbG: Double, val fatG: Double, val kcal: Int)

    /** 今日实际摄入 */
    data class Intake(val proteinG: Double, val carbG: Double, val fatG: Double, val kcal: Int)

    fun recommend(
        intake: Intake,
        goal: Goal,
        catalog: List<FoodCatalog.Food>,
        limit: Int = 4
    ): List<FoodCatalog.Food> {
        val proteinGap = (goal.proteinG - intake.proteinG).coerceAtLeast(0.0)
        val carbGap = (goal.carbG - intake.carbG).coerceAtLeast(0.0)
        val fatGap = (goal.fatG - intake.fatG).coerceAtLeast(0.0)
        val kcalLeft = goal.kcal - intake.kcal

        // 缺口占比 ≥ 一半即视为"主导短板"，只按该营养素打分；没有明显短板则均衡打分
        val gapSum = proteinGap + carbGap + fatGap
        val proteinDominant = gapSum > 0 && proteinGap / gapSum >= 0.5
        val carbDominant = !proteinDominant && gapSum > 0 && carbGap / gapSum >= 0.5
        val fatDominant = !proteinDominant && !carbDominant && gapSum > 0 && fatGap / gapSum >= 0.5
        val wProtein = if (proteinDominant) 2.5 else 1.0
        val wCarb = if (carbDominant) 2.5 else 1.0
        val wFat = if (fatDominant) 2.5 else 1.0
        val hardLowCal = kcalLeft <= 0   // 已超目标热量：只允许低热量选项
        val lowBudget = kcalLeft < 300   // 热量余量紧张：高热量食物降权

        return catalog.asSequence()
            .filter { it.kcal in 20..600 }                       // 排除白水类与极端值
            .filter { !hardLowCal || it.kcal <= 150 }
            .filter {
                // 必须真实补到短板营养
                when {
                    proteinDominant -> it.protein >= 5.0
                    carbDominant -> it.carb >= 15.0
                    fatDominant -> it.fat >= 8.0
                    else -> true
                }
            }
            .map { f ->
                var score = f.protein * wProtein + f.carb * wCarb + f.fat * wFat
                if (lowBudget) score -= f.kcal * 0.25
                f to score
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .distinctBy { it.category }                          // 每类最多一个，保证多样性
            .take(limit)
            .toList()
    }
}
