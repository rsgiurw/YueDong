package com.yuedong.fitness.domain

import com.yuedong.fitness.data.db.entity.UserProfileEntity
import com.yuedong.fitness.data.db.entity.WorkoutPlanEntity
import com.yuedong.fitness.domain.logic.BuiltinPlanSeed
import com.yuedong.fitness.domain.logic.BuiltinRuleRecommender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 计划推荐器单元测试：四种健身目的的映射、空列表兜底、推荐理由文案、种子数据完备性。
 */
class PlanRecommenderTest {

    private val recommender = BuiltinRuleRecommender()

    private fun plan(id: Long, purposeTag: String) = WorkoutPlanEntity(
        id = id,
        name = "计划$id",
        description = "",
        source = "BUILTIN",
        purposeTag = purposeTag,
        daysPerWeek = 3
    )

    private fun profile(purpose: String, weeklyDays: Int = 3) = UserProfileEntity(
        gender = "MALE",
        age = 25,
        heightCm = 175,
        currentWeightKg = 70.0,
        targetWeightKg = 65.0,
        weeklyExerciseDays = weeklyDays,
        purpose = purpose,
        createdAt = 0L,
        updatedAt = 0L
    )

    private val allPlans = listOf(
        plan(1, "FAT_LOSS"),
        plan(2, "MUSCLE_GAIN"),
        plan(3, "HEALTH")
    )

    @Test
    fun fatLoss_recommendsFatLossPlan() {
        val result = recommender.recommend(profile("FAT_LOSS"), allPlans)
        assertEquals(1L, result?.planId)
    }

    @Test
    fun muscleGain_recommendsStrengthPlan() {
        val result = recommender.recommend(profile("MUSCLE_GAIN"), allPlans)
        assertEquals(2L, result?.planId)
    }

    @Test
    fun health_recommendsHealthPlan() {
        val result = recommender.recommend(profile("HEALTH"), allPlans)
        assertEquals(3L, result?.planId)
    }

    @Test
    fun tone_sharesHealthPlan() {
        val result = recommender.recommend(profile("TONE"), allPlans)
        assertEquals(3L, result?.planId)
    }

    @Test
    fun recommendations_ranked_forFatLoss() {
        // 减脂用户：首选燃脂计划，兼容的健康计划次之；力量计划不出现（非增肌用户）
        val recs = recommender.recommendations(profile("FAT_LOSS"), allPlans)
        assertEquals(2, recs.size)
        assertEquals(1L, recs[0].planId)
        assertEquals(3L, recs[1].planId)
    }

    @Test
    fun recommendations_ranked_forTone() {
        // 塑形用户：首选健康活力，燃脂计划作为备选；力量计划不出现
        val recs = recommender.recommendations(profile("TONE"), allPlans)
        assertEquals(2, recs.size)
        assertEquals(3L, recs[0].planId)
        assertEquals(1L, recs[1].planId)
    }

    @Test
    fun recommendations_forMuscleGain_onlyStrength() {
        // 增肌用户：只推荐力量计划，不混入其他类型
        val recs = recommender.recommendations(profile("MUSCLE_GAIN"), allPlans)
        assertEquals(1, recs.size)
        assertEquals(2L, recs[0].planId)
    }

    @Test
    fun recommendations_secondaryReason_mentionsBackup() {
        val recs = recommender.recommendations(profile("FAT_LOSS"), allPlans)
        assertTrue(recs[0].reason.contains("最推荐"))
        assertTrue(recs[1].reason.contains("备选"))
    }

    @Test
    fun emptyPlans_returnsNull() {
        assertNull(recommender.recommend(profile("FAT_LOSS"), emptyList()))
    }

    @Test
    fun reason_isNonBlankAndMentionsPurpose() {
        val result = recommender.recommend(profile("FAT_LOSS", weeklyDays = 4), allPlans)
        assertNotNull(result)
        assertTrue(result!!.reason.contains("减脂"))
        assertTrue(result.reason.contains("每周 4 天"))
    }

    @Test
    fun builtinSeed_coversMainPurposes() {
        val tags = BuiltinPlanSeed.all().map { it.plan.purposeTag }
        assertTrue(tags.contains("FAT_LOSS"))
        assertTrue(tags.contains("MUSCLE_GAIN"))
        assertTrue(tags.contains("HEALTH"))
    }

    @Test
    fun builtinSeed_everyPlanHasCompleteCycleAndRestDays() {
        BuiltinPlanSeed.all().forEach { seed ->
            // 内容天数须整除循环天数（7 天节奏可重复填 14/21 天循环）
            assertEquals(0, seed.plan.cycleDays % seed.content.size)
            assertTrue(seed.content.any { it.day.isRestDay })
            assertEquals(
                (0 until seed.content.size).toList(),
                seed.content.map { it.day.orderIndex }
            )
        }
    }
}
