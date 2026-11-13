package com.yuedong.fitness.domain.extension

import com.yuedong.fitness.data.db.entity.UserProfileEntity
import com.yuedong.fitness.data.db.entity.WorkoutPlanEntity

/**
 * AI 健身教练服务（V1 未实现，仅声明扩展点）。
 *
 * 接入要求（不可妥协）：
 * 1. 必须配置真实 LLM 服务的 API Key（推荐智谱 GLM，注册即有免费额度）
 * 2. 必须调用真实服务并解析真实响应
 * 3. 严禁任何 mock / 假数据 / 示例回复充当结果
 *
 * 预留原因：用户确认 V1 不做 AI 功能；接口先行锁定未来替换成本，
 * 实现类加入后 UI 与 ViewModel 的改动可控制在新增一个页面。
 */
interface AiCoachService {

    /** 与 AI 教练对话，返回教练回复文本。 */
    suspend fun chat(userMessage: String, history: List<Pair<String, String>>): String

    /** 根据用户资料生成个性化训练计划。 */
    suspend fun generatePlan(profile: UserProfileEntity): WorkoutPlanEntity
}
