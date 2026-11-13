package com.yuedong.fitness.domain.logic

/**
 * 动作教学库（v2.4 新模块；v2.5 扩展为覆盖 CommonExercises 全部 38 个动作的完整教学库）。
 * 插图采用纯代码绘制的火柴人姿势动画（见 ui/components/StickFigure.kt），
 * 不依赖任何外部图片素材，零版权风险、零体积、离线可用。
 * 数据按分类拆分在 ExerciseDataExtra1/2/3 中，拆分方式与食品库 FoodCatalog 一致。
 */
data class ExerciseInfo(
    val name: String,
    /** 姿势动画的配对标识（对应 ExercisePoses 中的关键帧）。 */
    val illustration: String,
    val muscleGroups: List<String>,
    val difficulty: String,
    val benefits: List<String>,
    val steps: List<String>,
    val mistakes: List<String>,
    val breathing: String
)

object ExerciseLibrary {

    /** 基础收录的 4 个动作（教学深度的原始范例）。 */
    private val builtin = listOf(squat(), pushup(), kneelingPushup(), crunch())

    /**
     * 全部动作教学 = 基础收录 + 分类扩充，覆盖 CommonExercises 全部 101 个动作。
     * v2.6 新增 自重进阶/哑铃训练/杠铃力量/臀部专项 四个分类。
     */
    val all: List<ExerciseInfo> = builtin + extraChestBack + extraLegsCore + extraCardioStretch +
        extraAdvancedBodyweight + extraDumbbell + extraBarbell + extraGlutes + extraMixed + extraWarmup

    /** 别名表：简称/俗称 → 标准动作名（仅显式映射，避免"跪姿俯卧撑"误匹配"俯卧撑"）。 */
    private val aliases = mapOf(
        "深蹲" to "徒手深蹲",
        "拉伸放松" to "全身拉伸"
    )

    /** 精确匹配（含别名），不做子串模糊匹配。 */
    fun find(name: String): ExerciseInfo? {
        val trimmed = name.trim()
        val query = aliases[trimmed] ?: trimmed
        return all.find { it.name == query }
    }

    private fun squat() = ExerciseInfo(
        name = "徒手深蹲",
        illustration = "squat",
        muscleGroups = listOf("股四头肌", "臀大肌", "核心"),
        difficulty = "入门",
        benefits = listOf(
            "强化大腿和臀部肌群，塑造下肢线条",
            "提升核心稳定性，日常蹲起更有力",
            "大肌群参与度高，燃脂效率出色"
        ),
        steps = listOf(
            "双脚与肩同宽站立，脚尖微微向外打开",
            "臀部向后向下坐，像要坐到身后的椅子上",
            "下蹲至大腿约与地面平行，膝盖对准脚尖方向",
            "脚跟发力站起回到站立，全程背部挺直"
        ),
        mistakes = listOf(
            "膝盖内扣——膝盖应始终对准脚尖方向",
            "弓背或躯干过度前倾——挺胸收腹，重心落在全脚掌",
            "脚跟离地——重心跑到脚尖会增大膝盖压力"
        ),
        breathing = "下蹲时吸气，站起发力时呼气"
    )

    private fun pushup() = ExerciseInfo(
        name = "俯卧撑",
        illustration = "pushup",
        muscleGroups = listOf("胸大肌", "肱三头肌", "三角肌前束"),
        difficulty = "入门",
        benefits = listOf(
            "增强胸部和手臂的推撑力量",
            "全程收紧核心，顺带锻炼腹部与肩部稳定",
            "无需器械，随时随地都能练"
        ),
        steps = listOf(
            "双手略宽于肩撑地，手指朝前",
            "身体从头到脚呈一条直线，核心收紧",
            "屈肘下降，至胸部接近地面",
            "胸部发力推起，直到手臂自然伸直"
        ),
        mistakes = listOf(
            "塌腰或撅臀——核心没收紧，腰部在代偿",
            "手肘过度外展成 T 形——大臂与躯干约呈 45° 更护肩",
            "幅度不足——没降到胸部接近地面，训练效果打折"
        ),
        breathing = "下降时吸气，推起发力时呼气"
    )

    private fun kneelingPushup() = ExerciseInfo(
        name = "跪姿俯卧撑",
        illustration = "kneeling_pushup",
        muscleGroups = listOf("胸大肌", "肱三头肌"),
        difficulty = "入门",
        benefits = listOf(
            "膝盖支撑减轻负重，对肩膀压力更小",
            "同样锻炼胸部推撑力量，是标准俯卧撑的入门台阶",
            "更容易做标准动作，适合循序渐进"
        ),
        steps = listOf(
            "双手撑地略宽于肩，膝盖着地支撑",
            "小腿抬起交叉，从头部到膝盖呈一条直线",
            "屈肘下降，至胸部接近垫面",
            "胸部发力推起，直到手臂自然伸直"
        ),
        mistakes = listOf(
            "塌腰或撅臀——核心没收紧",
            "手肘过度外展——大臂与躯干约呈 45° 更护肩",
            "臀部先起——推起时整体同步发力"
        ),
        breathing = "下降时吸气，推起发力时呼气"
    )

    private fun crunch() = ExerciseInfo(
        name = "卷腹",
        illustration = "situp",
        muscleGroups = listOf("腹直肌", "核心"),
        difficulty = "入门",
        benefits = listOf(
            "直接锻炼腹直肌，强化腹部线条",
            "腰椎压力小于仰卧起坐，更安全",
            "提升核心力量，让其他训练更稳定"
        ),
        steps = listOf(
            "仰卧于垫上，双腿屈膝约 90 度，双手轻扶耳侧",
            "腹部发力，将上背部抬离垫面约 30 度",
            "顶端稍作停顿，感受腹部收缩",
            "缓慢落下还原，腰部始终贴地"
        ),
        mistakes = listOf(
            "双手抱头用力拉扯颈部——手只轻扶，发力靠腹部",
            "用惯性甩起甩落——全程慢速控制",
            "腰部离地——卷腹只抬上背部，不是坐起"
        ),
        breathing = "起身时呼气，下落时吸气"
    )
}
