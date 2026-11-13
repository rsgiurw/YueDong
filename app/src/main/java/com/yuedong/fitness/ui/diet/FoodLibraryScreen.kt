package com.yuedong.fitness.ui.diet

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.CustomFoodEntity
import com.yuedong.fitness.data.repository.DietLogRepository
import com.yuedong.fitness.domain.logic.FoodCatalog
import com.yuedong.fitness.ui.components.EmptyState
import com.yuedong.fitness.ui.components.ListActionSheet
import com.yuedong.fitness.ui.components.SearchField
import com.yuedong.fitness.ui.theme.DiveDataColors
import com.yuedong.fitness.ui.theme.DiveMono
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 食材库：食物热量库（分类浏览 + 搜索）+ 我的自定义食物管理（增删改查）。
 * 布局参考主流食物库：左侧分类栏 + 右侧圆形图标列表；
 * 与竞品差异点：每项都标出 蛋白质/碳水/脂肪，不只是热量。
 * 职责：浏览/搜索/新建/编辑/删除自定义食物；非职责：不记录饮食。
 */
class FoodLibraryViewModel(
    private val dietLogRepository: DietLogRepository,
    private val itemFlagRepository: com.yuedong.fitness.data.repository.ItemFlagRepository
) : ViewModel() {

    val customFoods: StateFlow<List<CustomFoodEntity>> = dietLogRepository.observeCustomFoods()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    /** 食物标记（置顶/收藏/隐藏）：name -> flags */
    val foodFlags: StateFlow<Map<String, Set<String>>> = itemFlagRepository.observeByType("food")
        .map { rows ->
            rows.groupBy({ it.itemName }, { it.flag })
                .mapValues { (_, flags) -> flags.toSet() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setFoodFlag(name: String, flag: String, on: Boolean) {
        viewModelScope.launch {
            if (on) itemFlagRepository.setFlag("food", name, flag)
            else itemFlagRepository.clearFlag("food", name, flag)
        }
    }

    /** 批量打/清标记（批量管理模式用） */
    fun setFoodsFlag(names: Collection<String>, flag: String, on: Boolean) {
        viewModelScope.launch {
            names.forEach { name ->
                if (on) itemFlagRepository.setFlag("food", name, flag)
                else itemFlagRepository.clearFlag("food", name, flag)
            }
        }
    }

    /** 批量删除自定义食物 */
    fun deleteCustomFoods(names: Collection<String>) {
        viewModelScope.launch { names.forEach { dietLogRepository.deleteCustomFood(it) } }
    }

    fun addCustomFood(name: String, kcal: Int, protein: Double, carb: Double, fat: Double, description: String = "") {
        viewModelScope.launch {
            dietLogRepository.upsertCustomFood(
                CustomFoodEntity(
                    name = name,
                    kcalPer100g = kcal,
                    proteinPer100g = protein,
                    carbPer100g = carb,
                    fatPer100g = fat,
                    description = description,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteCustomFood(name: String) {
        viewModelScope.launch { dietLogRepository.deleteCustomFood(name) }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as YueDongApplication
                FoodLibraryViewModel(app.container.dietLogRepository, app.container.itemFlagRepository)
            }
        }
    }
}

private const val FAVORITE_SECTION = "我的收藏"

/** 左侧分类栏宽度 */
private val CATEGORY_BAR_WIDTH = 96.dp

@Composable
fun FoodLibraryScreen(
    onBack: () -> Unit,
    initialCustom: Boolean = false,
    onOpenDetail: (String) -> Unit = {},
    onCreateCustomFood: () -> Unit = {},
    onEditCustomFood: (String) -> Unit = {},
    viewModel: FoodLibraryViewModel = viewModel(factory = FoodLibraryViewModel.factory())
) {
    var query by rememberSaveable { mutableStateOf("") }
    val customs by viewModel.customFoods.collectAsState()
    val foodFlags by viewModel.foodFlags.collectAsState()
    val favoriteFoods = foodFlags.filterValues { it.contains(com.yuedong.fitness.data.repository.ItemFlags.FAVORITE) }.keys
    val pinnedFoods = foodFlags.filterValues { it.contains(com.yuedong.fitness.data.repository.ItemFlags.PIN) }.keys
    var menuFood by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
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
    val categories = remember { FoodCatalog.grouped.map { it.first } }
    // 选中分类：默认第一个；CUSTOM_SECTION 表示"我的自定义"。
    // 用 rememberSaveable：进食物详情页再返回时保持原分类。
    var selectedCategory by rememberSaveable { mutableStateOf(if (initialCustom) CUSTOM_SECTION else categories.first()) }
    val selectingCustom = selectedCategory == CUSTOM_SECTION

    val q = query.trim()
    val searching = q.isNotEmpty()
    var detailCustom by remember { mutableStateOf<CustomFoodEntity?>(null) }
    val customNames = customs.map { it.name }

    // 当前显示列表的全部条目名（「全选」与计数用）
    val visibleNames: List<String> = when {
        searching -> FoodCatalog.all.filter { it.name.contains(q, ignoreCase = true) }.map { it.name } +
            customs.filter { it.name.contains(q, ignoreCase = true) }.map { it.name }
        selectedCategory == FAVORITE_SECTION ->
            FoodCatalog.all.filter { it.name in favoriteFoods }.map { it.name } +
                customs.filter { it.name in favoriteFoods }.map { it.name }
        selectingCustom -> customs.map { it.name }
        else -> FoodCatalog.grouped.firstOrNull { it.first == selectedCategory }?.second.orEmpty().map { it.name }
    }

    // 系统返回：管理模式先退出管理；自定义食物详情覆盖层先关闭
    if (managing) {
        androidx.activity.compose.BackHandler {
            managing = false
            selectedNames.clear()
        }
    }
    if (detailCustom != null) {
        androidx.activity.compose.BackHandler { detailCustom = null }
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
            Text("食物热量库", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        SearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "输入关键词搜索对应食物",
            modifier = Modifier
                .padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(10.dp))

        if (searching) {
            // 搜索模式：跨分类平铺全部匹配项（内置 + 自定义）
            val matched = FoodCatalog.all
                .filter { it.name.contains(q, ignoreCase = true) }
                .sortedByDescending { it.name in pinnedFoods }
            val matchedCustoms = customs
                .filter { it.name.contains(q, ignoreCase = true) }
                .sortedByDescending { it.name in pinnedFoods }
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "search_custom_header") {
                    SectionHeader("我的自定义", onAdd = { onCreateCustomFood() })
                }
                if (matchedCustoms.isEmpty()) {
                    item(key = "search_custom_empty") {
                        EmptyState("没有匹配“$q”的自定义食物")
                    }
                } else {
                    items(matchedCustoms, key = { "c_${it.name}" }) { food ->
                        CustomFoodRow(
                            food,
                            onOpenDetail = { detailCustom = food },
                            onDelete = { viewModel.deleteCustomFood(food.name) },
                            onMenu = { menuFood = food.name to true },
                            selecting = managing,
                            selected = food.name in selectedNames,
                            onSelect = { toggleSelect(food.name) },
                        onLongPress = { longPressSelect(food.name) }

                        )
                    }
                }
                item(key = "search_catalog_header") {
                    SectionHeader("内置食物库")
                }
                if (matched.isEmpty()) {
                    item(key = "search_catalog_empty") {
                        EmptyState("没有匹配“$q”的内置食物")
                    }
                } else {
                    items(matched, key = { "f_${it.name}" }) { food ->
                        CatalogFoodRow(
                            food,
                            onOpenDetail = onOpenDetail,
                            onMenu = { menuFood = food.name to false },
                            selecting = managing,
                            selected = food.name in selectedNames,
                            onSelect = { toggleSelect(food.name) },
                        onLongPress = { longPressSelect(food.name) }

                        )
                    }
                }
            }
        } else {
            // 浏览模式：左分类栏 + 右列表
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
                        val favBuiltins = FoodCatalog.all.filter { it.name in favoriteFoods }
                        val favCustoms = customs.filter { it.name in favoriteFoods }
                        if (favBuiltins.isEmpty() && favCustoms.isEmpty()) {
                            item(key = "fav_empty") {
                                EmptyState("还没有收藏食物\n点食物行右侧 ⋮ 选「收藏」")
                            }
                        } else {
                            favCustoms.forEach { food ->
                                item(key = "fav_c_${food.name}") {
                                    CustomFoodRow(
                                        food,
                                        onOpenDetail = { detailCustom = food },
                                        onDelete = { viewModel.deleteCustomFood(food.name) },
                                        onMenu = { menuFood = food.name to true },
                                        selecting = managing,
                                        selected = food.name in selectedNames,
                                        onSelect = { toggleSelect(food.name) },
                                    onLongPress = { longPressSelect(food.name) }

                                    )
                                }
                            }
                            favBuiltins.forEach { food ->
                                item(key = "fav_f_${food.name}") {
                                    CatalogFoodRow(
                            food,
                            onOpenDetail = onOpenDetail,
                            onMenu = { menuFood = food.name to false },
                            selecting = managing,
                            selected = food.name in selectedNames,
                            onSelect = { toggleSelect(food.name) },
                        onLongPress = { longPressSelect(food.name) }

                        )
                                }
                            }
                        }
                    } else if (selectingCustom) {
                        item(key = "custom_header") {
                            SectionHeader("我的自定义食物", onAdd = { onCreateCustomFood() })
                        }
                        if (customs.isEmpty()) {
                            item(key = "custom_empty") {
                                EmptyState("还没有自定义食物\n点右上角「＋ 新建」添加常吃的食物")
                            }
                        } else {
                            items(customs, key = { "c_${it.name}" }) { food ->
                                CustomFoodRow(
                                    food,
                                    onOpenDetail = { detailCustom = food },
                                    onDelete = { viewModel.deleteCustomFood(food.name) },
                                    onMenu = { menuFood = food.name to true },
                                    selecting = managing,
                                    selected = food.name in selectedNames,
                                    onSelect = { toggleSelect(food.name) },
                                onLongPress = { longPressSelect(food.name) }

                                )
                            }
                        }
                    } else {
                        val foods = FoodCatalog.grouped
                            .firstOrNull { it.first == selectedCategory }?.second.orEmpty()
                            .sortedByDescending { it.name in pinnedFoods }
                        item(key = "cat_header_$selectedCategory") {
                            SectionHeader("$selectedCategory")
                        }
                        items(foods, key = { "f_${it.name}" }) { food ->
                            CatalogFoodRow(
                            food,
                            onOpenDetail = onOpenDetail,
                            onMenu = { menuFood = food.name to false },
                            selecting = managing,
                            selected = food.name in selectedNames,
                            onSelect = { toggleSelect(food.name) },
                        onLongPress = { longPressSelect(food.name) }

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
                    viewModel.setFoodsFlag(
                        selectedNames.toList(),
                        com.yuedong.fitness.data.repository.ItemFlags.FAVORITE,
                        true
                    )
                    selectedNames.clear()
                    toastMessage = "已收藏 " + n + " 项食物"
                },
                com.yuedong.fitness.ui.components.BatchAction(
                    "删除",
                    MaterialTheme.colorScheme.error
                ) {
                    if (selectedNames.any { it in customNames }) confirmDelete = true
                    else toastMessage = "内置食物不可删除"
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
            title = { Text("删除选中的食物？") },
            text = { Text("将删除 " + targets.size + " 项自定义食物，删除后不可恢复；内置食物不受影响。") },
            confirmButton = {
                androidx.compose.material3.Button(onClick = {
                    viewModel.deleteCustomFoods(targets)
                    toastMessage = "已删除 " + targets.size + " 项食物"
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

    menuFood?.let { (name, isCustom) ->
        val fav = name in favoriteFoods
        val pinned = name in pinnedFoods
        ListActionSheet(
            visible = true,
            actions = buildList {
                add(
                    com.yuedong.fitness.ui.components.ListAction(
                        androidx.compose.material.icons.Icons.Filled.PushPin,
                        if (pinned) "取消置顶" else "置顶",
                        MaterialTheme.colorScheme.primary
                    ) { viewModel.setFoodFlag(name, com.yuedong.fitness.data.repository.ItemFlags.PIN, !pinned) }
                )
                add(
                    com.yuedong.fitness.ui.components.ListAction(
                        androidx.compose.material.icons.Icons.Filled.Favorite,
                        if (fav) "取消收藏" else "收藏",
                        MaterialTheme.colorScheme.primary
                    ) { viewModel.setFoodFlag(name, com.yuedong.fitness.data.repository.ItemFlags.FAVORITE, !fav) }
                )
                if (isCustom) {
                    add(
                        com.yuedong.fitness.ui.components.ListAction(
                            androidx.compose.material.icons.Icons.Filled.Delete,
                            "删除",
                            MaterialTheme.colorScheme.error
                        ) { viewModel.deleteCustomFood(name) }
                    )
                }
            },
            onDismiss = { menuFood = null }
        )
    }

    // 自定义食物详情：整页覆盖，内容按其营养值生成；顶栏铅笔进入编辑
    detailCustom?.let { food ->
        com.yuedong.fitness.ui.diet.FoodDetailScreen(
            name = food.name,
            onBack = { detailCustom = null },
            custom = food,
            onEdit = {
                detailCustom = null
                onEditCustomFood(food.name)
            }
        )
    }
    }
}

private const val CUSTOM_SECTION = "我的"

/** 左侧分类栏 */
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

/** 内置食物行：圆形图标 + 名称 + 热量 + 三大营养素（比竞品多给宏观营养） */
@Composable
private fun CatalogFoodRow(
    food: FoodCatalog.Food,
    onOpenDetail: (String) -> Unit = {},
    onMenu: () -> Unit = {},
    selecting: Boolean = false,
    selected: Boolean = false,
    onSelect: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (selecting) onSelect() else onOpenDetail(food.name) },
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
            val imgRes = foodImages[food.name]
            Box(
                Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (imgRes != null) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(imgRes),
                        contentDescription = food.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .graphicsLayer {
                                scaleX = 1.18f
                                scaleY = 1.18f
                            }
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = food.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    food.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(8.dp))
            val tone = when {
                food.kcal <= 120 -> DiveDataColors.levelLow
                food.kcal <= 300 -> DiveDataColors.levelMid
                else -> DiveDataColors.levelHigh
            }
            Text(
                "${food.kcal}\n千卡/100${food.unit}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = DiveMono,
                color = tone,
                textAlign = TextAlign.End,
                lineHeight = 16.sp
            )
            // 行尾「⋮」：长按菜单的可见入口
            if (!selecting) {
                com.yuedong.fitness.ui.components.RowMoreButton(
                    contentDescription = food.name + " 的操作",
                    onClick = onMenu
                )
            }
        }
    }
}

/** 自定义食物行：统一占位图标，点卡片看详情，行尾 ⋮ 操作 */
@Composable
private fun CustomFoodRow(
    food: CustomFoodEntity,
    onOpenDetail: (CustomFoodEntity) -> Unit,
    onDelete: () -> Unit,
    onMenu: () -> Unit = {},
    selecting: Boolean = false,
    selected: Boolean = false,
    onSelect: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    // 点行看详情（管理模式下点行=勾选）；行尾「⋮」/长按弹操作菜单
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (selecting) onSelect() else onOpenDetail(food) },
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
            Box(
                Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (food.imagePath != null) {
                    com.yuedong.fitness.ui.components.LocalMediaImage(
                        path = food.imagePath,
                        contentDescription = food.name,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                food.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            val tone = when {
                food.kcalPer100g <= 120 -> DiveDataColors.levelLow
                food.kcalPer100g <= 300 -> DiveDataColors.levelMid
                else -> DiveDataColors.levelHigh
            }
            Text(
                "${food.kcalPer100g}\n千卡/100克",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = DiveMono,
                color = tone,
                textAlign = TextAlign.End,
                lineHeight = 16.sp
            )
            if (!selecting) {
                com.yuedong.fitness.ui.components.RowMoreButton(
                    contentDescription = food.name + " 的操作",
                    onClick = onMenu
                )
            }
        }
    }
}
