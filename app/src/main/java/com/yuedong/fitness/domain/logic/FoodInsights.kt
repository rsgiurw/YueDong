package com.yuedong.fitness.domain.logic

import com.yuedong.fitness.domain.logic.FoodCatalog.Food
import kotlin.math.roundToInt

/**
 * 营养洞察规则引擎：由营养数据自动推导标签、洞察语句、适宜人群与食用建议。
 * 全量食物通用，无需逐条手写。
 */
object FoodInsights {

    /** 营养特点标签（规则推导） */
    fun tags(food: Food): List<String> {
        val t = mutableListOf<String>()
        if (food.protein >= 15) t.add("高蛋白")
        if (food.fat <= 3 && food.kcal <= 150) t.add("低脂")
        if (food.fat >= 20) t.add("高脂")
        if (food.carb >= 40) t.add("高碳水")
        if (food.carb <= 6 && food.kcal <= 120) t.add("低碳水")
        food.gi?.let { if (it <= 55) t.add("低GI") }
        if (food.kcal <= 80) t.add("低热量")
        if (food.kcal >= 350) t.add("高热量")
        if (food.category == "蔬菜菌菇") t.add("高纤维")
        if (food.category == "快乐水果" && food.kcal <= 60) t.add("低糖果")
        if (food.category == "奶豆饮品" && food.name.contains("酸奶")) t.add("益生菌")
        if (food.category == "每日坚果") t.add("优质脂肪")
        if (food.name.contains("鱼") || food.name.contains("虾") || food.name.contains("贝") ||
            food.name.contains("蚝") || food.name.contains("蛤") || food.name.contains("鳕")) t.add("富含DHA")
        return t
    }

    /** 一句话营养洞察（规则拼装） */
    fun insight(food: Food): String {
        val p = food.protein.round1()
        val c = food.carb.round1()
        val f = food.fat.round1()
        val sb = StringBuilder("每 100${food.unit} 含蛋白质 ${p}g、碳水 ${c}g、脂肪 ${f}g。")
        when {
            food.protein >= 15 -> sb.append(" 蛋白质含量突出，是补充优质蛋白的好选择。")
            food.category == "快乐水果" && food.kcal <= 60 -> sb.append(" 热量低水分足，水果里的轻盈之选。")
            food.category == "蔬菜菌菇" -> sb.append(" 膳食纤维与微量营养素丰富，热量几乎可以忽略。")
            food.fat >= 20 && food.kcal >= 350 -> sb.append(" 热量密度较高，解馋之余注意控制分量。")
            food.carb >= 40 -> sb.append(" 碳水为主，适合作为运动前后的能量补充。")
            food.kcal <= 100 -> sb.append(" 热量友好，日常食用没有负担。")
            else -> sb.append(" 营养较为均衡，搭配蔬菜食用更佳。")
        }
        return sb.toString()
    }

    /** 适宜人群 */
    fun suitableFor(food: Food): List<String> {
        val list = mutableListOf<String>()
        if (food.protein >= 15 && food.fat <= 15) list.add("增肌期")
        if (food.kcal <= 150) list.add("减脂期")
        food.gi?.let { if (it <= 55) list.add("控糖人群") }
        if (food.category == "蔬菜菌菇" || food.category == "快乐水果") list.add("大众日常")
        if (food.fat >= 20 || food.kcal >= 350) list.add("运动前后快速补能")
        return list.ifEmpty { listOf("大众日常") }
    }

    /**
     * 运动消耗对照：按用户当前体重与标准 MET 值计算
     * 千卡/分钟 = MET × 3.5 × 体重(kg) ÷ 200
     * MET 参考值：慢跑 8.0、跳绳（中速）11.0、快走 4.3
     */
    fun exerciseMinutes(food: Food, weightKg: Double): List<Triple<String, Int, String>> {
        val kcal = food.kcal.toDouble()
        fun minutes(met: Double) = (met * 3.5 * weightKg / 200.0).roundToInt().coerceAtLeast(1)
        return listOf(
            Triple("慢跑", minutes(8.0), "分钟"),
            Triple("跳绳", minutes(11.0), "分钟"),
            Triple("快走", minutes(4.3), "分钟")
        )
    }

    /** 食用建议（规则生成） */
    fun tip(food: Food): String = when {
        food.kcal >= 350 -> "热量密度较高，建议作为加餐少量品尝，正餐请控制总摄入。"
        food.fat >= 20 -> "脂肪含量较高，烹饪或食用时搭配蔬菜更均衡。"
        food.carb >= 40 && (food.gi ?: 0) >= 70 -> "碳水充足且升糖较快，运动前后吃最合适。"
        food.protein >= 15 -> "高蛋白食材，运动后食用有助于肌肉修复。"
        food.category == "快乐水果" -> "水果虽好但果糖集中，每天 200~350g 为宜。"
        else -> "热量适中，均衡搭配一日饮食即可安心食用。"
    }

    private fun Double.round1(): String {
        val v = (this * 10).roundToInt() / 10.0
        return if (v % 1.0 == 0.0) "${v.toInt()}" else "$v"
    }
}