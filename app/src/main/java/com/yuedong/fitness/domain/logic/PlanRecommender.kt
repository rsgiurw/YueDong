package com.yuedong.fitness.domain.logic

import com.yuedong.fitness.data.db.entity.UserProfileEntity
import com.yuedong.fitness.data.db.entity.WorkoutPlanEntity
import com.yuedong.fitness.domain.model.Purpose

/**
 * 计划推荐结果。
 */
data class PlanRecommendation(val planId: Long, val reason: String)

/**
 * 计划推荐器（模块契约见设计包 §8 PlanRecommender，核心扩展点）。
 * V2：从单一推荐升级为排序推荐——返回按匹配度降序的多套方案，
 * 首选标"为你推荐"，次选标"也适合"。
 * 将来 AiPlanRecommender 实现同一接口即可接入真实 LLM，界面零改动。
 */
interface PlanRecommender {

    /** 兼容旧调用：返回首选推荐。 */
    fun recommend(profile: UserProfileEntity, plans: List<WorkoutPlanEntity>): PlanRecommendation? =
        recommendations(profile, plans).firstOrNull()

    /** 按匹配度降序返回最多 3 套推荐。 */
    fun recommendations(
        profile: UserProfileEntity,
        plans: List<WorkoutPlanEntity>
    ): List<PlanRecommendation>
}

/**
 * 规则版推荐：
 * 1. 目的匹配——目的标签一致得高分；目的一致的兼容组合（减脂↔塑形↔健康）得中分
 * 2. 运动量接近——计划的每周天数与用户运动量越接近加分越多
 */
class BuiltinRuleRecommender : PlanRecommender {

    override fun recommendations(
        profile: UserProfileEntity,
        plans: List<WorkoutPlanEntity>
    ): List<PlanRecommendation> {
        if (plans.isEmpty()) return emptyList()

        val targetTag = if (profile.purpose == Purpose.TONE.name) {
            Purpose.HEALTH.name
        } else {
            profile.purpose
        }
        val compatible = compatibleTags(targetTag)
        val purposeLabel = Purpose.fromValue(profile.purpose)?.label ?: "健身"

        return plans
            .map { plan ->
                val tagScore = when {
                    plan.purposeTag == targetTag -> 100
                    plan.purposeTag in compatible -> 50
                    else -> 0
                }
                val daysScore = 10 - minOf(9, kotlin.math.abs(plan.daysPerWeek - profile.weeklyExerciseDays))
                Triple(plan.id, tagScore, daysScore)
            }
            .filter { it.second > 0 } // 目的不兼容的计划直接排除，不靠天数分混入
            .sortedWith(compareByDescending<Triple<Long, Int, Int>> { it.second }.thenByDescending { it.third })
            .take(3)
            .mapIndexed { index, (planId, _, _) ->
                val reason = if (index == 0) {
                    "根据你的「${purposeLabel}」目标与每周 ${profile.weeklyExerciseDays} 天的运动量，最推荐这套"
                } else {
                    "同样符合你的「${purposeLabel}」目标，可以作为备选方案"
                }
                PlanRecommendation(planId, reason)
            }
    }

    /** 与目标兼容的目的标签集合（塑形用户的目标按健康类计划处理；增肌只推力量类）。 */
    private fun compatibleTags(target: String): Set<String> = when (target) {
        "FAT_LOSS" -> setOf("FAT_LOSS", "TONE", "HEALTH")
        "HEALTH" -> setOf("HEALTH", "TONE", "FAT_LOSS")
        "MUSCLE_GAIN" -> setOf("MUSCLE_GAIN")
        else -> setOf("HEALTH", "TONE", "FAT_LOSS")
    }
}
