package com.yuedong.fitness.ui.records

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.ui.theme.AbyssGreen
import com.yuedong.fitness.ui.theme.AbyssGround
import com.yuedong.fitness.ui.theme.AbyssOutline
import com.yuedong.fitness.ui.theme.DeepBlue
import com.yuedong.fitness.ui.theme.DiveForcedDarkTheme
import com.yuedong.fitness.ui.theme.DiveLantern
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.ui.theme.MarineSnow
import com.yuedong.fitness.ui.theme.MarineSnowDim
import com.yuedong.fitness.ui.theme.MenuBlueDark
import com.yuedong.fitness.ui.theme.MenuOrangeDark
import com.yuedong.fitness.ui.theme.MenuPurpleDark
import com.yuedong.fitness.ui.theme.PosterBg
import com.yuedong.fitness.ui.theme.PosterPanel
import com.yuedong.fitness.ui.theme.ThermoclineBlue
import com.yuedong.fitness.util.AppClock
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * 训练报告 · 分享海报（跟随报表周期：周/月/年，恒为深色，供截图保存到相册）。
 * 视觉：深色底 + 蓝/紫光晕 + 玻璃面板 + 渐变数字 + 完成度圆环；
 * 数据全部来自真实聚合（ReportAgg + 打卡日期 + 资料），无占位内容。
 */

/** 海报数据（跟随报表周期聚合 + 全量打卡日期） */
data class ReportPosterData(
    val periodLabel: String,
    val monthTitle: String,
    val generatedText: String,
    /** 完成度：本期训练次数 / 目标次数（目标 = 每周运动天数 × 当期周数） */
    val completionPct: Int,
    val completionTarget: Int,
    val trainCount: Int,
    val dietOkDays: Int,
    val doneActions: Int,
    val totalActions: Int,
    val streak: Int,
    val daysInPeriod: Int,
    /** 全部有打卡的日期 */
    val checkedDates: Set<LocalDate>,
    /** 本期体重序列（迷你折线；不足两条为空） */
    val weightSeries: List<Double>,
    /** 历史最长连续打卡天数 */
    val longestStreak: Int,
    val badgeName: String,
    val badgeDesc: String,
    /** 徽章为本期内新达成时展示「新解锁」标 */
    val badgeNew: Boolean,
)

/** 历史最长连续打卡天数（按自然日连续） */
fun longestStreakOf(dates: Set<LocalDate>): Int {
    var best = 0
    var run = 0
    var prev: LocalDate? = null
    dates.sorted().forEach { d ->
        run = if (prev != null && prev.plusDays(1) == d) run + 1 else 1
        if (run > best) best = run
        prev = d
    }
    return best
}

/** 成就徽章按真实数据定级（可叠加时取最高档） */
fun posterBadge(monthTrain: Int, streak: Int, total: Int): Pair<String, String> = when {
    streak >= 7 -> "七日连潜" to "连续打卡 7 天以上，节奏已成"
    streak >= 3 -> "三连下潜" to "连续打卡 3 天以上，状态在线"
    total >= 15 -> "习惯成事" to "累计打卡 $total 次，坚持可见"
    monthTrain >= 1 -> "初出茅庐" to "完成本期首次训练"
    else -> "整装待发" to "从下一次训练开始"
}

/** 连击里程碑：当前连续之后的下一个成就 */
private val streakMilestones = listOf(3 to "三连下潜", 7 to "七日连潜", 14 to "坚持不懈", 30 to "月度全勤")

