package com.yuedong.fitness.ui.records

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.ui.components.EmptyState
import com.yuedong.fitness.ui.components.YueDialog
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.BodyMetricEntity
import com.yuedong.fitness.data.db.entity.DietLogEntity
import com.yuedong.fitness.data.repository.BodyMetricRepository
import com.yuedong.fitness.data.repository.CheckInRepository
import com.yuedong.fitness.data.repository.DiaryRepository
import com.yuedong.fitness.data.repository.DietLogRepository
import com.yuedong.fitness.data.repository.PlanRepository
import com.yuedong.fitness.data.repository.ProfileRepository
import com.yuedong.fitness.data.sample.SampleDataGenerator
import com.yuedong.fitness.data.settings.SettingsDataStore
import com.yuedong.fitness.domain.logic.FitnessCalculator
import com.yuedong.fitness.domain.model.FitnessInput
import com.yuedong.fitness.domain.model.Gender
import com.yuedong.fitness.domain.model.Purpose
import com.yuedong.fitness.ui.components.LineChart
import com.yuedong.fitness.ui.components.SkeletonBox
import com.yuedong.fitness.ui.theme.DiveDataColors
import com.yuedong.fitness.BuildConfig
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.util.AppClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 历史列表条目：打卡日期 + 当天计划名 + 心情/日记（若有）。
 */
data class HistoryItem(
    val date: LocalDate,
    val planName: String,
    val mood: Int?,
    val diaryText: String = "",
    /** 当天实际完成的动作名（进度表联查），用于历史行展示 */
    val exercises: List<String> = emptyList()
)

/**
 * 本周概况（规则聚合，全部来自本地真实数据）。
 * dietOkDays = 最近 7 天中"有饮食记录且摄入未超标"的天数。
 */
data class WeeklySummary(
    val weekCheckIns: Int,
    val streak: Int,
    val weightChangeKg: Double?,
    val referenceWeightKg: Double?,
    val latestWeightKg: Double?,
    val dietOkDays: Int = 0
)

data class RecordsUiState(
    val isLoading: Boolean = true,
    val streak: Int = 0,
    val totalCheckIns: Int = 0,
    val displayedMonth: YearMonth = YearMonth.now(),
    val checkedDays: Set<Int> = emptySet(),
    /** 每日饮食状态："good"=达标 / "over"=超标 */
    val dietStatus: Map<Int, String> = emptyMap(),
    val weights: List<BodyMetricEntity> = emptyList(),
    val history: List<HistoryItem> = emptyList(),
    val weekly: WeeklySummary = WeeklySummary(0, 0, null, null, null),
    val weekDietLogs: List<DietLogEntity> = emptyList(),
    val monthDietLogs: List<DietLogEntity> = emptyList(),
    /** 全量饮食日志（全部历史页与饮食记录卡数据源） */
    val allDietLogs: List<DietLogEntity> = emptyList()
)

