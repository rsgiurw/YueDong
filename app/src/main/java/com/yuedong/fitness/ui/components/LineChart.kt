package com.yuedong.fitness.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * 体重趋势折线（纯 Canvas 自绘，零第三方图表依赖）。
 * 真坐标轴布局：左侧 Y 轴刻度值、底部 X 轴日期刻度、横向网格线、绘图区四周留白。
 * 颜色全部来自 Material 主题角色，深浅色自动适配（等宽数字 + 等深线网格）。
 * @param values 按时间升序的数值序列
 * @param xLabels 每个数据点的横轴标签（如 "9.1"）
 * @param selectedIndex 当前选中节点；点击或拖动可切换，节点上方显示数值
 * @param avgValues 与 values 等长的均线序列（如 7 日平均），虚线绘制
 * @param areaFill 折线下方淡色渐变面积填充
 */
@Composable
fun LineChart(
    values: List<Double>,
    modifier: Modifier = Modifier,
    selectedIndex: Int = -1,
    onSelected: (Int) -> Unit = {},
    xLabels: List<String> = emptyList(),
    /** 空心点的下标集合（如继承来的体重值），与实心真实记录点形成视觉区分 */
    hollowIndices: Set<Int> = emptySet(),
    lineColor: Color = MaterialTheme.colorScheme.primary,
    pointColor: Color = MaterialTheme.colorScheme.primary,
    /** 同长度的均线序列（如 7 日平均），浅色虚线压在主折线下方 */
    avgValues: List<Double> = emptyList(),
    /** 折线下方淡色渐变面积填充（趋势页开，列表页关） */
    areaFill: Boolean = false,
    /** 每个数据点下方垫一圈同色光晕（进步报告用，视觉更饱满） */
    pointHalo: Boolean = false,
    /** 面积渐变顶端不透明度（默认 0.22；报告页调浓让图表更有分量） */
    areaTopAlpha: Float = 0.22f
) {
    val density = LocalDensity.current
    val tickTextPx = with(density) { 10.sp.toPx() }
    val valueTextPx = with(density) { 12.sp.toPx() }
    val yTickWidth = tickTextPx * 3.2f
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    fun selectFrom(x: Float, plotLeft: Float, plotRight: Float) {
        if (values.isEmpty()) return
        val step = (plotRight - plotLeft) / (values.size - 1).coerceAtLeast(1)
        val index = ((x - plotLeft) / step).roundToInt().coerceIn(0, values.lastIndex)
        onSelected(index)
    }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(values) {
                detectTapGestures { offset ->
                    selectFrom(offset.x, yTickWidth, size.width.toFloat())
                }
            }
            .pointerInput(values) {
                // 只拦截横向拖动选节点；竖向滑动放行给页面滚动，避免图表吃掉滚动手势
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        selectFrom(offset.x, yTickWidth, size.width.toFloat())
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        selectFrom(change.position.x, yTickWidth, size.width.toFloat())
                    }
                )
            }
    ) {
        if (values.isEmpty()) return@Canvas

        // ---- 布局：左侧 Y 轴刻度宽 + 底部 X 轴刻度高，四周留白 ----
        val xTickHeight = tickTextPx * 2.2f
        val topPad = valueTextPx * 1.6f
        val plotLeft = yTickWidth
        val plotRight = size.width - tickTextPx * 0.6f
        val plotTop = topPad
        val plotBottom = size.height - xTickHeight

        // ---- Y 轴：按数据范围选"整洁"步进（1/2/5/10/20/50kg…），网格自然落在 3~6 条 ----
        val dataMin = values.min()
        val dataMax = values.max()
        val dataRange = (dataMax - dataMin).coerceAtLeast(if (values.size == 1) 4.0 else 0.5)
        val rawStep = dataRange / 3.5
        val step = listOf(1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0).firstOrNull { it >= rawStep } ?: 100.0
        val yMin = kotlin.math.floor((dataMin - step * 0.5) / step) * step
        val yMax = kotlin.math.ceil((dataMax + step * 0.5) / step) * step
        val yRange = (yMax - yMin).coerceAtLeast(step)

        fun yFor(v: Double): Float =
            (plotBottom - (plotBottom - plotTop) * ((v - yMin) / yRange).toFloat())

        val axisPaint = android.graphics.Paint().apply {
            textSize = tickTextPx
            textAlign = android.graphics.Paint.Align.RIGHT
            color = axisColor.toArgb()
            isAntiAlias = true
        }

        // 网格线 + Y 刻度值（含上下边界）
        val gridCount = (yRange / step).roundToInt()
        for (i in 0..gridCount) {
            val v = yMin + step * i
            val y = yFor(v)
            drawLine(gridColor, Offset(plotLeft, y), Offset(plotRight, y), 2f)
            drawContext.canvas.nativeCanvas.drawText(
                if (v == v.toInt().toDouble()) "${v.toInt()}" else String.format("%,.1f", v),
                plotLeft - tickTextPx * 0.4f,
                y + tickTextPx * 0.35f,
                axisPaint
            )
        }

        // ---- X 轴日期刻度：点少全画，点多自动稀疏（约 6 个刻度）----
        if (xLabels.size == values.size && xLabels.isNotEmpty()) {
            val xPaint = android.graphics.Paint().apply {
                textSize = tickTextPx
                textAlign = android.graphics.Paint.Align.CENTER
                color = axisColor.toArgb()
                isAntiAlias = true
            }
            val xStep = (plotRight - plotLeft) / (values.size - 1).coerceAtLeast(1)
            // 刻度间隔自动计算：点少（近一周）每天一格；点多取 ≥ 点数/6 的最小"整洁"数
            val every = if (values.size <= 8) 1
            else listOf(2, 5, 10, 15, 20, 25, 30, 45, 60).firstOrNull { it >= values.size / 6.0 } ?: 60
            values.indices.forEach { index ->
                if (index % every == 0 || index == values.lastIndex) {
                    val x = plotLeft + index * xStep
                    // 刻度短线
                    drawLine(axisColor, Offset(x, plotBottom), Offset(x, plotBottom + 6f), 2f)
                    // X 轴刻度下移，与 Y 轴最下方的刻度值拉开一行，避免左下角文字打架
                    drawContext.canvas.nativeCanvas.drawText(
                        xLabels[index],
                        x.coerceIn(plotLeft + tickTextPx, plotRight - tickTextPx),
                        plotBottom + tickTextPx * 1.55f,
                        xPaint
                    )
                }
            }
        }

        // ---- 折线与数据点（单点居中显示）----
        val xStep = (plotRight - plotLeft) / (values.size - 1).coerceAtLeast(1)
        val points = values.mapIndexed { index, value ->
            val x = if (values.size == 1) (plotLeft + plotRight) / 2f else plotLeft + index * xStep
            Offset(x, yFor(value))
        }
        if (points.size == 1) {
            drawCircle(pointColor, 12f, points.first())
            return@Canvas
        }

        // ---- 面积填充：折线下方淡色渐变（趋势页视觉主体感）----
        if (areaFill) {
            val fillPath = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, plotBottom)
                lineTo(points.first().x, plotBottom)
                close()
            }
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    0f to lineColor.copy(alpha = areaTopAlpha),
                    1f to lineColor.copy(alpha = 0.02f),
                    startY = plotTop,
                    endY = plotBottom
                )
            )
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, color = lineColor, style = Stroke(width = 7f))

        // ---- 均线（可选）：浅色虚线压在主折线下方，看真实趋势不受单日波动干扰 ----
        if (avgValues.size == values.size && values.size > 1) {
            val avgPath = Path().apply {
                avgValues.forEachIndexed { index, v ->
                    val x = plotLeft + index * xStep
                    if (index == 0) moveTo(x, yFor(v)) else lineTo(x, yFor(v))
                }
            }
            drawPath(
                avgPath,
                color = axisColor.copy(alpha = 0.7f),
                style = Stroke(
                    width = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                )
            )
        }

        points.forEachIndexed { index, point ->
            if (index != selectedIndex) {
                if (pointHalo) {
                    drawCircle(pointColor.copy(alpha = 0.12f), radius = 22f, center = point)
                }
                if (index in hollowIndices) {
                    // 继承点：白底描边空心，直观区别于真实记录
                    drawCircle(surfaceColor, radius = 9f, center = point)
                    drawCircle(pointColor, radius = 9f, center = point, style = Stroke(width = 4f))
                } else {
                    drawCircle(pointColor, 10f, center = point)
                }
            }
        }

        // ---- 选中节点：竖线 + 白描边大点 + 数值 ----
        if (selectedIndex in values.indices) {
            val selected = points[selectedIndex]
            drawLine(
                color = pointColor.copy(alpha = 0.35f),
                start = Offset(selected.x, plotTop),
                end = Offset(selected.x, plotBottom),
                strokeWidth = 3f
            )
            drawCircle(surfaceColor, radius = 30f, center = selected)
            drawCircle(pointColor, radius = 22f, center = selected)
            val valueText = String.format("%.1f", values[selectedIndex])
            val valuePaint = android.graphics.Paint().apply {
                textSize = valueTextPx
                textAlign = android.graphics.Paint.Align.CENTER
                color = pointColor.toArgb()
                isAntiAlias = true
                isFakeBoldText = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                valueText,
                selected.x.coerceIn(plotLeft + valueTextPx, plotRight - valueTextPx),
                (selected.y - valueTextPx * 0.5f).coerceAtLeast(valueTextPx),
                valuePaint
            )
        }
    }
}
