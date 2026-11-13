package com.yuedong.fitness.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.minimumInteractiveComponentSize
import com.yuedong.fitness.ui.components.YueToaster
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuedong.fitness.ui.components.EmptyState
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 回收站:已删除的自定义内容(备忘录/照片/自定义食物/自定义动作)可恢复或彻底删除。
 * 彻底删除会同时清理关联的本地图片/视频文件。
 */
@Composable
fun RecycleBinScreen(
    onBack: () -> Unit,
    viewModel: RecycleBinViewModel = viewModel(factory = RecycleBinViewModel.factory())
) {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

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
            Column {
                Text("回收站", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "已删除的内容可在此恢复",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val isEmpty = state.memos.isEmpty() && state.photos.isEmpty() &&
            state.foods.isEmpty() && state.exercises.isEmpty() && state.plans.isEmpty()
        if (isEmpty) {
            EmptyState("回收站是空的")
            return@Column
        }

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.memos.isNotEmpty()) {
                item { SectionTitle("备忘录", state.memos.size) }
                items(state.memos, key = { "memo_${it.id}" }) { memo ->
                    DeletedRow(
                        title = memo.content.take(24).ifBlank { "无标题" },
                        subtitle = ""
                    ) {
                        viewModel.restoreMemo(memo.id)
                    }
                    PurgeRow {
                        scope.launch {
                            viewModel.purgeMemo(memo.id)
                            com.yuedong.fitness.ui.components.YueToaster.show("已彻底删除")
                        }
                    }
                }
            }
            if (state.photos.isNotEmpty()) {
                item { SectionTitle("照片", state.photos.size) }
                items(state.photos, key = { "photo_${it.id}" }) { photo ->
                    DeletedRow(title = "照片", subtitle = photo.date) {
                        viewModel.restorePhoto(photo.id)
                    }
                    PurgeRow {
                        scope.launch {
                            com.yuedong.fitness.ui.components.AppMedia.delete(photo.path)
                            viewModel.purgePhoto(photo.id)
                            com.yuedong.fitness.ui.components.YueToaster.show("已彻底删除")
                        }
                    }
                }
            }
            if (state.foods.isNotEmpty()) {
                item { SectionTitle("自定义食物", state.foods.size) }
                items(state.foods, key = { "food_${it.name}" }) { food ->
                    DeletedRow(title = food.name, subtitle = "${food.kcalPer100g} 千卡/100克") {
                        viewModel.restoreFood(food.name)
                    }
                    PurgeRow {
                        scope.launch {
                            com.yuedong.fitness.ui.components.AppMedia.delete(food.imagePath)
                            viewModel.purgeFood(food.name)
                            com.yuedong.fitness.ui.components.YueToaster.show("已彻底删除")
                        }
                    }
                }
            }
            if (state.exercises.isNotEmpty()) {
                item { SectionTitle("自定义动作", state.exercises.size) }
                items(state.exercises, key = { "ex_${it.name}" }) { exercise ->
                    DeletedRow(title = exercise.name, subtitle = exercise.difficulty) {
                        viewModel.restoreExercise(exercise.name)
                    }
                    PurgeRow {
                        scope.launch {
                            com.yuedong.fitness.ui.components.AppMedia.delete(exercise.videoPath)
                            com.yuedong.fitness.ui.components.AppMedia.delete(exercise.coverPath)
                            viewModel.purgeExercise(exercise.name)
                            com.yuedong.fitness.ui.components.YueToaster.show("已彻底删除")
                        }
                    }
                }
            }
            if (state.plans.isNotEmpty()) {
                item { SectionTitle("训练计划", state.plans.size) }
                items(state.plans, key = { "plan_${it.id}" }) { plan ->
                    DeletedRow(title = plan.name, subtitle = "已删除的计划仍保留打卡历史") {
                        viewModel.restorePlan(plan.id)
                    }
                    PurgeRow {
                        scope.launch {
                            viewModel.purgePlan(plan.id)
                            com.yuedong.fitness.ui.components.YueToaster.show("已彻底删除")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, count: Int) {
    Text(
        "$title · ${count}项",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
}

/** 已删除条目行:信息 + 「恢复」 */
@Composable
private fun DeletedRow(title: String, subtitle: String, onRestore: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            "恢复",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable(onClick = onRestore)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/** 彻底删除行:小字操作,防误触 */
@Composable
private fun PurgeRow(onPurge: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "彻底删除(不可恢复)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
            modifier = Modifier
                .clickable(onClick = onPurge)
                .padding(vertical = 4.dp)
        )
    }
}

/** 回收站 ViewModel:聚合四类已删除内容 */
class RecycleBinViewModel(
    private val memoRepository: com.yuedong.fitness.data.repository.MemoRepository,
    private val photoWallRepository: com.yuedong.fitness.data.repository.PhotoWallRepository,
    private val dietLogRepository: com.yuedong.fitness.data.repository.DietLogRepository,
    private val customExerciseRepository: com.yuedong.fitness.data.repository.CustomExerciseRepository,
    private val planRepository: com.yuedong.fitness.data.repository.PlanRepository
) : androidx.lifecycle.ViewModel() {

    data class RecycleBinState(
        val memos: List<com.yuedong.fitness.data.db.entity.MemoEntity> = emptyList(),
        val photos: List<com.yuedong.fitness.data.db.entity.PhotoWallEntity> = emptyList(),
        val foods: List<com.yuedong.fitness.data.db.entity.CustomFoodEntity> = emptyList(),
        val exercises: List<com.yuedong.fitness.data.db.entity.CustomExerciseEntity> = emptyList(),
        val plans: List<com.yuedong.fitness.data.db.entity.WorkoutPlanEntity> = emptyList()
    )

    val state: kotlinx.coroutines.flow.StateFlow<RecycleBinState> = kotlinx.coroutines.flow.combine(
        memoRepository.observeDeleted(),
        photoWallRepository.observeDeleted(),
        dietLogRepository.observeDeletedCustomFoods(),
        customExerciseRepository.observeDeleted(),
        planRepository.observeDeletedPlans()
    ) { memos, photos, foods, exercises, plans ->
        RecycleBinState(memos, photos, foods, exercises, plans)
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        RecycleBinState()
    )

    fun restoreMemo(id: Long) { viewModelScope.launch { memoRepository.restore(id) } }
    fun purgeMemo(id: Long) { viewModelScope.launch { memoRepository.purge(id) } }
    fun restorePhoto(id: Long) { viewModelScope.launch { photoWallRepository.restore(id) } }
    fun purgePhoto(id: Long) { viewModelScope.launch { photoWallRepository.purge(id) } }
    fun restoreFood(name: String) { viewModelScope.launch { dietLogRepository.restoreCustomFood(name) } }
    fun purgeFood(name: String) { viewModelScope.launch { dietLogRepository.purgeCustomFood(name) } }
    fun restoreExercise(name: String) { viewModelScope.launch { customExerciseRepository.restore(name) } }
    fun purgeExercise(name: String) { viewModelScope.launch { customExerciseRepository.purge(name) } }
    fun restorePlan(planId: Long) { viewModelScope.launch { planRepository.restorePlan(planId) } }
    fun purgePlan(planId: Long) { viewModelScope.launch { planRepository.purgePlan(planId) } }

    companion object {
        fun factory(): androidx.lifecycle.ViewModelProvider.Factory =
            androidx.lifecycle.viewmodel.viewModelFactory {
                initializer {
                    val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as com.yuedong.fitness.YueDongApplication
                    RecycleBinViewModel(
                        app.container.memoRepository,
                        app.container.photoWallRepository,
                        app.container.dietLogRepository,
                        app.container.customExerciseRepository,
                        app.container.planRepository
                    )
                }
            }
    }
}
