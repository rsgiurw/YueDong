package com.yuedong.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.yuedong.fitness.ui.theme.DiveDataColors
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import androidx.compose.runtime.snapshotFlow

/**
 * 打卡月历（模块契约见设计包 §8 Records）。
 * 双层状态圆（同一天可叠加训练与饮食两种状态，互不遮挡）：
 *   外环（细描边）= 训练完成（蓝）
 *   圆心（实心点）= 饮食结果（绿=达标 / 红=超标）
 * 今天 = 数字加粗 + 外圈淡蓝描边。无任何记录的日子不画圆。
 * 交互：格子上左右滑动跟手翻月（HorizontalPager），松手自动落位；换月后回调父级同步数据。
 */
@Composable
fun MonthCalendar(
    checkedDays: Set<Int>,
    displayedMonth: YearMonth,
    today: java.time.LocalDate,
    modifier: Modifier = Modifier,
    /** 饮食状态："good"=达标绿 / "over"=超标红；缺省=当天无饮食记录。 */
    dietStatus: Map<Int, String> = emptyMap(),
    /** 可补打卡的日子（过去、未打卡）：格子里显示「补」标记，点它即可补上 */
    makeUpDays: Set<Int> = emptySet(),
    /** 点击某一天（回调参数 = 几号），供外层弹出当天详情 */
    onDayClick: (Int) -> Unit = {},
    /** 翻到某个月份后回调（滑动落位或外部按钮翻页），父级同步数据 */
    onMonthChange: (YearMonth) -> Unit = {}
) {
    // 分页锚点：以首次显示的月份为基准，向前后各预留 2400 个月（约百年），足够翻页
    val initialMonth = remember { displayedMonth }
    val initialPage = remember { 2400 }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { initialPage * 2 })

    fun monthFor(page: Int): YearMonth = initialMonth.plusMonths((page - initialPage).toLong())

    // 翻页落位 → 通知父级换月加载该月数据
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            onMonthChange(monthFor(page))
        }
    }
    // 月份头直接读 pager 当前页：与网格同一个状态源，滑动与按钮翻页都不会脱节
    val headerMonth = monthFor(pagerState.currentPage)
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Column(modifier.fillMaxWidth()) {
        // 月份切换行（组件内置：滑动与按钮共用同一状态源）
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "上个月")
            }
            Text(
                headerMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年M月")),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "下个月")
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { weekday ->
                Box(
                    Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        weekday,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        // 跟手翻页：内容随手指平移，松手自动落位（系统日历同款交互）
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            MonthGrid(
                month = initialMonth.plusMonths((page - initialPage).toLong()),
                checkedDays = checkedDays,
                today = today,
                dietStatus = dietStatus,
                makeUpDays = makeUpDays,
                onDayClick = onDayClick
            )
        }
    }
}

/** 单月格子：周一开头补空位，固定行高 48dp（≥48dp 触摸目标），整月节奏一致 */
@Composable
private fun MonthGrid(
    month: YearMonth,
    checkedDays: Set<Int>,
    today: java.time.LocalDate,
    dietStatus: Map<Int, String>,
    makeUpDays: Set<Int>,
    onDayClick: (Int) -> Unit
) {
    val firstDay = month.atDay(1)
    val leadingBlanks = firstDay.dayOfWeek.value - 1 // 周一=1 → 前面补 0 个空位
    val cells: List<Int?> = List(leadingBlanks) { null } + (1..month.lengthOfMonth()).toList()

    Column(Modifier.fillMaxWidth()) {
        // 固定行高：每行一个等高槽位（48dp），有圈没圈的行完全一样高，整体对齐规整
        val cellHeight = 48.dp
        cells.chunked(7).forEach { row ->
            Row(Modifier.fillMaxWidth().height(cellHeight)) {
                row.forEach { day ->
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(enabled = day != null) { day?.let(onDayClick) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            DayCell(
                                day = day,
                                trained = day in checkedDays,
                                diet = dietStatus[day],
                                isToday = month.atDay(day) == today,
                                makeUp = day in makeUpDays
                            )
                        }
                    }
                }
                repeat(7 - row.size) {
                    Box(Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
    }
}

/** 日历格子专用字号（紧凑网格数字，命名常量） */
private val DAY_NUMBER = 13.sp
private val MAKEUP_MARK = 12.sp

/** 网格数字统一修剪行高（圆内垂直居中） */
private fun dayTextStyle(size: androidx.compose.ui.unit.TextUnit) = TextStyle(
    fontSize = size,
    lineHeight = size,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
)

@Composable
private fun DayCell(day: Int, trained: Boolean, diet: String?, isToday: Boolean, makeUp: Boolean = false) {
    // 可补打卡：琥珀色细圈 + 「补」字（替代数字），一眼看出这天能补
    if (makeUp && !trained) {
        Box(
            Modifier
                .size(30.dp)
                .border(1.25.dp, DiveDataColors.levelMid, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "补",
                style = dayTextStyle(MAKEUP_MARK),
                color = DiveDataColors.levelMid
            )
        }
        return
    }
    val dietDot = when (diet) {
        "good" -> DiveDataColors.levelLow
        "over" -> DiveDataColors.levelHigh
        else -> null
    }

    // 36dp 触控格；视觉元素统一落在居中的 30dp 圆内，保证整月节奏一致：
    //   今天 = 容器色浅填充 · 训练 = 1.5dp 细环 · 饮食 = 右上角 6dp 徽标点
    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
        if (isToday) {
            Box(
                Modifier
                    .size(30.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
        if (trained) {
            Box(
                Modifier
                    .size(30.dp)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
            )
        }
        Text(
            "$day",
            style = dayTextStyle(DAY_NUMBER),
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isToday -> MaterialTheme.colorScheme.onPrimary
                trained -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        if (dietDot != null) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(6.dp)
                    .background(dietDot, androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}
