package com.yuedong.fitness.domain.logic

/**
 * 动作教学数据·有氧、热身与拉伸放松（结构对齐食品库 FoodDataExtra 的拆分模式）。
 * 覆盖 CommonExercises「有氧」「热身」「拉伸放松」分类中尚未收录教学的动作。
 */
internal val extraCardioStretch: List<ExerciseInfo> = listOf(
    ExerciseInfo(
        name = "开合跳",
        illustration = "exercise",
        muscleGroups = listOf("全身", "心肺"),
        difficulty = "入门",
        benefits = listOf(
            "30 秒就能拉高心率，热身燃脂两相宜",
            "全身大肌群参与，协调性同步提升",
            "零器械零空间，碎片时间就能完成"
        ),
        steps = listOf(
            "站立，双脚并拢，手臂自然下垂",
            "跳起同时双脚向两侧分开，双手从体侧上举至头顶击掌",
            "再次跳起收回双脚与手臂",
            "保持节奏连续完成，落地屈膝缓冲"
        ),
        mistakes = listOf(
            "落地直膝重砸——屈膝缓冲，轻盈落地",
            "手臂举不到位——双手举过头顶才算完整一次",
            "节奏忽快忽慢——跟着固定节拍更持久"
        ),
        breathing = "保持均匀节奏呼吸，不要憋气"
    ),
    ExerciseInfo(
        name = "高抬腿",
        illustration = "exercise",
        muscleGroups = listOf("髂腰肌", "下肢", "心肺"),
        difficulty = "入门",
        benefits = listOf(
            "快速提升心率，燃脂效率高的原地动作",
            "强化髋屈肌与腿部节奏感，跑步更轻盈",
            "顺带激活核心，姿态更挺拔"
        ),
        steps = listOf(
            "站立，核心收紧，目视前方",
            "原地交替提膝至髋部高度，大腿与地面平行",
            "前脚掌轻快落地，身体略微前倾",
            "双臂配合双腿自然摆动，保持稳定节奏"
        ),
        mistakes = listOf(
            "只抬小腿不抬膝——膝盖要顶到髋部高度",
            "弯腰驼背——挺胸收腹，重心略前倾",
            "落地沉重——前脚掌着地，保持弹性"
        ),
        breathing = "随节奏两步一吸两步一呼，量力调整"
    ),
    ExerciseInfo(
        name = "原地慢跑",
        illustration = "exercise",
        muscleGroups = listOf("心肺", "下肢"),
        difficulty = "入门",
        benefits = listOf(
            "最温和的有氧起步，零基础也能坚持",
            "不受天气场地限制，客厅就能完成",
            "唤醒下肢与心肺，适合热身与恢复日"
        ),
        steps = listOf(
            "站立放松，双臂弯曲呈跑步姿势",
            "原地交替抬腿，节奏如慢跑",
            "前脚掌着地，脚步轻快",
            "保持能边做边聊天的强度即可"
        ),
        mistakes = listOf(
            "落地过重——控制节奏，轻盈着地",
            "含胸低头——挺胸目视前方",
            "一开始就冲速度——先热身 2 分钟再提速"
        ),
        breathing = "鼻吸口呼，保持能说话的强度"
    ),
    ExerciseInfo(
        name = "跳绳",
        illustration = "exercise",
        muscleGroups = listOf("心肺", "小腿", "协调性"),
        difficulty = "初级",
        benefits = listOf(
            "单位时间燃脂效率之王，10 分钟约等于 30 分钟慢跑",
            "提升手脚协调与节奏感",
            "强化小腿与踝关节，跑跳更有弹性"
        ),
        steps = listOf(
            "握柄置于髋侧，大臂贴近身体，用手腕摇绳",
            "绳子过顶时前脚掌起跳，高度约 2~3 厘米",
            "前脚掌轻落地，膝盖微屈缓冲",
            "保持稳定节奏，绊绳就调整呼吸从头再来"
        ),
        mistakes = listOf(
            "甩大臂——锁住大臂，只用手腕发力",
            "跳得太高——高度够绳子通过即可，省力更持久",
            "落地直膝重砸——膝踝缓冲，前脚掌着地"
        ),
        breathing = "保持均匀呼吸，节奏乱了先稳住呼吸"
    ),
    ExerciseInfo(
        name = "波比跳",
        illustration = "exercise",
        muscleGroups = listOf("全身爆发", "心肺"),
        difficulty = "中级",
        benefits = listOf(
            "公认的燃脂效率天花板，全身肌群一轮打尽",
            "力量与心肺同时提升，对意志力也是锻炼",
            "徒手完成，时间成本极低"
        ),
        steps = listOf(
            "站立后下蹲，双手撑地，双脚后跳成俯撑姿势",
            "（可选）完成一次俯卧撑",
            "双脚跳回双手两侧",
            "垂直跃起击掌，落地屈膝缓冲进入下一次"
        ),
        mistakes = listOf(
            "塌腰后跳——先收紧核心再伸腿",
            "起跳高度不足——每次都完整跳起来，别省略",
            "落地直膝——屈膝缓冲保护膝盖"
        ),
        breathing = "全程尽量规律呼吸，力竭时更要避免憋气"
    ),
    ExerciseInfo(
        name = "快走或慢跑",
        illustration = "exercise",
        muscleGroups = listOf("心肺", "下肢"),
        difficulty = "入门",
        benefits = listOf(
            "门槛最低的规律有氧，减脂与心肺健康双收益",
            "对关节冲击小，大体重人群也友好",
            "强度用\"能否聊天\"就能自测，容易坚持"
        ),
        steps = listOf(
            "抬头挺胸，目视前方，肩膀放松",
            "手臂约呈 90 度随步伐自然摆动",
            "脚跟或全脚掌滚动落地，步幅自然",
            "保持能断续说话的强度，持续 20 分钟以上效果更佳"
        ),
        mistakes = listOf(
            "步幅过大跨步跑——小步幅高步频，重心落在身体正下方",
            "含胸摆臂无力——挺胸，摆臂带动节奏",
            "一上来就跑快——先快走 5 分钟热身"
        ),
        breathing = "鼻吸口呼，三步一吸三步一呼，能说话即可"
    ),
    ExerciseInfo(
        name = "原地热身走",
        illustration = "exercise",
        muscleGroups = listOf("全身"),
        difficulty = "入门",
        benefits = listOf(
            "温和唤醒全身，为正式训练做好准备",
            "促进关节滑液分泌，降低受伤概率",
            "强度极低，任何人群都能安全完成"
        ),
        steps = listOf(
            "站立挺胸，双臂自然下垂",
            "原地交替迈步，像散步一样",
            "双臂配合步伐自然摆动",
            "逐渐加快节奏，直到身体微微发热"
        ),
        mistakes = listOf(
            "弯腰看手机——挺直立姿，专注热身",
            "步子拖地——抬腿踏步，脚掌干净离地",
            "一开始就太快——循序渐进让心率平稳上升"
        ),
        breathing = "自然呼吸即可，让呼吸随节奏慢慢加深"
    ),
    ExerciseInfo(
        name = "全身拉伸",
        illustration = "exercise",
        muscleGroups = listOf("全身"),
        difficulty = "入门",
        benefits = listOf(
            "训练后放松肌群，第二天酸痛更轻",
            "长期坚持改善柔韧性与体态",
            "帮助心率平稳回落，是结束训练的正确方式"
        ),
        steps = listOf(
            "从颈肩开始，依次到大臂、胸背、腿后侧、小腿",
            "每个部位静态拉伸 15~30 秒，有牵拉感但不疼",
            "拉伸时保持自然呼吸，不要弹震",
            "左右两侧都要做，时间对等"
        ),
        mistakes = listOf(
            "弹震式拉伸——静态保持，不要上下弹动",
            "拉到疼痛——微痛即止，疼痛是受伤信号",
            "憋气——呼吸越放松，拉伸效果越好"
        ),
        breathing = "深长缓慢的呼吸，呼气时再深入一点"
    ),
    ExerciseInfo(
        name = "背阔肌拉伸",
        illustration = "exercise",
        muscleGroups = listOf("背阔肌", "肩背"),
        difficulty = "入门",
        benefits = listOf(
            "缓解练背后的背部紧张，恢复背部柔韧",
            "改善久坐含胸的肩背僵硬",
            "动作温和，随时随地可以做"
        ),
        steps = listOf(
            "站立或跪姿，双臂向上伸直交握",
            "吸气延伸脊柱，呼气时上身向一侧侧屈",
            "感受对侧腋下至腰背的牵拉，保持 15~30 秒",
            "换边重复，两侧时间对等"
        ),
        mistakes = listOf(
            "耸肩——肩膀先远离耳朵再侧屈",
            "身体前倾代替侧屈——在额状面内纯侧向弯曲",
            "弹震加压——静态保持，配合呼吸深入"
        ),
        breathing = "呼气时进入更深的拉伸"
    ),
    ExerciseInfo(
        name = "猫式伸展",
        illustration = "exercise",
        muscleGroups = listOf("脊柱", "背部"),
        difficulty = "入门",
        benefits = listOf(
            "一弓一塌活动整条脊柱，久坐腰背僵硬的解药",
            "唤醒核心与脊柱灵活性，安全的腰部保养动作",
            "跪姿完成，身体负担极小"
        ),
        steps = listOf(
            "四点支撑，手腕在肩正下方，膝盖在髋正下方",
            "吸气时塌腰抬头，腹部向下沉（牛式）",
            "呼气时弓背低头，下巴找锁骨（猫式）",
            "缓慢交替，跟随呼吸流动"
        ),
        mistakes = listOf(
            "速度太快——跟随呼吸慢慢流动",
            "只动腰不动胸——让整条脊柱逐节活动",
            "耸肩——手臂推地，肩胛保持稳定"
        ),
        breathing = "塌腰时吸气，弓背时呼气"
    ),
    ExerciseInfo(
        name = "站立体前屈",
        illustration = "exercise",
        muscleGroups = listOf("腘绳肌", "下背", "小腿"),
        difficulty = "入门",
        benefits = listOf(
            "拉伸大腿后侧链条，摸不到脚尖的救星",
            "缓解久坐导致的下背紧绷",
            "动作简单，随时随地检测与改善柔韧性"
        ),
        steps = listOf(
            "站立双脚与髋同宽，膝盖可微屈",
            "呼气时以髋为折点，上身前倾下落",
            "双手自然下垂指向地面（或扶住小腿）",
            "保持 30 秒，每次呼气再放松下沉一点"
        ),
        mistakes = listOf(
            "直腿猛向下压——膝盖微屈保护下背",
            "借力弹震——静态保持即可",
            "憋气对抗——放松呼吸，柔韧来自放松"
        ),
        breathing = "每次呼气让身体再沉一点"
    ),
    ExerciseInfo(
        name = "婴儿式放松",
        illustration = "exercise",
        muscleGroups = listOf("背部", "肩部", "髋部"),
        difficulty = "入门",
        benefits = listOf(
            "训练后最好的放松姿势，腰背立即舒缓",
            "温和拉伸肩背与髋部，平静神经系统",
            "累了随时可以回到这个姿势休息"
        ),
        steps = listOf(
            "跪坐，大脚趾相触，双膝分开与髋同宽",
            "上身前俯，额头轻触垫面",
            "手臂前伸或放于体侧，肩膀放松下沉",
            "深呼吸，让下背随呼吸起伏，放松 30 秒以上"
        ),
        mistakes = listOf(
            "臀部离脚跟太高——可垫瑜伽砖或抱枕辅助",
            "肩膀紧张耸肩——肩膀远离耳朵",
            "呼吸短促——放慢呼吸，越慢越放松"
        ),
        breathing = "深长腹式呼吸，呼气时全身再松一层"
    ),
    ExerciseInfo(
        name = "颈肩拉伸",
        illustration = "exercise",
        muscleGroups = listOf("颈部", "斜方肌"),
        difficulty = "入门",
        benefits = listOf(
            "缓解低头族的肩颈酸痛，工作间隙就能做",
            "放松上斜方肌，改善肩颈紧张",
            "动作轻柔，安全系数高"
        ),
        steps = listOf(
            "坐直或站直，右手轻扶头部左侧",
            "呼气时头部缓慢向右侧倾斜，左肩主动下沉",
            "感受左侧颈肩的牵拉，保持 15~30 秒",
            "换边重复，动作始终轻柔"
        ),
        mistakes = listOf(
            "用手猛拉头部——只加轻微助力，拉伸靠自身重量",
            "拉伸侧耸肩——肩膀主动下沉效果才好",
            "动作过快过猛——颈椎娇贵，慢是前提"
        ),
        breathing = "缓慢呼吸，呼气时再倾斜多一点"
    )
)
