package com.yuedong.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yuedong.fitness.ui.theme.ActionOnGradient
import com.yuedong.fitness.ui.theme.DiveGradients
import com.yuedong.fitness.ui.theme.diveDark

/**
 * 主行动按钮（全 APP 统一 CTA）：潜航蓝渐变底 + 52dp 高 + 12dp 圆角 + 白字契约。
 * 收敛主按钮「渐变/默认、40~52dp 高、多种圆角」各写一套的分裂；
 * 渐变上的文字色走 ActionOnGradient（白），不用 onPrimary——深色主题的 onPrimary 是深蓝，压在蓝渐变上不可读。
 */
@Composable
fun PrimaryCta(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = ActionOnGradient,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = ActionOnGradient
        )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    DiveGradients.actionButton(diveDark()),
                    MaterialTheme.shapes.medium
                )
                .alpha(if (enabled) 1f else 0.45f),
            contentAlignment = Alignment.Center
        ) {
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
