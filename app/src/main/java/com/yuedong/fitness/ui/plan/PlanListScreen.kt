package com.yuedong.fitness.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.yuedong.fitness.ui.theme.diveDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.ui.components.EmptyState
import com.yuedong.fitness.ui.components.SearchField
import com.yuedong.fitness.ui.components.ListActionSheet
import com.yuedong.fitness.ui.components.SkeletonBox
import com.yuedong.fitness.ui.components.YueDialog
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.WorkoutPlanEntity
import com.yuedong.fitness.data.repository.CheckInRepository
import com.yuedong.fitness.data.repository.PlanRepository
import com.yuedong.fitness.data.repository.ProfileRepository
import com.yuedong.fitness.domain.logic.PlanRecommender
import com.yuedong.fitness.domain.model.Purpose
import com.yuedong.fitness.ui.theme.DiveDataColors
import com.yuedong.fitness.ui.theme.DiveGradients
import com.yuedong.fitness.ui.theme.DiveMenuAccents
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.util.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 计划列表卡片数据：把"计划 + 是否激活 + 推荐等级"组装成界面友好结构。
 * badge："为你推荐"（最匹配）/ "也适合"（备选）/ null（不标）。
 */
data class PlanCardUi(
    val plan: WorkoutPlanEntity,
    val isActive: Boolean,
    val badge: String?,
    val recommendReason: String?
)

/**
 * 计划列表（模块契约见设计包 §8 Plan）。
 * 职责：计划列表展示、推荐标记、切换激活计划、左划删除自定义计划；非职责：不做详情渲染与表单。
 */
