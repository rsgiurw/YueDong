package com.yuedong.fitness.domain.logic

/**
 * 动作教学数据·自重进阶（v2.6 新增分类）。
 * 覆盖 CommonExercises「自重进阶」分类：俯卧撑/引体/深蹲的进阶变式与自重高阶动作。
 */
internal val extraAdvancedBodyweight: List<ExerciseInfo> = listOf(
    ExerciseInfo(
        name = "上斜俯卧撑",
        illustration = "exercise",
        muscleGroups = listOf("胸大肌上束", "肱三头肌", "核心"),
        difficulty = "入门",
        benefits = listOf(
            "手撑高处减轻负重，是标准俯卧撑的进阶台阶",
            "发力角度偏向胸上部，改善上胸薄弱",
            "适合做不了标准俯卧撑的人循序渐进"
        ),
        steps = listOf(
            "双手撑在稳固的凳子或台阶上，略宽于肩",
            "双脚后伸，身体从头到脚呈一条直线",
            "屈肘下降，至胸部轻触支撑边缘",
            "胸部发力推起，直到手臂自然伸直"
        ),
        mistakes = listOf(
            "塌腰或撅臀——核心收紧，保持直线",
            "支撑物不稳晃动——确认凳子靠墙固定再练",
            "手肘外展过大——大臂与躯干约呈 45 度"
        ),
        breathing = "下降时吸气，推起时呼气"
    ),
    ExerciseInfo(
        name = "宽距俯卧撑",
        illustration = "exercise",
        muscleGroups = listOf("胸大肌外侧", "三角肌前束"),
        difficulty = "初级",
        benefits = listOf(
            "双手间距加宽，胸肌拉伸与收缩幅度更大",
            "侧重胸肌外侧，让胸型更宽更饱满",
            "对肱三头肌要求低于标准俯卧撑，更专注胸"
        ),
        steps = listOf(
            "双手撑地约为肩宽的 1.5 倍，手指朝外约 45 度",
            "身体从头到脚呈一条直线，核心收紧",
            "屈肘下降，至胸部接近地面",
            "胸部发力推起，感受胸外侧收缩"
        ),
        mistakes = listOf(
            "间距过宽导致肩部受压——约 1.5 倍肩宽即可",
            "塌腰——腹部持续收紧",
            "幅度不足——胸部接近地面再推起"
        ),
        breathing = "下降时吸气，推起时呼气"
    ),
    ExerciseInfo(
        name = "钻石俯卧撑",
        illustration = "exercise",
        muscleGroups = listOf("肱三头肌", "胸大肌中缝"),
        difficulty = "中级",
        benefits = listOf(
            "双手拼成钻石形，强烈刺激肱三头肌",
            "同时雕刻胸肌中缝线条",
            "是俯卧撑向单臂进阶的重要过渡"
        ),
        steps = listOf(
            "双手食指与拇指相对拼成钻石形，置于胸口正下方",
            "身体呈一条直线，手肘贴近肋骨",
            "屈肘下降，至胸口轻触双手",
            "肱三头肌发力推起，顶端肘部不锁死"
        ),
        mistakes = listOf(
            "手肘向外打开——肘部始终贴近身体",
            "重心前移压手腕——手掌落在胸口正下方",
            "撅臀——核心收紧保持直线"
        ),
        breathing = "下降时吸气，推起时呼气"
    ),
    ExerciseInfo(
        name = "击掌俯卧撑",
        illustration = "exercise",
        muscleGroups = listOf("胸大肌", "肱三头肌", "上肢爆发力"),
        difficulty = "中级",
        benefits = listOf(
            "爆发力训练经典动作，提升推起速度与力量",
            "刺激快肌纤维，突破俯卧撑力量瓶颈",
            "对球类与格斗运动的上肢爆发力迁移性极好"
        ),
        steps = listOf(
            "从标准俯卧撑姿势开始，下降至胸部接近地面",
            "爆发式推起，让双手短暂离地完成击掌",
            "屈肘缓冲落地，进入下一次",
            "由少到多，熟练前可先练双手离地不击掌"
        ),
        mistakes = listOf(
            "落地直肘硬砸——屈肘缓冲保护手腕肘",
            "只追求击掌不顾幅度——下降要到位",
            "塌腰——全程核心收紧"
        ),
        breathing = "推起时短促呼气，下降时吸气"
    ),
    ExerciseInfo(
        name = "双杠臂屈伸",
        illustration = "exercise",
        muscleGroups = listOf("肱三头肌", "胸大肌下束", "三角肌前束"),
        difficulty = "中级",
        benefits = listOf(
            "自重推类王牌，上肢推力量的第二标杆",
            "身体略前倾偏胸、直立偏三头，可自由侧重",
            "强化肩带稳定性，卧推力量的好搭档"
        ),
        steps = listOf(
            "双手握杠跳上支撑，手臂伸直，肩胛下沉",
            "身体微前倾（偏胸）或保持直立（偏三头）",
            "屈肘下降，至大臂约与地面平行",
            "发力推起至手臂伸直，不锁死肘关节"
        ),
        mistakes = listOf(
            "下降过深——肩前侧疼痛信号，到大臂平行即可",
            "耸肩——全程肩胛下沉锁定",
            "晃腿借力——双腿并拢交叠，身体稳定"
        ),
        breathing = "下降时吸气，推起时呼气"
    ),
    ExerciseInfo(
        name = "引体缓降",
        illustration = "exercise",
        muscleGroups = listOf("背阔肌", "肱二头肌"),
        difficulty = "初级",
        benefits = listOf(
            "引体向上最有效的过渡练习，只做下降段",
            "离心收缩对背部力量的刺激更强",
            "帮你从 0 个引体稳步走向第一个标准引体"
        ),
        steps = listOf(
            "踩凳子或跳起到达引体顶端（下巴过杠）",
            "全程绷紧背部与手臂",
            "用 3~5 秒缓慢下放，直到手臂完全伸直",
            "回落底部后重新上杠，进行下一次"
        ),
        mistakes = listOf(
            "自由落体——下放越慢效果越好",
            "中途松劲——全程保持张力",
            "借蹬腿拖延——专注控制下降速度"
        ),
        breathing = "下放过程中保持均匀呼吸"
    ),
    ExerciseInfo(
        name = "反手引体向上",
        illustration = "exercise",
        muscleGroups = listOf("背阔肌", "肱二头肌"),
        difficulty = "中级",
        benefits = listOf(
            "反手握法让肱二头肌参与更多，通常比正手更容易",
            "背阔肌下部与二头刺激更集中",
            "从反手入门是走向正手引体的好路径"
        ),
        steps = listOf(
            "掌心朝向自己，双手与肩同宽握杠",
            "肩胛下沉，屈肘将身体向上拉起",
            "下巴过杠，顶端稍停",
            "缓慢下放至手臂伸直"
        ),
        mistakes = listOf(
            "借助惯性甩身——核心收紧匀速拉起",
            "手腕过度屈曲——杠压在掌根而非手指",
            "半程——完整幅度才有完整刺激"
        ),
        breathing = "上拉时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "悬垂举腿",
        illustration = "exercise",
        muscleGroups = listOf("下腹部", "髂腰肌", "握力"),
        difficulty = "中级",
        benefits = listOf(
            "悬垂姿势消除地面借力，下腹刺激最纯粹",
            "同步提升握力与肩带稳定",
            "核心抗摆动控制，功能性极强"
        ),
        steps = listOf(
            "正握悬垂于单杠，肩胛收紧身体不晃",
            "膝盖微屈，腹肌发力将腿向上抬起",
            "抬至大腿与地面平行以上（能力够可举向脚尖）",
            "缓慢下放，避免身体前后摆动"
        ),
        mistakes = listOf(
            "身体前后荡——放慢速度，肩背绷住",
            "只抬腿不动骨盆——顶端卷起骨盆，腹肌才收缩",
            "靠惯性甩腿——全程由腹部控制"
        ),
        breathing = "上抬时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "单腿深蹲",
        illustration = "squat",
        muscleGroups = listOf("股四头肌", "臀大肌", "核心"),
        difficulty = "高级",
        benefits = listOf(
            "自重腿部力量的最高标准之一",
            "单侧训练彻底改善左右腿不平衡",
            "对踝、膝、髋的稳定与柔韧都是全面考验"
        ),
        steps = listOf(
            "站立后抬起一条腿向前伸直（可扶物辅助平衡）",
            "另一条腿屈髋屈膝缓慢下蹲",
            "下蹲至大腿贴小腿的可用深度，膝盖对准脚尖",
            "脚跟发力站起，全程重心落在支撑脚全脚掌"
        ),
        mistakes = listOf(
            "脚跟离地——踝关节活动度不足可垫高脚跟练习",
            "膝盖内扣——始终对准脚尖方向",
            "借助反弹站起——下蹲慢、站起控制"
        ),
        breathing = "下蹲时吸气，站起时呼气"
    ),
    ExerciseInfo(
        name = "保加利亚分腿蹲",
        illustration = "thigh",
        muscleGroups = listOf("股四头肌", "臀大肌", "腘绳肌"),
        difficulty = "中级",
        benefits = listOf(
            "后脚垫高版本弓步蹲，单腿刺激强度翻倍",
            "对臀部和腘绳肌的塑造效果出色",
            "改善平衡与髋部稳定性，膝伤风险低于大重量双腿蹲"
        ),
        steps = listOf(
            "后脚背搭在凳子上，前脚向前迈一大步",
            "重心垂直下降，前膝弯曲至约 90 度",
            "后膝接近地面但不触地",
            "前腿发力站起，单侧做完换边"
        ),
        mistakes = listOf(
            "前脚距离凳子太近——步幅要够大",
            "身体过度前倾——挺直躯干，重心在前腿",
            "借力弹起——缓慢有控制地完成"
        ),
        breathing = "下蹲时吸气，站起时呼气"
    ),
    ExerciseInfo(
        name = "深蹲跳",
        illustration = "squat",
        muscleGroups = listOf("股四头肌", "臀大肌", "心肺"),
        difficulty = "中级",
        benefits = listOf(
            "深蹲的爆发力版本，燃脂心率拉得极快",
            "发展下肢弹性与垂直跳跃能力",
            "徒手完成，随时随地都能练"
        ),
        steps = listOf(
            "双脚与肩同宽站立，先做一次四分之一深蹲蓄力",
            "爆发向上跳起，手臂向后摆带动",
            "落地时屈膝屈髋缓冲，直接衔接下一次",
            "保持节奏连贯，膝盖始终对准脚尖"
        ),
        mistakes = listOf(
            "落地膝盖内扣——对准脚尖，轻柔缓冲",
            "蹲得太浅就跳——至少蹲到大腿半程再发力",
            "直腿落地——永远屈膝落地"
        ),
        breathing = "跳起时呼气，落地缓冲时吸气"
    ),
    ExerciseInfo(
        name = "靠墙倒立撑",
        illustration = "exercise",
        muscleGroups = listOf("三角肌", "肱三头肌", "肩带稳定"),
        difficulty = "高级",
        benefits = listOf(
            "肩部力量的天花板级自重训练",
            "倒立姿势顺带锻炼核心与平衡感",
            "突破推举平台期的秘密武器"
        ),
        steps = listOf(
            "背靠墙爬升成倒立姿势，双手略宽于肩",
            "头顶轻触地面为目标点，肘部微屈蓄力",
            "发力推起身体，直到手臂伸直",
            "全程核心收紧，身体不塌腰"
        ),
        mistakes = listOf(
            "腰腹塌陷香蕉形——臀腹收紧身体成直线",
            "下降过深撞头——控制幅度循序渐进",
            "手腕未热身——先充分活动手腕再练"
        ),
        breathing = "推起时呼气，下降时吸气，不要憋气过久"
    ),
    ExerciseInfo(
        name = "侧平板支撑",
        illustration = "exercise",
        muscleGroups = listOf("腹斜肌", "核心侧链", "肩带"),
        difficulty = "入门",
        benefits = listOf(
            "针对性强化腹斜肌与腰方肌，收紧侧腰",
            "改善骨盆侧向稳定，跑步步态更稳",
            "动作简单，两侧对称练习防止肌力失衡"
        ),
        steps = listOf(
            "侧卧，肘部撑地在肩正下方，双脚并拢",
            "髋部抬起离地，身体从头到脚呈一条直线",
            "上侧手臂叉腰或伸直指向天花板",
            "保持规定时长，均匀呼吸，换边重复"
        ),
        mistakes = listOf(
            "髋部下沉——臀部持续收紧上顶",
            "身体前后倾——肩肘髋三点在同一平面",
            "耸肩——肩膀远离耳朵"
        ),
        breathing = "全程均匀呼吸，不要憋气"
    )
)
