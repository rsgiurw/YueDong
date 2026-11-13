package com.yuedong.fitness.domain.logic

/**
 * 动作教学数据·既有分类扩充（v2.6）。
 * 覆盖 胸肩/背臂/腿臀/核心/有氧/拉伸放松 六个原有分类的新增动作。
 */
internal val extraMixed: List<ExerciseInfo> = listOf(
    // ---------- 胸肩 ----------
    ExerciseInfo(
        name = "阿诺德推举",
        illustration = "arnold_press",
        muscleGroups = listOf("三角肌前束", "三角肌中束"),
        difficulty = "初级",
        benefits = listOf(
            "推举过程旋转手腕，三角肌前中束同时受到刺激",
            "传奇动作，肩部围度与线条兼顾",
            "比普通推举的张力时间更长"
        ),
        steps = listOf(
            "坐姿或站姿，双手持哑铃举至肩前，掌心朝内",
            "向上推起的同时旋转手腕，至顶端掌心朝前",
            "顶端手臂接近伸直，不锁死",
            "沿原轨迹旋转下放回起始位"
        ),
        mistakes = listOf(
            "旋转过快变成普通推举——匀速旋转增加张力",
            "腰部反弓——收腹沉肩",
            "重量过大变形——从小重量学动作"
        ),
        breathing = "推起时呼气，下放时吸气"
    ),
    // ---------- 背臂 ----------
    ExerciseInfo(
        name = "直臂下压",
        illustration = "straight_arm_pulldown",
        muscleGroups = listOf("背阔肌", "大圆肌", "肱三头肌"),
        difficulty = "初级",
        benefits = listOf(
            "肘部锁定让背阔肌孤立发力，练背先找感觉的最佳选择",
            "强化背阔肌向心收缩前的拉伸能力",
            "改善硬拉与引体中的背阔肌激活"
        ),
        steps = listOf(
            "面向龙门架，双手与肩同宽正握直杆，后退一步",
            "上身微微前倾，手臂伸直与地面约呈 45 度",
            "肩胛下沉，直臂将横杆下压至大腿前侧",
            "缓慢回放至手臂充分上举，感受背部拉伸"
        ),
        mistakes = listOf(
            "屈肘变成三头下压——肘部全程锁定伸直",
            "耸肩——肩胛先下沉再下压",
            "用身体重量下压——躯干稳定，背部发力"
        ),
        breathing = "下压时呼气，回放时吸气"
    ),
    ExerciseInfo(
        name = "反手高位下拉",
        illustration = "reverse_lat_pulldown",
        muscleGroups = listOf("背阔肌下部", "肱二头肌"),
        difficulty = "初级",
        benefits = listOf(
            "反手握让二头更多参与，拉起重量更大",
            "侧重背阔肌下部与下背衔接的线条",
            "与正手下拉搭配，背部全方位发展"
        ),
        steps = listOf(
            "反手握距与肩同宽，大腿固定，上身微后倾",
            "肩胛下沉，肘部贴身向下拉横杆至腹部上方",
            "顶端挤压背阔肌",
            "缓慢回放至手臂伸直"
        ),
        mistakes = listOf(
            "身体后仰过多——固定 15 度左右",
            "手腕翻转变弯举——杠压在掌根",
            "回放过快——离心控制 2~3 秒"
        ),
        breathing = "下拉时呼气，回放时吸气"
    ),
    ExerciseInfo(
        name = "蝴蝶机反飞",
        illustration = "reverse_pec_deck",
        muscleGroups = listOf("三角肌后束", "斜方肌中部"),
        difficulty = "入门",
        benefits = listOf(
            "器械版俯身飞鸟，后束孤立训练最省心的方式",
            "轨迹固定不借力，新手也能精准刺激肩后束",
            "平衡胸肩肌力，改善圆肩体态"
        ),
        steps = listOf(
            "面对蝴蝶机坐好，胸口贴紧靠垫",
            "双手握把手，手臂与肩同高微屈",
            "向身体后方展开双手，感受肩后束收缩",
            "顶端停顿一秒，缓慢还原"
        ),
        mistakes = listOf(
            "上身离开靠垫借力——胸口始终贴紧",
            "耸肩——肩胛下沉",
            "幅度不足——双手展开至身体正侧方"
        ),
        breathing = "展开时呼气，还原时吸气"
    ),
    // ---------- 腿臀 ----------
    ExerciseInfo(
        name = "腿弯举",
        illustration = "leg_curl",
        muscleGroups = listOf("腘绳肌", "小腿三头肌"),
        difficulty = "入门",
        benefits = listOf(
            "腘绳肌唯一的孤立训练器械动作",
            "平衡股四头肌与腘绳肌力量，保护膝盖",
            "俯卧版本对腰椎几乎无压力"
        ),
        steps = listOf(
            "俯卧于器械，脚踝后侧贴紧滚垫，双腿伸直",
            "双手握紧把手，髋部贴紧垫面",
            "腘绳肌发力屈膝，将滚垫拉向臀部",
            "缓慢还原至双腿伸直"
        ),
        mistakes = listOf(
            "髋部抬起借力——髂骨贴住垫面",
            "顶端过快——膝盖弯曲到最紧处停顿",
            "还原自由落体——离心放慢"
        ),
        breathing = "屈膝时呼气，还原时吸气"
    ),
    ExerciseInfo(
        name = "腿屈伸",
        illustration = "leg_extension",
        muscleGroups = listOf("股四头肌"),
        difficulty = "入门",
        benefits = listOf(
            "股四头肌最直接的孤立训练动作",
            "强化膝盖周围肌腱与韧带，保护膝关节",
            "深蹲与腿举的绝佳补充"
        ),
        steps = listOf(
            "坐姿调整靠背，膝盖对准器械转轴，脚踝贴紧滚垫",
            "双手握住座椅把手，上身稳定",
            "股四头肌发力伸直双腿，顶端停顿一秒",
            "缓慢还原至膝盖约 90 度，配重不归位"
        ),
        mistakes = listOf(
            "借助上身摆动——背部贴紧靠背",
            "下放过快——缓慢还原保持张力",
            "顶端不停顿——停顿一秒挤压股四头肌"
        ),
        breathing = "伸腿时呼气，还原时吸气"
    ),
    ExerciseInfo(
        name = "坐姿提踵",
        illustration = "seated_calf_raise",
        muscleGroups = listOf("小腿三头肌", "比目鱼肌"),
        difficulty = "入门",
        benefits = listOf(
            "屈膝姿势专门刺激比目鱼肌，与站姿提踵互补",
            "小腿整体围度与线条的必备动作",
            "坐姿稳定，重量可以上得很大"
        ),
        steps = listOf(
            "坐姿将前脚掌踩在踏板上，滚垫压在膝盖上部",
            "脚跟下沉至小腿充分拉伸",
            "前脚掌发力踮起至最高点",
            "顶端停顿一秒，缓慢下放"
        ),
        mistakes = listOf(
            "用手压膝盖代偿——腿部自身发力",
            "幅度太小——每个都做全幅度",
            "速度过快——慢起慢落加停顿"
        ),
        breathing = "踮起时呼气，下放时吸气"
    ),
    // ---------- 核心 ----------
    ExerciseInfo(
        name = "死虫式",
        illustration = "exercise",
        muscleGroups = listOf("深层核心", "腹横肌"),
        difficulty = "入门",
        benefits = listOf(
            "核心康复训练之王，强化深层腹横肌",
            "仰卧位对腰椎零压力，腰痛人群的首选",
            "学会在四肢活动中稳定骨盆与脊柱"
        ),
        steps = listOf(
            "仰卧，双臂伸直指向天花板，双腿抬起屈膝 90 度",
            "下背贴实地垫，腰部不留缝隙",
            "对侧手臂与腿同时缓慢伸展放低，不触地",
            "收回原位，换另一侧交替进行"
        ),
        mistakes = listOf(
            "腰部拱起离地——下背始终压实垫子",
            "速度太快——像死虫一样缓慢移动",
            "憋气——全程匀速呼吸"
        ),
        breathing = "伸展时呼气，收回时吸气"
    ),
    ExerciseInfo(
        name = "鸟狗式",
        illustration = "exercise",
        muscleGroups = listOf("深层核心", "竖脊肌", "臀大肌"),
        difficulty = "入门",
        benefits = listOf(
            "对侧手脚伸展训练核心抗旋转能力",
            "强化下背稳定性，腰突康复的经典动作",
            "四点支撑位安全，负荷可控"
        ),
        steps = listOf(
            "四点支撑，手腕在肩正下方，膝盖在髋正下方",
            "背部保持平直，收紧核心",
            "对侧手臂向前伸直，同时同侧腿向后伸直",
            "顶端停顿两秒，缓慢收回，换边交替"
        ),
        mistakes = listOf(
            "腰部塌陷或过度旋转——脊柱保持中立位",
            "抬手抬腿过高——以不破坏骨盆稳定为准",
            "速度过快——慢伸出慢收回"
        ),
        breathing = "伸展时呼气，收回时吸气"
    ),
    ExerciseInfo(
        name = "超人式",
        illustration = "exercise",
        muscleGroups = listOf("下背竖脊肌", "臀大肌", "腘绳肌"),
        difficulty = "入门",
        benefits = listOf(
            "俯卧反向卷腹，强化下背的黄金动作",
            "平衡腹背肌力，预防久坐腰痛",
            "零器械零空间，随时随地可做"
        ),
        steps = listOf(
            "俯卧于垫上，双臂向前伸直，双腿并拢",
            "呼气时同时抬起手臂、胸口与双腿",
            "顶端保持 2 秒，感受下背与臀部收缩",
            "缓慢放下还原，重复"
        ),
        mistakes = listOf(
            "颈部过度后仰——目视垫面，颈部保持中立",
            "借助爆发力甩起——缓慢抬起缓慢放下",
            "幅度追求过高——微酸即可，疼痛即停"
        ),
        breathing = "抬起时呼气，下落时吸气"
    ),
    ExerciseInfo(
        name = "仰卧单车",
        illustration = "situp",
        muscleGroups = listOf("腹直肌", "腹斜肌", "髂腰肌"),
        difficulty = "初级",
        benefits = listOf(
            "被研究评为腹直肌与腹斜肌刺激排名第一的动作",
            "旋转加屈髋的复合模式，效率极高",
            "徒手完成，燃脂塑腰两不误"
        ),
        steps = listOf(
            "仰卧，双手轻扶耳侧，双腿抬起离地",
            "上身向一侧卷起旋转，同时该侧膝盖向胸口收回",
            "对侧腿伸直放低，肘部尽量靠近对侧膝盖",
            "左右交替，像骑自行车一样连贯"
        ),
        mistakes = listOf(
            "双手抱头拉扯颈部——手只轻扶",
            "只动肘部不转肩——肩膀要转过去",
            "腰部离地——下背压实垫面"
        ),
        breathing = "每收一次呼气一次，节奏均匀"
    ),
    ExerciseInfo(
        name = "V字两头起",
        illustration = "situp",
        muscleGroups = listOf("腹直肌", "髂腰肌"),
        difficulty = "中级",
        benefits = listOf(
            "上下腹同时收缩的高效动作",
            "强化髋屈肌力量与身体折叠能力",
            "幅度大开大合，对核心整合能力要求高"
        ),
        steps = listOf(
            "仰卧，双臂伸直置于头后，双腿伸直",
            "腹部发力，上半身与双腿同时向上抬起",
            "指尖尽力触碰脚背，身体呈 V 字形",
            "缓慢下放还原，脚与手不落地连续进行"
        ),
        mistakes = listOf(
            "爆发甩起——用腹肌控制全过程",
            "屈膝完成——双腿保持伸直才有强度",
            "腰部猛砸落地——缓慢下放有控制"
        ),
        breathing = "起时呼气，落时吸气"
    ),
    ExerciseInfo(
        name = "触踝卷腹",
        illustration = "situp",
        muscleGroups = listOf("腹直肌", "腹斜肌"),
        difficulty = "入门",
        benefits = listOf(
            "小幅卷腹配合左右触踝，侧腹持续紧张",
            "节奏轻快，燃脂训练的常用动作",
            "强度适中，适合放在核心训练中后段"
        ),
        steps = listOf(
            "仰卧屈膝，双脚踩地略宽于肩",
            "上身微微卷起离开垫面并保持",
            "右手去触碰右脚踝，随后左手触碰左脚踝",
            "左右交替，全程上背不落地"
        ),
        mistakes = listOf(
            "完全躺平再起——上身保持卷起状态",
            "用手臂够脚而身体不转——侧腹要发力",
            "脖子紧张——眼睛看向天花板方向"
        ),
        breathing = "每触一次呼气一次"
    ),
    ExerciseInfo(
        name = "仰卧起坐",
        illustration = "situp",
        muscleGroups = listOf("腹直肌", "髂腰肌"),
        difficulty = "初级",
        benefits = listOf(
            "最经典的腹肌训练动作，人人都认识",
            "完整卷起幅度对上腹刺激强",
            "可固定脚部或加载负重进阶"
        ),
        steps = listOf(
            "仰卧屈膝约 90 度，双手交叉置于胸前或耳侧",
            "腹部发力将上身卷起坐起",
            "上身立直后缓慢下放",
            "下背触地即开始下一次，全程受控"
        ),
        mistakes = listOf(
            "抱头拉颈——手放胸前或耳侧",
            "猛起猛落——全程慢速控制",
            "脚离地借力——双脚踩实或请人固定"
        ),
        breathing = "起身时呼气，下落时吸气"
    ),
    ExerciseInfo(
        name = "平板支撑开合跳",
        illustration = "exercise",
        muscleGroups = listOf("核心", "肩带", "心肺"),
        difficulty = "初级",
        benefits = listOf(
            "静态支撑叠加动态开合，核心稳定与燃脂兼得",
            "强度高于普通平板支撑，耗时短见效快",
            "强化肩带动态稳定性"
        ),
        steps = listOf(
            "从标准平板支撑姿势开始，肘部撑地",
            "核心收紧，双脚像开合跳一样向外跳开",
            "再跳回并拢，为一个完整动作",
            "臀部保持水平，连续完成规定时长"
        ),
        mistakes = listOf(
            "跳开时臀部抬高——臀部与肩背保持水平",
            "塌腰——腹部持续收紧",
            "落地声音过大——前脚掌轻触缓冲"
        ),
        breathing = "开合配合呼吸节奏，不要憋气"
    ),
    // ---------- 有氧 ----------
    ExerciseInfo(
        name = "椭圆机",
        illustration = "exercise",
        muscleGroups = listOf("心肺", "下肢"),
        difficulty = "入门",
        benefits = listOf(
            "全身有氧器械中对膝盖冲击最小的一款",
            "上下肢同时参与，燃脂效率高",
            "阻力可调，适合所有水平人群"
        ),
        steps = listOf(
            "双脚踩稳踏板，双手轻扶活动把手",
            "保持上身直立，核心收紧",
            "脚跟不离踏板，随滑轨画椭圆轨迹",
            "推拉把手带动上肢，保持稳定节奏"
        ),
        mistakes = listOf(
            "踮脚尖踩踏——全脚掌贴合踏板",
            "身体左右晃动——核心收紧保持直立",
            "阻力调太低滑空——保持适中阻力"
        ),
        breathing = "保持能说话的有氧强度呼吸"
    ),
    ExerciseInfo(
        name = "划船机",
        illustration = "exercise",
        muscleGroups = listOf("心肺", "背阔肌", "腿部"),
        difficulty = "初级",
        benefits = listOf(
            "全身 85% 肌肉参与的高效有氧器械",
            "推蹬拉桨强化背部与腿部，体态更挺拔",
            "对膝关节冲击小，大体重人群友好"
        ),
        steps = listOf(
            "屈膝收腿握住把手，上身微微前倾",
            "先蹬腿，再顺势后倾上身，最后屈肘拉桨至腹部",
            "按相反顺序还原：伸臂、送髋、屈膝",
            "保持动作连贯，划桨节奏稳定"
        ),
        mistakes = listOf(
            "先拉臂后蹬腿——发力顺序是腿、身、臂",
            "弓腰——背部挺直，核心收紧",
            "只用手臂划——腿部蹬伸才是主要动力"
        ),
        breathing = "拉桨时呼气，回桨时吸气"
    ),
    ExerciseInfo(
        name = "动感单车",
        illustration = "exercise",
        muscleGroups = listOf("心肺", "股四头肌"),
        difficulty = "入门",
        benefits = listOf(
            "室内有氧人气之王，跟课骑行燃脂氛围好",
            "坐姿完成对关节冲击小",
            "阻力随时可调，新手到高阶都适用"
        ),
        steps = listOf(
            "调整座椅高度至站立时髋部等高，车把略高于座椅",
            "双脚前脚掌踩稳踏板并固定",
            "上身放松微前倾，核心收紧",
            "跟随节奏踩踏，站姿骑行时臀部微微离开座椅"
        ),
        mistakes = listOf(
            "座椅过低膝盖压力大——踏板最低点腿接近伸直",
            "完全用脚尖踩——前脚掌大面积接触踏板",
            "手腕压死——手臂微屈放松"
        ),
        breathing = "保持有节奏的深呼吸"
    ),
    ExerciseInfo(
        name = "游泳",
        illustration = "exercise",
        muscleGroups = listOf("心肺", "全身"),
        difficulty = "入门",
        benefits = listOf(
            "水的浮力让关节几乎零负担，全龄友好",
            "全身肌群参与，心肺与体态同步改善",
            "夏季燃脂首选，体能消耗大体感舒适"
        ),
        steps = listOf(
            "下水前做好 5 分钟陆上热身",
            "以蛙泳或自由泳为主，动作放松",
            "保持匀速游进，注意换气节奏",
            "每次 20~30 分钟，组间在池边休息"
        ),
        mistakes = listOf(
            "空腹或饱腹游泳——饭后一小时再下水",
            "憋气硬游——掌握换气节奏再追求距离",
            "不热身直接下水——易抽筋，务必先热身"
        ),
        breathing = "掌握水中吐气、水面吸气的节奏"
    ),
    // ---------- 拉伸放松 ----------
    ExerciseInfo(
        name = "大腿前侧拉伸",
        illustration = "exercise",
        muscleGroups = listOf("股四头肌"),
        difficulty = "入门",
        benefits = listOf(
            "深蹲、跑步后放松大腿前侧的必备拉伸",
            "改善久坐导致的股四头肌紧张",
            "缓解膝盖周围紧张感"
        ),
        steps = listOf(
            "站立，一手扶墙保持平衡",
            "另一手抓住同侧脚背，将脚跟向臀部方向拉近",
            "保持膝盖朝下，感受大腿前侧拉伸",
            "保持 15~30 秒后换边"
        ),
        mistakes = listOf(
            "腰部过度反弓——收紧腹部再拉伸",
            "膝盖外撇——膝盖指向地面方向",
            "弹震拉扯——静态保持即可"
        ),
        breathing = "呼气时将脚跟再拉近一点"
    ),
    ExerciseInfo(
        name = "蝴蝶式拉伸",
        illustration = "exercise",
        muscleGroups = listOf("大腿内侧", "髋部"),
        difficulty = "入门",
        benefits = listOf(
            "打开髋部、拉伸大腿内侧的首选动作",
            "改善久坐导致的髋部僵硬",
            "女性经期放松与产后恢复常用动作"
        ),
        steps = listOf(
            "坐姿，双脚脚心相对，双膝向两侧打开",
            "双手握住脚背，上身挺直",
            "呼气时上身缓缓前倾，膝盖向地面下沉",
            "保持 30 秒，感受大腿内侧拉伸"
        ),
        mistakes = listOf(
            "弓背压膝——挺直脊柱再前倾",
            "用蛮力下压膝盖——循序渐进",
            "脚离身体太远——脚跟靠近会阴效果更好"
        ),
        breathing = "深呼吸，呼气时放松下沉"
    ),
    ExerciseInfo(
        name = "胸部拉伸",
        illustration = "exercise",
        muscleGroups = listOf("胸大肌", "三角肌前束"),
        difficulty = "入门",
        benefits = listOf(
            "打开紧张圆肩，改善含胸体态",
            "卧推练后放松胸肌，恢复更快",
            "借助门框即可完成，随时随地"
        ),
        steps = listOf(
            "站在门框旁，前臂贴紧门框，肘部与肩同高",
            "向前迈出一小步，身体前转",
            "感受胸部前侧明显拉伸，保持 15~30 秒",
            "换另一侧手臂与门框重复"
        ),
        mistakes = listOf(
            "肘部过低——肘与肩同高拉伸胸大肌",
            "耸肩——肩膀下沉远离耳朵",
            "力度过大产生疼痛——微酸即止"
        ),
        breathing = "呼气时身体再前转一点"
    ),
    ExerciseInfo(
        name = "小腿拉伸",
        illustration = "exercise",
        muscleGroups = listOf("小腿三头肌", "跟腱"),
        difficulty = "入门",
        benefits = listOf(
            "跑步跳跃后放松小腿，预防跟腱紧张",
            "改善穿高跟鞋导致的小腿短缩",
            "促进下肢血液回流，缓解腿胀"
        ),
        steps = listOf(
            "面对墙壁站立，双手推墙",
            "一只脚向后撤一大步，脚跟踩实地面",
            "前膝弯曲，后腿保持伸直",
            "感受后侧小腿拉伸，保持 15~30 秒换边"
        ),
        mistakes = listOf(
            "后脚跟离地——脚跟务必踩实",
            "后腿膝盖弯曲——伸直才能拉到腓肠肌",
            "身体歪斜——髋部朝向正前方"
        ),
        breathing = "自然呼吸，呼气时再深入"
    ),
    ExerciseInfo(
        name = "90/90髋部拉伸",
        illustration = "exercise",
        muscleGroups = listOf("髋部旋转肌群", "臀部"),
        difficulty = "初级",
        benefits = listOf(
            "改善髋内旋与外旋活动度的全能动作",
            "缓解久坐髋部僵硬与深蹲时髋部卡顿",
            "一块垫子就能完成，体态与运动表现双提升"
        ),
        steps = listOf(
            "坐于垫上，前腿屈膝 90 度平放身前",
            "后腿屈膝 90 度置于身体侧后方",
            "上身保持挺直，向前倾压向前腿方向",
            "保持 30 秒后换边，双腿互换位置"
        ),
        mistakes = listOf(
            "两侧肩膀一高一低——上身朝前腿方向摆正",
            "塌腰弓背——脊柱保持中立延展",
            "硬压产生疼痛——微酸即可"
        ),
        breathing = "呼气时上身再前倾一点"
    ),
    ExerciseInfo(
        name = "站姿侧腰拉伸",
        illustration = "exercise",
        muscleGroups = listOf("腹斜肌", "腰方肌"),
        difficulty = "入门",
        benefits = listOf(
            "拉伸侧腰与腹斜肌，缓解久坐腰部酸胀",
            "改善脊柱侧向柔韧性",
            "工作间隙站立即可完成"
        ),
        steps = listOf(
            "站立，双脚与髋同宽，一手叉腰",
            "另一侧手臂向上伸直过头",
            "呼气时身体向叉腰侧弯曲，上方手臂延展",
            "保持 15~30 秒后换边"
        ),
        mistakes = listOf(
            "身体前倾代替侧屈——在侧向平面内弯曲",
            "耸肩——肩膀下沉",
            "弹震——静态保持"
        ),
        breathing = "侧屈时呼气，保持时深呼吸"
    )
)
