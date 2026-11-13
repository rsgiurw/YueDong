package com.yuedong.fitness.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 渐变体系：用海洋色相（蓝→青）的水洗渐变替代纯色表单卡。
 * 浅色 = 水面光带（清透浅蓝），深色 = 深渊微光（更深的墨蓝）。
 * 按钮渐变两档都足够深，保证白色文字达 WCAG AA。
 */
object DiveGradients {

    private val darkAqua = listOf(Color(0xFF14395F), Color(0xFF0A1C2E))
    private val lightAqua = listOf(Color(0xFFDCEBFE), Color(0xFFBFD9FD))

    private val darkWash = listOf(Color(0xFF0F2434), Color(0xFF0A1A28))
    private val lightWash = listOf(Color(0xFFFDFEFF), Color(0xFFEFF6FC))

    private val darkButton = listOf(Color(0xFF1668DC), Color(0xFF3D8EF8))
    private val lightButton = listOf(Color(0xFF1677FF), Color(0xFF3F8CFA))

    /** 主视觉面板 / hero 卡：较饱和的水色渐变 */
    fun depthBand(dark: Boolean): Brush = Brush.linearGradient(if (dark) darkAqua else lightAqua)

    /** 普通卡片水洗底：替代纯白 / 纯色表单卡 */
    fun cardWash(dark: Boolean): Brush = Brush.verticalGradient(if (dark) darkWash else lightWash)

    /** 主行动按钮：深→浅蓝横渐变，白字 */
    fun actionButton(dark: Boolean): Brush =
        Brush.horizontalGradient(if (dark) darkButton else lightButton)

    /** 英雄数字面板：柔和光晕 */
    fun heroGlow(dark: Boolean): Brush = Brush.verticalGradient(
        if (dark) listOf(Color(0xFF14355E), Color(0xFF0B1F3A))
        else listOf(Color(0xFFEAF2FE), Color(0xFFC9DEFB))
    )

    /** 报告页头部：深蓝渐变横幅，明暗主题同款深色（白字，与下方卡片形成对比） */
    fun reportHero(): Brush = Brush.linearGradient(listOf(HeroDeepNavy, HeroDeepBlue, HeroBrightBlue))

    /** 周/月报页面背景：海报同源的深渊夜色，页面本体直接铺（白字契约） */
    fun reportHeroBg(): Brush = Brush.verticalGradient(listOf(PosterBg, AbyssGround))
}
