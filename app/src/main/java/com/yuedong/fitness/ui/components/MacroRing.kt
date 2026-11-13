package com.yuedong.fitness.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yuedong.fitness.ui.theme.DiveMono
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 小营养素圈（今日页同款）：淡同色轨道 + 进度弧 + 进度端点圆点，
 * 环心数值与「值/目标」，环下标签与百分比。今日页与饮食「每日热量目标」卡共用。
 */
@Composable
fun MacroRing(
    label: String,
    value: Int,
    target: Int?,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val fraction = target?.takeIf { it > 0 }?.let { value.toFloat() / it }?.coerceIn(0f, 1f) ?: 0f
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable(onClick = onClick)) {
            Canvas(Modifier.size(62.dp)) {
                val stroke = 6.dp.toPx()
                // 淡同色轨道（比灰轨道更柔，同参考图）
                drawArc(
                    color = color.copy(alpha = 0.16f),
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                if (fraction > 0f) {
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * fraction,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                // 进度端点小圆点（0% 时恰在正上方）
                val theta = Math.toRadians(-90.0 + 360.0 * fraction)
                val r = size.minDimension / 2f
                val c = Offset(
                    x = size.width / 2f + r * cos(theta).toFloat(),
                    y = size.height / 2f + r * sin(theta).toFloat()
                )
                drawCircle(color = color, radius = 3.5.dp.toPx(), center = c)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$value",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DiveMono,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    target?.let { "$value/$it" } ?: "--",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = DiveMono,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "${(fraction * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = DiveMono,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
