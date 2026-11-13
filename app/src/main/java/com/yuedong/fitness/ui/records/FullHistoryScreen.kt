package com.yuedong.fitness.ui.records

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import com.yuedong.fitness.ui.components.EmptyState
import com.yuedong.fitness.ui.components.ListActionSheet
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlin.math.abs
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.DiaryEntryEntity
import com.yuedong.fitness.data.repository.CheckInRepository
import com.yuedong.fitness.data.repository.DiaryRepository
import com.yuedong.fitness.data.repository.DietLogRepository
import com.yuedong.fitness.data.repository.PlanRepository
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.util.AppClock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 全部历史页的模式：打卡 / 饮食。 */
const val HISTORY_MODE_CHECKIN = "checkin"
const val HISTORY_MODE_DIET = "diet"

/** 饮食按天聚合后的展示行。 */
data class DietDayGroup(
    val date: LocalDate,
    val count: Int,
    val totalCalories: Int,
    val logs: List<DietLogRow>
) {
    data class DietLogRow(
        val mealLabel: String,
        val foodName: String,
        val grams: Double,
        val calories: Int
    )
}

/**
 * 全部历史页（记录页"查看全部"的目标页）。
 * 职责：全量历史的长列表浏览（真 LazyColumn 虚拟化，几百条也只渲染屏幕内的行）；
 * 非职责：不做统计汇总（记录页职责）、不支持编辑。
 */
