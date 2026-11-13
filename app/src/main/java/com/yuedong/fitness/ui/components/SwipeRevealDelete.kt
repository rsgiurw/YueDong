package com.yuedong.fitness.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * 左滑操作统一交互（全 APP 列表删除唯一入口，两段式 + 可选收藏）：
 * 1. 左滑拉到红色区域全宽 → 长震动 + 「拉不动」（视觉锁在阈值处）
 * 2. 露出区可点：♥收藏（可选，点了停留可再点取消）｜删除（点击或继续深拉触发）
 * 3. 锁住后继续往左拉过删除阈值 → 直接删除该行，无任何提示
 * 4. 未达阈值松手 / 右滑 → 弹回
 *
 * 所有列表删除（自建动作/自建计划/自建食物/饮食条目）共用本组件。
 */
@Composable
fun SwipeRevealDelete(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    deleteLabel: String = "删除",
    /** 传入则露出区多一个收藏块，点击回调切换收藏态 */
    isFavorite: Boolean? = null,
    onToggleFavorite: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val hasFavorite = isFavorite != null && onToggleFavorite != null
    val favBlockPx = with(density) { 76.dp.toPx() }
    val delBlockPx = with(density) { 96.dp.toPx() }
    val revealPx = if (hasFavorite) favBlockPx + delBlockPx else delBlockPx
    val deletePx = revealPx + with(density) { 64.dp.toPx() }
    val scope = rememberCoroutineScope()
    var offset by remember { mutableFloatStateOf(0f) }
    var armed by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    fun settle(to: Float) {
        scope.launch {
            val anim = Animatable(offset)
            anim.animateTo(to, tween(180))
            offset = anim.value
        }
    }

    Box(modifier.fillMaxWidth()) {
        // 露出区：♥收藏（可选） + 删除
        Row(Modifier.matchParentSize()) {
            if (hasFavorite) {
                Box(
                    Modifier
                        .width(76.dp)
                        .fillMaxHeight()
                        .background(com.yuedong.fitness.ui.theme.DiveDataColors.levelMid, MaterialTheme.shapes.medium)
                        .clickable(enabled = enabled && armed && !deleting) { onToggleFavorite?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "收藏",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        // 潜灯琥珀底很浅，白字对比不足（尤其暗色），配深渊墨蓝字才可读
                        color = com.yuedong.fitness.ui.theme.AbyssGround
                    )
                }
            }
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.error, MaterialTheme.shapes.medium)
                    .clickable(enabled = enabled && armed && !deleting) {
                        if (!deleting) {
                            deleting = true
                            onDelete()
                        }
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    deleteLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(end = 30.dp)
                )
            }
        }
        // 内容行：跟手偏移 + 两段式手势
        Row(
            Modifier
                .offset { IntOffset(offset.roundToInt(), 0) }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    var raw = 0f
                    detectHorizontalDragGestures(
                        onDragStart = {
                            if (deleting) return@detectHorizontalDragGestures
                            raw = offset
                            armed = false
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            raw += dragAmount
                            offset = raw.coerceIn(-deletePx, 0f)
                            if (!armed && abs(raw) >= revealPx && sign(raw) < 0) {
                                // 第一阈值：震动 + 锁住（视觉钳在露出宽度，拉不动）
                                armed = true
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            if (armed && !deleting && abs(raw) >= deletePx) {
                                // 第二阈值：直接删除，无提示
                                deleting = true
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDelete()
                            }
                        },
                        onDragEnd = {
                            if (!deleting) {
                                settle(if (armed) -revealPx else 0f)
                            }
                        }
                    )
                }
        ) { content() }
    }
}
