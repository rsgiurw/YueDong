package com.yuedong.fitness.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yuedong.fitness.R

/** 潜表等宽数字（OWN-WORLD 契约）：全部测量数值使用此字族。 */
val DiveMono = FontFamily(
    Font(R.font.jetbrainsmono_regular),
    Font(R.font.jetbrainsmono_bold, FontWeight.Bold)
)

/**
 * 当前实际生效的明暗（跟随 app 内「外观主题」设置，而非系统设置）。
 * 渐变 / 数据色 / 页面分支一律用 diveDark() 判断，
 * 禁止直接用 isSystemInDarkTheme()——否则 app 内手动切主题时两边打架，
 * 会出现浅色卡片配浅色文字的不可读页面。
 */
val LocalDiveDark = androidx.compose.runtime.staticCompositionLocalOf { false }

@Composable
fun diveDark(): Boolean = LocalDiveDark.current

/** 深度阶段地表：连击越深，今日页地表越沉（暗色可感知，浅色恒定甲板）。 */
@Composable
fun diveGroundFor(streak: Int): Color {
    val dark = diveDark()
    if (!dark) return DeckGround
    return when {
        streak >= 30 -> Color(0xFF03080D)
        streak >= 14 -> Color(0xFF050C13)
        streak >= 7 -> Color(0xFF060F18)
        streak >= 3 -> Color(0xFF07121C)
        else -> Color(0xFF08141F)
    }
}

/**
 * 全局主题（impeccable 方向契约：深海渐进下潜 · Operate 模式）。
 * 暗色 = 深渊（品牌主身份，随系统深色模式）；浅色 = 水面休整层。
 * 关闭系统动态取色：品牌一致性优先（PRODUCT.md 约束）。
 * 形状：卡片 12–16dp，小控件才是胶囊（craft-floor elevation/radius 纪律）。
 */

private val AbyssalColorScheme = darkColorScheme(
    primary = ThermoclineBlue,
    onPrimary = Color(0xFF041E3D),
    primaryContainer = ThermoclineBlueContainer,
    onPrimaryContainer = ThermoclineBlueGlow,
    secondary = MarineSnowDim,
    onSecondary = AbyssGround,
    secondaryContainer = Color(0xFF14395C),
    onSecondaryContainer = Color(0xFFB0C9D4),
    tertiary = AbyssGreen,
    onTertiary = Color(0xFF062715),
    tertiaryContainer = Color(0xFF0B3D28),
    onTertiaryContainer = Color(0xFFB8F0D4),
    background = AbyssGround,
    onBackground = MarineSnow,
    surface = AbyssSurface,
    onSurface = MarineSnow,
    surfaceVariant = AbyssSurfaceHigh,
    onSurfaceVariant = MarineSnowDim,
    surfaceTint = ThermoclineBlue,
    surfaceDim = Color(0xFF02060B),
    surfaceBright = Color(0xFF17334A),
    surfaceContainerLowest = Color(0xFF030A10),
    surfaceContainerLow = Color(0xFF0A1824),
    surfaceContainer = Color(0xFF0D1F2E),
    surfaceContainerHigh = Color(0xFF13293C),
    surfaceContainerHighest = Color(0xFF1A3346),
    outline = AbyssOutline,
    outlineVariant = AbyssOutlineDim,
    error = AbyssAlert,
    onError = Color(0xFF3A0E08),
    errorContainer = AbyssAlertContainer,
    onErrorContainer = Color(0xFFFFDAD2),
    inverseSurface = DeckSurface,
    inverseOnSurface = InkDeep,
    inversePrimary = DeepBlue
)

