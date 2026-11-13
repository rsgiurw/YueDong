package com.yuedong.fitness.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.AssistChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yuedong.fitness.BuildConfig
import com.yuedong.fitness.domain.update.AppUpdater
import com.yuedong.fitness.domain.update.UpdateInfo
import com.yuedong.fitness.ui.components.YueToaster
import com.yuedong.fitness.ui.theme.DiveMenuAccents
import com.yuedong.fitness.ui.update.UpdateDialog
import kotlinx.coroutines.launch

/** 关于悦动:版本 + 简介 + 数据隐私说明(从设置页独立)。 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenGuide: () -> Unit = {},
    onOpenBackup: () -> Unit = {},
    onOpenRecycleBin: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var manualUpdate by remember { mutableStateOf<UpdateInfo?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text("关于悦动", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "悦动 v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(10.dp))
                TrustBadge("数据本地")
                Spacer(Modifier.width(6.dp))
                TrustBadge("零上传")
            }
            Spacer(Modifier.height(10.dp))
            // 手动检查更新（启动时也会静默检查一次；数据不离开手机）
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !checking) {
                        checking = true
                        scope.launch {
                            val result = runCatching { AppUpdater.checkForUpdate() }
                            checking = false
                            when {
                                result.isFailure -> YueToaster.show("检查更新失败,请稍后重试")
                                result.getOrNull() == null ->
                                    YueToaster.show("已是最新版本 v${BuildConfig.VERSION_NAME}")
                                else -> manualUpdate = result.getOrNull()
                            }
                        }
                    }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .background(DiveMenuAccents.purple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (checking) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("检查更新", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "当前 v${BuildConfig.VERSION_NAME} · 启动时也会自动检查",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!checking) {
                    Text(
                        "检查",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            AboutLine("简介", "一款纯本地的健身记录应用:训练计划、每日打卡、体重趋势、饮食记录与热量建议")
            AboutLine("素材声明", "动作示范视频由 AI 生成;食物图片来自 foodiesfeed(CC0 公有领域);等宽字体为 JetBrains Mono(OFL 开源许可)")
            AboutLine("数据隐私", "所有数据仅保存在手机本地,不上传任何服务器;仅检查更新时联网读取版本信息,不发送任何个人数据;卸载应用即彻底删除")
            AboutLine("免责", "计划与建议仅供参考,不构成医疗意见;如有不适请停止训练并咨询专业人士")
            Spacer(Modifier.height(14.dp))
            AboutLine("本版更新", "应用内检查更新与一键升级、部位标签与多选、动作/食材详情浏览直达、营养建议圆环、新手指南全新改版")
            AboutLine("近期更新", "克数 50 克步进、深色模式可读性修复、训练提醒调度修复")
            Spacer(Modifier.height(14.dp))
            AboutLine("数据管理", "「数据与备份」可导出 zip 或导入恢复;删错的内容到「回收站」找回")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AssistChip(
                    onClick = onOpenBackup,
                    label = { Text("备份与恢复") },
                    leadingIcon = {
                        Icon(Icons.Filled.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
                AssistChip(
                    onClick = onOpenRecycleBin,
                    label = { Text("回收站") },
                    leadingIcon = {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }
    }

    manualUpdate?.let { info ->
        UpdateDialog(info = info, onDismiss = { if (!info.force) manualUpdate = null })
    }
}

/** 信任徽章：描边小胶囊（零联网 / 零上传） */
@Composable
private fun TrustBadge(text: String) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        color = androidx.compose.ui.graphics.Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun AboutLine(title: String, detail: String) {
    Row(Modifier.padding(vertical = 5.dp)) {
        Spacer(Modifier.size(2.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
