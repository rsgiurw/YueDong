package com.yuedong.fitness.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yuedong.fitness.R
import kotlin.math.min

/**
 * 火柴人姿势：所有关节点位于 100×120 的虚拟坐标空间（y 轴向下，垫子上表面约 y=108）。
 * dots 为需要红色高亮标注的"发力点"（胸/臀等）。
 */
data class StickPose(
    val head: Offset,
    val neck: Offset,
    val shoulder: Offset,
    val hip: Offset,
    val elbow: Offset,
    val hand: Offset,
    val knee: Offset,
    val foot: Offset,
    val rearElbow: Offset,
    val rearHand: Offset,
    val rearKnee: Offset,
    val rearFoot: Offset,
    val dots: List<Offset> = emptyList(),
    val headRadius: Float = 8f
)

/** 两个关键帧之间的线性插值（用于动作循环动画）。 */
fun lerpPose(a: StickPose, b: StickPose, t: Float): StickPose {
    fun l(p: Offset, q: Offset) = Offset(p.x + (q.x - p.x) * t, p.y + (q.y - p.y) * t)
    return StickPose(
        head = l(a.head, b.head),
        neck = l(a.neck, b.neck),
        shoulder = l(a.shoulder, b.shoulder),
        hip = l(a.hip, b.hip),
        elbow = l(a.elbow, b.elbow),
        hand = l(a.hand, b.hand),
        knee = l(a.knee, b.knee),
        foot = l(a.foot, b.foot),
        rearElbow = l(a.rearElbow, b.rearElbow),
        rearHand = l(a.rearHand, b.rearHand),
        rearKnee = l(a.rearKnee, b.rearKnee),
        rearFoot = l(a.rearFoot, b.rearFoot),
        dots = a.dots.zip(b.dots) { p, q -> l(p, q) }.ifEmpty { a.dots },
        headRadius = a.headRadius
    )
}

/**
 * 示例动作的关键帧姿势库（侧视图，面朝右）。
 * 高亮段约定："thigh" 大腿发力 / "arm" 手臂与胸部发力。
 */
object ExercisePoses {

    // ---------- 深蹲：站立 → 下蹲 ----------

    val squatStart = StickPose(
        head = Offset(54f, 16f), neck = Offset(51f, 25f), shoulder = Offset(50f, 28f),
        hip = Offset(48f, 58f),
        elbow = Offset(41f, 42f), hand = Offset(40f, 54f),
        knee = Offset(49f, 84f), foot = Offset(57f, 107f),
        rearElbow = Offset(44f, 43f), rearHand = Offset(43f, 55f),
        rearKnee = Offset(47f, 84f), rearFoot = Offset(55f, 108f),
        dots = listOf(Offset(50f, 60f))
    )

    val squatEnd = StickPose(
        head = Offset(62f, 30f), neck = Offset(57f, 37f), shoulder = Offset(56f, 40f),
        hip = Offset(37f, 63f),
        elbow = Offset(70f, 46f), hand = Offset(84f, 44f),
        knee = Offset(52f, 83f), foot = Offset(57f, 107f),
        rearElbow = Offset(68f, 49f), rearHand = Offset(82f, 47f),
        rearKnee = Offset(50f, 85f), rearFoot = Offset(55f, 108f),
        dots = listOf(Offset(38f, 66f))
    )

    // ---------- 俯卧撑：撑起（平板） → 下压 ----------

    val pushupStart = StickPose(
        head = Offset(84f, 85f), neck = Offset(76f, 89f), shoulder = Offset(72f, 92f),
        hip = Offset(42f, 94f),
        elbow = Offset(72f, 100f), hand = Offset(72f, 107f),
        knee = Offset(24f, 97f), foot = Offset(12f, 105f),
        rearElbow = Offset(69f, 101f), rearHand = Offset(69f, 107f),
        rearKnee = Offset(22f, 99f), rearFoot = Offset(10f, 106f),
        dots = listOf(Offset(69f, 93f))
    )

    val pushupEnd = StickPose(
        head = Offset(68f, 96f), neck = Offset(61f, 100f), shoulder = Offset(56f, 103f),
        hip = Offset(27f, 101f),
        elbow = Offset(63f, 106f), hand = Offset(72f, 107f),
        knee = Offset(10f, 103f), foot = Offset(3f, 107f),
        rearElbow = Offset(60f, 107f), rearHand = Offset(69f, 108f),
        rearKnee = Offset(8f, 105f), rearFoot = Offset(1f, 108f),
        dots = listOf(Offset(52f, 104f))
    )

