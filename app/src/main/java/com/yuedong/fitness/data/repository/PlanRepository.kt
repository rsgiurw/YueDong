package com.yuedong.fitness.data.repository

import androidx.room.withTransaction
import com.yuedong.fitness.data.db.AppDatabase
import com.yuedong.fitness.data.db.PlanDayWithExercises
import com.yuedong.fitness.data.db.dao.PlanDayDao
import com.yuedong.fitness.data.db.dao.PlanExerciseDao
import com.yuedong.fitness.data.db.dao.WorkoutPlanDao
import com.yuedong.fitness.data.db.entity.PlanDayEntity
import com.yuedong.fitness.data.db.entity.PlanExerciseEntity
import com.yuedong.fitness.data.db.entity.WorkoutPlanEntity
import com.yuedong.fitness.data.settings.SettingsDataStore
import kotlinx.coroutines.flow.Flow

/**
 * 训练计划仓库：计划/安排/动作的统一入口，并负责"激活计划"的一致性
 * （workout_plan.isActive 与 DataStore.activePlanId 只允许经由本类修改）。
 */
interface PlanRepository {
    fun observePlans(): Flow<List<WorkoutPlanEntity>>
    fun observeDeletedPlans(): Flow<List<WorkoutPlanEntity>>
    fun observePlan(planId: Long): Flow<WorkoutPlanEntity?>
    fun observeActivePlan(): Flow<WorkoutPlanEntity?>
    fun observeActivePlanId(): Flow<Long?>
    fun observePlanDays(planId: Long): Flow<List<PlanDayEntity>>
    fun observeExercisesOfPlan(planId: Long): Flow<List<PlanExerciseEntity>>
    suspend fun getPlan(planId: Long): WorkoutPlanEntity?
    suspend fun getPlanDays(planId: Long): List<PlanDayEntity>
    suspend fun getExercisesOfDay(planDayId: Long): List<PlanExerciseEntity>
    suspend fun getExercisesOfPlan(planId: Long): List<PlanExerciseEntity>
    suspend fun builtinPlanCount(): Int

    /** 整包写入一个计划（计划 + 每日安排 + 动作），单事务保证原子性，返回新计划 id。 */
    suspend fun insertPlanWithContent(plan: WorkoutPlanEntity, content: List<PlanDayWithExercises>): Long

    /** 编辑已有计划：单事务内更新计划本体、清空旧安排后按新内容重写，返回计划 id。 */
    suspend fun updatePlanWithContent(
        planId: Long,
        plan: WorkoutPlanEntity,
        content: List<PlanDayWithExercises>
    ): Long

    /** 删除自定义计划：仅允许删除 CUSTOM 来源；若为当前计划则同时清空激活状态。历史打卡保留。 */
    suspend fun deleteCustomPlan(planId: Long): Boolean
    suspend fun deleteAnyPlan(planId: Long): Boolean
    suspend fun restorePlan(planId: Long)
    suspend fun purgePlan(planId: Long)

    /** 切换激活计划：同时更新数据表与配置存储。 */
    suspend fun setActivePlan(planId: Long)
    suspend fun setFavorite(planId: Long, fav: Boolean)
    suspend fun setPinned(planId: Long, pinned: Boolean)
}

