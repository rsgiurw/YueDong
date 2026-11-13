package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 用户自定义食物表：名称唯一；营养值均为"每 100 克"。
 */
@Entity(tableName = "custom_food", indices = [Index(value = ["name"], unique = true)])
data class CustomFoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kcalPer100g: Int,
    val proteinPer100g: Double,
    val carbPer100g: Double,
    val fatPer100g: Double,
    val description: String = "",
    /** 食材图片路径（应用私有目录）；null = 未上传 */
    val imagePath: String? = null,
    /** 自定义图文段落：段与段用 \u001E 分隔，标题与正文用 \u001F 分隔 */
    val sections: String = "",
    val createdAt: Long,
    /** 软删除时间;null = 未删除,进回收站可恢复 */
    val deletedAt: Long? = null
)
