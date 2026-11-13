package com.yuedong.fitness.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.ui.components.PrimaryCta
import com.yuedong.fitness.ui.components.YueDialog
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.PlanDayWithExercises
import com.yuedong.fitness.data.db.entity.PlanDayEntity
import com.yuedong.fitness.data.db.entity.PlanExerciseEntity
import com.yuedong.fitness.data.db.entity.WorkoutPlanEntity
import com.yuedong.fitness.data.repository.PlanRepository
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.domain.logic.CommonExercises
import com.yuedong.fitness.domain.model.Purpose
import com.yuedong.fitness.domain.validation.InputValidator
import com.yuedong.fitness.domain.validation.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 新建/编辑计划表单（v2.2：7 天固定列表、默认全休息、灯泡选常见动作、默认 1 组 1 次、错误就地显示）。
 */
data class EditableExerciseUi(
    val name: String = "",
    val setsText: String = "1组",
    val repsOrDuration: String = "1次",
    val nameError: Boolean = false,
    val setsError: Boolean = false
)

data class EditableDayUi(
    val isRestDay: Boolean = true,
    val exercises: List<EditableExerciseUi> = emptyList(),
    val dayError: String? = null
)

data class PlanEditUiState(
    val name: String = "",
    val purposeTag: Purpose = Purpose.FAT_LOSS,
    val days: List<EditableDayUi> = List(7) { EditableDayUi() },
    /** 训练周期天数（3~90）：动作按此天数循环轮换；0 = 跟随模板 7 天 */
    val cycleDays: Int = 0,
    val nameError: String? = null,
    val weeklyError: String? = null,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    /** 保存失败次数：界面据此自动滚动到第一个出错位置。 */
    val saveFailTick: Int = 0
)

/**
 * 新建/编辑自定义计划（模块契约见设计包 §8 Plan）。
 * 职责：表单状态、就地校验、组装实体并入库/更新；非职责：不管理"激活计划"。
 * 每周训练天数 = 7 天中训练日的数量，自动计算。
 */