@Composable
fun ReportPoster(
    data: ReportPosterData,
    period: ReportPeriod,
    today: LocalDate,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .clipToBounds()
            .background(Brush.verticalGradient(listOf(PosterBg, AbyssGround)))
    ) {
        // 背景光晕：蓝 / 紫（视觉深度）
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-60).dp)
                .size(210.dp)
                .background(
                    Brush.radialGradient(listOf(ThermoclineBlue.copy(alpha = 0.16f), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-40).dp, y = 80.dp)
                .size(180.dp)
                .background(
                    Brush.radialGradient(listOf(MenuPurpleDark.copy(alpha = 0.10f), Color.Transparent)),
                    CircleShape
                )
        )

        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp)) {
            // ---- 头部：周期徽标 + 标题 + 生成信息 ----
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(50))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Box(Modifier.size(5.dp).background(AbyssGreen, CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(data.periodLabel, color = MarineSnowDim, fontSize = 10.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    data.monthTitle,
                    color = MarineSnow,
                    fontSize = 24.sp, lineHeight = 28.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "悦动 · ${data.generatedText} 生成",
                    color = MarineSnowDim.copy(alpha = 0.7f),
                    fontSize = 9.sp
                )
            }
            Spacer(Modifier.height(16.dp))

            // ---- 本期完成度：数字 + 渐变圆环 ----
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(PosterPanel.copy(alpha = 0.55f), MaterialTheme.shapes.extraLarge)
                    .border(1.dp, Color.White.copy(alpha = 0.06f), MaterialTheme.shapes.extraLarge)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("本期完成度", color = MarineSnowDim, fontSize = 12.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${data.completionPct}%",
                        color = MarineSnow,
                        fontSize = 30.sp, lineHeight = 30.sp,
                        fontWeight = FontWeight.Black, fontFamily = DiveMono
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "已训练 ${data.trainCount} / 目标 ${data.completionTarget} 次",
                        color = MarineSnowDim.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
                CompletionRing(percent = data.completionPct, modifier = Modifier.size(76.dp)) {
                    Icon(
                        Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = ThermoclineBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            // ---- 本期数据面板：六项指标合一，细分隔线（替代卡片堆叠） ----
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(PosterPanel.copy(alpha = 0.55f), MaterialTheme.shapes.extraLarge)
                    .border(1.dp, Color.White.copy(alpha = 0.06f), MaterialTheme.shapes.extraLarge)
            ) {
            Row(Modifier.fillMaxWidth()) {
                PosterStatCell(
                    label = "训练次数", value = data.trainCount, unit = "次",
                    fraction = if (data.completionTarget > 0) (data.trainCount.toFloat() / data.completionTarget).coerceIn(0f, 1f) else 0f,
                    accent = ThermoclineBlue, modifier = Modifier.weight(1f), flat = true
                )
                PosterStatCell(
                    label = "饮食达标", value = data.dietOkDays, unit = "天",
                    fraction = if (data.daysInPeriod > 0) (data.dietOkDays.toFloat() / data.daysInPeriod).coerceIn(0f, 1f) else 0f,
                    accent = AbyssGreen, modifier = Modifier.weight(1f), flat = true
                )
            }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f)))
                Row(Modifier.fillMaxWidth()) {
                PosterStatCell(
                    label = "连续打卡", value = data.streak, unit = "天",
                    fraction = (data.streak.toFloat() / 7f).coerceIn(0f, 1f),
                    accent = DiveLantern, modifier = Modifier.weight(1f), flat = true
                )
                PosterStatCell(
                    label = "完成动作", value = data.doneActions, unit = "项",
                    fraction = if (data.totalActions > 0) data.doneActions.toFloat() / data.totalActions else 0f,
                    accent = DeepBlue, modifier = Modifier.weight(1f), flat = true
                )
            }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f)))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.MonitorWeight,
                            contentDescription = null,
                            tint = MenuPurpleDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("体重变化", color = MarineSnowDim, fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    val hasWeight = data.weightSeries.size >= 2
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            if (hasWeight)
                                String.format("%+.1f", data.weightSeries.last() - data.weightSeries.first())
                            else "—",
                            color = MarineSnow,
                            fontSize = 22.sp, lineHeight = 22.sp,
                            fontWeight = FontWeight.Black, fontFamily = DiveMono
                        )
                        Text(
                            " kg",
                            color = MarineSnowDim,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (hasWeight)
                            String.format("%.1f", data.weightSeries.first()) + " → " +
                                String.format("%.1f", data.weightSeries.last()) + " kg"
                        else "本期体重记录不足",
                        color = MarineSnowDim.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontFamily = DiveMono
                    )
                    if (hasWeight) {
                        Spacer(Modifier.height(6.dp))
                        MiniSparkline(
                            values = data.weightSeries,
                            color = MenuPurpleDark.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth().height(20.dp)
                        )
                    }
                }
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = DiveLantern,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("最长连续", color = MarineSnowDim, fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            data.longestStreak.toString(),
                            color = MarineSnow,
                            fontSize = 22.sp, lineHeight = 22.sp,
                            fontWeight = FontWeight.Black, fontFamily = DiveMono
                        )
                        Text(
                            " 天",
                            color = MarineSnowDim,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "历史最长连续打卡纪录",
                        color = MarineSnowDim.copy(alpha = 0.7f),
                        fontSize = 9.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    // 火焰连击：点亮数 = min(3, 最长连击)
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(3) { i ->
                            Icon(
                                Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = if (i < data.longestStreak.coerceAtMost(3)) DiveLantern
                                else MarineSnowDim.copy(alpha = 0.2f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // ---- 训练日历（跟随周期：周=本周 7 格 / 月=当月网格 / 年=12 个月出勤 + 训练分布） ----
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(PosterPanel.copy(alpha = 0.55f), MaterialTheme.shapes.large)
                    .border(1.dp, AbyssOutline.copy(alpha = 0.4f), MaterialTheme.shapes.large)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        when (period) {
                            ReportPeriod.WEEKLY -> "本周训练日历"
                            ReportPeriod.YEARLY -> "全年出勤月历"
                            else -> "本月训练日历"
                        },
                        color = MarineSnow, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (period == ReportPeriod.YEARLY) {
                        Text("少", color = MarineSnowDim.copy(alpha = 0.6f), fontSize = 9.sp)
                        Spacer(Modifier.width(4.dp))
                        listOf(0.08f, 0.28f, 0.5f, 0.85f).forEach { a ->
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .background(ThermoclineBlue.copy(alpha = a), RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(2.dp))
                        }
                        Spacer(Modifier.width(2.dp))
                        Text("多", color = MarineSnowDim.copy(alpha = 0.6f), fontSize = 9.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (period == ReportPeriod.YEARLY) {
                    // 12 个月出勤：当前月渐变高亮，其余颜色深浅按当月打卡次数
                    val months = (1..12).map { m ->
                        m to data.checkedDates.count { it.year == today.year && it.monthValue == m }
                    }
                    months.chunked(6).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { (m, count) ->
                                val isCurrent = m == today.monthValue
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .background(
                                            when {
                                                isCurrent -> Brush.linearGradient(
                                                    listOf(
                                                        ThermoclineBlue.copy(alpha = 0.30f),
                                                        MenuPurpleDark.copy(alpha = 0.20f)
                                                    )
                                                )
                                                count == 0 -> SolidColor(Color.White.copy(alpha = 0.02f))
                                                else -> SolidColor(
                                                    ThermoclineBlue.copy(
                                                        alpha = 0.15f + (count.coerceAtMost(10) / 10f) * 0.5f
                                                    )
                                                )
                                            },
                                            MaterialTheme.shapes.small
                                        )
                                        .border(
                                            1.dp,
                                            if (isCurrent) ThermoclineBlue.copy(alpha = 0.45f)
                                            else Color.White.copy(alpha = 0.04f),
                                            MaterialTheme.shapes.small
                                        )
                                        .padding(vertical = 7.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        m.toString() + "月",
                                        color = if (count > 0 || isCurrent) MarineSnow else MarineSnowDim.copy(alpha = 0.6f),
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        count.toString(),
                                        color = if (count > 0 || isCurrent) MarineSnow else MarineSnowDim.copy(alpha = 0.45f),
                                        fontSize = 14.sp, lineHeight = 14.sp,
                                        fontWeight = FontWeight.Bold, fontFamily = DiveMono
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    // 当月训练分布：逐日小格热力
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.05f))
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("本月训练分布", color = MarineSnowDim.copy(alpha = 0.7f), fontSize = 9.sp)
                    Spacer(Modifier.height(6.dp))
                    val monthDays = (1..today.lengthOfMonth()).map { today.withDayOfMonth(it) }
                    monthDays.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            week.forEach { d ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(9.dp)
                                        .background(
                                            if (d in data.checkedDates) ThermoclineBlue
                                            else Color.White.copy(alpha = 0.05f),
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                            repeat(7 - week.size) { Box(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                } else {
                    Row(Modifier.fillMaxWidth()) {
                        listOf("一", "二", "三", "四", "五", "六", "日").forEach {
                            Text(
                                it,
                                color = MarineSnowDim.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    val weekStart =
                        if (period == ReportPeriod.WEEKLY) today.with(DayOfWeek.MONDAY)
                        else today.withDayOfMonth(1)
                    val days: List<LocalDate?> =
                        if (period == ReportPeriod.WEEKLY) (0..6).map { weekStart.plusDays(it.toLong()) }
                        else {
                            val offset = weekStart.dayOfWeek.value - 1
                            List(offset) { null } + (1..weekStart.lengthOfMonth())
                                .map { weekStart.plusDays((it - 1).toLong()) }
                        }
                    days.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            week.forEach { day ->
                                Box(Modifier.weight(1f).padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
                                    if (day == null) {
                                        Spacer(Modifier.height(30.dp))
                                    } else {
                                        val checked = day in data.checkedDates
                                        Box(
                                            Modifier
                                                .size(30.dp)
                                                .background(
                                                    if (checked) ThermoclineBlue else Color.Transparent,
                                                    MaterialTheme.shapes.small
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                day.dayOfMonth.toString(),
                                                fontSize = 11.sp,
                                                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                                                color = if (checked) MarineSnow else MarineSnowDim.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        repeat(7 - week.size) { Box(Modifier.weight(1f)) }
                    }
                }
            }
            }
            Spacer(Modifier.height(16.dp))

            // ---- 成就徽章 + 下个成就进度 ----
            val nextMilestone = streakMilestones.firstOrNull { it.first > data.streak }
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(DiveLantern.copy(alpha = 0.08f), MaterialTheme.shapes.extraLarge)
                    .border(1.dp, DiveLantern.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(50.dp)
                            .background(DiveLantern.copy(alpha = 0.14f), MaterialTheme.shapes.large)
                            .border(1.dp, DiveLantern.copy(alpha = 0.4f), MaterialTheme.shapes.large),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = DiveLantern,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(data.badgeName, color = MarineSnow, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            if (data.badgeNew) {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "新解锁",
                                    color = DiveLantern,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(DiveLantern.copy(alpha = 0.12f), RoundedCornerShape(50))
                                        .border(1.dp, DiveLantern.copy(alpha = 0.3f), RoundedCornerShape(50))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(data.badgeDesc, color = MarineSnowDim, fontSize = 12.sp)
                    }
                }
                nextMilestone?.let { (target, name) ->
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DiveLantern.copy(alpha = 0.15f))
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Text("下个成就：$name", color = MarineSnowDim.copy(alpha = 0.8f), fontSize = 9.sp)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "连续 ${data.streak}/$target 天",
                            color = MarineSnowDim.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontFamily = DiveMono
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(50))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth((data.streak.toFloat() / target).coerceIn(0f, 1f))
                                .height(5.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(DiveLantern, MenuOrangeDark)),
                                    RoundedCornerShape(50)
                                )
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // ---- 品牌页脚 ----
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AbyssOutline.copy(alpha = 0.5f))
            )
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(com.yuedong.fitness.R.drawable.mascot_whale),
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(MaterialTheme.shapes.small)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("悦动", color = MarineSnow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("记录每一次进步", color = MarineSnowDim.copy(alpha = 0.7f), fontSize = 10.sp)
                }
                Spacer(Modifier.weight(1f))
                Text("数据均来自本人真实记录", color = MarineSnowDim.copy(alpha = 0.55f), fontSize = 10.sp)
            }
        }
    }
}

/** 完成度圆环：渐变描边 + 入场填充动画，中心放图标 */
@Composable
private fun CompletionRing(
    percent: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(percent) {
        sweep.animateTo(percent.coerceIn(0, 100) / 100f, tween(900, easing = FastOutSlowInEasing))
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 6.dp.toPx()
            val inset = stroke / 2 + 1.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            drawArc(
                color = Color.White.copy(alpha = 0.05f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            drawArc(
                brush = Brush.linearGradient(listOf(ThermoclineBlue, MenuPurpleDark)),
                startAngle = -90f, sweepAngle = 360f * sweep.value, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        content()
    }
}

/** 迷你体重折线 */
@Composable
private fun MiniSparkline(values: List<Double>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0 } ?: 1.0
        val stepX = size.width / (values.size - 1)
        val usable = size.height - 6f
        val points = values.mapIndexed { i, v ->
            Offset(i * stepX, 3f + ((max - v) / range * usable).toFloat())
        }
        for (i in 1 until points.size) {
            drawLine(color, points[i - 1], points[i], strokeWidth = 4f)
        }
        drawCircle(color, radius = 5f, center = points.first())
        drawCircle(color, radius = 5f, center = points.last())
    }
}

/** 数据卡：状态色点 + 渐变数字 + 迷你进度条 */
@Composable
private fun PosterStatCell(
    label: String,
    value: Int,
    unit: String,
    fraction: Float,
    accent: Color,
    modifier: Modifier = Modifier,
    flat: Boolean = false
) {
    Column(
        modifier
            .then(
                if (flat) Modifier
                else Modifier
                    .background(PosterPanel.copy(alpha = 0.55f), MaterialTheme.shapes.large)
                    .border(1.dp, Color.White.copy(alpha = 0.06f), MaterialTheme.shapes.large)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(5.dp).background(accent, androidx.compose.foundation.shape.CircleShape))
            Spacer(Modifier.width(5.dp))
            Text(label, color = MarineSnowDim, fontSize = 11.sp)
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value.toString(),
                style = TextStyle(
                    brush = Brush.verticalGradient(listOf(MarineSnow, MarineSnowDim)),
                    fontSize = 24.sp, lineHeight = 24.sp,
                    fontWeight = FontWeight.Black, fontFamily = DiveMono
                )
            )
            Text(
                " $unit",
                color = MarineSnowDim,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(50))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(3.dp)
                    .background(accent, RoundedCornerShape(50))
            )
        }
    }
}

/**
 * 保存海报到相册（API 29+ 写入 Pictures/悦动；低版本写入应用外部目录）。
 * 返回是否成功；由调用方负责提示。
 */
fun savePosterToGallery(context: Context, bitmap: Bitmap, displayName: String): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/悦动")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            true
        } else {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return false
            val file = java.io.File(dir, "$displayName.png")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        }
    } catch (e: Exception) {
        false
    }
}

/** 分享海报页（独立页面）：整页预览本期海报，保存到相册。恒为深色。 */
@Composable
fun ReportPosterScreen(
    period: ReportPeriod = ReportPeriod.MONTHLY,
    onBack: () -> Unit,
    viewModel: ReportViewModel = viewModel(factory = ReportViewModel.factory())
) {
    val raw by viewModel.raw.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val view = LocalView.current

    // 页面恒为深色，状态栏图标切白，离开恢复
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
        val today = AppClock.today()
        val agg = aggregateReport(period, raw)
        val (badgeName, badgeDesc) = posterBadge(agg.trainCount, agg.streak, agg.total)
        val completionTarget = ((agg.daysInPeriod / 7.0) * raw.weeklyTarget).roundToInt().coerceAtLeast(1)
        val completionPct = (agg.trainCount * 100 / completionTarget).coerceIn(0, 100)
        val periodLabel = when (period) {
            ReportPeriod.WEEKLY -> "周报"
            ReportPeriod.YEARLY -> "年度报告"
            else -> "月度报告"
        }
        val title = when (period) {
            ReportPeriod.WEEKLY -> agg.rangeText
            ReportPeriod.YEARLY -> "${today.year}年"
            else -> "${today.year}年${today.monthValue}月"
        }
        val posterData = ReportPosterData(
            periodLabel = periodLabel,
            monthTitle = title,
            generatedText = "${today.monthValue}月${today.dayOfMonth}日",
            completionPct = completionPct,
            completionTarget = completionTarget,
            trainCount = agg.trainCount,
            dietOkDays = agg.dietOkDays,
            doneActions = agg.doneActions,
            totalActions = agg.totalActions,
            streak = agg.streak,
            daysInPeriod = agg.daysInPeriod,
            checkedDates = raw.dates,
            weightSeries = agg.weights.map { it.second },
            longestStreak = longestStreakOf(raw.dates),
            badgeName = badgeName,
            badgeDesc = badgeDesc,
            badgeNew = agg.trainCount >= 1 && agg.total <= 3,
        )

        Column(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(PosterBg, AbyssGround)))
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
            ) {
                Box(
                    Modifier
                        .clipToBounds()
                        .drawWithContent {
                            graphicsLayer.record { this@drawWithContent.drawContent() }
                            drawContent()
                        }
                ) {
                    ReportPoster(posterData, period, today)
                }
                Spacer(Modifier.height(16.dp))
            }
            Box(
                Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.shapes.medium)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
                        .clickable {
                            scope.launch {
                                val ok = runCatching {
                                    savePosterToGallery(
                                        context,
                                        graphicsLayer.toImageBitmap().asAndroidBitmap(),
                                        "悦动训练报告_${today.year}年${today.monthValue}月"
                                    )
                                }.getOrDefault(false)
                                com.yuedong.fitness.ui.components.YueToaster.show(
                                    if (ok) "已保存到相册「Pictures/悦动」" else "保存失败，请重试"
                                )
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "保存到相册",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
