package com.yuedong.fitness.domain.logic

/**
 * 动作教学数据·臀部专项（v2.6 新增分类）。
 * 覆盖 CommonExercises「臀部专项」分类：针对臀部三束与髋稳定的孤立训练。
 */
internal val extraGlutes: List<ExerciseInfo> = listOf(
    ExerciseInfo(
        name = "单腿臀桥",
        illustration = "exercise",
        muscleGroups = listOf("臀大肌", "腘绳肌", "核心"),
        difficulty = "初级",
        benefits = listOf(
            "单腿版本让臀部发力翻倍，兼练骨盆稳定",
            "改善左右臀肌力不平衡",
            "无需器械，在家就能完成"
        ),
        steps = listOf(
            "仰卧屈膝，一条腿伸直抬起指向天花板",
            "支撑侧臀部发力顶起髋部至身体呈一条线",
            "顶端夹臀停顿一秒",
            "缓慢下放，单侧完成后换边"
        ),
        mistakes = listOf(
            "骨盆倾斜旋转——髋部保持水平朝上",
            "用腘绳肌代偿——脚跟再靠近臀部",
            "腰部反弓——顶到身体直线即可"
        ),
        breathing = "顶起时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "蚌式开合",
        illustration = "exercise",
        muscleGroups = listOf("臀中肌", "臀小肌"),
        difficulty = "入门",
        benefits = listOf(
            "激活臀中肌的第一名动作，改善膝痛髋弹响",
            "强化骨盆侧向稳定，跑步更稳",
            "幅度小难度低，康复与热身两相宜"
        ),
        steps = listOf(
            "侧卧屈膝约 90 度，双脚并拢叠放",
            "保持双脚相触，上侧膝盖像贝壳一样打开",
            "打开至最大幅度，感受臀部外侧收缩",
            "缓慢合拢还原，单侧完成后换边"
        ),
        mistakes = listOf(
            "骨盆后倒借力——躯干稳定，只动腿",
            "幅度追求大开——臀部外侧收紧即可",
            "速度过快——慢开慢合效果更好"
        ),
        breathing = "打开时呼气，合拢时吸气"
    ),
    ExerciseInfo(
        name = "跪姿后踢腿",
        illustration = "exercise",
        muscleGroups = listOf("臀大肌", "腘绳肌"),
        difficulty = "入门",
        benefits = listOf(
            "孤立刺激臀大肌，塑形提臀的经典动作",
            "跪姿稳定，腰腹压力小",
            "可绑弹力带或踝沙袋逐步进阶"
        ),
        steps = listOf(
            "四点支撑，手腕在肩正下方，膝盖在髋正下方",
            "一侧腿屈膝约 90 度，脚尖勾起",
            "臀部发力将大腿向后上方踢起，至大腿与地面平行",
            "缓慢收回，膝盖不落地连续完成，换边"
        ),
        mistakes = listOf(
            "塌腰借力——核心收紧，腰部不动",
            "向外侧踢——向后上方踢，臀部发力方向才对",
            "幅度不足——大腿至少踢到与地面平行"
        ),
        breathing = "后踢时呼气，收回时吸气"
    ),
    ExerciseInfo(
        name = "绳索后踢腿",
        illustration = "exercise",
        muscleGroups = listOf("臀大肌", "腘绳肌"),
        difficulty = "初级",
        benefits = listOf(
            "绳索恒定张力，臀部全程受力不间断",
            "站姿完成更稳定，重量可精细调节",
            "塑形提臀效果出色的器械动作"
        ),
        steps = listOf(
            "面向龙门架，踝带绑在一只脚踝上，双手扶架",
            "上身微微前倾，核心收紧",
            "臀部发力将绑带腿向后上方踢起",
            "缓慢还原，单侧完成规定次数后换边"
        ),
        mistakes = listOf(
            "上身大幅摆动——躯干稳定，只动髋关节",
            "向外侧踢——向后上方直线踢出",
            "还原过快——有控制地回放"
        ),
        breathing = "后踢时呼气，还原时吸气"
    ),
    ExerciseInfo(
        name = "髋外展机",
        illustration = "exercise",
        muscleGroups = listOf("臀中肌", "臀小肌"),
        difficulty = "入门",
        benefits = listOf(
            "器械轨迹固定，臀中肌孤立训练最省心",
            "强化骨盆侧向稳定，保护膝盖",
            "改善假胯宽，臀部上缘线条更紧致"
        ),
        steps = listOf(
            "坐姿调整挡板位置，膝盖外侧贴紧挡板",
            "背部贴紧靠背，双手握住扶手",
            "双腿发力向外展开至最大可控幅度",
            "缓慢还原，配重不完全归位保持张力"
        ),
        mistakes = listOf(
            "上身前倾借力——背部贴靠背，用髋发力",
            "幅度过大弓腰——展开到可控范围即可",
            "还原速度过快——缓慢回放保持张力"
        ),
        breathing = "外展时呼气，还原时吸气"
    ),
    ExerciseInfo(
        name = "站姿侧抬腿",
        illustration = "exercise",
        muscleGroups = listOf("臀中肌", "臀小肌"),
        difficulty = "入门",
        benefits = listOf(
            "徒手即可完成，随时改善臀部外侧松弛",
            "强化髋外展能力，单腿站立更稳",
            "坚持练习改善假胯宽体态"
        ),
        steps = listOf(
            "站立，一手扶椅背或墙面保持平衡",
            "外侧腿伸直向身体侧方抬起",
            "抬至约 45 度，感受臀部外侧收缩",
            "缓慢下放还原，单侧完成后换边"
        ),
        mistakes = listOf(
            "身体侧倾借力——躯干保持直立",
            "腿向前抬——向正侧方抬起",
            "速度过快——慢抬慢放"
        ),
        breathing = "抬腿时呼气，下放时吸气"
    )
)
