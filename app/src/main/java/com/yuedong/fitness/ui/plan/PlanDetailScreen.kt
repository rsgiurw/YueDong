package com.yuedong.fitness.ui.plan

import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.PlanDayEntity
import com.yuedong.fitness.data.db.entity.PlanExerciseEntity
import com.yuedong.fitness.data.db.entity.WorkoutPlanEntity
import com.yuedong.fitness.ui.exercise.ExerciseVideoCatalog
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.data.repository.PlanRepository
import com.yuedong.fitness.domain.model.Purpose
import com.yuedong.fitness.util.AppClock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 计划详情数据：计划 + 按天分组动作 + 是否激活。
 */
data class PlanDayUi(val day: PlanDayEntity, val exercises: List<PlanExerciseEntity>)

data class PlanDetailUi(
    val plan: WorkoutPlanEntity,
    val days: List<PlanDayUi>,
    val isActive: Boolean
)

/**
 * 计划详情（只读展示 + 切换激活）。
 */
class PlanDetailViewModel(
    private val planId: Long,
    private val planRepository: PlanRepository
) : ViewModel() {

    val uiState = combine(
        planRepository.observePlan(planId),
        planRepository.observePlanDays(planId),
        planRepository.observeExercisesOfPlan(planId),
        planRepository.observeActivePlanId()
    ) { plan, days, exercises, activeId ->
        if (plan == null) {
            null
        } else {
            PlanDetailUi(
                plan = plan,
                days = days.map { day ->
                    PlanDayUi(day = day, exercises = exercises.filter { it.planDayId == day.id })
                },
                isActive = plan.id == activeId
            )
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), null)

    fun setActive() {
        viewModelScope.launch { planRepository.setActivePlan(planId) }
    }

    companion object {
        fun factory(planId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                PlanDetailViewModel(planId, app.container.planRepository)
            }
        }
    }
}

@Composable
fun PlanDetailScreen(
    planId: Long,
    onBack: () -> Unit,
    onEditPlan: (Long) -> Unit = {},
    onOpenExercise: (String) -> Unit = {},
    viewModel: PlanDetailViewModel = viewModel(
        key = "plan_$planId",
        factory = PlanDetailViewModel.factory(planId)
    )
) {
    val currentDetail = viewModel.uiState.collectAsState().value
    if (currentDetail == null) {
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
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Text("计划详情", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        return
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
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text(
                    currentDetail.plan.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // 顶部右侧快捷操作：自建计划可修改；未启用可一键启用
                if (currentDetail.plan.source == "CUSTOM") {
                    TextButton(
                        onClick = { onEditPlan(currentDetail.plan.id) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("修改")
                    }
                }
                if (!currentDetail.isActive) {
                    Button(
                        onClick = { viewModel.setActive() },
                        modifier = Modifier.padding(start = 4.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)
                    ) {
                        Text("启用")
                    }
                }
            }
            val pagerState = rememberPagerState { currentDetail.days.size }
            val coroutineScope = rememberCoroutineScope()
            Column {
                Spacer(Modifier.height(8.dp))
                Text(
                    currentDetail.plan.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailChip(Purpose.fromValue(currentDetail.plan.purposeTag)?.label ?: currentDetail.plan.purposeTag)
                    DetailChip("每周 ${currentDetail.plan.daysPerWeek} 天")
                    DetailChip("周期 ${if (currentDetail.plan.cycleDays > 0) currentDetail.plan.cycleDays else currentDetail.days.size} 天循环")
                }
                if (currentDetail.isActive) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "当前计划 ✓",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(12.dp))
                // 日期指示器：点击直达任意天；当前翻到的天高亮
                val todayIdx = pagerState.currentPage
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    currentDetail.days.forEachIndexed { index, dayUi ->
                        // 指示条跟随滑动页（此前只在"今天"时高亮，滑动页面指示不变）
                        val isCurrent = index == pagerState.currentPage
                        val isToday = index == todayIdx
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isCurrent && isToday -> MaterialTheme.colorScheme.tertiary
                                isCurrent -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(
                                    if (isToday) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    val dayUi = currentDetail.days[pageIndex]
                    DayCard(
                        dayIndex = pageIndex + 1,
                        dayUi = dayUi,
                        onOpenExercise = onOpenExercise
                    )
                }
            }
        }
    }
}

/** 单日训练卡：休息日显示文案；训练日列出全部动作（点动作名看视频教学） */
@Composable
private fun DayCard(dayIndex: Int, dayUi: PlanDayUi, onOpenExercise: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "第 $dayIndex 天",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (dayUi.day.isRestDay) "休息日" else "训练日",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            if (dayUi.day.isRestDay) {
                Text(
                    "休息也是训练的一部分，肌肉正在恢复变强",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (dayUi.exercises.isEmpty()) {
                Text(
                    "这一天还没有安排动作",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                dayUi.exercises.forEach { exercise ->
                    ExerciseRow(exercise) {
                        onOpenExercise(exercise.name)
                    }
                }
            }
        }
    }
}

/** 动作行（参考图）：缩略图 + 名称/要领 + 右侧「N组 × N」胶囊 + 查看动作教学 */
@Composable
private fun ExerciseRow(exercise: PlanExerciseEntity, onClick: (() -> Unit)?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (onClick != null) {
            Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 7.dp)
        } else {
            Modifier.fillMaxWidth().padding(vertical = 7.dp)
        }
    ) {
        val thumbRes = ExerciseVideoCatalog.thumbFor(exercise.name)
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
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                exercise.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            if (exercise.tip.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    exercise.tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    "${exercise.sets} 组 × ${exercise.repsOrDuration}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DiveMono,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
