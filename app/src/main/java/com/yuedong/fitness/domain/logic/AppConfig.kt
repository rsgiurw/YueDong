package com.yuedong.fitness.domain.logic

import com.yuedong.fitness.domain.model.Purpose

/**
 * 业务常量集中配置（设计包 §6 配置管理层）。
 * 隐藏内部：活动系数表、各目的热量调整与营养素比例，禁止散落在界面代码里。
 */
object AppConfig {

    /** 每日目标热量下限保护（千卡）。 */
    const val femaleCalorieFloor = 1200
    const val maleCalorieFloor = 1500

    /** 脂肪供能占比（用于计算脂肪克数）。 */
    const val fatCalorieShare = 0.25

    /** 各健身目的的每日热量调整（千卡）。 */
    fun calorieAdjustment(purpose: Purpose): Int = when (purpose) {
        Purpose.FAT_LOSS -> -400
        Purpose.TONE -> -200
        Purpose.HEALTH -> 0
        Purpose.MUSCLE_GAIN -> 300
    }

    /** 各健身目的的每公斤体重蛋白质摄入系数（克/公斤）。 */
    fun proteinPerKg(purpose: Purpose): Double = when (purpose) {
        Purpose.FAT_LOSS, Purpose.MUSCLE_GAIN -> 1.8
        Purpose.TONE -> 1.5
        Purpose.HEALTH -> 1.2
    }
}
