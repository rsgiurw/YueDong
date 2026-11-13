package com.yuedong.fitness.ui.diet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.domain.logic.FoodCatalog
import com.yuedong.fitness.ui.components.EmptyState
import com.yuedong.fitness.ui.theme.DiveMono
import com.yuedong.fitness.domain.logic.FoodInsights
import com.yuedong.fitness.domain.logic.FoodPairings
import com.yuedong.fitness.ui.theme.DiveDataColors
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
private fun kcalTone(kcal: Int): Color = when {
    kcal <= 120 -> DiveDataColors.levelLow
    kcal <= 300 -> DiveDataColors.levelMid
    else -> DiveDataColors.levelHigh
}

private fun kcalLabel(kcal: Int): String = when {
    kcal <= 120 -> "低热量"
    kcal <= 300 -> "中热量"
    else -> "较高热量"
}

@Composable
private fun recommendLevel(food: FoodCatalog.Food): Pair<Color, String> = when {
    food.kcal <= 150 && food.fat <= 10 -> DiveDataColors.levelLow to "建议食用"
    food.kcal <= 300 && food.fat <= 20 -> DiveDataColors.levelMid to "适量食用"
    else -> DiveDataColors.levelNeutral to "浅尝即可"
}

private fun fmt1(v: Double): String {
    val r = (v * 10).roundToInt() / 10.0
    return if (r % 1.0 == 0.0) "${r.toInt()}" else "$r"
}

/** 段落存储格式：段与段 \u001E 分隔，标题与正文 \u001F 分隔 */
private fun parseSections(raw: String?): List<Pair<String, String>> =
    raw?.split('\u001E')
        ?.filter { it.isNotBlank() }
        ?.map { entry ->
            val idx = entry.indexOf('\u001F')
            if (idx >= 0) entry.take(idx) to entry.substring(idx + 1) else entry to ""
        }
        ?: emptyList()

/** 详情页轻量 ViewModel：取用户当前体重 + 按名称观察同名自定义食物（图片/段落随库实时更新） */
class FoodDetailViewModel(
    private val dietLogRepository: com.yuedong.fitness.data.repository.DietLogRepository,
    bodyMetricRepository: com.yuedong.fitness.data.repository.BodyMetricRepository,
    profileRepository: com.yuedong.fitness.data.repository.ProfileRepository,
    name: String
) : ViewModel() {
    val weightKg: StateFlow<Double> = combine(
        bodyMetricRepository.observeLatest(),
        profileRepository.observeProfile()
    ) { latest, profile ->
        latest?.weightKg ?: profile?.currentWeightKg ?: 60.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 60.0)

    val liveCustom: StateFlow<com.yuedong.fitness.data.db.entity.CustomFoodEntity?> =
        dietLogRepository.observeCustomFoods()
            .map { list -> list.firstOrNull { it.name == name } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        fun factory(name: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as YueDongApplication
                FoodDetailViewModel(
                    app.container.dietLogRepository,
                    app.container.bodyMetricRepository,
                    app.container.profileRepository,
                    name
                )
            }
        }
    }
}

/**
 * 食物详情页（差异化设计）：
 * 运动消耗按用户当前体重与 MET 标准实时计算；运动图标为自绘矢量，全机型渲染一致。
 */
