package com.yuedong.fitness.domain.extension

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.yuedong.fitness.MainActivity
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.util.AppClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * 训练提醒闹钟接收端:到点检查当天是否已打卡,未打卡才发通知;
 * 无论是否提醒,都重排下一天的闹钟(以触发时刻重算,不存在周期累积漂移)。
 */
class TrainingReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(TrainingReminderWorker.KEY_ID, 0L)
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as YueDongApplication
                val reminder = app.container.remindersRepository.byId(id) ?: return@launch
                if (!reminder.enabled) return@launch
                // 先重排下一天,保证提醒链条不断
                TrainingReminderWorker.scheduleOne(context, id, reminder.hour, reminder.minute, true)

                val today = AppClock.todayString()
                val sqlite = app.container.database.openHelper.writableDatabase
                val trained = withContext(Dispatchers.IO) {
                    sqlite.query(
                        "SELECT 1 FROM check_in_record WHERE date = ? LIMIT 1", arrayOf(today)
                    ).use { it.moveToFirst() }
                }
                if (trained) return@launch

                if (Build.VERSION.SDK_INT >= 33 &&
                    app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) return@launch

                val manager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= 26) {
                    manager.createNotificationChannel(
                        NotificationChannel(
                            TrainingReminderWorker.CHANNEL_ID, "训练提醒", NotificationManager.IMPORTANCE_DEFAULT
                        )
                    )
                }
                val contentIntent = PendingIntent.getActivity(
                    app, 0, Intent(app, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val notification = NotificationCompat.Builder(app, TrainingReminderWorker.CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                    .setContentTitle("该训练啦")
                    .setContentText("今天的训练计划还没打卡,花 20 分钟动一动吧")
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .build()
                manager.notify(id.toInt(), notification)
            } finally {
                result.finish()
            }
        }
    }
}

/**
 * 训练提醒调度门面(名字沿用历史,调用点无需改动)。
 * 用 AlarmManager 精确闹钟而非 WorkManager 周期任务:周期任务的首次执行延迟会被
 * 系统调度固定进后续周期(表现为提醒恒慢一分钟),精确闹钟不存在该问题。
 */
object TrainingReminderWorker {

    const val CHANNEL_ID = "training_reminder"
    const val KEY_ID = "reminder_id"

    private fun operation(context: Context, id: Long): PendingIntent = PendingIntent.getBroadcast(
        context, id.toInt(),
        Intent(context, TrainingReminderReceiver::class.java).putExtra(KEY_ID, id),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun nextTrigger(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (next.timeInMillis <= now.timeInMillis) next.add(Calendar.DATE, 1)
        return next.timeInMillis
    }

    /** 单条调度:enabled = 每天 时:分 的精确闹钟;否则取消 */
    fun scheduleOne(context: Context, id: Long, hour: Int, minute: Int, enabled: Boolean) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val operation = operation(context, id)
        if (!enabled) {
            am.cancel(operation)
            return
        }
        val triggerAt = nextTrigger(hour, minute)
        val canExact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        } else {
            // 无精确闹钟权限:1 分钟窗口闹钟兜底,去系统设置授权后自动恢复精确
            am.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, 60_000L, operation)
        }
    }

    fun cancel(context: Context, id: Long) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(operation(context, id))
    }
}
