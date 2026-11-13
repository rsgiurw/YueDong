package com.yuedong.fitness.ui.exercise

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.ui.components.EmptyState
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.ui.components.SearchField
import com.yuedong.fitness.ui.components.ListActionSheet
import com.yuedong.fitness.ui.components.YueDialog
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.CustomExerciseEntity
import com.yuedong.fitness.data.repository.CustomExerciseRepository
import com.yuedong.fitness.data.repository.ExerciseFavoriteRepository
import com.yuedong.fitness.domain.logic.CommonExercises
import com.yuedong.fitness.domain.logic.ExerciseLibrary
import com.yuedong.fitness.domain.validation.InputValidator
import com.yuedong.fitness.domain.validation.ValidationResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 动作库（v2.5 新增；v2.6 布局与食物热量库统一为"左侧分类栏 + 右侧列表"）：
 * 全部动作的视频教学浏览 + 我的自定义动作管理（增删改）。
 * 职责：浏览/搜索/跳转详情、自定义动作的增删改；非职责：不承载计划逻辑。
 */
class ExerciseLibraryViewModel(
    private val customExerciseRepository: CustomExerciseRepository,
    private val exerciseFavoriteRepository: ExerciseFavoriteRepository,
    private val itemFlagRepository: com.yuedong.fitness.data.repository.ItemFlagRepository
) : ViewModel() {

    /** 置顶 / 隐藏标记（name → flags） */
    val exerciseFlags: StateFlow<Map<String, Set<String>>> = itemFlagRepository.observeByType("exercise")
        .map { rows ->
            rows.groupBy({ it.itemName }, { it.flag })
                .mapValues { (_, flags) -> flags.toSet() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setPinned(name: String, pinned: Boolean) {
        viewModelScope.launch {
            if (pinned) itemFlagRepository.setFlag("exercise", name, com.yuedong.fitness.data.repository.ItemFlags.PIN)
            else itemFlagRepository.clearFlag("exercise", name, com.yuedong.fitness.data.repository.ItemFlags.PIN)
        }
    }

    fun setHidden(name: String, hidden: Boolean) {
        viewModelScope.launch {
            if (hidden) itemFlagRepository.setFlag("exercise", name, com.yuedong.fitness.data.repository.ItemFlags.HIDDEN)
            else itemFlagRepository.clearFlag("exercise", name, com.yuedong.fitness.data.repository.ItemFlags.HIDDEN)
        }
    }

    val customExercises: StateFlow<List<CustomExerciseEntity>> =
        customExerciseRepository.observeAll().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    /** 收藏的动作名集合（按收藏时间倒序） */
    val favoriteNames: StateFlow<List<String>> = exerciseFavoriteRepository.observeAll()
        .map { favs -> favs.map { it.exerciseName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleFavorite(name: String, currentlyFavorite: Boolean) {
        viewModelScope.launch {
            if (currentlyFavorite) exerciseFavoriteRepository.removeFavorite(name)
            else exerciseFavoriteRepository.addFavorite(name)
        }
    }

    fun upsertCustom(name: String, setsText: String, repsOrDuration: String, difficulty: String, description: String) {
        viewModelScope.launch { customExerciseRepository.upsert(name, setsText, repsOrDuration, difficulty, description) }
    }

    fun deleteCustom(name: String) {
        viewModelScope.launch { customExerciseRepository.delete(name) }
    }

    /** 批量设置收藏（批量管理模式用）：favorite=true 全部加入收藏 */
    fun setFavorites(names: Collection<String>, favorite: Boolean) {
        viewModelScope.launch { names.forEach { toggleFavorite(it, !favorite) } }
    }

    /** 批量删除自定义动作 */
    fun deleteCustoms(names: Collection<String>) {
        viewModelScope.launch { names.forEach { customExerciseRepository.delete(it) } }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                ExerciseLibraryViewModel(
                    app.container.customExerciseRepository,
                    app.container.exerciseFavoriteRepository,
                    app.container.itemFlagRepository
                )
            }
        }
    }
}

/** 左侧分类栏宽度（与 FoodLibraryScreen 一致） */
private val CATEGORY_BAR_WIDTH = 96.dp

private const val CUSTOM_SECTION = "我的创作"
private const val FAVORITE_SECTION = "我的收藏"

@Composable
fun ExerciseLibraryScreen(
    onBack: () -> Unit,
    initialCustom: Boolean = false,
    onOpenExercise: (String) -> Unit,
    onCreateCustom: (String) -> Unit = {},
    onEditCustom: (String) -> Unit = {},
    viewModel: ExerciseLibraryViewModel = viewModel(factory = ExerciseLibraryViewModel.factory())
) {
    var query by rememberSaveable { mutableStateOf("") }
    var menuExercise by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var deleteTarget by remember { mutableStateOf<String?>(null) }
    // 批量管理模式：勾选当前列表里的多项，底部统一收藏/删除
    var managing by remember { mutableStateOf(false) }
    val selectedNames = remember { androidx.compose.runtime.mutableStateListOf<String>() }
    var toastMessage by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var confirmDelete by remember { androidx.compose.runtime.mutableStateOf(false) }
    fun toggleSelect(name: String) {
        if (selectedNames.contains(name)) selectedNames.remove(name) else selectedNames.add(name)
    }
    // 长按 = 进入选择模式并选中该行
    fun longPressSelect(name: String) {
        if (!managing) managing = true
        toggleSelect(name)
    }
    val customs by viewModel.customExercises.collectAsState()
    val exerciseFlags by viewModel.exerciseFlags.collectAsState()
    val favoriteNames by viewModel.favoriteNames.collectAsState()
    val categories = remember { ExerciseVideoCatalog.grouped.map { it.first } }
    // 选中分类：默认第一个；CUSTOM_SECTION 表示"我的自定义"。
    // 用 rememberSaveable：进详情页再返回时保持原分类，不重置回"热身"。
    var selectedCategory by rememberSaveable { mutableStateOf(if (initialCustom) CUSTOM_SECTION else categories.first()) }
    val selectingCustom = selectedCategory == CUSTOM_SECTION

    val q = query.trim()
    val searching = q.isNotEmpty()
    val hiddenNames = exerciseFlags.filterValues { it.contains(com.yuedong.fitness.data.repository.ItemFlags.HIDDEN) }.keys
    val pinnedNames = exerciseFlags.filterValues { it.contains(com.yuedong.fitness.data.repository.ItemFlags.PIN) }.keys

    // 当前显示列表的全部条目名（「全选」与计数用）
    val visibleNames: List<String> = when {
        searching -> ExerciseVideoCatalog.grouped.flatMap { it.second }
            .filter { it.contains(q, ignoreCase = true) && it !in hiddenNames } +
            customs.filter { it.name.contains(q, ignoreCase = true) && "hidden" !in (exerciseFlags[it.name] ?: emptySet()) }
                .map { it.name }
        selectedCategory == FAVORITE_SECTION -> favoriteNames.filter { it !in hiddenNames }
        selectingCustom -> customs.filter { "hidden" !in (exerciseFlags[it.name] ?: emptySet()) }.map { it.name }
        else -> ExerciseVideoCatalog.grouped
            .firstOrNull { it.first == selectedCategory }?.second.orEmpty()
            .filter { it !in hiddenNames }
    }

    // 系统返回：管理模式先退出管理
    if (managing) {
        androidx.activity.compose.BackHandler {
            managing = false
            selectedNames.clear()
        }
    }

    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 顶栏
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text("动作库", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        SearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "输入动作名字搜索",
            modifier = Modifier
                .padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(10.dp))

        if (searching) {
            // 搜索模式：跨分类平铺全部匹配项（内置 + 自定义）
            val matched = ExerciseVideoCatalog.grouped
                .flatMap { it.second }
                .filter { it.contains(q, ignoreCase = true) && it !in hiddenNames }
                .sortedByDescending { it in pinnedNames }
            val matchedCustoms = customs
                .filter { it.name.contains(q, ignoreCase = true) && "hidden" !in (exerciseFlags[it.name] ?: emptySet()) }
                .sortedByDescending { "pin" in (exerciseFlags[it.name] ?: emptySet()) }
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "search_custom_header") {
                    SectionHeader("我的自定义", onAdd = { onCreateCustom(query) })
                }
                if (matchedCustoms.isEmpty()) {
                    item(key = "search_custom_empty") {
                        EmptyHint("没有匹配“$q”的自定义动作")
                    }
                } else {
                    items(matchedCustoms, key = { "c_${it.name}" }) { entity ->
                        CustomExerciseRow(
                            entity,
                            onEdit = { onEditCustom(entity.name) },
                            onDelete = { viewModel.deleteCustom(entity.name) },
                            isFavorite = entity.name in favoriteNames,
                            onToggleFavorite = { viewModel.toggleFavorite(entity.name, entity.name in favoriteNames) },
                            onMenu = { menuExercise = entity.name to true },
                            selecting = managing,
                            selected = entity.name in selectedNames,
                            onLongPress = { longPressSelect(entity.name) },
                            onSelect = { toggleSelect(entity.name) }
                        )
                    }
                }
                item(key = "search_catalog_header") {
                    SectionHeader("内置动作库")
                }
                if (matched.isEmpty()) {
                    item(key = "search_catalog_empty") {
                        EmptyHint("没有匹配“$q”的内置动作")
                    }
                } else {
                    items(matched, key = { "e_$it" }) { name ->
                        ExerciseRow(
                            name,
                            onOpenExercise,
                            onMenu = { menuExercise = name to false },
                            selecting = managing,
                            selected = name in selectedNames,
                            onLongPress = { longPressSelect(name) },
                            onSelect = { toggleSelect(name) }
                        )
                    }
                }
            }
        } else {
            // 浏览模式：左分类栏 + 右列表（与食物热量库同款左右结构）
            Row(Modifier.fillMaxSize()) {
                CategoryRail(
                    categories = listOf(FAVORITE_SECTION, CUSTOM_SECTION) + categories,
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it }
                )
                LazyColumn(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedCategory == FAVORITE_SECTION) {
                        item(key = "fav_header") {
                            SectionHeader("我的收藏")
                        }
                        if (favoriteNames.isEmpty()) {
                            item(key = "fav_empty") {
                                EmptyHint("还没有收藏动作\n点动作行右侧 ⋮ 选「收藏」")
                            }
                        } else {
                            items(favoriteNames.filter { it !in hiddenNames }, key = { "f_$it" }) { name ->
                                ExerciseRow(
                                    name,
                                    onOpenExercise,
                                    onMenu = { menuExercise = name to false },
                                    selecting = managing,
                                    selected = name in selectedNames,
                                    onLongPress = { longPressSelect(name) },
                                    onSelect = { toggleSelect(name) }
                                )
                            }
                        }
                    } else if (selectingCustom) {
                        item(key = "custom_header") {
                            SectionHeader("我的自定义动作", onAdd = { onCreateCustom(query) })
                        }
                        if (customs.isEmpty()) {
                            item(key = "custom_empty") {
                                EmptyHint("还没有自定义动作\n点右上角「＋ 新建」添加自己常练的动作")
                            }
                        } else {
                            val customsVisible = customs
                                .filter { "hidden" !in (exerciseFlags[it.name] ?: emptySet()) }
                                .sortedByDescending { "pin" in (exerciseFlags[it.name] ?: emptySet()) }
                            if (customsVisible.isEmpty()) {
                                item(key = "custom_empty2") {
                                    EmptyHint("还没有自定义动作\n点右上角「＋ 新建」添加自己常练的动作")
                                }
                            } else {
                                items(customsVisible, key = { "c_${it.name}" }) { entity ->
                                    CustomExerciseRow(
                                        entity,
                                        onEdit = { onEditCustom(entity.name) },
                                        onDelete = { viewModel.deleteCustom(entity.name) },
                                        onMenu = { menuExercise = entity.name to true },
                                        selecting = managing,
                                        selected = entity.name in selectedNames,
                                        onLongPress = { longPressSelect(entity.name) },
                                        onSelect = { toggleSelect(entity.name) }
                                    )
                                }
                            }
                        }
                    } else {
                        val names = ExerciseVideoCatalog.grouped
                            .firstOrNull { it.first == selectedCategory }?.second.orEmpty()
                            .filter { it !in hiddenNames }
                            .sortedByDescending { it in pinnedNames }
                        item(key = "cat_header_$selectedCategory") {
                            SectionHeader("$selectedCategory")
                        }
                        items(names, key = { "e_$it" }) { name ->
                            ExerciseRow(
                                name,
                                onOpenExercise,
                                onMenu = { menuExercise = name to false },
                                selecting = managing,
                                selected = name in selectedNames,
                                onLongPress = { longPressSelect(name) },
                                onSelect = { toggleSelect(name) }
                            )
                        }
                    }
                }
            }
        }
    }

    // 批量管理底栏：勾选当前列表的多项，统一收藏 / 删除自定义
    if (managing) {
        com.yuedong.fitness.ui.components.BatchManageBar(
            selectedCount = selectedNames.size,
            totalCount = visibleNames.size,
            onToggleSelectAll = {
                if (visibleNames.isNotEmpty() && selectedNames.size == visibleNames.size) {
                    selectedNames.clear()
                } else {
                    selectedNames.clear()
                    selectedNames.addAll(visibleNames)
                }
            },
            onExit = {
                managing = false
                selectedNames.clear()
            },
            actions = listOf(
                com.yuedong.fitness.ui.components.BatchAction("收藏") {
                    val n = selectedNames.size
                    viewModel.setFavorites(selectedNames.toList(), true)
                    selectedNames.clear()
                    toastMessage = "已收藏 " + n + " 个动作"
                },
                com.yuedong.fitness.ui.components.BatchAction(
                    "删除",
                    MaterialTheme.colorScheme.error
                ) {
                    if (selectedNames.any { it in customs.map { c -> c.name } }) confirmDelete = true
                    else toastMessage = "内置动作不可删除"
                }
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // 批量删除二次确认（危险操作防误触）
    if (confirmDelete) {
        val targets = selectedNames.filter { it in customs.map { c -> c.name } }
        com.yuedong.fitness.ui.components.YueDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除选中的动作？") },
            text = { Text("将删除 " + targets.size + " 个自定义动作，删除后不可恢复；内置动作不受影响。") },
            confirmButton = {
                androidx.compose.material3.Button(onClick = {
                    viewModel.deleteCustoms(targets)
                    toastMessage = "已删除 " + targets.size + " 个动作"
                    confirmDelete = false
                    selectedNames.clear()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmDelete = false }) { Text("取消") }
            }
        )
    }

    com.yuedong.fitness.ui.components.YueToastHost(
        message = toastMessage,
        onDismiss = { toastMessage = null },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 24.dp)
    )

    menuExercise?.let { (name, isCustom) ->
        val fav = name in favoriteNames
        val pinned = "pin" in (exerciseFlags[name] ?: emptySet())
        ListActionSheet(
            visible = true,
            actions = listOf(
                com.yuedong.fitness.ui.components.ListAction(
                    androidx.compose.material.icons.Icons.Filled.PushPin,
                    if (pinned) "取消置顶" else "置顶",
                    MaterialTheme.colorScheme.primary
                ) { viewModel.setPinned(name, !pinned) },
                com.yuedong.fitness.ui.components.ListAction(
                    androidx.compose.material.icons.Icons.Filled.Favorite,
                    if (fav) "取消收藏" else "收藏",
                    MaterialTheme.colorScheme.primary
                ) { viewModel.toggleFavorite(name, fav) }
            ) + if (isCustom) {
                listOf(
                    com.yuedong.fitness.ui.components.ListAction(
                        androidx.compose.material.icons.Icons.Filled.Delete,
                        "删除动作",
                        MaterialTheme.colorScheme.error
                    ) { viewModel.deleteCustom(name) }
                )
            } else emptyList(),
            onDismiss = { menuExercise = null }
        )
    }

    } // Box

    deleteTarget?.let { name ->
        YueDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除自定义动作？") },
            text = { Text("将删除「$name」。已在计划中使用它的部分不受影响。此操作不可撤销。") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteCustom(name)
                    deleteTarget = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            }
        )
    }
}