class PlanListViewModel(
    private val planRepository: PlanRepository,
    profileRepository: ProfileRepository,
    private val checkInRepository: CheckInRepository,
    private val settings: com.yuedong.fitness.data.settings.SettingsDataStore,
    recommender: PlanRecommender
) : ViewModel() {

    val uiState: StateFlow<UiState<List<PlanCardUi>>> = combine(
        planRepository.observePlans(),
        planRepository.observeActivePlanId(),
        profileRepository.observeProfile()
    ) { plans, activeId, profile ->
        if (plans.isEmpty()) {
            UiState.Empty("内置计划正在准备中…")
        } else {
            val recs = profile?.let { recommender.recommendations(it, plans) } ?: emptyList()
            UiState.Success(
                plans.map { plan ->
                    val isActive = plan.id == activeId
                    val index = recs.indexOfFirst { it.planId == plan.id }
                    val badge = when {
                        isActive -> null
                        index == 0 -> "为你推荐"
                        index == 1 -> "也适合"
                        else -> null
                    }
                    PlanCardUi(
                        plan = plan,
                        isActive = isActive,
                        badge = badge,
                        recommendReason = if (index >= 0) recs[index].reason else null
                    )
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    /** 收藏的计划 id 集合 */
    val favoritePlanIds: StateFlow<Set<Long>> = planRepository.observePlans()
        .map { plans -> plans.filter { it.isFavorite }.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun togglePlanFavorite(planId: Long, currentlyFavorite: Boolean) {
        viewModelScope.launch { planRepository.setFavorite(planId, !currentlyFavorite) }
    }

    val activeProgress: StateFlow<Pair<Int, Int>?> = planRepository.observeActivePlanId()
        .flatMapLatest { activeId ->
            if (activeId == null) {
                flowOf(null)
            } else {
                combine(
                    planRepository.observePlanDays(activeId),
                    planRepository.observeActivePlan(),
                    checkInRepository.observeAll(),
                    settings.observeActivePlanStartedAt()
                ) { days, plan, checkIns, startedAt ->
                    // 总天数 = 虚拟周期（cycleDays>0）优先，否则模板天数
                    val total = plan?.cycleDays?.takeIf { it > 0 } ?: days.size
                    // 周期循环:推算今天所处的周期,done 只统计本周期窗口内的打卡天数
                    val startedDate = startedAt?.let {
                        java.time.Instant.ofEpochMilli(it)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    }
                    val today = com.yuedong.fitness.util.AppClock.today()
                    val done = if (startedDate == null || total <= 0) {
                        checkIns.filter { it.planId == activeId }
                            .map { it.date }.distinct().size
                    } else {
                        val elapsed = java.time.temporal.ChronoUnit.DAYS
                            .between(startedDate, today).coerceAtLeast(0)
                        val cycleStart = startedDate.plusDays(elapsed / total * total)
                        val cycleEnd = cycleStart.plusDays(total.toLong())
                        checkIns.filter { it.planId == activeId }
                            .map { it.date }
                            .filter { d ->
                                runCatching { java.time.LocalDate.parse(d) }.getOrNull()
                                    ?.let { !it.isBefore(cycleStart) && it.isBefore(cycleEnd) } == true
                            }
                            .distinct().size
                    }
                    done.coerceAtMost(total) to total
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setActive(planId: Long) {
        viewModelScope.launch { planRepository.setActivePlan(planId) }
    }

    fun setPinned(planId: Long, pinned: Boolean) {
        viewModelScope.launch { planRepository.setPinned(planId, pinned) }
    }

    /** 删除计划（内置/自建均可；当前使用中的计划不在列表内）；历史打卡保留（显示兜底名）。 */
    fun deletePlan(planId: Long) {
        viewModelScope.launch { planRepository.deleteAnyPlan(planId) }
    }

    /** 批量设置收藏（批量管理模式用）：favorite=true 全部加入收藏 */
    fun setPlanFavorites(ids: Collection<Long>, favorite: Boolean) {
        viewModelScope.launch { ids.forEach { togglePlanFavorite(it, !favorite) } }
    }

    /** 批量删除（列表内均为非当前计划） */
    fun deletePlans(ids: Collection<Long>) {
        viewModelScope.launch { ids.forEach { planRepository.deleteAnyPlan(it) } }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YueDongApplication
                PlanListViewModel(app.container.planRepository, app.container.profileRepository, app.container.checkInRepository, app.container.settings, app.container.planRecommender)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanListScreen(
    onOpenPlan: (Long) -> Unit,
    onCreatePlan: () -> Unit,
    onOpenLibrary: () -> Unit = {},
    viewModel: PlanListViewModel = viewModel(factory = PlanListViewModel.factory())
) {
    val state by viewModel.uiState.collectAsState()
    var pendingActivate by remember { mutableStateOf<PlanCardUi?>(null) }

    var selectedCategory by remember { mutableStateOf("全部") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var menuCard by remember { mutableStateOf<PlanCardUi?>(null) }
    // 批量管理模式：勾选列表里的多个计划，底部统一收藏/删除
    var managing by remember { mutableStateOf(false) }
    val selectedIds = remember { androidx.compose.runtime.mutableStateListOf<Long>() }
    var toastMessage by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var confirmDeletePlans by remember { androidx.compose.runtime.mutableStateOf(false) }
    var deleteTargets by remember { androidx.compose.runtime.mutableStateOf<List<Long>>(emptyList()) }
    // 长按 = 进入选择模式并选中该行
    fun longPressSelect(id: Long) {
        if (!managing) managing = true
        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
    }
    val favoritePlanIds by viewModel.favoritePlanIds.collectAsState()
    // 左侧分类导航：全部 / 自建 / 收藏 / 推荐 / 目的分类
    val railItems = listOf("全部", "收藏", "自建", "推荐", "减脂", "增肌", "塑形", "健康")

    // 系统返回：管理模式先退出管理
    if (managing) {
        androidx.activity.compose.BackHandler {
            managing = false
            selectedIds.clear()
        }
    }

    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 顶栏：标题 + 管理 + 新建
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "计划",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onCreatePlan,
                modifier = Modifier.height(40.dp),
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)
            ) {
                Text("＋ 自建计划", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))

        when (val s = state) {
            UiState.Loading -> Column(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonBox(Modifier.fillMaxWidth().height(170.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SkeletonBox(Modifier.weight(1f).height(84.dp))
                    SkeletonBox(Modifier.weight(1f).height(84.dp))
                    SkeletonBox(Modifier.weight(1f).height(84.dp))
                }
                SkeletonBox(Modifier.fillMaxWidth().height(120.dp))
                SkeletonBox(Modifier.fillMaxWidth().height(90.dp))
            }
            is UiState.Empty -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyState(s.message)
            }
            is UiState.Error -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyState(s.message)
            }
            is UiState.Success -> {
                val purposeByName = mapOf(
                    Purpose.FAT_LOSS.label to Purpose.FAT_LOSS,
                    Purpose.MUSCLE_GAIN.label to Purpose.MUSCLE_GAIN,
                    Purpose.TONE.label to Purpose.TONE,
                    Purpose.HEALTH.label to Purpose.HEALTH
                )
                val inCategory: (PlanCardUi) -> Boolean = { card ->
                    when (selectedCategory) {
                        "全部" -> true
                        "自建" -> card.plan.source == "CUSTOM"
                        "收藏" -> favoritePlanIds.contains(card.plan.id)
                        "推荐" -> card.plan.source == "BUILTIN"
                        else -> purposeByName[selectedCategory]?.name == card.plan.purposeTag
                    }
                }
                val activeCard = s.data.find { it.isActive }
                val progress by viewModel.activeProgress.collectAsState()
                val (doneDays, totalDays) = progress ?: (0 to 0)
                val q = searchQuery.trim()
                val searching = q.isNotEmpty()
                val others = s.data
                    .filter { !it.isActive }
                    .filter {
                        if (searching) it.plan.name.contains(q, ignoreCase = true)
                        else inCategory(it)
                    }
                    .sortedWith(
                        compareByDescending<PlanCardUi> { it.plan.isPinned }
                            .thenByDescending { it.badge == "为你推荐" }
                            .thenByDescending { it.badge == "也适合" }
                    )

                // 进行中 hero 卡
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    if (activeCard != null) {
                        ActivePlanHero(
                            card = activeCard,
                            doneDays = doneDays,
                            totalDays = totalDays,
                            onClick = { onOpenPlan(activeCard.plan.id) }
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(0.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(
                                "还没有激活计划，挑一套点「设为当前」开始吧",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                // 搜索
                SearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "搜索计划名称",
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))
                // 分类导航：收藏 / 自建 / 推荐 / 动作库（点已选中的回全部）
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickEntry(
                        Icons.Filled.Favorite, com.yuedong.fitness.ui.theme.DiveMenuAccents.orange, "收藏", Modifier.weight(1f),
                        selected = selectedCategory == "收藏"
                    ) { selectedCategory = if (selectedCategory == "收藏") "全部" else "收藏" }
                    QuickEntry(
                        Icons.Filled.Edit, com.yuedong.fitness.ui.theme.DiveMenuAccents.orange, "自建", Modifier.weight(1f),
                        selected = selectedCategory == "自建"
                    ) { selectedCategory = if (selectedCategory == "自建") "全部" else "自建" }
                    QuickEntry(
                        Icons.Filled.AutoAwesome, com.yuedong.fitness.ui.theme.DiveMenuAccents.blue, "推荐", Modifier.weight(1f),
                        selected = selectedCategory == "推荐"
                    ) { selectedCategory = if (selectedCategory == "推荐") "全部" else "推荐" }
                    QuickEntry(
                        Icons.Filled.FitnessCenter, com.yuedong.fitness.ui.theme.DiveMenuAccents.purple, "动作库", Modifier.weight(1f),
                        onClick = onOpenLibrary
                    )
                }
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
                ) {
                    // ---- 其他计划列表 ----
                    item {
                        Text(
                            if (searching) "搜索结果（${others.size}）" else "其他计划（${others.size}）",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (others.isEmpty()) {
                        item {
                            EmptyState(
                                if (searching) "没有匹配“$q”的计划" else "该分类下暂时没有计划"
                            )
                        }
                    }
                    items(others) { card ->
                        CompactPlanRow(
                            ui = card,
                            onOpen = { onOpenPlan(card.plan.id) },
                            onLongClick = { menuCard = card },
                            selecting = managing,
                            selected = card.plan.id in selectedIds,
                            onSelect = {
                                if (selectedIds.contains(card.plan.id)) selectedIds.remove(card.plan.id)
                                else selectedIds.add(card.plan.id)
                            },
                            onLongPress = {
                                if (!managing) managing = true
                                if (selectedIds.contains(card.plan.id)) selectedIds.remove(card.plan.id)
                                else selectedIds.add(card.plan.id)
                            }
                        )
                    }
                }

                // 批量管理底栏：勾选列表里的多个计划，统一收藏 / 删除自建
                if (managing) {
                    com.yuedong.fitness.ui.components.BatchManageBar(
                        selectedCount = selectedIds.size,
                        totalCount = others.size,
                        onToggleSelectAll = {
                            val ids = others.map { it.plan.id }
                            if (ids.isNotEmpty() && selectedIds.size == ids.size) {
                                selectedIds.clear()
                            } else {
                                selectedIds.clear()
                                selectedIds.addAll(ids)
                            }
                        },
                        onExit = {
                            managing = false
                            selectedIds.clear()
                        },
                        actions = listOf(
                            com.yuedong.fitness.ui.components.BatchAction("收藏") {
                                val n = selectedIds.size
                                viewModel.setPlanFavorites(selectedIds.toList(), true)
                                selectedIds.clear()
                                toastMessage = "已收藏 " + n + " 个计划"
                            },
                            com.yuedong.fitness.ui.components.BatchAction(
                                "删除",
                                MaterialTheme.colorScheme.error
                            ) {
                                val customs = selectedIds.filter { id ->
                                    others.firstOrNull { it.plan.id == id }?.plan?.source == "CUSTOM"
                                }
                                if (customs.isEmpty()) {
                                    toastMessage = "内置计划不可删除"
                                } else {
                                    deleteTargets = customs
                                    confirmDeletePlans = true
                                }
                            }
                        )
                    )
                }
            }
        }
    }

    // 批量删除二次确认（危险操作防误触）
    if (confirmDeletePlans) {
        com.yuedong.fitness.ui.components.YueDialog(
            onDismissRequest = { confirmDeletePlans = false },
            title = { Text("删除选中的计划？") },
            text = { Text("将删除选中的自建计划，删除后不可恢复；内置计划不受影响。") },
            confirmButton = {
                androidx.compose.material3.Button(onClick = {
                    viewModel.deletePlans(deleteTargets)
                    toastMessage = "已删除 " + deleteTargets.size + " 个计划"
                    selectedIds.clear()
                    confirmDeletePlans = false
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmDeletePlans = false }) { Text("取消") }
            }
        )
    }

    // 胶囊轻提示
    com.yuedong.fitness.ui.components.YueToastHost(
        message = toastMessage,
        onDismiss = { toastMessage = null },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 24.dp)
    )
    }

    menuCard?.let { card ->
        val fav = favoritePlanIds.contains(card.plan.id)
        ListActionSheet(
            visible = true,
            actions = buildList {
                add(
                    com.yuedong.fitness.ui.components.ListAction(
                        Icons.Filled.CheckCircle,
                        "设为当前",
                        MaterialTheme.colorScheme.primary
                    ) { viewModel.setActive(card.plan.id) }
                )
                add(
                    com.yuedong.fitness.ui.components.ListAction(
                        Icons.Filled.Favorite,
                        if (fav) "取消收藏" else "收藏",
                        MaterialTheme.colorScheme.primary
                    ) { viewModel.togglePlanFavorite(card.plan.id, fav) }
                )
                add(
                    com.yuedong.fitness.ui.components.ListAction(
                        Icons.Filled.PushPin,
                        if (card.plan.isPinned) "取消置顶" else "置顶",
                        MaterialTheme.colorScheme.primary
                    ) { viewModel.setPinned(card.plan.id, !card.plan.isPinned) }
                )
                add(
                    com.yuedong.fitness.ui.components.ListAction(
                        Icons.Filled.Delete,
                        "删除计划",
                        MaterialTheme.colorScheme.error
                    ) { viewModel.deletePlan(card.plan.id) }
                )
            },
            onDismiss = { menuCard = null }
        )
    }

    if (pendingActivate != null) {
        val target = pendingActivate!!
        YueDialog(
            onDismissRequest = { pendingActivate = null },
            title = { Text("切换到「${target.plan.name}」？") },
            text = {
                Text(
                    "新计划将从今天起按第 1 天重新计算训练日程；" +
                        "过去的打卡与体重记录会一直保留在记录页。"
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setActive(target.plan.id)
                    pendingActivate = null
                }) { Text("切换") }
            },
            dismissButton = {
                TextButton(onClick = { pendingActivate = null }) { Text("取消") }
            }
        )
    }

}

/** 列表数据源与空态提示已收敛到组合处计算，无需辅助函数。 */

/** 进行中 hero 卡：徽章 + 计划名/周期 + 还剩天数大字 + 进度条（5/7）+ 右侧手写体寄语 */
@Composable
private fun ActivePlanHero(
    card: PlanCardUi,
    doneDays: Int,
    totalDays: Int,
    onClick: () -> Unit
) {
    val completed = totalDays > 0 && doneDays >= totalDays
    val progressFraction = if (totalDays > 0) {
        (doneDays.toFloat() / totalDays).coerceIn(0f, 1f)
    } else 0f
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                DiveGradients.depthBand(diveDark()),
                MaterialTheme.shapes.large
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        card.plan.name + " / " + (card.plan.cycleDays.takeIf { it > 0 } ?: totalDays.takeIf { it > 0 } ?: 7) + "天",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // 徽章：🌱 进行中 / ✓ 周期完成
                    Text(
                        if (completed) "✓ 周期完成" else "🌱 进行中",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (completed) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .background(
                                (if (completed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onPrimaryContainer)
                                    .copy(alpha = if (completed) 0.18f else 0.14f),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        if (completed) "周期完成" else "已完成周期 还剩",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (!completed) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${totalDays - doneDays}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = DiveMono,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "天",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (completed) "保持节奏，太棒了" else "按计划推进就好",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (completed) MaterialTheme.colorScheme.tertiary else DiveDataColors.levelLow,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f),
                        drawStopIndicator = {}
                    )
                }
            }
        }
    }
}

/** 快捷入口：彩色圆角方块图标 + 文字（自建计划/动作库/收藏/减脂搜索） */

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
/** 快捷入口：彩色圆角方块图标 + 文字（收藏/自建/推荐/动作库） */
@Composable
private fun QuickEntry(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    title: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(64.dp)
                .clip(MaterialTheme.shapes.large)
                .background(accent.copy(alpha = 0.14f))
                .border(
                    if (selected) 2.dp else 0.dp,
                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    MaterialTheme.shapes.large
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CompactPlanRow(
    ui: PlanCardUi,
    onOpen: () -> Unit,
    onLongClick: () -> Unit,
    selecting: Boolean = false,
    selected: Boolean = false,
    onSelect: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (selecting) onSelect() else onOpen() },
                onLongClick = { if (!selecting) onLongPress() }
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selecting) {
                com.yuedong.fitness.ui.components.SelectionDot(selected)
                Spacer(Modifier.width(10.dp))
            }
            val (iconRes, tint) = when {
                ui.plan.source == "CUSTOM" -> Icons.Filled.Edit to MaterialTheme.colorScheme.onSurfaceVariant
                ui.plan.purposeTag == "FAT_LOSS" -> Icons.Filled.Whatshot to DiveDataColors.levelMid
                ui.plan.purposeTag == "MUSCLE_GAIN" -> Icons.Filled.FitnessCenter to MaterialTheme.colorScheme.primary
                ui.plan.purposeTag == "TONE" -> Icons.Filled.SelfImprovement to com.yuedong.fitness.ui.theme.DiveMenuAccents.purple
                else -> Icons.Filled.Favorite to com.yuedong.fitness.ui.theme.DiveMenuAccents.green
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconRes, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        ui.plan.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        color = when (ui.plan.purposeTag) {
                            "FAT_LOSS" -> DiveDataColors.levelMid.copy(alpha = 0.18f)
                            "MUSCLE_GAIN" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            "TONE" -> com.yuedong.fitness.ui.theme.DiveMenuAccents.purple.copy(alpha = 0.16f)
                            else -> com.yuedong.fitness.ui.theme.DiveMenuAccents.green.copy(alpha = 0.16f)
                        },
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            when (ui.plan.purposeTag) {
                                "FAT_LOSS" -> "减脂"
                                "MUSCLE_GAIN" -> "增肌"
                                "TONE" -> "塑形"
                                else -> "健康"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (ui.plan.purposeTag) {
                                "FAT_LOSS" -> DiveDataColors.levelMid
                                "MUSCLE_GAIN" -> MaterialTheme.colorScheme.primary
                                else -> com.yuedong.fitness.ui.theme.DiveMenuAccents.green
                            },
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (ui.badge != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                ui.badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 5.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        "周期 " + (if (ui.plan.cycleDays > 0) ui.plan.cycleDays else 7) + " 天 · 每周 " + ui.plan.daysPerWeek + " 天",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = DiveMono,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            // 行尾「⋮」：设为当前 / 收藏 / 置顶 / 删除（管理模式下隐藏，避免误触）
            if (!selecting) {
                com.yuedong.fitness.ui.components.RowMoreButton(
                    contentDescription = ui.plan.name + " 的操作",
                    onClick = onLongClick
                )
            }

        }
    }
}
