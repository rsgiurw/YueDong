package com.yuedong.fitness.domain.logic

import com.yuedong.fitness.data.db.PlanDayWithExercises
import com.yuedong.fitness.data.db.entity.PlanDayEntity
import com.yuedong.fitness.data.db.entity.PlanExerciseEntity
import com.yuedong.fitness.data.db.entity.WorkoutPlanEntity

/**
 * 内置计划种子数据（模块契约见设计包 §8 Plan）。
 * 职责：提供 3 套真实可执行的中文训练计划内容；非职责：不做数据库写入（由 AppContainer 幂等执行）。
 * 训练节奏统一为 7 天循环：第 1/3/5 天训练、第 2/4/6/7 天休息，对应"每周 3 天"。
 */
object BuiltinPlanSeed {

    /** 一套待写入的内置计划（计划本体 + 每日安排与动作）。 */
    data class SeedEntry(val plan: WorkoutPlanEntity, val content: List<PlanDayWithExercises>)

    fun all(): List<SeedEntry> = listOf(
        homeFatLoss(), gymStrength(), homeHealth(),
        coreStrength14(), dumbbellUpper21(),
        jumpRopeHiit(), dumbbellFullBody(), peachGlutes(), deskReset()
    )

    fun allPlanCount(): Int = all().size

    private fun restDay(orderIndex: Int) = PlanDayWithExercises(
        day = PlanDayEntity(planId = 0, orderIndex = orderIndex, title = "休息", isRestDay = true),
        exercises = emptyList()
    )

    private data class Ex(val name: String, val sets: Int, val reps: String, val tip: String)

    private fun trainingDay(orderIndex: Int, title: String, exercises: List<Ex>) =
        PlanDayWithExercises(
            day = PlanDayEntity(planId = 0, orderIndex = orderIndex, title = title, isRestDay = false),
            exercises = exercises.map {
                PlanExerciseEntity(planDayId = 0, name = it.name, sets = it.sets, repsOrDuration = it.reps, tip = it.tip)
            }
        )

    // ---------- 1. 居家徒手·燃脂入门（减脂） ----------

    private fun homeFatLoss() = SeedEntry(
        plan = WorkoutPlanEntity(
            name = "居家徒手·燃脂入门",
            description = "无需器械，在家跟着节奏燃烧脂肪，适合新手的减脂起点",
            source = "BUILTIN",
            purposeTag = "FAT_LOSS",
            daysPerWeek = 3,
            cycleDays = 7
        ),
        content = listOf(
            trainingDay(
                0, "全身唤醒", listOf(
                    Ex("开合跳", 3, "45秒", "落地屈膝缓冲，保持轻盈节奏"),
                    Ex("徒手深蹲", 3, "15次", "膝盖对准脚尖方向，背部挺直"),
                    Ex("跪姿俯卧撑", 3, "10次", "核心收紧，身体从头到膝成一条直线"),
                    Ex("臀桥", 3, "12次", "顶起时夹紧臀部，停顿一秒再下落"),
                    Ex("平板支撑", 3, "30秒", "不要塌腰撅臀，均匀呼吸")
                )
            ),
            restDay(1),
            trainingDay(
                2, "有氧燃脂", listOf(
                    Ex("原地慢跑", 3, "2分钟", "前脚掌落地，保持轻松节奏"),
                    Ex("高抬腿", 3, "40秒", "膝盖抬至髋部高度，摆臂带动"),
                    Ex("登山跑", 3, "30秒", "肩膀保持在手腕正上方"),
                    Ex("开合跳", 3, "45秒", "节奏由慢到快，量力而行"),
                    Ex("拉伸放松", 1, "5分钟", "静态拉伸，每个部位保持15~30秒")
                )
            ),
            restDay(3),
            trainingDay(
                4, "核心塑形", listOf(
                    Ex("卷腹", 3, "15次", "下背贴地，用腹部发力而非颈部"),
                    Ex("俄罗斯转体", 3, "20次", "转动躯干，手臂只是跟随"),
                    Ex("仰卧交替抬腿", 3, "12次", "腰部贴地，防止弓背代偿"),
                    Ex("平板支撑", 3, "40秒", "感受腹部持续发力"),
                    Ex("臀桥", 3, "15次", "动作顶端不要过度顶腰")
                )
            ),
            restDay(5),
            restDay(6)
        )
    )

