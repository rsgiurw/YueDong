package com.yuedong.fitness.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * 胶囊轻提示（全 APP 统一的操作反馈）：
 * 深色圆角短条从底部轻微上浮出现，约 1.6 秒后自动消失。
 * 用法：页面根部 Box 内调用 YueToastHost；message 置空即触发退出动画。
 */
@Composable
fun YueToastHost(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 24.dp
) {
    // 退出动画期间保留最后一条文案，避免消失过程中文字闪空
    var lastMessage by remember { mutableStateOf("") }
    if (!message.isNullOrEmpty()) lastMessage = message

    LaunchedEffect(message) {
        if (!message.isNullOrEmpty()) {
            delay(1600)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = !message.isNullOrEmpty(),
        enter = slideInVertically(tween(200)) { it / 2 } + fadeIn(tween(200)),
        exit = slideOutVertically(tween(200)) { it / 2 } + fadeOut(tween(200)),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.inverseSurface,
            shadowElevation = 6.dp
        ) {
            Text(
                lastMessage,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
            )
        }
    }
}
