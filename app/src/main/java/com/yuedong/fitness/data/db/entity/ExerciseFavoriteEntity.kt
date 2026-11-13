package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 动作收藏：按动作名收藏（内置动作与自定义动作通用），收藏时间倒序展示。 */
@Entity(tableName = "exercise_favorite")
data class ExerciseFavoriteEntity(
    @PrimaryKey val exerciseName: String,
    val createdAt: Long
)
