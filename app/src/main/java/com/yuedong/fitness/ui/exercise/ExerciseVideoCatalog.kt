package com.yuedong.fitness.ui.exercise

import com.yuedong.fitness.R
import com.yuedong.fitness.domain.logic.CommonExercises

/**
 * 动作视频目录（由 import_videos.py 自动生成，勿手改；补视频后重跑该脚本即可更新）。
 * 共 110 个动作，已接入视频 103 个，其余待视频交付。
 * 缩略图由 extract_thumbs.py 从视频末帧生成，与视频同名（R.drawable.ex_xxx 对应 R.raw.ex_xxx.mp4）。
 * 分类与推荐训练量统一复用 CommonExercises，避免两份数据漂移。
 */
object ExerciseVideoCatalog {

    private val resByName: Map<String, Int> = mapOf(
        // 热身
        "原地热身走" to R.raw.ex_warmup_walk,
        "肩部绕环" to R.raw.ex_shoulder_circle,
        "手腕脚踝环绕" to R.raw.ex_wrist_ankle_circle,
        "髋部绕环" to R.raw.ex_hip_circle,
        "腰部绕环" to R.raw.ex_waist_circle,
        "弓步转体" to R.raw.ex_lunge_twist,
        "前后摆腿" to R.raw.ex_leg_swing_front,
        "侧向摆腿" to R.raw.ex_leg_swing_side,
        "行走提踵" to R.raw.ex_walking_calf_raise,
        "踢腿走" to R.raw.ex_leg_kick_walk,
        // 胸肩
        "俯卧撑" to R.raw.ex_pushup,
        "跪姿俯卧撑" to R.raw.ex_kneeling_pushup,
        "杠铃卧推" to R.raw.ex_barbell_bench,
        "上斜哑铃推举" to R.raw.ex_incline_db_press,
        "器械夹胸" to R.raw.ex_pec_deck,
        "坐姿推肩" to R.raw.ex_shoulder_press,
        "哑铃侧平举" to R.raw.ex_lateral_raise,
        // 背臂
        "引体向上" to R.raw.ex_pullup,
        "高位下拉" to R.raw.ex_lat_pulldown,
        "坐姿划船" to R.raw.ex_seated_row,
        "杠铃弯举" to R.raw.ex_barbell_curl,
        "锤式弯举" to R.raw.ex_hammer_curl,
        "绳索下压" to R.raw.ex_cable_pushdown,
        "哑铃颈后臂屈伸" to R.raw.ex_overhead_ext,
        // 腿臀
        "徒手深蹲" to R.raw.ex_squat,
        "弓步蹲" to R.raw.ex_lunge,
        "史密斯深蹲" to R.raw.ex_smith_squat,
        "腿举" to R.raw.ex_leg_press,
        "臀桥" to R.raw.ex_glute_bridge,
        "站姿提踵" to R.raw.ex_calf_raise,
        // 臀部专项
        "单腿臀桥" to R.raw.ex_single_leg_glute_bridge,
        "蚌式开合" to R.raw.ex_clamshell,
        "跪姿后踢腿" to R.raw.ex_glute_kickback,
        "绳索后踢腿" to R.raw.ex_cable_kickback,
        "髋外展机" to R.raw.ex_hip_abduction,
        "站姿侧抬腿" to R.raw.ex_standing_side_leg_raise,
        // 核心
        "平板支撑" to R.raw.ex_plank,
        "平板支撑开合跳" to R.raw.ex_plank_jack,
        "卷腹" to R.raw.ex_crunch,
        "触踝卷腹" to R.raw.ex_heel_touch,
        "仰卧起坐" to R.raw.ex_situp,
        "俄罗斯转体" to R.raw.ex_russian_twist,
        "仰卧交替抬腿" to R.raw.ex_lying_leg_raise,
        "仰卧单车" to R.raw.ex_bicycle_crunch,
        "V字两头起" to R.raw.ex_v_up,
        "死虫式" to R.raw.ex_dead_bug,
        "鸟狗式" to R.raw.ex_bird_dog,
        "超人式" to R.raw.ex_superman,
        "登山跑" to R.raw.ex_mountain_climber,
        // 自重进阶
        "上斜俯卧撑" to R.raw.ex_incline_pushup,
        "宽距俯卧撑" to R.raw.ex_wide_pushup,
        "钻石俯卧撑" to R.raw.ex_diamond_pushup,
        "击掌俯卧撑" to R.raw.ex_clap_pushup,
        "双杠臂屈伸" to R.raw.ex_dip,
        "引体缓降" to R.raw.ex_negative_pullup,
        "反手引体向上" to R.raw.ex_chinup,
        "悬垂举腿" to R.raw.ex_hanging_leg_raise,
        "单腿深蹲" to R.raw.ex_pistol_squat,
        "保加利亚分腿蹲" to R.raw.ex_bulgarian_split_squat,
        "深蹲跳" to R.raw.ex_jump_squat,
        "靠墙倒立撑" to R.raw.ex_wall_handstand_pushup,
        "侧平板支撑" to R.raw.ex_side_plank,
        // 哑铃训练
        "哑铃卧推" to R.raw.ex_db_bench,
        "哑铃划船" to R.raw.ex_db_row,
        "哑铃硬拉" to R.raw.ex_db_rdl,
        "哑铃肩上推举" to R.raw.ex_db_shoulder_press,
        "高脚杯深蹲" to R.raw.ex_goblet_squat,
        "哑铃箭步蹲" to R.raw.ex_db_lunge,
        "俯身哑铃飞鸟" to R.raw.ex_reverse_fly,
        "哑铃前平举" to R.raw.ex_front_raise,
        "哑铃耸肩" to R.raw.ex_db_shrug,
        "哑铃仰卧臂屈伸" to R.raw.ex_db_skullcrusher,
        "农夫行走" to R.raw.ex_farmer_walk,
        // 杠铃力量
        "杠铃深蹲" to R.raw.ex_barbell_squat,
        "杠铃硬拉" to R.raw.ex_barbell_deadlift,
        "杠铃划船" to R.raw.ex_barbell_row,
        "站姿杠铃推举" to R.raw.ex_ohp,
        "上斜杠铃卧推" to R.raw.ex_incline_barbell_bench,
        "杠铃臀桥" to R.raw.ex_barbell_hip_thrust,
        "杠铃耸肩" to R.raw.ex_barbell_shrug,
        "杠铃早安式" to R.raw.ex_barbell_good_morning,
        // 有氧
        "开合跳" to R.raw.ex_jumping_jack,
        "高抬腿" to R.raw.ex_high_knees,
        "原地慢跑" to R.raw.ex_jog,
        "跳绳" to R.raw.ex_jump_rope,
        "波比跳" to R.raw.ex_burpee,
        "快走或慢跑" to R.raw.ex_brisk_walk,
        "椭圆机" to R.raw.ex_elliptical,
        "划船机" to R.raw.ex_rowing_machine,
        "动感单车" to R.raw.ex_spin_bike,
        "游泳" to R.raw.ex_swimming,
        // 拉伸放松
        "全身拉伸" to R.raw.ex_full_stretch,
        "背阔肌拉伸" to R.raw.ex_lat_stretch,
        "猫式伸展" to R.raw.ex_cat_stretch,
        "站立体前屈" to R.raw.ex_forward_fold,
        "婴儿式放松" to R.raw.ex_child_pose,
        "颈肩拉伸" to R.raw.ex_neck_stretch,
        "大腿前侧拉伸" to R.raw.ex_quad_stretch,
        "蝴蝶式拉伸" to R.raw.ex_butterfly_stretch,
        "胸部拉伸" to R.raw.ex_chest_stretch,
        "小腿拉伸" to R.raw.ex_calf_stretch,
        "90/90髋部拉伸" to R.raw.ex_hip_9090_stretch,
        "站姿侧腰拉伸" to R.raw.ex_standing_side_stretch,
        // —— 器械增补动作（ExerciseDataExtra8）——
        "阿诺德推举" to R.raw.ex_arnold_press,
        "直臂下压" to R.raw.ex_straight_arm_pulldown,
        "反手高位下拉" to R.raw.ex_reverse_lat_pulldown,
        "蝴蝶机反飞" to R.raw.ex_reverse_pec_deck,
        "腿弯举" to R.raw.ex_leg_curl,
        "腿屈伸" to R.raw.ex_leg_extension,
        "坐姿提踵" to R.raw.ex_seated_calf_raise,
    )