/** 左侧分类栏（与 FoodLibraryScreen 同款交互） */
@Composable
private fun CategoryRail(categories: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(
        Modifier
            .width(CATEGORY_BAR_WIDTH)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(Modifier.fillMaxSize()) {
            items(categories, key = { it }) { category ->
                val isSelected = category == selected
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else androidx.compose.ui.graphics.Color.Transparent
                        )
                        .clickable { onSelect(category) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/** 右侧列表标题行 */
@Composable
private fun SectionHeader(title: String, onAdd: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (onAdd != null) {
            TextButton(onClick = onAdd) { Text("＋ 新建") }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    EmptyState(text)
}

/** 分类图标已统一为 FitnessCenter 占位（与动作选择页一致），此映射移除 */

/** 内置动作行：圆形图标 + 名称 + 推荐训练量，右侧难度等级（与食物行的热量对应） */
@Composable
private fun ExerciseRow(
    name: String,
    onOpenExercise: (String) -> Unit,
    onMenu: () -> Unit = {},
    selecting: Boolean = false,
    selected: Boolean = false,
    onSelect: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val volume = CommonExercises.find(name)
    val difficulty = ExerciseLibrary.find(name)?.difficulty
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (selecting) onSelect() else onOpenExercise(name) },
                onLongClick = { if (selecting) onSelect() else onLongPress() }
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selecting) {
                com.yuedong.fitness.ui.components.SelectionDot(selected)
                Spacer(Modifier.width(10.dp))
            }
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                val thumb = ExerciseVideoCatalog.thumbFor(name)
                if (thumb != null) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(thumb),
                        contentDescription = name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.large)
                    )
                } else {
                    Box(
                        Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.large),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.FitnessCenter,
                            contentDescription = name,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
                    volume?.let { "${it.sets} 组 × ${shortReps(it.reps)}" } ?: "视频教学",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = DiveMono,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                difficulty ?: "",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End
            )
            // 行尾「⋮」：长按菜单的可见入口
            if (!selecting) {
                com.yuedong.fitness.ui.components.RowMoreButton(
                    contentDescription = name + " 的操作",
                    onClick = onMenu
                )
            }

        }
    }
}

