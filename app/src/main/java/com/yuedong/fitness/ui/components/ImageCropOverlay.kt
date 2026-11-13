package com.yuedong.fitness.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

/**
 * 微信「编辑图片」式裁剪层：图片铺满全屏，裁剪框外压暗、框内明亮；
 * 双指缩放/拖动（全屏手势），底部「取消 / 旋转 / 确定」。
 * 用全屏 Dialog 承载（盖住底部导航等页面元素）；aspect = 裁剪框宽高比（宽/高，1f = 方形）。
 * 「弹窗统一 YueDialog」的豁免件：全屏手势工具层需要完整手势域与无边距画布，
 * 底部弹层壳承载不了（PhotoWall 全屏预览同此理由）。
 */
@Composable
fun ImageCropOverlay(
    source: Bitmap,
    aspect: Float,
    onConfirm: (Bitmap) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val density = LocalDensity.current
            var current by remember { mutableStateOf(source) }

            val screenW = constraints.maxWidth.toFloat()
            val screenH = constraints.maxHeight.toFloat()
            // 居中裁剪框
            val frameW = minOf(screenW * 0.9f, screenH * 0.58f * aspect)
            val frameH = frameW / aspect
            val frameLeft = (screenW - frameW) / 2f
            val frameTop = (screenH - frameH) / 2f
            // 图片以「盖住裁剪框」为最小缩放（而非铺满整屏），否则同宽高比的图没有位移空间
            val base = maxOf(frameW / current.width, frameH / current.height)
            var scale by remember(current) { mutableFloatStateOf(1f) }
            var offset by remember(current) {
                mutableStateOf(
                    Offset(
                        (frameLeft + frameW - current.width * base) / 2f,
                        (frameTop + frameH - current.height * base) / 2f
                    )
                )
            }

            fun clampOffset() {
                val w = current.width * base * scale
                val h = current.height * base * scale
                // 图片边缘不允许离开裁剪框：框必须始终被图片盖住
                offset = Offset(
                    offset.x.coerceIn(frameLeft + frameW - w, frameLeft),
                    offset.y.coerceIn(frameTop + frameH - h, frameTop)
                )
            }

            fun confirm() {
                val s = base * scale
                val cx = ((frameLeft - offset.x) / s).roundToInt().coerceIn(0, current.width - 1)
                val cy = ((frameTop - offset.y) / s).roundToInt().coerceIn(0, current.height - 1)
                val cw = minOf((frameW / s).roundToInt(), current.width - cx).coerceAtLeast(1)
                val ch = minOf((frameH / s).roundToInt(), current.height - cy).coerceAtLeast(1)
                onConfirm(Bitmap.createBitmap(current, cx, cy, cw, ch))
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(current) {
                        // 手写变换手势:不依赖 detectTransformGestures 的状态机,
                        // 事件自行消费,行为完全可控;纯点按(无位移无缩放)不消费,
                        // 不影响子级按钮的点击
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            do {
                                val event = awaitPointerEvent()
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                if (zoomChange != 1f || panChange != Offset.Zero) {
                                    event.changes.forEach { it.consume() }
                                    val newScale = (scale * zoomChange).coerceIn(1f, 6f)
                                    if (zoomChange != 1f) {
                                        // 围绕手势中心缩放:质心下的图像点保持不动
                                        val centroid = event.calculateCentroid()
                                        offset = Offset(
                                            centroid.x - (centroid.x - offset.x) * (newScale / scale),
                                            centroid.y - (centroid.y - offset.y) * (newScale / scale)
                                        )
                                    }
                                    scale = newScale
                                    offset += panChange
                                    clampOffset()
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
            ) {
                // 图片铺满全屏
                Image(
                    bitmap = current.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                        .requiredSize(
                            with(density) { (current.width * base * scale).toDp() },
                            with(density) { (current.height * base * scale).toDp() }
                        )
                )
                // 框外压暗 + 白色边框
                Canvas(Modifier.fillMaxSize()) {
                    val scrim = Color.Black.copy(alpha = 0.62f)
                    drawRect(scrim, topLeft = Offset(0f, 0f), size = Size(screenW, frameTop))
                    drawRect(
                        scrim,
                        topLeft = Offset(0f, frameTop + frameH),
                        size = Size(screenW, screenH - frameTop - frameH)
                    )
                    drawRect(scrim, topLeft = Offset(0f, frameTop), size = Size(frameLeft, frameH))
                    drawRect(
                        scrim,
                        topLeft = Offset(frameLeft + frameW, frameTop),
                        size = Size(screenW - frameLeft - frameW, frameH)
                    )
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(frameLeft, frameTop),
                        size = Size(frameW, frameH),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                // 底部操作条：取消 / 旋转 / 确定
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = Color.White) }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        val matrix = android.graphics.Matrix().apply { postRotate(90f) }
                        current = Bitmap.createBitmap(
                            current, 0, 0, current.width, current.height, matrix, true
                        )
                    }) {
                        Icon(
                            Icons.Filled.RotateRight,
                            contentDescription = "旋转",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { confirm() },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) { Text("确定", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