    // ---------- 4. 核心强化 14 天（核心·14 天周期 = 7 天模板两轮） ----------

    private fun coreStrength14() = SeedEntry(
        plan = WorkoutPlanEntity(
            name = "核心强化 14 天",
            description = "两周核心专项：深层稳定与腹肌线条并重，器械与徒手结合",
            source = "BUILTIN",
            purposeTag = "FAT_LOSS",
            daysPerWeek = 4,
            cycleDays = 14
        ),
        content = listOf(
            trainingDay(
                0, "核心唤醒", listOf(
                    Ex("死虫式", 3, "10次", "腰部贴地，对侧手脚同时伸展"),
                    Ex("卷腹", 3, "15次", "下背贴地，用腹部发力而非颈部"),
                    Ex("俄罗斯转体", 3, "20次", "转动躯干，手臂只是跟随"),
                    Ex("平板支撑", 3, "40秒", "不要塌腰撅臀，均匀呼吸")
                )
            ),
            trainingDay(
                1, "器械辅助", listOf(
                    Ex("蝴蝶机反飞", 3, "12次", "肘部微屈，用肩后束发力"),
                    Ex("直臂下压", 3, "12次", "手臂伸直画弧，肩胛下沉"),
                    Ex("反手高位下拉", 3, "10次", "反手握距，拉至胸口上方"),
                    Ex("平板支撑", 3, "45秒", "保持呼吸，不憋气")
                )
            ),
            restDay(2),
            trainingDay(
                3, "腹肌线条", listOf(
                    Ex("仰卧交替抬腿", 3, "12次", "腰部贴地，防止弓背代偿"),
                    Ex("卷腹", 3, "20次", "动作顶端呼气收紧"),
                    Ex("俄罗斯转体", 3, "24次", "双脚离地增加难度"),
                    Ex("臀桥", 3, "15次", "顶起时夹紧臀部")
                )
            ),
            restDay(4),
            trainingDay(
                5, "全身强化", listOf(
                    Ex("阿诺德推举", 3, "10次", "旋转手腕，前中束兼顾"),
                    Ex("坐姿提踵", 3, "15次", "顶端停顿一秒"),
                    Ex("腿屈伸", 3, "12次", "膝盖对准方向，顶端收紧"),
                    Ex("平板支撑", 3, "50秒", "分化注意力，保持稳定呼吸")
                )
            ),
            restDay(6),
            restDay(7),
            trainingDay(
                8, "核心循环", listOf(
                    Ex("卷腹", 3, "15次", "节奏均匀不借力"),
                    Ex("仰卧交替抬腿", 3, "12次", "腰部始终贴地"),
                    Ex("俄罗斯转体", 3, "20次", "转体由腹斜肌主导"),
                    Ex("臀桥", 3, "15次", "感受臀部而非腰部")
                )
            ),
            trainingDay(
                9, "上肢收尾", listOf(
                    Ex("蝴蝶机反飞", 3, "12次", "肘部微屈固定角度"),
                    Ex("直臂下压", 3, "12次", "肩胛下沉再启动"),
                    Ex("反手高位下拉", 3, "10次", "反手握距拉至锁骨"),
                    Ex("平板支撑", 3, "60秒", "最后冲刺，坚持到底")
                )
            ),
            restDay(10),
            restDay(11),
            trainingDay(
                12, "下肢稳定", listOf(
                    Ex("腿屈伸", 3, "12次", "顶端停顿一秒再下落"),
                    Ex("腿弯举", 3, "12次", "控制离心不甩腿"),
                    Ex("坐姿提踵", 3, "15次", "全程幅度完整"),
                    Ex("死虫式", 3, "10次", "对侧伸展保持骨盆稳定")
                )
            ),
            trainingDay(
                13, "综合收尾", listOf(
                    Ex("阿诺德推举", 3, "10次", "手腕旋转完整"),
                    Ex("卷腹", 3, "20次", "两周成果检验"),
                    Ex("平板支撑", 3, "60秒", "比第一周更强了"),
                    Ex("拉伸放松", 1, "5分钟", " static 拉伸放松全身")
                )
            )
        )
    )

