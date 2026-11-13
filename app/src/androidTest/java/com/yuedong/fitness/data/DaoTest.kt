package com.yuedong.fitness.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yuedong.fitness.data.db.AppDatabase
import com.yuedong.fitness.data.db.PlanDayWithExercises
import com.yuedong.fitness.data.db.entity.BodyMetricEntity
import com.yuedong.fitness.data.db.entity.CheckInRecordEntity
import com.yuedong.fitness.data.db.entity.DiaryEntryEntity
import com.yuedong.fitness.data.db.entity.PlanDayEntity
import com.yuedong.fitness.data.db.entity.PlanExerciseEntity
import com.yuedong.fitness.data.db.entity.UserProfileEntity
import com.yuedong.fitness.data.db.entity.WorkoutPlanEntity
import com.yuedong.fitness.data.repository.PlanRepositoryImpl
import com.yuedong.fitness.data.settings.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Room 数据层真机测试（运行在已连接的安卓设备上）。
 * 覆盖：单行资料覆盖、打卡/体重/日记的"同日幂等"、计划级联删除、
 * 计划整包写入（Repository 事务）与激活计划切换。
 */
@RunWith(AndroidJUnit4::class)
class DaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun profile_upsert_overwritesSingleRow() = runBlocking {
        val dao = db.profileDao()
        dao.upsert(
            UserProfileEntity(
                gender = "MALE", age = 25, heightCm = 175,
                currentWeightKg = 70.0, targetWeightKg = 65.0,
                weeklyExerciseDays = 3, purpose = "FAT_LOSS",
                createdAt = 1L, updatedAt = 1L
            )
        )
        dao.upsert(
            UserProfileEntity(
                gender = "MALE", age = 26, heightCm = 175,
                currentWeightKg = 69.5, targetWeightKg = 65.0,
                weeklyExerciseDays = 4, purpose = "FAT_LOSS",
                createdAt = 1L, updatedAt = 2L
            )
        )
        val profile = dao.observeProfile().first()
        assertEquals(26, profile?.age)
    }

    @Test
    fun checkIn_sameDate_keptSingleRecord() = runBlocking {
        val dao = db.checkInRecordDao()
        dao.save(CheckInRecordEntity(date = "2026-09-05", planId = 1, note = "第一次", createdAt = 1L))
        dao.save(CheckInRecordEntity(date = "2026-09-05", planId = 1, note = "第二次", createdAt = 2L))
        val all = dao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("第二次", all[0].note)
    }

    @Test
    fun bodyMetric_sameDate_replaced() = runBlocking {
        val dao = db.bodyMetricDao()
        dao.save(BodyMetricEntity(date = "2026-09-05", weightKg = 70.0, createdAt = 1L, updatedAt = 1L))
        dao.save(BodyMetricEntity(date = "2026-09-05", weightKg = 69.8, createdAt = 1L, updatedAt = 2L))
        val latest = dao.observeLatest().first()
        assertEquals(69.8, latest?.weightKg!!, 0.001)
        assertEquals(1, dao.observeAll().first().size)
    }

    @Test
    fun diary_sameDate_replaced() = runBlocking {
        val dao = db.diaryEntryDao()
        dao.save(DiaryEntryEntity(date = "2026-09-05", mood = 4, content = "练完了", updatedAt = 1L))
        dao.save(DiaryEntryEntity(date = "2026-09-05", mood = 5, content = "状态很好", updatedAt = 2L))
        assertEquals("状态很好", dao.getByDate("2026-09-05")?.content)
        assertEquals(1, dao.observeAll().first().size)
    }

    @Test
    fun planContent_cascadeDeleteRemovesChildren() = runBlocking {
        val planDao = db.workoutPlanDao()
        val dayDao = db.planDayDao()
        val exerciseDao = db.planExerciseDao()

        val planId = planDao.insert(
            WorkoutPlanEntity(
                name = "测试计划", description = "用于测试",
                source = "BUILTIN", purposeTag = "FAT_LOSS", daysPerWeek = 3
            )
        )
        val dayId = dayDao.insert(
            PlanDayEntity(planId = planId, orderIndex = 0, title = "全身唤醒", isRestDay = false)
        )
        exerciseDao.insertAll(
            listOf(
                PlanExerciseEntity(planDayId = dayId, name = "深蹲", sets = 3, repsOrDuration = "12次", tip = "背部挺直")
            )
        )

        assertEquals(1, exerciseDao.observeByPlan(planId).first().size)

        planDao.deleteById(planId)

        assertTrue(dayDao.getByPlan(planId).isEmpty())
        assertTrue(exerciseDao.observeByPlan(planId).first().isEmpty())
    }

    @Test
    fun planRepository_insertWithContent_andSetActivePlan() = runBlocking {
        val settings = SettingsDataStore(ApplicationProvider.getApplicationContext())
        val repo = PlanRepositoryImpl(
            database = db,
            planDao = db.workoutPlanDao(),
            planDayDao = db.planDayDao(),
            planExerciseDao = db.planExerciseDao(),
            settings = settings
        )

        val content = listOf(
            PlanDayWithExercises(
                day = PlanDayEntity(planId = 0, orderIndex = 0, title = "训练日", isRestDay = false),
                exercises = listOf(
                    PlanExerciseEntity(planDayId = 0, name = "俯卧撑", sets = 3, repsOrDuration = "10次", tip = "核心收紧")
                )
            ),
            PlanDayWithExercises(
                day = PlanDayEntity(planId = 0, orderIndex = 1, title = "休息", isRestDay = true),
                exercises = emptyList()
            )
        )
        val planId = repo.insertPlanWithContent(
            WorkoutPlanEntity(
                name = "整包计划", description = "d",
                source = "CUSTOM", purposeTag = "HEALTH", daysPerWeek = 2
            ),
            content
        )

        // 整包写入后：2 个安排日，第一个安排日挂 1 个动作，外键已重映射
        val days = repo.getPlanDays(planId)
        assertEquals(2, days.size)
        val exercises = repo.getExercisesOfDay(days[0].id)
        assertEquals(1, exercises.size)
        assertEquals("俯卧撑", exercises[0].name)

        // 激活切换：数据表与配置存储同时生效
        repo.setActivePlan(planId)
        assertEquals(planId, repo.observeActivePlan().first()?.id)
        assertEquals(planId, settings.observeActivePlanId().first())
    }
}
