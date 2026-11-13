package com.yuedong.fitness.ui.diet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.rememberLazyListState
import com.yuedong.fitness.ui.anim.rememberCountUpValue
import kotlinx.coroutines.flow.filter
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.yuedong.fitness.ui.components.YueDialog
import com.yuedong.fitness.ui.components.EmptyState
import com.yuedong.fitness.ui.components.SearchField
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.CustomFoodEntity
import com.yuedong.fitness.data.db.entity.DietLogEntity
import com.yuedong.fitness.data.repository.DietLogRepository
import com.yuedong.fitness.data.repository.BodyMetricRepository
import com.yuedong.fitness.data.repository.ProfileRepository
import com.yuedong.fitness.domain.logic.DietAdvisor
import com.yuedong.fitness.domain.logic.DietTip
import com.yuedong.fitness.domain.logic.QuickAddSuggester
import com.yuedong.fitness.domain.logic.FitnessCalculator
import com.yuedong.fitness.domain.logic.FoodCatalog
import com.yuedong.fitness.ui.components.ListActionSheet
import com.yuedong.fitness.ui.components.HelpHintIcon
import com.yuedong.fitness.ui.components.SheetCard
import com.yuedong.fitness.ui.theme.DiveDataColors
import com.yuedong.fitness.ui.theme.DiveGradients
import com.yuedong.fitness.ui.theme.diveDark
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.ui.theme.DiveNumeric
import androidx.compose.material3.minimumInteractiveComponentSize
import com.yuedong.fitness.domain.model.FitnessInput
import com.yuedong.fitness.domain.model.Gender
import com.yuedong.fitness.domain.model.Purpose
import com.yuedong.fitness.domain.model.FitnessMetrics
import com.yuedong.fitness.ui.components.MacroRing
import com.yuedong.fitness.ui.components.TipCard
import com.yuedong.fitness.util.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.LocalTime
import kotlin.math.roundToInt
import com.yuedong.fitness.util.AppClock

/** 餐次常量与工具。 */
object MealTypes {
    const val BREAKFAST = "BREAKFAST"
    const val LUNCH = "LUNCH"
    const val DINNER = "DINNER"
    const val SNACK = "SNACK"

    val ordered = listOf(BREAKFAST, LUNCH, DINNER, SNACK)

    fun label(value: String): String = when (value) {
        BREAKFAST -> "早餐"
        LUNCH -> "午餐"
        DINNER -> "晚餐"
        else -> "加餐"
    }

    /** 按当前时段预选餐次，减少一次点击。 */
    fun defaultByClock(): String {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 5..9 -> BREAKFAST
            in 10..13 -> LUNCH
            in 17..20 -> DINNER
            else -> SNACK
        }
    }
}

/** 一份食物选择结果（内置目录与自定义食物统一表示）。 */
data class FoodSelection(
    val name: String,
    val kcalPer100: Int,
    val proteinPer100: Double,
    val carbPer100: Double,
    val fatPer100: Double
)

// ---------------- 建议页状态 ----------------

data class DietUiState(
    val isLoading: Boolean = true,
    val hasProfile: Boolean = false,
    val purposeLabel: String? = null,
    val purposeName: String? = null,
    val weightKg: Double? = null,
    val metrics: FitnessMetrics? = null
)

// ---------------- 今日记录状态 ----------------

data class DietTotals(
    val calories: Int = 0,
    val protein: Double = 0.0,
    val carb: Double = 0.0,
    val fat: Double = 0.0
)

data class DietLogState(
    val isLoading: Boolean = true,
    val entries: List<DietLogEntity> = emptyList(),
    val totals: DietTotals = DietTotals(),
    val customFoods: List<CustomFoodEntity> = emptyList()
)

/**
 * 饮食页（模块契约见设计包 §8 Diet）。
 * 职责：营养建议展示 + 今日饮食记录（选食物/克数/餐次、汇总对比目标、删除）。
 * 非职责：不修改资料；食物营养值为常见均值，界面已注明。
 */