    // ---------- 5. 上肢塑形 21 天（哑铃·21 天周期 = 7 天模板三轮） ----------

    private fun dumbbellUpper21() = SeedEntry(
        plan = WorkoutPlanEntity(
            name = "上肢塑形 21 天",
            description = "三周哑铃上肢计划：肩/背/手臂轮流雕刻，居家哑铃即可完成",
            source = "BUILTIN",
            purposeTag = "MUSCLE_GAIN",
            daysPerWeek = 4,
            cycleDays = 21
        ),
        content = listOf(
            trainingDay(
                0, "肩部塑形", listOf(
                    Ex("阿诺德推举", 4, "10次", "旋转手腕让前中束都受力"),
                    Ex("侧平举", 3, "12次", "小臂微屈，抬至肩平"),
                    Ex("俯身飞鸟", 3, "12次", "俯身 45 度，肩后束发力")
                )
            ),
            trainingDay(
                1, "背部厚度", listOf(
                    Ex("反手高位下拉", 4, "10次", "反手握距拉至锁骨"),
                    Ex("直臂下压", 3, "12次", "肩胛下沉，手臂画弧"),
                    Ex("蝴蝶机反飞", 3, "12次", "后束孤立训练")
                )
            ),
            restDay(2),
            trainingDay(
                3, "手臂雕刻", listOf(
                    Ex("哑铃弯举", 4, "12次", "大臂固定不晃动"),
                    Ex("锤式弯举", 3, "12次", "中立握距刺激肱肌"),
                    Ex("俯身臂屈伸", 3, "12次", "大臂平行地面")
                )
            ),
            restDay(4),
            trainingDay(
                5, "下肢收尾", listOf(
                    Ex("坐姿提踵", 4, "15次", "全程幅度完整"),
                    Ex("腿弯举", 3, "12次", "离心控制缓慢下落"),
                    Ex("腿屈伸", 3, "12次", "顶端收缩一秒")
                )
            ),
            restDay(6),
            restDay(7),
            trainingDay(
                8, "肩部进阶", listOf(
                    Ex("阿诺德推举", 4, "12次", "第二周加量"),
                    Ex("侧平举", 4, "12次", "顶峰停顿一秒"),
                    Ex("俯身飞鸟", 3, "15次", "肩后束持续紧张")
                )
            ),
            trainingDay(
                9, "背部加宽", listOf(
                    Ex("反手高位下拉", 4, "12次", "肩胛先动再屈臂"),
                    Ex("直臂下压", 4, "12次", "全程不弓腰"),
                    Ex("蝴蝶机反飞", 3, "15次", "挤压肩后束")
                )
            ),
            restDay(10),
            trainingDay(
                11, "手臂冲刺", listOf(
                    Ex("哑铃弯举", 4, "15次", "顶端挤压二头"),
                    Ex("锤式弯举", 4, "12次", "肱肌与前臂兼顾"),
                    Ex("俯身臂屈伸", 4, "12次", "三头持续紧张")
                )
            ),
            restDay(12),
            trainingDay(
                13, "下肢强化", listOf(
                    Ex("坐姿提踵", 4, "15次", "最后阶段不松懈"),
                    Ex("腿弯举", 4, "12次", "腘绳肌充分收缩"),
                    Ex("腿屈伸", 4, "12次", "股四头精准发力")
                )
            ),
            restDay(14),
            restDay(15),
            trainingDay(
                16, "肩部收官", listOf(
                    Ex("阿诺德推举", 4, "12次", "三周成果检验"),
                    Ex("侧平举", 4, "15次", "肩中束线条明显"),
                    Ex("俯身飞鸟", 3, "15次", "后束不借力")
                )
            ),
            trainingDay(
                17, "背部收官", listOf(
                    Ex("反手高位下拉", 4, "12次", "背阔肌充分挤压"),
                    Ex("直臂下压", 4, "12次", "全程背阔肌紧张"),
                    Ex("蝴蝶机反飞", 3, "15次", "收尾不借力")
                )
            ),
            restDay(18),
            trainingDay(
                19, "手臂收官", listOf(
                    Ex("哑铃弯举", 4, "15次", "二头峰值收缩"),
                    Ex("锤式弯举", 4, "12次", "前臂稳定"),
                    Ex("俯身臂屈伸", 4, "12次", "三头完整收缩")
                )
            ),
            restDay(20)
        )
    )


