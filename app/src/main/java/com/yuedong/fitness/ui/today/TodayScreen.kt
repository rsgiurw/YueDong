package com.yuedong.fitness.ui.today

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import com.yuedong.fitness.ui.theme.diveDark
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.ui.components.DiaryDialog
import com.yuedong.fitness.ui.components.MacroRing
import com.yuedong.fitness.ui.components.PrimaryCta
import com.yuedong.fitness.ui.components.SkeletonBox
import com.yuedong.fitness.ui.components.WeightLogDialog
import com.yuedong.fitness.ui.components.WashCard
import com.yuedong.fitness.ui.components.YueDialog
import com.yuedong.fitness.ui.components.YueToaster
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.BodyMetricEntity
import com.yuedong.fitness.data.db.entity.CheckInRecordEntity
import com.yuedong.fitness.data.db.entity.DiaryEntryEntity
import com.yuedong.fitness.data.db.entity.PlanDayEntity
import com.yuedong.fitness.data.db.entity.PlanExerciseEntity
import com.yuedong.fitness.data.db.entity.UserProfileEntity
import com.yuedong.fitness.data.db.entity.WorkoutPlanEntity
import com.yuedong.fitness.data.repository.BodyMetricRepository
import com.yuedong.fitness.data.repository.CheckInRepository
import com.yuedong.fitness.data.repository.DiaryRepository
import com.yuedong.fitness.data.repository.DietLogRepository
import com.yuedong.fitness.data.repository.ExerciseProgressRepository
import com.yuedong.fitness.data.repository.PlanRepository
import com.yuedong.fitness.data.repository.ProfileRepository
import com.yuedong.fitness.data.settings.SettingsDataStore
import com.yuedong.fitness.domain.logic.FitnessCalculator
import com.yuedong.fitness.domain.model.Purpose
import com.yuedong.fitness.ui.anim.rememberCountUpValue
import com.yuedong.fitness.ui.theme.diveGroundFor
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.ui.theme.DiveNumeric
import com.yuedong.fitness.ui.theme.DiveGradients
import com.yuedong.fitness.ui.theme.DiveDataColors
import com.yuedong.fitness.util.AppClock
import com.yuedong.fitness.ui.theme.strataColor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

/** 新手引导单步：目标 key（null = 纯文字收尾）+ 大字标题 + 说明 */
private data class CoachSpot(val key: String?, val title: String, val desc: String)

// 引导步骤表：文案与目标一一对应（ targets 的 bounds 由页面采集）
private val coachSteps = listOf(
    CoachSpot("checkin", "连续打卡 · 你的下潜深度", "练完打卡，连击越长潜得越深"),
    CoachSpot("training", "训练卡 · 每天的主页", "照着清单练，点动作名看示范视频"),
    CoachSpot("rings", "记录饮食，环就点亮", "点任意一圈去记录饮食，热量与营养实时更新"),
    CoachSpot("weight", "随手记体重", "两秒记一笔，趋势图每天长一个点"),
    CoachSpot("nav:1", "计划，在这里选", "内置三套 + 自定义，选一个就能开练"),
    CoachSpot("nav:2", "饮食，随吃随记", "食材库按克记录，三餐营养一目了然"),
    CoachSpot("nav:3", "记录，看见坚持", "打卡日历、体重趋势、进步报告都在这"),
    CoachSpot("nav:4", "我的，一切从这里设置", "修改资料、训练提醒、数据备份"),
    CoachSpot(null, "一切进步都需要有数据支撑哦", "去「我的 · 修改资料」完善身体数据，目标与建议马上生成")
)

// ===== 新手引导样式参数（可自行微调）=====
private val CoachDimAlpha = 0.62f           // 遮罩压暗浓度（越大越暗）
private val CoachHoleRadius = 18.dp         // 挖孔圆角半径
private val CoachOutlineGap = 5.dp          // 描边框与挖孔边缘的间距
private val CoachOutlineStroke = 2.5.dp     // 描边框线宽
private val CoachCalloutAboveGap = 170.dp   // 文案块在方框「上方」时的偏移（调小 = 文案更贴近方框）
private val CoachCalloutBelowGap = 18.dp    // 文案块在方框「下方」时的偏移
private val CoachClosingTop = 180.dp        // 收尾步（无方框）文案距屏幕顶的距离
private val CoachArrowStartX = 120.dp       // 箭头起笔相对终点的横向偏移
private val CoachArrowMidBulge = 60f        // 箭头弧度（px，越大越弯）
private val CoachArrowStroke = 3.dp         // 箭头线宽
private val CoachArrowDotR = 5.dp           // 箭头起笔圆点半径
private val CoachArrowHead = 16.dp          // 箭头头部尺寸
private val CoachBandHeight = 68.dp         // 底部标签亮带高度
private val CoachBandTipFrac = 0.42f        // 箭头落点在亮带内的高度比例（0=顶 1=底）
private val CoachArrowNavStartX = 90.dp     // 导航标签箭头：统一从落点左上方起笔
private val CoachHolePad = 2.dp             // 内容卡挖孔四周外扩，保证完全盖住卡片边缘

/**
 * 今日页聚合状态（模块契约见设计包 §8 CheckIn）。
 */
