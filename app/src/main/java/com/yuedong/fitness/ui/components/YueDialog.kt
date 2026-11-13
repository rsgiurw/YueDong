package com.yuedong.fitness.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/**
 * 「悦动」统一弹窗（v2：底部弹出）。
 * 参数签名与 material3.AlertDialog 兼容，便于整站替换；
 * 全 APP 弹窗统一从底部滑出（ModalBottomSheet），支持下拉关闭与系统返回。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YueDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    dismissButton: @Composable RowScope.() -> Unit = {},
    title: @Composable () -> Unit = {},
    text: @Composable () -> Unit = {},
    shape: Shape = MaterialTheme.shapes.extraLarge,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = shape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        modifier = modifier
    ) {
        // 弹层关闭（确认/取消/点外部）时收起输入法，避免键盘残留
        val imeView = androidx.compose.ui.platform.LocalView.current
        val ime = imeView.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
            as? android.view.inputmethod.InputMethodManager
        val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        androidx.compose.runtime.DisposableEffect(imeView) {
            onDispose {
                keyboard?.hide()
                focusManager.clearFocus()
                (imeView.context as? android.app.Activity)?.window?.decorView?.windowToken?.let { token ->
                    ime?.hideSoftInputFromWindow(token, 0)
                }
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Box { title() }
            Spacer(Modifier.height(14.dp))
            text()
            Spacer(Modifier.height(18.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                dismissButton()
                confirmButton()
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** 底部弹层里的内容表面（浅色圆角卡，如添加食物弹层里的食物信息卡） */
@Composable
fun SheetCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) { content() }
    }
}
