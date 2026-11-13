package com.yuedong.fitness.ui.profile

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.yuedong.fitness.ui.theme.diveDark
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewModelScope
import com.yuedong.fitness.data.db.entity.UserProfileEntity
import com.yuedong.fitness.domain.model.Gender
import com.yuedong.fitness.domain.model.Purpose
import com.yuedong.fitness.domain.model.TargetProgressUi
import com.yuedong.fitness.data.repository.PlanRepository
import com.yuedong.fitness.data.repository.BodyMetricRepository
import com.yuedong.fitness.R
import com.yuedong.fitness.ui.components.BmiPreviewCard
import com.yuedong.fitness.ui.components.LocalMediaImage
import com.yuedong.fitness.ui.components.SkeletonBox
import com.yuedong.fitness.ui.theme.DiveDataColors
import com.yuedong.fitness.ui.theme.DiveGradients
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.util.AppClock
import java.util.Locale
import kotlin.math.roundToInt

/** 我的页（模块契约见设计包 §8 Profile）。 */
@Composable
fun ProfileScreen(
    onGoPlan: () -> Unit,
    onEditProfile: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.factory())
) {
    val state by viewModel.uiState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.isLoading) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SkeletonBox(Modifier.fillMaxWidth().height(190.dp))
                    SkeletonBox(Modifier.fillMaxWidth().height(150.dp))
                    SkeletonBox(Modifier.fillMaxWidth().height(110.dp))
                }
            }
            return@LazyColumn
        }
        item {
            // 主卡:用户区 + 统计(heroGlow 渐变,白字)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(com.yuedong.fitness.ui.theme.DiveGradients.heroGlow(diveDark()), MaterialTheme.shapes.large)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(68.dp).clip(CircleShape)) {
                        val profile = state.profile
                        if (profile?.avatarPath != null) {
                            LocalMediaImage(
                                path = profile.avatarPath,
                                contentDescription = "头像",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(
                                    if (profile?.gender == "FEMALE") R.drawable.avatar_default_female
                                    else R.drawable.avatar_default_male
                                ),
                                contentDescription = "默认头像",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            state.profile?.nickname ?: "悦动用户",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(3.dp))
                        val profile = state.profile
                        Text(
                            listOfNotNull(
                                profile?.let { Gender.fromValue(it.gender)?.label ?: it.gender },
                                profile?.let { "${it.age}岁" },
                                profile?.let { "${it.heightCm}cm" }
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onEditProfile() }
                    ) {
                        Text(
                            "修改资料",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f))
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    StatCellLight(state.streak.toString(), "天", "连续打卡", Modifier.weight(1f))
                    StatCellLight(state.totalDays.toString(), "天", "累计打卡", Modifier.weight(1f))
                    StatCellLight(
                        state.effectiveWeightKg?.let { String.format(Locale.CHINA, "%.1f", it) } ?: "--",
                        "kg",
                        state.profile?.let { "目标 " + String.format(Locale.CHINA, "%.0f", it.targetWeightKg) + " kg" } ?: "目标体重",
                        Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            // 外观主题:直接在主页面切换,不进二级页
            GroupBlock {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.DarkMode, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        "外观主题",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.material3.FilterChip(
                        selected = themeMode != "dark",
                        onClick = { viewModel.setThemeMode("light") },
                        label = { Text("浅色") }
                    )
                    Spacer(Modifier.width(8.dp))
                    androidx.compose.material3.FilterChip(
                        selected = themeMode == "dark",
                        onClick = { viewModel.setThemeMode("dark") },
                        label = { Text("深色") }
                    )
                }
            }
        }
        item {
            GroupBlock {
                RowLine(
                    Icons.Filled.PhotoLibrary, "身材照片墙",
                    value = "记录对比照", accent = com.yuedong.fitness.ui.theme.DiveMenuAccents.blue
                ) {
                    onNavigate(com.yuedong.fitness.navigation.Routes.PHOTO_WALL)
                }
                RowLine(
                    Icons.Filled.Edit, "训练备忘录",
                    value = "随手记想法", accent = com.yuedong.fitness.ui.theme.DiveMenuAccents.purple
                ) {
                    onNavigate(com.yuedong.fitness.navigation.Routes.MEMO_NOTES)
                }
                RowLine(
                    Icons.Filled.Delete, "回收站",
                    value = "已删除内容可恢复", accent = com.yuedong.fitness.ui.theme.DiveMenuAccents.orange,
                    showDivider = false
                ) {
                    onNavigate(com.yuedong.fitness.navigation.Routes.RECYCLE_BIN)
                }
            }
        }
        item {
            GroupBlock {
                RowLine(
                    Icons.Filled.School, "新手指南",
                    value = "三分钟上手", accent = com.yuedong.fitness.ui.theme.DiveMenuAccents.green
                ) {
                    onNavigate(com.yuedong.fitness.navigation.Routes.GUIDE)
                }
                RowLine(
                    Icons.Filled.Backup, "数据与备份",
                    value = "导出 / 导入", accent = com.yuedong.fitness.ui.theme.DiveMenuAccents.blue
                ) {
                    onNavigate(com.yuedong.fitness.navigation.Routes.BACKUP)
                }
                RowLine(
                    Icons.Filled.Notifications, "训练提醒",
                    value = "每日定时通知", accent = com.yuedong.fitness.ui.theme.DiveMenuAccents.purple
                ) {
                    onNavigate(com.yuedong.fitness.navigation.Routes.REMINDER)
                }
                RowLine(
                    Icons.Filled.Info, "关于悦动",
                    value = "版本与数据说明", accent = com.yuedong.fitness.ui.theme.DiveMenuAccents.green
                ) {
                    onNavigate(com.yuedong.fitness.navigation.Routes.ABOUT)
                }
            }
        }
    }
}

