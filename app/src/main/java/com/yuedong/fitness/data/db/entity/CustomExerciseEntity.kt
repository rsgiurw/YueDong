package com.yuedong.fitness.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 用户自定义动作表：名称唯一，带默认组数与次数/时长。
 * 在计划编辑里输入过的非内置动作会自动入库，供以后复用。
 */
@Entity(tableName = "custom_exercise", indices = [Index(value = ["name"], unique = true)])
data class CustomExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val setsText: String = "1组",
    val repsOrDuration: String = "1次",
    val difficulty: String = "入门",
    /** 部位标签（胸部/背部/腿部…）；空 = 未选择 */
    val bodyPart: String = "",
    val description: String = "",
    /** 示范视频路径（应用私有目录）；null = 未上传 */
    val videoPath: String? = null,
    /** 封面图片路径（应用私有目录）；null = 未上传 */
    val coverPath: String? = null,
    /** 自定义图文段落：段与段用 \u001E 分隔，标题与正文用 \u001F 分隔 */
    val sections: String = "",
    val createdAt: Long,
    /** 软删除时间;null = 未删除,进回收站可恢复 */
    val deletedAt: Long? = null
)