/**
 * 记录页（模块契约见设计包 §8 Records）。
 * 职责：只读聚合打卡/体重/日记/饮食数据并展示；非职责：不写数据（演示数据生成除外，显式触发）。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecordsViewModel(
    private val checkInRepository: CheckInRepository,
    private val planRepository: PlanRepository,
    bodyMetricRepository: BodyMetricRepository,
    diaryRepository: DiaryRepository,
    private val dietLogRepository: DietLogRepository,
    profileRepository: ProfileRepository,
    private val sampleDataGenerator: SampleDataGenerator,
    private val clearAllData: suspend () -> Unit
) : ViewModel() {

    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    val displayedMonth: StateFlow<YearMonth> = _displayedMonth

    fun previousMonth() = _displayedMonth.update { it.minusMonths(1) }
    fun nextMonth() = _displayedMonth.update { it.plusMonths(1) }

    /** 调试：模拟到明天 / 重置（解决跨天逻辑需要真实等待的测试痛点）。 */
    fun advanceDebugDay() = AppClock.advanceDay()
    fun resetDebugDate() = AppClock.reset()

    /** 补打卡：为过去漏掉的某天补一条打卡记录（记在当前激活计划名下），连击随之重算 */
    fun makeUpCheckIn(date: LocalDate) {
        viewModelScope.launch {
            val planId = planRepository.observeActivePlanId().first()
            checkInRepository.save(
                com.yuedong.fitness.data.db.entity.CheckInRecordEntity(
                    date = date.toString(),
                    planId = planId ?: 0L,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

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

    private val monthDietFlow = _displayedMonth.flatMapLatest { month ->
        dietLogRepository.observeLogsBetween(month.atDay(1).toString(), month.atEndOfMonth().toString())
    }

    /** 全量饮食日志：供"饮食记录"卡与全部历史页使用（月历配色仍用月度流）。 */
    private val allDietFlow = dietLogRepository.observeLogsBetween("2000-01-01", "2999-12-31")

    private val weekDietFlow = AppClock.offsetDays.flatMapLatest { offset ->
        val today = LocalDate.now().plusDays(offset)
        dietLogRepository.observeLogsBetween(today.minusDays(6).toString(), today.toString())
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

    val uiState: StateFlow<RecordsUiState> = combine(
        combine(coreFlow, monthDietFlow, weekDietFlow, targetFlow, AppClock.offsetDays) { core, monthDiet, weekDiet, target, offset ->
            listOf(core, monthDiet, weekDiet, target, offset)
        },
        _displayedMonth,
        allDietFlow
    ) { parts, month, allDiet ->
        @Suppress("UNCHECKED_CAST")
        val core = parts[0] as Core
        @Suppress("UNCHECKED_CAST")
        val monthDiet = parts[1] as List<DietLogEntity>
        @Suppress("UNCHECKED_CAST")
        val weekDiet = parts[2] as List<DietLogEntity>
        val target = parts[3] as Int?
        val today = LocalDate.now().plusDays(parts[4] as Long)

        val allDates = core.records.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
        // 排除"未来日期"的体重（日期快进测试残留），避免最新体重/周变化被荒谬拉偏
        val sortedWeights = core.weights
            .filter { w ->
                val d = runCatching { LocalDate.parse(w.date) }.getOrNull() ?: LocalDate.MIN
                !d.isAfter(today)
            }
            .sortedBy {
                runCatching { LocalDate.parse(it.date) }.getOrNull() ?: LocalDate.MIN
            }

        // 月历：训练圆环 + 饮食圆心双层状态（可叠加）
        val monthDates = allDates
            .filter { it.year == month.year && it.monthValue == month.monthValue }
            .map { it.dayOfMonth }
            .toSet()
        val dietStatus: Map<Int, String> = buildMap {
            (1..month.lengthOfMonth()).forEach { day ->
                val date = month.atDay(day)
                val dayKcal = monthDiet.filter { it.date == date.toString() }.sumOf { it.calories }
                if (dayKcal > 0 && target != null) {
                    put(day, if (dayKcal <= target) "good" else "over")
                }
            }
        }

        // 体重变化：最新一次 vs 7 天前（或更早最近一次）
        val latest = sortedWeights.lastOrNull()
        val weightChange: Double? = latest?.let { last ->
            val latestDate = runCatching { LocalDate.parse(last.date) }.getOrNull() ?: return@let null
            val referenceDate = latestDate.minusDays(7)
            val reference = sortedWeights.lastOrNull { metric ->
                val d = runCatching { LocalDate.parse(metric.date) }.getOrNull()
                d != null && !d.isAfter(referenceDate)
            }
            reference?.let { last.weightKg - it.weightKg }
        }
        val referenceWeight = weightChange?.let { change ->
            val latestW = latest?.weightKg ?: 0.0
            Math.round((latestW - change) * 10) / 10.0
        }

        val weekAgo = today.minusDays(6)
        // 只统计窗口内且不晚于今天的打卡：日期快进留下的"未来打卡"不算数
        val weekCheckIns = allDates.count { !it.isBefore(weekAgo) && !it.isAfter(today) }

        val moodByDate = core.diaries.associate { it.date to it }
        // 历史倒序（最新在前），界面按页展示
        val history = core.records.map { record ->
            val diary = moodByDate[record.date]
            HistoryItem(
                date = runCatching { LocalDate.parse(record.date) }.getOrDefault(today),
                planName = core.planNames[record.planId] ?: "已删除的计划",
                mood = diary?.mood,
                diaryText = diary?.content.orEmpty()
            )
        }.sortedByDescending { it.date }

        RecordsUiState(
            isLoading = false,
            streak = FitnessCalculator.streak(allDates, today),
            totalCheckIns = core.records.size,
            displayedMonth = month,
            checkedDays = monthDates,
            dietStatus = dietStatus,
            weights = sortedWeights,
            history = history,
            weekly = WeeklySummary(
                weekCheckIns = weekCheckIns,
                streak = FitnessCalculator.streak(allDates, today),
                weightChangeKg = weightChange,
                referenceWeightKg = referenceWeight,
                latestWeightKg = latest?.weightKg,
                dietOkDays = weekDiet
                    .groupBy { it.date }
                    .count { (_, logs) ->
                        target == null || logs.sumOf { it.calories } <= target
                    }
            ),
            weekDietLogs = weekDiet,
            monthDietLogs = monthDiet,
            allDietLogs = allDiet
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordsUiState())

    /** 显式生成演示数据（用户在界面上主动触发）。 */
    fun generateSampleData() {
        viewModelScope.launch { sampleDataGenerator.generate() }
    }

    /** 调试：清空全部本地数据，回到首次安装状态（问卷重新开始）。 */
    fun clearAll() {
        viewModelScope.launch { clearAllData() }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                val container = app.container
                RecordsViewModel(
                    checkInRepository = container.checkInRepository,
                    planRepository = container.planRepository,
                    bodyMetricRepository = container.bodyMetricRepository,
                    diaryRepository = container.diaryRepository,
                    dietLogRepository = container.dietLogRepository,
                    profileRepository = container.profileRepository,
                    sampleDataGenerator = container.sampleDataGenerator,
                    clearAllData = { container.clearAllData() }
                )
            }
        }
    }
}

// ---------------- 界面 ----------------

private fun fmtKcal(v: Int): String = v.toString()

@Composable
fun RecordsScreen(
    onGoDiet: () -> Unit = {},
    onOpenFullHistory: (String) -> Unit = {},
    onOpenWeeklyReport: () -> Unit = {},
    onOpenDay: (String) -> Unit = {},
    onOpenWeightTrend: () -> Unit = {},
    onOpenCheckInCalendar: () -> Unit = {},
    viewModel: RecordsViewModel = viewModel(factory = RecordsViewModel.factory())
) {
    val state by viewModel.uiState.collectAsState()
    var showSampleDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }

    if (state.isLoading) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonBox(Modifier.fillMaxWidth().height(140.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBox(Modifier.weight(1.6f).height(206.dp))
                SkeletonBox(Modifier.weight(1f).height(206.dp))
            }
            SkeletonBox(Modifier.fillMaxWidth().height(120.dp))
            SkeletonBox(Modifier.fillMaxWidth().height(110.dp))
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // 标题 + 右上水色晕染装饰（drawBehind 画，不占布局高度）
            val washColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            Box(
                Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val center = Offset(size.width - 30.dp.toPx(), -40.dp.toPx())
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(washColor, Color.Transparent),
                                center = center,
                                radius = 150.dp.toPx()
                            ),
                            radius = 150.dp.toPx(),
                            center = center
                        )
                    }
            ) {
                Column {
                    Text("记录", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            ProgressReportCard(
                weekCheckIns = state.weekly.weekCheckIns,
                dietOkDays = state.weekly.dietOkDays,
                streak = state.weekly.streak,
                totalCheckIns = state.totalCheckIns,
                onClick = onOpenWeeklyReport
            )
        }
        item {
            // 体重趋势（迷你曲线）+ 打卡日历（月度圆环）双列
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WeightTrendCard(
                    weights = state.weights.takeLast(7),
                    onClick = onOpenWeightTrend,
                    modifier = Modifier.weight(1.6f)
                )
                CheckInRingCard(
                    month = state.displayedMonth,
                    checkedCount = state.checkedDays.size,
                    onClick = onOpenCheckInCalendar,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            DietSummaryCard(
                state = state,
                today = today,
                onGoDiet = onGoDiet,
                onShowAll = { onOpenFullHistory(HISTORY_MODE_DIET) }
            )
        }
        item {
            TrainingRecordsCard(
                history = state.history,
                onShowAll = { onOpenFullHistory(HISTORY_MODE_CHECKIN) }
            )
        }
        if (com.yuedong.fitness.BuildConfig.DEBUG) item {
            // 调试工具区：演示数据 + 日期快进（仅 debug 构建显示，市场包不可见）
            val clockOffset by AppClock.offsetDays.collectAsState()
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { viewModel.advanceDebugDay() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (clockOffset > 0) "⏩ 再快进一天（共 +${clockOffset} 天）" else "⏩ 模拟到明天（调试）",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (clockOffset > 0) {
                    TextButton(onClick = { viewModel.resetDebugDate() }) {
                        Text(
                            "↺ 重置日期",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            TextButton(
                onClick = { showSampleDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "生成演示数据（用于调试与展示）",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "🗑 清空数据重新开始",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showSampleDialog) {
        YueDialog(
            onDismissRequest = { showSampleDialog = false },
            title = { Text("生成演示数据？") },
            text = {
                Text(
                    "将向本地数据库写入过去 21 天的打卡、体重、日记与最近 7 天的饮食示例记录，" +
                        "用于调试和演示效果。\n\n注意：今天的同类记录可能被示例数据覆盖；" +
                        "如需清空，可用下方「清空数据重新开始」。"
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.generateSampleData()
                    showSampleDialog = false
                }) { Text("生成") }
            },
            dismissButton = {
                TextButton(onClick = { showSampleDialog = false }) { Text("取消") }
            }
        )
    }

    if (showClearDialog) {
        YueDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空全部数据？") },
            text = {
                Text(
                    "将删除本机上的全部资料、计划、打卡、体重、日记与饮食记录，" +
                        "并回到首次安装的问卷状态。此操作不可撤销。"
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.clearAll()
                    showClearDialog = false
                }) { Text("清空") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }
}

/** 卡片通用容器：surface 底、无阴影、16dp 圆角 */
@Composable
private fun SurfaceCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        content()
    }
}

/** 卡片标题行：蓝色小图标 + 标题 */
@Composable
private fun CardTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(7.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

/** 我的进步报告：四列核心数字 + 本周摘要条，整卡进周报 */
@Composable
private fun ProgressReportCard(
    weekCheckIns: Int,
    dietOkDays: Int,
    streak: Int,
    totalCheckIns: Int,
    onClick: () -> Unit
) {
    SurfaceCard {
        Column(Modifier.clickable { onClick() }.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(7.dp))
                Text("训练报告", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                ReportStat("$weekCheckIns", "本周训练", Modifier.weight(1f))
                ReportStat("$dietOkDays", "饮食达标", Modifier.weight(1f))
                ReportStat("$streak", "连续天数", Modifier.weight(1f))
                ReportStat("$totalCheckIns", "总计天数", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ReportStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = DiveMono,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 体重趋势：7 日迷你曲线（进独立趋势页看完整） */
@Composable
private fun WeightTrendCard(
    weights: List<BodyMetricEntity>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SurfaceCard(modifier) {
        Column(Modifier.clickable { onClick() }.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CardTitle(Icons.Filled.ShowChart, "体重趋势")
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "曲线 · 7日均线 · 测量记录",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            if (weights.size < 2) {
                Box(
                    Modifier.fillMaxWidth().height(132.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState("记录两天体重后\n这里会显示趋势曲线")
                }
            } else {
                val trendGreen = DiveDataColors.levelLow
                val dates = weights.map { w ->
                    runCatching { LocalDate.parse(w.date) }.getOrNull()
                        ?.format(DateTimeFormatter.ofPattern("M/d", Locale.CHINA)) ?: ""
                }
                LineChart(
                    values = weights.map { it.weightKg },
                    modifier = Modifier.fillMaxWidth().height(132.dp),
                    // 窄卡只留首/中/尾三个日期，避免 x 轴标签互相重叠
                    xLabels = dates.mapIndexed { i, s ->
                        if (i == 0 || i == dates.lastIndex || i == dates.size / 2) s else ""
                    },
                    lineColor = trendGreen,
                    pointColor = trendGreen,
                    areaFill = true
                )
            }
        }
    }
}

/** 打卡日历：月度打卡进度环（进独立日历页补打卡），内容区高度与体重趋势卡对齐 */
@Composable
private fun CheckInRingCard(
    month: YearMonth,
    checkedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fraction = if (month.lengthOfMonth() > 0) {
        (checkedCount.toFloat() / month.lengthOfMonth()).coerceIn(0f, 1f)
    } else 0f
    val ringColor = DiveDataColors.levelLow
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    SurfaceCard(modifier) {
        Column(Modifier.clickable { onClick() }.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CardTitle(Icons.Filled.CalendarMonth, "打卡日历")
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "当月打卡进度",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            // 窄卡竖排居中：进度环 + 打卡天数（高度与体重趋势卡对齐）
            Box(
                Modifier.fillMaxWidth().height(132.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(Modifier.size(84.dp)) {
                            val stroke = 9.dp.toPx()
                            val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                            val topLeft = Offset(stroke / 2, stroke / 2)
                            drawArc(
                                color = trackColor,
                                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Butt),
                                topLeft = topLeft, size = arcSize
                            )
                            if (fraction > 0f) {
                                drawArc(
                                    color = ringColor,
                                    startAngle = -90f, sweepAngle = 360f * fraction, useCenter = false,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                                    topLeft = topLeft, size = arcSize
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "${month.monthValue}",
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = DiveMono,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "月",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${checkedCount}/${month.lengthOfMonth()} 天",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = DiveMono,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/** 饮食记录：今日/本周/本月笔数 + 三列摄入统计 */
@Composable
private fun DietSummaryCard(
    state: RecordsUiState,
    today: LocalDate,
    onGoDiet: () -> Unit,
    onShowAll: () -> Unit
) {
    val todayLogs = state.weekDietLogs.filter { it.date == today.toString() }
    val todayKcal = todayLogs.sumOf { it.calories }
    val weekKcal = state.weekDietLogs.sumOf { it.calories }
    val month = state.displayedMonth
    val elapsedDays = if (month == YearMonth.from(today)) today.dayOfMonth else month.lengthOfMonth()
    val monthKcal = state.monthDietLogs.sumOf { it.calories }
    val monthAvg = if (elapsedDays > 0) (monthKcal.toFloat() / elapsedDays).roundToInt() else 0
    val weekAvg = (weekKcal.toFloat() / 7).roundToInt()

    SurfaceCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CardTitle(Icons.Filled.Restaurant, "饮食记录")
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier
                        .heightIn(min = 32.dp)
                        .clickable(onClick = onShowAll),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "全部",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "查看全部",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            // 主指标：今日摄入
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    fmtKcal(todayKcal),
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = DiveMono,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    " 千卡 · 今日摄入",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "本周平均 ${fmtKcal(weekAvg)} 千卡 · 本月平均 ${fmtKcal(monthAvg)} 千卡",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


/** 训练记录：按计划聚合的图标磁贴，左右滑动翻页（进全部历史看明细）；数据为全部历史累计 */
@Composable
private fun TrainingRecordsCard(
    history: List<HistoryItem>,
    onShowAll: () -> Unit
) {
    val byPlan = history
        .groupBy { it.planName }
        .map { (name, items) -> Triple(name, items.size, workoutIcon(name)) }
        .sortedByDescending { it.second }

    SurfaceCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CardTitle(Icons.Filled.FitnessCenter, "训练记录")
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier
                        .heightIn(min = 32.dp)
                        .clickable(onClick = onShowAll),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "全部",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "查看全部",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "全部记录 · 按计划累计打卡次数",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            if (byPlan.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(com.yuedong.fitness.R.drawable.mascot_shiba),
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(MaterialTheme.shapes.large)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "还没有训练记录，从「今日」页开始第一次打卡吧",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else if (byPlan.size == 1) {
                // 只有一个计划：整行横排磁贴，右侧不再留空
                val (name, count, icon) = byPlan.first()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { onShowAll() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "累计打卡 $count 次",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = DiveMono,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (byPlan.size == 2) {
                // 两个计划：两块并排铺满
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    byPlan.forEach { (name, count, icon) ->
                        TrainingTile(name, count, icon, Modifier.weight(1f), onShowAll)
                    }
                }
            } else {
                // 每页 6 个磁贴（2 行 × 3 列），左右滑动翻页
                val pages = byPlan.chunked(6)
                val pagerState = rememberPagerState(pageCount = { pages.size })
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                    val tiles = pages[page]
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            tiles.take(3).forEach { (name, count, icon) ->
                                TrainingTile(name, count, icon, Modifier.weight(1f), onShowAll)
                            }
                            repeat(3 - tiles.size.coerceAtMost(3)) { Spacer(Modifier.weight(1f)) }
                        }
                        if (tiles.size > 3) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                tiles.drop(3).forEach { (name, count, icon) ->
                                    TrainingTile(name, count, icon, Modifier.weight(1f), onShowAll)
                                }
                                repeat(3 - (tiles.size - 3)) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
                if (pages.size > 1) {
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        pages.indices.forEach { i ->
                            val selected = pagerState.currentPage == i
                            Box(
                                Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(width = if (selected) 16.dp else 6.dp, height = 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainingTile(
    name: String,
    count: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(
            name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "$count 次",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = DiveMono,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 按计划名关键词挑一个贴切图标 */
private fun workoutIcon(planName: String): ImageVector = when {
    planName.contains("力量") || planName.contains("健身房") -> Icons.Filled.FitnessCenter
    planName.contains("燃脂") || planName.contains("跑") -> Icons.Filled.DirectionsRun
    planName.contains("徒手") || planName.contains("活力") -> Icons.Filled.SelfImprovement
    planName.contains("HIIT") || planName.contains("间歇") -> Icons.Filled.LocalFireDepartment
    else -> Icons.Filled.FitnessCenter
}
