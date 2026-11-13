package com.yuedong.fitness.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 行尾「⋮」按钮：长按才有的收藏/置顶/删除等操作给出可见入口（可发现性），
 * 点击后弹出的仍是统一的 ListActionSheet，与长按行为一致。
 */
@Composable
fun RowMoreButton(contentDescription: String = "更多操作", onClick: () -> Unit) {
    // 不压制尺寸：IconButton 默认 48dp 命中区（此前 size(30.dp) 热区不达标）
    IconButton(onClick = onClick) {
        Icon(
            Icons.Filled.MoreVert,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** 批量管理模式下，列表行首的选中圈 */
@Composable
fun SelectionDot(selected: Boolean) {
    if (selected) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = "已选",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
    } else {
        Icon(
            Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.size(22.dp)
        )
    }
}

/** 批量管理底栏的一个操作按钮（如「收藏」「删除」） */
data class BatchAction(
    val label: String,
    val tint: Color = Color.Unspecified,
    val onClick: () -> Unit
)

/**
 * 批量管理模式底栏（计划/食材库/动作库共用）：
 * 全选/取消全选 · 已选计数 · 批量操作按钮 · 完成退出。
 */
@Composable
fun BatchManageBar(
    selectedCount: Int,
    totalCount: Int,
    onToggleSelectAll: () -> Unit,
    onExit: () -> Unit,
    actions: List<BatchAction>,
    modifier: Modifier = Modifier
) {
    Surface(shadowElevation = 8.dp, modifier = modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onToggleSelectAll) {
                Text(
                    if (selectedCount == totalCount && totalCount > 0) "取消全选" else "全选",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                "已选 $selectedCount / $totalCount",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            actions.forEach { action ->
                TextButton(onClick = action.onClick) {
                    Text(
                        action.label,
                        fontWeight = FontWeight.Bold,
                        color = if (action.tint == Color.Unspecified) MaterialTheme.colorScheme.primary else action.tint
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            TextButton(onClick = onExit) { Text("完成") }
        }
    }
}
