package com.yuedong.fitness.ui.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yuedong.fitness.BuildConfig
import com.yuedong.fitness.domain.update.AppUpdater
import com.yuedong.fitness.domain.update.UpdateInfo
import com.yuedong.fitness.ui.components.YueDialog
import kotlinx.coroutines.launch
import java.io.File

/** 更新流程内部状态：提示 → 下载 → （如需）授权 → 已拉起安装 / 失败 */
private sealed interface Phase {
    data object Prompt : Phase
    data object Downloading : Phase
    data class NeedPermission(val apk: File) : Phase
    data object Installing : Phase
    data object Failed : Phase
}

private fun mb(bytes: Long): String = "%.1fMB".format(bytes / 1048576.0)

/**
 * 更新底部弹层：展示新版本与更新日志，一键下载（带进度）并拉起系统安装器。
 * info.force = true 时为“强制更新”，不可跳过（用于老版本出现严重问题时兜底）。
 */
@Composable
fun UpdateDialog(info: UpdateInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var phase by remember { mutableStateOf<Phase>(Phase.Prompt) }
    var done by remember { mutableStateOf(0L) }
    var total by remember { mutableStateOf<Long?>(null) }

    fun startInstall(apk: File) {
        if (AppUpdater.canInstallApk(context)) {
            AppUpdater.launchInstaller(context, apk)
            phase = Phase.Installing
        } else {
            phase = Phase.NeedPermission(apk)
        }
    }

    fun startDownload() {
        phase = Phase.Downloading
        done = 0L
        total = null
        scope.launch {
            val apk = AppUpdater.findDownloadedApk(context, info)
                ?: AppUpdater.downloadApk(context, info) { d, t -> done = d; total = t }
            if (apk == null) phase = Phase.Failed else startInstall(apk)
        }
    }

    YueDialog(
        onDismissRequest = { if (!info.force && phase != Phase.Downloading) onDismiss() },
        title = { Text("发现新版本 v${info.versionName}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "当前 v${BuildConfig.VERSION_NAME} → 新版 v${info.versionName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (info.changelog.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        info.changelog,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                when (val p = phase) {
                    is Phase.Prompt -> Text(
                        "安装包含全部动作视频（约 130MB），建议在 Wi-Fi 下更新；覆盖安装，全部数据保留",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    is Phase.Downloading -> {
                        val pct = total?.takeIf { it > 0 }?.let { (done * 100 / it).toInt() }
                        if (pct != null) LinearProgressIndicator(
                            progress = { pct / 100f },
                            modifier = Modifier.fillMaxWidth()
                        ) else LinearProgressIndicator(Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (total != null) "正在下载 ${mb(done)} / ${mb(total!!)} · 请勿退出应用"
                            else "正在下载 ${mb(done)} · 请勿退出应用",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    is Phase.NeedPermission -> Text(
                        "系统要求先授予「安装未知应用」权限：点下方按钮跳转设置，打开开关后返回，再点一次按钮即可安装",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    is Phase.Installing -> Text(
                        "已打开系统安装界面，请在那里点「继续安装」完成升级；数据与记录全部保留",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    is Phase.Failed -> Text(
                        "下载失败，请检查网络后重试",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Row {
                when (val p = phase) {
                    is Phase.Prompt -> TextButton(onClick = { startDownload() }) {
                        Text("立即更新", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    is Phase.Downloading -> Text(
                        "下载中…",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                    is Phase.NeedPermission -> TextButton(onClick = {
                        if (AppUpdater.canInstallApk(context)) {
                            AppUpdater.launchInstaller(context, p.apk)
                            phase = Phase.Installing
                        } else {
                            AppUpdater.openInstallPermissionSetting(context)
                        }
                    }) {
                        Text(
                            if (AppUpdater.canInstallApk(context)) "立即安装" else "去允许安装",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    is Phase.Installing -> TextButton(onClick = onDismiss) {
                        Text("完成", fontWeight = FontWeight.Bold)
                    }
                    is Phase.Failed -> TextButton(onClick = { startDownload() }) {
                        Text("重试", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        dismissButton = {
            if (!info.force && (phase == Phase.Prompt || phase == Phase.Failed)) {
                TextButton(onClick = onDismiss) { Text("稍后再说") }
            }
        }
    )
}
