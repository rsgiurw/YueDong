package com.yuedong.fitness.domain.logic

/**
 * 动作教学数据·腿臀与核心（结构对齐食品库 FoodDataExtra 的拆分模式）。
 * 覆盖 CommonExercises「腿臀」「核心」分类中尚未收录教学的动作。
 */
internal val extraLegsCore: List<ExerciseInfo> = listOf(
    ExerciseInfo(
        name = "弓步蹲",
        illustration = "thigh",
        muscleGroups = listOf("股四头肌", "臀大肌", "腘绳肌"),
        difficulty = "入门",
        benefits = listOf(
            "单侧训练改善左右腿力量不均衡",
            "强化下肢的同时考验平衡，功能性拉满",
            "臀腿同步刺激，塑形效率高"
        ),
        steps = listOf(
            "站立后一脚向前迈出一大步，重心垂直下降",
            "前膝弯曲至约 90 度，后膝接近地面但不触地",
            "前脚全脚掌发力，后脚前脚掌点地支撑",
            "前腿蹬地站起收回，两腿交替进行"
        ),
        mistakes = listOf(
            "前膝超过脚尖过多——步幅加大，重心垂直向下坐",
            "身体前倾——上身保持直立，目视前方",
            "后膝砸地——缓慢下降，始终有控制"
        ),
        breathing = "下蹲时吸气，站起时呼气"
    ),
    ExerciseInfo(
        name = "史密斯深蹲",
        illustration = "squat",
        muscleGroups = listOf("股四头肌", "臀大肌"),
        difficulty = "初级",
        benefits = listOf(
            "杠铃轨迹固定，专注腿部发力不用操心平衡",
            "新手学习深蹲模式的最佳过渡，敢蹲得更深",
            "脚位可前后调整，针对性侧重腿或臀"
        ),
        steps = listOf(
            "杠铃置于斜方肌上部，双手握杆稳定，双脚出杠前方一步",
            "屈髋屈膝下蹲至大腿约与地面平行",
            "全脚掌踩实，膝盖始终对准脚尖方向",
            "蹬地站起，顶端不锁死膝盖"
        ),
        mistakes = listOf(
            "双脚正对杠铃正下方——脚要前移，动作更像\"往后坐\"",
            "膝盖内扣——始终对准脚尖方向",
            "蹲得太浅——至少到大腿平行才有完整刺激"
        ),
        breathing = "下蹲时吸气，站起时呼气"
    ),
    ExerciseInfo(
        name = "腿举",
        illustration = "thigh",
        muscleGroups = listOf("股四头肌", "臀大肌", "腘绳肌"),
        difficulty = "初级",
        benefits = listOf(
            "大重量安全练腿，腰部压力远小于自由深蹲",
            "脚位可调：低踩偏股四头肌，高踩偏臀与腘绳肌",
            "新手也能稳步上重量，建立腿部力量基础"
        ),
        steps = listOf(
            "背臀贴紧座椅，双脚与肩同宽踩在平台中部",
            "解锁安全销，屈膝下放平台至膝盖约 90 度或略深",
            "脚掌发力推回，接近伸直但不锁死膝盖",
            "全程腰部贴紧靠背，不拱起"
        ),
        mistakes = listOf(
            "膝盖完全锁死——保持微屈，张力不断",
            "下放过深导致臀部离椅——幅度以腰部不离靠背为准",
            "膝盖内扣——始终对准脚尖方向"
        ),
        breathing = "下放时吸气，蹬起时呼气"
    ),
    ExerciseInfo(
        name = "臀桥",
        illustration = "exercise",
        muscleGroups = listOf("臀大肌", "腘绳肌", "核心"),
        difficulty = "入门",
        benefits = listOf(
            "精准激活臀部，久坐扁臀的急救动作",
            "腰椎负担极小，腰部不适也能安心练臀",
            "强化伸髋力量，是跑跳与深蹲的基础模式"
        ),
        steps = listOf(
            "仰卧屈膝，双脚踩地与肩同宽，脚跟离臀约一拳半",
            "臀部发力将髋部向上顶起，肩胛贴地",
            "顶端身体呈一条斜线，夹紧臀部停顿一秒",
            "缓慢下放，腰部轻触垫面再进行下一次"
        ),
        mistakes = listOf(
            "用腰顶起（过度反弓）——顶端收臀收腹，肋骨下沉",
            "脚离臀部太远——腘绳肌代偿，脚跟再靠近臀部",
            "速度过快——慢起慢落，专注臀部收缩感"
        ),
        breathing = "顶起时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "站姿提踵",
        illustration = "exercise",
        muscleGroups = listOf("小腿三头肌"),
        difficulty = "入门",
        benefits = listOf(
            "孤立强化小腿肌，改善\"上半截粗\"的腿型",
            "提升踝关节稳定性，跑跳落地更有弹性",
            "器械或台阶边缘都能做，随处可练"
        ),
        steps = listOf(
            "前脚掌踩在踏板边缘，脚跟悬空，身体直立",
            "缓慢下沉脚跟至小腿有充分拉伸感",
            "脚跟发力踮起至最高点",
            "顶端停顿一秒，感受小腿收缩后缓慢下放"
        ),
        mistakes = listOf(
            "幅度太小——每次都做到最深拉伸与最高点",
            "屈膝借力——膝盖伸直锁定动作轨迹",
            "速度太快弹震——慢起慢落加顶端停顿"
        ),
        breathing = "踮起时呼气，下放时吸气"
    ),
    ExerciseInfo(
        name = "平板支撑",
        illustration = "exercise",
        muscleGroups = listOf("核心肌群", "肩带"),
        difficulty = "入门",
        benefits = listOf(
            "静态训练全面激活深层核心，保护腰椎",
            "改善久坐塌腰体态，站姿更挺拔",
            "零器械零空间，随时随地都能练"
        ),
        steps = listOf(
            "双肘撑地与肩同宽，双脚向后伸展脚尖点地",
            "头、肩、髋、踝呈一条直线，目视地面",
            "收腹夹臀，骨盆微微后倾（避免塌腰）",
            "均匀呼吸保持规定时长，质量优先于时长"
        ),
        mistakes = listOf(
            "塌腰——骨盆后倾，腹部持续收紧",
            "撅臀——重心略向前移到肩肘正上方",
            "憋气硬撑——保持节奏呼吸，撑不住就休息"
        ),
        breathing = "全程均匀呼吸，不要憋气"
    ),
    ExerciseInfo(
        name = "俄罗斯转体",
        illustration = "situp",
        muscleGroups = listOf("腹斜肌", "腹直肌", "髋屈肌"),
        difficulty = "入门",
        benefits = listOf(
            "雕塑侧腹线条，收紧腰围",
            "旋转发力是日常与运动的核心动作模式",
            "徒手或抱负重皆可，难度自由调节"
        ),
        steps = listOf(
            "坐姿屈膝，脚跟点地，上身微微后倾约 45 度",
            "双手合握于胸前，腹部绷紧维持平衡",
            "以腹斜肌发力带动躯干向一侧旋转",
            "左右交替，转动的是肩膀与躯干而非只摆手臂"
        ),
        mistakes = listOf(
            "只摆手臂不转躯干——肩膀要跟着转，感受侧腹收缩",
            "弓背圆肩——挺胸拔背，脊柱保持延展",
            "速度过快惯性甩——慢速控制，每次都转到位"
        ),
        breathing = "转体时呼气，回正时吸气"
    ),
    ExerciseInfo(
        name = "仰卧交替抬腿",
        illustration = "situp",
        muscleGroups = listOf("下腹部", "髂腰肌"),
        difficulty = "入门",
        benefits = listOf(
            "针对顽固的下腹部，收紧小肚子",
            "强化髋屈肌与核心抗伸展能力",
            "动作简单，躺下就能练"
        ),
        steps = listOf(
            "仰卧，双腿伸直，双手掌心朝下垫在臀部两侧",
            "下背压实地垫，双腿抬起至与地面约 45 度",
            "一条腿缓慢下放至接近地面，另一条保持不动",
            "左右交替上下，节奏平稳，全程腰部不离地"
        ),
        mistakes = listOf(
            "腰部弓起离地——下背压实垫子，抬腿角度可以高一些",
            "用惯性甩腿——每条腿都由腹部控制起落",
            "头颈用力抬起——头放平，注意力放在下腹"
        ),
        breathing = "保持自然节奏呼吸，下放时呼气"
    ),
    ExerciseInfo(
        name = "登山跑",
        illustration = "pushup",
        muscleGroups = listOf("核心", "肩带", "髋屈肌"),
        difficulty = "入门",
        benefits = listOf(
            "支撑位动态核心训练，腹肌与稳定一起练",
            "强度易调节，能快速拉高心率助燃脂",
            "顺带强化肩部支撑耐力"
        ),
        steps = listOf(
            "从俯撑姿势开始，手腕在肩正下方，身体呈一条直线",
            "一条腿屈膝向胸口提，膝盖朝向胸部",
            "两腿快速交替，像原地\"跑\"起来",
            "肩膀保持稳定不上下起伏，臀部不抬高"
        ),
        mistakes = listOf(
            "臀部翘高——身体压平，重心保持在肩正上方",
            "脚落地沉重跺地——前脚掌轻触",
            "塌腰——核心收紧，全程保持直线"
        ),
        breathing = "保持有节奏的呼吸，速度快时可两步一吸两步一呼"
    )
)
