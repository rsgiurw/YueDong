package com.yuedong.fitness.domain.logic

/**
 * 动作教学数据·热身扩充（v2.6）。
 * 覆盖 CommonExercises「热身」分类新增的 9 个动态热身动作：
 * 关节绕环 + 动态拉伸 + 步态激活，正式训练前按顺序完成即可充分热身。
 */
internal val extraWarmup: List<ExerciseInfo> = listOf(
    ExerciseInfo(
        name = "肩部绕环",
        illustration = "exercise",
        muscleGroups = listOf("肩袖肌群", "三角肌"),
        difficulty = "入门",
        benefits = listOf(
            "唤醒肩袖肌群，推举卧推前必做的护肩热身",
            "增加肩关节滑液分泌，减少训练中肩部弹响",
            "站位即可完成，任何训练日都适用"
        ),
        steps = listOf(
            "站立，双脚与肩同宽，双臂自然下垂",
            "双臂伸直向前画大圆绕环 10 次",
            "再向后画大圆绕环 10 次",
            "逐渐加大绕环幅度，直到肩部发热"
        ),
        mistakes = listOf(
            "绕环幅度太小——手臂尽量画最大的圆",
            "速度太快甩肩——匀速有控制地绕",
            "耸肩——肩膀保持下沉"
        ),
        breathing = "自然呼吸，保持节奏"
    ),
    ExerciseInfo(
        name = "手腕脚踝环绕",
        illustration = "exercise",
        muscleGroups = listOf("腕关节", "踝关节"),
        difficulty = "入门",
        benefits = listOf(
            "激活腕踝小关节，俯卧撑与跑跳前必做",
            "预防手腕承重训练与跑跳中的崴脚扭伤",
            "动作轻柔，随时随地都能完成"
        ),
        steps = listOf(
            "十指交叉，手腕顺时针环绕 10 圈",
            "换逆时针环绕 10 圈",
            "脚尖点地，脚踝顺时针绕环 10 圈",
            "换逆时针绕环 10 圈，双脚轮流完成"
        ),
        mistakes = listOf(
            "只绕一个方向——顺逆时针都要做",
            "幅度太小——尽量画完整的圆",
            "速度太快——缓慢感受关节活动"
        ),
        breathing = "自然呼吸即可"
    ),
    ExerciseInfo(
        name = "髋部绕环",
        illustration = "exercise",
        muscleGroups = listOf("髋关节", "臀部"),
        difficulty = "入门",
        benefits = listOf(
            "激活髋关节，深蹲硬拉前的必备热身",
            "改善久坐导致的髋部僵硬",
            "增加髋部滑液分泌，下蹲更顺畅"
        ),
        steps = listOf(
            "双脚略宽于肩，双手叉腰",
            "髋部按顺时针方向画大圆绕环 10 圈",
            "换逆时针方向绕环 10 圈",
            "幅度逐渐加大，感受髋关节充分活动"
        ),
        mistakes = listOf(
            "只动腰不动髋——用髋关节带动画圆",
            "膝盖跟着大幅转动——上半身保持相对稳定",
            "幅度太小——画最大的圆"
        ),
        breathing = "自然呼吸，保持节奏"
    ),
    ExerciseInfo(
        name = "腰部绕环",
        illustration = "exercise",
        muscleGroups = listOf("腰椎", "腰方肌"),
        difficulty = "入门",
        benefits = listOf(
            "温和活动腰椎，久坐腰僵的快速缓解",
            "训练前激活腰部肌群，降低弯腰受伤风险",
            "站立即可完成，动作轻柔安全"
        ),
        steps = listOf(
            "双脚与肩同宽，双手叉腰，拇指朝前",
            "腰部按顺时针方向画圆绕环 10 圈",
            "换逆时针方向绕环 10 圈",
            "保持骨盆以下稳定，只活动腰部"
        ),
        mistakes = listOf(
            "整个身体一起晃——下半身稳住，只动腰",
            "绕环速度过快——缓慢画圆",
            "幅度过大猛甩——循序渐进加大幅度"
        ),
        breathing = "自然呼吸，不要憋气"
    ),
    ExerciseInfo(
        name = "弓步转体",
        illustration = "exercise",
        muscleGroups = listOf("髋屈肌", "胸椎", "核心"),
        difficulty = "入门",
        benefits = listOf(
            "被称为世界最棒拉伸，一个动作热身全身",
            "同时拉伸髋屈肌并旋转胸椎，跑跳前必做",
            "徒手完成，为深蹲硬拉做准备尤其合适"
        ),
        steps = listOf(
            "弓步跪姿，前膝弯曲 90 度，后膝轻触地面",
            "前侧同侧手掌撑地保持平衡",
            "另一侧手臂向上打开，眼睛跟随手指转动胸椎",
            "回到原位，每侧重复 8 次"
        ),
        mistakes = listOf(
            "塌腰驼背——脊柱保持延展",
            "转身幅度不够——眼睛看向手，胸椎尽量打开",
            "前膝超过脚尖——调整步幅，膝盖对准脚尖"
        ),
        breathing = "打开时吸气，回正时呼气"
    ),
    ExerciseInfo(
        name = "前后摆腿",
        illustration = "exercise",
        muscleGroups = listOf("腘绳肌", "髋屈肌"),
        difficulty = "入门",
        benefits = listOf(
            "动态拉伸大腿后侧与髋屈肌，跑步前必做",
            "比静态拉伸更适合热身，不降低爆发力",
            "改善摆腿幅度，步幅更舒展"
        ),
        steps = listOf(
            "站立，一手扶墙或扶稳支撑物",
            "单腿伸直前后摆动，摆到最大可控幅度",
            "保持上身挺直，骨盆中立",
            "连续摆 15 次后换腿"
        ),
        mistakes = listOf(
            "弯腰驼背——上身挺直，只动腿",
            "用力过猛弹震——幅度逐渐加大",
            "支撑侧膝盖乱晃——支撑腿保持稳定"
        ),
        breathing = "摆动配合呼吸，保持节奏"
    ),
    ExerciseInfo(
        name = "侧向摆腿",
        illustration = "exercise",
        muscleGroups = listOf("大腿内侧", "臀中肌"),
        difficulty = "入门",
        benefits = listOf(
            "动态拉伸大腿内外侧，侧向运动前必做",
            "激活臀中肌，提升侧向稳定性",
            "改善髋部内收肌紧张"
        ),
        steps = listOf(
            "站立，一手扶墙，身体正直",
            "一条腿伸直向身体外侧摆起，再摆回跨过身体中线",
            "保持骨盆朝前，只有腿在侧向摆动",
            "连续摆 12 次后换腿"
        ),
        mistakes = listOf(
            "上身侧倾借力——躯干保持直立",
            "腿屈膝抬起——伸直摆动效果更好",
            "脚尖朝地内扣——脚尖微朝上"
        ),
        breathing = "摆动配合呼吸，保持节奏"
    ),
    ExerciseInfo(
        name = "行走提踵",
        illustration = "exercise",
        muscleGroups = listOf("小腿三头肌", "踝关节"),
        difficulty = "入门",
        benefits = listOf(
            "边走边踮脚，激活小腿与踝关节",
            "强化脚踝稳定性，跑跳落地更有弹性",
            "行走中完成，简单不易忘"
        ),
        steps = listOf(
            "站立挺胸，双脚并拢",
            "脚跟抬起用前脚掌站立，走一小步",
            "落步后再次提踵，向前行走",
            "保持 1 分钟，脚跟尽量高"
        ),
        mistakes = listOf(
            "脚跟落地过重——轻柔过渡",
            "身体前后晃——核心收紧，目视前方",
            "提踵幅度不足——每次都踮到最高"
        ),
        breathing = "自然呼吸，保持节奏"
    ),
    ExerciseInfo(
        name = "踢腿走",
        illustration = "exercise",
        muscleGroups = listOf("腘绳肌", "髋屈肌"),
        difficulty = "入门",
        benefits = listOf(
            "直腿行走摆踢，动态拉伸大腿后侧",
            "提升腘绳肌柔韧性，预防跑跳拉伤",
            "行走节奏简单，热身尾段最佳"
        ),
        steps = listOf(
            "站立挺胸，双臂侧平举保持平衡",
            "一条腿伸直向前上方踢起，尽量触碰对侧手掌",
            "踢腿腿落地，换另一条腿踢",
            "像行军一样交替前行，各踢 12 次"
        ),
        mistakes = listOf(
            "膝盖弯曲——踢腿腿保持伸直",
            "上身后仰过度——核心收紧，微微前倾即可",
            "用手掌去找腿——手掌固定，腿来碰手"
        ),
        breathing = "每踢一次呼气一次，节奏均匀"
    )
)
