package com.yuedong.fitness.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.yuedong.fitness.ui.theme.DiveGradients
import com.yuedong.fitness.ui.theme.diveDark

/**
 * 水洗卡（全 APP 内容卡统一容器）：海洋色相水洗渐变底 + 1dp 等深线描边 + 16dp 圆角。
 * 收敛「渐变水洗卡」与「纯 surface 平底卡」并存的分裂——今日页与其子页、引导页共用此容器。
 * 默认内容内边距 16dp；可点击时波纹按卡片圆角裁剪。
 */
@Composable
fun WashCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    val colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            elevation = CardDefaults.cardElevation(0.dp),
            border = border
        ) {
            Column(
                Modifier
                    .background(DiveGradients.cardWash(diveDark()), shape)
                    .padding(contentPadding)
            ) { content() }
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            elevation = CardDefaults.cardElevation(0.dp),
            border = border
        ) {
            Column(
                Modifier
                    .background(DiveGradients.cardWash(diveDark()), shape)
                    .padding(contentPadding)
            ) { content() }
        }
    }
}
