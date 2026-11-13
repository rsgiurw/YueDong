package com.yuedong.fitness.domain

import com.yuedong.fitness.domain.logic.FoodCatalog
import com.yuedong.fitness.domain.logic.QuickAddSuggester
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickAddSuggesterTest {

    private val catalog = listOf(
        FoodCatalog.Food("鸡胸肉", "肉蛋海鲜", "🍗", 133, 31.0, 0.0, 3.0),
        FoodCatalog.Food("脱脂牛奶", "奶豆饮品", "🥛", 34, 3.4, 5.0, 0.0),
        FoodCatalog.Food("可乐", "快乐水", "🥤", 43, 0.0, 10.6, 0.0),
        FoodCatalog.Food("巧克力", "快乐零食", "🍫", 546, 4.3, 58.0, 30.0),
        FoodCatalog.Food("米饭", "主食谷物", "🍚", 116, 2.6, 25.9, 0.3),
        FoodCatalog.Food("核桃", "每日坚果", "🌰", 646, 14.9, 9.6, 58.8)
    )

    @Test
    fun 蛋白缺口主导时优先推荐高蛋白食物() {
        val rec = QuickAddSuggester.recommend(
            intake = QuickAddSuggester.Intake(proteinG = 10.0, carbG = 280.0, fatG = 55.0, kcal = 900),
            goal = QuickAddSuggester.Goal(proteinG = 90.0, carbG = 300.0, fatG = 60.0, kcal = 1800),
            catalog = catalog
        )
        // 高蛋白食物排第一；高糖高脂零食（蛋白 <5g）不能进推荐
        assertEquals("鸡胸肉", rec.first().name)
        assertTrue(rec.none { it.name == "巧克力" })
    }

    @Test
    fun 已超目标热量时只推荐低热量食物() {
        val rec = QuickAddSuggester.recommend(
            intake = QuickAddSuggester.Intake(proteinG = 60.0, carbG = 250.0, fatG = 50.0, kcal = 2200),
            goal = QuickAddSuggester.Goal(proteinG = 90.0, carbG = 300.0, fatG = 60.0, kcal = 1800),
            catalog = catalog
        )
        assertTrue(rec.isNotEmpty())
        assertTrue(rec.all { it.kcal <= 150 })
    }

    @Test
    fun 没有明显短板时均衡推荐且分类互不重复() {
        val rec = QuickAddSuggester.recommend(
            intake = QuickAddSuggester.Intake(proteinG = 45.0, carbG = 150.0, fatG = 30.0, kcal = 900),
            goal = QuickAddSuggester.Goal(proteinG = 90.0, carbG = 300.0, fatG = 60.0, kcal = 1800),
            catalog = catalog
        )
        assertEquals(rec.size, rec.map { it.category }.distinct().size)
    }
}
