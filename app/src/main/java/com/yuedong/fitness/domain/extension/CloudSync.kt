package com.yuedong.fitness.domain.extension

/**
 * 云同步服务（V1 未实现，仅声明扩展点）。
 *
 * 接入要求：需要先引入账号体系；同步前必须获得用户明确授权；
 * 本地数据始终是权威副本（offline-first），云端仅做备份与多端同步。
 *
 * 预留原因：Repository 层已隔离数据源，接入云同步只需新增实现类，UI 不变。
 */
interface CloudSync {

    /** 将本地新增/变更的数据推送到云端。 */
    suspend fun push()

    /** 从云端拉取数据并合并到本地。 */
    suspend fun pull()
}
