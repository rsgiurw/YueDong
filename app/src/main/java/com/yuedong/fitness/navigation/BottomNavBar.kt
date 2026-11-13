package com.yuedong.fitness.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yuedong.fitness.ui.theme.diveDark

/**
 * 底部导航的 5 个标签定义：路由 + 中文标签 + 图标。
 */
data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    TabItem(Routes.TODAY, "今日", Icons.Filled.Today),
    TabItem(Routes.PLAN, "计划", Icons.Filled.FitnessCenter),
    TabItem(Routes.DIET, "饮食", Icons.Filled.Restaurant),
    TabItem(Routes.RECORDS, "记录", Icons.Filled.Insights),
    TabItem(Routes.PROFILE, "我的", Icons.Filled.Person)
)

/**
 * 悬浮层底部导航（v4 · 果冻弹跳）：
 * - 导航条脱离屏幕边缘，全胶囊外形 + 投影 + 细描边；
 * - 选中项为主题色椭圆胶囊（完全盖住图标+文字，图案转白）；
 * - 点击切换时胶囊以低阻尼弹簧「果冻式」弹跳到目标标签（带过冲回弹）。
 * 注意：内容容器必须固定高度（58dp）——bottomBar 槽位是松约束，
 * fillMaxHeight 在无界高度下会吃满整屏（上一版的炸因）。
 */
@Composable
fun YueDongBottomBar(currentRoute: String?, onTabClick: (String) -> Unit) {
    // 半透明胶囊:内容从导航条下滚过时隐约透出(透明白天/透出深渊微光)
    val barColor = (if (diveDark()) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }).copy(alpha = 0.88f)
    val unselected = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = barColor,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (diveDark()) 0.28f else 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                val tabWidth: Dp = maxWidth / bottomTabs.size
                // 记住最后一个标签：进入二级页时胶囊冻结在原地，
                // 不再被兜底逻辑拽到第 0 个标签（那正是"动画乱跳"的观感来源）
                var lastTab by remember { mutableStateOf(bottomTabs.first()) }
                bottomTabs.firstOrNull { it.route == currentRoute }?.let { lastTab = it }
                val selIndex = bottomTabs.indexOf(lastTab)
                // 果冻弹跳：低阻尼弹簧，胶囊带过冲回弹地弹到目标标签
                val indicatorX: Dp by animateDpAsState(
                    targetValue = tabWidth * selIndex,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "tabIndicator"
                )
                Box(
                    Modifier
                        .offset(x = indicatorX + 4.dp)
                        .width(tabWidth - 8.dp)
                        .fillMaxHeight()
                        .padding(vertical = 3.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                    )
                }
                Row(Modifier.fillMaxHeight()) {
                    bottomTabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        // 被主题色胶囊盖住时图案转白；未选中为悬浮颗粒色
                        val tint by animateColorAsState(
                            targetValue = if (selected) androidx.compose.ui.graphics.Color.White else unselected,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "tabTint"
                        )
                        val iconScale by animateFloatAsState(
                            targetValue = if (selected) 1.08f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "iconScale"
                        )
                        Column(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onTabClick(tab.route) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                tint = tint,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(iconScale)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                tab.label,
                                color = tint,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
