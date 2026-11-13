package com.yuedong.fitness.ui.records

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.BodyMetricEntity
import com.yuedong.fitness.data.db.entity.CheckInRecordEntity
import com.yuedong.fitness.data.db.entity.DietLogEntity
import com.yuedong.fitness.data.db.entity.PlanExerciseEntity
import com.yuedong.fitness.domain.model.Gender
import com.yuedong.fitness.domain.model.Purpose
import com.yuedong.fitness.domain.model.TargetProgressUi
import com.yuedong.fitness.domain.validation.InputValidator
import com.yuedong.fitness.domain.validation.ValidationResult
import com.yuedong.fitness.ui.components.EmptyState
import com.yuedong.fitness.ui.components.DiaryDialog
import com.yuedong.fitness.ui.components.WeightLogDialog
import com.yuedong.fitness.ui.components.YueDialog
import com.yuedong.fitness.ui.diet.FoodLogPage
import com.yuedong.fitness.ui.theme.DiveGradients
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.ui.theme.diveDark
import com.yuedong.fitness.util.AppClock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.yuedong.fitness.data.repository.CheckInRepository
import com.yuedong.fitness.data.repository.ExerciseProgressRepository
import com.yuedong.fitness.data.repository.PlanRepository

/** 当日详情:打卡/补打卡 + 当日训练 + 饮食 + 日记 + 体重(均可补录)。 */
data class DayDetailUiState(
    val dateIso: String,
    val checkedIn: Boolean = false,
    val planName: String? = null,
    val exercises: List<Pair<PlanExerciseEntity, Boolean>> = emptyList(),
    val doneCount: Int = 0,
    val totalCount: Int = 0
)

