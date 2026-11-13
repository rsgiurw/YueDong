package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 饮食记录表：每条 = 某天某餐吃的一份食物（营养值在记录时按克数快照计算）。
 * mealType 取值：BREAKFAST / LUNCH / DINNER / SNACK。
 */
@Entity(tableName = "diet_log", indices = [Index("date")])
data class DietLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val mealType: String,
    val foodName: String,
    val grams: Double,
    val calories: Int,
    val proteinG: Double,
    val carbG: Double,
    val fatG: Double,
    val createdAt: Long
)
