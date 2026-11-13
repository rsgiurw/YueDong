package com.yuedong.fitness.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuedong.fitness.R
import com.yuedong.fitness.domain.model.Gender
import com.yuedong.fitness.domain.model.Purpose
import com.yuedong.fitness.domain.validation.InputValidator
import com.yuedong.fitness.domain.validation.ValidationResult
import com.yuedong.fitness.ui.components.AppMedia
import com.yuedong.fitness.ui.components.ImageCropOverlay
import com.yuedong.fitness.ui.components.LocalMediaImage
import com.yuedong.fitness.ui.components.YueDialog
import kotlinx.coroutines.launch

/** 修改资料:信息行式,每行点击弹底部弹层修改,确认后即时生效。 */
@Composable
fun ProfileEditScreen(
    onBack: () -> Unit,
    viewModel: ProfileEditViewModel = viewModel(factory = ProfileEditViewModel.factory())
) {
    val profile by viewModel.profile.collectAsState()
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var cropBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var editField by remember { mutableStateOf<String?>(null) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = AppMedia.loadBitmap(context.applicationContext, uri)
                if (bmp != null) cropBitmap = bmp
            }
        }
    }

    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            // 顶栏:返回 | 标题居中
            Box(Modifier.fillMaxWidth().height(56.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text(
                    "修改资料",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 分区说明
            Text(
                "以下信息用于计算热量与营养目标",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            )

            // 头像
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val p = profile
                Box(Modifier.size(72.dp)) {
                    if (p?.avatarPath != null) {
                        LocalMediaImage(
                            path = p.avatarPath,
                            contentDescription = "头像",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Image(
                            painter = painterResource(
                                if (profile?.gender == "FEMALE") R.drawable.avatar_default_female
                                else R.drawable.avatar_default_male
                            ),
                            contentDescription = "默认头像",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("头像", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (profile?.avatarPath != null) "点击更换" else "点击设置",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

            // 昵称
            SettingRow("昵称", profile?.nickname ?: "悦动用户") { editField = "nickname" }
            RowDivider()

            // 性别(展示;修改走性别行弹层)
            SettingRow("性别", Gender.fromValue(profile?.gender ?: "MALE")?.label ?: "男") { editField = "gender" }
            RowDivider()

            // 年龄
            SettingRow("年龄", (profile?.age ?: 0).toString() + " 岁") { editField = "age" }
            RowDivider()

            // 身高
            SettingRow("身高", (profile?.heightCm ?: 0).toString() + " cm") { editField = "height" }
            RowDivider()

            // 健身目的
            SettingRow(
                "健身目的",
                Purpose.fromValue(profile?.purpose ?: "HEALTH")?.label ?: "健康"
            ) { editField = "purpose" }
            RowDivider()

            // 每周运动天数
            SettingRow("每周运动天数", "每周 " + (profile?.weeklyExerciseDays ?: 3) + " 天") { editField = "weekly" }
            RowDivider()

            // 体重(当前 + 目标)
            SettingRow(
                "体重",
                "当前 " + String.format(java.util.Locale.CHINA, "%.1f", profile?.currentWeightKg ?: 0.0) +
                    " kg · 目标 " + String.format(java.util.Locale.CHINA, "%.0f", profile?.targetWeightKg ?: 0.0) + " kg"
            ) { editField = "weight" }

            Spacer(Modifier.weight(1f))

            // 底部说明
            Text(
                "所有数据仅保存在这台手机上",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // ===== 字段弹层 =====
        when (editField) {
            "nickname" -> {
                var text by remember { mutableStateOf(profile?.nickname ?: "") }
                YueDialog(
                    onDismissRequest = { editField = null },
                    title = { Text("昵称", fontWeight = FontWeight.Bold) },
                    text = {
                        OutlinedTextField(
                            shape = MaterialTheme.shapes.small,
                            value = text,
                            onValueChange = { text = it.take(16) },
                            label = { Text("昵称(最多 16 字)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        DialogAction("确定") {
                            viewModel.applyNickname(text)
                            editField = null
                        }
                    },
                    dismissButton = { DialogAction("取消") { editField = null } }
                )
            }
            "gender" -> {
                YueDialog(
                    onDismissRequest = { editField = null },
                    title = { Text("性别", fontWeight = FontWeight.Bold) },
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Gender.entries.forEach { g ->
                                FilterChip(
                                    selected = profile?.gender == g.name,
                                    onClick = {
                                        viewModel.applyGender(g)
                                        editField = null
                                    },
                                    label = { Text(g.label) }
                                )
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        Text(
                            "取消",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .clickable { editField = null }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                )
            }
            "age" -> {
                NumberEditDialog(
                    title = "年龄",
                    label = "年龄(岁)",
                    initial = (profile?.age ?: 0).toString(),
                    validate = { InputValidator.validateAge(it) },
                    onConfirm = { viewModel.applyAge(it) },
                    onDismiss = { editField = null }
                )
            }
            "height" -> {
                NumberEditDialog(
                    title = "身高",
                    label = "身高(厘米)",
                    initial = (profile?.heightCm ?: 0).toString(),
                    validate = { InputValidator.validateHeight(it) },
                    onConfirm = { viewModel.applyHeight(it) },
                    onDismiss = { editField = null }
                )
            }
            "purpose" -> {
                YueDialog(
                    onDismissRequest = { editField = null },
                    title = { Text("健身目的", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Purpose.entries.forEach { p ->
                                FilterChip(
                                    selected = profile?.purpose == p.name,
                                    onClick = {
                                        viewModel.applyPurpose(p)
                                        editField = null
                                    },
                                    label = { Text(p.label) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        Text(
                            "取消",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .clickable { editField = null }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                )
            }
            "weekly" -> {
                var days by remember { mutableStateOf(profile?.weeklyExerciseDays ?: 3) }
                YueDialog(
                    onDismissRequest = { editField = null },
                    title = { Text("每周运动天数", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(
                                "每周 $days 天",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Slider(
                                value = days.toFloat(),
                                onValueChange = { days = it.toInt() },
                                valueRange = 0f..7f,
                                steps = 6
                            )
                        }
                    },
                    confirmButton = {
                        DialogAction("完成") {
                            viewModel.applyWeeklyDays(days)
                            editField = null
                        }
                    },
                    dismissButton = { DialogAction("取消") { editField = null } }
                )
            }
            "weight" -> {
                var currentText by remember { mutableStateOf(profile?.currentWeightKg?.toString() ?: "") }
                var targetText by remember { mutableStateOf(profile?.targetWeightKg?.toString() ?: "") }
                var err by remember { mutableStateOf<String?>(null) }
                YueDialog(
                    onDismissRequest = { editField = null },
                    title = { Text("体重", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            OutlinedTextField(
                                shape = MaterialTheme.shapes.small,
                                value = currentText,
                                onValueChange = { currentText = it; err = null },
                                label = { Text("当前体重(公斤)") },
                                isError = err != null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                shape = MaterialTheme.shapes.small,
                                value = targetText,
                                onValueChange = { targetText = it; err = null },
                                label = { Text("目标体重(公斤)") },
                                isError = err != null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            err?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    confirmButton = {
                        DialogAction("确定") {
                            val cur = currentText.trim().toDoubleOrNull()
                            val tgt = targetText.trim().toDoubleOrNull()
                            when {
                                cur == null || cur < 30 || cur > 300 -> err = "当前体重需在 30~300 之间"
                                tgt == null || tgt < 30 || tgt > 300 -> err = "目标体重需在 30~300 之间"
                                else -> {
                                    err = viewModel.applyWeights(cur.toString(), tgt.toString())
                                    if (err == null) editField = null
                                }
                            }
                        }
                    },
                    dismissButton = { DialogAction("取消") { editField = null } }
                )
            }
        }

        // 头像裁剪
        cropBitmap?.let { bmp ->
            ImageCropOverlay(
                source = bmp,
                aspect = 1f,
                onConfirm = { cropped ->
                    scope.launch {
                        val path = AppMedia.saveBitmap(context.applicationContext, cropped)
                        if (path != null) {
                            AppMedia.delete(profile?.avatarPath)
                            viewModel.applyAvatar(path)
                        }
                        cropBitmap = null
                    }
                },
                onDismiss = { cropBitmap = null }
            )
        }
    }
}

@Composable
private fun DialogAction(text: String, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

/** 信息行:左标题 + 右当前值 + 箭头 */
@Composable
private fun SettingRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f),
        modifier = Modifier.padding(start = 20.dp)
    )
}

/** 数值编辑弹层:输入 + 校验,合法后确定生效 */
@Composable
private fun NumberEditDialog(
    title: String,
    label: String,
    initial: String,
    validate: (String) -> ValidationResult<*>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    var err by remember { mutableStateOf<String?>(null) }
    YueDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    shape = MaterialTheme.shapes.small,
                    value = text,
                    onValueChange = {
                        text = it
                        err = null
                    },
                    label = { Text(label) },
                    isError = err != null,
                    supportingText = err?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            DialogAction("确定") {
                val r = validate(text)
                // 合法即保存并收起弹层；键盘由 YueDialog onDispose 统一收起
                if (r is ValidationResult.Invalid) err = r.message else {
                    onConfirm(text)
                    onDismiss()
                }
            }
        },
        dismissButton = { DialogAction("取消") { onDismiss() } }
    )
}
