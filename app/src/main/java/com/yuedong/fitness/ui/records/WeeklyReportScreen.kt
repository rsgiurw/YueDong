package com.yuedong.fitness.ui.records

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuedong.fitness.data.db.entity.DietLogEntity
import com.yuedong.fitness.domain.model.FitnessInput
import com.yuedong.fitness.domain.model.Gender
import com.yuedong.fitness.domain.model.Purpose
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.ui.anim.rememberCountUpValue
import com.yuedong.fitness.ui.components.LineChart
import com.yuedong.fitness.ui.theme.DiveDataColors
import com.yuedong.fitness.ui.theme.DiveGradients
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.ui.theme.DiveNumeric
import com.yuedong.fitness.ui.theme.AbyssGround
import com.yuedong.fitness.ui.theme.DiveForcedDarkTheme
import com.yuedong.fitness.ui.theme.DiveLantern
import com.yuedong.fitness.ui.theme.PosterBg
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.util.AppClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** 报表周期 */
enum class ReportPeriod { DAILY, WEEKLY, MONTHLY, YEARLY }

/** 原始打卡/饮食/体重数据（全量） */
data class ReportRaw(
    val dates: Set<LocalDate> = emptySet(),
    val dietKcalByDay: Map<LocalDate, Int> = emptyMap(),
    val dietLogs: List<DietLogEntity> = emptyList(),
    val weights: List<Pair<LocalDate, Double>> = emptyList(),
    val target: Int? = null,
    val streak: Int = 0,
    val total: Int = 0,
    /** 每周运动天数目标（来自健康资料，完成度分母用） */
    val weeklyTarget: Int = 3,
    /** 当期逐项训练进度（done 计数用） */
    val progress: List<com.yuedong.fitness.data.db.entity.ExerciseProgressEntity> = emptyList()
)

/** 宏量营养素占比（按热量加权：蛋白/碳水 4 kcal·g，脂肪 9 kcal·g） */
data class MacroShare(val proteinPct: Int, val carbPct: Int, val fatPct: Int)



/** 某个周期的聚合结果 */
data class ReportAgg(
    val title: String = "",
    val rangeText: String = "",
    val trainCount: Int = 0,
    val lastTrainCount: Int = 0,
    val dietOkDays: Int = 0,
    val lastDietOkDays: Int = 0,
    val dietLogDays: Int = 0,
    val avgKcal: Int = 0,
    val lastAvgKcal: Int = 0,
    val weightStart: Double? = null,
    val weightEnd: Double? = null,
    val weightDeltaKg: Double? = null,
    val weights: List<Pair<LocalDate, Double>> = emptyList(),
    val macro: MacroShare? = null,
    val streak: Int = 0,
    val total: Int = 0,
    /** 当期每天的打卡点阵（日期, 是否打卡）；年报不提供（365 天太密） */
    val dotDays: List<Pair<java.time.LocalDate, Boolean>> = emptyList(),
    /** 本期结论：一句话 + 要点（标签, 数值文案, 是否向好） */
    val headline: String = "",
    val points: List<Triple<String, String, Boolean>> = emptyList(),
    val doneActions: Int = 0,
    val totalActions: Int = 0,
    val dietOkRate: Int = 0,
    /** 当期自然天数（年报点阵不生成，用它兜底算频率占比） */
    val daysInPeriod: Int = 1
)

class ReportViewModel(
    checkInRepository: com.yuedong.fitness.data.repository.CheckInRepository,
    dietLogRepository: com.yuedong.fitness.data.repository.DietLogRepository,
    bodyMetricRepository: com.yuedong.fitness.data.repository.BodyMetricRepository,
    profileRepository: com.yuedong.fitness.data.repository.ProfileRepository,
    progressRepository: com.yuedong.fitness.data.repository.ExerciseProgressRepository
) : ViewModel() {

    val raw: StateFlow<ReportRaw> = combine(
        checkInRepository.observeAll(),
        dietLogRepository.observeLogsBetween("2000-01-01", "2999-12-31"),
        bodyMetricRepository.observeAll(),
        profileRepository.observeProfile(),
        progressRepository.observeAll()
    ) { checkIns, dietLogs, weights, profile, progress ->
        val dates = checkIns.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }.toSet()
        val dietByDay = dietLogs.mapNotNull { d ->
            runCatching { LocalDate.parse(d.date) }.getOrNull()?.let { it to d.calories }
        }.groupBy({ it.first }, { it.second }).mapValues { e -> e.value.sum() }
        val w = weights.mapNotNull { m ->
            runCatching { LocalDate.parse(m.date) }.getOrNull()?.let { it to m.weightKg }
        }
        val target = profile?.let {
            com.yuedong.fitness.domain.logic.FitnessCalculator.metrics(
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
        ReportRaw(
            dates = dates,
            dietKcalByDay = dietByDay,
            dietLogs = dietLogs,
            weights = w,
            target = target,
            progress = progress,
            streak = com.yuedong.fitness.domain.logic.FitnessCalculator.streak(dates.toList(), AppClock.today()),
            total = dates.size,
            weeklyTarget = profile?.weeklyExerciseDays ?: 3
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportRaw())

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                ReportViewModel(
                    app.container.checkInRepository,
                    app.container.dietLogRepository,
                    app.container.bodyMetricRepository,
                    app.container.profileRepository,
                    app.container.exerciseProgressRepository
                )
            }
        }
    }
}