    // ---------- 2. 健身房·力量入门（增肌） ----------

    private fun gymStrength() = SeedEntry(
        plan = WorkoutPlanEntity(
            name = "健身房·力量入门",
            description = "经典三分化器械训练，胸肩背腿轮流练，打下力量基础",
            source = "BUILTIN",
            purposeTag = "MUSCLE_GAIN",
            daysPerWeek = 3,
            cycleDays = 14
        ),
        content = listOf(
            trainingDay(
                0, "胸+三头", listOf(
                    Ex("杠铃卧推", 4, "10次", "肩胛收紧，杠铃轻触胸口即推起"),
                    Ex("上斜哑铃推举", 3, "12次", "凳面调至30~45度，感受上胸发力"),
                    Ex("器械夹胸", 3, "12次", "肘部微屈，想象抱住一棵大树"),
                    Ex("绳索下压", 3, "12次", "大臂夹紧身体，只动小臂"),
                    Ex("哑铃颈后臂屈伸", 3, "10次", "肘部保持稳定，缓慢下放")
                )
            ),
            restDay(1),
            trainingDay(
                2, "背+二头", listOf(
                    Ex("高位下拉", 4, "10次", "挺胸沉肩，用背部发力而非手臂"),
                    Ex("坐姿划船", 3, "12次", "拉至腹部，肩胛骨向中间收拢"),
                    Ex("杠铃弯举", 3, "12次", "肘部固定在身体两侧，不晃动借力"),
                    Ex("锤式弯举", 3, "12次", "掌心相对，重点锻炼肱肌"),
                    Ex("背阔肌拉伸", 1, "5分钟", "训练后放松，缓解背部紧张")
                )
            ),
            restDay(3),
            trainingDay(
                4, "腿+肩", listOf(
                    Ex("史密斯深蹲", 4, "10次", "下蹲至大腿约与地面平行"),
                    Ex("腿举", 3, "12次", "膝盖不要完全锁死，保持张力"),
                    Ex("坐姿推肩", 3, "12次", "核心收紧，腰部不要反弓"),
                    Ex("哑铃侧平举", 3, "15次", "抬至与肩同高，小拇指略微向上"),
                    Ex("站姿提踵", 3, "20次", "顶端停顿一秒，充分收缩小腿")
                )
            ),
            restDay(5),
            restDay(6)
        )
    )

    // ---------- 3. 居家·健康活力（健康/塑形共用） ----------