class PlanEditViewModel(
    private val planRepository: PlanRepository,
    private val customExerciseRepository: com.yuedong.fitness.data.repository.CustomExerciseRepository,
    private val editPlanId: Long?
) : ViewModel() {

    /** 我的历史自定义动作（灯泡选择器顶部展示，含组数/次数）。 */
    val customExercises: StateFlow<List<com.yuedong.fitness.data.db.entity.CustomExerciseEntity>> =
        customExerciseRepository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(PlanEditUiState())
    val uiState: StateFlow<PlanEditUiState> = _uiState.asStateFlow()

    init {
        editPlanId?.let { loadExisting(it) }
    }

    private fun loadExisting(planId: Long) {
        viewModelScope.launch {
            val plan = planRepository.getPlan(planId) ?: return@launch
            if (plan.source != "CUSTOM") return@launch
            val days = planRepository.getPlanDays(planId)
            val exercises = planRepository.getExercisesOfPlan(planId)

            val dayStates: List<EditableDayUi> = (0..6).map { index ->
                val day = days.find { it.orderIndex == index }
                if (day == null || day.isRestDay) {
                    EditableDayUi(isRestDay = true)
                } else {
                    EditableDayUi(
                        isRestDay = false,
                        exercises = exercises.filter { it.planDayId == day.id }.map {
                            EditableExerciseUi(
                                name = it.name,
                                setsText = "${it.sets}组",
                                repsOrDuration = it.repsOrDuration
                            )
                        }
                    )
                }
            }
            _uiState.update {
                it.copy(
                    name = plan.name,
                    purposeTag = Purpose.fromValue(plan.purposeTag) ?: Purpose.FAT_LOSS,
                    days = dayStates,
                    cycleDays = if (plan.cycleDays > 0) plan.cycleDays else 0,
                    isEditing = true
                )
            }
        }
    }

    fun setName(value: String) = _uiState.update { it.copy(name = value, nameError = null) }
    fun setPurposeTag(value: Purpose) = _uiState.update { it.copy(purposeTag = value) }

    /** 调整训练周期天数（3~90 天）：动作按此天数循环轮换 */
    fun setCycleDays(value: Int) = _uiState.update {
        it.copy(cycleDays = value.coerceIn(3, 90))
    }

    fun toggleRestDay(index: Int) = _uiState.update {
        it.copy(
            days = it.days.mapIndexed { i, day ->
                if (i == index) {
                    if (day.isRestDay) {
                        // 转训练日不预置空行：展开后只有「添加动作」一个入口
                        day.copy(isRestDay = false, dayError = null)
                    } else {
                        day.copy(isRestDay = true, dayError = null)
                    }
                } else {
                    day
                }
            },
            weeklyError = null
        )
    }

    /** 从动作库选完：同动作已存在则组数叠加合并为一行，否则追加新行 */
    fun addExerciseFromLibrary(dayIndex: Int, name: String, setsText: String?, repsOrDuration: String?) =
        _uiState.update {
            it.copy(
                days = it.days.mapIndexed { i, day ->
                    if (i == dayIndex) {
                        val incomingSets = setsText?.filter { c -> c.isDigit() }?.toIntOrNull() ?: 1
                        val existingIdx = day.exercises.indexOfFirst { it.name == name }
                        val exercises = if (existingIdx >= 0) {
                            // 同动作叠加：组数相加（上限 99 组），次数/时长保持每组标准量
                            day.exercises.mapIndexed { ei, ex ->
                                if (ei == existingIdx) {
                                    val curSets = ex.setsText.filter { c -> c.isDigit() }.toIntOrNull() ?: 1
                                    ex.copy(setsText = "${(curSets + incomingSets).coerceAtMost(99)}组")
                                } else {
                                    ex
                                }
                            }
                        } else {
                            day.exercises + EditableExerciseUi(
                                name = name,
                                setsText = setsText ?: "1组",
                                repsOrDuration = repsOrDuration ?: "1次"
                            )
                        }
                        day.copy(exercises = exercises, dayError = null)
                    } else {
                        day
                    }
                },
                weeklyError = null
            )
        }


    fun removeExercise(dayIndex: Int, exerciseIndex: Int) = _uiState.update {
        it.copy(
            days = it.days.mapIndexed { i, day ->
                if (i == dayIndex) {
                    day.copy(exercises = day.exercises.filterIndexed { ei, _ -> ei != exerciseIndex })
                } else {
                    day
                }
            }
        )
    }

    fun updateExercise(dayIndex: Int, exerciseIndex: Int, value: EditableExerciseUi) = _uiState.update {
        it.copy(
            days = it.days.mapIndexed { i, day ->
                if (i == dayIndex) {
                    day.copy(exercises = day.exercises.mapIndexed { ei, ex ->
                        if (ei == exerciseIndex) {
                            // 就地反馈：名称填了就消错；组数实时校验（非空且越界才标红）
                            val nameError = if (value.name.trim().isEmpty()) ex.nameError else false
                            val setsInvalid = InputValidator.validateSets(value.setsText) is ValidationResult.Invalid
                            val setsError = value.setsText.isNotBlank() && setsInvalid
                            value.copy(nameError = nameError, setsError = setsError)
                        } else {
                            ex
                        }
                    })
                } else {
                    day
                }
            }
        )
    }

    fun save(onDone: () -> Unit) {
        val state = _uiState.value

        val nameResult = InputValidator.validatePlanName(state.name)
        if (state.days.none { !it.isRestDay }) {
            _uiState.update {
                it.copy(
                    weeklyError = "至少把一天设为训练日（点某一天的「改为训练日」）",
                    saveFailTick = it.saveFailTick + 1
                )
            }
            return
        }

        // 全量校验并把错误标到对应的输入框上（就地显示 + 自动滚动到出错位置）
        // 名称不再手输（从动作库选择）：没选动作的空行直接忽略，不算错误
        var hasError = nameResult is ValidationResult.Invalid
        val newDays = state.days.mapIndexed { dayIndex, day ->
            if (day.isRestDay) return@mapIndexed day.copy(dayError = null)
            val filled = day.exercises.filter { it.name.isNotBlank() }
            val dayError = if (filled.isEmpty()) {
                hasError = true
                "这个训练日还没有动作，点「添加动作」去动作库选一个"
            } else {
                null
            }
            val exercises = filled.map { ex ->
                val setsError = InputValidator.validateSets(ex.setsText) is ValidationResult.Invalid
                if (setsError) hasError = true
                ex.copy(nameError = false, setsError = setsError)
            }
            day.copy(dayError = dayError, exercises = exercises)
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    days = newDays,
                    nameError = (nameResult as? ValidationResult.Invalid)?.message,
                    saveFailTick = it.saveFailTick + 1
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val trainingDays = state.days.count { !it.isRestDay }
            val plan = WorkoutPlanEntity(
                id = editPlanId ?: 0L,
                name = (nameResult as ValidationResult.Valid).value,
                description = "我的自定义计划",
                source = "CUSTOM",
                purposeTag = state.purposeTag.name,
                daysPerWeek = trainingDays,
                cycleDays = if (state.cycleDays > 0) state.cycleDays else state.days.size
            )
            val content = state.days.mapIndexed { dayIndex, day ->
                PlanDayWithExercises(
                    day = PlanDayEntity(
                        planId = 0,
                        orderIndex = dayIndex,
                        title = if (day.isRestDay) "休息" else "训练",
                        isRestDay = day.isRestDay
                    ),
                    exercises = day.exercises.map { exercise ->
                        PlanExerciseEntity(
                            planDayId = 0,
                            name = exercise.name.trim(),
                            sets = (InputValidator.validateSets(exercise.setsText) as ValidationResult.Valid).value,
                            repsOrDuration = exercise.repsOrDuration.trim(),
                            tip = ""
                        )
                    }
                )
            }
            if (state.isEditing && editPlanId != null) {
                planRepository.updatePlanWithContent(editPlanId, plan, content)
            } else {
                planRepository.insertPlanWithContent(plan, content)
            }
            // 自定义动作自动入库：不在常见动作库里的名称连同组数/次数一起存，以后可复用
            state.days.flatMap { it.exercises }
                .filter { it.name.trim().isNotEmpty() && CommonExercises.find(it.name.trim()) == null }
                .associateBy({ it.name.trim() }, { it })
                .forEach { (_, ex) ->
                    customExerciseRepository.upsert(
                        name = ex.name.trim(),
                        setsText = ex.setsText.trim(),
                        repsOrDuration = ex.repsOrDuration.trim(),
                        difficulty = "入门",
                        description = ""
                    )
                }
            onDone()
        }
    }

    companion object {
        fun factory(planId: Long?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                PlanEditViewModel(
                    planRepository = app.container.planRepository,
                    customExerciseRepository = app.container.customExerciseRepository,
                    editPlanId = planId
                )
            }
        }
    }
}

