package com.yuedong.fitness.domain.logic

/**
 * 动作教学数据·杠铃力量（v2.6 新增分类）。
 * 覆盖 CommonExercises「杠铃力量」分类：健身房杠铃三大项及其变式。
 */
internal val extraBarbell: List<ExerciseInfo> = listOf(
    ExerciseInfo(
        name = "杠铃深蹲",
        illustration = "squat",
        muscleGroups = listOf("股四头肌", "臀大肌", "核心"),
        difficulty = "中级",
        benefits = listOf(
            "力量训练之王，全身肌肉与激素水平全面提升",
            "下肢力量与骨密度增长效率最高的动作",
            "深蹲模式是跑跳等一切下肢运动的基础"
        ),
        steps = listOf(
            "杠铃置于斜方肌上部，双手握稳，出杠后站距与肩同宽",
            "吸气收腹，屈髋屈膝同步下蹲",
            "蹲至大腿约与地面平行或略深，膝盖对准脚尖",
            "蹬地站起，呼气，顶端膝盖不锁死"
        ),
        mistakes = listOf(
            "弓背弯腰——挺胸收腹，背部刚性锁住",
            "膝盖内扣——始终对准脚尖方向",
            "重心前移脚跟离地——重心落在全脚掌"
        ),
        breathing = "下蹲前吸气憋住稳定核心，站起过程呼气"
    ),
    ExerciseInfo(
        name = "杠铃硬拉",
        illustration = "exercise",
        muscleGroups = listOf("臀大肌", "腘绳肌", "竖脊肌"),
        difficulty = "中级",
        benefits = listOf(
            "全身力量与后侧链条训练之王",
            "强化臀腿背的同时提升握力与核心刚性",
            "正确的硬拉模式是搬运重物最安全的姿势基础"
        ),
        steps = listOf(
            "杠铃贴紧小腿，屈髋俯身正握杠铃，背部平直",
            "深吸气绷紧核心，蹬地拉起杠铃贴身上行",
            "站直时臀部完全收紧，肩胛中立",
            "以髋为铰链俯身，控制杠铃贴腿下放回地面"
        ),
        mistakes = listOf(
            "弓背起拉——腰椎风险极高，重量减到能挺直背",
            "杠铃离腿——全程贴身上行",
            "顶端过度后仰——站直即可，不要向后晃"
        ),
        breathing = "拉起前吸气绷紧，过膝后呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "杠铃划船",
        illustration = "exercise",
        muscleGroups = listOf("背阔肌", "斜方肌中部", "竖脊肌"),
        difficulty = "中级",
        benefits = listOf(
            "大重量增厚背部，与硬拉卧推并称力量三大项",
            "俯身姿态下核心与下背同步得到强化",
            "改善体态，让背部更宽更厚"
        ),
        steps = listOf(
            "屈髋俯身约 45 度，膝盖微屈，背部平直",
            "正握杠铃垂于肩下方，肩胛先收紧",
            "肘部贴身向后拉杠铃至腹部",
            "缓慢下放还原，全程保持俯身角度"
        ),
        mistakes = listOf(
            "上身起伏借力——俯身角度全程锁住",
            "弓背——核心绷紧背平直，重量适中",
            "拉向胸口——目标下腹部，中背发力"
        ),
        breathing = "拉起时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "站姿杠铃推举",
        illustration = "exercise",
        muscleGroups = listOf("三角肌", "肱三头肌", "核心"),
        difficulty = "中级",
        benefits = listOf(
            "站姿推举是上肢力量与核心刚性的综合考验",
            "全身协调发力，力量迁移性极强",
            "强化肩部三束，打造宽厚肩部"
        ),
        steps = listOf(
            "杠铃架于锁骨前，双手略宽于肩，肘部微收",
            "双脚与髋同宽站立，臀腹收紧",
            "杠铃贴脸垂直向上推过头顶",
            "顶端手臂伸直，杠铃落在重心正上方，缓慢回落"
        ),
        mistakes = listOf(
            "腰部反弓——收腹夹臀，肋骨下压",
            "杠铃绕过下巴前推——贴脸直上直下",
            "膝盖弯曲借力——站姿锁膝，纯上肢发力"
        ),
        breathing = "推起前吸气，过顶稳定后呼气"
    ),
    ExerciseInfo(
        name = "上斜杠铃卧推",
        illustration = "exercise",
        muscleGroups = listOf("胸大肌上束", "三角肌前束", "肱三头肌"),
        difficulty = "中级",
        benefits = listOf(
            "大重量专项强化上胸，胸型更立体",
            "杠铃版本可上更大重量，力量增长快",
            "改善上胸薄弱导致的空杯胸型"
        ),
        steps = listOf(
            "凳面调至 30~45 度，肩胛后缩沉入凳面",
            "双手略宽于肩握杠，起杠移至上胸正上方",
            "控制杠铃下落轻触上胸位置",
            "胸部发力推起，手臂接近伸直不锁死"
        ),
        mistakes = listOf(
            "臀部离开凳面——沉臀贴凳稳定躯干",
            "落点过低撞胸口——固定落在锁骨与乳头连线上部",
            "弹杠借力——控制离心轻触即推"
        ),
        breathing = "下落时吸气，推起时呼气"
    ),
    ExerciseInfo(
        name = "杠铃臀桥",
        illustration = "exercise",
        muscleGroups = listOf("臀大肌", "腘绳肌", "核心"),
        difficulty = "初级",
        benefits = listOf(
            "臀部训练负荷天花板，精准高效增臀",
            "脊柱不承重，腰部不适者也能大重量练臀",
            "改善伸髋力量，跑跳与硬拉表现同步提升"
        ),
        steps = listOf(
            "上背靠稳凳边，杠铃加垫置于髋部",
            "双脚踩实地面，脚跟离臀约一拳半",
            "臀部发力顶起髋部，至躯干与大腿呈一条线",
            "顶端夹臀停顿一秒，缓慢下放"
        ),
        mistakes = listOf(
            "过度顶腰反弓——顶端肋骨下沉，夹臀即止",
            "脚距不对——脚跟再靠近臀部找臀部发力感",
            "用腰起桥——意念集中在臀部收缩"
        ),
        breathing = "顶起时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "杠铃耸肩",
        illustration = "exercise",
        muscleGroups = listOf("斜方肌上部", "握力"),
        difficulty = "入门",
        benefits = listOf(
            "大重量强化斜方肌，打造强壮颈肩线条",
            "动作简单，力量增长立竿见影",
            "顺带大幅提升握力"
        ),
        steps = listOf(
            "正握杠铃垂于体前，手臂伸直，身体站直",
            "肩膀垂直向上耸起，向耳朵方向靠拢",
            "顶端停顿一秒",
            "缓慢下放杠铃至完全拉伸"
        ),
        mistakes = listOf(
            "转肩绕环——垂直方向运动",
            "屈肘上拉——手臂锁死，只动肩膀",
            "幅度不足——耸到最高放到底部"
        ),
        breathing = "耸肩时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "杠铃早安式",
        illustration = "exercise",
        muscleGroups = listOf("腘绳肌", "臀大肌", "下背竖脊肌"),
        difficulty = "中级",
        benefits = listOf(
            "杠铃置于背上强化下背与腘绳肌的经典动作",
            "强化硬拉的锁定能力与背部刚性",
            "小重量就能获得强力的后侧链刺激"
        ),
        steps = listOf(
            "杠铃置于斜方肌上部，与深蹲同握法",
            "膝盖微屈锁定，挺胸收腹",
            "以髋为轴俯身，臀部向后推送，杠铃沿腿下移",
            "至躯干接近与地面平行后，臀部发力顶髋站直"
        ),
        mistakes = listOf(
            "弓背——重量减半，宁轻勿弯",
            "变成深蹲——膝盖角度锁定不变",
            "下放过深——躯干约平行地面即可"
        ),
        breathing = "俯身时吸气，站直时呼气"
    )
)
