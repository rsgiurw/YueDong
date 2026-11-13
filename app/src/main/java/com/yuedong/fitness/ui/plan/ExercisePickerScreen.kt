package com.yuedong.fitness.ui.plan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.CustomExerciseEntity
import com.yuedong.fitness.data.repository.CustomExerciseRepository
import com.yuedong.fitness.domain.logic.CommonExercises
import com.yuedong.fitness.ui.components.SearchField
import com.yuedong.fitness.ui.components.EmptyState
import com.yuedong.fitness.ui.exercise.ExerciseVideoCatalog
import com.yuedong.fitness.ui.theme.DiveMono
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 添加动作交换总线：选择页写入，编辑页在 ON_RESUME 时吸收。
 * 同一动作可重复添加（selected 里允许重复项）；用进程级单例而非导航结果回传。
 */
object ExercisePickBus {
    const val SOURCE_PLAN = "plan"
    const val SOURCE_DAY_DETAIL = "day_detail"

    var targetDayIndex: Int = -1
    var source: String = SOURCE_PLAN
    val selected = mutableStateListOf<String>()

    /** 只要有选中动作就算待处理(完成按钮可用) */
    val pending: Boolean get() = selected.isNotEmpty()

    fun start(dayIndex: Int) {
        targetDayIndex = dayIndex
        source = SOURCE_PLAN
        selected.clear()
    }

    fun startForDayDetail() {
        targetDayIndex = -1
        source = SOURCE_DAY_DETAIL
        selected.clear()
    }

    /** 点一次加一条，允许重复 */
    fun add(name: String) {
        selected.add(name)
    }

    /** 移除该动作的一个实例 */
    fun removeOne(name: String) {
        selected.remove(name)
    }

    fun clear() {
        targetDayIndex = -1
        selected.clear()
    }
}