@Composable
fun FullHistoryScreen(
    mode: String,
    onBack: () -> Unit,
    onOpenDay: (String) -> Unit = {},
    viewModel: FullHistoryViewModel = viewModel(
        key = mode,
        factory = FullHistoryViewModel.factory(mode)
    )
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 顶栏：返回 + 标题 + 汇总
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Column {
                Text(
                    if (mode == HISTORY_MODE_DIET) "全部饮食记录" else "全部打卡历史",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    state.summaryText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // 月份切片：任何时刻只看一个月，数据量与历史总量无关
        var selectedMonth by remember { mutableStateOf(YearMonth.from(AppClock.today())) }
        var dietFilter by remember { mutableStateOf("全部") }
        val currentMonth = YearMonth.from(AppClock.today())

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { selectedMonth = selectedMonth.minusMonths(1) },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "上一月")
            }
            AnimatedContent(
                targetState = selectedMonth,
                transitionSpec = {
                    val forward = targetState.isAfter(initialState)
                    (slideInHorizontally(tween(200)) { if (forward) it / 2 else -it / 2 } + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally(tween(200)) { if (forward) -it / 2 else it / 2 } + fadeOut(tween(200)))
                },
                label = "monthTitle"
            ) { m ->
                Text(
                    "${m.year}年${m.monthValue}月",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            IconButton(
                onClick = { selectedMonth = selectedMonth.plusMonths(1) },
                enabled = selectedMonth < currentMonth,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "下一月",
                    tint = if (selectedMonth < currentMonth) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
        if (mode == HISTORY_MODE_DIET) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("全部", "早餐", "午餐", "晚餐", "加餐", "高热量").forEach { f ->
                    androidx.compose.material3.FilterChip(
                        selected = dietFilter == f,
                        onClick = { dietFilter = f },
                        label = { Text(f, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        // 月份分页：跟手滑动翻页（与打卡日历同款）；落位后同步数据与顶栏
        val anchorMonth = remember { YearMonth.from(AppClock.today()) }
        val anchorPage = remember { 2400 }
        val pagerState = rememberPagerState(initialPage = anchorPage, pageCount = { anchorPage * 2 })
        fun monthFor(page: Int): YearMonth = anchorMonth.plusMonths((page - anchorPage).toLong())
        val coroutineScope = rememberCoroutineScope()

        // 顶栏月份与滑动页双向同步：滑动落位 → 顶栏跟随；按钮换月 → 滚动到对应页
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.settledPage }.collect { page ->
                val m = monthFor(page)
                if (m != selectedMonth) selectedMonth = m
            }
        }
        LaunchedEffect(selectedMonth) {
            val target = anchorPage + java.time.temporal.ChronoUnit.MONTHS.between(anchorMonth, selectedMonth)
            if (pagerState.currentPage != target.toInt()) pagerState.animateScrollToPage(target.toInt())
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { page ->
            val selMonth = anchorMonth.plusMonths((page - anchorPage).toLong())
            Column(Modifier.fillMaxSize()) {
                if (mode == HISTORY_MODE_DIET) {
                    val monthDays = state.dietDays
                        .filter {
                            it.date.year == selMonth.year && it.date.monthValue == selMonth.monthValue
                        }
                        .let { days ->
                            if (dietFilter == "全部") days
                            else days.mapNotNull { day ->
                                val logs = day.logs.filter {
                                    if (dietFilter == "高热量") it.calories >= 500 else it.mealLabel == dietFilter
                                }
                                if (logs.isEmpty()) null
                                else day.copy(
                                    count = logs.size,
                                    totalCalories = logs.sumOf { it.calories },
                                    logs = logs
                                )
                            }
                        }
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
                    ) {
                        if (monthDays.isEmpty()) {
                            item(key = "empty") {
                                EmptyState("本月没有符合条件的记录\n换个月份或筛选条件试试")
                            }
                        } else {
                            val recorded = monthDays.size
                            val avg = monthDays.sumOf { it.totalCalories } / recorded
                            stickyHeader(key = "month_header") {
                                MonthHeader(
                                    "${selMonth.year}年${selMonth.monthValue}月",
                                    "记录 $recorded 天 · 日均 $avg 千卡"
                                )
                            }
                            items(monthDays, key = { it.date.toEpochDay() }) { day ->
                                DietDaySection(day)
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f)
                                )
                            }
                        }
                    }
                } else {
                    val monthCheckins = state.checkins.filter {
                        it.date.year == selMonth.year && it.date.monthValue == selMonth.monthValue
                    }
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
                    ) {
                        if (monthCheckins.isEmpty()) {
                            item(key = "empty") {
                                EmptyState("本月没有打卡记录\n换个月份看看，或从「今日」页开始")
                            }
                        } else {
                            stickyHeader(key = "month_header") {
                                MonthHeader(
                                    "${selMonth.year}年${selMonth.monthValue}月",
                                    "打卡 ${monthCheckins.size} 次"
                                )
                            }
                            itemsIndexed(monthCheckins, key = { _, item -> item.date.toEpochDay() }) { index, item ->
                                CheckInRow(
                                    item = item,
                                    onClick = { onOpenDay(item.date.toString()) },
                                    onDelete = { viewModel.deleteCheckin(item.date.toString()) }
                                )
                                if (index != monthCheckins.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 吸顶月份头：滚动时固定在顶部，带当月小结 */
@Composable
private fun MonthHeader(title: String, summary: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                summary,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = DiveMono,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DietDaySection(day: DietDayGroup) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                day.date.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${day.count} 笔 · 共 ${day.totalCalories} 千卡",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        day.logs.forEach { log ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    log.mealLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(
                    log.foodName,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    "${log.grams.toInt()}g · ${log.calories} 千卡",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CheckInRow(
    item: HistoryItem,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { menuOpen = true }
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                item.planName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                item.date.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (item.exercises.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    buildString {
                        append(item.exercises.take(3).joinToString("、"))
                        if (item.exercises.size > 3) append(" 等 " + item.exercises.size + " 项")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            "✓",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold
        )
    }
    if (menuOpen) {
        ListActionSheet(
            visible = true,
            title = item.date.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)),
            actions = listOf(
                com.yuedong.fitness.ui.components.ListAction(
                    Icons.Filled.Delete,
                    "删除这条打卡",
                    MaterialTheme.colorScheme.error
                ) { onDelete() }
            ),
            onDismiss = { menuOpen = false }
        )
    }
}

/** 全部历史页状态：两种模式各自只填一半。 */
data class FullHistoryUiState(
    val checkins: List<HistoryItem> = emptyList(),
    val dietDays: List<DietDayGroup> = emptyList()
) {
    val isEmpty: Boolean get() = checkins.isEmpty() && dietDays.isEmpty()
    val summaryText: String
        get() = when {
            checkins.isNotEmpty() -> "共 ${checkins.size} 条打卡"
            dietDays.isNotEmpty() -> "共 ${dietDays.size} 天 · ${dietDays.sumOf { it.count }} 笔饮食"
            else -> ""
        }
}

class FullHistoryViewModel(
    mode: String,
    private val checkInRepository: CheckInRepository,
    private val progressRepository: com.yuedong.fitness.data.repository.ExerciseProgressRepository,
    planRepository: PlanRepository,
    diaryRepository: DiaryRepository,
    dietLogRepository: DietLogRepository
) : ViewModel() {

    /** 左滑删除某天的打卡（Room 失效通知会自动刷新列表） */
    fun deleteCheckin(date: String) {
        viewModelScope.launch { checkInRepository.deleteByDate(date) }
    }

    private val mealLabels = mapOf(
        "BREAKFAST" to "早餐", "LUNCH" to "午餐", "DINNER" to "晚餐", "SNACK" to "加餐"
    )

    private val checkinFlow = if (mode == HISTORY_MODE_CHECKIN) {
        combine(
            checkInRepository.observeAll(),
            planRepository.observePlans(),
            diaryRepository.observeAll()
        ) { records, plans, diaries ->
            val moodByDate = diaries.associate { it.date to it.mood }
            val planNamesById = plans.associate { it.id to it.name }
            FullHistoryUiState(
                checkins = records
                    .sortedByDescending { it.date }
                    .map { record ->
                        val doneNames = progressRepository.doneExerciseNamesForDate(record.date)
                        HistoryItem(
                            date = runCatching { LocalDate.parse(record.date) }.getOrDefault(AppClock.today()),
                            planName = planNamesById[record.planId] ?: "已删除的计划",
                            mood = moodByDate[record.date],
                            exercises = doneNames
                        )
                    }
            )
        }
    } else {
        flowOf(FullHistoryUiState())
    }

    private val dietFlow = if (mode == HISTORY_MODE_DIET) {
        dietLogRepository.observeLogsBetween("2000-01-01", "2999-12-31").map { logs ->
            val days = logs.groupBy { it.date }
                .map { (date, dayLogs) ->
                    DietDayGroup(
                        date = runCatching { LocalDate.parse(date) }.getOrDefault(AppClock.today()),
                        count = dayLogs.size,
                        totalCalories = dayLogs.sumOf { it.calories },
                        logs = dayLogs.sortedBy { it.createdAt }.map {
                            DietDayGroup.DietLogRow(
                                mealLabel = mealLabels[it.mealType] ?: "加餐",
                                foodName = it.foodName,
                                grams = it.grams,
                                calories = it.calories
                            )
                        }
                    )
                }.sortedByDescending { it.date }
            FullHistoryUiState(dietDays = days)
        }
    } else {
        flowOf(FullHistoryUiState())
    }

    val uiState: StateFlow<FullHistoryUiState> = combine(checkinFlow, dietFlow) { a, b ->
        FullHistoryUiState(checkins = a.checkins, dietDays = b.dietDays)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FullHistoryUiState())

    companion object {
        fun factory(mode: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as YueDongApplication
                val container = app.container
                FullHistoryViewModel(
                    mode = mode,
                    progressRepository = app.container.exerciseProgressRepository,
                    checkInRepository = container.checkInRepository,
                    planRepository = container.planRepository,
                    diaryRepository = container.diaryRepository,
                    dietLogRepository = container.dietLogRepository
                )
            }
        }
    }
}