data class TodayUiState(
    val isLoading: Boolean = true,
    val hasProfile: Boolean = false,
    val purposeLabel: String? = null,
    val activePlan: WorkoutPlanEntity? = null,
    val todayDay: PlanDayEntity? = null,
    val todayExercises: List<PlanExerciseEntity> = emptyList(),
    val tomorrowLabel: String? = null,
    val planDayNumber: Int = 0,
    val planCycleLength: Int = 0,
    val isCheckedIn: Boolean = false,
    val streak: Int = 0,
    val totalCheckIns: Int = 0,
    val latestWeight: BodyMetricEntity? = null,
    val diary: DiaryEntryEntity? = null,
    /** 周期里程碑：今天恰好完成一个完整周期时非空 */
    val cycleMilestone: String? = null,
    /** 是否记录过任意一餐饮食（新手任务用） */
    val hasAnyDietLog: Boolean = false,
    /** 今日动作完成进度：planExerciseId → 是否完成 */
    val exerciseDone: Map<Long, Boolean> = emptyMap(),
    /** 今日饮食卡：已记热量/蛋白质与目标 */
    val todayKcal: Int = 0,
    val todayProteinG: Double = 0.0,
    val todayCarbG: Double = 0.0,
    val todayFatG: Double = 0.0,
    val targetKcal: Int? = null,
    val targetProteinG: Int? = null,
    val targetCarbG: Int? = null,
    val targetFatG: Int? = null,
    /** 近 5 次体重记录（按日期升序），半宽卡迷你趋势用 */
    val recentWeights: List<BodyMetricEntity> = emptyList(),
    /** 近 5 天（含今天）心情：日期 → 心情等级（null = 当天没写） */
    val recentMoods: List<Pair<String, Int?>> = emptyList()
)

