package com.yuedong.fitness.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * 测量数值专用档（潜表等宽契约）：热量/体重/组次等大号数字统一从这里取样式，
 * 替代页面里散落的 fontSize 字面量（30/34/40/44sp 等曾各成一套）。
 * 颜色由调用处给（数值可能是 onBackground / primary / DiveDataColors 等）。
 */
object DiveNumeric {
    /** 环心数字、克数选择器、提醒时间等超大主角数字（统一原 40/44sp） */
    val display = TextStyle(
        fontFamily = DiveMono,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp
    )

    /** 卡片英雄数字：热量/体重主数值（统一原 28/30/34sp） */
    val large = TextStyle(
        fontFamily = DiveMono,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    )

    /** 次级强调数值：指标值、统计格大数（统一原 20/24sp） */
    val medium = TextStyle(
        fontFamily = DiveMono,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    )
}
