package com.yuedong.fitness.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yuedong.fitness.domain.logic.CommonExercises
import com.yuedong.fitness.domain.logic.ExerciseLibrary
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.ui.components.ExerciseIllustrationCard
import com.yuedong.fitness.ui.components.TipCard
import com.yuedong.fitness.ui.components.VideoExerciseIllustrationCard
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.YueDongApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 动作教学详情页：视频示范（全部动作）+ 图文详解（已收录动作）+ 推荐训练量。
 * 数据源双轨：视频来自 ExerciseVideoCatalog，图文来自 ExerciseLibrary，二者独立演进。
 */
@Composable
fun ExerciseDetailScreen(
    name: String,
    onBack: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    /** 浏览模式（从新建计划选动作进入）：不显示编辑/删除 */
    viewOnly: Boolean = false,
    viewModel: ExerciseDetailViewModel = viewModel(
        key = "exercise_detail_$name",
        factory = ExerciseDetailViewModel.factory(name)
    )
) {
    val custom by viewModel.custom.collectAsState()
    var confirmDelete by remember { mutableStateOf(false) }
    val info = remember(name) { ExerciseLibrary.find(name) }
    val videoRes = remember(name) { ExerciseVideoCatalog.videoFor(name) }
    val recommendation = remember(name) { CommonExercises.find(name) }
    val category = remember(name) { ExerciseVideoCatalog.categoryOf(name) }
    // 自建动作的用户图文段落（标题 + 内容）
    val userSections = remember(name, custom) { parseSections(custom?.sections) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                info?.name ?: name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (custom != null && !viewOnly) {
                androidx.compose.material3.TextButton(onClick = { onEdit?.invoke() }) {
                    Text("编辑", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                androidx.compose.material3.TextButton(onClick = { confirmDelete = true }) {
                    Text("删除", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
        ) {
            item {
                if (custom?.videoPath != null) {
                    VideoExerciseIllustrationCard(
                        videoUri = android.net.Uri.fromFile(java.io.File(custom!!.videoPath!!)),
                        legend = "我的示范视频 · 静音自动循环"
                    )
                } else if (custom?.coverPath != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            com.yuedong.fitness.ui.components.LocalMediaImage(
                                path = custom!!.coverPath,
                                contentDescription = name,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3f)
                                    .clip(MaterialTheme.shapes.large)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "我的封面 · 可在「编辑」中更换",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else if (custom != null) {
                    // 无视频无封面：默认吉祥物封面
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(com.yuedong.fitness.R.drawable.mascot_shiba),
                                contentDescription = name,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3f)
                                    .clip(MaterialTheme.shapes.large)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "默认封面 · 上传封面或视频可在「编辑」中更换",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else if (videoRes != null) {
                    VideoExerciseIllustrationCard(
                        videoRes = videoRes,
                        legend = "🔴 红色 = 主要发力部位"
                    )
                } else if (info != null) {
                    ExerciseIllustrationCard(illustration = info.illustration)
                } else {
                    TipCard(
                        emoji = "🎬",
                        title = "动作视频制作中",
                        body = "先参考下方图文要点，视频即将补全"
                    )
                }
            }
            item {
                // 标签行：自建（自建标记/部位/难度/推荐量）与内置（分类/肌群/难度）同一布局
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (custom != null) {
                        val c = custom!!
                        TagChip("自建动作", filled = true)
                        c.bodyPart.split(',').map { it.trim() }.filter { it.isNotBlank() }
                            .forEach { part -> TagChip(part) }
                        TagChip("难度：${c.difficulty}", filled = false)
                        TagChip("推荐 ${c.setsText} × ${c.repsOrDuration}", filled = false, mono = true)
                    } else {
                        category?.let { TagChip(it) }
                        info?.muscleGroups?.forEach { muscle -> TagChip(muscle) }
                        Spacer(Modifier.weight(1f))
                        info?.let {
                            TagChip("难度：${it.difficulty}", filled = false)
                        }
                    }
                }
            }
            if (custom == null) {
                item {
                    TipCard(
                        emoji = "🎯",
                        title = "推荐训练量",
                        body = recommendation?.let { "常建议 ${it.sets} 组 × ${it.reps}，可根据自身状态增减" }
                            ?: "暂无推荐数据"
                    )
                }
            }
            // 自建动作：渲染用户添加的「标题 + 内容」图文段落（不显示内置图文详解）
            if (custom != null) {
                userSections.forEach { (title, content) ->
                    item(key = "sec_$title") {
                        SectionCard(title.ifBlank { "详情" }) {
                            Text(
                                content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            if (info != null && custom == null) {
                item {
                    SectionCard("动作为你带来的好处") {
                        info.benefits.forEach { benefit ->
                            Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                                Text("✅", style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.size(10.dp))
                                Text(
                                    benefit,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                item {
                    SectionCard("动作步骤") {
                        info.steps.forEachIndexed { index, step ->
                            Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                                Box(
                                    Modifier
                                        .size(22.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${index + 1}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.size(10.dp))
                                Text(
                                    step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                item {
                    SectionCard("常见错误") {
                        info.mistakes.forEach { mistake ->
                            Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                                Text("❌", style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.size(10.dp))
                                Text(
                                    mistake,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                item {
                    TipCard(emoji = "💨", title = "呼吸节奏", body = info.breathing)
                }
            }
            item {
                Text(
                    "动作示范仅供参考，请量力而行",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(60.dp))
            }
        }
        // 删除自定义动作二次确认（连同示范视频文件一起清理）
        if (confirmDelete && custom != null) {
            com.yuedong.fitness.ui.components.YueDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("删除自定义动作？") },
                text = { Text("将删除「${custom!!.name}」及其示范视频。已在计划中使用它的部分不受影响。此操作不可撤销。") },
                confirmButton = {
                    androidx.compose.material3.Button(onClick = {
                        viewModel.delete(name, custom!!.videoPath)
                        confirmDelete = false
                        onDelete?.invoke()
                    }) { Text("删除", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { confirmDelete = false }) { Text("取消") }
                }
            )
        }
    }
}

@Composable
private fun TagChip(text: String, filled: Boolean = true, mono: Boolean = false) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = if (filled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = if (mono) DiveMono else null,
            color = if (filled) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/** 段落存储格式：段与段 \u001E 分隔，标题与正文 \u001F 分隔（与自定义动作编辑器一致） */
/** 段落存储格式：段与段 \u001E 分隔，标题与正文 \u001F 分隔（与自定义动作编辑器一致） */
private fun parseSections(raw: String?): List<Pair<String, String>> =
    raw?.split('\u001E')
        ?.filter { it.isNotBlank() }
        ?.map { entry ->
            val idx = entry.indexOf('\u001F')
            if (idx >= 0) entry.take(idx) to entry.substring(idx + 1) else entry to ""
        }
        ?: emptyList()

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

/** 详情页自加载：按名称观察同名自定义动作，供媒体/组次与编辑删除使用 */
class ExerciseDetailViewModel(
    private val repository: com.yuedong.fitness.data.repository.CustomExerciseRepository,
    name: String
) : ViewModel() {
    val custom: StateFlow<com.yuedong.fitness.data.db.entity.CustomExerciseEntity?> =
        repository.observeAll()
            .map { list -> list.firstOrNull { it.name == name } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(name: String, videoPath: String?) {
        com.yuedong.fitness.ui.components.AppMedia.delete(videoPath)
        viewModelScope.launch { repository.delete(name) }
    }

    companion object {
        fun factory(name: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                ExerciseDetailViewModel(app.container.customExerciseRepository, name)
            }
        }
    }
}
