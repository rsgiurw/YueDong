package com.yuedong.fitness.domain.logic

/**
 * 动作教学数据·哑铃训练（v2.6 新增分类）。
 * 覆盖 CommonExercises「哑铃训练」分类：一对哑铃即可完成的全身训练动作。
 */
internal val extraDumbbell: List<ExerciseInfo> = listOf(
    ExerciseInfo(
        name = "哑铃卧推",
        illustration = "exercise",
        muscleGroups = listOf("胸大肌", "三角肌前束", "肱三头肌"),
        difficulty = "初级",
        benefits = listOf(
            "比杠铃活动轨迹更自由，对肩关节更友好",
            "左右独立发力，改善两侧胸肌不平衡",
            "家庭健身练胸的核心动作"
        ),
        steps = listOf(
            "仰卧于平凳，双脚踩实，双手各持哑铃举至胸口上方",
            "肩胛后缩下沉，小臂垂直地面",
            "控制哑铃向两侧下落，至大臂略低于躯干平面",
            "胸部发力沿弧线推起，顶端不碰撞哑铃"
        ),
        mistakes = listOf(
            "哑铃下落过深拉扯肩部——大臂略低于躯干即可",
            "手腕后折——握哑铃让杠铃杆压在掌根",
            "推起时哑铃相撞——保持张力，轻靠即止"
        ),
        breathing = "下落时吸气，推起时呼气"
    ),
    ExerciseInfo(
        name = "哑铃划船",
        illustration = "exercise",
        muscleGroups = listOf("背阔肌", "斜方肌中部", "肱二头肌"),
        difficulty = "初级",
        benefits = listOf(
            "单臂支撑姿态稳定，发力感最容易找到",
            "强化背阔肌厚度，改善高低肩与圆肩",
            "一只哑铃就能练，居家健身首选背部动作"
        ),
        steps = listOf(
            "俯身单手撑凳，另一手持哑铃手臂自然下垂",
            "背部平直，肩胛先向脊柱方向收紧",
            "肘部贴身向后拉起哑铃至腰侧",
            "缓慢下放，充分拉伸背部再拉起"
        ),
        mistakes = listOf(
            "身体旋转借力——躯干稳定，只动手臂",
            "拉向胸口——目标腰侧，角度不对背发力变少",
            "耸肩——先沉肩胛再屈肘"
        ),
        breathing = "拉起时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "哑铃硬拉",
        illustration = "exercise",
        muscleGroups = listOf("腘绳肌", "臀大肌", "下背竖脊肌"),
        difficulty = "初级",
        benefits = listOf(
            "强化大腿后侧链条，久坐族的必备动作",
            "直腿版本对腘绳肌拉伸刺激极强",
            "学习正确的髋屈伸模式，保护腰椎"
        ),
        steps = listOf(
            "双脚与髋同宽，双手持哑铃垂于体前",
            "膝盖微屈锁定，以髋为轴上身前倾",
            "哑铃沿大腿前侧下放，至腘绳肌有强烈拉伸感",
            "臀部发力顶髋站直，顶端挤压臀部"
        ),
        mistakes = listOf(
            "弓背——全程挺胸，背部平直如桌板",
            "用下背发力拉起——力量来源是臀部顶髋",
            "膝盖过度弯曲变成深蹲——微屈锁定角度"
        ),
        breathing = "下放时吸气，站直时呼气"
    ),
    ExerciseInfo(
        name = "哑铃肩上推举",
        illustration = "exercise",
        muscleGroups = listOf("三角肌", "肱三头肌", "核心"),
        difficulty = "初级",
        benefits = listOf(
            "哑铃版推肩，轨迹自然对肩关节更友好",
            "站姿完成顺带强化核心抗侧屈能力",
            "左右独立推举，暴露并改善弱侧"
        ),
        steps = listOf(
            "站距与髋同宽，哑铃举至肩侧，掌心朝前",
            "收腹沉肩，核心绷紧稳定躯干",
            "垂直向上推起，至手臂接近伸直",
            "缓慢下放回肩侧，全程不耸腰"
        ),
        mistakes = listOf(
            "腰部反弓借力——收腹夹臀，肋骨下沉",
            "推偏向前——哑铃应推至头顶正上方",
            "下放过低——放到耳朵两侧即可"
        ),
        breathing = "推起时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "高脚杯深蹲",
        illustration = "squat",
        muscleGroups = listOf("股四头肌", "臀大肌", "核心"),
        difficulty = "入门",
        benefits = listOf(
            "负重置于胸前，天然强制背部挺直",
            "学习深蹲模式最友好的入门动作",
            "对膝盖压力小，新手和大体重人群都适合"
        ),
        steps = listOf(
            "双手捧哑铃于胸前，肘部朝下贴近身体",
            "双脚与肩同宽，屈髋屈膝下蹲",
            "蹲至大腿约与地面平行，肘部落在膝盖内侧之间",
            "脚跟发力站起，全程挺胸哑铃贴身"
        ),
        mistakes = listOf(
            "上身过度前倾——哑铃贴紧胸口，挺胸下蹲",
            "膝盖内扣——对准脚尖方向",
            "脚跟离地——重心放在全脚掌"
        ),
        breathing = "下蹲时吸气，站起时呼气"
    ),
    ExerciseInfo(
        name = "哑铃箭步蹲",
        illustration = "thigh",
        muscleGroups = listOf("臀大肌", "股四头肌", "腘绳肌"),
        difficulty = "初级",
        benefits = listOf(
            "负重版弓步蹲，下肢刺激更强",
            "行进间完成提升平衡与协调能力",
            "单侧发力改善左右腿力量差异"
        ),
        steps = listOf(
            "双手持哑铃垂于体侧，站直挺胸",
            "一脚向前迈出，重心垂直下降成弓步",
            "前膝约 90 度，后膝接近地面",
            "前腿蹬地站起，换腿前行或原地交替"
        ),
        mistakes = listOf(
            "上身摇晃——哑铃重量适中，核心收紧",
            "步幅过小——前膝会超过脚尖，加大步幅",
            "后膝砸地——缓慢下降有控制"
        ),
        breathing = "下蹲时吸气，站起时呼气"
    ),
    ExerciseInfo(
        name = "俯身哑铃飞鸟",
        illustration = "exercise",
        muscleGroups = listOf("三角肌后束", "斜方肌中部", "背阔肌上部"),
        difficulty = "初级",
        benefits = listOf(
            "三角肌后束几乎唯一的高效孤立动作",
            "改善圆肩驼背，平衡胸肩肌力",
            "小重量高次数即可看到肩后侧线条变化"
        ),
        steps = listOf(
            "双脚与髋同宽，屈髋俯身至上身接近与地面平行",
            "双手持哑铃垂下，掌心相对，肘部微屈",
            "以肩带发力向两侧抬起哑铃至与肩同高",
            "顶端停顿一秒，缓慢下放"
        ),
        mistakes = listOf(
            "用重量甩起——减轻重量，用肩后束发力",
            "屈臂角度变化——肘部锁定微屈角度",
            "上身抬起借力——俯身角度保持不变"
        ),
        breathing = "抬起时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "哑铃前平举",
        illustration = "exercise",
        muscleGroups = listOf("三角肌前束"),
        difficulty = "入门",
        benefits = listOf(
            "孤立刺激三角肌前束，肩部正面饱满度提升",
            "与侧平举搭配，肩部三束全覆盖",
            "动作轨迹简单，新手易上手"
        ),
        steps = listOf(
            "站立，哑铃垂于大腿前侧，掌心朝后",
            "手臂伸直向前抬起至与肩同高",
            "顶端停顿，感受肩前束收缩",
            "缓慢下放还原，左右交替或同步"
        ),
        mistakes = listOf(
            "耸肩借力——斜方肌放松，肩下沉",
            "甩动惯性——慢起慢落",
            "抬过头——与肩同高即可"
        ),
        breathing = "抬起时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "哑铃耸肩",
        illustration = "exercise",
        muscleGroups = listOf("斜方肌上部", "肩胛提肌"),
        difficulty = "入门",
        benefits = listOf(
            "强化上斜方肌，改善溜肩与颈肩线条",
            "动作幅度小重量大，安全高效",
            "长期伏案者练它能缓解肩颈无力酸痛"
        ),
        steps = listOf(
            "双手持哑铃垂于体侧，身体站直",
            "肩膀垂直向上耸起，向耳朵方向靠拢",
            "顶端停顿一秒，感受斜方肌收缩",
            "缓慢下放至最低点，充分拉伸"
        ),
        mistakes = listOf(
            "转肩画圈——垂直上下运动即可",
            "屈肘借力——手臂始终伸直",
            "幅度太小——耸到位、放到底"
        ),
        breathing = "耸肩时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "哑铃仰卧臂屈伸",
        illustration = "arm",
        muscleGroups = listOf("肱三头肌"),
        difficulty = "初级",
        benefits = listOf(
            "仰卧轨迹下三头肌受力均匀充分",
            "比杠铃版本活动轨迹更自由，肘部压力小",
            "雕刻大臂后侧的基础动作"
        ),
        steps = listOf(
            "仰卧于平凳，双手各持哑铃举至胸口上方",
            "大臂保持垂直固定，肘部指向天花板",
            "屈肘让哑铃向头部两侧下落至大臂略低于水平",
            "肱三头发力举回顶端"
        ),
        mistakes = listOf(
            "大臂前后摆动——大臂锁死只动小臂",
            "下放过快——全程控制速度",
            "腰部离凳——双腿屈膝踩实稳定躯干"
        ),
        breathing = "下落时吸气，上举时呼气"
    ),
    ExerciseInfo(
        name = "农夫行走",
        illustration = "exercise",
        muscleGroups = listOf("握力", "斜方肌", "核心"),
        difficulty = "入门",
        benefits = listOf(
            "最实用的功能性训练，提重物的日常模式",
            "同步强化握力、斜方肌与核心稳定",
            "简单安全，大重量也不易受伤"
        ),
        steps = listOf(
            "双手各持一个哑铃垂于体侧",
            "挺胸收腹，肩胛微微收紧",
            "小步稳定向前行走，哑铃不晃动",
            "走够规定距离或时间后轻放哑铃"
        ),
        mistakes = listOf(
            "身体左右摇摆——核心收紧，步幅放小",
            "耸肩缩脖——肩膀下沉，用握力挂住重量",
            "低头看地——目视前方"
        ),
        breathing = "保持自然节奏呼吸，不要憋气"
    )
)