    private fun homeHealth() = SeedEntry(
        plan = WorkoutPlanEntity(
            name = "居家·健康活力",
            description = "拉伸、基础力量与轻有氧的温柔组合，保持活力、改善体质",
            source = "BUILTIN",
            purposeTag = "HEALTH",
            daysPerWeek = 3,
            cycleDays = 7
        ),
        content = listOf(
            trainingDay(
                0, "拉伸激活", listOf(
                    Ex("原地热身走", 1, "3分钟", "摆臂自然，让身体微微发热"),
                    Ex("颈肩拉伸", 2, "30秒", "动作轻柔，不要猛拉"),
                    Ex("猫式伸展", 2, "10次", "配合呼吸，弓背与塌腰交替"),
                    Ex("站立体前屈", 2, "30秒", "膝盖可微屈，感受大腿后侧"),
                    Ex("婴儿式放松", 2, "30秒", "深呼吸，放松肩背")
                )
            ),
            restDay(1),
            trainingDay(
                2, "基础力量", listOf(
                    Ex("徒手深蹲", 3, "12次", "起立时呼气，动作慢而稳"),
                    Ex("跪姿俯卧撑", 3, "8次", "手肘与身体约呈45度"),
                    Ex("弓步蹲", 3, "10次", "每侧各10次，前膝不超过脚尖"),
                    Ex("臀桥", 3, "12次", "感受臀部发力，而不是腰部"),
                    Ex("平板支撑", 2, "20秒", "质量优先，不追时长")
                )
            ),
            restDay(3),
            trainingDay(
                4, "有氧+核心", listOf(
                    Ex("快走或慢跑", 1, "10分钟", "能正常聊天的强度刚好"),
                    Ex("开合跳", 2, "30秒", "轻松节奏即可"),
                    Ex("卷腹", 2, "12次", "动作幅度小没关系，感受腹部"),
                    Ex("仰卧交替抬腿", 2, "10次", "缓慢交替，腰部贴地"),
                    Ex("全身拉伸", 1, "5分钟", "练后拉伸，第二天不那么酸")
                )
            ),
            restDay(5),
            restDay(6)
        )
    )
    // ---------- 6. 跳绳间歇·极速燃脂（减脂） ----------
    private fun jumpRopeHiit() = SeedEntry(
        plan = WorkoutPlanEntity(
            name = "跳绳间歇·极速燃脂",
            description = "跳绳与徒手动作交替的间歇训练，短时间高热量消耗",
            source = "BUILTIN",
            purposeTag = "FAT_LOSS",
            daysPerWeek = 4,
            cycleDays = 7
        ),
        content = listOf(
            trainingDay(0, "间歇燃脂 A", listOf(
                Ex("开合跳", 3, "45秒", "落地屈膝缓冲，保持轻盈节奏"),
                Ex("高抬腿", 3, "40秒", "膝盖抬至髋部高度，核心收紧"),
                Ex("波比跳", 3, "10次", "落地时屈膝保护膝盖"),
                Ex("登山跑", 3, "30秒", "肩在腕正上方，臀部不塌")
            )),
            restDay(1),
            trainingDay(2, "间歇燃脂 B", listOf(
                Ex("徒手深蹲", 3, "15次", "膝盖对准脚尖方向，背部挺直"),
                Ex("卷腹", 3, "15次", "腹部发力，将上背部抬离垫面约 30 度"),
                Ex("高抬腿", 3, "40秒", "用前脚掌落地，保持节奏"),
                Ex("平板支撑", 3, "40秒", "身体成一条直线，不塌腰不撅臀")
            )),
            restDay(3),
            trainingDay(4, "全身循环", listOf(
                Ex("波比跳", 3, "12次", "动作质量优先，累了就放慢"),
                Ex("徒手深蹲", 3, "20次", "大腿后侧发力起身"),
                Ex("开合跳", 3, "45秒", "双手头顶击掌"),
                Ex("卷腹", 3, "20次", "呼气时卷起，吸气落下")
            )),
            restDay(5),
            restDay(6)
        )
    )

