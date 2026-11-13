package com.yuedong.fitness.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
import com.yuedong.fitness.data.repository.BodyMetricRepository
import com.yuedong.fitness.data.repository.ProfileRepository
import com.yuedong.fitness.ui.components.BackHeader
import com.yuedong.fitness.ui.components.EmptyState
import com.yuedong.fitness.ui.components.LineChart
import com.yuedong.fitness.ui.components.WeightLogDialog
import com.yuedong.fitness.ui.theme.DiveDataColors
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.ui.theme.DiveNumeric
import com.yuedong.fitness.util.AppClock
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 体重趋势页（独立页，从记录页/今日页进入）：
 * 与记录页同一套 LineChart 折线（点按/滑动切换节点、节点上方显示数值），
 * 范围档位 近一周/近30天/近90天/近一年，逐日数据（无记录日沿用上次体重）。
 */
class WeightTrendViewModel(
    private val bodyMetricRepository: BodyMetricRepository,
    profileRepository: ProfileRepository
) : ViewModel() {

    data class TrendUi(
        val weights: List<BodyMetricEntity> = emptyList(),
        val heightCm: Int? = null,
        val targetWeightKg: Double? = null
    )

    val uiState: StateFlow<TrendUi> = combine(
        bodyMetricRepository.observeAll(),
        profileRepository.observeProfile()
    ) { weights, profile ->
        TrendUi(
            weights = weights.sortedBy {
                runCatching { LocalDate.parse(it.date) }.getOrDefault(LocalDate.MIN)
            },
            heightCm = profile?.heightCm,
            targetWeightKg = profile?.targetWeightKg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrendUi())

    /** 记录今日体重（趋势页就地添加） */
    fun saveWeight(kg: Double) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            bodyMetricRepository.saveCurrent(
                BodyMetricEntity(
                    date = AppClock.todayString(),
                    weightKg = kg,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                WeightTrendViewModel(app.container.bodyMetricRepository, app.container.profileRepository)
            }
        }
    }
}

private val rangeOptions = listOf(7 to "近一周", 30 to "近30天", 90 to "近90天", 365 to "近一年")

/** 测量记录列表每次展开的条数：默认 5 条，数据再多也分页加载，不一次性铺满页面 */
private const val RECORDS_PAGE_SIZE = 5

/** 测量记录行：记录 + 较上一条变化（delta 为 null = 没有上一条可比） */
private data class RecordRow(val record: BodyMetricEntity, val delta: Double?, val date: LocalDate)

