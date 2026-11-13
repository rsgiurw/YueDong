package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.CustomExerciseDao
import com.yuedong.fitness.data.db.entity.CustomExerciseEntity
import kotlinx.coroutines.flow.Flow

/** 自定义动作仓库：计划编辑输入过的动作（含组数/次数）自动入库，供以后复用。 */
interface CustomExerciseRepository {
    fun observeAll(): Flow<List<CustomExerciseEntity>>
    fun observeDeleted(): Flow<List<CustomExerciseEntity>>
    suspend fun getByName(name: String): CustomExerciseEntity?
    /** videoPath/coverPath 语义：null = 保留原值；"" = 清除；非空 = 覆盖为新路径。sections：null = 保留原值 */
    suspend fun upsert(
        name: String,
        setsText: String,
        repsOrDuration: String,
        difficulty: String,
        description: String,
        videoPath: String? = null,
        coverPath: String? = null,
        sections: String? = null,
        bodyPart: String = ""
    )
    suspend fun delete(name: String)
    suspend fun restore(name: String)
    suspend fun purge(name: String)
}

class CustomExerciseRepositoryImpl(private val dao: CustomExerciseDao) : CustomExerciseRepository {
    override fun observeAll(): Flow<List<CustomExerciseEntity>> = dao.observeAll()
    override fun observeDeleted(): Flow<List<CustomExerciseEntity>> = dao.observeDeleted()

    override suspend fun getByName(name: String): CustomExerciseEntity? = dao.getByName(name.trim())

    override suspend fun upsert(
        name: String,
        setsText: String,
        repsOrDuration: String,
        difficulty: String,
        description: String,
        videoPath: String?,
        coverPath: String?,
        sections: String?,
        bodyPart: String
    ) {
        // 同名编辑：保留原 id 更新，避免 REPLACE + 自增 id 在唯一索引下静默失败
        val trimmedName = name.trim()
        val existing = dao.getByName(trimmedName)
        val entity = if (existing != null) {
            existing.copy(
                setsText = setsText.trim(),
                repsOrDuration = repsOrDuration.trim(),
                difficulty = difficulty,
                description = description.trim(),
                videoPath = when (videoPath) {
                    null -> existing.videoPath
                    "" -> null
                    else -> videoPath
                },
                coverPath = when (coverPath) {
                    null -> existing.coverPath
                    "" -> null
                    else -> coverPath
                },
                sections = sections ?: existing.sections,
                bodyPart = bodyPart
            )
        } else {
            CustomExerciseEntity(
                name = trimmedName,
                setsText = setsText.trim(),
                repsOrDuration = repsOrDuration.trim(),
                difficulty = difficulty,
                description = description.trim(),
                videoPath = videoPath?.ifEmpty { null },
                coverPath = coverPath?.ifEmpty { null },
                sections = sections ?: "",
                bodyPart = bodyPart,
                createdAt = System.currentTimeMillis()
            )
        }
        dao.upsert(entity)
    }

    override suspend fun delete(name: String) {
        dao.softDeleteByName(name, System.currentTimeMillis())
    }

    override suspend fun restore(name: String) {
        dao.restoreByName(name)
    }

    override suspend fun purge(name: String) {
        dao.deleteByName(name)
    }
}
