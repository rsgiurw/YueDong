package com.yuedong.fitness.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.yuedong.fitness.ui.theme.AbyssAlert
import com.yuedong.fitness.ui.theme.AbyssGreen
import com.yuedong.fitness.ui.theme.DiveLantern
import com.yuedong.fitness.ui.theme.MenuOrangeDark
import com.yuedong.fitness.ui.theme.MenuPurpleDark
import com.yuedong.fitness.ui.theme.ThermoclineBlue
import kotlin.random.Random

/**
 * 完成问卷时的彩纸动效：一次性飘落，约 2.8 秒后淡出。
 * 纯 Canvas 绘制，不引入第三方动画库。
 */
@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier) {
    val pieces = remember {
        // 彩纸只用品牌色板（Color.kt 唯一色值区的既有常量），明暗主题都不出戏
        val palette = listOf(
            ThermoclineBlue, AbyssGreen, DiveLantern,
            AbyssAlert, MenuPurpleDark, MenuOrangeDark
        )
        List(70) {
            ConfettiPiece(
                x = Random.nextFloat(),
                speed = 0.7f + Random.nextFloat() * 0.7f,
                size = 8f + Random.nextFloat() * 10f,
                color = palette[Random.nextInt(palette.size)],
                startY = -Random.nextFloat() * 0.4f
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 2800, easing = LinearEasing))
    }

    Canvas(modifier.fillMaxSize()) {
        pieces.forEach { piece ->
            val y = size.height * piece.startY + size.height * 1.25f * progress.value * piece.speed
            drawRoundRect(
                color = piece.color.copy(alpha = (1f - progress.value * 0.55f)),
                topLeft = Offset(piece.x * size.width, y),
                size = Size(piece.size, piece.size * 0.55f)
            )
        }
    }
}

private data class ConfettiPiece(
    val x: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val startY: Float
)
