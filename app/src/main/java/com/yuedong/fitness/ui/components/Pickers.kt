package com.yuedong.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/** 滚轮选中/未选中档字号（选择器专用档，命名常量统一） */
private val WHEEL_SELECTED = 26.sp
private val WHEEL_NORMAL = 18.sp

/**
 * 竖向滚轮数字选择器（年龄、身高）。
 * 惯性滚动自动吸附居中；点击数字后通过 onItemClick 回调（调用方可用它实现自动跳下一页）。
 * 对齐原理：列表首尾放等高占位项，使"真实视口中心 = 列表几何中心"，刻度线与选中值严格对齐。
 *
 * @param onSelected 滚动停止后的落点（持续选择）
 * @param onItemClick 用户直接点击某项（一般用于选中并自动进入下一步）
 * @param onScrollStarted 用户开始拖动（调用方可取消挂起的自动跳页）
 */
@Composable
fun VerticalWheelPicker(
    values: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onItemClick: ((Int) -> Unit)? = null,
    onScrollStarted: (() -> Unit)? = null
) {
    val itemHeight: Dp = 48.dp
    val viewportHeight: Dp = itemHeight * 5
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val itemSizePx = with(density) { itemHeight.roundToPx() }
    var viewportPx by remember { mutableIntStateOf(0) }

    // 当前停在中心的"真实条目"序号（过滤掉首尾占位项）
    val centerIndex by remember {
        derivedStateOf {
            val mid = viewportPx / 2
            listState.layoutInfo.visibleItemsInfo
                .filter { it.size == itemSizePx }
                .minByOrNull { abs(it.offset + it.size / 2 - mid) }
                ?.index?.minus(1) ?: -1
        }
    }

    // 把某个值滚回中心：占位头占 1 个布局位，真实条目 index 从 1 开始
    suspend fun scrollToCenter(index: Int) {
        if (viewportPx == 0 || values.isEmpty()) return
        val idx = index.coerceIn(0, values.lastIndex)
        listState.scrollToItem(
            index = idx + 1,
            scrollOffset = viewportPx / 2 - itemSizePx / 2
        )
    }

    LaunchedEffect(viewportPx, selectedIndex) {
        if (viewportPx > 0 && !listState.isScrollInProgress && centerIndex != selectedIndex) {
            scrollToCenter(selectedIndex)
        }
    }
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            onScrollStarted?.invoke()
        } else {
            val idx = centerIndex
            if (idx in values.indices && idx != selectedIndex) onSelected(idx)
        }
    }

    Box(modifier.height(viewportHeight).fillMaxWidth()) {
        // 中心参考线：上下两条，夹住选中项
        Column(Modifier.align(Alignment.Center).fillMaxWidth()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            )
            Spacer(Modifier.height(itemHeight))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            )
        }
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { viewportPx = it.height }
        ) {
            item { Spacer(Modifier.height(itemHeight * 2)) }
            itemsIndexed(values) { index, value ->
                val selected = index == centerIndex
                val distance = abs(index - centerIndex)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable { onItemClick?.invoke(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value,
                        fontSize = if (selected) WHEEL_SELECTED else WHEEL_NORMAL,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                                .copy(alpha = (1f - distance * 0.25f).coerceIn(0.35f, 1f))
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(itemHeight * 2)) }
        }
    }
}

/**
 * 横向体重标尺：刻度 + 中心指示线 + 惯性吸附。回调语义与 VerticalWheelPicker 相同。
 */
@Composable
fun HorizontalRulerPicker(
    values: List<Double>,
    selectedIndex: Int,
    labelText: (Double) -> String,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    labelEvery: Int = 10,
    onItemClick: ((Int) -> Unit)? = null,
    onScrollStarted: (() -> Unit)? = null
) {
    val tickWidth: Dp = 14.dp
    val viewportHeight: Dp = 64.dp
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val itemSizePx = with(density) { tickWidth.roundToPx() }
    var viewportPx by remember { mutableIntStateOf(0) }

    val centerIndex by remember {
        derivedStateOf {
            val mid = viewportPx / 2
            listState.layoutInfo.visibleItemsInfo
                .filter { it.size == itemSizePx }
                .minByOrNull { abs(it.offset + it.size / 2 - mid) }
                ?.index?.minus(1) ?: -1
        }
    }

    suspend fun scrollToCenterRuler(index: Int) {
        if (viewportPx == 0 || values.isEmpty()) return
        val idx = index.coerceIn(0, values.lastIndex)
        listState.scrollToItem(
            index = idx + 1,
            scrollOffset = viewportPx / 2 - itemSizePx / 2
        )
    }

    LaunchedEffect(viewportPx, selectedIndex) {
        if (viewportPx > 0 && !listState.isScrollInProgress && centerIndex != selectedIndex) {
            scrollToCenterRuler(selectedIndex)
        }
    }
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            onScrollStarted?.invoke()
        } else {
            val idx = centerIndex
            if (idx in values.indices && idx != selectedIndex) onSelected(idx)
        }
    }

    Box(modifier.height(viewportHeight).fillMaxWidth()) {
        Box(
            Modifier
                .align(Alignment.Center)
                .width(2.dp)
                .height(viewportHeight)
                .background(MaterialTheme.colorScheme.primary)
        )
        LazyRow(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { viewportPx = it.width }
        ) {
            item { Spacer(Modifier.width(tickWidth * 7)) }
            itemsIndexed(values) { index, _ ->
                val isMajor = index % labelEvery == 0
                val isMid = index % (labelEvery / 2) == 0
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(tickWidth)
                        .clickable { onItemClick?.invoke(index) }
                ) {
                    Box(
                        Modifier
                            .width(if (isMajor) 2.dp else 1.dp)
                            .height(
                                when {
                                    isMajor -> 22.dp
                                    isMid -> 15.dp
                                    else -> 9.dp
                                }
                            )
                            .background(
                                if (isMajor) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                }
                            )
                    )
                    if (isMajor) {
                        Spacer(Modifier.height(3.dp))
                        // 用 3 倍宽的透明容器让数字不换行，并保持与刻度对齐
                        Box(
                            Modifier
                                .width(tickWidth * 3)
                                .offset(x = -tickWidth),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = labelText(values[index]),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.width(tickWidth * 7)) }
        }
    }
}