class DayDetailViewModel(
    dateString: String,
    private val checkInRepository: CheckInRepository,
    private val planRepository: PlanRepository,
    private val progressRepository: ExerciseProgressRepository,
    private val dietLogRepository: com.yuedong.fitness.data.repository.DietLogRepository,
    private val bodyMetricRepository: com.yuedong.fitness.data.repository.BodyMetricRepository,
    private val diaryRepository: com.yuedong.fitness.data.repository.DiaryRepository,
    private val dayExtraExerciseRepository: com.yuedong.fitness.data.repository.DayExtraExerciseRepository,
    private val settings: com.yuedong.fitness.data.settings.SettingsDataStore
) : ViewModel() {

    private val date: LocalDate = runCatching { LocalDate.parse(dateString) }.getOrElse { AppClock.today() }
    val dateIso: String = date.toString()

    /** 补打卡:为过去漏掉的日子补一条记录(当前激活计划名下,注明「补打卡」) */
    fun makeUpCheckIn() {
        viewModelScope.launch {
            val planId = planRepository.observeActivePlanId().first()
            checkInRepository.save(
                CheckInRecordEntity(
                    date = dateIso,
                    planId = planId ?: 0L,
                    note = "补打卡",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    /** 当日额外动作(补录) */
    val extraExercises = dayExtraExerciseRepository.observeByDate(dateIso).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    /** 当日日记 */
    val diary = diaryRepository.observeByDate(dateIso).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    /** 当日饮食记录 */
    val dietLogs = dietLogRepository.observeLogsByDate(dateIso).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    /** 是否可补打卡:过去未打卡,且不早于「第一天」(首次使用日期,取不到时回退为最早一条打卡日) */
    val canMakeUp: kotlinx.coroutines.flow.StateFlow<Boolean> = combine(
        settings.observeUsageStartDate(),
        checkInRepository.observeAll()
    ) { start, records ->
        val stored = start?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val earliest = records.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }.minOrNull()
        val firstDay = listOfNotNull(stored, earliest).minOrNull() ?: return@combine true
        !date.isBefore(firstDay)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /** 当日体重记录(有记录才显示) */
    val weight = bodyMetricRepository.observeAll().map { all ->
        all.filter { it.date == dateIso }.maxByOrNull { it.updatedAt }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    fun saveDiary(mood: Int, content: String) {
        viewModelScope.launch {
            diaryRepository.save(
                com.yuedong.fitness.data.db.entity.DiaryEntryEntity(
                    date = dateIso,
                    mood = mood,
                    content = content,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun addExtraExercise(name: String, setsText: String) {
        viewModelScope.launch {
            dayExtraExerciseRepository.add(dateIso, name, setsText)
        }
    }

    fun deleteExtraExercise(id: Long) {
        viewModelScope.launch {
            dayExtraExerciseRepository.delete(id)
        }
    }

    fun saveWeight(weightKg: Double) {
        viewModelScope.launch {
            bodyMetricRepository.save(
                BodyMetricEntity(
                    date = dateIso,
                    weightKg = weightKg,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun addFood(
        date: String,
        mealType: String,
        foodName: String,
        grams: Double,
        calories: Int,
        proteinG: Double,
        carbG: Double,
        fatG: Double
    ) {
        viewModelScope.launch {
            dietLogRepository.addLog(
                DietLogEntity(
                    date = date,
                    mealType = mealType,
                    foodName = foodName,
                    grams = grams,
                    calories = calories,
                    proteinG = proteinG,
                    carbG = carbG,
                    fatG = fatG,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    /** 打卡 → 计划名/计划动作 → 完成态 逐层组合 */
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: kotlinx.coroutines.flow.StateFlow<DayDetailUiState> =
        checkInRepository.observeByDate(dateIso).flatMapLatest { checkIn ->
            progressRepository.observePlanIdForDate(dateIso).flatMapLatest { progressPlanId ->
                val planId = progressPlanId ?: checkIn?.planId ?: 0L
                combine(
                    planRepository.observePlan(planId),
                    planRepository.observePlanDays(planId),
                    planRepository.observeExercisesOfPlan(planId),
                    progressRepository.observeByDate(dateIso),
                    settings.observeActivePlanStartedAt()
                ) { plan, planDays, planExercises, progress, startedAt ->
                    val doneIds = progress.filter { it.done }.map { it.planExerciseId }.toSet()
                    val sortedDays = planDays.sortedBy { it.orderIndex }
                    val startedDate = startedAt?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    // 只有当该日期落在计划开始日之后,才展示当日应做的计划动作;
                    // 更早的补卡日不自动铺计划动作(用户可用「添加动作」手动补记当天实际做的内容)
                    val showPlanExercises = startedDate != null && !date.isBefore(startedDate)
                    val elapsed = startedDate?.let { ChronoUnit.DAYS.between(it, date) } ?: 0L
                    val index = if (sortedDays.isEmpty()) 0
                    else ((elapsed % sortedDays.size).toInt() + sortedDays.size) % sortedDays.size
                    val dayExercises = if (!showPlanExercises || sortedDays.isEmpty()) emptyList()
                    else planExercises.filter { it.planDayId == sortedDays[index].id }
                    val exercises = dayExercises.map { it to (it.id in doneIds) }
                    DayDetailUiState(
                        dateIso = dateIso,
                        checkedIn = checkIn != null,
                        planName = plan?.name,
                        exercises = exercises,
                        doneCount = exercises.count { it.second },
                        totalCount = exercises.size
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DayDetailUiState(dateIso = dateIso)
        )

    companion object {
        fun factory(dateString: String): androidx.lifecycle.ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as YueDongApplication
                DayDetailViewModel(
                    dateString = dateString,
                    checkInRepository = app.container.checkInRepository,
                    planRepository = app.container.planRepository,
                    progressRepository = app.container.exerciseProgressRepository,
                    dietLogRepository = app.container.dietLogRepository,
                    bodyMetricRepository = app.container.bodyMetricRepository,
                    diaryRepository = app.container.diaryRepository,
                    dayExtraExerciseRepository = app.container.dayExtraExerciseRepository,
                    settings = app.container.settings
                )
            }
        }
    }
}

@Composable
fun DayDetailScreen(
    date: String?,
    onBack: () -> Unit,
    onOpenExercisePicker: () -> Unit = {},
    viewModel: DayDetailViewModel = viewModel(
        key = "day_detail_$date",
        factory = DayDetailViewModel.factory(date ?: AppClock.todayString())
    )
) {
    val state by viewModel.state.collectAsState()
    val canMakeUp by viewModel.canMakeUp.collectAsState()
    val dietLogs by viewModel.dietLogs.collectAsState()
    val extraExercises by viewModel.extraExercises.collectAsState()
    val diary by viewModel.diary.collectAsState()
    val weightEntry by viewModel.weight.collectAsState()
    val parsedDate = runCatching { LocalDate.parse(viewModel.dateIso) }.getOrElse { AppClock.today() }
    val dateText = parsedDate.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA))
    val dateLabel = if (parsedDate == AppClock.today()) "今天" else dateText
    var showFoodLog by remember { mutableStateOf(false) }
    var showDiary by remember { mutableStateOf(false) }
    var showWeight by remember { mutableStateOf(false) }
    var showAddExercise by remember { mutableStateOf(false) }

    // 从动作库选择返回:把选中的动作补录为当日额外动作
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME &&
                com.yuedong.fitness.ui.plan.ExercisePickBus.source == com.yuedong.fitness.ui.plan.ExercisePickBus.SOURCE_DAY_DETAIL &&
                com.yuedong.fitness.ui.plan.ExercisePickBus.selected.isNotEmpty()
            ) {
                com.yuedong.fitness.ui.plan.ExercisePickBus.selected.toList().forEach { name ->
                    val rec = com.yuedong.fitness.domain.logic.CommonExercises.find(name)
                    val setsText = rec?.let { it.sets.toString() + " 组 × " + it.reps } ?: ""
                    viewModel.addExtraExercise(name, setsText)
                }
                com.yuedong.fitness.ui.plan.ExercisePickBus.clear()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text("当日记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 日期 + 打卡状态/补打卡 + 训练概览
                item {
                    Column(Modifier.padding(horizontal = 4.dp)) {
                        Text(dateLabel, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    com.yuedong.fitness.ui.theme.DiveGradients.heroGlow(diveDark()),
                                    MaterialTheme.shapes.large
                                ),
                            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (state.checkedIn) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            if (state.checkedIn) "已打卡 ✓" else "未打卡",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (state.checkedIn) MaterialTheme.colorScheme.tertiary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(Modifier.weight(1f))
                                    if (!state.checkedIn && parsedDate.isBefore(AppClock.today()) && canMakeUp) {
                                        Button(
                                            onClick = { viewModel.makeUpCheckIn() },
                                            modifier = Modifier.height(36.dp),
                                            shape = MaterialTheme.shapes.large,
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)
                                        ) {
                                            Text("补打卡", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "训练完成 ${state.doneCount + extraExercises.size}/${state.totalCount + extraExercises.size} 项",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DiveMono
                                )
                                if (state.exercises.isNotEmpty()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        buildString {
                                            val done = state.exercises.filter { it.second }.map { it.first.name }
                                            val undone = state.exercises.filterNot { it.second }.map { it.first.name }
                                            if (done.isNotEmpty()) append("已练:" + done.joinToString("、"))
                                            if (undone.isNotEmpty()) {
                                                if (done.isNotEmpty()) append("　")
                                                append("未练:" + undone.joinToString("、"))
                                            }
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                // 当日训练(含添加动作入口与额外动作列表)
                item {
                    DaySectionCard(
                        title = "当日训练",
                        action = {
                            Text(
                                "＋ 添加动作",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clickable {
                                        com.yuedong.fitness.ui.plan.ExercisePickBus.startForDayDetail()
                                        onOpenExercisePicker()
                                    }
                                    .padding(start = 8.dp)
                            )
                        }
                    ) {
                        // 补录的额外动作也算记录：仅有额外动作时不再显示空态
                        if (state.exercises.isEmpty() && extraExercises.isEmpty()) {
                            SectionEmpty("当天没有逐项训练记录")
                        } else {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    state.planName ?: "训练计划",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "完成 ${state.doneCount + extraExercises.size}/${state.totalCount + extraExercises.size} 项",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = DiveMono,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            state.exercises.forEach { (exercise, done) ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (done) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                        contentDescription = if (done) "已完成" else "未完成",
                                        tint = if (done) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        exercise.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "${exercise.sets} 组 × ${exercise.repsOrDuration}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = DiveMono,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (extraExercises.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            extraExercises.forEach { extra ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "＋",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        extra.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        extra.setsText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        Icons.Filled.Cancel,
                                        contentDescription = "删除",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.deleteExtraExercise(extra.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                // 当日饮食
                item {
                    DaySectionCard(
                        title = "当日饮食",
                        action = {
                            Text(
                                "＋ 添加食物",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clickable { showFoodLog = true }
                                    .padding(start = 8.dp)
                            )
                        }
                    ) {
                        Spacer(Modifier.height(2.dp))
                        if (dietLogs.isEmpty()) {
                            SectionEmpty("当天没有饮食记录")
                        } else {
                            val totalKcal = dietLogs.sumOf { it.calories }
                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    "共 ${dietLogs.size} 项",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "合计 $totalKcal 千卡",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = DiveMono,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            dietLogs.forEach { log ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        mealLabel(log.mealType),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(44.dp)
                                    )
                                    Text(
                                        log.foodName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${log.grams.roundToInt()} 克 · ${log.calories} 千卡",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = DiveMono,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                // 当日体重:显示 + 补录/修改
                item {
                    DaySectionCard(
                        title = "当日体重",
                        action = {
                            Text(
                                if (weightEntry == null) "补录体重" else "修改",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clickable { showWeight = true }
                                    .padding(start = 8.dp)
                            )
                        }
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                weightEntry?.let { String.format(Locale.CHINA, "%.1f", it.weightKg) } ?: "--",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DiveMono
                            )
                            Text(
                                " 公斤",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
                // 当日日记
                item {
                    DaySectionCard(title = "当日日记") {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { showDiary = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val d = diary
                            if (d != null) {
                                Text(
                                    when (d.mood) {
                                        1 -> "😞"; 2 -> "😟"; 3 -> "😐"; 4 -> "🙂"; else -> "😄"
                                    },
                                    fontSize = 18.sp
                                )
                            } else {
                                Icon(
                                    Icons.Filled.EditNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                d?.content?.ifBlank { "(只有心情～)" }
                                    ?: "当天还没写日记，写一句吧",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // ===== 弹层集合 =====
        // 添加食物:复用 FoodLogPage 整页选择器,保存到该日期
        if (showFoodLog) {
            FoodLogPage(
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.yuedong.fitness.ui.diet.DietViewModel.factory()
                ),
                initialMeal = "",
                dateLabel = dateText,
                recentLogs = dietLogs,
                customFoods = emptyList(),
                onAddCustom = { _, _, _, _, _, _ -> },
                onSaveAll = { selections, gramsList, meal ->
                    selections.forEachIndexed { index, sel ->
                        viewModel.addFood(
                            date = viewModel.dateIso,
                            mealType = meal,
                            foodName = sel.name,
                            grams = gramsList[index],
                            calories = (sel.kcalPer100 * gramsList[index] / 100.0).roundToInt(),
                            proteinG = sel.proteinPer100 * gramsList[index] / 100.0,
                            carbG = sel.carbPer100 * gramsList[index] / 100.0,
                            fatG = sel.fatPer100 * gramsList[index] / 100.0
                        )
                    }
                    showFoodLog = false
                },
                onDismiss = { showFoodLog = false }
            )
        }

        // 日记弹层
        if (showDiary) {
            DiaryDialog(
                existing = diary,
                onDismiss = { showDiary = false },
                onSave = { mood, content ->
                    viewModel.saveDiary(mood, content)
                    showDiary = false
                },
                title = dateLabel + "日记"
            )
        }

        // 补录体重弹层
        if (showWeight) {
            WeightLogDialog(
                initial = weightEntry?.weightKg ?: 70.0,
                onDismiss = { showWeight = false },
                onSave = { kg ->
                    viewModel.saveWeight(kg)
                    showWeight = false
                }
            )
        }

        // 添加动作弹层
        if (showAddExercise) {
            var exName by remember { mutableStateOf("") }
            var exSets by remember { mutableStateOf("3 组 × 12 次") }
            YueDialog(
                onDismissRequest = { showAddExercise = false },
                title = { Text("添加动作", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            shape = MaterialTheme.shapes.small,
                            value = exName,
                            onValueChange = { exName = it },
                            label = { Text("动作名称") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            shape = MaterialTheme.shapes.small,
                            value = exSets,
                            onValueChange = { exSets = it },
                            label = { Text("组次(如:3 组 × 12 次)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (exName.isNotBlank()) {
                            viewModel.addExtraExercise(exName.trim(), exSets.trim())
                        }
                        showAddExercise = false
                    }) {
                        Text("添加", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddExercise = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
private fun DaySectionCard(
    title: String,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                action?.invoke()
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SectionEmpty(text: String) {
    EmptyState(text)
}

/** 餐次中文名(数据存英文常量) */
private fun mealLabel(value: String): String = when (value) {
    "BREAKFAST" -> "早餐"
    "LUNCH" -> "午餐"
    "DINNER" -> "晚餐"
    "SNACK" -> "加餐"
    else -> value
}