/** 按周期聚合（纯函数）。日 = 今天对比昨天；周/月/年 = 本期对比上期。 */
fun aggregateReport(period: ReportPeriod, raw: ReportRaw): ReportAgg {
    val today = AppClock.today()
    val fmt = DateTimeFormatter.ofPattern("M月d日")

    fun dietOkInRange(range: ClosedRange<LocalDate>) =
        raw.dietKcalByDay.filter { (day, kcal) -> day in range && (raw.target == null || kcal <= raw.target) }.size

    fun trainInRange(range: ClosedRange<LocalDate>) = raw.dates.count { it in range }

    fun avgKcalInRange(range: ClosedRange<LocalDate>): Int {
        val vals = raw.dietKcalByDay.filter { it.key in range }.values
        return if (vals.isEmpty()) 0 else vals.average().roundToInt()
    }

    fun macroInRange(range: ClosedRange<LocalDate>): MacroShare? {
        val logs = raw.dietLogs.filter {
            (runCatching { LocalDate.parse(it.date) }.getOrNull())?.let { it in range } == true
        }
        val p = logs.sumOf { it.proteinG }
        val c = logs.sumOf { it.carbG }
        val f = logs.sumOf { it.fatG }
        val pc = p * 4.0; val cc = c * 4.0; val fc = f * 9.0
        val sum = pc + cc + fc
        if (sum <= 0.0) return null
        val pp = (pc / sum * 100).roundToInt()
        val cp = (cc / sum * 100).roundToInt()
        return MacroShare(proteinPct = pp, carbPct = cp, fatPct = (100 - pp - cp).coerceAtLeast(0))
    }

    val inRangeWeights = raw.weights.filter { it.first >= today.minusDays(365) }.sortedBy { it.first }

    val range: ClosedRange<LocalDate>
    val last: ClosedRange<LocalDate>
    when (period) {
        ReportPeriod.DAILY -> { range = today..today; last = today.minusDays(1)..today.minusDays(1) }
        ReportPeriod.WEEKLY -> {
            val monday = today.with(java.time.DayOfWeek.MONDAY)
            range = monday..today; last = monday.minusDays(7)..monday.minusDays(1)
        }
        ReportPeriod.MONTHLY -> {
            val first = today.withDayOfMonth(1)
            range = first..today
            last = first.minusMonths(1).withDayOfMonth(1)..first.minusDays(1)
        }
        ReportPeriod.YEARLY -> {
            val first = today.withDayOfYear(1)
            range = first..today
            last = first.minusYears(1).withDayOfYear(1)..first.minusDays(1)
        }
        else -> { range = today..today; last = today..today }
    }

    // 打卡点阵：当期范围内每天是否打卡（年报太密不生成）
    val dotDays = if (period == ReportPeriod.YEARLY) emptyList()
    else generateSequence(range.start) { it.plusDays(1) }
        .takeWhile { it <= range.endInclusive.coerceAtMost(today) }
        .map { it to (it in raw.dates) }
        .toList()

    val title = when (period) {
        ReportPeriod.DAILY -> "今日"
        ReportPeriod.WEEKLY -> "本周"
        ReportPeriod.MONTHLY -> "${today.monthValue}月"
        ReportPeriod.YEARLY -> "${today.year}年"
    }
    val rangeText = when (period) {
        ReportPeriod.DAILY -> today.format(fmt)
        ReportPeriod.YEARLY -> "${today.year}年"
        else -> "${range.start.format(fmt)} - ${range.endInclusive.format(fmt)}"
    }

    val periodWeights = raw.weights.filter { it.first in range }.sortedBy { it.first }
    val weightStart = periodWeights.firstOrNull()?.second
    val weightEnd = periodWeights.lastOrNull()?.second
    val weightDeltaKg = if (periodWeights.size >= 2 && weightStart != null && weightEnd != null) {
        weightEnd - weightStart
    } else null
    val periodProgress = raw.progress.filter {
        (runCatching { LocalDate.parse(it.date) }.getOrNull())?.let { it in range } == true
    }
    val doneActions = periodProgress.count { it.done }
    val totalActions = periodProgress.size
    val lastRangeKcal = raw.dietKcalByDay.filter { it.key in last }.values
    val lastAvgKcal = if (lastRangeKcal.isEmpty()) 0 else lastRangeKcal.average().roundToInt()
    val periodLength = dotDays.size.coerceAtLeast(1)
    val daysInPeriod = (range.endInclusive.coerceAtMost(today).toEpochDay() - range.start.toEpochDay() + 1)
        .toInt().coerceAtLeast(1)
    val agg_trainCount = trainInRange(range)
    val agg_lastTrainCount = trainInRange(last)
    val agg_dietOkDays = dietOkInRange(range)
    val agg_lastDietOkDays = dietOkInRange(last)
    val wd = weightDeltaKg
    // 结论：训练量对比 + 饮食达标 + 体重变化 → 一句话 + 三条要点
    val trainDiff = agg_trainCount - agg_lastTrainCount
    val headline = buildString {
        if (agg_trainCount == 0) {
            append("本期还没有打卡记录，从今天开始第一个圈吧")
        } else {
            append("本期训练 ").append(agg_trainCount).append(" 次")
            if (agg_lastTrainCount > 0) {
                append(if (trainDiff > 0) "，比上期多 ${trainDiff} 次" else if (trainDiff < 0) "，比上期少 ${-trainDiff} 次" else "，与上期持平")
            }
            append(if (agg_dietOkDays >= agg_trainCount && agg_trainCount > 0) "，饮食执行到位" else "")
        }
    }
    val points = listOf(
        Triple("训练", "${agg_trainCount} 次（上期 ${agg_lastTrainCount}）", trainDiff >= 0),
        Triple("饮食达标", "${agg_dietOkDays} 天（上期 ${agg_lastDietOkDays}）", agg_dietOkDays >= agg_lastDietOkDays),
        Triple("体重变化", wd?.let { String.format("%+.1f kg", it) } ?: "暂无数据", wd?.let { it >= 0f } ?: false)
    )


    val macro = macroInRange(range)

    return ReportAgg(
        title = title,
        rangeText = rangeText,
        trainCount = trainInRange(range),
        lastTrainCount = trainInRange(last),
        dietOkDays = dietOkInRange(range),
        lastDietOkDays = dietOkInRange(last),
        dietLogDays = raw.dietKcalByDay.count { it.key in range },
        avgKcal = avgKcalInRange(range),
        lastAvgKcal = lastAvgKcal,
        weightStart = weightStart,
        weightEnd = weightEnd,
        weightDeltaKg = weightDeltaKg,
        weights = periodWeights,
        macro = macro,
        streak = raw.streak,
        total = raw.total,
        dotDays = dotDays,
        totalActions = totalActions,
        dietOkRate = agg_dietOkDays * 100 / periodLength,
        headline = headline,
        points = points,
        daysInPeriod = daysInPeriod
    )
}