    /** 返回 (起始姿势, 结束姿势, 需要红色高亮的线段)。 */
    fun pair(illustration: String): Triple<StickPose, StickPose, Set<String>> = when (illustration) {
        "squat" -> Triple(squatStart, squatEnd, setOf("thigh"))
        else -> Triple(pushupStart, pushupEnd, setOf("arm"))
    }
}

/** 扁平插画人物的配色：色值集中在 theme/Color.kt 的插画资产色区（唯一色值容身处）。 */
private object FigurePalette {
    val Skin = com.yuedong.fitness.ui.theme.FigSkin
    val SkinShade = com.yuedong.fitness.ui.theme.FigSkinShade
    val Shorts = com.yuedong.fitness.ui.theme.FigShirt
    val ShortsShade = com.yuedong.fitness.ui.theme.FigShirtShade
    val Hair = com.yuedong.fitness.ui.theme.FigHair
    val Eye = com.yuedong.fitness.ui.theme.FigEye
    val Shoe = com.yuedong.fitness.ui.theme.FigShoe
    val Mat = com.yuedong.fitness.ui.theme.FigMat
    val MatEdge = com.yuedong.fitness.ui.theme.FigMatEdge
    val Highlight = com.yuedong.fitness.ui.theme.FigHighlight
}

/** 插画舞台：白底插画资产（AI 图/火柴人画布）统一落在这一底色上，卡片容器本身随主题 */
@Composable
private fun IllustrationStage(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .background(com.yuedong.fitness.ui.theme.FigCanvas),
        contentAlignment = Alignment.Center
    ) { content() }
}

/**
 * 扁平插画风格的动作人物画布（虚拟空间 100×120 等比缩放居中）。
 * @param ghosts 以低透明度预先绘制的起止姿势（动作轨迹残影）。
 * @param highlights 需要红色高亮的部位：thigh=大腿、arm=大臂
 */
@Composable
fun StickFigureCanvas(
    pose: StickPose,
    highlights: Set<String>,
    modifier: Modifier = Modifier,
    ghosts: List<StickPose> = emptyList()
) {
    Canvas(modifier) {
        val scale = min(size.width / 100f, size.height / 120f)
        val offsetX = (size.width - 100f * scale) / 2f
        val offsetY = (size.height - 120f * scale) / 2f

        // 垫子
        drawRoundRect(
            color = FigurePalette.Mat,
            topLeft = map(Offset(4f, 108f), scale, offsetX, offsetY),
            size = Size(92f * scale, 5.5f * scale),
            cornerRadius = CornerRadius(2.5f * scale)
        )
        drawRoundRect(
            color = FigurePalette.MatEdge,
            topLeft = map(Offset(4f, 112f), scale, offsetX, offsetY),
            size = Size(92f * scale, 1.6f * scale),
            cornerRadius = CornerRadius(0.8f * scale)
        )

        ghosts.forEach { ghost ->
            drawFlatFigure(ghost, highlights, 0.12f, scale, offsetX, offsetY)
        }
        drawFlatFigure(pose, highlights, 1f, scale, offsetX, offsetY)
    }
}

private fun map(p: Offset, scale: Float, ox: Float, oy: Float) =
    Offset(p.x * scale + ox, p.y * scale + oy)

private fun Offset.shift(dx: Float, dy: Float) = Offset(x + dx, y + dy)