@Composable
fun PlanEditScreen(
    onBack: () -> Unit,
    planId: Long? = null,
    /** 进入动作选择页（目标天/替换行由 ExercisePickBus 记录） */
    onPickExercise: () -> Unit = {},
    viewModel: PlanEditViewModel = viewModel(
        key = "plan_edit_${planId ?: -1L}",
        factory = PlanEditViewModel.factory(planId)
    )
) {
    val state by viewModel.uiState.collectAsState()
    val customExercises by viewModel.customExercises.collectAsState()

    // 从动作选择页回来（本页 ON_RESUME）时吸收已添加清单，逐个填入对应天（允许重复）
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME && ExercisePickBus.pending &&
                ExercisePickBus.source == ExercisePickBus.SOURCE_PLAN
            ) {
                val picked = ExercisePickBus.selected.toList()
                val dayIdx = ExercisePickBus.targetDayIndex
                if (dayIdx in state.days.indices) {
                    picked.forEach { name ->
                        val rec = CommonExercises.find(name)
                        val custom = customExercises.find { it.name == name }
                        val recSets = rec?.sets?.toString()?.plus("组")
                            ?: custom?.setsText?.takeIf { it.isNotBlank() }
                        val recReps = rec?.reps ?: custom?.repsOrDuration?.takeIf { it.isNotBlank() }
                        viewModel.addExerciseFromLibrary(dayIdx, name, recSets, recReps)
                    }
                }
                ExercisePickBus.clear()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 保存失败时自动滚动到第一个出错位置
    val listState = rememberLazyListState()
    LaunchedEffect(state.saveFailTick) {
        if (state.saveFailTick > 0) {
            listState.animateScrollToItem(firstErrorItemIndex(state))
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            // 顶栏：返回 + 居中标题
            Box(Modifier.fillMaxWidth().height(48.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text(
                    if (state.isEditing) "编辑自定义计划" else "新建自定义计划",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState
            ) {
                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::setName,
                        placeholder = { Text("例如：我的睡前燃脂87") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        isError = state.nameError != null,
                        supportingText = { state.nameError?.let { Text(it) } },
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(Modifier.height(18.dp))
                    SectionHeader(Icons.Filled.Adjust, "健身目的")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Purpose.entries.forEach { purpose ->
                            PurposeChip(
                                label = purpose.label,
                                selected = state.purposeTag == purpose,
                                onClick = { viewModel.setPurposeTag(purpose) }
                            )
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(20.dp))
                    SectionHeader(Icons.Filled.CalendarMonth, "每周安排（7天循环）")
                    Spacer(Modifier.height(10.dp))
                    val trainingDays = state.days.count { !it.isRestDay }
                    val weeklyError = state.weeklyError
                    val bannerColor = if (weeklyError != null) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(bannerColor)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ⓘ ",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (weeklyError != null) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            when {
                                weeklyError != null -> weeklyError
                                trainingDays == 0 -> "默认都是休息日，点「改为训练日」开始安排训练周期。"
                                else -> "已安排每周训练 $trainingDays 天，点训练日可继续编辑动作。"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (weeklyError != null) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(14.dp))
                    // 周期步进 + 7 天列表：同一张卡（参考图）
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            // 训练周期：默认 7 天一个循环，选周期数即总天数 = 7 × N
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "训练周期",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    if (displayCycleDays(state) > 7) "动作按 7 天模板循环轮换" else "7 天一个循环",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1, 2, 3, 4).forEach { n ->
                                    androidx.compose.material3.FilterChip(
                                        selected = displayCycleDays(state) == n * 7,
                                        onClick = { viewModel.setCycleDays(n * 7) },
                                        label = {
                                            Text(
                                                "${n}周期",
                                                style = MaterialTheme.typography.labelLarge,
                                                maxLines = 1
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            if (displayCycleDays(state) > 7) {
                                Text(
                                    "共 ${displayCycleDays(state)} 天",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                            state.days.forEachIndexed { dayIndex, day ->
                                DayEditorRow(
                                    dayIndex = dayIndex,
                                    day = day,
                                    viewModel = viewModel,
                                    onPickForAdd = {
                                        ExercisePickBus.start(dayIndex)
                                        onPickExercise()
                                    }
                                )
                                if (dayIndex != state.days.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            PrimaryCta(
                text = if (state.isSaving) "保存中…" else if (state.isEditing) "保存修改" else "保存计划",
                onClick = { viewModel.save(onDone = onBack) },
                enabled = !state.isSaving,
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
            )
        }
    }
}

private fun displayCycleDays(state: PlanEditUiState): Int =
    if (state.cycleDays > 0) state.cycleDays else state.days.size

/** 每天一行：第 N 天 + 切换胶囊（休息日→改为训练日；训练日→改为休息日）+ 训练日可展开动作编辑 */
@Composable
private fun DayEditorRow(
    dayIndex: Int,
    day: EditableDayUi,
    viewModel: PlanEditViewModel,
    onPickForAdd: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable {
                    if (day.isRestDay) {
                        viewModel.toggleRestDay(dayIndex)
                    } else {
                        expanded = !expanded
                    }
                }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "第 ${dayIndex + 1} 天",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(12.dp))
            // 切换动作胶囊：休息日 → 改为训练日；训练日 → 改为休息日
            Surface(
                color = if (day.isRestDay) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable { viewModel.toggleRestDay(dayIndex) }
            ) {
                Text(
                    if (day.isRestDay) "改为训练日" else "改为休息日",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (day.isRestDay) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            if (!day.isRestDay) {
                // 训练日状态徽章（收起时也能看出这是训练日）
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        "训练日",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (!day.isRestDay && expanded) {
            day.exercises.forEachIndexed { exerciseIndex, exercise ->
                ExerciseEditorRow(
                    exercise = exercise,
                    onUpdate = { viewModel.updateExercise(dayIndex, exerciseIndex, it) },
                    onRemove = { viewModel.removeExercise(dayIndex, exerciseIndex) }
                )
            }
            // 添加动作：进动作选择页（描边按钮）
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clickable { onPickForAdd() }
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape, modifier = Modifier.size(18.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("＋", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(7.dp))
                    Text(
                        "添加动作（去动作库选）",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        day.dayError?.let { error ->
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

/** 动作行：第一行 = 缩略图 + 名称 + 删除；第二行 = 组数 + 次数/时长（改动作 = 删掉重加） */
@Composable
private fun ExerciseEditorRow(
    exercise: EditableExerciseUi,
    onUpdate: (EditableExerciseUi) -> Unit,
    onRemove: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            val thumbRes = com.yuedong.fitness.ui.exercise.ExerciseVideoCatalog.thumbFor(exercise.name)
            if (thumbRes != null) {
                androidx.compose.foundation.Image(
                    androidx.compose.ui.res.painterResource(thumbRes),
                    contentDescription = exercise.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.large)
                )
            } else {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            // 名称（改动作 = 删掉此行重新添加）
            Text(
                exercise.name.ifEmpty { "未选择动作" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (exercise.name.isEmpty()) FontWeight.Normal else FontWeight.Bold,
                color = if (exercise.name.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "删除动作", modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 54.dp)
        ) {
            Column(Modifier.width(96.dp)) {
                Text("组数", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = exercise.setsText,
                    onValueChange = { text -> onUpdate(exercise.copy(setsText = text)) },
                    isError = exercise.setsError,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("次数 / 时长", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = exercise.repsOrDuration,
                    onValueChange = { text -> onUpdate(exercise.copy(repsOrDuration = text)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (exercise.setsError) {
            Text(
                "组数需为 1~10",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 54.dp, top = 2.dp)
            )
        }
    }
}

/** 计算第一个出错位置对应的滚动条目索引（0=名称 2=每周安排 3=周期与每天卡片）。 */
private fun firstErrorItemIndex(state: PlanEditUiState): Int {
    if (state.nameError != null) return 0
    if (state.weeklyError != null) return 2
    val hasDayError = state.days.any { day ->
        day.dayError != null || day.exercises.any { it.nameError || it.setsError }
    }
    return if (hasDayError) 3 else 0
}

/** 区块标题：图标 + 加粗标题（参考图样式） */
@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

/** 目的胶囊：选中实心主色白字，未选中描边（参考图样式） */
@Composable
private fun PurposeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        )
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp)
        )
    }
}
