package com.yuedong.fitness.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** 长按菜单条目：图标 + 文案 + 点击行为 */
data class ListAction(
    val icon: ImageVector,
    val label: String,
    val tint: Color = Color.Unspecified,
    val action: () -> Unit
)

/**
 * 列表长按操作菜单（底部弹层，全 app 统一）：
 * 长按任意列表行弹出，展示该行的可用操作（收藏/置顶/删除等）。
 * 菜单不显示条目标题，只列操作；点操作后自动关闭。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListActionSheet(
    visible: Boolean,
    title: String? = null,
    actions: List<ListAction>,
    onDismiss: () -> Unit
) {
    if (!visible) return
    // 容器规范与 YueDialog 壳一致（surfaceContainerHigh + extraLarge 顶角 + 零色调抬升），
    // 避免「M3 默认弹层」和「悦动弹层」两套观感并存
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            if (title != null) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            actions.forEach { action ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            action.action()
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        action.icon,
                        contentDescription = null,
                        tint = if (action.tint == Color.Unspecified) MaterialTheme.colorScheme.onSurface else action.tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        action.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (action.tint == Color.Unspecified) MaterialTheme.colorScheme.onSurface else action.tint
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}
