package com.yuedong.fitness.data.repository

import com.yuedong.fitness.data.db.dao.ProfileDao
import com.yuedong.fitness.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/** 用户资料仓库：问卷、计算、个人页都只通过它访问资料。 */
interface ProfileRepository {
    fun observeProfile(): Flow<UserProfileEntity?>
    suspend fun save(profile: UserProfileEntity)
}

class ProfileRepositoryImpl(private val dao: ProfileDao) : ProfileRepository {
    override fun observeProfile(): Flow<UserProfileEntity?> = dao.observeProfile()

    override suspend fun save(profile: UserProfileEntity) {
        dao.upsert(profile)
    }
}