@Composable
fun WeightTrendScreen(
    onBack: () -> Unit,
    viewModel: WeightTrendViewModel = viewModel(factory = WeightTrendViewModel.factory())
) {
    val state by viewModel.uiState.collectAsState()
    var rangeDays by remember { mutableIntStateOf(7) }
    var showLogDialog by remember { mutableStateOf(false) }
    var selectedIdx by remember(rangeDays) { mutableIntStateOf(-1) } // -1 = 默认最新
    var visibleRecordCount by remember { mutableIntStateOf(RECORDS_PAGE_SIZE) }
    val today = AppClock.today()

    val weights = state.weights
    val latest = weights.lastOrNull()
    val prev = weights.getOrNull(weights.size - 2)
    val heightM = state.heightCm?.let { it / 100.0 }
    val bmi = heightM?.takeIf { it > 0.3 }?.let { m ->
        latest?.let { it.weightKg / (m * m) }
    }
    val prevBmi = heightM?.takeIf { it > 0.3 }?.let { m ->
        prev?.let { it.weightKg / (m * m) }
    }

    // 增重/减重模式：目标体重高于首个记录 = 增重模式（颜色语义随目标方向翻转）
    val startWeight = weights.firstOrNull()?.weightKg
    val targetWeightKg = state.targetWeightKg
    val isGainMode = targetWeightKg != null && startWeight != null && targetWeightKg > startWeight

    // 范围过滤
    val inRange = weights.mapNotNull { w ->
        val d = runCatching { LocalDate.parse(w.date) }.getOrNull()
        if (d != null && !d.isBefore(today.minusDays((rangeDays - 1).toLong())) && !d.isAfter(today)) {
            w to d
        } else null
    }

    // 逐日序列（无记录日沿用上次体重，曲线连续直观——与记录页同逻辑）
    data class TrendPoint(val date: LocalDate, val weightKg: Double, val real: Boolean)
    val dailyPoints: List<TrendPoint> = if (inRange.isEmpty()) {
        emptyList()
    } else {
        val byDay = inRange.associateBy { it.second }
        val pts = mutableListOf<TrendPoint>()
        var carry = inRange.first().first.weightKg
        var d = inRange.first().second
        while (!d.isAfter(today)) {
            val hit = byDay[d]
            if (hit != null) {
                carry = hit.first.weightKg
                pts += TrendPoint(d, carry, real = true)
            } else {
                pts += TrendPoint(d, carry, real = false)
            }
            d = d.plusDays(1)
        }
        pts
    }

    // 7 日均线：截至当日的近 7 个逐日值均值（窗口不足取已有全部）
    val avgValues = dailyPoints.mapIndexed { i, _ ->
        val from = (i - 6).coerceAtLeast(0)
        dailyPoints.subList(from, i + 1).map { it.weightKg }.average()
    }

    // 顶部指标：较上次变化
    val lastDelta = if (latest != null && prev != null) latest.weightKg - prev.weightKg else null

    // 目标进度：按「剩余距离占最初剩余距离」的比例计（当前体重越过目标时归零，不会虚标 100%）
    val targetProgress: Triple<Double, Double, Int>? = run {
        val t = state.targetWeightKg
        val cur = latest?.weightKg
        if (t == null || cur == null || startWeight == null || (t - startWeight).let { it > -0.05 && it < 0.05 }) null
        else {
            val initialRemaining = kotlin.math.abs(t - startWeight)
            val nowRemaining = kotlin.math.abs(t - cur)
            val progress = if (initialRemaining <= 0.001) 1.0
            else (1.0 - nowRemaining / initialRemaining).coerceIn(0.0, 1.0)
            Triple(kotlin.math.abs(t - cur), t - startWeight, (progress * 100).roundToInt())
        }
    }
    /** 当前体重是否已越过目标（方向与目标相反） */
    val passedTarget = latest != null && targetWeightKg != null && startWeight != null &&
        (latest.weightKg - targetWeightKg) * (targetWeightKg - startWeight) > 0.05

    // 长周期变化：近30天 / 近90天（对比不晚于 N 天前的最近一次记录）
    fun deltaOverDays(days: Long): Double? {
        val ref = weights.lastOrNull {
            runCatching { LocalDate.parse(it.date) }.getOrDefault(LocalDate.MIN) <= today.minusDays(days)
        } ?: return null
        if (latest == null || latest === ref) return null
        return latest.weightKg - ref.weightKg
    }
    val delta30 = deltaOverDays(30)
    val delta90 = deltaOverDays(90)

    // 测量记录（新在前）+ 较上一条变化；分页展示
    val recordRows = weights.indices.reversed().map { i ->
        val w = weights[i]
        val prevKg = weights.getOrNull(i - 1)?.weightKg
        RecordRow(w, prevKg?.let { w.weightKg - it }, runCatching { LocalDate.parse(w.date) }.getOrDefault(today))
    }
    val visibleRecords = recordRows.take(visibleRecordCount)

    val safeIndex = selectedIdx.coerceIn(0, (dailyPoints.size - 1).coerceAtLeast(0))
    val selected = dailyPoints.getOrNull(safeIndex)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // 顶栏（二级页统一 BackHeader）
        BackHeader(title = "体重趋势", onBack = onBack)
        Spacer(Modifier.height(8.dp))
        // 三指标卡（参考图）：当前体重 / 目标体重 / 距离目标还差 + 目标进度条
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    StatWithIcon(
                        Icons.Filled.MonitorWeight,
                        "当前体重",
                        latest?.let { String.format("%.1f", it.weightKg) } ?: "--",
                        DiveDataColors.levelLow,
                        Modifier.weight(1f)
                    )
                    StatWithIcon(
                        Icons.Filled.GpsFixed,
                        "目标体重",
                        targetWeightKg?.let { String.format("%.1f", it) } ?: "--",
                        MaterialTheme.colorScheme.primary,
                        Modifier.weight(1f)
                    )
                    StatWithIcon(
                        Icons.Filled.Flag,
                        if (passedTarget) "已超过目标" else "距离目标还差",
                        targetProgress?.let { String.format("%.1f", it.first) } ?: "--",
                        DiveDataColors.levelMid,
                        Modifier.weight(1f)
                    )
                }
                targetProgress?.let { (_, _, pct) ->
                    Spacer(Modifier.height(14.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { (pct / 100.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = DiveDataColors.levelLow,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt,
                        drawStopIndicator = {}
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "初始体重 " + (startWeight?.let { String.format("%.1f", it) } ?: "--") + " kg",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = DiveMono,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "当前 " + (latest?.let { String.format("%.1f", it.weightKg) } ?: "--") +
                                " → 目标 " + (targetWeightKg?.let { String.format("%.1f", it) } ?: "--") + " kg",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = DiveMono,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "$pct%",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = DiveMono,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        // 较上次卡：体重 / BMI / 近30天 / 近90天（承接原「近30天变化」卡的数据）
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text("较上次", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth()) {
                    DeltaStat("体重", lastDelta, "kg", isGainMode, Modifier.weight(1f))
                    DeltaStat("BMI", bmi?.let { b -> prevBmi?.let { b - it } }, "", isGainMode, Modifier.weight(1f))
                    DeltaStat("近30天", delta30, "kg", isGainMode, Modifier.weight(1f))
                    DeltaStat("近90天", delta90, "kg", isGainMode, Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        // 图表卡：与记录页同款折线
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(16.dp)) {
                // 范围档位
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rangeOptions.forEach { (days, label) ->
                        FilterChip(
                            selected = rangeDays == days,
                            onClick = { rangeDays = days },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (dailyPoints.isEmpty()) {
                    EmptyState(
                        "该时间段内没有体重记录\n点右上角「＋」补记一条",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LineChart(
                        values = dailyPoints.map { it.weightKg },
                        selectedIndex = safeIndex,
                        onSelected = { selectedIdx = it },
                        xLabels = dailyPoints.map { it.date.format(DateTimeFormatter.ofPattern("M.d")) },
                        hollowIndices = dailyPoints.indices.filter { !dailyPoints[it].real }.toSet(),
                        avgValues = avgValues,
                        areaFill = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    selected?.let { p ->
                        val dateText = p.date.format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINA))
                        Text(
                            if (p.real) "$dateText · 体重 ${p.weightKg} 公斤"
                            else "$dateText · ${p.weightKg} 公斤（沿用上次记录）",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        // 测量记录卡：标题右侧「＋」记录体重；列表分页加载，数据再多不撑爆页面
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("测量记录", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "共 ${recordRows.size} 条",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    // 记录体重入口：右上角蓝色圆形＋（点击弹记录对话框；视觉 30dp 圆，命中区 48dp）
                    androidx.compose.material3.Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clickable { showLogDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "记录体重",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(30.dp).padding(6.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                if (visibleRecords.isEmpty()) {
                    EmptyState("还没有测量记录，点右上角「＋」记第一笔")
                } else {
                    visibleRecords.forEach { row ->
                        RecordLine(row, isGainMode, today)
                    }
                    if (recordRows.size > visibleRecordCount) {
                        TextButton(
                            onClick = { visibleRecordCount += RECORDS_PAGE_SIZE },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "加载更多（还有 ${recordRows.size - visibleRecordCount} 条）",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showLogDialog) {
        WeightLogDialog(
            initial = latest?.weightKg ?: 65.0,
            onDismiss = { showLogDialog = false },
            onSave = { kg ->
                viewModel.saveWeight(kg)
                showLogDialog = false
            }
        )
    }
}

/** 单条测量记录：日期 + 体重 + 较上一条变化 */
@Composable
private fun RecordLine(row: RecordRow, isGainMode: Boolean, today: LocalDate) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            row.date.format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp)
        )
        Text(
            String.format("%.1f", row.record.weightKg) + " kg",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = DiveMono,
            modifier = Modifier.weight(1f)
        )
        if (row.delta != null) {
            val good = (row.delta >= 0) == isGainMode
            Text(
                (if (row.delta >= 0) "▲ +" else "▼ ") + String.format("%.1f", row.delta) + " kg",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = DiveMono,
                color = if (good) MaterialTheme.colorScheme.tertiary else DiveDataColors.levelHigh
            )
        } else {
            Text("-", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 三指标之一：小图标 + 标签 + 彩色大数字（weight 由调用处的 Row 传入） */
@Composable
private fun StatWithIcon(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = valueColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                style = DiveNumeric.medium,
                color = valueColor
            )
            if (value != "--") {
                Text(
                    " kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

/** 较上次四列之一：标签 + ▲▼ 带方向色变化量 */
@Composable
private fun DeltaStat(
    label: String,
    delta: Double?,
    unit: String,
    isGainMode: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        val text = when {
            delta == null -> "--"
            // BMI 增减幅度小，保留一位；体重保留一位
            else -> (if (delta >= 0) "▲ " else "▼ ") + String.format("%.1f", kotlin.math.abs(delta))
        }
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = DiveMono,
            color = when {
                delta == null -> MaterialTheme.colorScheme.onSurfaceVariant
                (delta >= 0) == isGainMode -> MaterialTheme.colorScheme.tertiary
                else -> DiveDataColors.levelHigh
            }
        )
        if (delta != null && unit.isNotEmpty()) {
            Text(
                unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