/** 绘制一个扁平插画风格人物（近/远侧肢体、蓝短裤、头发、鞋子、肌肉高亮）。 */
private fun DrawScope.drawFlatFigure(
    pose: StickPose,
    highlights: Set<String>,
    alphaFactor: Float,
    scale: Float,
    ox: Float,
    oy: Float
) {
    fun m(p: Offset) = map(p, scale, ox, oy)
    fun a(color: Color) = color.copy(alpha = color.alpha * alphaFactor)
    val cap = StrokeCap.Round
    val limb = 5f * scale

    // 远侧手臂
    drawLine(a(FigurePalette.SkinShade), m(pose.shoulder), m(pose.rearElbow), limb * 0.85f, cap)
    drawLine(a(FigurePalette.SkinShade), m(pose.rearElbow), m(pose.rearHand), limb * 0.85f, cap)
    drawCircle(a(FigurePalette.SkinShade), 2.6f * scale, m(pose.rearHand))
    // 远侧腿
    drawLine(a(FigurePalette.SkinShade), m(pose.hip), m(pose.rearKnee), limb * 0.9f, cap)
    drawLine(a(FigurePalette.SkinShade), m(pose.rearKnee), m(pose.rearFoot), limb * 0.9f, cap)
    drawLine(a(FigurePalette.Shoe), m(pose.rearFoot), m(pose.rearFoot.shift(4.5f, 0f)), 3.4f * scale, cap)

    // 躯干
    drawLine(a(FigurePalette.Skin), m(pose.neck), m(pose.hip), 11f * scale, cap)

    // 近侧腿
    drawLine(a(FigurePalette.Skin), m(pose.hip), m(pose.knee), limb, cap)
    drawLine(a(FigurePalette.Skin), m(pose.knee), m(pose.foot), limb, cap)
    drawLine(a(FigurePalette.Shoe), m(pose.foot), m(pose.foot.shift(5f, 0f)), 3.8f * scale, cap)

    // 大腿发力高亮（画在短裤之下，露出膝盖段）
    if ("thigh" in highlights) {
        drawLine(
            FigurePalette.Highlight.copy(alpha = 0.7f * alphaFactor),
            m(pose.hip), m(pose.knee), (limb + 1.6f * scale), cap
        )
        drawLine(
            FigurePalette.Highlight.copy(alpha = 0.45f * alphaFactor),
            m(pose.hip), m(pose.rearKnee), (limb + 1.2f * scale), cap
        )
    }

    // 蓝色短裤（大腿上段）
    fun towards(from: Offset, to: Offset, fraction: Float) =
        Offset(from.x + (to.x - from.x) * fraction, from.y + (to.y - from.y) * fraction)
    drawLine(a(FigurePalette.Shorts), m(pose.hip), m(towards(pose.hip, pose.knee, 0.42f)), (limb + 1.8f * scale), cap)
    drawLine(
        a(FigurePalette.ShortsShade), m(pose.hip),
        m(towards(pose.hip, pose.rearKnee, 0.42f)), (limb + 1.5f * scale), cap
    )

    // 近侧手臂
    drawLine(a(FigurePalette.Skin), m(pose.shoulder), m(pose.elbow), limb * 0.95f, cap)
    drawLine(a(FigurePalette.Skin), m(pose.elbow), m(pose.hand), limb * 0.95f, cap)
    drawCircle(a(FigurePalette.Skin), 2.8f * scale, m(pose.hand))

    // 手臂发力高亮
    if ("arm" in highlights) {
        drawLine(
            FigurePalette.Highlight.copy(alpha = 0.7f * alphaFactor),
            m(pose.shoulder), m(pose.elbow), (limb + 1.6f * scale), cap
        )
    }

    // 头（头发在耳后 + 眼睛）
    drawCircle(a(FigurePalette.Hair), pose.headRadius * 0.95f * scale, m(pose.head.shift(-3f, -3f)))
    drawCircle(a(FigurePalette.Skin), pose.headRadius * scale, m(pose.head))
    drawCircle(a(FigurePalette.Eye), 0.9f * scale, m(pose.head.shift(3.4f, 0.6f)))

    // 发力点（胸/臀）
    pose.dots.forEach { dot ->
        drawCircle(FigurePalette.Highlight.copy(alpha = 0.85f * alphaFactor), 4.6f * scale, m(dot))
    }
}

/** 已有 AI 生成视频素材的动作（视频模式优先，其次图片，最后代码绘制）。 */
private fun videoAssetsFor(illustration: String): Int? = when (illustration) {
    "squat" -> R.raw.ex_squat
    "pushup" -> R.raw.ex_pushup
    "kneeling_pushup" -> R.raw.ex_kneeling_pushup
    "situp" -> R.raw.ex_crunch
    else -> null
}

/** 已有 AI 生成图片素材的动作（图片模式优先，其余回退到代码绘制）。 */
private data class ExerciseImages(val start: Int, val end: Int, val legend: String)

private fun imageAssetsFor(illustration: String): ExerciseImages? = when (illustration) {
    "squat" -> ExerciseImages(
        R.drawable.ex_squat_start,
        R.drawable.ex_squat_end,
        "🔴 红色 = 主要发力部位（大腿与臀部）"
    )
    else -> null
}

