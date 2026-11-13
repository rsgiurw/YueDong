package com.yuedong.fitness.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.yuedong.fitness.ui.components.BackHeader
import com.yuedong.fitness.ui.theme.DiveNumeric
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuedong.fitness.domain.extension.TrainingReminderWorker
import com.yuedong.fitness.ui.components.LocalMediaImage
import com.yuedong.fitness.ui.components.YueDialog
import com.yuedong.fitness.ui.theme.DiveMono
import kotlinx.coroutines.flow.stateIn
import android.content.Context
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer



/** 训练提醒列表:仿系统闹钟。空 = 插画 + 无提醒 + 大加号;已有 = 时间行 + 开关,点击行进入编辑。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReminderListScreen(
    onBack: () -> Unit,
    onOpenEdit: (Long) -> Unit,
    viewModel: RemindersListViewModel = viewModel(factory = RemindersListViewModel.factory())
) {
    val context = LocalContext.current
    val reminders by viewModel.reminders.collectAsState()
    var deleteTarget by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        BackHeader(title = "训练提醒", onBack = onBack)

        if (reminders.isEmpty()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LocalMediaImage(
                        path = null,
                        contentDescription = null,
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("无提醒", style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                reminders.forEach { r ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
                            .combinedClickable(
                                onClick = { onOpenEdit(r.id) },
                                onLongClick = { deleteTarget = r.id }
                            )
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "%02d:%02d".format(r.hour, r.minute),
                                fontSize = 40.sp,
                                lineHeight = 44.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DiveMono
                            )
                            Text(
                                "每天 · 训练提醒",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = r.enabled,
                            onCheckedChange = { want ->
                                viewModel.setEnabled(context, r.id, r.hour, r.minute, want)
                            }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // 底部居中大加号:新建提醒
        Box(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(76.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { onOpenEdit(-1L) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "添加提醒",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // 长按删除确认
        deleteTarget?.let { id ->
            YueDialog(
                onDismissRequest = { deleteTarget = null },
                title = { Text("删除此提醒?", fontWeight = FontWeight.Bold) },
                text = { Text("删除后将不再收到该时间的训练提醒。") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.delete(context, id)
                        deleteTarget = null
                    }) {
                        Text("删除", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteTarget = null }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

/** 提醒列表 ViewModel */
class RemindersListViewModel(
    private val remindersRepository: com.yuedong.fitness.data.repository.RemindersRepository
) : androidx.lifecycle.ViewModel() {

    val reminders = remindersRepository.observeAll()
        .stateIn(
            viewModelScope,
            kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun setEnabled(context: Context, id: Long, hour: Int, minute: Int, enabled: Boolean) {
        viewModelScope.launch {
            remindersRepository.setEnabled(id, enabled)
            TrainingReminderWorker.scheduleOne(context, id, hour, minute, enabled)
        }
    }

    fun delete(context: Context, id: Long) {
        viewModelScope.launch {
            remindersRepository.delete(id)
            TrainingReminderWorker.cancel(context, id)
        }
    }

    companion object {
        fun factory(): androidx.lifecycle.ViewModelProvider.Factory =
            androidx.lifecycle.viewmodel.viewModelFactory {
                initializer {
                    val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as com.yuedong.fitness.YueDongApplication
                    RemindersListViewModel(app.container.remindersRepository)
                }
            }
    }
}
