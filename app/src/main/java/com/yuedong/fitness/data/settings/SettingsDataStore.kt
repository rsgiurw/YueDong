package com.yuedong.fitness.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.yuedong.fitness.util.AppClock

/** DataStore 实例：进程级单例（由属性委托保证）。 */
private val Context.yueDongDataStore by preferencesDataStore(name = "yuedong_settings")

/**
 * 偏好设置存储（模块契约见设计包 §8 DataLayer）。
 * 职责：读写两类轻量配置——是否完成首次问卷、当前激活计划 id。
 * 非职责：不存业务数据（业务数据一律走 Room）。
 */
class SettingsDataStore(private val context: Context) {

    private val onboardingKey = booleanPreferencesKey("onboarding_completed")
    private val activePlanKey = longPreferencesKey("active_plan_id")
    private val activePlanStartedKey = longPreferencesKey("active_plan_started_at")
    private val sampleGeneratedKey = booleanPreferencesKey("sample_generated")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val coachShownKey = booleanPreferencesKey("coach_guide_shown")

    fun observeOnboardingCompleted(): Flow<Boolean> =
        context.yueDongDataStore.data.map { it[onboardingKey] ?: false }

    suspend fun setOnboardingCompleted(value: Boolean) {
        context.yueDongDataStore.edit { it[onboardingKey] = value }
    }

    /** 新手遮罩引导是否已展示（仅展示一次） */
    fun observeCoachShown(): Flow<Boolean> =
        context.yueDongDataStore.data.map { it[coachShownKey] ?: false }

    suspend fun setCoachShown(value: Boolean) {
        context.yueDongDataStore.edit { it[coachShownKey] = value }
    }

    /** 激活计划 id；未设置或非法值(<=0)一律视为未设置。 */
    fun observeActivePlanId(): Flow<Long?> =
        context.yueDongDataStore.data.map { it[activePlanKey]?.takeIf { id -> id > 0 } }

    suspend fun setActivePlanId(value: Long?) {
        context.yueDongDataStore.edit { prefs ->
            if (value == null) prefs.remove(activePlanKey) else prefs[activePlanKey] = value
        }
    }

    /** 当前计划的激活日期（毫秒时间戳）：用于推算"今天该练第几天"。 */
    fun observeActivePlanStartedAt(): Flow<Long?> =
        context.yueDongDataStore.data.map { it[activePlanStartedKey]?.takeIf { v -> v > 0 } }

    suspend fun setActivePlanStartedAt(value: Long?) {
        context.yueDongDataStore.edit { prefs ->
            if (value == null) prefs.remove(activePlanStartedKey) else prefs[activePlanStartedKey] = value
        }
    }

    /** 演示数据是否已自动生成过（只生成一次）。 */
    /** 主题模式：system 跟随系统 / light 浅色 / dark 深色 */
    fun observeThemeMode(): Flow<String> =
        context.yueDongDataStore.data.map { it[themeModeKey] ?: "light" }

    suspend fun setThemeMode(value: String) {
        context.yueDongDataStore.edit { it[themeModeKey] = value }
    }

    fun observeSampleGenerated(): Flow<Boolean> =
        context.yueDongDataStore.data.map { it[sampleGeneratedKey] ?: false }

    suspend fun setSampleGenerated(value: Boolean) {
        context.yueDongDataStore.edit { it[sampleGeneratedKey] = value }
    }

    /** 内置计划是否已完成首次播种（只播种一次，之后删除内置计划不会再复活）。 */
    private val builtinSeededKey = booleanPreferencesKey("builtin_seeded")

    fun observeBuiltinSeeded(): Flow<Boolean> =
        context.yueDongDataStore.data.map { it[builtinSeededKey] ?: false }

    suspend fun setBuiltinSeeded(value: Boolean) {
        context.yueDongDataStore.edit { it[builtinSeededKey] = value }
    }

    /** 训练提醒开关（每日 20:00 本地通知，当天已打卡则不打扰）。 */
    private val reminderKey = booleanPreferencesKey("training_reminder_enabled")
    private val reminderHourKey = intPreferencesKey("training_reminder_hour")
    private val reminderMinuteKey = intPreferencesKey("training_reminder_minute")

    fun observeReminderEnabled(): Flow<Boolean> =
        context.yueDongDataStore.data.map { it[reminderKey] ?: false }

    suspend fun setReminderEnabled(value: Boolean) {
        context.yueDongDataStore.edit { it[reminderKey] = value }
    }

    fun observeReminderTime(): Flow<Pair<Int, Int>> =
        context.yueDongDataStore.data.map { (it[reminderHourKey] ?: 20) to (it[reminderMinuteKey] ?: 0) }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.yueDongDataStore.edit {
            it[reminderHourKey] = hour
            it[reminderMinuteKey] = minute
        }
    }

    /** 清空全部偏好（配合"清空数据重新开始"调试功能）。 */
    suspend fun reset() {
        context.yueDongDataStore.edit { it.clear() }
    }

    /** 「第一天」= 首次使用日期：补打卡只允许这一天及以后（之前的日子不存在使用行为）。 */
    private val usageStartKey = stringPreferencesKey("usage_start_date")

    /** 不存在时写入今天；「清空数据重新开始」后重写为清空当天。 */
    suspend fun ensureUsageStartDate() {
        context.yueDongDataStore.edit { prefs ->
            if (prefs[usageStartKey] == null) prefs[usageStartKey] = AppClock.todayString()
        }
    }

    fun observeUsageStartDate(): Flow<String?> =
        context.yueDongDataStore.data.map { it[usageStartKey] }
}
