package com.yuedong.fitness.data.sample

import com.yuedong.fitness.data.db.entity.BodyMetricEntity
import com.yuedong.fitness.data.db.entity.CheckInRecordEntity
import com.yuedong.fitness.data.db.entity.DiaryEntryEntity
import com.yuedong.fitness.data.db.entity.DietLogEntity
import com.yuedong.fitness.data.repository.BodyMetricRepository
import com.yuedong.fitness.data.repository.CheckInRepository
import com.yuedong.fitness.data.repository.DiaryRepository
import com.yuedong.fitness.data.repository.DietLogRepository
import com.yuedong.fitness.domain.logic.FoodCatalog
import com.yuedong.fitness.util.AppClock
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 演示数据生成器：显式由用户触发，向本地数据库写入过去若干天的
 * 打卡/体重/日记/饮食示例记录，用于调试与作品集演示。
 * 注意：同一天的记录会被覆盖（打卡/体重/日记为每日一条），演示数据可能覆盖当天真实记录。
 */
class SampleDataGenerator(
    private val checkInRepository: CheckInRepository,
    private val bodyMetricRepository: BodyMetricRepository,
    private val diaryRepository: DiaryRepository,
    private val dietLogRepository: DietLogRepository
) {

    suspend fun generate(days: Int = 21) {
        val today = AppClock.today()
        val random = Random(20260905) // 固定种子：多次生成结果一致，便于演示
        val now = System.currentTimeMillis()
        val diaryTexts = listOf(
            "练得很爽，出汗了",
            "有点累但坚持下来了",
            "状态一般，做完就好",
            "今天的深蹲特别到位",
            "练完神清气爽"
        )
        val mealSlots = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")

        for (offset in days - 1 downTo 0) {
            val date = today.minusDays(offset.toLong())
            val dateText = date.toString()

            // 打卡：约每 4 天休息一天
            if (offset % 4 != 2) {
                checkInRepository.save(
                    CheckInRecordEntity(date = dateText, planId = 1, createdAt = now - offset * 86_400_000L)
                )
                // 打卡日的日记
                if (offset % 3 == 0) {
                    diaryRepository.save(
                        DiaryEntryEntity(
                            date = dateText,
                            mood = 3 + random.nextInt(3),
                            content = diaryTexts[random.nextInt(diaryTexts.size)],
                            updatedAt = now
                        )
                    )
                }
            }

            // 体重：每 2 天一次，整体缓慢下降并带自然波动
            if (offset % 2 == 0) {
                val weight = 69.5 - (days - offset) * 0.06 + random.nextDouble() * 0.3
                bodyMetricRepository.save(
                    BodyMetricEntity(
                        date = dateText,
                        weightKg = Math.round(weight * 10) / 10.0,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }

            // 饮食：最近 7 天，每天 2~3 笔
            if (offset <= 6) {
                repeat(2 + random.nextInt(2)) { index ->
                    val food = FoodCatalog.all[random.nextInt(FoodCatalog.all.size)]
                    val grams = 100.0 + random.nextInt(4) * 50
                    val ratio = grams / 100.0
                    dietLogRepository.addLog(
                        DietLogEntity(
                            date = dateText,
                            mealType = mealSlots[(index + random.nextInt(2)) % mealSlots.size],
                            foodName = food.name,
                            grams = grams,
                            calories = (food.kcal * ratio).roundToInt(),
                            proteinG = (food.protein * ratio * 10).roundToInt() / 10.0,
                            carbG = (food.carb * ratio * 10).roundToInt() / 10.0,
                            fatG = (food.fat * ratio * 10).roundToInt() / 10.0,
                            createdAt = now - offset * 3_600_000L + index
                        )
                    )
                }
            }
        }
    }
}
