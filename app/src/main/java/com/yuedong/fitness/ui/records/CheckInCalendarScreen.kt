package com.yuedong.fitness.ui.records

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.BodyMetricEntity
import com.yuedong.fitness.data.db.entity.DietLogEntity
import com.yuedong.fitness.data.repository.BodyMetricRepository
import com.yuedong.fitness.data.repository.CheckInRepository
import com.yuedong.fitness.data.repository.DiaryRepository
import com.yuedong.fitness.data.repository.DietLogRepository
import com.yuedong.fitness.data.repository.PlanRepository
import com.yuedong.fitness.data.repository.ProfileRepository
import com.yuedong.fitness.domain.logic.FitnessCalculator
import com.yuedong.fitness.domain.model.FitnessInput
import com.yuedong.fitness.domain.model.Gender
import com.yuedong.fitness.domain.model.Purpose
import com.yuedong.fitness.ui.components.MonthCalendar
import com.yuedong.fitness.ui.components.BackHeader
import com.yuedong.fitness.ui.theme.DiveDataColors
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.util.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 打卡日历（独立页，从今日页下拉横幅进入）。
 * 月历展示打卡圈、饮食点与「补」标记；点任意一天看当天四行小结，
 * 过去漏掉的日子可补打卡（补的卡在详情里会注明「补打卡」）。
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Suppress("OPT_IN_USAGE")
class CheckInCalendarViewModel(
    private val checkInRepository: CheckInRepository,
    private val planRepository: PlanRepository,
    bodyMetricRepository: BodyMetricRepository,
    private val diaryRepository: DiaryRepository,
    private val dietLogRepository: DietLogRepository,
    profileRepository: ProfileRepository,
    exerciseProgressRepository: com.yuedong.fitness.data.repository.ExerciseProgressRepository,
    private val settings: com.yuedong.fitness.data.settings.SettingsDataStore
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    fun previousMonth() = _month.update { it.minusMonths(1) }
    fun nextMonth() = _month.update { it.plusMonths(1) }

    /** 直接跳到某个月（滑动翻页与「回到本月」共用） */
    fun showMonth(target: YearMonth) = _month.update { target }

    /** 活跃计划的排布信息 */
    private data class PlanSchedule(
        val days: List<com.yuedong.fitness.data.db.entity.PlanDayEntity>,
        val startedDate: java.time.LocalDate?
    )

    data class CalendarUi(
        val checkedDays: Set<Int> = emptySet(),
        val makeUpDays: Set<Int> = emptySet(),
        val streak: Int = 0,
        val total: Int = 0,
        /** 累计训练：真实有过训练的天数（按完成进度表去重日期，休息日打卡不计入） */
        val trainedTotal: Int = 0,
        val todayDiary: com.yuedong.fitness.data.db.entity.DiaryEntryEntity? = null,
        /** 饮食状态："good"=达标 / "over"=超标 */
        val dietStatus: Map<Int, String> = emptyMap(),
        val weights: List<BodyMetricEntity> = emptyList(),
        val history: List<HistoryItem> = emptyList(),
        val allDietLogs: List<DietLogEntity> = emptyList()
    )

    private data class Core(
        val records: List<com.yuedong.fitness.data.db.entity.CheckInRecordEntity>,
        val planNames: Map<Long, String>,
        val weights: List<BodyMetricEntity>,
        val diaries: List<com.yuedong.fitness.data.db.entity.DiaryEntryEntity>
    )

    private val coreFlow = combine(
        checkInRepository.observeAll(),
        planRepository.observePlans(),
        bodyMetricRepository.observeAll(),
        diaryRepository.observeAll()
    ) { records, plans, weights, diaries ->
        Core(records, plans.associate { it.id to it.name }, weights, diaries)
    }

    private val allDietFlow = dietLogRepository.observeLogsBetween("2000-01-01", "2999-12-31")

    /** 真实训练日：完成进度表里 done=true 的去重日期（休息日打卡不产生进度记录） */
    private val trainedDatesFlow = exerciseProgressRepository.observeAll()
        .map { rows ->
            rows.filter { it.done }
                .mapNotNull { row -> runCatching { java.time.LocalDate.parse(row.date) }.getOrNull() }
                .distinct()
        }

    /** 活跃计划的日历排布：开始日期 + 逐日休息标记，用于判断某天是否计划休息日 */
    private val planScheduleFlow = planRepository.observeActivePlan().flatMapLatest { plan ->
        if (plan == null) {
            kotlinx.coroutines.flow.flowOf(null)
        } else {
            combine(
                planRepository.observePlanDays(plan.id),
                settings.observeActivePlanStartedAt()
            ) { days, startedAt ->
                PlanSchedule(
                    days = days.sortedBy { it.orderIndex },
                    startedDate = startedAt?.let {
                        java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    }
                )
            }
        }
    }

    /** 某日期按计划排布是否为休息日（无排布信息时视为非休息日，按真实训练计） */
    private fun isPlanRestDay(date: java.time.LocalDate, schedule: PlanSchedule?): Boolean {
        if (schedule == null || schedule.days.isEmpty()) return false
        val startedDate = schedule.startedDate ?: return false
        val elapsed = java.time.temporal.ChronoUnit.DAYS.between(startedDate, date)
        val index = ((elapsed % schedule.days.size).toInt() + schedule.days.size) % schedule.days.size
        return schedule.days.getOrNull(index)?.isRestDay == true
    }

    private val targetFlow = profileRepository.observeProfile().map { profile ->
        profile?.let {
            FitnessCalculator.metrics(
                FitnessInput(
                    gender = Gender.fromValue(it.gender) ?: Gender.MALE,
                    age = it.age,
                    heightCm = it.heightCm,
                    weightKg = it.currentWeightKg,
                    weeklyExerciseDays = it.weeklyExerciseDays,
                    purpose = Purpose.fromValue(it.purpose) ?: Purpose.HEALTH
                )
            ).targetCalories
        }
    }

    val uiState: StateFlow<CalendarUi> = combine(
        combine(
            combine(coreFlow, allDietFlow, _month, AppClock.offsetDays, targetFlow) { core, allDiet, month, offset, target ->
                listOf(core, allDiet, month, offset, target)
            },
            settings.observeUsageStartDate()
        ) { parts, usageStart -> parts to usageStart },
        combine(planScheduleFlow, trainedDatesFlow) { schedule, dates -> schedule to dates }
    ) { (parts, usageStart), (schedule, trainedDates) ->
        @Suppress("UNCHECKED_CAST")
        val core = parts[0] as Core
        @Suppress("UNCHECKED_CAST")
        val allDiet = parts[1] as List<DietLogEntity>
        @Suppress("UNCHECKED_CAST")
        val month = parts[2] as YearMonth
        @Suppress("UNCHECKED_CAST")
        val offset = parts[3] as Long
        @Suppress("UNCHECKED_CAST")
        val target = parts[4] as Int?
        val today = LocalDate.now().plusDays(offset)
        val dates = core.records.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
        val checked = dates
            .filter { it.year == month.year && it.monthValue == month.monthValue }
            .map { it.dayOfMonth }
            .toSet()
        // 可补打卡 = 本月里今天之前、且没打卡的日子
        val lastMakeUpDay = when {
            month.isBefore(YearMonth.from(today)) -> month.lengthOfMonth()
            month == YearMonth.from(today) -> today.dayOfMonth - 1
            else -> 0
        }
        // 第一天 = 首次使用日期(取不到时回退为最早一条打卡)；第一天之前不能补打卡
        val usageStartDate = (usageStart as? String)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val firstDayOfUsage = listOfNotNull(usageStartDate, dates.minOrNull()).minOrNull()
        // 当月饮食达标状态（与记录页日历同规则）
        val monthDiet = allDiet.filter {
            val d = runCatching { LocalDate.parse(it.date) }.getOrNull()
            d != null && d.year == month.year && d.monthValue == month.monthValue
        }
        val dietStatus = buildMap {
            (1..month.lengthOfMonth()).forEach { day ->
                val dayKcal = monthDiet.filter { it.date == month.atDay(day).toString() }
                    .sumOf { it.calories }
                if (dayKcal > 0 && target != null) {
                    put(day, if (dayKcal <= target) "good" else "over")
                }
            }
        }
        // 打卡历史（补的卡 planName = note，详情里注明「补打卡」）
        val moodByDate = core.diaries.associate { it.date to it }
        val history = core.records.map { record ->
            val diary = moodByDate[record.date]
            HistoryItem(
                date = runCatching { LocalDate.parse(record.date) }.getOrDefault(today),
                planName = record.note?.takeIf { it.isNotBlank() }
                    ?: core.planNames[record.planId] ?: "已删除的计划",
                mood = diary?.mood,
                diaryText = diary?.content.orEmpty()
            )
        }.sortedByDescending { it.date }

        val todayDiary = core.diaries.firstOrNull { it.date == AppClock.todayString() }
        CalendarUi(
            checkedDays = checked,
            makeUpDays = (1..lastMakeUpDay).filter { d ->
                d !in checked && (firstDayOfUsage?.let { !month.atDay(d).isBefore(it) } ?: true)
            }.toSet(),
            streak = FitnessCalculator.streak(dates, today),
            total = dates.size,
            trainedTotal = trainedDates.count { it <= today && !isPlanRestDay(it, schedule) },
            todayDiary = todayDiary,
            dietStatus = dietStatus,
            weights = core.weights,
            history = history,
            allDietLogs = allDiet
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUi())

    /** 补打卡：为过去漏掉的某天补一条记录（记在当前激活计划名下，note 标注「补打卡」） */
    fun makeUpCheckIn(date: LocalDate) {
        viewModelScope.launch {
            val planId = planRepository.observeActivePlanId().first()
            checkInRepository.save(
                com.yuedong.fitness.data.db.entity.CheckInRecordEntity(
                    date = date.toString(),
                    planId = planId ?: 0L,
                    note = "补打卡",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    /** 保存今日日记（心情 + 一句话） */
    fun saveDiary(mood: Int, content: String) {
        viewModelScope.launch {
            diaryRepository.save(
                com.yuedong.fitness.data.db.entity.DiaryEntryEntity(
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
                CheckInCalendarViewModel(
                    app.container.checkInRepository,
                    app.container.planRepository,
                    app.container.bodyMetricRepository,
                    app.container.diaryRepository,
                    app.container.dietLogRepository,
                    app.container.profileRepository,
                    app.container.exerciseProgressRepository,
                    app.container.settings
                )
            }
        }
    }
}

@Composable
fun CheckInCalendarScreen(
    onBack: () -> Unit,
    /** 点已打卡的日子 / 补卡成功后 → 打开「当日记录」页补全当天数据 */
    onOpenDay: (String) -> Unit = {},
    viewModel: CheckInCalendarViewModel = viewModel(factory = CheckInCalendarViewModel.factory())
) {
    val state by viewModel.uiState.collectAsState()
    val month by viewModel.month.collectAsState()
    var diaryDialogVisible by rememberSaveable { mutableStateOf(false) }
    val today = AppClock.today()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 顶栏（二级页统一 BackHeader）
        BackHeader(title = "打卡日历", onBack = onBack)
        // 统计（一行）
        Row(
            Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "已连续打卡 ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${state.streak}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = DiveMono,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                " 天 · 累计打卡 ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${state.total}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = DiveMono,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                " 天",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                MonthCalendar(
                    checkedDays = state.checkedDays,
                    displayedMonth = month,
                    today = today,
                    dietStatus = state.dietStatus,
                    makeUpDays = state.makeUpDays,
                    onDayClick = { day ->
                        onOpenDay(month.atDay(day).toString())
                    },
                    onMonthChange = { viewModel.showMonth(it) }
                )
                Spacer(Modifier.height(10.dp))
                // 标识图例：与月历标记一一对应
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CalendarLegendItem(ringColor = MaterialTheme.colorScheme.primary, label = "已锻炼")
                    Spacer(Modifier.width(10.dp))
                    CalendarLegendItem(dotColor = DiveDataColors.levelLow, label = "饮食达标")
                    Spacer(Modifier.width(10.dp))
                    CalendarLegendItem(dotColor = DiveDataColors.levelHigh, label = "饮食超标")
                    Spacer(Modifier.width(10.dp))
                    CalendarLegendItem(makeUp = true, label = "可补打卡")
                }
                Spacer(Modifier.height(6.dp))
                    Spacer(Modifier.height(12.dp))
                    // 月度小结：三列统计（打卡/饮食达标/累计）
                    val goodDays = state.dietStatus.values.count { it == "good" }
                    // 淡染色块（非 Card）：月历卡内的小结瓦片，避免卡中卡
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MonthStat("本月打卡", state.checkedDays.size.toString() + " 天")
                            MonthStat("饮食达标", state.dietStatus.values.count { it == "good" }.toString() + " 天")
                            MonthStat("累计训练", state.trainedTotal.toString() + " 天")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    // 今日日记：输入框样式，点进去写/改
                    val td = state.todayDiary
                    Column {
                        Text(
                            "今日日记",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                    MaterialTheme.shapes.small
                                )
                                .clickable { diaryDialogVisible = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (td != null) {
                                Text(
                                    moodEmoji(td.mood),
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            } else {
                                Icon(
                                    androidx.compose.material.icons.Icons.Filled.EditNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(end = 2.dp)
                                )
                            }
                            Text(
                                td?.let { it.content.ifBlank { "（只有心情～）" } }
                                    ?: "今天还没写日记，写一句吧",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (td != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
            }
        }
    }
    // 今日日记编辑（与今日页共用组件）
    if (diaryDialogVisible) {
        com.yuedong.fitness.ui.components.DiaryDialog(
            existing = state.todayDiary,
            onDismiss = { diaryDialogVisible = false },
            onSave = { mood, content ->
                viewModel.saveDiary(mood, content)
                diaryDialogVisible = false
            }
        )
    }
}

/** 打卡日历月度小结三列项 */
@Composable
private fun MonthStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = DiveMono,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 日历图例项：sample 画标记样式（蓝环/绿点/红点/补圈），label 写含义 */
@Composable
private fun CalendarLegendItem(
    label: String,
    ringColor: Color? = null,
    dotColor: Color? = null,
    makeUp: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when {
            ringColor != null -> {
                Box(
                    Modifier
                        .size(14.dp)
                        .border(1.5.dp, ringColor, androidx.compose.foundation.shape.CircleShape)
                )
            }
            dotColor != null -> {
                Box(Modifier.size(8.dp).background(dotColor, androidx.compose.foundation.shape.CircleShape))
            }
            makeUp -> {
                Box(
                    Modifier
                        .size(14.dp)
                        .border(1.25.dp, com.yuedong.fitness.ui.theme.DiveDataColors.levelMid, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "补",
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        color = com.yuedong.fitness.ui.theme.DiveDataColors.levelMid
                    )
                }
            }
        }
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
