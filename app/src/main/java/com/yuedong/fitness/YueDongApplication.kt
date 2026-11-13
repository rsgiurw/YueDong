package com.yuedong.fitness

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.yuedong.fitness.data.db.AppDatabase
import com.yuedong.fitness.data.db.MIGRATION_1_2
import com.yuedong.fitness.data.db.MIGRATION_2_3
import com.yuedong.fitness.data.db.MIGRATION_3_4
import com.yuedong.fitness.data.db.MIGRATION_4_5
import com.yuedong.fitness.data.db.MIGRATION_5_6
import com.yuedong.fitness.data.db.MIGRATION_6_7
import com.yuedong.fitness.data.db.MIGRATION_7_8
import com.yuedong.fitness.data.db.MIGRATION_8_9
import com.yuedong.fitness.data.db.MIGRATION_9_10
import com.yuedong.fitness.data.db.MIGRATION_10_11
import com.yuedong.fitness.data.db.MIGRATION_11_12
import com.yuedong.fitness.data.db.MIGRATION_12_13
import com.yuedong.fitness.data.db.MIGRATION_13_14
import com.yuedong.fitness.data.db.MIGRATION_14_15
import com.yuedong.fitness.data.db.MIGRATION_15_16
import com.yuedong.fitness.data.db.MIGRATION_16_17
import com.yuedong.fitness.data.db.MIGRATION_17_18
import com.yuedong.fitness.data.db.MIGRATION_18_19
import com.yuedong.fitness.data.db.MIGRATION_19_20
import com.yuedong.fitness.data.db.MIGRATION_20_21
import com.yuedong.fitness.data.db.MIGRATION_21_22
import com.yuedong.fitness.data.db.MIGRATION_22_23
import com.yuedong.fitness.data.repository.BodyMetricRepository
import com.yuedong.fitness.data.repository.BodyMetricRepositoryImpl
import com.yuedong.fitness.data.repository.CheckInRepository
import com.yuedong.fitness.data.repository.DayExtraExerciseRepository
import com.yuedong.fitness.data.repository.DayExtraExerciseRepositoryImpl
import com.yuedong.fitness.data.repository.CheckInRepositoryImpl
import com.yuedong.fitness.data.repository.CustomExerciseRepository
import com.yuedong.fitness.data.repository.ExerciseFavoriteRepository
import com.yuedong.fitness.data.repository.ItemFlagRepository
import com.yuedong.fitness.data.repository.ExerciseFavoriteRepositoryImpl
import com.yuedong.fitness.data.repository.ItemFlagRepositoryImpl
import com.yuedong.fitness.data.repository.CustomExerciseRepositoryImpl
import com.yuedong.fitness.data.repository.DietLogRepository
import com.yuedong.fitness.data.repository.DietLogRepositoryImpl
import com.yuedong.fitness.data.repository.DiaryRepository
import com.yuedong.fitness.data.repository.DiaryRepositoryImpl
import com.yuedong.fitness.data.repository.WaterRepository
import com.yuedong.fitness.data.repository.WaterRepositoryImpl
import com.yuedong.fitness.data.repository.ExerciseProgressRepository
import com.yuedong.fitness.data.repository.ExerciseProgressRepositoryImpl
import com.yuedong.fitness.data.repository.PhotoWallRepository
import com.yuedong.fitness.data.repository.MemoRepository
import com.yuedong.fitness.data.repository.MemoRepositoryImpl
import com.yuedong.fitness.data.repository.PhotoWallRepositoryImpl
import com.yuedong.fitness.data.repository.PlanRepository
import com.yuedong.fitness.data.repository.PlanRepositoryImpl
import com.yuedong.fitness.data.repository.RemindersRepository
import com.yuedong.fitness.data.repository.RemindersRepositoryImpl
import com.yuedong.fitness.data.repository.ProfileRepository
import com.yuedong.fitness.data.repository.ProfileRepositoryImpl
import com.yuedong.fitness.data.sample.SampleDataGenerator
import com.yuedong.fitness.data.settings.SettingsDataStore
import com.yuedong.fitness.domain.logic.BuiltinPlanSeed
import com.yuedong.fitness.domain.logic.BuiltinRuleRecommender
import com.yuedong.fitness.domain.logic.PlanRecommender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 轻量手动依赖容器（设计包：模块化单体，不引入 DI 框架）。
 * 职责：创建并持有数据库、配置存储与全部仓库单例，供各 ViewModel 取用。
 */
