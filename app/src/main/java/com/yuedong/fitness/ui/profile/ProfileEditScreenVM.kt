package com.yuedong.fitness.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.data.db.entity.BodyMetricEntity
import com.yuedong.fitness.data.db.entity.UserProfileEntity
import com.yuedong.fitness.data.repository.BodyMetricRepository
import com.yuedong.fitness.data.repository.ProfileRepository
import com.yuedong.fitness.domain.model.Gender
import com.yuedong.fitness.domain.model.Purpose
import com.yuedong.fitness.domain.validation.InputValidator
import com.yuedong.fitness.domain.validation.ValidationResult
import com.yuedong.fitness.util.AppClock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 修改资料 ViewModel:资料流 + 分字段即时保存(每行确认即写库)。 */
class ProfileEditViewModel(
    private val profileRepository: ProfileRepository,
    private val bodyMetricRepository: BodyMetricRepository
) : ViewModel() {

    val profile: StateFlow<UserProfileEntity?> = profileRepository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** 覆盖式保存单个字段：无档案（渐进式引导，未走过问卷）时先创建默认档案再套用修改 */
    private fun persist(transform: (UserProfileEntity) -> UserProfileEntity) {
        viewModelScope.launch {
            val current = profileRepository.observeProfile().first()
                ?: UserProfileEntity(
                    gender = "MALE",
                    age = 25,
                    heightCm = 170,
                    currentWeightKg = 65.0,
                    targetWeightKg = 65.0,
                    weeklyExerciseDays = 3,
                    purpose = "HEALTH",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            profileRepository.save(transform(current))
        }
    }

    /** 昵称 */
    fun applyNickname(value: String) = persist {
        it.copy(nickname = value.trim().ifBlank { "悦动用户" })
    }

    /** 性别 */
    fun applyGender(gender: Gender) = persist { it.copy(gender = gender.name) }

    /** 健身目的 */
    fun applyPurpose(purpose: Purpose) = persist { it.copy(purpose = purpose.name) }

    /** 每周运动天数 */
    fun applyWeeklyDays(days: Int) = persist { it.copy(weeklyExerciseDays = days) }

    /** 头像路径(调用方负责旧文件清理) */
    fun applyAvatar(path: String?) = persist { it.copy(avatarPath = path) }

    /** 年龄:校验非法返回错误文案,合法写库返回 null */
    fun applyAge(text: String): String? =
        when (val r = InputValidator.validateAge(text)) {
            is ValidationResult.Invalid -> r.message
            is ValidationResult.Valid -> {
                persist { it.copy(age = r.value) }
                null
            }
        }

    /** 身高:同上 */
    fun applyHeight(text: String): String? =
        when (val r = InputValidator.validateHeight(text)) {
            is ValidationResult.Invalid -> r.message
            is ValidationResult.Valid -> {
                persist { it.copy(heightCm = r.value) }
                null
            }
        }

    /** 体重(当前+目标):写资料并同步今日体重记录(与原逻辑一致) */
    fun applyWeights(currentText: String, targetText: String): String? {
        val current = currentText.trim().toDoubleOrNull()
            ?: return "当前体重需为数值"
        val target = targetText.trim().toDoubleOrNull()
            ?: return "目标体重需为数值"
        if (current < 30 || current > 300) return "当前体重需在 30~300 之间"
        if (target < 30 || target > 300) return "目标体重需在 30~300 之间"

        persist { it.copy(currentWeightKg = current, targetWeightKg = target) }
        viewModelScope.launch {
            bodyMetricRepository.saveCurrent(
                BodyMetricEntity(
                    date = AppClock.todayString(),
                    weightKg = current,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return null
    }

    companion object {
        fun factory(): androidx.lifecycle.ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as YueDongApplication
                ProfileEditViewModel(app.container.profileRepository, app.container.bodyMetricRepository)
            }
        }
    }
}