    fun videoFor(name: String): Int? = resByName[name.trim()]

    /** 动作缩略图：视频末帧（extract_thumbs.py 生成到 drawable-nodpi，与视频同名）。 */
    private val thumbResByName: Map<String, Int> = mapOf(
        // 热身
        "原地热身走" to R.drawable.ex_warmup_walk,
        "肩部绕环" to R.drawable.ex_shoulder_circle,
        "手腕脚踝环绕" to R.drawable.ex_wrist_ankle_circle,
        "髋部绕环" to R.drawable.ex_hip_circle,
        "腰部绕环" to R.drawable.ex_waist_circle,
        "弓步转体" to R.drawable.ex_lunge_twist,
        "前后摆腿" to R.drawable.ex_leg_swing_front,
        "侧向摆腿" to R.drawable.ex_leg_swing_side,
        "行走提踵" to R.drawable.ex_walking_calf_raise,
        "踢腿走" to R.drawable.ex_leg_kick_walk,
        // 胸肩
        "俯卧撑" to R.drawable.ex_pushup,
        "跪姿俯卧撑" to R.drawable.ex_kneeling_pushup,
        "杠铃卧推" to R.drawable.ex_barbell_bench,
        "上斜哑铃推举" to R.drawable.ex_incline_db_press,
        "器械夹胸" to R.drawable.ex_pec_deck,
        "坐姿推肩" to R.drawable.ex_shoulder_press,
        "哑铃侧平举" to R.drawable.ex_lateral_raise,
        // 背臂
        "引体向上" to R.drawable.ex_pullup,
        "高位下拉" to R.drawable.ex_lat_pulldown,
        "坐姿划船" to R.drawable.ex_seated_row,
        "杠铃弯举" to R.drawable.ex_barbell_curl,
        "锤式弯举" to R.drawable.ex_hammer_curl,
        "绳索下压" to R.drawable.ex_cable_pushdown,
        "哑铃颈后臂屈伸" to R.drawable.ex_overhead_ext,
        // 腿臀
        "徒手深蹲" to R.drawable.ex_squat,
        "弓步蹲" to R.drawable.ex_lunge,
        "史密斯深蹲" to R.drawable.ex_smith_squat,
        "腿举" to R.drawable.ex_leg_press,
        "臀桥" to R.drawable.ex_glute_bridge,
        "站姿提踵" to R.drawable.ex_calf_raise,
        // 臀部专项
        "单腿臀桥" to R.drawable.ex_single_leg_glute_bridge,
        "蚌式开合" to R.drawable.ex_clamshell,
        "跪姿后踢腿" to R.drawable.ex_glute_kickback,
        "绳索后踢腿" to R.drawable.ex_cable_kickback,
        "髋外展机" to R.drawable.ex_hip_abduction,
        "站姿侧抬腿" to R.drawable.ex_standing_side_leg_raise,
        // 核心
        "平板支撑" to R.drawable.ex_plank,
        "平板支撑开合跳" to R.drawable.ex_plank_jack,
        "卷腹" to R.drawable.ex_crunch,
        "触踝卷腹" to R.drawable.ex_heel_touch,
        "仰卧起坐" to R.drawable.ex_situp,
        "俄罗斯转体" to R.drawable.ex_russian_twist,
        "仰卧交替抬腿" to R.drawable.ex_lying_leg_raise,
        "仰卧单车" to R.drawable.ex_bicycle_crunch,
        "V字两头起" to R.drawable.ex_v_up,
        "死虫式" to R.drawable.ex_dead_bug,
        "鸟狗式" to R.drawable.ex_bird_dog,
        "超人式" to R.drawable.ex_superman,
        "登山跑" to R.drawable.ex_mountain_climber,
        // 自重进阶
        "上斜俯卧撑" to R.drawable.ex_incline_pushup,
        "宽距俯卧撑" to R.drawable.ex_wide_pushup,
        "钻石俯卧撑" to R.drawable.ex_diamond_pushup,
        "击掌俯卧撑" to R.drawable.ex_clap_pushup,
        "双杠臂屈伸" to R.drawable.ex_dip,
        "引体缓降" to R.drawable.ex_negative_pullup,
        "反手引体向上" to R.drawable.ex_chinup,
        "悬垂举腿" to R.drawable.ex_hanging_leg_raise,
        "单腿深蹲" to R.drawable.ex_pistol_squat,
        "保加利亚分腿蹲" to R.drawable.ex_bulgarian_split_squat,
        "深蹲跳" to R.drawable.ex_jump_squat,
        "靠墙倒立撑" to R.drawable.ex_wall_handstand_pushup,
        "侧平板支撑" to R.drawable.ex_side_plank,
        // 哑铃训练
        "哑铃卧推" to R.drawable.ex_db_bench,
        "哑铃划船" to R.drawable.ex_db_row,
        "哑铃硬拉" to R.drawable.ex_db_rdl,
        "哑铃肩上推举" to R.drawable.ex_db_shoulder_press,
        "高脚杯深蹲" to R.drawable.ex_goblet_squat,
        "哑铃箭步蹲" to R.drawable.ex_db_lunge,
        "俯身哑铃飞鸟" to R.drawable.ex_reverse_fly,
        "哑铃前平举" to R.drawable.ex_front_raise,
        "哑铃耸肩" to R.drawable.ex_db_shrug,
        "哑铃仰卧臂屈伸" to R.drawable.ex_db_skullcrusher,
        "农夫行走" to R.drawable.ex_farmer_walk,
        // 杠铃力量
        "杠铃深蹲" to R.drawable.ex_barbell_squat,
        "杠铃硬拉" to R.drawable.ex_barbell_deadlift,
        "杠铃划船" to R.drawable.ex_barbell_row,
        "站姿杠铃推举" to R.drawable.ex_ohp,
        "上斜杠铃卧推" to R.drawable.ex_incline_barbell_bench,
        "杠铃臀桥" to R.drawable.ex_barbell_hip_thrust,
        "杠铃耸肩" to R.drawable.ex_barbell_shrug,
        "杠铃早安式" to R.drawable.ex_barbell_good_morning,
        // 有氧
        "开合跳" to R.drawable.ex_jumping_jack,
        "高抬腿" to R.drawable.ex_high_knees,
        "原地慢跑" to R.drawable.ex_jog,
        "跳绳" to R.drawable.ex_jump_rope,
        "波比跳" to R.drawable.ex_burpee,
        "快走或慢跑" to R.drawable.ex_brisk_walk,
        "椭圆机" to R.drawable.ex_elliptical,
        "划船机" to R.drawable.ex_rowing_machine,
        "动感单车" to R.drawable.ex_spin_bike,
        "游泳" to R.drawable.ex_swimming,
        // 拉伸放松
        "全身拉伸" to R.drawable.ex_full_stretch,
        "背阔肌拉伸" to R.drawable.ex_lat_stretch,
        "猫式伸展" to R.drawable.ex_cat_stretch,
        "站立体前屈" to R.drawable.ex_forward_fold,
        "婴儿式放松" to R.drawable.ex_child_pose,
        "颈肩拉伸" to R.drawable.ex_neck_stretch,
        "大腿前侧拉伸" to R.drawable.ex_quad_stretch,
        "蝴蝶式拉伸" to R.drawable.ex_butterfly_stretch,
        "胸部拉伸" to R.drawable.ex_chest_stretch,
        "小腿拉伸" to R.drawable.ex_calf_stretch,
        "90/90髋部拉伸" to R.drawable.ex_hip_9090_stretch,
        "站姿侧腰拉伸" to R.drawable.ex_standing_side_stretch,
        // —— 器械增补动作（视频末帧缩略图）——
        "阿诺德推举" to R.drawable.ex_arnold_press,
        "直臂下压" to R.drawable.ex_straight_arm_pulldown,
        "反手高位下拉" to R.drawable.ex_reverse_lat_pulldown,
        "蝴蝶机反飞" to R.drawable.ex_reverse_pec_deck,
        "腿弯举" to R.drawable.ex_leg_curl,
        "腿屈伸" to R.drawable.ex_leg_extension,
        "坐姿提踵" to R.drawable.ex_seated_calf_raise,
    )

    fun thumbFor(name: String): Int? = thumbResByName[name.trim()]

    /** 动作所属分类（与视频目录/推荐训练量同源）。 */
    fun categoryOf(name: String): String? =
        CommonExercises.grouped.firstOrNull { (_, exercises) ->
            exercises.any { it.name == name.trim() }
        }?.first

    /** 分类 -> 动作名列表（顺序与 CommonExercises 一致）。 */
    val grouped: List<Pair<String, List<String>>> =
        CommonExercises.grouped.map { (category, exercises) ->
            category to exercises.map { it.name }
        }
}