class ExercisePickerViewModel(
    customExerciseRepository: CustomExerciseRepository,
    private val planRepository: com.yuedong.fitness.data.repository.PlanRepository,
    private val itemFlagRepository: com.yuedong.fitness.data.repository.ItemFlagRepository
) : ViewModel() {
    /** 内置动作名集合（判断「最近添加」里的名字是否仍真实存在于动作库） */
    private val catalogNames: Set<String> =
        ExerciseVideoCatalog.grouped.flatMap { it.second }.toSet()

    val exerciseFlags: StateFlow<Map<String, Set<String>>> =
        itemFlagRepository.observeByType("exercise")
            .map { rows ->
                rows.groupBy({ it.itemName }, { it.flag })
                    .mapValues { (_, flags) -> flags.toSet() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
    val customExercises: StateFlow<List<CustomExerciseEntity>> =
        customExerciseRepository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 最近添加：所有计划里最近排入的动作（按写入先后倒序，去重取前 12）。
     *  已删除（回收站）的自定义动作、已隐藏的内置动作不再出现——它们已不在动作库中。 */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recentExercises: StateFlow<List<String>> =
        planRepository.observePlans().flatMapLatest { plans ->
            if (plans.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                val nameFlow = combine(plans.map { planRepository.observeExercisesOfPlan(it.id) }) { lists ->
                    lists.flatMap { it }
                        .sortedByDescending { it.id }
                        .map { it.name }
                        .distinct()
                }
                kotlinx.coroutines.flow.combine(
                    nameFlow,
                    customExercises,
                    exerciseFlags
                ) { names, customs, flags ->
                    val hidden = flags
                        .filterValues { it.contains(com.yuedong.fitness.data.repository.ItemFlags.HIDDEN) }
                        .keys
                    val activeCustoms = customs.map { it.name }.toSet()
                    names.filter { n ->
                        n !in hidden && (n in activeCustoms || catalogNames.contains(n))
                    }.take(12)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                ExercisePickerViewModel(
                    app.container.customExerciseRepository,
                    app.container.planRepository,
                    app.container.itemFlagRepository
                )
            }
        }
    }
}

private fun recommendedHint(name: String): String {
    val rec = CommonExercises.find(name)
    return if (rec != null) "${rec.sets} 组 × ${rec.reps}" else "视频教学"
}


/**
 * 动作选择页（计划编辑专用）：
 * 左侧分类导航 + 右侧动作列表（与食材添加页同款布局）；
 * 点「＋」加一条（可重复添加），底部角标点开已添加清单，点「完成」一次性带回。
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerScreen(
    onBack: () -> Unit,
    /** 完成添加后关闭本页 */
    onDone: () -> Unit,
    /** 点击动作行 → 查看动作详情（添加仍走行尾「＋」） */
    onOpenExercise: (String) -> Unit = {},
    viewModel: ExercisePickerViewModel = viewModel(factory = ExercisePickerViewModel.factory())
) {
    val customs by viewModel.customExercises.collectAsState()
    val recent by viewModel.recentExercises.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    // 顶部标签：0 所有动作 / 1 最近添加（默认）/ 2 自定义
    var pickerTab by rememberSaveable { mutableIntStateOf(1) }
    var showPickedSheet by remember { mutableStateOf(false) }

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
            Column {
                Text(
                    "添加动作",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "点一次加一条，可重复添加；完成后点「完成」带回",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        SearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "输入动作名字搜索",
            modifier = Modifier
                .padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(6.dp))
        // 顶部标签：所有动作 / 最近添加 / 自定义（默认最近添加）
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TopPickerTab("所有动作", pickerTab == 0) { pickerTab = 0 }
            TopPickerTab("最近添加", pickerTab == 1) { pickerTab = 1 }
            TopPickerTab("自定义", pickerTab == 2) { pickerTab = 2 }
        }
        Spacer(Modifier.height(2.dp))

        if (query.isNotBlank() || pickerTab != 0) {
            // 搜索结果 / 最近添加 / 自定义：平铺列表
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val q = query.trim()
                when {
                    q.isNotBlank() -> {
                        val matchedCustoms = customs.filter { it.name.contains(q, ignoreCase = true) }
                        val matched = ExerciseVideoCatalog.grouped
                            .flatMap { it.second }
                            .filter { it.contains(q, ignoreCase = true) }
                        if (matchedCustoms.isNotEmpty()) {
                            item(key = "s_custom_header") { PickerSectionHeader("我的动作") }
                            matchedCustoms.forEach { entity ->
                                item(key = "s_c_${entity.name}") {
                                    PickerRow(
                                        name = entity.name,
                                        hint = "${entity.setsText} × ${entity.repsOrDuration}",
                                        addedCount = ExercisePickBus.selected.count { it == entity.name },
                                        onSelect = { ExercisePickBus.add(entity.name) },
                                        onOpenDetail = { onOpenExercise(entity.name) },
                                        onRemove = { ExercisePickBus.removeOne(entity.name) },
                                        coverPath = entity.coverPath
                                    )
                                }
                            }
                        }
                        item(key = "s_catalog_header") { PickerSectionHeader("内置动作库") }
                        if (matched.isEmpty()) {
                            item(key = "s_empty") { PickerEmptyHint("没有匹配“$q”的内置动作") }
                        } else {
                            matched.forEach { name ->
                                item(key = "s_e_$name") {
                                    PickerRow(
                                        name = name,
                                        hint = recommendedHint(name),
                                        addedCount = ExercisePickBus.selected.count { it == name },
                                        onSelect = { ExercisePickBus.add(name) },
                                        onOpenDetail = { onOpenExercise(name) },
                                        onRemove = { ExercisePickBus.removeOne(name) }
                                    )
                                }
                            }
                        }
                    }
                    pickerTab == 1 -> {
                        if (recent.isEmpty()) {
                            item(key = "recent_empty") {
                                PickerEmptyHint("还没有最近添加的动作\n去「所有动作」挑一个加进计划")
                            }
                        } else {
                            recent.forEach { name ->
                                item(key = "r_$name") {
                                    PickerRow(
                                        name = name,
                                        hint = recommendedHint(name),
                                        addedCount = ExercisePickBus.selected.count { it == name },
                                        onSelect = { ExercisePickBus.add(name) },
                                        onOpenDetail = { onOpenExercise(name) },
                                        onRemove = { ExercisePickBus.removeOne(name) }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        if (customs.isEmpty()) {
                            item(key = "tab_custom_empty") {
                                PickerEmptyHint("还没有自定义动作\n去动作库右上角「＋ 新建」添加")
                            }
                        } else {
                            customs.forEach { entity ->
                                item(key = "t_${entity.name}") {
                                    PickerRow(
                                        name = entity.name,
                                        hint = "${entity.setsText} × ${entity.repsOrDuration}",
                                        addedCount = ExercisePickBus.selected.count { it == entity.name },
                                        onSelect = { ExercisePickBus.add(entity.name) },
                                        onOpenDetail = { onOpenExercise(entity.name) },
                                        onRemove = { ExercisePickBus.removeOne(entity.name) },
                                        coverPath = entity.coverPath
                                    )
                                }
                            }
                        }
                    }
                }
                item(key = "bottom_gap") { Spacer(Modifier.height(12.dp)) }
            }
        } else {
            // 所有动作：左侧标签与右侧滚动双向联动（翻到哪个分类左侧高亮跟随，点左侧跳到该分类）
            val sections = remember(customs.size) {
                buildList {
                    if (customs.isNotEmpty()) add("我的动作" to customs.map { it.name })
                    ExerciseVideoCatalog.grouped.forEach { (category, names) -> add(category to names) }
                }
            }
            val listState = rememberLazyListState()
            // 每个分区的起始下标（标题占 1 格，后面跟条目）
            val sectionStarts = remember(sections) {
                var acc = 0
                sections.map { (label, names) -> label to acc.also { acc += 1 + names.size } }
            }
            val currentLabel by remember(sectionStarts) {
                derivedStateOf {
                    val i = listState.firstVisibleItemIndex
                    sectionStarts.lastOrNull { i >= it.second }?.first
                        ?: sections.firstOrNull()?.first.orEmpty()
                }
            }
            val scope = rememberCoroutineScope()
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    Modifier
                        .width(96.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    sections.forEach { (label, _) ->
                        val isSelected = currentLabel == label
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    scope.launch {
                                        listState.animateScrollToItem(
                                            sectionStarts.first { it.first == label }.second
                                        )
                                    }
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                LazyColumn(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    state = listState,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sections.forEach { (label, names) ->
                        item(key = "h_$label") { PickerSectionHeader(label) }
                        if (label == "我的动作") {
                            customs.forEach { entity ->
                                item(key = "m_${entity.name}") {
                                    PickerRow(
                                        name = entity.name,
                                        hint = "${entity.setsText} × ${entity.repsOrDuration}",
                                        addedCount = ExercisePickBus.selected.count { it == entity.name },
                                        onSelect = { ExercisePickBus.add(entity.name) },
                                        onOpenDetail = { onOpenExercise(entity.name) },
                                        onRemove = { ExercisePickBus.removeOne(entity.name) },
                                        coverPath = entity.coverPath
                                    )
                                }
                            }
                        } else {
                            names.forEach { name ->
                                item(key = "e2_$name") {
                                    PickerRow(
                                        name = name,
                                        hint = recommendedHint(name),
                                        addedCount = ExercisePickBus.selected.count { it == name },
                                        onSelect = { ExercisePickBus.add(name) },
                                        onOpenDetail = { onOpenExercise(name) },
                                        onRemove = { ExercisePickBus.removeOne(name) }
                                    )
                                }
                            }
                        }
                    }
                    item(key = "bottom_gap") { Spacer(Modifier.height(12.dp)) }
                }
            }
        }

        // 底部完成条：已添加角标（点开清单）+ 完成
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        onClick = { if (ExercisePickBus.selected.isNotEmpty()) showPickedSheet = true },
                        shape = MaterialTheme.shapes.medium,
                        color = if (ExercisePickBus.selected.isEmpty()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Box(Modifier.padding(12.dp)) {
                            Icon(
                                Icons.Filled.FitnessCenter,
                                contentDescription = "查看已添加动作",
                                tint = if (ExercisePickBus.selected.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    if (ExercisePickBus.selected.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                        ) {
                            Text(
                                ExercisePickBus.selected.size.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        if (ExercisePickBus.pending) onDone()
                    },
                    enabled = ExercisePickBus.selected.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("完成", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 已添加清单弹层：点角标展开，可就地移除
    if (showPickedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPickedSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    "已添加 " + ExercisePickBus.selected.size + " 条动作",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                val distinct = ExercisePickBus.selected.distinct()
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(distinct.size) { index ->
                        val name = distinct[index]
                        AddedRow(
                            name = name,
                            hint = recommendedHint(name),
                            count = ExercisePickBus.selected.count { it == name },
                            onRemove = { ExercisePickBus.removeOne(name) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PickerSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

/** 顶部标签（最近添加/所有动作/自定义）：文字 + 选中下划线，与食材添加页一致 */
@Composable
private fun TopPickerTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(3.dp))
        Box(
            Modifier
                .width(26.dp)
                .height(3.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    RoundedCornerShape(50)
                )
        )
    }
}

@Composable
private fun PickerEmptyHint(text: String) {
    EmptyState(text)
}

/** 动作行：缩略图 + 名称 + 推荐组次；行尾单个「＋」加一条（已加变实底），移除走角标清单 */
@Composable
private fun PickerRow(
    name: String,
    hint: String,
    addedCount: Int,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
    onOpenDetail: () -> Unit = {},
    coverPath: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetail),
        colors = CardDefaults.cardColors(
            containerColor = if (addedCount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (coverPath != null) {
                com.yuedong.fitness.ui.components.LocalMediaImage(
                    path = coverPath,
                    contentDescription = name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.large)
                )
            } else {
                val thumb = ExerciseVideoCatalog.thumbFor(name)
                if (thumb != null) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(thumb),
                        contentDescription = name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.large)
                    )
                } else {
                    // 自定义动作无封面：默认吉祥物封面
                    Image(
                        painter = androidx.compose.ui.res.painterResource(com.yuedong.fitness.R.drawable.mascot_shiba),
                        contentDescription = name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.large)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    hint,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = DiveMono,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // 单个「＋」：点一次加一条；已加的在底部角标清单里移除
            Surface(
                onClick = onSelect,
                shape = CircleShape,
                color = if (addedCount > 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Text(
                    "＋",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (addedCount > 0) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                )
            }
        }
    }
}

/** 已添加页签的行：名称 + 组次 + 移除一个实例 */
@Composable
private fun AddedRow(name: String, hint: String, count: Int, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val thumb = ExerciseVideoCatalog.thumbFor(name)
            if (thumb != null) {
                Image(
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    if (count > 1) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                "×$count",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DiveMono,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    hint,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = DiveMono,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "移除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
