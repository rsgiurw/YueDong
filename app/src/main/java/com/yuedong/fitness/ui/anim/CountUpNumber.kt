package com.yuedong.fitness.ui.anim

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

/**
 * T013：关键大数字进入页面时从 0 滚动到当前值（默认 450ms）。
 * 仅首次播放；之后目标值变化直接跳到新值（数据刷新不重滚），
 * 最终值始终与 target 一致；动画走 Animatable，不阻塞交互。
 */
@Composable
fun rememberCountUpValue(target: Int, durationMs: Int = 450): State<Float> {
    val animatable = remember { Animatable(0f) }
    var played by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(target) {
        if (!played) {
            played = true
            animatable.animateTo(
                target.toFloat(),
                animationSpec = tween(durationMs, easing = FastOutSlowInEasing)
            )
        } else {
            animatable.snapTo(target.toFloat())
        }
    }
    return animatable.asState()
}