@Composable
fun FoodDetailScreen(
    name: String,
    onBack: () -> Unit,
    /** 传入则为自定义食物：按其营养值生成全部详情内容 */
    custom: com.yuedong.fitness.data.db.entity.CustomFoodEntity? = null,
    /** 自定义食物的编辑入口（顶栏铅笔） */
    /** 浏览模式（从记录饮食进入）：不显示编辑入口 */
    viewOnly: Boolean = false,
    onEdit: (() -> Unit)? = null,
    viewModel: FoodDetailViewModel = viewModel(
        key = "food_detail_vm_$name",
        factory = FoodDetailViewModel.factory(name)
    )
) {
    val liveCustom by viewModel.liveCustom.collectAsState()
    // 显式传入优先（库内覆盖层），否则按名称从库里自加载（路由入口也能看到图片与段落）
    val resolved = custom ?: liveCustom
    val food = resolved?.let {
        FoodCatalog.Food(
            name = it.name,
            category = "我的自定义",
            emoji = "\u2B50",
            kcal = it.kcalPer100g,
            protein = it.proteinPer100g,
            carb = it.carbPer100g,
            fat = it.fatPer100g
        )
    } ?: remember(name) { FoodCatalog.find(name) }
    val weightKg by viewModel.weightKg.collectAsState()
    if (food == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            EmptyState("没有找到该食物")
        }
        return
    }

    val kj = (food.kcal * 4.184).roundToInt()
    val tone = kcalTone(food.kcal)
    val pKcal = food.protein * 4
    val cKcal = food.carb * 4
    val fKcal = food.fat * 9
    val total = (pKcal + cKcal + fKcal).coerceAtLeast(0.001)
    val pShare = (pKcal / total * 100).roundToInt()
    val cShare = (cKcal / total * 100).roundToInt()
    val fShare = 100 - pShare - cShare
    val tags = FoodInsights.tags(food)
    val insight = FoodInsights.insight(food)
    val suitable = FoodInsights.suitableFor(food)
    val exercise = FoodInsights.exerciseMinutes(food, weightKg)
    val tip = FoodInsights.tip(food)
    val (levelColor, levelText) = recommendLevel(food)
    val tone2 = levelColor
    val customDescription = resolved?.description?.trim().orEmpty()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text("食物详情", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            if (resolved != null && onEdit != null && !viewOnly) {
                IconButton(onClick = onEdit) {
                    Icon(androidx.compose.material.icons.Icons.Filled.Edit, contentDescription = "编辑")
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imgRes = foodImages[food.name]
            val customImagePath = resolved?.imagePath
            Box(
                Modifier.size(88.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (customImagePath != null) {
                    com.yuedong.fitness.ui.components.LocalMediaImage(
                        path = customImagePath,
                        contentDescription = food.name,
                        modifier = Modifier.size(88.dp).clip(CircleShape)
                    )
                } else if (imgRes != null) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(imgRes),
                        contentDescription = food.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.size(88.dp).clip(CircleShape)
                    )
                } else {
                    androidx.compose.material3.Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = food.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(food.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SmallChip(food.category, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.colorScheme.primary)
                SmallChip("每100${food.unit}", MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), MaterialTheme.colorScheme.onSurfaceVariant)
                SmallChip(levelText, levelColor.copy(alpha = 0.12f), levelColor)
            }
        }
        Spacer(Modifier.height(16.dp))

        // 自定义食物的介绍（编辑弹窗里填写）
        if (customDescription.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            SectionCard("介绍") {
                Text(
                    customDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // 自定义图文段落（编辑里添加的「标题 + 内容」）
        parseSections(resolved?.sections).forEach { section ->
            SectionCard(section.first.ifBlank { "详情" }) {
                Text(
                    section.second,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // 运动消耗对照：自绘矢量图标 + 按当前体重与 MET 实时计算
        SectionCard("吃下去，运动多久消耗掉？") {
            Row(Modifier.fillMaxWidth()) {
                exercise.forEach { (label, min, _) ->
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (label.contains("跳")) {
                            ExerciseIcon(label)
                        } else if (label.contains("走")) {
                            Icon(
                                Icons.Filled.DirectionsWalk,
                                contentDescription = label,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        } else {
                            Icon(
                                Icons.Filled.DirectionsRun,
                                contentDescription = label,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "$min 分钟",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DiveMono,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "按你当前体重 ${fmt1(weightKg)}kg 与运动强度（MET）估算，个体存在差异",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(12.dp))

        SectionCard("三大供能营养素") {
            if (total <= 0.001) {
                Text("暂无宏量营养素数据", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                ) {
                    if (pKcal > 0) Box(Modifier.weight(pKcal.toFloat()).background(DiveDataColors.protein))
                    if (cKcal > 0) Box(Modifier.weight(cKcal.toFloat()).background(DiveDataColors.carb))
                    if (fKcal > 0) Box(Modifier.weight(fKcal.toFloat()).background(DiveDataColors.fat))
                }
                Spacer(Modifier.height(10.dp))
                MacroLegend("蛋白质", food.protein, pShare, DiveDataColors.protein)
                MacroLegend("碳水化合物", food.carb, cShare, DiveDataColors.carb)
                MacroLegend("脂肪", food.fat, fShare, DiveDataColors.fat)
                Spacer(Modifier.height(8.dp))
                Text(
                    "条形为供能占比，非重量占比（脂肪供能效率是碳水的 2.25 倍）",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        food.gi?.let { gi ->
            SectionCard("升糖反应（GI）") {
                GiScale(gi)
                Spacer(Modifier.height(8.dp))
                Text(
                    when {
                        gi <= 55 -> "低升糖食物：血糖波动平缓，控糖人群友好。"
                        gi <= 70 -> "中升糖食物：搭配蛋白质或蔬菜一起吃更平稳。"
                        else -> "高升糖食物：单独大量食用血糖波动较大，运动后补充较合适。"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // 常见搭配是内置食物的静态数据，自建食物不展示（图文段落由用户填写）
        if (resolved == null) {
            SectionCard("常见搭配") {
                Text(FoodPairings.pairingOf(food), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
        }
        SectionCard("营养特点") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.take(3).forEach { tag ->
                    SmallChip(tag, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            if (tags.size > 3) {
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.drop(3).forEach { tag ->
                        SmallChip(tag, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(insight, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))

        SectionCard("食用建议") {
            Text(tip, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(28.dp))
    }
}

/** 运动图标：自绘矢量火柴人（跑 / 跳绳 / 走），不依赖系统 emoji，全机型渲染一致 */
@Composable
private fun ExerciseIcon(kind: String) {
    val c = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(width = 44.dp, height = 40.dp)) {
        val w = size.width
        val h = size.height
        val sw = 5f
        val headR = 9f
        when {
            kind.contains("跳") -> {
                // 跳绳：站立模型（与旁边一致），双臂屈肘持柄，绳经脚下
                val skin = com.yuedong.fitness.ui.theme.FigSkinWarm
                val hair = com.yuedong.fitness.ui.theme.FigHairBrown
                val shirt = com.yuedong.fitness.ui.theme.FigShirtOrange
                val pants = com.yuedong.fitness.ui.theme.FigPantsGray
                val ropeC = com.yuedong.fitness.ui.theme.FigRopeBlue
                val limb = (w * 0.07f).coerceAtLeast(5f)
                drawCircle(skin, headR * 1.1f, Offset(w * 0.5f, h * 0.13f))
                drawArc(hair, startAngle = 180f, sweepAngle = 180f, useCenter = true,
                    topLeft = Offset(w * 0.5f - headR * 1.1f, h * 0.13f - headR * 1.1f),
                    size = Size(headR * 2.2f, headR * 2.2f))
                drawLine(shirt, Offset(w * 0.5f, h * 0.21f), Offset(w * 0.5f, h * 0.48f), w * 0.14f, StrokeCap.Round)
                drawLine(shirt, Offset(w * 0.5f, h * 0.25f), Offset(w * 0.30f, h * 0.36f), w * 0.09f, StrokeCap.Round)
                drawLine(shirt, Offset(w * 0.5f, h * 0.25f), Offset(w * 0.70f, h * 0.36f), w * 0.09f, StrokeCap.Round)
                drawLine(pants, Offset(w * 0.5f, h * 0.48f), Offset(w * 0.40f, h * 0.68f), w * 0.10f, StrokeCap.Round)
                drawLine(pants, Offset(w * 0.40f, h * 0.68f), Offset(w * 0.40f, h * 0.85f), w * 0.09f, StrokeCap.Round)
                drawLine(pants, Offset(w * 0.5f, h * 0.48f), Offset(w * 0.60f, h * 0.68f), w * 0.10f, StrokeCap.Round)
                drawLine(pants, Offset(w * 0.60f, h * 0.68f), Offset(w * 0.62f, h * 0.85f), w * 0.09f, StrokeCap.Round)
                val ropePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.26f, h * 0.32f)
                    quadraticTo(w * 0.5f, h * 1.02f, w * 0.74f, h * 0.32f)
                }
                drawPath(ropePath, ropeC, style = Stroke(w * 0.04f, cap = StrokeCap.Round))
            }
            kind.contains("走") -> {
                val skin = com.yuedong.fitness.ui.theme.FigSkinWarm
                val hair = com.yuedong.fitness.ui.theme.FigHairBrown
                val shirt = com.yuedong.fitness.ui.theme.FigShirtOrange
                val pants = com.yuedong.fitness.ui.theme.FigPantsGray
                drawCircle(skin, headR * 1.1f, Offset(w * 0.44f, h * 0.14f))
                drawArc(hair, startAngle = 180f, sweepAngle = 180f, useCenter = true,
                    topLeft = Offset(w * 0.44f - headR * 1.1f, h * 0.14f - headR * 1.1f),
                    size = Size(headR * 2.2f, headR * 2.2f))
                drawLine(shirt, Offset(w * 0.44f, h * 0.24f), Offset(w * 0.5f, h * 0.50f), w * 0.16f, StrokeCap.Round)
                drawLine(shirt, Offset(w * 0.45f, h * 0.30f), Offset(w * 0.66f, h * 0.42f), w * 0.09f, StrokeCap.Round)
                drawLine(shirt, Offset(w * 0.45f, h * 0.30f), Offset(w * 0.26f, h * 0.44f), w * 0.09f, StrokeCap.Round)
                drawLine(pants, Offset(w * 0.5f, h * 0.50f), Offset(w * 0.38f, h * 0.70f), w * 0.10f, StrokeCap.Round)
                drawLine(pants, Offset(w * 0.38f, h * 0.70f), Offset(w * 0.34f, h * 0.86f), w * 0.09f, StrokeCap.Round)
                drawLine(pants, Offset(w * 0.5f, h * 0.50f), Offset(w * 0.62f, h * 0.68f), w * 0.10f, StrokeCap.Round)
                drawLine(pants, Offset(w * 0.62f, h * 0.68f), Offset(w * 0.74f, h * 0.84f), w * 0.09f, StrokeCap.Round)
            }
            else -> {
                val skin = com.yuedong.fitness.ui.theme.FigSkinWarm
                val hair = com.yuedong.fitness.ui.theme.FigHairBrown
                val shirt = com.yuedong.fitness.ui.theme.FigShirtOrange
                val pants = com.yuedong.fitness.ui.theme.FigPantsGray
                drawCircle(skin, headR * 1.1f, Offset(w * 0.46f, h * 0.14f))
                drawArc(hair, startAngle = 160f, sweepAngle = 200f, useCenter = true,
                    topLeft = Offset(w * 0.46f - headR * 1.1f, h * 0.14f - headR * 1.1f),
                    size = Size(headR * 2.2f, headR * 2.2f))
                drawLine(shirt, Offset(w * 0.48f, h * 0.24f), Offset(w * 0.56f, h * 0.52f), w * 0.16f, StrokeCap.Round)
                drawLine(shirt, Offset(w * 0.5f, h * 0.30f), Offset(w * 0.72f, h * 0.38f), w * 0.09f, StrokeCap.Round)
                drawLine(shirt, Offset(w * 0.5f, h * 0.30f), Offset(w * 0.30f, h * 0.40f), w * 0.09f, StrokeCap.Round)
                drawLine(pants, Offset(w * 0.56f, h * 0.52f), Offset(w * 0.40f, h * 0.72f), w * 0.10f, StrokeCap.Round)
                drawLine(pants, Offset(w * 0.40f, h * 0.72f), Offset(w * 0.28f, h * 0.88f), w * 0.09f, StrokeCap.Round)
                drawLine(pants, Offset(w * 0.56f, h * 0.52f), Offset(w * 0.72f, h * 0.68f), w * 0.10f, StrokeCap.Round)
                drawLine(pants, Offset(w * 0.72f, h * 0.68f), Offset(w * 0.80f, h * 0.86f), w * 0.09f, StrokeCap.Round)
            }
        }
    }
}

/** GI 渐变标尺 + 指针 */
@Composable
private fun GiScale(gi: Int) {
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(50))
        ) {
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(0.55f).background(DiveDataColors.levelLow))
                Box(Modifier.weight(0.15f).background(DiveDataColors.levelMid))
                Box(Modifier.weight(0.30f).background(DiveDataColors.levelHigh))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text("55", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text("70", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1.5f))
            Text("100", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(2.dp))
        val pos = (gi / 100f).coerceIn(0.04f, 0.96f)
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(pos.coerceAtLeast(0.001f)))
            Text(
                "▲ 当前 $gi",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = DiveMono,
                color = when {
                    gi <= 55 -> DiveDataColors.levelLow
                    gi <= 70 -> DiveDataColors.levelMid
                    else -> DiveDataColors.levelHigh
                }
            )
            Spacer(Modifier.weight((1f - pos).coerceAtLeast(0.001f)))
        }
    }
}

@Composable
private fun SectionCard(title: String? = null, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            title?.let {
                Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
            }
            content()
        }
    }
}

@Composable
private fun SmallChip(text: String, bg: Color, fg: Color) {    Box(
        Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MacroLegend(label: String, grams: Double, share: Int, color: Color) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(
            "${fmt1(grams)}g · $share%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}