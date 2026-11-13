package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户资料表（单行，id 恒为 1）。
 * gender 取值：MALE / FEMALE；purpose 取值：FAT_LOSS / MUSCLE_GAIN / TONE / HEALTH。
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1,
    val nickname: String = "悦动用户",
    val gender: String,
    val age: Int,
    val heightCm: Int,
    val currentWeightKg: Double,
    val targetWeightKg: Double,
    val weeklyExerciseDays: Int,
    val purpose: String,
    /** 头像本地路径（filesDir/media）；null = 未设置，展示按性别区分的默认头像 */
    val avatarPath: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
