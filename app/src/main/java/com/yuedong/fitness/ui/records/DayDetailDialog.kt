package com.yuedong.fitness.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import com.yuedong.fitness.ui.components.YueDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 心情等级 → 表情 */
fun moodEmoji(mood: Int): String = when (mood) {
    1 -> "😢"
    2 -> "😟"
    3 -> "😐"
    4 -> "🙂"
    else -> "😄"
}

/**
 * 某一天的详情弹窗：训练 / 饮食 / 体重 / 日记 四行小结。
 * 过去未打卡的日子显示「补打卡」按钮（onMakeUp）；补过的卡 planName 会是「补打卡」。
 */
@Composable
fun DayDetailDialog(
    date: LocalDate,
    today: LocalDate,
    history: HistoryItem?,
    dietStatus: String?,
    dietLogs: List<com.yuedong.fitness.data.db.entity.DietLogEntity>,
    weightKg: Double?,
    onDismiss: () -> Unit,
    onMakeUp: () -> Unit = {}
) {
    val dateText = date.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA))
    val kcal = dietLogs.sumOf { it.calories }
    val canMakeUp = date.isBefore(today) && history == null

    YueDialog(
        onDismissRequest = onDismiss,
        title = { Text(dateText) },
        text = {
            if (date.isAfter(today)) {
                Text(
                    "还没到这一天，先专注今天吧 😄",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // 训练
                    DetailRow(
                        label = if (history != null) "✓ 已打卡" else "未打卡",
                        detail = history?.planName?.takeIf { it != "已删除的计划" }.orEmpty(),
                        highlight = history != null
                    )
                    // 饮食
                    DetailRow(
                        label = if (dietLogs.isEmpty()) "未记录饮食" else "吃了 $kcal 千卡",
                        detail = when {
                            dietLogs.isEmpty() -> "没有留下饮食记录"
                            dietStatus == "good" -> "在目标范围内，吃得不错"
                            dietStatus == "over" -> "超出当日目标了"
                            else -> "共 ${dietLogs.size} 笔"
                        },
                        highlight = dietStatus == "good"
                    )
                    // 体重
                    DetailRow(
                        label = if (weightKg != null) "体重 $weightKg 公斤" else "没记体重",
                        detail = if (weightKg != null) "当天记录" else "趋势图会沿用之前的数值",
                        highlight = weightKg != null
                    )
                    // 日记
                    if (history?.mood != null || !history?.diaryText.isNullOrBlank()) {
                        DetailRow(
                            label = (history?.mood?.let { moodEmoji(it) } ?: "📝") +
                                (history?.diaryText?.takeIf { it.isNotBlank() }?.let { " $it" } ?: " 写了日记"),
                            detail = "",
                            highlight = false
                        )
                    } else {
                        DetailRow(label = "没写日记", detail = "在「今日」页可以补一句感受", highlight = false)
                    }
                    if (canMakeUp) {
                        Text(
                            "漏打的这一天可以补上，连击不断",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row {
                if (canMakeUp) {
                    Button(onClick = onMakeUp) { Text("补打卡") }
                    Spacer(Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) { Text(if (canMakeUp) "取消" else "知道了") }
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, detail: String, highlight: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        if (detail.isNotBlank()) {
            Spacer(Modifier.width(8.dp))
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
