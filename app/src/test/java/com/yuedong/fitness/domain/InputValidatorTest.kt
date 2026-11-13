package com.yuedong.fitness.domain

import com.yuedong.fitness.domain.validation.InputValidator
import com.yuedong.fitness.domain.validation.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 输入校验器单元测试：覆盖每条规则的合法值、边界值与非法值。
 */
class InputValidatorTest {

    // ---- 年龄 10~80 ----

    @Test
    fun age_valid() {
        assertEquals(25, (InputValidator.validateAge("25") as ValidationResult.Valid).value)
        assertEquals(10, (InputValidator.validateAge("10") as ValidationResult.Valid).value)
        assertEquals(80, (InputValidator.validateAge("80") as ValidationResult.Valid).value)
    }

    @Test
    fun age_invalid() {
        assertTrue(InputValidator.validateAge("9") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateAge("81") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateAge("abc") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateAge("") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateAge("  ") is ValidationResult.Invalid)
    }

    @Test
    fun age_message_mentionsRange() {
        val result = InputValidator.validateAge("9") as ValidationResult.Invalid
        assertTrue(result.message.contains("10~80"))
    }

    // ---- 身高 100~250 ----

    @Test
    fun height_valid() {
        assertEquals(175, (InputValidator.validateHeight("175") as ValidationResult.Valid).value)
        assertEquals(100, (InputValidator.validateHeight("100") as ValidationResult.Valid).value)
        assertEquals(250, (InputValidator.validateHeight("250") as ValidationResult.Valid).value)
    }

    @Test
    fun height_invalid() {
        assertTrue(InputValidator.validateHeight("99") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateHeight("251") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateHeight("1.75") is ValidationResult.Invalid)
    }

    // ---- 体重 30~300 ----

    @Test
    fun weight_valid() {
        assertEquals(65.5, (InputValidator.validateWeight("65.5") as ValidationResult.Valid).value, 0.001)
        assertEquals(30.0, (InputValidator.validateWeight("30") as ValidationResult.Valid).value, 0.001)
        assertEquals(300.0, (InputValidator.validateWeight("300") as ValidationResult.Valid).value, 0.001)
    }

    @Test
    fun weight_invalid() {
        assertTrue(InputValidator.validateWeight("29.9") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateWeight("300.1") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateWeight("七十") is ValidationResult.Invalid)
    }

    // ---- 目标体重：范围 + 与当前差 ≤50 ----

    @Test
    fun targetWeight_valid_atBoundary() {
        val result = InputValidator.validateTargetWeight(70.0, "120")
        assertEquals(120.0, (result as ValidationResult.Valid).value, 0.001)
    }

    @Test
    fun targetWeight_invalid_overDiff() {
        assertTrue(InputValidator.validateTargetWeight(70.0, "120.5") is ValidationResult.Invalid)
    }

    @Test
    fun targetWeight_invalid_outOfRange() {
        assertTrue(InputValidator.validateTargetWeight(70.0, "15") is ValidationResult.Invalid)
    }

    @Test
    fun targetWeight_message_mentionsDiff() {
        val result = InputValidator.validateTargetWeight(70.0, "121") as ValidationResult.Invalid
        assertTrue(result.message.contains("50"))
    }

    // ---- 每周运动天数 0~7 ----

    @Test
    fun weeklyDays_valid() {
        assertEquals(0, (InputValidator.validateWeeklyDays(0) as ValidationResult.Valid).value)
        assertEquals(7, (InputValidator.validateWeeklyDays(7) as ValidationResult.Valid).value)
    }

    @Test
    fun weeklyDays_invalid() {
        assertTrue(InputValidator.validateWeeklyDays(null) is ValidationResult.Invalid)
        assertTrue(InputValidator.validateWeeklyDays(8) is ValidationResult.Invalid)
        assertTrue(InputValidator.validateWeeklyDays(-1) is ValidationResult.Invalid)
    }

    // ---- 计划名称 1~20 字 ----

    @Test
    fun planName_valid() {
        assertEquals(
            "徒手力量",
            (InputValidator.validatePlanName("  徒手力量  ") as ValidationResult.Valid).value
        )
        val maxName = "一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾" // 恰好 20 字
        assertEquals(maxName, (InputValidator.validatePlanName(maxName) as ValidationResult.Valid).value)
    }

    @Test
    fun planName_invalid() {
        assertTrue(InputValidator.validatePlanName("   ") is ValidationResult.Invalid)
        assertTrue(InputValidator.validatePlanName("") is ValidationResult.Invalid)
        assertTrue(InputValidator.validatePlanName("一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾再") is ValidationResult.Invalid) // 21 字
    }

    // ---- 动作组数 1~10 ----

    @Test
    fun sets_valid() {
        assertEquals(1, (InputValidator.validateSets("1") as ValidationResult.Valid).value)
        assertEquals(10, (InputValidator.validateSets("10") as ValidationResult.Valid).value)
        assertEquals(3, (InputValidator.validateSets("3组") as ValidationResult.Valid).value)
        assertEquals(1, (InputValidator.validateSets("1组") as ValidationResult.Valid).value)
    }

    @Test
    fun sets_invalid() {
        assertTrue(InputValidator.validateSets("0") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateSets("11") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateSets("11组") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateSets("组") is ValidationResult.Invalid)
        assertTrue(InputValidator.validateSets("") is ValidationResult.Invalid)
    }
}
