package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 身材照片墙:按日期存本地对比照,图片文件在 filesDir/media,卸载即清理。
 */
@Entity(tableName = "photo_wall")
data class PhotoWallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 归属日期 yyyy-MM-dd */
    val date: String,
    val path: String,
    val createdAt: Long,
    /** 软删除时间;null = 未删除,进回收站可恢复 */
    val deletedAt: Long? = null
)