/**
 * 训练报表（我的进步报告）：恒暗高端风（DiveForcedDarkTheme 局部强制深渊色板）。
 * 简洁分区：概览四格（数字滚动）+ 计划完成率 / 本月出勤热图 /
 * 体重趋势渐变面积图（数据点光晕）/ 营养堆叠条；右上导出 + 底部按钮生成分享海报。
 * 周期切换 周/月/年；数字全部 DiveMono 等宽体（DESIGN.md 契约）。
 */
@Composable
fun WeeklyReportScreen(
    onBack: () -> Unit,
    onOpenPoster: (ReportPeriod) -> Unit = {},
    viewModel: ReportViewModel = viewModel(factory = ReportViewModel.factory())
) {
    val raw by viewModel.raw.collectAsState()
    var period by rememberSaveable { mutableStateOf(ReportPeriod.WEEKLY) }
    val agg = aggregateReport(period, raw)

    // Hero 恒为深色，进页时状态栏图标切白，离开恢复
    val view = LocalView.current
    DisposableEffect(view) {
        var ctx = view.context
        while (ctx is ContextWrapper && ctx !is Activity) ctx = ctx.baseContext
        val controller = (ctx as? Activity)?.window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            if (controller != null && previous != null) controller.isAppearanceLightStatusBars = previous
        }
    }

    DiveForcedDarkTheme {
        val cs = MaterialTheme.colorScheme
        val greenAccent = cs.tertiary

        Column(
            Modifier
                .fillMaxSize()
                .background(DiveGradients.reportHeroBg())
                .verticalScroll(rememberScrollState())
        ) {
            // ---------- 头部：返回 + 标题 + 导出 ----------
            Row(
                Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.10f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "训练报告",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = { onOpenPoster(period) },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.10f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "生成分享海报",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            GlassPeriodPills(selected = period, onSelect = { period = it }, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(22.dp))

        // ---------- 分区（依次上浮入场） ----------
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(2.dp))

            // 本期概览：四个关键数字 + 计划完成率
            SectionCaption("本期概览", agg.rangeText)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCell("训练次数", agg.trainCount, "次", Modifier.weight(1f))
                SummaryCell("饮食达标", agg.dietOkDays, "天", Modifier.weight(1f))
                SummaryCell("完成动作", agg.doneActions, "项", Modifier.weight(1f))
                SummaryCell("连续打卡", raw.streak, "天", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            val barTarget = if (agg.totalActions == 0) 0f else agg.doneActions.toFloat() / agg.totalActions
            val barAnim by animateFloatAsState(
                targetValue = barTarget,
                animationSpec = tween(800, easing = FastOutSlowInEasing),
                label = "planBar"
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("计划完成率", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text(
                    "${agg.doneActions}/${agg.totalActions}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DiveMono,
                    color = cs.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(cs.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(50))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(barAnim.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(cs.primary, RoundedCornerShape(50))
                )
            }
            Spacer(Modifier.height(24.dp))

            // 体重趋势：渐变面积图 + 数据点光晕，可点住拖动查看每天
            SectionCaption("体重趋势", "公斤")
            Spacer(Modifier.height(8.dp))
            EnterIn(1) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(Modifier.padding(16.dp)) {
                        if (agg.weights.isNotEmpty()) {
                            var sel by remember(agg.weights) { mutableIntStateOf(-1) }
                            val selIdx = if (sel == -1) agg.weights.lastIndex else sel.coerceIn(0, agg.weights.lastIndex)
                            LineChart(
                                values = agg.weights.map { it.second },
                                selectedIndex = selIdx,
                                onSelected = { sel = it },
                                xLabels = agg.weights.map { "${it.first.monthValue}.${it.first.dayOfMonth}" },
                                pointHalo = true,
                                areaFill = true,
                                areaTopAlpha = 0.30f
                            )
                            Spacer(Modifier.height(12.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(cs.outlineVariant.copy(alpha = 0.4f))
                            )
                            Spacer(Modifier.height(10.dp))
                            val picked = agg.weights.getOrNull(selIdx)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("最新记录", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                                    Text(
                                        picked?.let {
                                            "${it.first.format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINA))} · ${String.format("%.1f", it.second)} 公斤"
                                        } ?: "暂无记录",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (agg.weightDeltaKg != null) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("较首日", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                                        Text(
                                            String.format("%+.1f", agg.weightDeltaKg) + " 公斤",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = DiveMono
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                "本周期没有体重记录",
                                style = MaterialTheme.typography.bodyMedium,
                                color = cs.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // 本月出勤：迷你热图 + 出勤率
            val today = AppClock.today()
            val checkedDays = raw.dates
                .filter { it.year == today.year && it.monthValue == today.monthValue }
                .map { it.dayOfMonth }
                .toSet()
            val attendRate = checkedDays.size * 100 / today.lengthOfMonth()
            SectionCaption("本月出勤", "${checkedDays.size} / ${today.lengthOfMonth()} 天")
            Spacer(Modifier.height(8.dp))
            EnterIn(2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            listOf("一", "二", "三", "四", "五", "六", "日").forEach { wd ->
                                Text(
                                    wd,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        val offset = today.withDayOfMonth(1).dayOfWeek.value - 1
                        val cells: List<Int?> = List(offset) { null } + (1..today.lengthOfMonth()).toList()
                        cells.chunked(7).forEach { week ->
                            Row(Modifier.fillMaxWidth()) {
                                week.forEach { day ->
                                    Box(Modifier.weight(1f).padding(horizontal = 2.dp)) {
                                        if (day == null) {
                                            Spacer(Modifier.height(16.dp))
                                        } else {
                                            Box(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(16.dp)
                                                    .background(
                                                        when {
                                                            day == today.dayOfMonth -> DiveDataColors.levelMid
                                                            day in checkedDays -> cs.primary.copy(alpha = 0.85f)
                                                            else -> cs.outlineVariant.copy(alpha = 0.5f)
                                                        },
                                                        RoundedCornerShape(50)
                                                    )
                                            )
                                        }
                                    }
                                }
                                repeat(7 - week.size) { Box(Modifier.weight(1f)) }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(cs.outlineVariant.copy(alpha = 0.4f))
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("出勤率", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                            Spacer(Modifier.weight(1f))
                            Text(
                                "$attendRate%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DiveMono,
                                color = cs.primary
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // 营养分析：平均热量 + 达标 + 营养堆叠条
            SectionCaption("营养分析", agg.rangeText)
            Spacer(Modifier.height(8.dp))
            EnterIn(4) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("平均热量摄入", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            val kcalAnim = rememberCountUpValue(agg.avgKcal)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "${kcalAnim.value.roundToInt()}",
                                    style = DiveNumeric.large
                                )
                                Text(
                                    " 千卡",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(greenAccent.copy(alpha = 0.12f), MaterialTheme.shapes.extraSmall)
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, null, tint = greenAccent, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "达标 ${agg.dietOkDays} 天",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = greenAccent
                                )
                            }
                        }
                        agg.macro?.let { macro ->
                            Spacer(Modifier.height(16.dp))
                            Text("三大营养素比例", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth().height(12.dp).clipToBounds()) {
                                Box(Modifier.weight(macro.proteinPct.coerceAtLeast(1).toFloat()).fillMaxHeight().background(DiveDataColors.protein))
                                Box(Modifier.weight(macro.carbPct.coerceAtLeast(1).toFloat()).fillMaxHeight().background(DiveDataColors.carb))
                                Box(Modifier.weight(macro.fatPct.coerceAtLeast(1).toFloat()).fillMaxHeight().background(DiveDataColors.fat))
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth()) {
                                MacroLegendCell(DiveDataColors.protein, "蛋白质", macro.proteinPct, Modifier.weight(1f))
                                MacroLegendCell(DiveDataColors.carb, "碳水", macro.carbPct, Modifier.weight(1f))
                                MacroLegendCell(DiveDataColors.fat, "脂肪", macro.fatPct, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // 生成分享海报
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(cs.primary.copy(alpha = 0.10f), MaterialTheme.shapes.medium)
                    .border(1.dp, cs.primary.copy(alpha = 0.25f), MaterialTheme.shapes.medium)
                    .clickable { onOpenPoster(period) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "生成分享海报",
                    color = cs.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
    }
}

/** 进入视口时依次上浮入场：淡入 + 上移复位，index 决定延迟档位 */
@Composable
private fun EnterIn(index: Int, content: @Composable () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        progress.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
    }
    Box(
        Modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 30f
        }
    ) { content() }
}

/** 毛玻璃周期切换：深色 Hero 上的半透明白胶囊，选中 = 白底深字 */
@Composable
private fun GlassPeriodPills(
    selected: ReportPeriod,
    onSelect: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        "周" to ReportPeriod.WEEKLY,
        "月" to ReportPeriod.MONTHLY,
        "年" to ReportPeriod.YEARLY
    )
    Row(
        modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (label, period) ->
            val isSelected = period == selected
            Box(
                Modifier
                    .weight(1f)
                    .clipToBounds()
                    .background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(50))
                    .clickable { onSelect(period) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

/** 分区小标题：左标题 + 右侧补充（细字分区头，高端报表样式） */
@Composable
private fun SectionCaption(title: String, trailing: String?, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                trailing,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 营养图例：色点 + 名称 + 加粗百分比 */
@Composable
private fun MacroLegendCell(color: Color, label: String, pct: Int, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        Text(
            "$pct%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = DiveMono
        )
    }
}

/** 概览四格：数字滚动 + 单位 + 标签（深色面板 + 细边框） */
@Composable
private fun SummaryCell(label: String, value: Int, unit: String, modifier: Modifier = Modifier) {
    val shown = rememberCountUpValue(value)
    Column(
        modifier
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                MaterialTheme.shapes.medium
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), MaterialTheme.shapes.medium)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "${shown.value.roundToInt()}",
                style = DiveNumeric.medium
            )
            Text(
                " $unit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
