package com.yuedong.fitness.domain.logic

/**
 * 常见动作库（v2.2 采纳用户建议：不知道练什么也能选）。
 * v2.6 扩充至 101 个动作、11 个分类（新增 自重进阶/哑铃训练/杠铃力量/臀部专项），
 * 覆盖 居家徒手 + 健身房器械 + 小工具（哑铃/弹力带）全场景。
 * 每个动作自带推荐组数与次数/时长，选中后自动填入；仍可自由输入列表之外的名称。
 */
object CommonExercises {

    /** 单个常见动作：名称 + 推荐组数 + 推荐次数/时长。 */
    data class CommonExercise(val name: String, val sets: Int, val reps: String)

    /** 分类 -> 动作列表。 */
    val grouped: List<Pair<String, List<CommonExercise>>> = listOf(
        "热身" to listOf(
            CommonExercise("原地热身走", 1, "3分钟"),
            CommonExercise("肩部绕环", 2, "20次"),
            CommonExercise("手腕脚踝环绕", 2, "20次"),
            CommonExercise("髋部绕环", 2, "每侧10次"),
            CommonExercise("腰部绕环", 2, "每侧10次"),
            CommonExercise("弓步转体", 2, "每侧8次"),
            CommonExercise("前后摆腿", 2, "每侧15次"),
            CommonExercise("侧向摆腿", 2, "每侧12次"),
            CommonExercise("行走提踵", 1, "1分钟"),
            CommonExercise("踢腿走", 2, "每侧12次")
        ),
        "胸肩" to listOf(
            CommonExercise("俯卧撑", 3, "10次"),
            CommonExercise("跪姿俯卧撑", 3, "10次"),
            CommonExercise("阿诺德推举", 3, "12次"),
            CommonExercise("杠铃卧推", 4, "10次"),
            CommonExercise("上斜哑铃推举", 3, "12次"),
            CommonExercise("器械夹胸", 3, "12次"),
            CommonExercise("坐姿推肩", 3, "12次"),
            CommonExercise("哑铃侧平举", 3, "15次")
        ),
        "背臂" to listOf(
            CommonExercise("引体向上", 3, "8次"),
            CommonExercise("高位下拉", 4, "10次"),
            CommonExercise("反手高位下拉", 3, "10次"),
            CommonExercise("直臂下压", 3, "12次"),
            CommonExercise("坐姿划船", 3, "12次"),
            CommonExercise("蝴蝶机反飞", 3, "15次"),
            CommonExercise("杠铃弯举", 3, "12次"),
            CommonExercise("锤式弯举", 3, "12次"),
            CommonExercise("绳索下压", 3, "12次"),
            CommonExercise("哑铃颈后臂屈伸", 3, "10次")
        ),
        "腿臀" to listOf(
            CommonExercise("徒手深蹲", 3, "15次"),
            CommonExercise("弓步蹲", 3, "10次"),
            CommonExercise("史密斯深蹲", 4, "10次"),
            CommonExercise("腿举", 3, "12次"),
            CommonExercise("腿弯举", 3, "12次"),
            CommonExercise("腿屈伸", 3, "12次"),
            CommonExercise("臀桥", 3, "15次"),
            CommonExercise("站姿提踵", 3, "20次"),
            CommonExercise("坐姿提踵", 3, "15次")
        ),
        "臀部专项" to listOf(
            CommonExercise("单腿臀桥", 3, "每侧12次"),
            CommonExercise("蚌式开合", 3, "每侧20次"),
            CommonExercise("跪姿后踢腿", 3, "每侧15次"),
            CommonExercise("绳索后踢腿", 3, "每侧12次"),
            CommonExercise("髋外展机", 3, "15次"),
            CommonExercise("站姿侧抬腿", 3, "每侧20次")
        ),
        "核心" to listOf(
            CommonExercise("平板支撑", 3, "30秒"),
            CommonExercise("平板支撑开合跳", 3, "30秒"),
            CommonExercise("卷腹", 3, "15次"),
            CommonExercise("触踝卷腹", 3, "16次"),
            CommonExercise("仰卧起坐", 3, "12次"),
            CommonExercise("俄罗斯转体", 3, "20次"),
            CommonExercise("仰卧交替抬腿", 3, "12次"),
            CommonExercise("仰卧单车", 3, "20次"),
            CommonExercise("V字两头起", 3, "12次"),
            CommonExercise("死虫式", 3, "每侧10次"),
            CommonExercise("鸟狗式", 3, "每侧10次"),
            CommonExercise("超人式", 3, "12次"),
            CommonExercise("登山跑", 3, "30秒")
        ),
        "自重进阶" to listOf(
            CommonExercise("上斜俯卧撑", 3, "12次"),
            CommonExercise("宽距俯卧撑", 3, "10次"),
            CommonExercise("钻石俯卧撑", 3, "8次"),
            CommonExercise("击掌俯卧撑", 3, "6次"),
            CommonExercise("双杠臂屈伸", 3, "8次"),
            CommonExercise("引体缓降", 3, "5次"),
            CommonExercise("反手引体向上", 3, "8次"),
            CommonExercise("悬垂举腿", 3, "10次"),
            CommonExercise("单腿深蹲", 3, "每侧6次"),
            CommonExercise("保加利亚分腿蹲", 3, "每侧10次"),
            CommonExercise("深蹲跳", 3, "12次"),
            CommonExercise("靠墙倒立撑", 3, "5次"),
            CommonExercise("侧平板支撑", 3, "每侧30秒")
        ),
        "哑铃训练" to listOf(
            CommonExercise("哑铃卧推", 4, "10次"),
            CommonExercise("哑铃划船", 3, "10次"),
            CommonExercise("哑铃硬拉", 3, "10次"),
            CommonExercise("哑铃肩上推举", 3, "10次"),
            CommonExercise("高脚杯深蹲", 3, "12次"),
            CommonExercise("哑铃箭步蹲", 3, "每侧10次"),
            CommonExercise("俯身哑铃飞鸟", 3, "15次"),
            CommonExercise("哑铃前平举", 3, "12次"),
            CommonExercise("哑铃耸肩", 3, "15次"),
            CommonExercise("哑铃仰卧臂屈伸", 3, "10次"),
            CommonExercise("农夫行走", 3, "30秒")
        ),
        "杠铃力量" to listOf(
            CommonExercise("杠铃深蹲", 4, "8次"),
            CommonExercise("杠铃硬拉", 4, "8次"),
            CommonExercise("杠铃划船", 4, "10次"),
            CommonExercise("站姿杠铃推举", 4, "8次"),
            CommonExercise("上斜杠铃卧推", 3, "10次"),
            CommonExercise("杠铃臀桥", 4, "12次"),
            CommonExercise("杠铃耸肩", 3, "15次"),
            CommonExercise("杠铃早安式", 3, "10次")
        ),
        "有氧" to listOf(
            CommonExercise("开合跳", 3, "45秒"),
            CommonExercise("高抬腿", 3, "40秒"),
            CommonExercise("原地慢跑", 3, "2分钟"),
            CommonExercise("跳绳", 3, "1分钟"),
            CommonExercise("波比跳", 3, "10次"),
            CommonExercise("快走或慢跑", 1, "10分钟"),
            CommonExercise("椭圆机", 2, "10分钟"),
            CommonExercise("划船机", 2, "8分钟"),
            CommonExercise("动感单车", 2, "10分钟"),
            CommonExercise("游泳", 2, "15分钟")
        ),
        "拉伸放松" to listOf(
            CommonExercise("全身拉伸", 1, "5分钟"),
            CommonExercise("背阔肌拉伸", 1, "5分钟"),
            CommonExercise("猫式伸展", 2, "10次"),
            CommonExercise("站立体前屈", 2, "30秒"),
            CommonExercise("婴儿式放松", 2, "30秒"),
            CommonExercise("颈肩拉伸", 2, "30秒"),
            CommonExercise("大腿前侧拉伸", 2, "30秒"),
            CommonExercise("蝴蝶式拉伸", 2, "30秒"),
            CommonExercise("胸部拉伸", 2, "30秒"),
            CommonExercise("小腿拉伸", 2, "30秒"),
            CommonExercise("90/90髋部拉伸", 2, "30秒"),
            CommonExercise("站姿侧腰拉伸", 2, "30秒")
        )
    )

    /** 按名称查找推荐训练量。 */
    fun find(name: String): CommonExercise? = grouped.flatMap { it.second }.find { it.name == name }
}
