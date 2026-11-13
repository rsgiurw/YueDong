package com.yuedong.fitness.domain

import com.yuedong.fitness.domain.logic.DietAdvisor
import com.yuedong.fitness.domain.model.Purpose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 饮食建议文案库测试：每个目的建议数量、内容非空、四种目的互不相同、未知值回退。
 */
class DietAdvisorTest {

    @Test
    fun everyPurpose_hasThreeToFourTips() {
        Purpose.entries.forEach { purpose ->
            val tips = DietAdvisor.tips(purpose)
            assertTrue("「${purpose.name}」的建议数量应在 3~4 条", tips.size in 3..4)
        }
    }

    @Test
    fun everyTip_hasNonBlankContent() {
        Purpose.entries.forEach { purpose ->
            DietAdvisor.tips(purpose).forEach { tip ->
                assertTrue(tip.emoji.isNotBlank())
                assertTrue(tip.title.isNotBlank())
                assertTrue("「${tip.title}」的说明不应为空", tip.detail.isNotBlank())
            }
        }
    }

    @Test
    fun tips_differAcrossPurposes() {
        val joined = Purpose.entries.map { purpose ->
            DietAdvisor.tips(purpose).joinToString("|") { it.title }
        }
        assertEquals("四种目的的标题集合应互不相同", joined.size, joined.toSet().size)
    }

    @Test
    fun tipsFor_fallbackToHealthOnUnknown() {
        assertEquals(
            DietAdvisor.tips(Purpose.HEALTH).map { it.title },
            DietAdvisor.tipsFor("UNKNOWN").map { it.title }
        )
    }

    @Test
    fun meals_everyPurposeHasThreeMeals() {
        Purpose.entries.forEach { purpose ->
            val meals = DietAdvisor.meals(purpose)
            assertEquals("「${purpose.name}」应有 3 餐示例", 3, meals.size)
            meals.forEach { meal ->
                assertTrue(meal.emoji.isNotBlank())
                assertTrue(meal.foods.isNotBlank())
            }
        }
    }

    @Test
    fun meals_differAcrossPurposes() {
        val joined = Purpose.entries.map { purpose ->
            DietAdvisor.meals(purpose).joinToString("|") { it.foods }
        }
        assertEquals("四种目的的三餐搭配应互不相同", joined.size, joined.toSet().size)
    }

    @Test
    fun tipsFor_byDatabaseValue() {
        assertEquals(
            DietAdvisor.tips(Purpose.FAT_LOSS).map { it.title },
            DietAdvisor.tipsFor("FAT_LOSS").map { it.title }
        )
    }
}
