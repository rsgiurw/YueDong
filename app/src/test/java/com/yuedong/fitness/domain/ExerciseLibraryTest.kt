package com.yuedong.fitness.domain

import com.yuedong.fitness.domain.logic.CommonExercises
import com.yuedong.fitness.domain.logic.ExerciseLibrary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 动作教学库覆盖度测试：CommonExercises 里的每个动作都必须有完整图文教学，
 * 防止后续往 CommonExercises 加动作时漏写教学详情。
 */
class ExerciseLibraryTest {

    @Test
    fun `常见动作全部有图文教学`() {
        val missing = CommonExercises.grouped
            .flatMap { it.second }
            .map { it.name }
            .filter { ExerciseLibrary.find(it) == null }
        assertTrue("缺少图文教学的动作: $missing", missing.isEmpty())
    }

    @Test
    fun `教学条目内容完整`() {
        ExerciseLibrary.all.forEach { info ->
            assertTrue("${info.name} 缺少好处说明", info.benefits.isNotEmpty())
            assertTrue("${info.name} 步骤过少", info.steps.size >= 3)
            assertTrue("${info.name} 缺少常见错误", info.mistakes.isNotEmpty())
            assertTrue("${info.name} 缺少呼吸说明", info.breathing.isNotBlank())
            assertTrue("${info.name} 缺少目标肌群", info.muscleGroups.isNotEmpty())
        }
    }

    @Test
    fun `动作名称不重复`() {
        val names = ExerciseLibrary.all.map { it.name }
        assertEquals("存在重名动作: ${names.groupBy { it }.filterValues { it.size > 1 }.keys}",
            names.size, names.toSet().size)
    }

    @Test
    fun `别名可以命中教学`() {
        assertNotNull(ExerciseLibrary.find("深蹲"))
        assertNotNull(ExerciseLibrary.find("拉伸放松"))
    }
}
