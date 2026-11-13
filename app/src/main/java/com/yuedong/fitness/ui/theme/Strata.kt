package com.yuedong.fitness.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * 深度断层带色阶（B 方向 · 下潜叙事派）。
 * 10 档从「海面」到「深渊」，按坚持深度插值取色。
 * 暗色 / 浅色各一套，由 DepthStrataBackground 按滚动与连击选取。
 */

// ---------- 暗色断层带（深渊侧） ----------
val StrataDark = listOf(
    Color(0xFF0A1A28), // 0 海面微光
    Color(0xFF091724), // 1
    Color(0xFF08141F), // 2 表层
    Color(0xFF07121C), // 3
    Color(0xFF060F18), // 4 温跃层
    Color(0xFF050C13), // 5
    Color(0xFF050A10), // 6 礁石斜坡
    Color(0xFF04080C), // 7
    Color(0xFF030609), // 8 中光层
    Color(0xFF020407), // 9 深渊层
)

// ---------- 浅色断层带（水面侧） ----------
val StrataLight = listOf(
    Color(0xFFE9F1F5), // 0 海雾甲板
    Color(0xFFE3EDF3), // 1
    Color(0xFFDDE9F0), // 2
    Color(0xFFD6E4ED), // 3
    Color(0xFFCFDFEA), // 4
    Color(0xFFC8D9E6), // 5
    Color(0xFFC1D3E3), // 6
    Color(0xFFBACDDF), // 7
    Color(0xFFB3C7DA), // 8
    Color(0xFFACC1D6), // 9 深处浅蓝
)

/** 按下潜进度（0f=表层 → 1f=深渊层）取断层色（线性插值相邻两档） */
fun strataColor(progress: Float, dark: Boolean): Color {
    val table = if (dark) StrataDark else StrataLight
    val clamped = progress.coerceIn(0f, 1f)
    val scaled = clamped * (table.size - 1)
    val idx = scaled.toInt()
    val next = (idx + 1).coerceAtMost(table.size - 1)
    val frac = scaled - idx
    val a = table[idx]
    val b = table[next]
    return Color(
        red = a.red + (b.red - a.red) * frac,
        green = a.green + (b.green - a.green) * frac,
        blue = a.blue + (b.blue - a.blue) * frac,
        alpha = a.alpha
    )
}