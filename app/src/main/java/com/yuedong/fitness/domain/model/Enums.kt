package com.yuedong.fitness.domain.model

/**
 * 性别（中文标签用于界面，名称用于数据库存储）。
 */
enum class Gender(val label: String) {
    MALE("男"),
    FEMALE("女");

    companion object {
        fun fromValue(value: String): Gender? = entries.find { it.name == value }
    }
}

/**
 * 健身目的。
 */
enum class Purpose(val label: String, val description: String) {
    FAT_LOSS("减脂", "燃烧脂肪，减轻体重"),
    MUSCLE_GAIN("增肌", "增加肌肉与力量"),
    TONE("塑形", "紧致线条，轻微减脂"),
    HEALTH("健康", "保持活力，改善体质");

    companion object {
        fun fromValue(value: String): Purpose? = entries.find { it.name == value }
    }
}

/**
 * BMI 分类（边界：18.5 / 24 / 28）。
 */
enum class BmiCategory(val label: String) {
    UNDERWEIGHT("偏瘦"),
    NORMAL("正常"),
    OVERWEIGHT("超重"),
    OBESE("肥胖")
}