private val SurfaceIntervalColorScheme = lightColorScheme(
    primary = DeepBlue,
    onPrimary = Color.White,
    primaryContainer = DeepBlueContainer,
    onPrimaryContainer = DeepBlueOnContainer,
    secondary = InkDim,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE8EE),
    onSecondaryContainer = InkDeep,
    tertiary = DeckGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCDEFDF),
    onTertiaryContainer = Color(0xFF06301E),
    background = DeckGround,
    onBackground = InkDeep,
    surface = DeckSurface,
    onSurface = InkDeep,
    surfaceVariant = DeckSurfaceDim,
    onSurfaceVariant = InkDim,
    surfaceTint = DeepBlue,
    surfaceDim = Color(0xFFD9E3E9),
    surfaceBright = Color(0xFFF8FBFC),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF1F6F9),
    surfaceContainer = Color(0xFFEBF2F6),
    surfaceContainerHigh = Color(0xFFE4EDF2),
    surfaceContainerHighest = Color(0xFFDCE8EE),
    outline = DeckOutline,
    outlineVariant = Color(0xFFB9CBD4),
    error = DeckAlert,
    onError = Color.White,
    errorContainer = DeckAlertContainer,
    onErrorContainer = Color(0xFF3A0E08),
    inverseSurface = AbyssSurface,
    inverseOnSurface = MarineSnow,
    inversePrimary = ThermoclineBlue
)

private val DiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun YueDongTheme(
    /** system 跟随系统 / light 浅色 / dark 深色 */
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) AbyssalColorScheme else SurfaceIntervalColorScheme,
        typography = Typography,
        shapes = DiveShapes,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(LocalDiveDark provides dark) {
                content()
            }
        }
    )
}

/**
 * 恒暗容器：报表、分享海报等品牌深色页面用。
 * 局部强制深渊色板 + diveDark()=true，不随外观设置切换——
 * 页内所有 colorScheme 角色、DiveDataColors 自动走深色值。
 */
@Composable
fun DiveForcedDarkTheme(content: @Composable () -> Unit) {
    val scheme = AbyssalColorScheme
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        shapes = DiveShapes,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalDiveDark provides true,
                androidx.compose.material3.LocalContentColor provides scheme.onSurface
            ) {
                content()
            }
        }
    )
}

/**
 * 数据可视化分类色：宏量营养、热量/GI 等级、日历判定。
 * 随明暗主题自动切换（深渊/水面两套），界面代码从这里取色。
 */
object DiveDataColors {
    val protein: Color @Composable get() = if (diveDark()) Color(0xFF7CB8FF) else Color(0xFF2E7CD6)
    val carb: Color @Composable get() = if (diveDark()) Color(0xFF66D98F) else Color(0xFF1E8E4E)
    val fat: Color @Composable get() = if (diveDark()) DiveLantern else DeckOrange

    val levelLow: Color @Composable get() = if (diveDark()) AbyssGreen else DeckGreen
    val levelMid: Color @Composable get() = if (diveDark()) DiveLantern else DeckOrange
    val levelHigh: Color @Composable get() = if (diveDark()) AbyssAlert else DeckAlert
    val levelNeutral: Color @Composable get() = if (diveDark()) MarineSnowDim else InkDim

    /** BMI 四级分类：偏瘦 / 正常 / 超重 / 肥胖（中国成人标准 WS/T 428-2013） */
    val bmiUnder: Color @Composable get() = if (diveDark()) BmiUnderDark else BmiUnderLight
    val bmiNormal: Color @Composable get() = levelLow
    val bmiOver: Color @Composable get() = if (diveDark()) AbyssOrange else BmiOverLight
    val bmiObese: Color @Composable get() = levelHigh
}

/**
 * 菜单图标底色：实底圆 + 白色图标（「我的」页功能入口等）。
 * 浅深两套，深色下提亮一档保证与白图标的对比。
 */
object DiveMenuAccents {
    private val dark: Boolean @Composable get() = diveDark()

    val blue: Color @Composable get() = if (dark) MenuBlueDark else MenuBlueLight
    val purple: Color @Composable get() = if (dark) MenuPurpleDark else MenuPurpleLight
    val green: Color @Composable get() = if (dark) MenuGreenDark else MenuGreenLight
    val orange: Color @Composable get() = if (dark) MenuOrangeDark else MenuOrangeLight
}
