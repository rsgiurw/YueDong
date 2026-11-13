package com.yuedong.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yuedong.fitness.ui.components.YueDialog
import com.yuedong.fitness.data.db.entity.DiaryEntryEntity
import com.yuedong.fitness.ui.records.moodEmoji

/**
 * 今日日记弹窗（今日页与打卡日历页共用）：选心情 + 一句话，可留空心情文字。
 */
@Composable
fun DiaryDialog(
    existing: DiaryEntryEntity?,
    onDismiss: () -> Unit,
    onSave: (Int, String) -> Unit,
    title: String = "今日日记"
) {
    var mood by remember { mutableIntStateOf(existing?.mood ?: 0) }
    var content by remember { mutableStateOf(existing?.content ?: "") }

    YueDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("今天感觉怎么样？", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    (1..5).forEach { level ->
                        Box(
                            Modifier
                                .size(48.dp)
                                .background(
                                    if (mood == level) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    },
                                    CircleShape
                                )
                                .clickable { mood = level },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(moodEmoji(level), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    shape = MaterialTheme.shapes.small,
                    value = content,
                    onValueChange = { if (it.length <= 100) content = it },
                    placeholder = { Text("一句话记录今天的感受（可选）") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${content.length}/100",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(mood, content) },
                enabled = mood in 1..5
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
