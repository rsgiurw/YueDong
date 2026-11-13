package com.yuedong.fitness.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/**
 * 可注入的应用时钟（可测试性设计）。
 * 正常情况下 today() = 系统今天；调试模式下可通过 advanceDay() 平移"今天"，
 * 所有业务（打卡日期、日历、连击、周统计、今日饮食）统一从这里取日期，
 * 使跨天逻辑无需真实等待即可测试。
 */
object AppClock {

    private val _offsetDays = MutableStateFlow(0L)
    val offsetDays: StateFlow<Long> = _offsetDays.asStateFlow()

    /** 业务"今天"：系统日期 + 调试偏移。 */
    fun today(): LocalDate = LocalDate.now().plusDays(_offsetDays.value)

    fun todayString(): String = today().toString()

    /** 快进一天（调试）。 */
    fun advanceDay() {
        _offsetDays.value += 1
    }

    /** 重置为真实今天（调试）。 */
    fun reset() {
        _offsetDays.value = 0
    }
}