/** 自定义动作行：统一占位图标，点卡片编辑，行尾 ⋮ 操作 */
@Composable
private fun CustomExerciseRow(
    entity: CustomExerciseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMenu: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    selecting: Boolean = false,
    selected: Boolean = false,
    onSelect: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    // 点行进查看页（页内右上角「编辑」进入修改）；管理模式下点行=勾选；行尾「⋮」/长按弹操作菜单
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (selecting) onSelect() else onEdit() },
                onLongClick = { if (selecting) onSelect() else onLongPress() }
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selecting) {
                com.yuedong.fitness.ui.components.SelectionDot(selected)
                Spacer(Modifier.width(10.dp))
            }
            if (entity.coverPath != null) {
                com.yuedong.fitness.ui.components.LocalMediaImage(
                    path = entity.coverPath,
                    contentDescription = entity.name,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(MaterialTheme.shapes.large)
                )
            } else {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(com.yuedong.fitness.R.drawable.mascot_shiba),
                    contentDescription = entity.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(MaterialTheme.shapes.large)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entity.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${shortReps(entity.setsText)} 组 × ${shortReps(entity.repsOrDuration)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = DiveMono,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                entity.difficulty,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (!selecting) {
                com.yuedong.fitness.ui.components.RowMoreButton(
                    contentDescription = entity.name + " 的操作",
                    onClick = onMenu
                )
            }

        }
    }
}

/** 训练量文案归一化：去空格与「组/次」冗余，「每侧 15 次」→「15次/侧」，避免行内截断 */
private fun shortReps(reps: String): String {
    val compact = reps.replace(" ", "").replace("组", "").replace("次", "次")
    val perSide = Regex("每侧(\\d+次?)").find(compact)
    if (perSide != null) return perSide.groupValues[1] + "/侧"
    return compact.replace("每侧", "/侧")
}