    // ---------- 7. 哑铃全身·肌力进阶（增肌） ----------
    private fun dumbbellFullBody() = SeedEntry(
        plan = WorkoutPlanEntity(
            name = "哑铃全身·肌力进阶",
            description = "一对哑铃练遍全身，适合家庭的增肌进阶方案",
            source = "BUILTIN",
            purposeTag = "MUSCLE_GAIN",
            daysPerWeek = 3,
            cycleDays = 7
        ),
        content = listOf(
            trainingDay(0, "推力日", listOf(
                Ex("哑铃肩推", 4, "10次", "手腕保持中立，不塌腰"),
                Ex("俯卧撑", 4, "12次", "肩胛不耸起，胸部贴近地面"),
                Ex("哑铃卧推", 4, "10次", "下放到大臂与地面平行即可")
            )),
            trainingDay(1, "拉力日", listOf(
                Ex("哑铃划船", 4, "10次", "背部发力，肘部贴近身体"),
                Ex("哑铃硬拉", 4, "10次", "背部平直，臀向后坐"),
                Ex("卷腹", 3, "20次", "下巴微收，颈部放松")
            )),
            restDay(2),
            trainingDay(3, "腿部日", listOf(
                Ex("徒手深蹲", 4, "15次", "下蹲到大腿与地面平行"),
                Ex("弓步蹲", 3, "12次", "前膝不超过脚尖太多"),
                Ex("臀桥", 3, "15次", "顶峰夹臀 1 秒再下落")
            )),
            restDay(4),
            restDay(5),
            trainingDay(6, "全身循环", listOf(
                Ex("哑铃肩推", 3, "12次", "选择可完成全部组数的重量"),
                Ex("哑铃划船", 3, "12次", "感受背阔肌收缩"),
                Ex("徒手深蹲", 3, "15次", "节奏放慢，底部停顿 1 秒")
            ))
        )
    )

    // ---------- 8. 蜜桃臀腿·曲线雕刻（塑形） ----------
    private fun peachGlutes() = SeedEntry(
        plan = WorkoutPlanEntity(
            name = "蜜桃臀腿·曲线雕刻",
            description = "以臀腿为主的塑形计划，居家即可完成",
            source = "BUILTIN",
            purposeTag = "TONE",
            daysPerWeek = 3,
            cycleDays = 7
        ),
        content = listOf(
            trainingDay(0, "臀腿激活", listOf(
                Ex("徒手深蹲", 4, "15次", "脚尖略外展，感受臀部发力"),
                Ex("臀桥", 4, "15次", "顶峰夹臀 1 秒"),
                Ex("弓步蹲", 3, "12次", "重心放在前脚跟")
            )),
            restDay(1),
            trainingDay(2, "臀腿强化", listOf(
                Ex("弓步蹲", 4, "12次", "上身保持直立"),
                Ex("徒手深蹲", 4, "20次", "蹲到底部停顿 1 秒"),
                Ex("平板支撑", 3, "40秒", "核心全程收紧")
            )),
            restDay(3),
            trainingDay(4, "臀腿收尾", listOf(
                Ex("臀桥", 4, "20次", "顶峰挤压臀部 2 秒"),
                Ex("卷腹", 3, "15次", "腹部持续张力"),
                Ex("猫式伸展", 2, "30秒", "配合呼吸放松腰背")
            )),
            restDay(5),
            restDay(6)
        )
    )

    // ---------- 9. 办公室久坐·唤醒放松（健康） ----------
    private fun deskReset() = SeedEntry(
        plan = WorkoutPlanEntity(
            name = "办公室久坐·唤醒放松",
            description = "针对久坐人群的低强度放松计划，缓解颈肩腰背紧张",
            source = "BUILTIN",
            purposeTag = "HEALTH",
            daysPerWeek = 2,
            cycleDays = 7
        ),
        content = listOf(
            trainingDay(0, "颈肩唤醒", listOf(
                Ex("原地热身走", 1, "3分钟", "自然摆臂，逐渐加快"),
                Ex("颈肩拉伸", 3, "30秒", "动作缓慢，不要耸肩"),
                Ex("猫式伸展", 2, "30秒", "配合呼吸，弓背与塌腰交替")
            )),
            restDay(1),
            trainingDay(2, "腰背放松", listOf(
                Ex("站立体前屈", 3, "30秒", "膝盖微屈，感受大腿后侧"),
                Ex("婴儿式放松", 2, "40秒", "深呼吸，放松肩背"),
                Ex("颈肩拉伸", 2, "30秒", "左右各保持 30 秒")
            )),
            restDay(3),
            restDay(4),
            trainingDay(5, "全身舒展", listOf(
                Ex("原地热身走", 1, "5分钟", "微微出汗即可"),
                Ex("猫式伸展", 3, "30秒", "动作缓慢柔和"),
                Ex("站立体前屈", 2, "30秒", "不要憋气")
            )),
            restDay(6)
        )
    )
}
