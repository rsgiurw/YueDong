package com.yuedong.fitness.ui.diet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import com.yuedong.fitness.ui.components.YueDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * 新建/覆盖自定义食物弹窗（饮食记录与食材库共用）。
 * 传入 existing 时为编辑模式（预填当前值，保存即覆盖同名条目）。
 * 营养值按每 100 克填写；负数会被就地拦截。
 */
@Composable
fun CustomFoodDialog(
    onSave: (name: String, kcal: Int, protein: Double, carb: Double, fat: Double, description: String) -> Unit,
    onDismiss: () -> Unit,
    initial: com.yuedong.fitness.data.db.entity.CustomFoodEntity? = null
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var kcal by remember { mutableStateOf(initial?.kcalPer100g?.toString() ?: "") }
    var protein by remember { mutableStateOf(initial?.proteinPer100g?.toString() ?: "") }
    var carb by remember { mutableStateOf(initial?.carbPer100g?.toString() ?: "") }
    var fat by remember { mutableStateOf(initial?.fatPer100g?.toString() ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    fun toDouble(text: String): Double? = text.trim().toDoubleOrNull()

    YueDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建自定义食物") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "营养值按每 100 克填写，可参考包装营养成分表",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    shape = MaterialTheme.shapes.small,
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("食物名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    shape = MaterialTheme.shapes.small,
                    value = kcal,
                    onValueChange = { kcal = it },
                    placeholder = { Text("热量（千卡/100克）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.small,
                        value = protein,
                        onValueChange = { protein = it },
                        placeholder = { Text("蛋白 g") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.small,
                        value = carb,
                        onValueChange = { carb = it },
                        placeholder = { Text("碳水 g") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.small,
                        value = fat,
                        onValueChange = { fat = it },
                        placeholder = { Text("脂肪 g") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    shape = MaterialTheme.shapes.small,
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("介绍（口味、食用建议等，选填）") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val kcalValue = toDouble(kcal)
                val proteinValue = toDouble(protein) ?: 0.0
                val carbValue = toDouble(carb) ?: 0.0
                val fatValue = toDouble(fat) ?: 0.0
                when {
                    name.trim().isEmpty() -> error = "请填写食物名称"
                    kcalValue == null || kcalValue < 0 -> error = "热量不能为负数，请填写有效数值"
                    proteinValue < 0 || carbValue < 0 || fatValue < 0 ->
                        error = "蛋白质/碳水/脂肪不能为负数"
                    else -> onSave(name.trim(), kcalValue.roundToInt(), proteinValue, carbValue, fatValue, description.trim())
                }
            }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