internal class DietViewModel(
    profileRepository: ProfileRepository,
    bodyMetricRepository: BodyMetricRepository,
    private val dietLogRepository: DietLogRepository,
    private val waterRepository: com.yuedong.fitness.data.repository.WaterRepository
) : ViewModel() {

    /** 今日饮食日志随"调试日期"联动（日期快进后记录的也是模拟日期）。 */
    private val todayLogsFlow = AppClock.offsetDays.flatMapLatest {
        dietLogRepository.observeLogsByDate(AppClock.todayString())
    }

    /** 记录饮食的已选清单：点食材行会跳详情页，VM 托管保证往返不丢（完成保存后 clearPickedFoods） */
    val pickedFoods = mutableStateMapOf<String, PickedFood>()

    /** 记录饮食所选餐次（跳页返回后保留） */
    var pickedMeal: String? = null

    /** 记录饮食页完成保存后清空已选 */
    fun clearPickedFoods() = pickedFoods.clear()

    /** 今日饮水 (已喝 ml, 目标 ml)：口径与今日页一致 */
    val water: StateFlow<Pair<Int, Int>> = combine(
        waterRepository.observeTotalForDate(AppClock.todayString()),
        profileRepository.observeProfile()
    ) { totalMl, profile ->
        val weightKg = profile?.currentWeightKg?.takeIf { it > 0 } ?: 65.0
        val target = ((weightKg * 30).coerceIn(1200.0, 3000.0).toInt() / 50) * 50
        totalMl to target
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0 to 2000)

    val uiState: StateFlow<UiState<DietUiState>> = combine(
        profileRepository.observeProfile(),
        bodyMetricRepository.observeLatest()
    ) { profile, latestMetric ->
        if (profile == null) {
            UiState.Success(DietUiState(isLoading = false, hasProfile = false))
        } else {
            // 体重一致性：优先使用最新体重记录，无记录时回退问卷体重
            val effectiveWeight = latestMetric?.weightKg ?: profile.currentWeightKg
            val input = FitnessInput(
                gender = Gender.fromValue(profile.gender) ?: Gender.MALE,
                age = profile.age,
                heightCm = profile.heightCm,
                weightKg = effectiveWeight,
                weeklyExerciseDays = profile.weeklyExerciseDays,
                purpose = Purpose.fromValue(profile.purpose) ?: Purpose.HEALTH
                )
                UiState.Success(
                    DietUiState(
                        isLoading = false,
                        hasProfile = true,
                        purposeLabel = Purpose.fromValue(profile.purpose)?.label,
                        purposeName = profile.purpose,
                        weightKg = effectiveWeight,
                        metrics = FitnessCalculator.metrics(input)
                    )
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    val logState: StateFlow<DietLogState> = combine(
        todayLogsFlow,
        dietLogRepository.observeCustomFoods()
    ) { logs, customs ->
        DietLogState(
            isLoading = false,
            entries = logs,
            totals = DietTotals(
                calories = logs.sumOf { it.calories },
                protein = logs.sumOf { it.proteinG },
                carb = logs.sumOf { it.carbG },
                fat = logs.sumOf { it.fatG }
            ),
            customFoods = customs
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DietLogState())

    /** 记一笔饮食：营养值按克数快照计算后入库。 */
    fun logFood(selection: FoodSelection, mealType: String, grams: Double) {
        val ratio = grams / 100.0
        viewModelScope.launch {
            dietLogRepository.addLog(
                DietLogEntity(
                    date = AppClock.todayString(),
                    mealType = mealType,
                    foodName = selection.name,
                    grams = grams,
                    calories = (selection.kcalPer100 * ratio).roundToInt(),
                    proteinG = (selection.proteinPer100 * ratio * 10).roundToInt() / 10.0,
                    carbG = (selection.carbPer100 * ratio * 10).roundToInt() / 10.0,
                    fatG = (selection.fatPer100 * ratio * 10).roundToInt() / 10.0,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch { dietLogRepository.deleteLog(id) }
    }

    /** 新建/覆盖自定义食物。 */
    fun addCustomFood(
        name: String,
        kcal: Int,
        protein: Double,
        carb: Double,
        fat: Double,
        onSelected: (FoodSelection) -> Unit
    ) {
        viewModelScope.launch {
            dietLogRepository.upsertCustomFood(
                CustomFoodEntity(
                    name = name,
                    kcalPer100g = kcal,
                    proteinPer100g = protein,
                    carbPer100g = carb,
                    fatPer100g = fat,
                    createdAt = System.currentTimeMillis()
                )
            )
            onSelected(FoodSelection(name, kcal, protein, carb, fat))
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                DietViewModel(
                    profileRepository = app.container.profileRepository,
                    bodyMetricRepository = app.container.bodyMetricRepository,
                    dietLogRepository = app.container.dietLogRepository,
                    waterRepository = app.container.waterRepository
                )
            }
        }
    }
}

@Composable
internal fun DietScreen(
    onOpenFoodLibrary: () -> Unit = {},
    /** 记录饮食里点食材行 → 食材详情页（与食材库同一目的地） */
    onOpenFoodDetail: (String) -> Unit = {},
    /** 未建档时引导去填写身体数据 */
    onOpenProfileEdit: () -> Unit = {},
    initialTab: Int = 0,
    /** 从今日页餐位入口带来的一次性餐次提示（BREAKFAST/LUNCH/DINNER/SNACK） */
    mealHint: String = "",
    viewModel: DietViewModel = viewModel(factory = DietViewModel.factory())
) {
    val state by viewModel.uiState.collectAsState()
    val logState by viewModel.logState.collectAsState()
    var tab by remember { mutableIntStateOf(initialTab.coerceIn(0, 1)) }

    var logDialogVisible by rememberSaveable { mutableStateOf(false) }
    val water by viewModel.water.collectAsState()
    var toastMessage by remember { mutableStateOf<String?>(null) }
    // 今日页点餐位进来：直接带餐次弹出记录对话框；关闭后清掉提示
    var quickMeal by remember { mutableStateOf("") }
    val openLogPage: () -> Unit = { logDialogVisible = true }
    LaunchedEffect(mealHint) {
        if (mealHint.isNotBlank()) {
            quickMeal = mealHint
            logDialogVisible = true
        }
    }
    // 系统返回：整页「记录饮食」打开时先关闭它，而不是退出饮食页
    if (logDialogVisible) {
        androidx.activity.compose.BackHandler {
            logDialogVisible = false
            quickMeal = ""
        }
    }

    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("饮食", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            // 右上角「记录饮食」入口：与计划页「＋ 自建计划」同款按钮样式
            Button(
                onClick = { logDialogVisible = true },
                modifier = Modifier.height(40.dp),
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)
            ) {
                Text("＋ 记录饮食", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        TabRow(
            selectedTabIndex = tab,
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("今日记录") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("营养建议") })
            Tab(selected = tab == 2, onClick = {
                onOpenFoodLibrary()
                tab = 0
            }, text = { Text("食材库") })
        }
        Spacer(Modifier.height(12.dp))
        when (tab) {
            0 -> DietLogTab(
                logState = logState,
                metrics = (state as? UiState.Success)?.data?.metrics,
                onDelete = viewModel::deleteLog,
                customFoods = logState.customFoods
            )
            1 -> AdviceTab(
                    state,
                    logState,
                    logState.totals.calories,
                    viewModel,
                    onQuickLog = { food ->
                        viewModel.logFood(food, MealTypes.defaultByClock(), 100.0)
                        toastMessage = "已记入 " + food.name + " · 100 克"
                    },
                    onAdd = { logDialogVisible = true },
                    water = water,
                    onOpenProfileEdit = onOpenProfileEdit
                )
            2 -> {
                onOpenFoodLibrary()
                tab = 0
            }
            else -> DietLogTab(
                logState = logState,
                metrics = (state as? UiState.Success)?.data?.metrics,
                onDelete = viewModel::deleteLog,
                customFoods = logState.customFoods
            )
        }
    }

    if (logDialogVisible) {
        FoodLogPage(
            viewModel = viewModel,
            initialMeal = quickMeal,
            dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日")),
            recentLogs = logState.entries,
            customFoods = logState.customFoods,
            onAddCustom = { name, kcal, p, c, f, onSelected ->
                viewModel.addCustomFood(name, kcal, p, c, f, onSelected)
            },
            onOpenFoodDetail = onOpenFoodDetail,
            onSaveAll = { selections, gramsList, meal ->
                selections.forEachIndexed { index, sel ->
                    viewModel.logFood(sel, meal, gramsList[index])
                }
                viewModel.clearPickedFoods()
                logDialogVisible = false
                quickMeal = ""
            },
            onDismiss = {
                logDialogVisible = false
                quickMeal = ""
            }
        )
    }

    // 胶囊轻提示
    com.yuedong.fitness.ui.components.YueToastHost(
        message = toastMessage,
        onDismiss = { toastMessage = null },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 24.dp)
    )
    }
}

// ---------------- Tab 1：营养建议 ----------------

@Composable
private fun AdviceTab(
    state: UiState<DietUiState>,
    logState: DietLogState,
    intakeCalories: Int,
    viewModel: DietViewModel,
    onQuickLog: (FoodSelection) -> Unit,
    onAdd: () -> Unit,
    water: Pair<Int, Int> = 0 to 0,
    onOpenProfileEdit: () -> Unit = {}
) {
    when (val s = state) {
        UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(s.message, color = MaterialTheme.colorScheme.error)
        }
        is UiState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(s.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        is UiState.Success -> {
            val ui = s.data
            if (!ui.hasProfile || ui.metrics == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(72.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("先完成健康问卷", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "回答几个问题，就能算出专属于你的\n每日热量与营养目标",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onOpenProfileEdit) {
                            Text("30 秒填写身体数据")
                        }
                    }
                }
            } else {
                DietContent(
                    purposeLabel = ui.purposeLabel ?: "健康",
                    purposeName = ui.purposeName ?: "HEALTH",
                    weightKg = ui.weightKg,
                    metrics = ui.metrics,
                    intakeCalories = intakeCalories,
                    totals = logState.totals,
                    hasRecords = logState.entries.isNotEmpty(),
                    customFoods = logState.customFoods,
                    water = water,
                    onQuickLog = onQuickLog,
                    onAdd = onAdd
                )
            }
        }
    }
}

@Composable
private fun DietContent(
    purposeLabel: String,
    purposeName: String,
    weightKg: Double?,
    metrics: FitnessMetrics,
    intakeCalories: Int,
    totals: DietTotals,
    hasRecords: Boolean,
    customFoods: List<CustomFoodEntity>,
    onQuickLog: (FoodSelection) -> Unit,
    onAdd: () -> Unit,
    water: Pair<Int, Int> = 0 to 0
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.layout.Box(
                            Modifier
                                .size(30.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                androidx.compose.material.icons.Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "每日热量目标",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        PurposeBadge(purposeLabel)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "${metrics.targetCalories}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DiveMono,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            " 千卡/天",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "每日总消耗约 ${metrics.tdee} · 基础代谢 ${metrics.bmr} 千卡",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(6.dp))
                        HelpHintIcon(
                            title = "TDEE 与 BMR 是什么",
                            text = "TDEE（每日总消耗）：你一天总共消耗的热量；BMR（基础代谢）：完全静息时身体维持运转所需的消耗。两者按你的性别、年龄、身高体重与每周运动量估算。"
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
                    Spacer(Modifier.height(12.dp))
                    // 纯目标摘要（摄入进度环在「今日记录」，此处不重复）
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TargetNumber("蛋白质", "${metrics.proteinG}克", Modifier.weight(1f), DiveDataColors.protein)
                        TargetNumber("碳水", "${metrics.carbG}克", Modifier.weight(1f), DiveDataColors.carb)
                        TargetNumber("脂肪", "${metrics.fatG}克", Modifier.weight(1f), DiveDataColors.fat)
                        TargetNumber("饮水", "${metrics.waterTargetMl}毫升", Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item {
            // 一日三餐参考：比例 3:4:3 分配目标热量，搭配按健身目的定制
            val breakfast = (metrics.targetCalories * 0.3).roundToInt()
            val lunch = (metrics.targetCalories * 0.4).roundToInt()
            val dinner = metrics.targetCalories - breakfast - lunch
            val meals = DietAdvisor.mealsFor(purposeName)
            Column {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.layout.Box(
                        Modifier
                            .size(30.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("一日三餐参考", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        meals.forEachIndexed { index, meal ->
                            val kcal = when (index) {
                                0 -> breakfast
                                1 -> lunch
                                else -> dinner
                            }
                            val percent = when (index) {
                                0 -> "约 30%"
                                1 -> "约 40%"
                                else -> "约 30%"
                            }
                            // 主题图标替代 emoji（🌅☀️🌙）：随明暗主题着色，气质统一
                            val mealIcon = when (index) {
                                0 -> Icons.Filled.WbTwilight
                                1 -> Icons.Filled.WbSunny
                                else -> Icons.Filled.Nightlight
                            }
                            MealRow(mealIcon, "${meal.name} · $percent", kcal, meal.foods)
                            if (index != meals.lastIndex) {
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
        item {
            DietSuggestionCard(totals = totals, metrics = metrics, customFoods = customFoods, onQuickLog = onQuickLog)
        }
    }
}

// ---------------- Tab 2：今日记录 ----------------
// ---------------- Tab 2 内嵌组件：热量环卡 ----------------

@Composable
private fun CalorieRingCard(logState: DietLogState, metrics: FitnessMetrics?) {
    // 常显（与今日页摄入卡同一待遇）：无档案时目标以「—」占位，卡片不再整张消失
    val intake = logState.totals.calories
    val target = metrics?.targetCalories
    val overTarget = target != null && intake > target
    val ringColor = if (overTarget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    com.yuedong.fitness.ui.theme.DiveGradients.heroGlow(diveDark()),
                    MaterialTheme.shapes.large
                ),
            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 大渐变环（图一）：缺口 + 渐变弧 + 环心大数字
                Box(contentAlignment = Alignment.Center) {
                    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                    val overColor = MaterialTheme.colorScheme.error
                    Canvas(Modifier.size(168.dp)) {
                        val stroke = 12.dp.toPx()
                        val gapAngle = 40f
                        val arcStart = -90f + gapAngle / 2
                        val arcSweep = 360f - gapAngle
                        drawArc(
                            color = trackColor,
                            startAngle = arcStart, sweepAngle = arcSweep, useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        val ratio = target?.takeIf { it > 0 }?.let { intake.toFloat() / it }?.coerceIn(0f, 1f) ?: 0f
                        val baseSweep = arcSweep * ratio.coerceIn(0f, 1f)
                        if (ratio > 0f) {
                            drawArc(
                                brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                                    0f to ringColor.copy(alpha = 0.10f),
                                    0.5f to ringColor.copy(alpha = 0.45f),
                                    1f to ringColor
                                ),
                                startAngle = arcStart,
                                sweepAngle = baseSweep,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        if (target != null && ratio > 1f) {
                            val overFrac = (ratio - 1f).coerceIn(0f, 0.25f)
                            drawArc(
                                color = overColor,
                                startAngle = arcStart + baseSweep,
                                sweepAngle = arcSweep * overFrac,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val intakeDisplay = rememberCountUpValue(intake)
                        Text(
                            "今日已摄入",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                intakeDisplay.value.roundToInt().toString(),
                                style = DiveNumeric.display,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                " 千卡",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                // 状态胶囊（参考图：徽章 + 数字，全圆角）
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (overTarget) MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp, end = 14.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        if (target != null) {
                            Text(
                                if (overTarget) "已超出" else "还可吃",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (overTarget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        (if (overTarget) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary).copy(alpha = 0.14f),
                                        RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            target?.let { t ->
                                (if (overTarget) intake - t else t - intake).toString() + " / 目标 " + t + " 千卡"
                            } ?: "目标 — 千卡",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DiveMono,
                            color = when {
                                target == null -> MaterialTheme.colorScheme.onSurfaceVariant
                                overTarget -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                // 三大营养素并入环卡（图二）：行式 圆点+名称+进度条+数值
                MacroInline("蛋白质", logState.totals.protein.roundToInt(), metrics?.proteinG, DiveDataColors.protein)
                Spacer(Modifier.height(8.dp))
                MacroInline("碳水", logState.totals.carb.roundToInt(), metrics?.carbG, DiveDataColors.carb)
                Spacer(Modifier.height(8.dp))
                MacroInline("脂肪", logState.totals.fat.roundToInt(), metrics?.fatG, DiveDataColors.fat)
            }
        }
    if (metrics != null && logState.totals.calories > metrics.tdee) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.10f), MaterialTheme.shapes.medium)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
                androidx.compose.material.icons.Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "已超过每日总消耗 " + metrics.tdee + " 千卡",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}


@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
private fun DietLogTab(
    logState: DietLogState,
    metrics: FitnessMetrics?,
    onDelete: (Long) -> Unit,
    customFoods: List<CustomFoodEntity>
) {
    var menuEntryId by remember { mutableStateOf<Long?>(null) }
    // 点击条目查看详情
    var detailEntryId by remember { mutableStateOf<Long?>(null) }

    if (logState.entries.isEmpty()) {
        // 空态：热量卡 + 填满剩余高度的引导卡，页面视觉占满、底部留白一致
        Column(Modifier.fillMaxSize()) {
            CalorieRingCard(logState = logState, metrics = metrics)
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(com.yuedong.fitness.R.drawable.mascot_shiba),
                        contentDescription = null,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "今天还没有记录饮食\n点右上角「记录饮食」开始",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    } else {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
    ) {
        item { CalorieRingCard(logState = logState, metrics = metrics) }
        // ⑥ 记录列表（按餐次分组；行带营养概要；左滑滑过即删）
        if (logState.entries.isNotEmpty()) {
            MealTypes.ordered.forEach { meal ->
                val mealEntries = logState.entries.filter { it.mealType == meal }
                if (mealEntries.isNotEmpty()) {
                    item(key = "header_$meal") {
                        Text(
                            MealTypes.label(meal) + " · 共 " + mealEntries.sumOf { it.calories } + " 千卡",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    itemsIndexed(mealEntries, key = { _, entry -> entry.id }) { _, entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { detailEntryId = entry.id },
                                onLongClick = { menuEntryId = entry.id }
                            ),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val img = foodImages[entry.foodName]
                                androidx.compose.foundation.layout.Box(
                                    Modifier
                                        .size(44.dp)
                                        .clip(MaterialTheme.shapes.small),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (img != null) {
                                        androidx.compose.foundation.Image(
                                            painter = androidx.compose.ui.res.painterResource(img),
                                            contentDescription = entry.foodName,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        androidx.compose.foundation.layout.Box(
                                            Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            androidx.compose.material3.Icon(
                                                Icons.Filled.Restaurant,
                                                contentDescription = entry.foodName,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        entry.foodName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.size(3.dp))
                                    Text(
                                        entry.grams.roundToInt().toString() + " 克 · 蛋白 " + fmtD(entry.proteinG) + " · 碳水 " + fmtD(entry.carbG) + " · 脂肪 " + fmtD(entry.fatG),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                // 热量数字固定列宽：位数不同也从同一位置起笔
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        entry.calories.toString(),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = DiveMono,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.width(56.dp)
                                    )
                                    Text(
                                        "千卡",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    menuEntryId?.let { entryId ->
        val entry = logState.entries.firstOrNull { it.id == entryId }
        ListActionSheet(
            visible = true,
            title = entry?.foodName,
            actions = listOf(
                com.yuedong.fitness.ui.components.ListAction(
                    androidx.compose.material.icons.Icons.Filled.Delete,
                    "删除这条记录",
                    MaterialTheme.colorScheme.error
                ) { onDelete(entryId) }
            ),
            onDismiss = { menuEntryId = null }
        )
    }

    // 点击条目：查看这笔饮食记录的详情
    detailEntryId?.let { entryId ->
        val entry = logState.entries.firstOrNull { it.id == entryId }
        if (entry != null) {
            YueDialog(
                onDismissRequest = { detailEntryId = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val img = foodImages[entry.foodName]
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(MaterialTheme.shapes.small),
                            contentAlignment = Alignment.Center
                        ) {
                            if (img != null) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(img),
                                    contentDescription = entry.foodName,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                androidx.compose.foundation.layout.Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.Icon(
                                        Icons.Filled.Restaurant,
                                        contentDescription = entry.foodName,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                entry.foodName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                MealTypes.label(entry.mealType) + " · " + entry.calories + " 千卡",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DiveMono,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                text = {
                    Column {
                        EntryDetailLine("克数", entry.grams.roundToInt().toString() + " 克")
                        EntryDetailLine("热量", entry.calories.toString() + " 千卡")
                        EntryDetailLine("蛋白质", fmtD(entry.proteinG) + " 克")
                        EntryDetailLine("碳水", fmtD(entry.carbG) + " 克")
                        EntryDetailLine("脂肪", fmtD(entry.fatG) + " 克")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { detailEntryId = null }) { Text("关闭") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        onDelete(entry.id)
                        detailEntryId = null
                    }) {
                        Text("删除这条记录", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    }
}
    }

/** 添加食物弹层的营养明细行：彩点 + 名称 + 右侧数值 */
@Composable
private fun SheetNutrientRow(label: String, value: String, dotColor: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(dotColor, CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = DiveMono
        )
    }
}

/** 条目详情行：左标签 + 右数值 */
@Composable
private fun EntryDetailLine(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = DiveMono
        )
    }
}

// ---------------- 记一笔饮食弹窗 ----------------

/** 已选清单里的一项：食物营养选择 + 用户选定的克数 */
internal data class PickedFood(val sel: FoodSelection, val grams: Double)


/**
 * 记录饮食（整页）：顶部返回 + 「9月9日 午餐 ▾」餐次下拉，
 * 搜索 + 最近添加/常见/自定义标签（默认最近添加）+ 左侧食物分类栏 + 右侧列表，
 * 底部完成条。点食物行选克数后加入清单（已选再点移除），已选项排在清单下方，
 * 完成按各自克数批量记入。
 */
@Composable
@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)
internal fun FoodLogPage(
    viewModel: DietViewModel,
    initialMeal: String,
    /** 顶部日期文案（补记过去某天时传那天，默认今天） */
    dateLabel: String,
    recentLogs: List<com.yuedong.fitness.data.db.entity.DietLogEntity>,
    customFoods: List<CustomFoodEntity>,
    onAddCustom: (name: String, kcal: Int, protein: Double, carb: Double, fat: Double, onSelected: (FoodSelection) -> Unit) -> Unit,
    /** 批量保存：所选食物（默认 100 克/项）+ 餐次 */
    onOpenFoodDetail: (String) -> Unit = {},
    onSaveAll: (List<FoodSelection>, List<Double>, String) -> Unit,
    onDismiss: () -> Unit
) {
    var meal by remember {
        mutableStateOf(
            viewModel.pickedMeal
                ?: (if (initialMeal in MealTypes.ordered) initialMeal else MealTypes.defaultByClock())
        )
    }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { viewModel.pickedMeal = meal }
    }
    var mealMenuExpanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var listMode by remember { mutableStateOf("最近添加") }
    var showCustomForm by remember { mutableStateOf(false) }
    /** 已选清单弹层 */
    var showPickedSheet by remember { mutableStateOf(false) }
    /** 待选克数的食物（弹出克数选择） */
    var pendingPick by remember { mutableStateOf<FoodSelection?>(null) }
    var grams by remember { mutableDoubleStateOf(100.0) }
    /** 已选食物：名称 → 营养选择与克数（按添加顺序；换分类/搜索后仍保留） */
    // 已选清单托管在 ViewModel：点食材行跳出本页看详情，回来不丢
    val pickedFoods = viewModel.pickedFoods

    val flatFoods = FoodCatalog.grouped.flatMap { it.second }
    val byName = flatFoods.associateBy { it.name }
    data class Pick(
        val name: String,
        val sub: String,
        val img: Int?,
        val sel: FoodSelection
    )
    val picks: List<Pick> = when {
        query.isNotBlank() -> {
            buildList {
                customFoods.forEach { c ->
                    add(
                        Pick(
                            c.name + "（自定义）", "${c.kcalPer100g}千卡/100克",
                            foodImages[c.name],
                            FoodSelection(c.name, c.kcalPer100g, c.proteinPer100g, c.carbPer100g, c.fatPer100g)
                        )
                    )
                }
                flatFoods.forEach { f ->
                    if (f.name.contains(query.trim(), ignoreCase = true)) {
                        add(
                            Pick(
                                f.name,
                                "${f.kcal}千卡/100${f.unit}",
                                foodImages[f.name],
                                FoodSelection(f.name, f.kcal, f.protein, f.carb, f.fat)
                            )
                        )
                    }
                }
            }
        }
        listMode == "最近添加" -> {
            // 按添加时间排序，新的在前；同名食物只留最新一次
            recentLogs.sortedByDescending { it.id }
                .distinctBy { it.foodName }
                .take(15)
                .mapNotNull { e ->
                    val g = e.grams.takeIf { it > 0.0 } ?: 100.0
                    val f = byName[e.foodName]
                    Pick(
                        e.foodName,
                        "${f?.kcal ?: (e.calories / g * 100).roundToInt()}千卡/100克",
                        foodImages[e.foodName],
                        f?.let { FoodSelection(it.name, it.kcal, it.protein, it.carb, it.fat) }
                            ?: FoodSelection(
                                e.foodName,
                                (e.calories / g * 100).roundToInt(),
                                e.proteinG / g * 100.0,
                                e.carbG / g * 100.0,
                                e.fatG / g * 100.0
                            )
                    )
                }
        }
        listMode == "自定义" -> {
            customFoods.map { c ->
                Pick(
                    c.name + "（自定义）",
                    "${c.kcalPer100g}千卡/100克",
                    null,
                    FoodSelection(c.name, c.kcalPer100g, c.proteinPer100g, c.carbPer100g, c.fatPer100g)
                )
            }
        }
        else -> emptyList() // 「所有食材」标签走分区联动视图，不经 picks
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 顶栏：返回 + 日期餐次下拉
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                onClick = { mealMenuExpanded = true },
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$dateLabel " + MealTypes.label(meal),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        " ▾",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            DropdownMenu(
                expanded = mealMenuExpanded,
                onDismissRequest = { mealMenuExpanded = false },
                modifier = Modifier.align(Alignment.Center)
            ) {
                MealTypes.ordered.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(MealTypes.label(type)) },
                        onClick = {
                            meal = type
                            mealMenuExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        // 搜索
        SearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "请输入食物名称",
            modifier = Modifier
                .padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))
        // 顶部标签：所有食材 / 最近添加 / 自定义（默认停在最近添加，与添加动作页顺序一致）
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TopFoodTab("所有食材", listMode == "所有食材") { listMode = "所有食材" }
            TopFoodTab("最近添加", listMode == "最近添加") { listMode = "最近添加" }
            TopFoodTab("自定义", listMode == "自定义") { listMode = "自定义" }
        }
        Spacer(Modifier.height(4.dp))
        // 主体
        Row(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (query.isNotBlank() || listMode != "所有食材") {
                // 搜索 / 最近添加 / 自定义：平铺列表
                Box(Modifier.weight(1f).fillMaxHeight()) {
                if (listMode == "自定义") {
                    // 自定义：标题 + 添加 + 列表（照参考 App）
                    Column(Modifier.fillMaxHeight()) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "自定义食物",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clickable { showCustomForm = true }
                            ) {
                                Text("＋", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(
                                    " 添加",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        if (picks.isEmpty()) {
                            EmptyState("暂无自定义食物\n点右上角「添加」建一个")
                        } else {
                            LazyColumn(Modifier.fillMaxHeight()) {
                                items(picks.size) { i ->
                                    val item = picks[i]
                                    val already = pickedFoods.containsKey(item.sel.name)
                                    FoodPickRow(
                                        item.img, item.name, item.sub,
                                        picked = already,
                                        onToggle = {
                                            if (already) pickedFoods.remove(item.sel.name)
                                            else {
                                                pendingPick = item.sel
                                                grams = 100.0
                                            }
                                        },
                                        onOpenDetail = { onOpenFoodDetail(item.sel.name) }
                                    )
                                }
                            }
                        }
                    }
                } else if (picks.isEmpty()) {
                    EmptyState(
                        if (listMode == "最近添加") "还没有记录，去「所有食材」挑一个吧" else "该分类暂无食物",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(Modifier.fillMaxHeight()) {
                        items(picks.size) { i ->
                            val item = picks[i]
                            val already = pickedFoods.containsKey(item.sel.name)
                            FoodPickRow(
                                item.img, item.name, item.sub,
                                picked = already,
                                onToggle = {
                                    if (already) pickedFoods.remove(item.sel.name)
                                    else {
                                        pendingPick = item.sel
                                        grams = 100.0
                                    }
                                },
                                onOpenDetail = { onOpenFoodDetail(item.sel.name) }
                            )
                        }
                    }
                }
                }
            } else {
                // 所有食材：左侧标签与右侧滚动双向联动（与添加动作页一致）
                val listState = rememberLazyListState()
                val sectionStarts = remember {
                    var acc = 0
                    FoodCatalog.grouped.map { (cat, foods) -> cat to acc.also { acc += 1 + foods.size } }
                }
                val currentLabel by remember {
                    derivedStateOf {
                        val i = listState.firstVisibleItemIndex
                        sectionStarts.lastOrNull { i >= it.second }?.first
                            ?: FoodCatalog.grouped.firstOrNull()?.first.orEmpty()
                    }
                }
                val scope = rememberCoroutineScope()
                Column(
                    Modifier
                        .width(104.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    FoodCatalog.grouped.forEach { (cat, _) ->
                        val isSelected = currentLabel == cat
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    scope.launch {
                                        listState.animateScrollToItem(
                                            sectionStarts.first { it.first == cat }.second
                                        )
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                cat,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                LazyColumn(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    state = listState,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    FoodCatalog.grouped.forEach { (cat, foods) ->
                        item(key = "h_$cat") {
                            Text(
                                cat,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }
                        foods.forEach { f ->
                            item(key = "f_${f.name}") {
                                val already = pickedFoods.containsKey(f.name)
                                FoodPickRow(
                                    foodImages[f.name],
                                    f.name,
                                    "${f.kcal}千卡/100${f.unit}",
                                    picked = already,
                                    onToggle = {
                                        if (already) pickedFoods.remove(f.name)
                                        else {
                                            pendingPick = FoodSelection(f.name, f.kcal, f.protein, f.carb, f.fat)
                                            grams = 100.0
                                        }
                                    },
                                    onOpenDetail = { onOpenFoodDetail(f.name) }
                                )
                            }
                        }
                    }
                }
            }
        }
        // 底部完成条：已选角标（点开清单）+ 餐次切换 + 完成（参考图样式）
        Surface(shadowElevation = 6.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 已选角标按钮：点击展开所有正在添加的食物
                Box {
                    Surface(
                        onClick = { if (pickedFoods.isNotEmpty()) showPickedSheet = true },
                        shape = MaterialTheme.shapes.medium,
                        color = if (pickedFoods.isEmpty()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Box(Modifier.padding(9.dp)) {
                            Icon(
                                Icons.Filled.Restaurant,
                                contentDescription = "查看已选食物",
                                tint = if (pickedFoods.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    if (pickedFoods.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                        ) {
                            Text(
                                pickedFoods.size.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        val sels = pickedFoods.values.map { it.sel }
                        val gramList = pickedFoods.values.map { it.grams }
                        onSaveAll(sels, gramList, meal)
                    },
                    enabled = pickedFoods.isNotEmpty(),
                    modifier = Modifier.height(48.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 30.dp)
                ) {
                    Text("完成", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 已选清单弹层：点角标展开所有正在添加的食物，点垃圾桶移除
    if (showPickedSheet) {
        val totalKcal = pickedFoods.values.sumOf { (it.sel.kcalPer100 * it.grams / 100.0).roundToInt() }
        ModalBottomSheet(
            onDismissRequest = { showPickedSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            // 容器规范与 YueDialog 壳一致（避免 M3 默认观感与悦动弹层并存）
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    "共 " + pickedFoods.size + " 项食物，总计 " + totalKcal + " 千卡",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    pickedFoods.forEach { (name, picked) ->
                        item(key = name) {
                            val kcal = (picked.sel.kcalPer100 * picked.grams / 100.0).roundToInt()
                            val img = foodImages[name]
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (img != null) {
                                        androidx.compose.foundation.Image(
                                            painter = androidx.compose.ui.res.painterResource(img),
                                            contentDescription = name,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        androidx.compose.material3.Icon(
                                            Icons.Filled.Restaurant,
                                            contentDescription = name,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        picked.grams.roundToInt().toString() + " 克 · " + kcal + " 千卡",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = DiveMono,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { pickedFoods.remove(name) }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "移除 " + name,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                if (pickedFoods.isEmpty()) {
                    EmptyState("已全部移除")
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    // 添加食物弹层：选克数 + 各营养含量（按所选克数实时换算，参考图样式）
    pendingPick?.let { food ->
        ModalBottomSheet(
            onDismissRequest = { pendingPick = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 660.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // 标题行：添加食物 + 关闭
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "添加食物",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { pendingPick = null }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                // 食物信息卡：名称 + 热量/100克
                SheetCard {
                    Text(food.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${food.kcalPer100}千卡/100克",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(10.dp))
                // 克数调整：− ［100 克］＋，步进 50 克（50~1000）
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { grams = (grams - 50.0).coerceAtLeast(50.0) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            "−",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                    Spacer(Modifier.width(24.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            String.format("%.0f", grams),
                            style = DiveNumeric.display,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            " 克",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                    Spacer(Modifier.width(24.dp))
                    Surface(
                        onClick = { grams = (grams + 50.0).coerceAtMost(1000.0) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "＋",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                // 营养明细（按所选克数实时换算，保留一位小数）
                val ratio = grams / 100.0
                SheetNutrientRow("食物热量", "${(food.kcalPer100 * ratio).roundToInt()}千卡", MaterialTheme.colorScheme.primary)
                SheetNutrientRow("碳水化合物", String.format("%.1f", food.carbPer100 * ratio) + "g", DiveDataColors.carb)
                SheetNutrientRow("蛋白质", String.format("%.1f", food.proteinPer100 * ratio) + "g", DiveDataColors.protein)
                SheetNutrientRow("脂肪", String.format("%.1f", food.fatPer100 * ratio) + "g", DiveDataColors.fat)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        pickedFoods[food.name] = PickedFood(food, grams)
                        pendingPick = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("确认", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    // 新建自定义食物
    if (showCustomForm) {
        CustomFoodDialog(
            onSave = { name, kcal, p, c, f, description ->
                onAddCustom(name, kcal, p, c, f) { sel ->
                    // 新建成功后直接按 100 克加入已选清单
                    if (!pickedFoods.containsKey(sel.name)) pickedFoods[sel.name] = PickedFood(sel, 100.0)
                }
                showCustomForm = false
            },
            onDismiss = { showCustomForm = false }
        )
    }
}

@Composable
private fun MealRow(icon: androidx.compose.ui.graphics.vector.ImageVector, name: String, kcal: Int, pairing: String) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(6.dp))
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            // 与标题同字号，保证左右两列视觉重量一致、整齐
            Text(
                "$kcal 千卡",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = DiveMono,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            pairing,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 目标数字摘要格：大数字 + 标签（营养建议页专用，无进度语义） */
@Composable
private fun TargetNumber(label: String, value: String, modifier: Modifier = Modifier, color: Color) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = DiveMono,
            color = color
        )
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PurposeBadge(text: String) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun TopFoodTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(3.dp))
        Box(
            Modifier
                .width(26.dp)
                .height(3.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    RoundedCornerShape(50)
                )
        )
    }
}

/** 食物行：食材库图片（无图用统一占位图标）+ 名称与营养小字，点击即加入清单（已选再点移除） */
@Composable
private fun FoodPickRow(
    img: Int?,
    name: String,
    sub: String,
    picked: Boolean = false,
    onToggle: () -> Unit,
    onOpenDetail: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenDetail)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (img != null) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(img),
                        contentDescription = name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                    )
                } else {
                    androidx.compose.material3.Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    sub,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                onClick = onToggle,
                shape = CircleShape,
                color = if (picked) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Text(
                    if (picked) "✓" else "＋",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (picked) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}


/** 根据今天的记录：一句话建议 + 快捷补记高蛋白食物 */
@Composable
private fun DietSuggestionCard(
    totals: DietTotals,
    metrics: FitnessMetrics?,
    customFoods: List<CustomFoodEntity>,
    onQuickLog: (FoodSelection) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("根据今天的记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (metrics == null) {
                Text(
                    "完成健康问卷后，这里会给出更贴合你的补记建议",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val proteinLeft = (metrics.proteinG - totals.protein).roundToInt()
                val carbLeft = (metrics.carbG - totals.carb).roundToInt()
                val fatLeft = (metrics.fatG - totals.fat).roundToInt()
                val kcalLeft = metrics.targetCalories - totals.calories
                // 与快捷补记推荐器同口径：找缺口最大的营养素来描述（占比 ≥ 一半即主导）
                val pGap = (metrics.proteinG - totals.protein).coerceAtLeast(0.0)
                val cGap = (metrics.carbG - totals.carb).coerceAtLeast(0.0)
                val fGap = (metrics.fatG - totals.fat).coerceAtLeast(0.0)
                val gapSum = pGap + cGap + fGap
                Text(
                    buildString {
                        append("已摄入 ${totals.calories} 千卡")
                        when {
                            kcalLeft < 0 -> append("，已超过目标 ${-kcalLeft} 千卡，今天就别再加餐啦")
                            gapSum > 0 && pGap / gapSum >= 0.5 -> append("，蛋白质还差约 $proteinLeft 克，来点高蛋白食物吧")
                            carbLeft > 0 && cGap / gapSum >= 0.5 -> append("，碳水还差约 $carbLeft 克，补点优质主食吧")
                            fatLeft > 0 && fGap / gapSum >= 0.5 -> append("，脂肪还差约 $fatLeft 克，来点坚果或橄榄油吧")
                            else -> append("，热量还剩 $kcalLeft 千卡，节奏不错")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "快捷补记",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // 按今日缺口智能推荐：哪个营养素缺得多补哪个，已超热量只推低卡
                val quick = QuickAddSuggester.recommend(
                    QuickAddSuggester.Intake(
                        proteinG = totals.protein,
                        carbG = totals.carb,
                        fatG = totals.fat,
                        kcal = totals.calories
                    ),
                    QuickAddSuggester.Goal(
                        proteinG = metrics.proteinG.toDouble(),
                        carbG = metrics.carbG.toDouble(),
                        fatG = metrics.fatG.toDouble(),
                        kcal = metrics.targetCalories
                    ),
                    FoodCatalog.grouped.flatMap { it.second }
                )
                // 图片四列卡（v2 设计图「快捷补记」样式）
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quick.take(4).forEach { f ->
                        Column(
                            Modifier
                                .weight(1f)
                                .clickable {
                                    onQuickLog(FoodSelection(f.name, f.kcal, f.protein, f.carb, f.fat))
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val img = foodImages[f.name]
                            androidx.compose.foundation.layout.Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (img != null) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(img),
                                        contentDescription = f.name,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    androidx.compose.material3.Icon(
                                        Icons.Filled.Restaurant,
                                        contentDescription = f.name,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                f.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}


/** 去小数尾零：15.0 → 15 */
private fun fmtD(v: Double): String = if (v % 1.0 == 0.0) "${v.toInt()}" else "$v"

/** 环卡内宏量行（图二样式）：彩点名称 + 进度条 + 克数/目标 */
@Composable
private fun MacroInline(name: String, value: Int, target: Int?, color: Color) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(color, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(
            name,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(44.dp)
        )
        LinearProgressIndicator(
            progress = { if ((target ?: 0) > 0) (value.toFloat() / target!!).coerceIn(0f, 1f) else 0f },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            drawStopIndicator = {}
        )
        Spacer(Modifier.width(10.dp))
        Row {
            Text(
                value.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = DiveMono,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.width(36.dp)
            )
            Text(
                " / " + (target?.toString() ?: "—") + " 克",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = DiveMono,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
