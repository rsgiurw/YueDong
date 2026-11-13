package com.yuedong.fitness.ui.profile

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.repository.RemindersRepository
import com.yuedong.fitness.domain.extension.TrainingReminderWorker
import com.yuedong.fitness.ui.components.VerticalWheelPicker
import com.yuedong.fitness.ui.theme.DiveMono
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 编辑提醒:新建/编辑单条训练提醒(取消/完成 + 时分滚轮 + 启用开关 + 删除)。 */
@Composable
fun ReminderEditScreen(
    reminderId: Long,
    onBack: () -> Unit,
    viewModel: ReminderEditViewModel = viewModel(
        key = "reminder_edit_$reminderId",
        factory = ReminderEditViewModel.factory(reminderId)
    )
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // 权限被拒也照常保存并调度：条目保持开启，闹钟在无权限时静默跳过，
    // 用户日后到系统设置补授权即可生效；回调里用保存后回传的真实 id。
    var pendingScheduleId by remember { androidx.compose.runtime.mutableLongStateOf(-1L) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        TrainingReminderWorker.scheduleOne(context, pendingScheduleId, state.hour, state.minute, true)
    }
    // 精确闹钟权限：无授权时先用 1 分钟窗口闹钟兜底，并跳系统设置授权，下次保存即恢复精确
    fun requestExactAlarmIfNeeded(): Boolean {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) return false
        context.startActivity(
            android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .setData(android.net.Uri.fromParts("package", context.packageName, null))
        )
        return true
    }

    fun finishWithSave() {
        viewModel.save { id ->
            pendingScheduleId = id
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val exactDenied = state.enabled && requestExactAlarmIfNeeded()
            val granted = Build.VERSION.SDK_INT < 33 ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            when {
                !granted -> permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                else -> TrainingReminderWorker.scheduleOne(context, id, state.hour, state.minute, state.enabled)
            }
            // exactDenied 仅为触发授权页;闹钟已按窗口兜底排上,授权后下次保存自动精确
            exactDenied
        }
        onBack()
    }

    // 距离下次提醒的文案
    val now = Calendar.getInstance()
    val next = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, state.hour)
        set(Calendar.MINUTE, state.minute)
        set(Calendar.SECOND, 0)
        if (timeInMillis <= now.timeInMillis) add(Calendar.DATE, 1)
    }
    val diffMin = ((next.timeInMillis - now.timeInMillis) / 60000L).toInt()
    val nextText = when {
        !state.enabled -> "提醒未开启"
        diffMin >= 1440 -> "距离下次提醒还有 " + (diffMin / 1440) + " 天"
        diffMin >= 60 -> "距离下次提醒还有 " + (diffMin / 60) + " 小时 " + (diffMin % 60) + " 分钟"
        else -> "距离下次提醒还有 " + diffMin + " 分钟"
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 顶栏:取消 | 标题 | 完成
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "取消",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable { onBack() }
                    .padding(8.dp)
            )
            Text(
                if (state.isNew) "新建提醒" else "编辑提醒",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                "完成",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable { finishWithSave() }
                    .padding(8.dp)
            )
        }
        Text(
            nextText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            textAlign = TextAlign.Center
        )

        // 白卡:时 / 分 滚轮(两列居中;组件自带 240dp 视口,固定高会导致选中行偏移)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(Modifier.width(110.dp)) {
                VerticalWheelPicker(
                    values = (0..23).map { "%02d".format(it) },
                    selectedIndex = state.hour,
                    onSelected = { viewModel.setHour(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(32.dp))
            Column(Modifier.width(110.dp)) {
                VerticalWheelPicker(
                    values = (0..59).map { "%02d".format(it) },
                    selectedIndex = state.minute,
                    onSelected = { viewModel.setMinute(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 启用开关
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge)
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("启用提醒", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "到点通知打卡,当天已训练则不打扰",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = state.enabled,
                onCheckedChange = { viewModel.setEnabled(it) }
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "提醒为本地通知,不联网、不响铃;当天完成打卡则自动跳过",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

/** 编辑提醒 ViewModel:单条提醒的新建/编辑/删除 */
class ReminderEditViewModel(
    private val reminderId: Long,
    private val remindersRepository: RemindersRepository
) : ViewModel() {

    data class UiState(
        val id: Long = 0,
        val hour: Int = 20,
        val minute: Int = 0,
        val enabled: Boolean = true,
        val isNew: Boolean = true
    )

    private val _state = MutableStateFlow(UiState(id = reminderId, isNew = reminderId <= 0))
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        if (reminderId > 0) {
            viewModelScope.launch {
                remindersRepository.byId(reminderId)?.let { r ->
                    _state.value = UiState(
                        id = r.id,
                        hour = r.hour,
                        minute = r.minute,
                        enabled = r.enabled,
                        isNew = false
                    )
                }
            }
        } else {
            // 新建:默认时间为当前时间
            val now = java.util.Calendar.getInstance()
            _state.value = UiState(
                id = -1,
                hour = now.get(java.util.Calendar.HOUR_OF_DAY),
                minute = now.get(java.util.Calendar.MINUTE),
                enabled = true,
                isNew = true
            )
        }
    }

    fun setHour(hour: Int) = _state.update { it.copy(hour = hour) }
    fun setMinute(minute: Int) = _state.update { it.copy(minute = minute) }
    fun setEnabled(enabled: Boolean) = _state.update { it.copy(enabled = enabled) }

    /** 保存:新建插入 / 编辑更新;完成后回传真实 id（新建的 id 由数据库分配，页面用它调度通知任务） */
    fun save(onSaved: (Long) -> Unit = {}) {
        val s = _state.value
        viewModelScope.launch {
            val id = if (s.isNew) remindersRepository.add(s.hour, s.minute)
            else {
                remindersRepository.update(s.id, s.hour, s.minute)
                s.id
            }
            _state.update { it.copy(id = id, isNew = false) }
            onSaved(id)
        }
    }

    /** 删除条目(调用方负责取消对应通知任务) */
    fun delete(context: android.content.Context) {
        val id = _state.value.id
        viewModelScope.launch {
            remindersRepository.delete(id)
            TrainingReminderWorker.cancel(context, id)
        }
    }

    companion object {
        fun factory(reminderId: Long): androidx.lifecycle.ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as YueDongApplication
                    ReminderEditViewModel(reminderId, app.container.remindersRepository)
                }
            }
    }
}
