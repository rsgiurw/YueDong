package com.yuedong.fitness

import android.os.Bundle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yuedong.fitness.navigation.YueDongApp
import com.yuedong.fitness.ui.theme.YueDongTheme

/**
 * 应用唯一入口 Activity（AppShell 模块）。
 * 职责：装配全局主题与应用骨架；不含任何业务逻辑。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as com.yuedong.fitness.YueDongApplication
        setContent {
            val themeMode by app.container.settings.observeThemeMode()
                .collectAsState(initial = "system")
            YueDongTheme(themeMode = themeMode) {
                YueDongApp()
            }
        }
    }
}
