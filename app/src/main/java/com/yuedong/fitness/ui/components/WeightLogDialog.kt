package com.yuedong.fitness.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import com.yuedong.fitness.ui.components.YueDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yuedong.fitness.ui.theme.DiveNumeric
import kotlin.math.roundToInt

/**
 * 今日体重记录弹窗（今日页与体重趋势页共用）：
 * 横向标尺选择，范围 30~300 公斤，步长 0.5。
 */
@Composable
fun WeightLogDialog(
    initial: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    val values = remember {
        buildList {
            var v = 30.0
            while (v <= 300.0) {
                add(Math.round(v * 10) / 10.0)
                v += 0.5
            }
        }
    }
    var selected by remember { mutableDoubleStateOf(initial.coerceIn(30.0, 300.0)) }

    YueDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录今天的体重") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        String.format("%.1f", selected),
                        style = DiveNumeric.large,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        " kg",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(
                    Modifier.height(12.dp)
                )
                HorizontalRulerPicker(
                    values = values,
                    selectedIndex = ((selected - 30.0) / 0.5).roundToInt()
                        .coerceIn(0, values.lastIndex),
                    labelText = { String.format("%.0f", it) },
                    onSelected = { selected = values[it] },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selected) }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
