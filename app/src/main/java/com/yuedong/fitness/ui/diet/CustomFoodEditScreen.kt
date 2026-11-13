package com.yuedong.fitness.ui.diet

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.CustomFoodEntity
import com.yuedong.fitness.data.repository.DietLogRepository
import com.yuedong.fitness.ui.components.AppMedia
import com.yuedong.fitness.ui.components.ImageCropOverlay
import com.yuedong.fitness.ui.components.LocalMediaImage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 新建/编辑自定义食物（整页）：布局对齐食材详情页——
 * 顶部食材图片（可上传/更换/移除，从相册选择），
 * 下方字段按「标题 + 可编辑内容」组织：名称、每 100 克营养值、介绍。
 * 新建时营养值按常见参考值预填，用户可自行修改。
 */
@Composable
fun CustomFoodEditScreen(
    editName: String?,
    onBack: () -> Unit,
    viewModel: CustomFoodEditViewModel = viewModel(
        key = "custom_food_edit_${editName ?: "new"}",
        factory = CustomFoodEditViewModel.factory(editName)
    )
) {
    val existing by viewModel.existing.collectAsState()
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

    var name by remember { mutableStateOf(loaded?.name ?: "") }
    var kcal by remember { mutableStateOf(loaded?.kcalPer100g?.toString() ?: "100") }
    var protein by remember { mutableStateOf(loaded?.proteinPer100g?.let { fmtFood(it) } ?: "5") }
    var carb by remember { mutableStateOf(loaded?.carbPer100g?.let { fmtFood(it) } ?: "15") }
    var fat by remember { mutableStateOf(loaded?.fatPer100g?.let { fmtFood(it) } ?: "3") }
    var sections by remember {
        mutableStateOf(
            loaded?.sections?.takeIf { it.isNotBlank() }?.let { parseSections(it) }
                ?: loaded?.description?.takeIf { it.isNotBlank() }?.let { listOf("介绍" to it) }
                ?: emptyList()
        )
    }
    var imagePath by remember { mutableStateOf(loaded?.imagePath) }
    var error by remember { mutableStateOf<String?>(null) }
    var cropBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var sectionErrorIndex by remember { mutableStateOf(-1) }
    val listState = rememberLazyListState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = AppMedia.loadBitmap(context.applicationContext, uri)
                if (bmp != null) cropBitmap = bmp
            }
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
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                if (viewModel.isEditing) "编辑食材" else "新建食材",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier
                                .size(120.dp)
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable {
                                    pickImage.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (imagePath != null) {
                                LocalMediaImage(
                                    path = imagePath,
                                    contentDescription = "食材图片",
                                    modifier = Modifier.size(120.dp)
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Filled.AddAPhoto,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "上传图片",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
                        ) {
                            TextButtonMini(if (imagePath != null) "更换图片" else "从相册选择") {
                                pickImage.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            if (imagePath != null) {
                                TextButtonMini("移除图片", tint = MaterialTheme.colorScheme.error) {
                                    viewModel.removeImage(imagePath) { imagePath = null }
                                }
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
                        OutlinedTextField(
                            shape = MaterialTheme.shapes.small,
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("食材名称") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "营养值 · 每 100 克",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            shape = MaterialTheme.shapes.small,
                            value = kcal,
                            onValueChange = { kcal = it },
                            label = { Text("热量（千卡）") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                shape = MaterialTheme.shapes.small,
                                value = protein,
                                onValueChange = { protein = it },
                                label = { Text("蛋白 g") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                shape = MaterialTheme.shapes.small,
                                value = carb,
                                onValueChange = { carb = it },
                                label = { Text("碳水 g") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                shape = MaterialTheme.shapes.small,
                                value = fat,
                                onValueChange = { fat = it },
                                label = { Text("脂肪 g") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "已按常见值预填，请按包装或实际情况修改",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "自定义图文段落",
                                style = MaterialTheme.typography.titleMedium,
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
                                label = { Text("标题（如：食用建议 / 搭配禁忌）") },
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
        }

        Button(
            onClick = {
                val kcalValue = kcal.trim().toDoubleOrNull()
                val proteinValue = protein.trim().toDoubleOrNull() ?: 0.0
                val carbValue = carb.trim().toDoubleOrNull() ?: 0.0
                val fatValue = fat.trim().toDoubleOrNull() ?: 0.0
                when {
                    name.trim().isEmpty() -> error = "请填写食材名称"
                    kcalValue == null || kcalValue < 0 -> error = "热量需为非负数值"
                    listOf(proteinValue, carbValue, fatValue).any { it < 0 } -> error = "营养值不能为负数"
                    else -> {
                        val badIdx = sections.indexOfFirst { it.second.isBlank() }
                        if (badIdx >= 0) {
                            // 空段落不放行：就地标红并滚动定位到段落卡
                            sectionErrorIndex = badIdx
                            error = "第 ${badIdx + 1} 段内容为空"
                            scope.launch { listState.animateScrollToItem(2) }
                        } else {
                            viewModel.save(
                                name = name.trim(),
                                kcal = kcalValue.roundToInt(),
                                protein = proteinValue,
                                carb = carbValue,
                                fat = fatValue,
                                sections = serializeSections(sections),
                                // 清除图片传空串（仓库层只在 null 时保留原值）
                                imagePath = imagePath ?: ""
                            )
                            onBack()
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(top = 10.dp, bottom = 12.dp)
                .fillMaxWidth()
                .height(50.dp)
        ) { Text(if (viewModel.isEditing) "保存修改" else "保存食材") }
        error?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        }
        cropBitmap?.let { bmp ->
            ImageCropOverlay(
                source = bmp,
                aspect = 1f,
                onConfirm = { cropped ->
                    scope.launch {
                        val p = AppMedia.saveBitmap(context.applicationContext, cropped)
                        if (p != null) {
                            AppMedia.delete(imagePath)
                            imagePath = p
                        }
                        cropBitmap = null
                    }
                },
                onDismiss = { cropBitmap = null }
            )
        }
    }
}

@Composable
private fun TextButtonMini(text: String, tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = tint)
    }
}

private fun fmtFood(v: Double): String {
    val r = (v * 10).roundToInt() / 10.0
    return if (r % 1.0 == 0.0) "${r.toInt()}" else "$r"
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

class CustomFoodEditViewModel private constructor(
    private val repository: DietLogRepository,
    editName: String?
) : ViewModel() {
    val isEditing: Boolean = editName != null

    val existing: StateFlow<CustomFoodEntity?> =
        if (editName != null) {
            repository.observeCustomFoods()
                .map { list -> list.firstOrNull { it.name == editName } }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
        } else {
            kotlinx.coroutines.flow.MutableStateFlow(null)
        }

    fun save(name: String, kcal: Int, protein: Double, carb: Double, fat: Double, sections: String, imagePath: String?) {
        viewModelScope.launch {
            // imagePath 语义：null = 保留原值（仓库层兜底）；"" = 清除；非空 = 新路径
            val entity = CustomFoodEntity(
                name = name,
                kcalPer100g = kcal,
                proteinPer100g = protein,
                carbPer100g = carb,
                fatPer100g = fat,
                description = "",
                sections = sections,
                imagePath = imagePath,
                createdAt = System.currentTimeMillis()
            )
            repository.upsertCustomFood(entity)
        }
    }

    fun importImage(context: android.content.Context, uri: android.net.Uri, replacing: String?, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val path = AppMedia.copyFromUri(context, uri, video = false)
            if (path != null) {
                AppMedia.delete(replacing)
                onDone(path)
            }
        }
    }

    fun removeImage(path: String?, onDone: () -> Unit) {
        AppMedia.delete(path)
        onDone()
    }

    companion object {
        fun factory(editName: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                CustomFoodEditViewModel(app.container.dietLogRepository, editName)
            }
        }
    }
}
