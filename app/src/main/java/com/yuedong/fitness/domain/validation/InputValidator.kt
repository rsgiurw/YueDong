package com.yuedong.fitness.domain.validation

/**
 * 校验结果：Valid 携带解析后的值，Invalid 携带中文错误文案。
 */
sealed interface ValidationResult<out T> {
    data class Valid<T>(val value: T) : ValidationResult<T>
    data class Invalid(val message: String) : ValidationResult<Nothing>
}

/**
 * 输入校验器（模块契约见设计包 §8 ValidationService）。
 * 职责：全部表单校验规则与中文文案；非职责：不弹窗、不持有状态（纯函数）。
 */
object InputValidator {

    private const val MSG_NUMBER = "请输入数字"
    const val AGE_RANGE = "年龄需在 10~80 之间"
    const val HEIGHT_RANGE = "身高需在 100~250 厘米之间"
    const val WEIGHT_RANGE = "体重需在 30~300 公斤之间"
    const val TARGET_DIFF = "目标体重与当前体重差距不能超过 50 公斤"

    fun validateAge(raw: String): ValidationResult<Int> {
        val number = parseDouble(raw) ?: return ValidationResult.Invalid(MSG_NUMBER)
        if (number < 10 || number > 80) return ValidationResult.Invalid(AGE_RANGE)
        return ValidationResult.Valid(number.toInt())
    }

    fun validateHeight(raw: String): ValidationResult<Int> {
        val number = parseDouble(raw) ?: return ValidationResult.Invalid(MSG_NUMBER)
        if (number < 100 || number > 250) return ValidationResult.Invalid(HEIGHT_RANGE)
        return ValidationResult.Valid(number.toInt())
    }

    fun validateWeight(raw: String): ValidationResult<Double> {
        val number = parseDouble(raw) ?: return ValidationResult.Invalid(MSG_NUMBER)
        if (number < 30 || number > 300) return ValidationResult.Invalid(WEIGHT_RANGE)
        return ValidationResult.Valid(number)
    }

    /** 目标体重：先满足体重范围，再要求与当前体重差距 ≤ 50 公斤。 */
    fun validateTargetWeight(currentWeightKg: Double, raw: String): ValidationResult<Double> {
        val number = parseDouble(raw) ?: return ValidationResult.Invalid(MSG_NUMBER)
        if (number < 30 || number > 300) return ValidationResult.Invalid(WEIGHT_RANGE)
        if (kotlin.math.abs(number - currentWeightKg) > 50) {
            return ValidationResult.Invalid(TARGET_DIFF)
        }
        return ValidationResult.Valid(number)
    }

    /** 每周运动天数（0~7）；界面用滑杆选择，此校验同时供未来表单复用。 */
    fun validateWeeklyDays(days: Int?): ValidationResult<Int> {
        if (days == null || days < 0 || days > 7) {
            return ValidationResult.Invalid("每周运动天数需在 0~7 天")
        }
        return ValidationResult.Valid(days)
    }

    const val PLAN_NAME_RULE = "计划名称需为 1~20 个字"

    /** 自定义计划名称：去首尾空白后需为 1~20 个字。 */
    fun validatePlanName(raw: String): ValidationResult<String> {
        val name = raw.trim()
        if (name.isEmpty() || name.length > 20) return ValidationResult.Invalid(PLAN_NAME_RULE)
        return ValidationResult.Valid(name)
    }

    const val SETS_RANGE = "组数需在 1~10 之间"

    /** 动作组数：1~10 组；允许带"组"字（如"3组"），自动提取数字。 */
    fun validateSets(raw: String): ValidationResult<Int> {
        val number = Regex("\\d+").find(raw.trim())?.value?.toDoubleOrNull()
            ?: return ValidationResult.Invalid(SETS_RANGE)
        if (number < 1 || number > 10) return ValidationResult.Invalid(SETS_RANGE)
        return ValidationResult.Valid(number.toInt())
    }

    private fun parseDouble(raw: String): Double? =
        raw.trim().toDoubleOrNull()
}