class PlanRepositoryImpl(
    private val database: AppDatabase,
    private val checkInRepository: CheckInRepository,
    private val planDao: WorkoutPlanDao,
    private val planDayDao: PlanDayDao,
    private val planExerciseDao: PlanExerciseDao,
    private val settings: SettingsDataStore
) : PlanRepository {

    override fun observePlans(): Flow<List<WorkoutPlanEntity>> = planDao.observeAll()
    override fun observeDeletedPlans(): Flow<List<WorkoutPlanEntity>> = planDao.observeDeleted()

    override fun observePlan(planId: Long): Flow<WorkoutPlanEntity?> = planDao.observeById(planId)

    override fun observeActivePlan(): Flow<WorkoutPlanEntity?> = planDao.observeActive()

    override fun observeActivePlanId(): Flow<Long?> = settings.observeActivePlanId()

    override fun observePlanDays(planId: Long): Flow<List<PlanDayEntity>> = planDayDao.observeByPlan(planId)

    override fun observeExercisesOfPlan(planId: Long): Flow<List<PlanExerciseEntity>> =
        planExerciseDao.observeByPlan(planId)

    override suspend fun getPlan(planId: Long): WorkoutPlanEntity? = planDao.getById(planId)

    override suspend fun getPlanDays(planId: Long): List<PlanDayEntity> = planDayDao.getByPlan(planId)

    override suspend fun getExercisesOfDay(planDayId: Long): List<PlanExerciseEntity> =
        planExerciseDao.getByDay(planDayId)

    override suspend fun getExercisesOfPlan(planId: Long): List<PlanExerciseEntity> =
        planExerciseDao.getByPlan(planId)

    override suspend fun builtinPlanCount(): Int = planDao.countBuiltin()

    override suspend fun insertPlanWithContent(
        plan: WorkoutPlanEntity,
        content: List<PlanDayWithExercises>
    ): Long = database.withTransaction {
        val planId = planDao.insert(plan)
        content.forEach { item ->
            val dayId = planDayDao.insert(item.day.copy(planId = planId))
            planExerciseDao.insertAll(item.exercises.map { it.copy(planDayId = dayId) })
        }
        planId
    }

    override suspend fun setFavorite(planId: Long, fav: Boolean) {
        planDao.setFavorite(planId, fav)
    }

    override suspend fun setPinned(planId: Long, pinned: Boolean) {
        planDao.setPinned(planId, pinned)
    }

    override suspend fun setActivePlan(planId: Long) {
        planDao.clearAllActive()
        planDao.markActive(planId)
        settings.setActivePlanId(planId)
        settings.setActivePlanStartedAt(System.currentTimeMillis())
        // 今天尚未过去：当天的打卡归属应跟随最新计划（否则日详情会按旧计划回填动作）
        val today = com.yuedong.fitness.util.AppClock.todayString()
        checkInRepository.getByDate(today)?.let { existing ->
            if (existing.planId != planId) {
                checkInRepository.save(
                    existing.copy(planId = planId, createdAt = System.currentTimeMillis())
                )
            }
        }
    }

    override suspend fun updatePlanWithContent(
        planId: Long,
        plan: WorkoutPlanEntity,
        content: List<PlanDayWithExercises>
    ): Long = database.withTransaction {
        planDao.update(plan.copy(id = planId))
        planDayDao.deleteByPlan(planId)
        content.forEach { item ->
            val dayId = planDayDao.insert(item.day.copy(planId = planId))
            planExerciseDao.insertAll(item.exercises.map { it.copy(planDayId = dayId) })
        }
        planId
    }

    override suspend fun deleteAnyPlan(planId: Long): Boolean {
        val plan = planDao.getById(planId) ?: return false
        database.withTransaction {
            if (plan.isActive) {
                planDao.clearAllActive()
                settings.setActivePlanId(null)
            }
            planDao.softDeleteById(planId, System.currentTimeMillis())
        }
        return true
    }

    override suspend fun restorePlan(planId: Long) {
        planDao.restoreById(planId)
    }

    override suspend fun purgePlan(planId: Long) {
        database.withTransaction {
            planDao.deleteById(planId)
            planDayDao.deleteByPlan(planId) // plan_exercise 由外键级联删除
        }
    }

    override suspend fun deleteCustomPlan(planId: Long): Boolean {
        val plan = planDao.getById(planId) ?: return false
        if (plan.source != "CUSTOM") return false
        database.withTransaction {
            if (plan.isActive) {
                planDao.clearAllActive()
                settings.setActivePlanId(null)
            }
            planDao.deleteById(planId)
        }
        return true
    }
}