class AppContainer(context: Context) {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "yuedong.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23)
            .build()

    val settings: SettingsDataStore = SettingsDataStore(context)

    val profileRepository: ProfileRepository = ProfileRepositoryImpl(database.profileDao())

    val checkInRepository: CheckInRepository = CheckInRepositoryImpl(database.checkInRecordDao())

    val planRepository: PlanRepository = PlanRepositoryImpl(
        database = database,
        checkInRepository = checkInRepository,
        planDao = database.workoutPlanDao(),
        planDayDao = database.planDayDao(),
        planExerciseDao = database.planExerciseDao(),
        settings = settings
    )

    val diaryRepository: DiaryRepository = DiaryRepositoryImpl(database.diaryEntryDao())

    val bodyMetricRepository: BodyMetricRepository = BodyMetricRepositoryImpl(database.bodyMetricDao())

    val waterRepository: WaterRepository = WaterRepositoryImpl(database.waterLogDao())

    val photoWallRepository: PhotoWallRepository = PhotoWallRepositoryImpl(database.photoWallDao())

    val memoRepository: MemoRepository = MemoRepositoryImpl(database.memoDao())

    val remindersRepository: RemindersRepository = RemindersRepositoryImpl(database.remindersDao())

    val dayExtraExerciseRepository: DayExtraExerciseRepository =
        DayExtraExerciseRepositoryImpl(database.dayExtraExerciseDao())

    val dietLogRepository: DietLogRepository = DietLogRepositoryImpl(database.dietLogDao(), database.customFoodDao())

    val customExerciseRepository: CustomExerciseRepository =
        CustomExerciseRepositoryImpl(database.customExerciseDao())

    val exerciseFavoriteRepository: ExerciseFavoriteRepository =
        ExerciseFavoriteRepositoryImpl(database.exerciseFavoriteDao())

    val itemFlagRepository: ItemFlagRepository =
        ItemFlagRepositoryImpl(database.listItemFlagDao())


    val exerciseProgressRepository: ExerciseProgressRepository =
        ExerciseProgressRepositoryImpl(database.exerciseProgressDao())

    val sampleDataGenerator = SampleDataGenerator(
        checkInRepository = checkInRepository,
        bodyMetricRepository = bodyMetricRepository,
        diaryRepository = diaryRepository,
        dietLogRepository = dietLogRepository
    )

    val planRecommender: PlanRecommender = BuiltinRuleRecommender()

    /** 调试：清空全部本地数据（数据库 + 偏好），回到首次安装状态，并重新播种内置计划。 */
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        database.clearAllTables()
        settings.reset()
        // 清空即重新开始：第一天 = 清空当天
        settings.ensureUsageStartDate()
        // 清空后内置计划必须立刻补回；随后增量播种：老用户升级后也能看到新增分类计划
        BuiltinPlanSeed.all().forEach { seed ->
            planRepository.insertPlanWithContent(seed.plan, seed.content)
        }
        val existingNames = planRepository.observePlans().first().map { it.name }.toSet()
        BuiltinPlanSeed.all().forEach { seed ->
            if (seed.plan.name !in existingNames) {
                planRepository.insertPlanWithContent(seed.plan, seed.content)
            }
        }
    }

    init {
        applicationScope.launch {
            // 内置计划播种：整个生命周期只执行一次；之后用户删除内置计划不会再复活
            if (!settings.observeBuiltinSeeded().first()) {
                if (planRepository.builtinPlanCount() == 0) {
                    BuiltinPlanSeed.all().forEach { seed ->
                        planRepository.insertPlanWithContent(seed.plan, seed.content)
                    }
                }
                settings.setBuiltinSeeded(true)
            }
        }
    }
}

/**
 * 应用入口（模块契约见设计包 §8 AppShell）。
 */
class YueDongApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // 历史版本用 WorkManager 周期任务做提醒,现已改用精确闹钟:清掉遗留任务避免双重提醒
        runCatching { androidx.work.WorkManager.getInstance(this).cancelAllWork() }
        // 记录「第一天」(首次使用日期)：补打卡只允许第一天及以后
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            container.settings.ensureUsageStartDate()
        }
    }
}
