package com.yuedong.fitness.ui.profile

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import com.yuedong.fitness.data.db.entity.MemoEntity
import com.yuedong.fitness.ui.components.EmptyState
import com.yuedong.fitness.ui.components.YueDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 训练备忘录:自由笔记;删除进入回收站,可恢复。 */
@Composable
fun MemoScreen(
    onBack: () -> Unit,
    viewModel: MemoViewModel = viewModel(factory = MemoViewModel.factory())
) {
    val memos by viewModel.memos.collectAsState()
    var editing by remember { mutableStateOf<com.yuedong.fitness.data.db.entity.MemoEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    val timeFmt = remember { SimpleDateFormat("M月d日 HH:mm", Locale.CHINA) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                "训练备忘录",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                editing = com.yuedong.fitness.data.db.entity.MemoEntity(updatedAt = System.currentTimeMillis())
                showEditor = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "新建备忘", tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (memos.isEmpty()) {
            EmptyState("还没有备忘\n点右上角 + 记一条")
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(memos, key = { it.id }) { memo ->
                    SurfaceCard {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editing = memo
                                    showEditor = true
                                }
                                .padding(16.dp)
                        ) {
                            Text(
                                memo.content,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 4
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                timeFmt.format(Date(memo.updatedAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        val target = editing ?: com.yuedong.fitness.data.db.entity.MemoEntity(updatedAt = System.currentTimeMillis())
        var title by remember { mutableStateOf(target.title) }
        var content by remember { mutableStateOf(target.content) }
        YueDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (target.id == 0L) "新建备忘" else "编辑备忘", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    shape = MaterialTheme.shapes.small,
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("记点什么…") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Text(
                    "保存",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            if (content.isNotBlank()) {
                                if (target.id == 0L) viewModel.add("", content.trim())
                                else viewModel.update(target.id, "", content.trim())
                            }
                            showEditor = false
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            },
            dismissButton = {
                Text(
                    "取消",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clickable { showEditor = false }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        )
    }
}

/** 备忘录 ViewModel */
class MemoViewModel(
    private val repository: com.yuedong.fitness.data.repository.MemoRepository
) : androidx.lifecycle.ViewModel() {

    val memos = repository.observeAll()
        .stateIn(
            viewModelScope,
            kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun add(title: String, content: String) {
        viewModelScope.launch { repository.add(title, content) }
    }

    fun update(id: Long, title: String, content: String) {
        viewModelScope.launch { repository.update(id, title, content) }
    }

    companion object {
        fun factory(): androidx.lifecycle.ViewModelProvider.Factory =
            androidx.lifecycle.viewmodel.viewModelFactory {
                initializer {
                    val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as com.yuedong.fitness.YueDongApplication
                    MemoViewModel(app.container.memoRepository)
                }
            }
    }
}