package com.yuedong.fitness.ui.today

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuedong.fitness.ui.components.BackHeader
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.util.AppClock
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 今日训练详情（独立页，从今日页训练卡点进）：
 * 聚焦"今天这一天"——完整动作清单 + 逐项勾选（全部完成自动打卡）。
 * 点击动作名可查看动作详情（视频演示）。
 */
@Composable
fun TodayTrainingScreen(
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit = {},
    viewModel: TodayViewModel = viewModel(factory = TodayViewModel.factory())
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val today = AppClock.today()
    val dateText = today.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA))
    val total = state.todayExercises.size
    val doneCount = state.todayExercises.count { state.exerciseDone[it.id] == true }
    val isRest = state.todayDay?.isRestDay == true

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 顶栏（二级页统一 BackHeader）
        BackHeader(title = dateText, onBack = onBack)

        if (isRest) {
            // 休息日：没有动作清单
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(40.dp))
                Text("😴", fontSize = 44.sp)
                Spacer(Modifier.height(12.dp))
                Text("今天是休息日", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "休息也是训练的一部分，肌肉正在恢复变强。\n去「今日」页打卡记录这份坚持吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        } else {
            Column(Modifier.padding(horizontal = 20.dp)) {
                // 标题块
                Text(
                    "第 ${state.planDayNumber} 天 · ${state.todayDay?.title ?: "训练日"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    buildString {
                        append(state.activePlan?.name ?: "训练计划")
                        append(" · 本轮周期还剩 ${state.planCycleLength - state.planDayNumber} 天")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(14.dp))
                // 进度
                // 标题跟随勾选状态：全部勾选 = 已完成打卡，否则一律是训练进度
                val allChecked = total > 0 && doneCount == total
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (allChecked) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (allChecked) "今日已完成打卡 ✓" else "今日训练进度",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (allChecked) MaterialTheme.colorScheme.tertiary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { if (total == 0) 0f else doneCount.toFloat() / total },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = if (doneCount == total && total > 0) MaterialTheme.colorScheme.tertiary
                                    else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    drawStopIndicator = {}
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "$doneCount/$total 项",
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = DiveMono,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "动作清单 · 点击条目看视频详情",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
            }
            // 动作清单
            LazyColumn(Modifier.padding(horizontal = 20.dp)) {
                items(state.todayExercises.size) { index ->
                    val exercise = state.todayExercises[index]
                    val done = state.exerciseDone[exercise.id] == true
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenExercise(exercise.name) }
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (done) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 完成勾选钮：点击勾选/取消该动作（卡片其余区域 = 看动作详情）
                            // 视觉圆 36dp，命中区保证 48dp
                            Box(
                                Modifier
                                    .minimumInteractiveComponentSize()
                                    .clickable {
                                        viewModel.toggleExercise(exercise.id, !done)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .background(
                                            if (done) MaterialTheme.colorScheme.tertiary
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (done) {
                                        Text(
                                            "✓",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onTertiary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(12.dp))
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
                                androidx.compose.foundation.layout.Box(
                                    Modifier
                                        .size(56.dp)
                                        .clip(MaterialTheme.shapes.large)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.Icon(
                                        Icons.Filled.FitnessCenter,
                                        contentDescription = exercise.name,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    exercise.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "${exercise.sets} 组 × ${exercise.repsOrDuration}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "查看视频详情",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(Modifier.width(28.dp).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "「完成今日训练」将记录全部动作并完成打卡",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.width(28.dp).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)))
                    }
                }
            }
        }
    }
}

/** 动作占位图标已统一为 FitnessCenter，原分类 emoji 映射移除 */