/** 卡片通用容器：surface 底、无阴影、16dp 圆角 */
@Composable
internal fun SurfaceCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        content()
    }
}

private fun fmt1(v: Double): String = String.format(Locale.CHINA, "%.1f", v)

/** 我的页界面状态 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: com.yuedong.fitness.data.db.entity.UserProfileEntity? = null,
    val bmi: Double = 0.0,
    val bmiCategory: com.yuedong.fitness.domain.model.BmiCategory = com.yuedong.fitness.domain.model.BmiCategory.NORMAL,
    val activePlanName: String? = null,
    val activePlanId: Long = 0L,
    val streak: Int = 0,
    val totalDays: Int = 0,
    val customFoodCount: Int = 0,
    val customExerciseCount: Int = 0,
    val progress: com.yuedong.fitness.domain.model.TargetProgressUi? = null,
    val hasAnyWeight: Boolean = false,
    val effectiveWeightKg: Double? = null
)

/** 我的页 ViewModel：资料 + BMI + 目标进度 + 打卡统计 + 主题偏好 */
class ProfileViewModel(
    profileRepository: com.yuedong.fitness.data.repository.ProfileRepository,
    planRepository: com.yuedong.fitness.data.repository.PlanRepository,
    bodyMetricRepository: com.yuedong.fitness.data.repository.BodyMetricRepository,
    checkInRepository: com.yuedong.fitness.data.repository.CheckInRepository,
    dietLogRepository: com.yuedong.fitness.data.repository.DietLogRepository,
    customExerciseRepository: com.yuedong.fitness.data.repository.CustomExerciseRepository,
    private val settings: com.yuedong.fitness.data.settings.SettingsDataStore
) : androidx.lifecycle.ViewModel() {

    val themeMode: kotlinx.coroutines.flow.StateFlow<String> = settings.observeThemeMode()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), "light")

    val customFoodCount: kotlinx.coroutines.flow.StateFlow<Int> = dietLogRepository.observeCustomFoods()
        .map { it.size }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), 0)

    val customExerciseCount: kotlinx.coroutines.flow.StateFlow<Int> = customExerciseRepository.observeAll()
        .map { it.size }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), 0)

    val reminderEnabled: kotlinx.coroutines.flow.StateFlow<Boolean> = settings.observeReminderEnabled()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), false)

    val reminderTime: kotlinx.coroutines.flow.StateFlow<Pair<Int, Int>> = settings.observeReminderTime()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), 20 to 0)

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { settings.setReminderEnabled(enabled) }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch { settings.setReminderTime(hour, minute) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    val uiState: kotlinx.coroutines.flow.StateFlow<ProfileUiState> = combine(
        profileRepository.observeProfile(),
        planRepository.observeActivePlan(),
        bodyMetricRepository.observeAll(),
        checkInRepository.observeAllDates(),
        settings.observeActivePlanId()
    ) { profile, activePlan, weights, checkinDates, activePlanId ->
        if (profile == null) {
            ProfileUiState(isLoading = false)
        } else {
            val sortedWeights = weights.sortedBy { runCatching { java.time.LocalDate.parse(it.date) }.getOrNull() ?: java.time.LocalDate.MIN }
            val effectiveWeight = sortedWeights.lastOrNull()?.weightKg ?: profile.currentWeightKg
            // 起点 = 最早一条体重记录；无记录时用资料体重兜底（此时已动 = 0）
            val startWeight = sortedWeights.firstOrNull()?.weightKg ?: profile.currentWeightKg
            val doneKg = kotlin.math.abs(effectiveWeight - startWeight)
            val remainingKg = kotlin.math.abs(profile.targetWeightKg - effectiveWeight)
            val total = doneKg + remainingKg
            ProfileUiState(
                isLoading = false,
                profile = profile,
                activePlanId = activePlanId ?: 0L,
                streak = com.yuedong.fitness.domain.logic.FitnessCalculator.streak(
                    checkinDates.mapNotNull { runCatching { java.time.LocalDate.parse(it) }.getOrNull() },
                    AppClock.today()
                ),
                totalDays = checkinDates.size,
                bmi = com.yuedong.fitness.domain.logic.FitnessCalculator.metrics(
                    com.yuedong.fitness.domain.model.FitnessInput(
                        gender = com.yuedong.fitness.domain.model.Gender.fromValue(profile.gender) ?: com.yuedong.fitness.domain.model.Gender.MALE,
                        age = profile.age,
                        heightCm = profile.heightCm,
                        weightKg = effectiveWeight,
                        weeklyExerciseDays = profile.weeklyExerciseDays,
                        purpose = com.yuedong.fitness.domain.model.Purpose.fromValue(profile.purpose) ?: com.yuedong.fitness.domain.model.Purpose.HEALTH
                    )
                ).bmi,
                activePlanName = activePlan?.name,
                hasAnyWeight = weights.isNotEmpty(),
                effectiveWeightKg = effectiveWeight,
                progress = TargetProgressUi(
                    startWeightKg = startWeight,
                    currentWeightKg = effectiveWeight,
                    targetWeightKg = profile.targetWeightKg,
                    doneKg = doneKg,
                    remainingKg = remainingKg,
                    progress = if (total > 1e-9) (doneKg / total).toFloat() else 1f
                )
            )
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    companion object {
        fun factory(): androidx.lifecycle.ViewModelProvider.Factory = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer {
                val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as com.yuedong.fitness.YueDongApplication
                ProfileViewModel(
                    app.container.profileRepository,
                    app.container.planRepository,
                    app.container.bodyMetricRepository,
                    app.container.checkInRepository,
                    app.container.dietLogRepository,
                    app.container.customExerciseRepository,
                    app.container.settings
                )
            }
        }
    }
}
