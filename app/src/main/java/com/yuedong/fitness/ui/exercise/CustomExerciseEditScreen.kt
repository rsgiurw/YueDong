package com.yuedong.fitness.ui.exercise

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.ExpandMore
import com.yuedong.fitness.ui.components.YueDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.CustomExerciseEntity
import com.yuedong.fitness.data.repository.CustomExerciseRepository
import com.yuedong.fitness.ui.components.AppMedia
import com.yuedong.fitness.ui.components.ImageCropOverlay
import com.yuedong.fitness.ui.components.LocalMediaImage
import com.yuedong.fitness.ui.components.VideoExerciseIllustrationCard
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 自定义动作 查看/编辑 页（整页，布局对齐动作详情页）：
 * - 默认查看态：标题为动作名，封面/示范视频 + 难度·组次 + 自定义图文段落（只读），右上角「编辑」；
 * - 编辑态：封面图片（裁剪后保存）与示范视频可上传/更换/移除（操作在「动作示范」卡内）；
 *   可自定义「标题 + 内容」段落；右上角「取消/保存」；
 * - 新建（无 editName）直接进入编辑态。
 */
@Composable
fun CustomExerciseEditScreen(
    editName: String?,
    prefillName: String,
    startInEdit: Boolean = false,
    onBack: () -> Unit,
    viewModel: CustomExerciseEditViewModel = viewModel(
        key = "custom_exercise_edit_${editName ?: "new"}",
        factory = CustomExerciseEditViewModel.factory(editName)
    )
) {
    val existing by viewModel.existing.collectAsState()
    // 编辑模式先等库里读出原值，字段才有初始内容
    if (viewModel.isEditing && existing == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }
    val loaded = existing
    var editing by remember { mutableStateOf(startInEdit || !viewModel.isEditing) }

    var name by remember { mutableStateOf(loaded?.name ?: prefillName) }
    var sets by remember { mutableStateOf(loaded?.setsText ?: "1组") }
    var reps by remember { mutableStateOf(loaded?.repsOrDuration ?: "1次") }
    var difficulty by remember { mutableStateOf(loaded?.difficulty ?: "入门") }
    var bodyParts by remember {
        mutableStateOf((loaded?.bodyPart ?: "").split(',').map { it.trim() }.filter { it.isNotBlank() })
    }
    var showPartPicker by remember { mutableStateOf(false) }
    var sections by remember { mutableStateOf(parseSections(loaded?.sections)) }
    var videoPath by remember { mutableStateOf(loaded?.videoPath) }
    var coverPath by remember { mutableStateOf(loaded?.coverPath) }
    val originalVideoPath = remember { loaded?.videoPath }
    val originalCoverPath = remember { loaded?.coverPath }
    var nameError by remember { mutableStateOf<String?>(null) }
    var sectionErrorIndex by remember { mutableStateOf(-1) }
    val listState = rememberLazyListState()
    if (showPartPicker) {
        YueDialog(
            onDismissRequest = { showPartPicker = false },
            title = { Text("锻炼部位（可多选）", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EXERCISE_PARTS.chunked(4).forEach { rowParts ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowParts.forEach { part ->
                                FilterChip(
                                    selected = part in bodyParts,
                                    onClick = {
                                        bodyParts = if (part in bodyParts) bodyParts - part else bodyParts + part
                                    },
                                    label = { Text(part, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPartPicker = false }) { Text("确定") }
            }
        )
    }
    var cropBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pickVideo = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.importMedia(context.applicationContext, uri, video = true, replacing = videoPath) { videoPath = it }
        }
    }
    val pickCover = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = AppMedia.loadBitmap(context.applicationContext, uri)
                if (bmp != null) cropBitmap = bmp
            }
        }
    }

    fun cancelEdit() {
        // 取消：清掉已拷贝但未保存的新媒体文件，回到查看态
        if (videoPath != originalVideoPath) AppMedia.delete(videoPath)
        if (coverPath != originalCoverPath) AppMedia.delete(coverPath)
        videoPath = originalVideoPath
        coverPath = originalCoverPath
        loaded?.let {
            sets = it.setsText
            reps = it.repsOrDuration
            difficulty = it.difficulty
            sections = parseSections(it.sections)
        }
        nameError = null
        editing = false
    }

    Box(Modifier.fillMaxSize()) {
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
                    loaded?.name ?: "新建动作",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (editing) {
                    TextButton(onClick = { cancelEdit() }) { Text("取消") }
                } else {
                    TextButton(onClick = { editing = true }) { Text("编辑", fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(Modifier.height(4.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 12.dp)
            ) {
                item {
                    if (editing) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (videoPath != null) {
                                // 「动作示范」卡：上传的视频就地播放，更换/移除都在卡内
                                VideoExerciseIllustrationCard(
                                    videoUri = android.net.Uri.fromFile(java.io.File(videoPath!!)),
                                    legend = "",
                                    actions = {
                                        TextButton(onClick = {
                                            pickVideo.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                            )
                                        }) { Text("更换视频") }
                                        TextButton(onClick = {
                                            viewModel.removeMedia(videoPath) { videoPath = null }
                                        }) { Text("移除视频", color = MaterialTheme.colorScheme.error) }
                                    }
                                )
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "动作示范",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                                .clip(MaterialTheme.shapes.large)
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                                .clickable {
                                                    pickVideo.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                                    )
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    Icons.Filled.Videocam,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(Modifier.height(6.dp))
                                                Text(
                                                    "上传示范视频",
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "封面图片",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(MaterialTheme.shapes.large)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                            .clickable {
                                                pickCover.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (coverPath != null) {
                                            LocalMediaImage(
                                                path = coverPath,
                                                contentDescription = "封面图片",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            // 默认封面：吉祥物插画（上传后即替换）
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(com.yuedong.fitness.R.drawable.mascot_shiba),
                                                contentDescription = "默认封面",
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Text(
                                                "默认封面 · 点按上传专属封面",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(bottom = 8.dp)
                                            )
                                        }
                                    }
                                    if (coverPath != null) {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                                        ) {
                                            TextButton(onClick = {
                                                pickCover.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }) { Text("更换图片") }
                                            TextButton(onClick = {
                                                viewModel.removeMedia(coverPath) { coverPath = null }
                                            }) { Text("移除图片", color = MaterialTheme.colorScheme.error) }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        when {
                            videoPath != null -> VideoExerciseIllustrationCard(
                                videoUri = android.net.Uri.fromFile(java.io.File(videoPath!!)),
                                legend = ""
                            )
                            coverPath != null -> Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                LocalMediaImage(
                                    path = coverPath,
                                    contentDescription = "封面图片",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp)
                                )
                            }
                            else -> Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "暂无封面与示范视频",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            if (editing) {
                                if (!viewModel.isEditing) {
                                    FieldLabel("动作名称")
                                    OutlinedTextField(
                                        shape = MaterialTheme.shapes.small,
                                        value = name,
                                        onValueChange = {
                                            name = it
                                            nameError = null
                                        },
                                        isError = nameError != null,
                                        supportingText = { nameError?.let { Text(it) } },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                                FieldLabel("难度")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("入门", "初级", "中级", "高级").forEach { level ->
                                        FilterChip(
                                            selected = difficulty == level,
                                            onClick = { difficulty = level },
                                            label = { Text(level, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                FieldLabel("锻炼部位")
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        Modifier
                                            .clickable { showPartPicker = true }
                                            .padding(horizontal = 12.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            bodyParts.joinToString("、").ifBlank { "选择锻炼部位（可多选）" },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (bodyParts.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Icon(
                                            Icons.Filled.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        FieldLabel("组数")
                                        OutlinedTextField(
                                            shape = MaterialTheme.shapes.small,
                                            value = sets,
                                            onValueChange = { sets = it },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    Column(Modifier.weight(1f)) {
                                        FieldLabel("次数 / 时长")
                                        OutlinedTextField(
                                            shape = MaterialTheme.shapes.small,
                                            value = reps,
                                            onValueChange = { reps = it },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    Column(Modifier.weight(1f)) { ViewStack("难度", difficulty) }
                                    Column(Modifier.weight(1f)) { ViewStack("锻炼部位", bodyParts.joinToString("、").ifBlank { "-" }) }
                                    Column(Modifier.weight(1f)) { ViewStack("推荐组次", "$sets × $reps") }
                                }
                            }
                        }
                    }
                }
                if (editing) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "自定义图文段落",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = {
                                        sections = sections + ("" to "")
                                    }) { Text("＋ 添加一段") }
                                }
                                Text(
                                    "每段会以「标题 + 内容」卡片展示在详情里",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))
                                sections.forEachIndexed { index, (title, content) ->
                                    OutlinedTextField(
                                        shape = MaterialTheme.shapes.small,
                                        value = title,
                                        onValueChange = { newTitle ->
                                            sections = sections.toMutableList().also {
                                                it[index] = newTitle to it[index].second
                                            }
                                        },
                                        label = { Text("标题（如：动作步骤 / 常见错误）") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedTextField(
                                        shape = MaterialTheme.shapes.small,
                                        value = content,
                                        onValueChange = { newContent ->
                                            if (sectionErrorIndex == index && newContent.isNotBlank()) {
                                                sectionErrorIndex = -1
                                            }
                                            sections = sections.toMutableList().also {
                                                it[index] = it[index].first to newContent
                                            }
                                        },
                                        label = { Text("内容") },
                                        minLines = 2,
                                        isError = sectionErrorIndex == index,
                                        supportingText = if (sectionErrorIndex == index) {
                                            { Text("请填写内容，或移除此段") }
                                        } else null,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    TextButton(onClick = {
                                        sections = sections.toMutableList().also { it.removeAt(index) }
                                    }) { Text("移除此段", color = MaterialTheme.colorScheme.error) }
                                    Spacer(Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                } else {
                    sections.forEachIndexed { index, (title, content) ->
                        item(key = "section_$index") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        title.ifBlank { "详情" },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(content, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            if (editing) {
                Button(
                    onClick = {
                        val badIdx = sections.indexOfFirst { it.second.isBlank() }
                        when {
                            name.trim().isEmpty() -> nameError = "请填写动作名称"
                            badIdx >= 0 -> {
                                // 空段落不放行：就地标红并滚动定位到段落卡
                                sectionErrorIndex = badIdx
                                scope.launch { listState.animateScrollToItem(2) }
                            }
                            else -> {
                                viewModel.save(
                                    name = name,
                                    setsText = sets.ifBlank { "1组" },
                                    repsOrDuration = reps.ifBlank { "1次" },
                                    difficulty = difficulty,
                                    bodyPart = bodyParts.joinToString(","),
                                    sections = serializeSections(sections),
                                    videoPath = videoPath,
                                    coverPath = coverPath
                                )
                                if (viewModel.isEditing) editing = false else onBack()
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 12.dp)
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        if (viewModel.isEditing) "保存修改" else "保存动作",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(Modifier.height(12.dp))
            }
        }
        cropBitmap?.let { bmp ->
            ImageCropOverlay(
                source = bmp,
                aspect = 16f / 9f,
                onConfirm = { cropped ->
                    scope.launch {
                        val p = AppMedia.saveBitmap(context.applicationContext, cropped)
                        if (p != null) {
                            AppMedia.delete(coverPath)
                            coverPath = p
                        }
                        cropBitmap = null
                    }
                },
                onDismiss = { cropBitmap = null }
            )
        }
    }
}

/** 查看态字段：标题 + 内容（上下对应，文字统一居中） */
@Composable
private fun ViewStack(label: String, content: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Text(
            content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

/** 段落存储格式：段与段 \u001E 分隔，标题与正文 \u001F 分隔 */
private fun parseSections(raw: String?): List<Pair<String, String>> =
    raw?.split('\u001E')
        ?.filter { it.isNotBlank() }
        ?.map { entry ->
            val idx = entry.indexOf('\u001F')
            if (idx >= 0) entry.take(idx) to entry.substring(idx + 1) else entry to ""
        }
        ?: emptyList()

private fun serializeSections(list: List<Pair<String, String>>): String =
    list.filter { it.first.isNotBlank() || it.second.isNotBlank() }
        .joinToString("\u001E") { "${it.first}\u001F${it.second}" }

/** 可选锻炼部位（全量集合，弹层中多选） */
private val EXERCISE_PARTS = listOf("胸部", "背部", "肩部", "手臂", "腹部", "核心", "臀部", "腿部", "小腿", "腰部", "全身", "有氧")

class CustomExerciseEditViewModel private constructor(
    private val repository: CustomExerciseRepository,
    editName: String?
) : ViewModel() {
    val isEditing: Boolean = editName != null

    /** 编辑目标（按名称定位）；新建模式恒为 null */
    val existing: StateFlow<CustomExerciseEntity?> =
        if (editName != null) {
            repository.observeAll()
                .map { list -> list.firstOrNull { it.name == editName } }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
        } else {
            kotlinx.coroutines.flow.MutableStateFlow(null)
        }

    fun save(
        name: String,
        setsText: String,
        repsOrDuration: String,
        difficulty: String,
        bodyPart: String,
        sections: String,
        videoPath: String?,
        coverPath: String?
    ) {
        viewModelScope.launch {
            repository.upsert(
                name = name,
                setsText = setsText,
                repsOrDuration = repsOrDuration,
                difficulty = difficulty,
                bodyPart = bodyPart,
                description = "",
                sections = sections,
                videoPath = videoPath,
                coverPath = coverPath
            )
        }
    }

    fun importMedia(context: android.content.Context, uri: android.net.Uri, video: Boolean, replacing: String?, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val path = AppMedia.copyFromUri(context, uri, video = video)
            if (path != null) {
                AppMedia.delete(replacing)
                onDone(path)
            }
        }
    }

    fun removeMedia(path: String?, onDone: () -> Unit) {
        AppMedia.delete(path)
        onDone()
    }

    companion object {
        fun factory(editName: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                CustomExerciseEditViewModel(app.container.customExerciseRepository, editName)
            }
        }
    }
}
