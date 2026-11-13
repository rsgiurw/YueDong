package com.yuedong.fitness.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * 「悦动」深海渐进下潜色板（impeccable 方向契约 OWN-WORLD）。
 * 所有界面代码只引用 MaterialTheme.colorScheme 语义角色；
 * 本文件是唯一允许出现具体色值的地方。
 * 兼容别名区（Yue* 旧名）仅供存量引用过渡，新代码禁止使用。
 */

// ---------- 深渊（暗色方案 = 品牌主身份） ----------
val AbyssGround = Color(0xFF08141F)        // 深渊地表：最深的墨蓝
val AbyssSurface = Color(0xFF0C1B29)       // 抬升一层：岩壁
val AbyssSurfaceHigh = Color(0xFF13293C)   // 再抬升：温跃层上方
val AbyssOutline = Color(0xFF24405A)       // 等深线
val AbyssOutlineDim = Color(0xFF16324A)    // 更弱的等深线
val MarineSnow = Color(0xFFDCE9F0)         // 海雪：主文字（粉笔白）
val MarineSnowDim = Color(0xFFA3BDC9)      // 悬浮颗粒：次级文字（足够亮，深色卡上小字可读）
val ThermoclineBlue = Color(0xFF4D9EFF)    // 温跃层蓝：当前深度 / 主强调
val ThermoclineBlueContainer = Color(0xFF11406E)   // 温跃层容器
val ThermoclineBlueGlow = Color(0xFFC9E2FF)        // 温跃层容器上的发光文字
val DiveLantern = Color(0xFFFFC95C)        // 潜灯暖黄：里程碑 / 连击点亮
val AbyssGreen = Color(0xFF3FD68F)         // 潜脑确认绿：完成态
val AbyssAlert = Color(0xFFFF6B5E)         // 潜脑警示红：错误 / 超限
val AbyssAlertContainer = Color(0xFF3A1220)
val AbyssOrange = Color(0xFFFFB366)        // BMI 分级橙（超重）

// ---------- 水面休整层（浅色方案） ----------
val DeckGround = Color(0xFFE9F1F5)         // 海雾甲板
val DeckSurface = Color(0xFFF8FBFC)        // 浪尖白
val DeckSurfaceDim = Color(0xFFDCE8EE)     // 湿甲板
val DeckOutline = Color(0xFF7C97A6)        // 浅等深线
val InkDeep = Color(0xFF0F2430)            // 墨蓝主文字
val InkDim = Color(0xFF4A6372)             // 次级文字
val DeepBlue = Color(0xFF1677FF)           // 深海蓝：浅色下的主强调
val DeepBlueContainer = Color(0xFFD9E9FF)
val DeepBlueOnContainer = Color(0xFF0A2F5E)
val DeckGreen = Color(0xFF18845C)          // 完成态（浅色）
val DeckAlert = Color(0xFFB3402F)          // 警示（浅色）
val DeckAlertContainer = Color(0xFFFFE2DD)
val DeckOrange = Color(0xFFB26A1B)         // BMI 分级橙（浅色）

// ---------- 进步报告 Hero 深蓝渐变（明暗两主题同款深色，白字契约） ----------
val HeroDeepNavy = Color(0xFF0A1A2C)       // 渐变起端：深渊夜航蓝
val HeroDeepBlue = Color(0xFF123B6E)       // 渐变中段：温跃层深蓝
val HeroBrightBlue = Color(0xFF1F62C6)     // 渐变末端：亮端（白字仍达 AA）

// ---------- 渐变主行动按钮的契约文字色（Gradients.actionButton 上的白字 AA 契约） ----------
val ActionOnGradient = Color(0xFFFFFFFF)

// ---------- 火柴人/扁平插画资产色（StickFigure 与食物详情火柴人共用，两套并行配色已合并于此） ----------
// 以 StickFigure 实际色板为正典（贴近 AI 参考视频的白底扁平风格）
val FigSkin = Color(0xFFF2C79F)            // 肤色
val FigSkinShade = Color(0xFFD8A874)       // 肤色阴影
val FigShirt = Color(0xFF3E6FE3)           // 短袖上衣（蓝）
val FigShirtShade = Color(0xFF3260C9)      // 上衣阴影
val FigHair = Color(0xFF3B2B20)            // 头发
val FigEye = Color(0xFF2B2B2B)             // 眼睛
val FigShoe = Color(0xFF455A64)            // 鞋
val FigMat = Color(0xFFC4C4C4)             // 瑜伽垫
val FigMatEdge = Color(0xFFADADAD)         // 垫子描边
val FigHighlight = Color(0xFFE5432E)       // 发力肌群高亮红
val FigCanvas = Color(0xFFFFFFFF)          // 插画舞台底（白底插画资产在此呈现，卡片容器随主题）

// 食物详情页火柴人（运动等量消耗示意）配色（原三段重复字面量收敛于此）
val FigSkinWarm = Color(0xFFFFCC80)        // 肤色（暖）
val FigHairBrown = Color(0xFF8D6E63)       // 棕发
val FigShirtOrange = Color(0xFFF57C00)     // 橙色上衣
val FigPantsGray = Color(0xFF90A4AE)       // 灰裤
val FigRopeBlue = Color(0xFF4A9DF8)        // 跳绳绳体

// ---------- 分享海报底色（海报恒为深色，与主题无关；文字/强调复用深渊色板） ----------
val PosterBg = Color(0xFF0D1B2E)           // 海报渐变顶端
val PosterPanel = Color(0xFF122741)        // 海报内面板/卡底

// ---------- BMI 四级分类色（浅/深两套，数据可视化用） ----------
val BmiUnderLight = Color(0xFF0E9494)      // 偏瘦：青（浅色）
val BmiUnderDark = Color(0xFF4FD8D0)       // 偏瘦：青（深色）
val BmiOverLight = Color(0xFFC46A0A)       // 超重：琥珀（浅色）
// 超重（深色）复用 AbyssOrange；正常 / 肥胖复用 Abyss·Deck 绿红两对

// ---------- 菜单图标底色（蓝/紫/绿/橙，白图标实底圆） ----------
val MenuBlueLight = Color(0xFF1677FF)
val MenuBlueDark = Color(0xFF3E8EF0)
val MenuPurpleLight = Color(0xFF7C5CE8)
val MenuPurpleDark = Color(0xFF9B82F5)
val MenuGreenLight = Color(0xFF1FA45B)
val MenuGreenDark = Color(0xFF3FCA7D)
val MenuOrangeLight = Color(0xFFE88A00)
val MenuOrangeDark = Color(0xFFFFA94D)

// ---------- 兼容别名（旧名 → 新世界暗色值；存量引用过渡用，新代码禁用） ----------
val YueBlue = ThermoclineBlue
val YueBlueDark = DeepBlue
val YueBlueContainer = ThermoclineBlueContainer
val OnBlueContainer = ThermoclineBlueGlow
val YueBackground = AbyssGround
val YueSurface = AbyssSurface
val YueSuccess = AbyssGreen
val YueErrorRed = AbyssAlert
val YueOverweightOrange = AbyssOrange
val YueTextPrimary = MarineSnow
val YueTextSecondary = MarineSnowDim
