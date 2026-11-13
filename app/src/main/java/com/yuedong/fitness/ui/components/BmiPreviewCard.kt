package com.yuedong.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yuedong.fitness.domain.logic.FitnessCalculator
import com.yuedong.fitness.domain.model.BmiCategory
import com.yuedong.fitness.ui.theme.DiveDataColors
import com.yuedong.fitness.ui.theme.DiveMono

/**
 * BMI 分类卡：一行式「BMI ⓘ 数值 + 分类胶囊」+ 四色刻度条（白点标记）+ 四档区间。
 * ⓘ 点开弹窗：计算公式 + 分级依据（中国成人标准 WS/T 428-2013）。
 */
@Composable
fun BmiPreviewCard(bmi: Double, modifier: Modifier = Modifier) {
    val category = FitnessCalculator.bmiCategory(bmi)
    val categoryColor = bmiCategoryColor(category)
    val under = DiveDataColors.bmiUnder
    val normal = DiveDataColors.bmiNormal
    val over = DiveDataColors.bmiOver
    val obese = DiveDataColors.bmiObese
    var showInfo by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // 一行式：BMI ⓘ + 数值 + 分类胶囊（整行不换行）
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "BMI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Box(
                    Modifier
                        .padding(bottom = 3.dp)
                        .size(28.dp)
                        .clickable { showInfo = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = "BMI 计算说明",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(Modifier.width(2.dp))
                Text(
                    text = String.format("%.1f", bmi),
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = DiveMono,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            // 分类刻度条：显示范围 14~34，四段宽度按区间长度；竖向指针 = 当前位置
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val totalW = maxWidth
                fun frac(b: Double): Float = ((b - 14.0) / (34.0 - 14.0)).toFloat().coerceIn(0f, 1f)
                Box(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        Box(Modifier.weight(4.5f).fillMaxHeight().background(under))
                        Box(Modifier.weight(5.5f).fillMaxHeight().background(normal))
                        Box(Modifier.weight(4f).fillMaxHeight().background(over))
                        Box(Modifier.weight(6f).fillMaxHeight().background(obese))
                    }
                    // 位置指针：骑在刻度条上的竖向圆角指针，随分类着色
                    Box(
                        Modifier
                            .offset(x = totalW * frac(bmi) - 2.dp, y = (-4).dp)
                            .size(width = 4.dp, height = 18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(categoryColor)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(2.dp))
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            // 四档标签与刻度条分段同权重分布：每档文字对齐各自的色块
            Row(Modifier.fillMaxWidth()) {
                Box(Modifier.weight(4.5f), contentAlignment = Alignment.Center) {
                    CategoryMark("偏瘦", "<18.5", category == BmiCategory.UNDERWEIGHT, under)
                }
                Box(Modifier.weight(5.5f), contentAlignment = Alignment.Center) {
                    CategoryMark("正常", "18.5~23.9", category == BmiCategory.NORMAL, normal)
                }
                Box(Modifier.weight(4f), contentAlignment = Alignment.Center) {
                    CategoryMark("超重", "24~27.9", category == BmiCategory.OVERWEIGHT, over)
                }
                Box(Modifier.weight(6f), contentAlignment = Alignment.Center) {
                    CategoryMark("肥胖", "≥28", category == BmiCategory.OBESE, obese)
                }
            }
        }
    }

    if (showInfo) {
        YueDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("BMI 是怎么算的") },
            text = { BmiInfoContent() },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("知道了") }
            }
        )
    }
}

/** BMI 说明弹窗内容：计算公式 + 分级依据 */
@Composable
private fun BmiInfoContent() {
    Column {
        Text("计算公式", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "BMI = 体重（公斤）÷ 身高²（米）\n例：70.0 公斤 ÷（1.75 × 1.75 米）≈ 22.9",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text("分级依据", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "中国成人标准（WS/T 428-2013），男女通用：",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        BmiRangeLine("偏瘦", "< 18.5", DiveDataColors.bmiUnder)
        BmiRangeLine("正常", "18.5 ~ 23.9", DiveDataColors.bmiNormal)
        BmiRangeLine("超重", "24 ~ 27.9", DiveDataColors.bmiOver)
        BmiRangeLine("肥胖", "≥ 28", DiveDataColors.bmiObese)
        Spacer(Modifier.height(10.dp))
        Text(
            "BMI 只是初筛指标：肌肉量大者可能被高估，请结合体脂与腰围综合判断。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun BmiRangeLine(label: String, range: String, color: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text(
            range,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = DiveMono,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryMark(label: String, range: String, highlighted: Boolean, color: Color) {
    // 四档字号统一，只用颜色+加粗区分当前档，避免高亮档把行撑大
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (highlighted) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = range,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (highlighted) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun bmiCategoryColor(category: BmiCategory): Color = when (category) {
    BmiCategory.UNDERWEIGHT -> DiveDataColors.bmiUnder
    BmiCategory.NORMAL -> DiveDataColors.bmiNormal
    BmiCategory.OVERWEIGHT -> DiveDataColors.bmiOver
    BmiCategory.OBESE -> DiveDataColors.bmiObese
}