/**
 * 动作教学插图卡（入口）：视频 > 图片 > 火柴人，三级回退。
 */
@Composable
fun ExerciseIllustrationCard(
    illustration: String,
    modifier: Modifier = Modifier
) {
    val videoRes = remember(illustration) { videoAssetsFor(illustration) }
    val images = remember(illustration) { imageAssetsFor(illustration) }
    when {
        videoRes != null -> VideoExerciseIllustrationCard(
            videoRes = videoRes,
            legend = "🔴 红色 = 主要发力部位（大腿与臀部）",
            modifier = modifier
        )
        images != null -> ImageExerciseIllustrationCard(
            startRes = images.start,
            endRes = images.end,
            legend = images.legend,
            modifier = modifier
        )
        else -> StickExerciseIllustrationCard(illustration = illustration, modifier = modifier)
    }
}

/**
 * 图片版教学卡：起始/结束两张 AI 生成插画，循环交叉淡化形成动作动画（可暂停），
 * 下方保留两张定格缩略图。图片由用户用 AI 工具按统一风格生成，版权干净。
 */
@Composable
fun ImageExerciseIllustrationCard(
    startRes: Int,
    endRes: Int,
    legend: String,
    modifier: Modifier = Modifier
) {
    var playing by remember { mutableStateOf(true) }
    var pausedAt by remember { mutableStateOf(0f) }

    val progress by rememberInfiniteTransition(label = "exerciseImg").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "imgProgress"
    )
    val t = if (playing) progress else pausedAt

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        // 容器随主题（此前写死白底，暗色下 onSurface 文字不可读）；插画资产落在白底舞台
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "动作示范",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = {
                        if (playing) {
                            pausedAt = progress
                            playing = false
                        } else {
                            playing = true
                        }
                    }
                ) {
                    Icon(
                        if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playing) "暂停动画" else "播放动画",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // 两图交叉淡化：起始图常驻，结束图按动画进度淡入淡出
            IllustrationStage(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(678f / 900f)
            ) {
                Image(
                    painter = painterResource(startRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(endRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    alpha = t
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    IllustrationStage(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(678f / 900f)
                    ) {
                        Image(
                            painter = painterResource(startRes),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(678f / 900f),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        "起始姿势",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    IllustrationStage(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(678f / 900f)
                    ) {
                        Image(
                            painter = painterResource(endRes),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(678f / 900f),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        "结束姿势",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                legend,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 火柴人版教学卡（图片素材未覆盖的动作回退使用）：大号循环动画（可暂停，带起止残影）
 * + 起始/结束姿势缩略图 + 图例。纯 Canvas 绘制，无图片资源依赖。
 */
@Composable
private fun StickExerciseIllustrationCard(
    illustration: String,
    modifier: Modifier = Modifier
) {
    val (start, end, highlights) = remember(illustration) { ExercisePoses.pair(illustration) }
    var playing by remember { mutableStateOf(true) }

    val progress by rememberInfiniteTransition(label = "exercise").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "poseProgress"
    )
    val t = if (playing) progress else 0f
    val current = remember(t, playing) { lerpPose(start, end, t) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        // 容器随主题；火柴人画布落在白底舞台（插画资产色按白底设计）
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "动作示范",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { playing = !playing }) {
                    Icon(
                        if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playing) "暂停动画" else "播放动画",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IllustrationStage(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(100f / 115f)
            ) {
                StickFigureCanvas(
                    pose = current,
                    highlights = highlights,
                    ghosts = listOf(start, end),
                    modifier = Modifier.fillMaxWidth().aspectRatio(100f / 115f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    IllustrationStage(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(100f / 120f)
                    ) {
                        StickFigureCanvas(
                            pose = start,
                            highlights = highlights,
                            modifier = Modifier.fillMaxWidth().aspectRatio(100f / 120f)
                        )
                    }
                    Text(
                        "起始姿势",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    IllustrationStage(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(100f / 120f)
                    ) {
                        StickFigureCanvas(
                            pose = end,
                            highlights = highlights,
                            modifier = Modifier.fillMaxWidth().aspectRatio(100f / 120f)
                        )
                    }
                    Text(
                        "结束姿势",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "🔴 红色 = 主要发力部位 · 淡色影像 = 动作起止轨迹",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