/**
 * 今日页（模块契约见设计包 §8 CheckIn）。
 * 职责：推算"今天该练什么"、幂等打卡、日记每日一条、体重速记。
 * 非职责：不做统计图表（记录页职责）、不修改计划。
 * 日程推算：以计划激活日起逐日顺推 7 天循环；无激活日期（老数据）从今天算起。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val planRepository: PlanRepository,
    private val checkInRepository: CheckInRepository,
    private val diaryRepository: DiaryRepository,
    private val bodyMetricRepository: BodyMetricRepository,
    private val profileRepository: ProfileRepository,
    private val dietLogRepository: DietLogRepository,
    private val exerciseProgressRepository: ExerciseProgressRepository,
    private val settings: SettingsDataStore,
    private val waterRepository: com.yuedong.fitness.data.repository.WaterRepository
) : ViewModel() {

    init {
        // 清理模拟时间线之外的进度残留；若今天按计划是休息日，清除当天不该存在的勾选
        viewModelScope.launch {
            runCatching {
                val today = AppClock.todayString()
                exerciseProgressRepository.deleteFutureRows(today)
                val plan = planRepository.observeActivePlan().first()
                val days = plan?.let { planRepository.observePlanDays(it.id).first() }
                val startedAt = settings.observeActivePlanStartedAt().first()
                val startedDate = startedAt?.let {
                    java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                }
                if (startedDate != null && !days.isNullOrEmpty()) {
                    val elapsed = java.time.temporal.ChronoUnit.DAYS.between(startedDate, AppClock.today())
                    val index = ((elapsed % days.size).toInt() + days.size) % days.size
                    val todayDay = days.sortedBy { it.orderIndex }.getOrNull(index)
                    if (todayDay?.isRestDay == true) {
                        exerciseProgressRepository.deleteByDate(today)
                    }
                }
            }
        }
    }

    private data class ScheduleInfo(
        val plan: WorkoutPlanEntity?,
        val cycleMilestone: String? = null,
        val todayDay: PlanDayEntity?,
        val exercises: List<PlanExerciseEntity>,
        val tomorrowLabel: String?,
        val dayNumber: Int,
        val cycleLength: Int
    )

    private val scheduleFlow = planRepository.observeActivePlan().flatMapLatest { plan ->
        if (plan == null) {
            flowOf(ScheduleInfo(null, null, null, emptyList(), null, 0, 0))
        } else {
            combine(
                planRepository.observePlanDays(plan.id),
                planRepository.observeExercisesOfPlan(plan.id),
                settings.observeActivePlanStartedAt(),
                AppClock.offsetDays
            ) { days, exercises, startedAt, offset ->
                val today = LocalDate.now().plusDays(offset)
                val sorted = days.sortedBy { it.orderIndex }
                val startedDate = startedAt?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                }
                val elapsedDays = startedDate?.let { ChronoUnit.DAYS.between(it, today) } ?: 0L
                val index = if (sorted.isEmpty()) {
                    0
                } else {
                    ((elapsedDays % sorted.size).toInt() + sorted.size) % sorted.size
                }
                val todayDay = sorted.find { it.orderIndex == index }
                // 明天预告：括号包计划名，避免与计划名自带的"·"分隔符混淆
                // 周期里程碑：今天恰好跑满一个完整周期（有 startedAt 且非第 0 天）
                val cycleLen = if (plan.cycleDays > 0) plan.cycleDays else sorted.size
                val cycleMilestone = startedDate?.let {
                    val elapsedAll = ChronoUnit.DAYS.between(it, today)
                    if (cycleLen > 0 && elapsedAll > 0 && elapsedAll % cycleLen == 0L)
                        "第 ${elapsedAll / cycleLen} 个周期今天完成！"
                    else null
                }
                val nextIndex = if (sorted.isEmpty()) 0 else (index + 1) % sorted.size
                val nextDay = sorted.getOrNull(nextIndex)
                val tomorrowLabel = nextDay?.let {
                    if (it.isRestDay) "休息日" else "训练日（${plan.name}）"
                }
                ScheduleInfo(
                    plan = plan,
                    cycleMilestone = cycleMilestone,
                    todayDay = todayDay,
                    exercises = todayDay?.let { day ->
                        exercises.filter { it.planDayId == day.id }
                    } ?: emptyList(),
                    tomorrowLabel = tomorrowLabel,
                    dayNumber = index + 1,
                    cycleLength = if (plan.cycleDays > 0) plan.cycleDays else sorted.size
                )
            }
        }
    }

    /** 今日已记录的饮食 */
    private val todayDietFlow = AppClock.offsetDays.flatMapLatest {
        dietLogRepository.observeLogsByDate(AppClock.todayString())
    }

    /** 全部日记（今日日记 + 近 5 天心情迷你趋势共用） */
    private val allDiaryFlow = diaryRepository.observeAll()

    /** 今日动作完成进度 */
    private val progressFlow = AppClock.offsetDays.flatMapLatest {
        exerciseProgressRepository.observeByDate(AppClock.todayString())
    }

    /** 全部体重（最新一次 + 近 5 次迷你趋势共用） */
    private val allWeightsFlow = bodyMetricRepository.observeAll()

    /** 每日目标（热量 + 蛋白质），随资料联动 */
    private val targetFlow = profileRepository.observeProfile().map { profile ->
        profile?.let {
            FitnessCalculator.metrics(
                com.yuedong.fitness.domain.model.FitnessInput(
                    gender = com.yuedong.fitness.domain.model.Gender.fromValue(it.gender)
                        ?: com.yuedong.fitness.domain.model.Gender.MALE,
                    age = it.age,
                    heightCm = it.heightCm,
                    weightKg = it.currentWeightKg,
                    weeklyExerciseDays = it.weeklyExerciseDays,
                    purpose = com.yuedong.fitness.domain.model.Purpose.fromValue(it.purpose)
                        ?: com.yuedong.fitness.domain.model.Purpose.HEALTH
                )
            )
        }
    }

    /** 新手遮罩引导是否已展示（仅首启展示一次） */
    val coachShown: StateFlow<Boolean> = settings.observeCoachShown()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setCoachShown() = viewModelScope.launch { settings.setCoachShown(true) }

    /** 今日饮水 (已喝 ml to 目标 ml)：目标按体重 30ml/kg 推导，落在 1200~3000 并取 50 的倍数 */
    val water: StateFlow<Pair<Int, Int>> = combine(
        waterRepository.observeTotalForDate(AppClock.todayString()),
        profileRepository.observeProfile()
    ) { totalMl, profile ->
        val weightKg = profile?.currentWeightKg?.takeIf { it > 0 } ?: 65.0
        val target = ((weightKg * 30).coerceIn(1200.0, 3000.0).toInt() / 50) * 50
        totalMl to target
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0 to 2000)

    /** 记一笔饮水（ml） */
    fun addWater(ml: Int) {
        viewModelScope.launch { waterRepository.add(AppClock.todayString(), ml) }
    }

    val uiState: StateFlow<TodayUiState> = combine(
        scheduleFlow,
        profileRepository.observeProfile(),
        checkInRepository.observeAllDates(),
        bodyMetricRepository.observeLatest(),
        combine(
            allDiaryFlow,
            dietLogRepository.observeLogsBetween("2000-01-01", "2999-12-31"),
            progressFlow,
            allWeightsFlow,
            targetFlow
        ) { allDiaries, allLogs, progress, allWeights, target ->
            listOf(allDiaries, allLogs, progress, allWeights, target)
        }
    ) { schedule, profile, checkInDates, latestWeight, parts ->
        val today = AppClock.today()
        val streakDates = checkInDates.mapNotNull { date ->
            runCatching { LocalDate.parse(date) }.getOrNull()
        }
        @Suppress("UNCHECKED_CAST")
        val allDiaries = parts[0] as List<com.yuedong.fitness.data.db.entity.DiaryEntryEntity>
        @Suppress("UNCHECKED_CAST")
        val allLogs = parts[1] as List<com.yuedong.fitness.data.db.entity.DietLogEntity>
        val hasAnyDietLog = allLogs.isNotEmpty()
        val todayLogs = allLogs.filter { it.date == AppClock.todayString() }
        @Suppress("UNCHECKED_CAST")
        val progress = parts[2] as List<com.yuedong.fitness.data.db.entity.ExerciseProgressEntity>
        @Suppress("UNCHECKED_CAST")
        val allWeights = parts[3] as List<com.yuedong.fitness.data.db.entity.BodyMetricEntity>
        val target = parts[4] as com.yuedong.fitness.domain.model.FitnessMetrics?

        val diary = allDiaries.firstOrNull { it.date == AppClock.todayString() }
        // 近 5 天心情（含今天，旧的在前）
        val moodByDate = allDiaries.associate { it.date to it.mood }
        val recentMoods = (4 downTo 0).map { back ->
            val d = today.minusDays(back.toLong()).toString()
            d to moodByDate[d]
        }
        // 近 5 次体重（按日期升序）
        val recentWeights = allWeights
            .filter { w -> runCatching { LocalDate.parse(w.date) }.getOrDefault(LocalDate.MIN) <= today }
            .sortedBy { runCatching { LocalDate.parse(it.date) }.getOrDefault(LocalDate.MIN) }
            .takeLast(5)

        TodayUiState(
            isLoading = false,
            hasProfile = profile != null,
            purposeLabel = profile?.let { Purpose.fromValue(it.purpose)?.label },
            activePlan = schedule.plan,
            todayDay = schedule.todayDay,
            todayExercises = schedule.exercises,
            tomorrowLabel = schedule.tomorrowLabel,
            planDayNumber = schedule.dayNumber,
            planCycleLength = schedule.cycleLength,
            isCheckedIn = checkInDates.contains(AppClock.todayString()),
            cycleMilestone = schedule.cycleMilestone,
            streak = FitnessCalculator.streak(streakDates, today),
            totalCheckIns = checkInDates.size,
            latestWeight = latestWeight,
            diary = diary,
            hasAnyDietLog = hasAnyDietLog,
            exerciseDone = progress.associate { it.planExerciseId to it.done },
            todayKcal = todayLogs.sumOf { it.calories },
            todayProteinG = todayLogs.sumOf { it.proteinG },
            todayCarbG = todayLogs.sumOf { it.carbG },
            todayFatG = todayLogs.sumOf { it.fatG },
            targetKcal = target?.targetCalories,
            targetProteinG = target?.proteinG,
            targetCarbG = target?.carbG,
            targetFatG = target?.fatG,
            recentWeights = recentWeights,
            recentMoods = recentMoods
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState(isLoading = true))

    /** 勾选/取消某个动作；全部完成时自动完成今日打卡（幂等）。 */
    fun toggleExercise(planExerciseId: Long, done: Boolean) {
        viewModelScope.launch {
            exerciseProgressRepository.setDone(AppClock.todayString(), planExerciseId, done)
            val state = uiState.value
            val allDone = state.todayExercises.isNotEmpty() &&
                state.todayExercises.all { state.exerciseDone[it.id] == true }
            if (allDone && !state.isCheckedIn) checkIn()
        }
    }

    /** 「完成今日训练」：今日全部动作一次勾完 + 打卡，进度与勾选状态同步更新。 */
    fun completeTodayTraining() {
        viewModelScope.launch {
            val state = uiState.value
            state.todayExercises
                .filter { state.exerciseDone[it.id] != true }
                .forEach { exerciseProgressRepository.setDone(AppClock.todayString(), it.id, true) }
            if (!state.isCheckedIn) checkIn()
        }
    }

    /** 幂等打卡：date 唯一索引 + REPLACE 策略，同一天重复点击只会有一条记录。 */
    fun checkIn() {
        val planId = uiState.value.activePlan?.id ?: return
        viewModelScope.launch {
            checkInRepository.save(
                CheckInRecordEntity(
                    date = AppClock.todayString(),
                    planId = planId,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveWeight(weightKg: Double) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            bodyMetricRepository.saveCurrent(
                BodyMetricEntity(
                    date = AppClock.todayString(),
                    weightKg = weightKg,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    /** 新手引导分步填写：保存身体档案并同步今日体重（口径与原问卷一致） */
    fun saveProfileSetup(gender: String, age: Int, heightCm: Int, weightKg: Double, purpose: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            profileRepository.save(
                UserProfileEntity(
                    gender = gender,
                    age = age,
                    heightCm = heightCm,
                    currentWeightKg = weightKg,
                    targetWeightKg = weightKg,
                    weeklyExerciseDays = 3,
                    purpose = purpose,
                    createdAt = now,
                    updatedAt = now
                )
            )
            bodyMetricRepository.save(
                BodyMetricEntity(
                    date = AppClock.todayString(),
                    weightKg = weightKg,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun saveDiary(mood: Int, content: String) {
        viewModelScope.launch {
            diaryRepository.save(
                DiaryEntryEntity(
                    date = AppClock.todayString(),
                    mood = mood.coerceIn(1, 5),
                    content = content.take(100),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                val container = app.container
                TodayViewModel(
                    planRepository = container.planRepository,
                    checkInRepository = container.checkInRepository,
                    diaryRepository = container.diaryRepository,
                    bodyMetricRepository = container.bodyMetricRepository,
                    profileRepository = container.profileRepository,
                    dietLogRepository = container.dietLogRepository,
                    exerciseProgressRepository = container.exerciseProgressRepository,
                    settings = container.settings,
                    waterRepository = container.waterRepository
                )
            }
        }
    }
}

@Composable
fun TodayScreen(
    onGoPlan: () -> Unit,
    onOpenDiet: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenTraining: () -> Unit = {},
    onOpenWeight: () -> Unit = {},
    onOpenProfileEdit: () -> Unit = {},
    viewModel: TodayViewModel = viewModel(factory = TodayViewModel.factory())
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonBox(Modifier.fillMaxWidth().height(190.dp))
            SkeletonBox(Modifier.fillMaxWidth().height(120.dp))
            SkeletonBox(Modifier.fillMaxWidth().height(150.dp))
            SkeletonBox(Modifier.fillMaxWidth().height(110.dp))
        }
        return
    }

    val context = LocalContext.current
    var weightDialogVisible by remember { mutableStateOf(false) }
    var diaryDialogVisible by remember { mutableStateOf(false) }
    var waterDialogVisible by remember { mutableStateOf(false) }
    val water by viewModel.water.collectAsState()
    // 新手任务卡：完善资料 → 选计划 → 首次打卡（全部完成后自动收起）
    var guideDismissed by rememberSaveable { mutableStateOf(false) }
    // 新手遮罩引导：-1 = 未开始；0..n = 高亮步进；完成后写 DataStore 仅展示一次
    var coachStep by rememberSaveable { mutableIntStateOf(-1) }
    val coachRects = remember { mutableStateMapOf<String, Rect>() }

    // T008：打卡成功瞬间的气泡上升（一次性 0.9s）
    var checkInBurst by remember { mutableIntStateOf(0) }
    val bubbleProgress = remember { androidx.compose.animation.core.Animatable(1f) }
    // 气泡由「未打卡 → 已打卡」的状态变化触发——训练卡与底部两个按钮都能触发
    var burstPrev by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var burstFirst by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(state.isCheckedIn) {
        when {
            burstFirst -> { burstFirst = false; burstPrev = state.isCheckedIn }
            state.isCheckedIn && !burstPrev -> { burstPrev = true; checkInBurst++ }
            !state.isCheckedIn -> burstPrev = false
        }
    }
    LaunchedEffect(checkInBurst) {
        if (checkInBurst > 0) {
            bubbleProgress.snapTo(0f)
            launch { bubbleProgress.animateTo(1f, tween(900, easing = LinearOutSlowInEasing)) }
        }
    }
    // 首启遮罩引导：数据就绪且从未展示 → 进入第 0 步
    val coachShown by viewModel.coachShown.collectAsState()
    LaunchedEffect(state.isLoading, coachShown) {
        if (!state.isLoading && !coachShown && coachStep == -1) coachStep = 0
    }

    Box(
        Modifier
            .fillMaxSize()
    ) {
    val listState = rememberLazyListState()
    val isDark = diveDark()
    // 引导开始前把列表归位到顶部：挖孔坐标取自卡片的窗口位置，列表滚动态下会错位
    LaunchedEffect(coachStep) {
        if (coachStep == 0) listState.scrollToItem(0)
    }
    // B 方向：滚动=下潜。基础深度由连击决定，滚动深度由列表滚动决定。
    val diveBase = (state.streak / 30f).coerceIn(0f, 1f)
    val scrollDepth = (listState.firstVisibleItemIndex + listState.firstVisibleItemScrollOffset / 800f) / 12f
    val strataProgress = (diveBase * 0.7f + scrollDepth * 0.3f).coerceIn(0f, 1f)

    Box(
        Modifier
            .fillMaxSize()
            .background(strataColor(strataProgress, isDark))
    ) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, top = 16.dp, end = 20.dp, bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TodayHeader(purposeLabel = state.purposeLabel)
            Spacer(Modifier.height(12.dp))
            // 打卡信息卡：常驻展示（不再藏于下拉手势）。上行连击→点进打卡日历，下行日记→就地写
            Column(
                Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coachRects["checkin"] = it.boundsInWindow() }
                    .clip(MaterialTheme.shapes.large)
                    .background(DiveGradients.depthBand(isDark), MaterialTheme.shapes.large)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCalendar() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = DiveDataColors.fat,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "连续打卡 ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "${state.streak}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DiveMono,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        " 天 · 累计打卡 ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "${state.totalCheckIns}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DiveMono,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        " 天",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = "查看打卡日历",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                state.cycleMilestone?.let { milestone ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = DiveDataColors.fat,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            milestone,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
                // 日记行：写了显示内容，没写给提示；点击就地写日记
                val diary = state.diary
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { diaryDialogVisible = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (diary != null) {
                        Text(moodEmoji(diary.mood), fontSize = 18.sp)
                    } else {
                        Icon(
                            Icons.Filled.EditNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        diary?.content?.ifBlank { "（只有心情～）" }
                            ?: "今天还没写日记，写一句吧",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            // 今日消耗：参考图排版——千卡在左上角一行（当前值加粗 + / 目标），四环居中主体
            val kcalDisplay = rememberCountUpValue(state.todayKcal)
            Column(
                Modifier
                    .onGloballyPositioned { coachRects["rings"] = it.boundsInWindow() }
                    .fillMaxWidth()
                    .background(
                        DiveGradients.heroGlow(diveDark()),
                        MaterialTheme.shapes.large
                    )
                    .padding(vertical = 16.dp, horizontal = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        kcalDisplay.value.roundToInt().toString(),
                        style = DiveNumeric.large,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        " / 目标 " + (state.targetKcal?.toString() ?: "—") + " 千卡",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                // 三大营养素小圈圈：点一下去今日饮食记录
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        MacroRing(
                            label = "蛋白(g)",
                            value = state.todayProteinG.roundToInt(),
                            target = state.targetProteinG,
                            color = DiveDataColors.protein,
                            onClick = onOpenDiet
                        )
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        MacroRing(
                            label = "碳水(g)",
                            value = state.todayCarbG.roundToInt(),
                            target = state.targetCarbG,
                            color = DiveDataColors.carb,
                            onClick = onOpenDiet
                        )
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        MacroRing(
                            label = "脂肪(g)",
                            value = state.todayFatG.roundToInt(),
                            target = state.targetFatG,
                            color = DiveDataColors.fat,
                            onClick = onOpenDiet
                        )
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        MacroRing(
                            label = "饮水(ml)",
                            value = water.first,
                            target = water.second,
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { waterDialogVisible = true }
                        )
                    }
                }
            }
        }
        item {
            Box(Modifier.onGloballyPositioned { coachRects["training"] = it.boundsInWindow() }) {
            TrainingCard(
                state = state,
                onCheckIn = viewModel::checkIn,
                onCompleteAll = viewModel::completeTodayTraining,
                onGoPlan = onGoPlan,
                onOpen = {
                    if (state.todayDay?.isRestDay == true) {
                        android.widget.Toast.makeText(context, "今日记得休息哦~", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        onOpenTraining()
                    }
                }
            )
            }
        }
        item {
            Box(Modifier.onGloballyPositioned { coachRects["weight"] = it.boundsInWindow() }) {
            WeightMiniCard(
                latest = state.latestWeight,
                recent = state.recentWeights,
                onLog = { weightDialogVisible = true },
                onOpen = onOpenWeight,
                modifier = Modifier.fillMaxWidth()
            )
            }
        }
        }
    }
    // T008：打卡成功气泡（从主按钮升起，0.9s 上浮淡出）；颜色取自主题色板，随明暗切换
    if (checkInBurst > 0 && bubbleProgress.value < 1f) {
        val colors = listOf(
            MaterialTheme.colorScheme.primary,
            DiveDataColors.protein,
            DiveDataColors.fat,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.secondary
        )
        (0..4).forEach { i ->
            val p = ((bubbleProgress.value - i * 0.12f) / 0.6f).coerceIn(0f, 1f)
            if (p > 0f) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = ((i - 2) * 34).dp, y = (-(p * 230 + i * 14)).dp)
                        .size((22 - i % 3 * 4).dp)
                        .alpha(1f - p)
                        .background(colors[i % colors.size].copy(alpha = 0.9f), CircleShape)
                )
            }
        }
    }
    }

    if (coachStep in coachSteps.indices) {
        val coach = coachSteps[coachStep]
        val density = LocalDensity.current
        val dm = LocalContext.current.resources.displayMetrics
        val (navHole, navTip) = if (coach.key?.startsWith("nav:") == true) {
            navTabHole(
                coach.key!!.removePrefix("nav:").toInt(),
                dm.widthPixels.toFloat(),
                dm.heightPixels.toFloat(),
                density
            )
        } else Rect.Zero to Offset.Zero
        // Popup 承载：盖住底部悬浮导航条（内容层画不到它上面），导航标签步骤才有"压暗 + 挖孔"效果；
        // 系统返回 = 跳过引导
        androidx.compose.ui.window.Popup(
            alignment = Alignment.TopStart,
            onDismissRequest = {
                viewModel.setCoachShown()
                coachStep = -1
            },
            properties = androidx.compose.ui.window.PopupProperties(
                focusable = true,
                // 不裁剪到"内容区"：默认 Popup 会被压到状态栏之下，挖孔整体下移一个状态栏高
                clippingEnabled = false
            )
        ) {
            CoachMarkOverlay(
                hole = when {
                    coach.key == null -> null
                    coach.key.startsWith("nav:") -> navHole
                    // 内容卡：四周外扩 CoachHolePad，保证框完全盖住卡片
                    else -> coachRects[coach.key]?.let { r ->
                        with(density) {
                            Rect(
                                r.left - CoachHolePad.toPx(),
                                r.top - CoachHolePad.toPx(),
                                r.right + CoachHolePad.toPx(),
                                r.bottom + CoachHolePad.toPx()
                            )
                        }
                    }
                },
                arrowTip = if (coach.key?.startsWith("nav:") == true) navTip else null,
                title = coach.title,
                desc = coach.desc,
                isLast = coachStep == coachSteps.lastIndex,
                stepText = if (coachStep == coachSteps.lastIndex) "点击「开始使用」去完善身体资料" else "下一步 · ${coachStep + 1}/${coachSteps.size}",
                onNext = {
                    if (coachStep >= coachSteps.lastIndex) {
                        // 最后一步：标记已完成并跳转修改资料
                        viewModel.setCoachShown()
                        coachStep = -1
                        onOpenProfileEdit()
                    } else {
                        coachStep += 1
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (waterDialogVisible) {
        YueDialog(
            onDismissRequest = { waterDialogVisible = false },
            title = { Text("饮水记录") },
            text = {
                Column {
                    Text(
                        "今天已喝 ${water.first} ml · 目标 ${water.second} ml",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(150, 250, 500).forEach { ml ->
                            FilledTonalButton(onClick = { viewModel.addWater(ml) }) {
                                Text("+$ml")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { waterDialogVisible = false }
                ) { Text("完成") }
            }
        )
    }

    if (weightDialogVisible) {
        WeightLogDialog(
            initial = state.latestWeight?.weightKg ?: 65.0,
            onDismiss = { weightDialogVisible = false },
            onSave = { kg ->
                viewModel.saveWeight(kg)
                weightDialogVisible = false
            }
        )
    }

    if (diaryDialogVisible) {
        DiaryDialog(
            existing = state.diary,
            onDismiss = { diaryDialogVisible = false },
            onSave = { mood, content ->
                viewModel.saveDiary(mood, content)
                diaryDialogVisible = false
            }
        )
    }

}

@Composable
private fun TodayHeader(purposeLabel: String?) {
    val dateText = AppClock.today().format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            dateText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (purposeLabel != null) {
            Spacer(Modifier.size(8.dp))
            Text(
                "· 本周目标：$purposeLabel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TrainingCard(
    state: TodayUiState,
    onCheckIn: () -> Unit,
    onGoPlan: () -> Unit,
    onCompleteAll: () -> Unit = {},
    onOpen: () -> Unit = {}
) {
    WashCard(onClick = onOpen) {
        Column {
            when {
                state.activePlan == null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(com.yuedong.fitness.R.drawable.mascot_shiba),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.large)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("还没有选择训练计划", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "去计划页挑一套，或新建属于自己的计划",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onGoPlan, modifier = Modifier.fillMaxWidth()) {
                        Text("去选计划")
                    }
                }
                state.todayDay?.isRestDay == true -> {
                    Text(
                        "今天休息 · 计划第 ${state.planDayNumber} 天",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "本轮周期还剩 ${state.planCycleLength - state.planDayNumber} 天 · 休息也是训练的一部分",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (state.tomorrowLabel != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "明天：${state.tomorrowLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    if (state.isCheckedIn) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), MaterialTheme.shapes.medium)
                                .padding(14.dp)
                        ) {
                            Text(
                                "已按计划休息 ✓ 连击不断",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onCheckIn,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text("今天按计划休息 · 打卡")
                        }
                    }
                }
                else -> {
                    val dayIndex = state.planDayNumber
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "第 $dayIndex 天",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            state.todayDay?.title ?: "训练日",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "本轮周期还剩 ${state.planCycleLength - dayIndex} 天",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    // 一行进度摘要：动作清单在计划详情里看，今日页保持一屏浏览
                    val total = state.todayExercises.size
                    val doneCount = state.todayExercises.count { state.exerciseDone[it.id] == true }
                    if (total > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { if (total == 0) 0f else doneCount.toFloat() / total },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = if (doneCount == total) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                drawStopIndicator = {}
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "$doneCount/$total 项",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = DiveMono,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (state.todayExercises.isNotEmpty() && !state.isCheckedIn) {
                        val doneCount = state.todayExercises.count { state.exerciseDone[it.id] == true }
                        Text(
                            "已完成 $doneCount/${state.todayExercises.size} 项" +
                                if (doneCount == state.todayExercises.size) "，太棒了！" else "，完成今日训练即可打卡",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (doneCount == state.todayExercises.size) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    val totalExercises = state.todayExercises.size
                    val allExercisesDone = totalExercises > 0 && doneCount == totalExercises
                    if (state.isCheckedIn && allExercisesDone) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), MaterialTheme.shapes.medium)
                                .padding(14.dp)
                        ) {
                            Text(
                                "已完成今日训练 ✓ 太棒了！",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (totalExercises > 0) {
                        PrimaryCta(
                            text = if (state.isCheckedIn) "勾完剩余动作" else "完成今日训练",
                            onClick = onCompleteAll
                        )
                    }
                    // 打卡后预告明天的安排，让用户心里有数
                    if (state.tomorrowLabel != null) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "明天：${state.tomorrowLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/** 点状迷你趋势：虚线连接各次记录，最新一次为实心大点 */
@Composable
private fun MiniDotsTrend(values: List<Double>, modifier: Modifier = Modifier) {
    val dotColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)
    val currentColor = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        if (values.isEmpty()) return@Canvas
        val minV = values.min()
        val range = (values.max() - minV).takeIf { it > 0.001 } ?: 1.0
        val n = values.size
        val margin = 8f
        val xs = if (n == 1) {
            listOf(size.width / 2f)
        } else {
            values.indices.map { margin + it * ((size.width - margin * 2) / (n - 1)) }
        }
        val ys = values.map { v ->
            (size.height * (1f - ((v - minV) / range).toFloat()))
                .coerceIn(size.height * 0.18f, size.height * 0.82f)
        }
        for (i in 0 until n - 1) {
            drawLine(
                color = lineColor,
                start = Offset(xs[i], ys[i]),
                end = Offset(xs[i + 1], ys[i + 1]),
                strokeWidth = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
        }
        values.indices.forEach { i ->
            val isLast = i == n - 1
            drawCircle(
                color = if (isLast) currentColor else dotColor,
                radius = (if (isLast) 4.dp else 2.5.dp).toPx(),
                center = Offset(xs[i], ys[i])
            )
        }
    }
}


/**
 * 体重记录卡（照参考样式）：标题 + 更新时间 + 绿色圆形加号 + 大数字公斤 + 点状趋势。
 */
@Composable
private fun WeightMiniCard(
    latest: BodyMetricEntity?,
    recent: List<BodyMetricEntity>,
    onLog: () -> Unit,
    onOpen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    WashCard(modifier = modifier, onClick = onOpen) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "体重记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                // 绿色圆形加号：就地记录（视觉 32dp 圆，命中区保证 48dp）
                Box(
                    Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(onClick = onLog),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "＋",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            Text(
                latest?.let {
                    val dt = java.time.Instant.ofEpochMilli(it.createdAt)
                        .atZone(java.time.ZoneId.systemDefault())
                    if (dt.toLocalDate() == AppClock.today()) {
                        dt.format(DateTimeFormatter.ofPattern("HH:mm")) + " 更新"
                    } else {
                        dt.format(DateTimeFormatter.ofPattern("M月d日")) + " 记录"
                    }
                } ?: "还没有记录",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(Modifier.height(18.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    latest?.let { String.format("%.2f", it.weightKg) } ?: "--",
                    style = DiveNumeric.large,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    " 公斤",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Spacer(Modifier.weight(1f))
                MiniDotsTrend(
                    values = recent.map { it.weightKg },
                    modifier = Modifier
                        .width(140.dp)
                        .height(30.dp)
                        .padding(bottom = 6.dp)
                )
            }
        }
    }
}

/**
 * 新手遮罩引导：全屏压暗 + 目标区域挖孔高亮 + 说明卡（下一步/完成）。
 * rect 为窗口坐标；null 时仅居中显示说明卡（无挖孔）。
 */
/**
 * 底部导航引导挖孔：目标标签的"亮带"（从标签列一直到屏幕底）。
 * 只用 X 几何（与 YueDongBottomBar 一致：左右 18dp 外距 + 6dp 内距、5 等分）——
 * ColorOS 的 insets/displayMetrics 高度互相矛盾，竖直方向任何推算都不可靠，
 * 亮带直通屏底可完全免疫竖直偏差。返回 挖孔区域 to 箭头落点(标签中心)。
 */
private fun navTabHole(index: Int, screenW: Float, screenH: Float, density: Density): Pair<Rect, Offset> {
    val w = with(density) { screenW.toDp() }
    val h = with(density) { screenH.toDp() }
    val tabW = (w - 48.dp) / 5f                    // (18+6)dp 左右内边距
    val left = 24.dp + tabW * index
    val bandTop = h - CoachBandHeight
    return with(density) {
        val hole = Rect(
            (left + 3.dp).toPx(), bandTop.toPx(),
            (left + tabW - 3.dp).toPx(), h.toPx()
        )
        val tip = Offset(
            (left + tabW / 2f).toPx(),
            hole.top + hole.height * CoachBandTipFrac
        )
        hole to tip
    }
}

/**
 * 新手遮罩引导（参考图风格）：全屏压暗 + 目标挖孔 + 白色框选描边 + 大字短语 + 曲线箭头 + OK 胶囊。
 * rect 为窗口坐标；null 时仅居中显示收尾文案。
 */
@Composable
private fun CoachMarkOverlay(
    hole: Rect?,
    title: String,
    desc: String,
    isLast: Boolean,
    stepText: String,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    arrowTip: Offset? = null
) {
    val density = LocalDensity.current
    val screenH = with(density) { LocalContext.current.resources.displayMetrics.heightPixels.toFloat() }

    // Popup 窗口在屏幕上的实际原点：clippingEnabled=false 时系统会把它上移一个状态栏高，
    // 而挖孔坐标是 Activity 窗口坐标——运行时实测原点并平移，无论系统怎么偏移都能对齐
    val popupView = LocalView.current
    var popupOrigin by remember { mutableStateOf(Offset.Zero) }
    androidx.compose.runtime.DisposableEffect(popupView) {
        val loc = IntArray(2)
        popupView.getLocationOnScreen(loc)
        popupOrigin = Offset(loc[0].toFloat(), loc[1].toFloat())
        onDispose { }
    }
    val alignedHole = hole?.translate(-popupOrigin.x, -popupOrigin.y)

    // 文案放在洞下方（洞在上半屏）或上方；曲线箭头从文案指向洞
    val below = alignedHole != null && alignedHole.center.y < screenH / 2f
    val titleYPx = when {
        alignedHole == null -> with(density) { CoachClosingTop.toPx() }
        below -> alignedHole.bottom + with(density) { CoachCalloutBelowGap.toPx() }
        else -> alignedHole.top - with(density) { CoachCalloutAboveGap.toPx() }
    }
    val titleY = with(density) { titleYPx.toDp() }

    Box(
        modifier
            // Popup 的最大高度约束会扣掉底部手势区，导致遮罩离屏底留一条亮缝——强制撑满整窗
            .requiredHeight(with(density) { screenH.toDp() })
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawRect(Color.Black.copy(alpha = CoachDimAlpha))
                alignedHole?.let { r ->
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(r.left, r.top),
                        size = Size(r.width, r.height),
                        cornerRadius = CornerRadius(CoachHoleRadius.toPx()),
                        blendMode = BlendMode.Clear
                    )
                }
                // 关键：画完遮罩必须把子内容画回来——此前缺这句，箭头/文字/OK 胶囊全部不可见
                drawContent()
            }
            .pointerInput(Unit) {
                // 只挡住下层点击，不推进步骤：只有点「知道了」才到下一步
                detectTapGestures { }
            }
            .fillMaxSize()
    ) {
        // 曲线箭头：从文案弯向挖孔（白，圆点起笔 + 三角箭头收笔）+ 挖孔外圈白色框选描边
        if (alignedHole != null) {
            val hole = alignedHole
            Canvas(Modifier.fillMaxSize()) {
                // 框选描边：外扩 CoachOutlineGap 的圆角白框（参考图的「框选」感）；导航条亮带步骤不画圈
                if (arrowTip == null) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.95f),
                        topLeft = Offset(hole.left - CoachOutlineGap.toPx(), hole.top - CoachOutlineGap.toPx()),
                        size = Size(hole.width + CoachOutlineGap.toPx() * 2, hole.height + CoachOutlineGap.toPx() * 2),
                        cornerRadius = CornerRadius(CoachHoleRadius.toPx() + CoachOutlineGap.toPx()),
                        style = Stroke(CoachOutlineStroke.toPx())
                    )
                }
                // 箭头终点：显式落点（导航标签）或挖孔上/下缘（内容卡）
                val end = arrowTip ?: Offset(
                    hole.center.x + 24.dp.toPx(),
                    if (below) hole.bottom + 10.dp.toPx() else hole.top - 10.dp.toPx()
                )
                // 起笔方向：内容卡 = 落点右上方；导航标签 = 统一从落点左上方下来
                val start = if (arrowTip != null) {
                    Offset(end.x - CoachArrowNavStartX.toPx(), titleYPx - 8.dp.toPx())
                } else {
                    Offset(end.x + CoachArrowStartX.toPx(), titleYPx + (if (below) 8.dp.toPx() else -8.dp.toPx()))
                }
                val mid = Offset((start.x + end.x) / 2f + CoachArrowMidBulge, (start.y + end.y) / 2f)
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(start.x, start.y)
                    quadraticBezierTo(mid.x, mid.y, end.x, end.y)
                }
                drawPath(path, Color.White, style = Stroke(CoachArrowStroke.toPx(), cap = StrokeCap.Round))
                drawCircle(Color.White, radius = CoachArrowDotR.toPx(), center = start)
                // 箭头头部
                val ang = kotlin.math.atan2(end.y - mid.y, end.x - mid.x)
                val hl = CoachArrowHead.toPx()
                val p1 = Offset(end.x - hl * kotlin.math.cos(ang - 0.5f), end.y - hl * kotlin.math.sin(ang - 0.5f))
                val p2 = Offset(end.x - hl * kotlin.math.cos(ang + 0.5f), end.y - hl * kotlin.math.sin(ang + 0.5f))
                drawLine(Color.White, p1, end, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Color.White, p2, end, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            }
        }
        // 大字短语 + 说明 + OK 胶囊 + 步数提示
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = titleY)
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                desc,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White,
                modifier = Modifier
                    .clickable(onClick = onNext)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    if (isLast) "开始使用" else "知道了",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                stepText,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

private fun moodEmoji(mood: Int): String = when (mood) {
    1 -> "😢"
    2 -> "😟"
    3 -> "😐"
    4 -> "🙂"
    else -> "😄"
}
