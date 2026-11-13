package com.yuedong.fitness.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import com.yuedong.fitness.ui.components.YueToaster
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.settings.SettingsDataStore
import com.yuedong.fitness.domain.extension.DataExporter
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import com.yuedong.fitness.ui.components.YueDialog
import com.yuedong.fitness.ui.theme.DiveMenuAccents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast

/** 数据与备份:全部记录与图片导出为 zip,或从备份恢复(覆盖当前数据)。 */
@Composable
fun BackupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as YueDongApplication
    val scope = rememberCoroutineScope()
    var pendingImport by remember { mutableStateOf<android.net.Uri?>(null) }

    val settingsDataStore = app.container.settings
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) scope.launch {
            val n = withContext(Dispatchers.IO) {
                val settings = JSONObject().apply {
                    put("activePlanId", settingsDataStore.observeActivePlanId().first() ?: JSONObject.NULL)
                    put("onboardingCompleted", settingsDataStore.observeOnboardingCompleted().first())
                    put("builtinSeeded", settingsDataStore.observeBuiltinSeeded().first())
                    put("themeMode", settingsDataStore.observeThemeMode().first())
                    put("reminderEnabled", settingsDataStore.observeReminderEnabled().first())
                    val (h, m) = settingsDataStore.observeReminderTime().first()
                    put("reminderHour", h)
                    put("reminderMinute", m)
                }
                DataExporter.exportTo(context.applicationContext, app.container.database, settings, uri)
            }
            com.yuedong.fitness.ui.components.YueToaster.show(
                if (n >= 0) "已导出 $n 条记录" else "导出失败,请重试"
            )
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) pendingImport = uri
    }

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
            Text("数据与备份", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text(
            "数据仅保存在这台手机上 · 定期备份,换机或卸载前务必先导出",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Column(Modifier.padding(20.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { exportLauncher.launch(DataExporter.backupFileName()) }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .background(DiveMenuAccents.blue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Backup,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("导出全部数据", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "训练、打卡、饮食、自定义内容与图片,打包为 zip",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "导出",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        importLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                    }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .background(DiveMenuAccents.green, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Backup,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("从备份恢复", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "导入后覆盖当前全部数据,无法撤销",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "导入",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    pendingImport?.let { uri ->
        YueDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("导入备份?") },
            text = { Text("导入会覆盖当前全部数据(含自定义内容与图片),且无法撤销。确定继续吗?") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val target = uri
                    pendingImport = null
                    scope.launch {
                        val ok = runCatching {
                            val settings = withContext(Dispatchers.IO) {
                                DataExporter.importFrom(
                                    context.applicationContext,
                                    app.container.database,
                                    target
                                )
                            }
                            settingsDataStore.setActivePlanId(
                                if (settings.isNull("activePlanId")) null else settings.getLong("activePlanId")
                            )
                            settingsDataStore.setOnboardingCompleted(settings.optBoolean("onboardingCompleted", true))
                            settingsDataStore.setBuiltinSeeded(settings.optBoolean("builtinSeeded", true))
                            settingsDataStore.setThemeMode(settings.optString("themeMode", "light"))
                            settingsDataStore.setReminderEnabled(settings.optBoolean("reminderEnabled", false))
                            settingsDataStore.setReminderTime(
                                settings.optInt("reminderHour", 20),
                                settings.optInt("reminderMinute", 0)
                            )
                        }.isSuccess
                        com.yuedong.fitness.ui.components.YueToaster.show(
                            if (ok) "导入成功,数据已恢复" else "导入失败,请确认文件是否为本 APP 导出的备份"
                        )
                    }
                }) {
                    Text("导入并覆盖", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { pendingImport = null }) {
                    Text("取消")
                }
            }
        )
    }
}
