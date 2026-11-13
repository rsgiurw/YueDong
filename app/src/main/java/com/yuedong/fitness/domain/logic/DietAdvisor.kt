package com.yuedong.fitness.domain.logic

import com.yuedong.fitness.domain.model.Purpose

/**
 * 单条饮食建议。foods 为可选的"推荐直接记录"的食物名（需在 FoodCatalog 或自定义食物中存在）。
 */
data class DietTip(
    val emoji: String,
    val title: String,
    val detail: String,
    val foods: List<String> = emptyList()
)

/**
 * 饮食建议文案库（模块契约见设计包 §8 Diet）。
 * 职责：按健身目的返回 3~4 条通用建议；非职责：不做热量计算（FitnessCalculator）、不查食物数据。
 * 文案集中在此便于审校；只给一般性建议，页面底部配免责说明。
 */
object DietAdvisor {

    fun tips(purpose: Purpose): List<DietTip> = when (purpose) {
        Purpose.FAT_LOSS -> listOf(
            DietTip("🔥", "热量缺口要合理", "每日缺口 300~500 千卡即可，减太快容易掉肌肉"),
            DietTip("🍗", "蛋白质优先", "每餐先吃蛋白质，增加饱腹感还能保住肌肉"),
            DietTip("🥤", "少喝含糖饮料", "一杯奶茶的热量可能抵一顿正餐，换成水或无糖茶"),
            DietTip("🍚", "主食粗细搭配", "用糙米、燕麦替换部分白米白面，饱腹更持久")
        )
        Purpose.MUSCLE_GAIN -> listOf(
            DietTip("🍚", "碳水是训练的燃料", "训练前后补充碳水，力量和恢复都会更好"),
            DietTip("🍗", "蛋白质分摊到每餐", "每餐 20~40 克，吸收利用率比一顿吃够更高"),
            DietTip("😋", "要吃够，别怕吃", "增肌需要热量略盈余，吃不够练得再狠也难长"),
            DietTip("💧", "多喝水", "肌肉含水比例高，饮水不足会直接影响训练状态")
        )
        Purpose.TONE -> listOf(
            DietTip("⚖️", "热量持平或微缺口", "塑形期不需要极端节食，稳住热量慢慢来"),
            DietTip("🥗", "蛋白质加蔬菜打底", "每餐一掌心蛋白质量 + 双拳蔬菜，简单好执行"),
            DietTip("🚫", "少油炸少精制糖", "油炸食品和甜点最容易悄悄突破热量预算"),
            DietTip("🍽️", "三餐规律", "固定的进食节奏能减少嘴馋和暴食")
        )
        Purpose.HEALTH -> listOf(
            DietTip("🌈", "食物多样化", "每天争取 12 种以上食材，营养素才全面"),
            DietTip("🥦", "多吃深色蔬菜", "深绿、橙红色的蔬菜维生素更丰富"),
            DietTip("🧂", "少油少盐", "每日盐不超过 5 克，给味觉减减负"),
            DietTip("🍎", "水果吃整个的", "榨汁会损失膳食纤维，糖分还更容易超标")
        )
    }

    /** 按数据库存储的枚举名取建议；未知值回退到健康建议。 */
    fun tipsFor(purposeName: String): List<DietTip> =
        tips(Purpose.fromValue(purposeName) ?: Purpose.HEALTH)

    /** 单餐示例：emoji + 餐名 + 推荐食物搭配（热量由目标热量按比例分配）。 */
    data class MealExample(val emoji: String, val name: String, val foods: String)

    /** 一日三餐示例：按健身目的定制食物搭配，热量比例统一 3:4:3。 */
    fun meals(purpose: Purpose): List<MealExample> = when (purpose) {
        Purpose.FAT_LOSS -> listOf(
            MealExample("🌅", "早餐", "燕麦 40g + 鸡蛋 1 个 + 无糖豆浆 250ml"),
            MealExample("☀️", "午餐", "糙米饭 150g + 鸡胸肉 150g + 绿叶蔬菜 200g"),
            MealExample("🌙", "晚餐", "清蒸鱼 150g + 蔬菜沙拉 200g，主食减半")
        )
        Purpose.MUSCLE_GAIN -> listOf(
            MealExample("🌅", "早餐", "鸡蛋 2 个 + 全麦面包 2 片 + 牛奶 300ml"),
            MealExample("☀️", "午餐", "米饭 200g + 瘦牛肉 150g + 时蔬 200g"),
            MealExample("🌙", "晚餐", "面条 200g + 鸡腿肉 150g + 西蓝花 150g")
        )
        Purpose.TONE -> listOf(
            MealExample("🌅", "早餐", "酸奶 200g + 鸡蛋 1 个 + 水果 1 个"),
            MealExample("☀️", "午餐", "米饭 150g + 瘦肉 120g + 时蔬 200g"),
            MealExample("🌙", "晚餐", "杂粮粥 1 碗 + 豆腐 150g + 青菜 200g")
        )
        Purpose.HEALTH -> listOf(
            MealExample("🌅", "早餐", "杂粮粥 1 碗 + 鸡蛋 1 个 + 坚果 15g"),
            MealExample("☀️", "午餐", "米饭 150g + 鱼肉 150g + 深色蔬菜 200g"),
            MealExample("🌙", "晚餐", "杂粮 100g + 豆制品 150g + 青菜 200g")
        )
    }

    /** 按数据库存储的枚举名取三餐示例。 */
    fun mealsFor(purposeName: String): List<MealExample> =
        meals(Purpose.fromValue(purposeName) ?: Purpose.HEALTH)
}
