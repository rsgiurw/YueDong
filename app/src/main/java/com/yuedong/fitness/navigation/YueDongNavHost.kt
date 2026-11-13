package com.yuedong.fitness.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yuedong.fitness.YueDongApplication
import com.yuedong.fitness.ui.components.YueToaster
import com.yuedong.fitness.ui.components.YueToastHost
import com.yuedong.fitness.ui.diet.DietScreen
import com.yuedong.fitness.ui.today.TodayTrainingScreen
import com.yuedong.fitness.ui.diet.FoodLibraryScreen
import com.yuedong.fitness.ui.exercise.ExerciseDetailScreen
import com.yuedong.fitness.ui.exercise.ExerciseLibraryScreen
import com.yuedong.fitness.ui.plan.PlanDetailScreen
import com.yuedong.fitness.ui.plan.PlanEditScreen
import com.yuedong.fitness.ui.plan.ExercisePickerScreen
import com.yuedong.fitness.ui.plan.PlanListScreen
import com.yuedong.fitness.ui.profile.PhotoWallScreen
import com.yuedong.fitness.ui.profile.ProfileScreen
import com.yuedong.fitness.ui.profile.AboutScreen
import com.yuedong.fitness.ui.profile.BackupScreen
import com.yuedong.fitness.ui.profile.MemoScreen
import com.yuedong.fitness.ui.profile.RecycleBinScreen
import com.yuedong.fitness.ui.profile.ReminderEditScreen
import com.yuedong.fitness.ui.profile.ReminderListScreen
import com.yuedong.fitness.ui.records.CheckInCalendarScreen
import com.yuedong.fitness.ui.records.DayDetailScreen
import com.yuedong.fitness.ui.records.FullHistoryScreen
import com.yuedong.fitness.ui.records.RecordsScreen
import com.yuedong.fitness.ui.today.TodayScreen

/**
 * 路由常量（AppShell 扩展点）：新增页面时在这里加路由即可。
 */
object Routes {
    const val TODAY = "today"
    const val PLAN = "plan"
    const val DIET = "diet"
    const val DIET_RECORD = "diet_record?meal={meal}"
    const val RECORDS = "records"
    const val CHECKIN_CALENDAR = "checkin_calendar"
    const val TODAY_TRAINING = "today_training"
    const val WEIGHT_TREND = "weight_trend"
    const val FULL_HISTORY = "full_history/{mode}"
    const val DAY_DETAIL = "day_detail/{date}"
    fun dayDetail(date: String) = "day_detail/${android.net.Uri.encode(date)}"
    fun fullHistory(mode: String) = "full_history/$mode"
    const val PROFILE = "profile"
    const val BACKUP = "backup"
    const val REMINDER = "reminder"
    const val REMINDER_EDIT = "reminder_edit/{id}"
    const val ABOUT = "about"
    const val MEMO_NOTES = "memo_notes"
    const val RECYCLE_BIN = "recycle_bin"
    const val GUIDE = "guide"
    const val PLAN_DETAIL = "plan_detail/{planId}"
    const val PLAN_EDIT = "plan_edit?planId={planId}"
    const val PROFILE_EDIT = "profile_edit"
    const val EXERCISE_DETAIL = "exercise_detail/{name}?viewOnly={viewOnly}"
    const val EXERCISE_LIBRARY = "exercise_library?tab={tab}"
    const val EXERCISE_PICKER = "exercise_picker"
    const val FOOD_LIBRARY = "food_library?tab={tab}"
    const val PHOTO_WALL = "photo_wall"
    const val FOOD_DETAIL = "food_detail/{name}?viewOnly={viewOnly}"
    const val CUSTOM_EXERCISE_EDIT = "custom_exercise_edit?name={name}&edit={edit}"
    const val CUSTOM_FOOD_EDIT = "custom_food_edit?name={name}"
    const val WEEKLY_REPORT = "weekly_report"
    const val REPORT_POSTER = "report_poster?period={period}"
    fun reportPoster(period: com.yuedong.fitness.ui.records.ReportPeriod) =
        "report_poster?period=${period.name}"
    fun foodDetail(name: String, viewOnly: Boolean = false) =
        "food_detail/${android.net.Uri.encode(name)}?viewOnly=$viewOnly"
    fun customExerciseEdit(name: String? = null, startInEdit: Boolean = false): String {
        val base = if (name.isNullOrBlank()) "custom_exercise_edit" else "custom_exercise_edit?name=${android.net.Uri.encode(name)}"
        return if (startInEdit) if (base.contains('?')) "$base&edit=true" else "$base?edit=true" else base
    }
    fun customFoodEdit(name: String? = null) =
        if (name.isNullOrBlank()) "custom_food_edit" else "custom_food_edit?name=${android.net.Uri.encode(name)}"

    fun planDetail(planId: Long) = "plan_detail/$planId"
    fun planEdit(planId: Long? = null) = if (planId == null) "plan_edit" else "plan_edit?planId=$planId"
    fun exerciseDetail(name: String, viewOnly: Boolean = false) =
        "exercise_detail/${android.net.Uri.encode(name)}?viewOnly=$viewOnly"
}

/**
 * 应用骨架（AppShell 模块）。
 * 职责：启动后台检查更新（静默失败）；主界面 = 底部 5 Tab + 页面路由。
 * 说明：首启直接进主界面（渐进式引导）；填写由今日页欢迎卡与各页空态按需触发。
 */
@Composable
fun YueDongApp() {
    // 启动检查更新：服务器 version.json 的 versionCode 更高时弹更新层；
    // 网络失败静默跳过（地址与发版方法见 AppUpdater.UPDATE_JSON_URL 与发版指南）
    var pendingUpdate by remember { mutableStateOf<com.yuedong.fitness.domain.update.UpdateInfo?>(null) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        pendingUpdate = runCatching {
            com.yuedong.fitness.domain.update.AppUpdater.checkForUpdate()
        }.getOrNull()
    }
    YueDongMainScreen()
    pendingUpdate?.let { info ->
        com.yuedong.fitness.ui.update.UpdateDialog(info = info, onDismiss = { if (!info.force) pendingUpdate = null })
    }
}

/**
 * 路由 → 底部标签根路由的映射：仅 5 个标签根页参与高亮与标签历史。
 * 「添加饮食」（diet_record）是二级任务页：不高亮底栏、不进标签历史，
 * 系统返回 = 弹回来源页（今日/记录页等），符合「返回回到上一次状态」。
 */
private fun tabRootOf(route: String?): String? = when (route) {
    Routes.TODAY -> Routes.TODAY
    Routes.PLAN -> Routes.PLAN
    Routes.DIET -> Routes.DIET
    Routes.RECORDS -> Routes.RECORDS
    Routes.PROFILE -> Routes.PROFILE
    else -> null
}

@Composable
private fun YueDongMainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val context = LocalContext.current

    // 标准底部导航:切标签 = 永远落到该标签根页(其上的二级页随之清掉)。
    // 这样主页面时刻保持「平级」:在任意主页面按返回,都是两次退出确认,而不是回今日。
    val goTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(Routes.TODAY)
            launchSingleTop = true
        }
    }

    // 主页面平级,返回键不切换标签:主页面按返回 = 2 秒内两次才退出应用;
    // 二级页逐级回退(框架默认)。拦截逻辑由 DoubleBackToExit() 在各标签根页内部注册,
    // 保证其优先级高于 NavHost 自身的返回处理。

    val toastMessage by com.yuedong.fitness.ui.components.YueToaster.message.collectAsState()
    val tabRootSet = remember {
        setOf(Routes.TODAY, Routes.PLAN, Routes.DIET, Routes.RECORDS, Routes.PROFILE)
    }

    // 悬浮层架构：内容全屏铺满，导航条悬浮在内容上方。
    // 内容底边距不随导航条显隐变化 → 切页时无布局突变（此前 Scaffold 槽位
    // 在动画结束后回收内边距，造成"下方内容突然才渲染出来"的跳变）。
    // 主页面之间横向滑动切换:只在五个标签根页生效(二级页与弹层不受影响);
    // 左滑 = 下一个标签,右滑 = 上一个标签。页面内部的横滑组件(月历翻页等)优先消费手势,不会误触
    val swipeOrder = listOf(Routes.TODAY, Routes.PLAN, Routes.DIET, Routes.RECORDS, Routes.PROFILE)
    val swipeIndex = swipeOrder.indexOf(currentRoute).takeIf { currentRoute in tabRootSet } ?: -1
    val swipeModifier = if (swipeIndex >= 0) {
        Modifier.pointerInput(swipeIndex) {
            val threshold = 110.dp.toPx()
            var totalDrag = 0f
            detectHorizontalDragGestures(
                onDragStart = { totalDrag = 0f },
                onDragCancel = { totalDrag = 0f },
                onDragEnd = {
                    when {
                        totalDrag <= -threshold && swipeIndex < swipeOrder.lastIndex ->
                            goTab(swipeOrder[swipeIndex + 1])
                        totalDrag >= threshold && swipeIndex > 0 ->
                            goTab(swipeOrder[swipeIndex - 1])
                    }
                    totalDrag = 0f
                }
            ) { change, dragAmount ->
                totalDrag += dragAmount
                change.consume()
            }
        }
    } else {
        Modifier
    }

    androidx.compose.foundation.layout.Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.TODAY,
            modifier = Modifier
                .fillMaxSize()
                .then(swipeModifier),
            // T012：页面切换 200ms 淡入 + 轻微上浮，替代瞬时硬切（克制为主）
            // 标签间切换 = Material Fade-Through（淡入 + 轻缩放，无位移）；
            // 进入二级页 = 上浮滑入；返回 = 淡出（标签页带轻微放大）
            enterTransition = {
                if (targetState.destination.route in tabRootSet) {
                    fadeIn(tween(240)) + scaleIn(initialScale = 0.96f, animationSpec = tween(240))
                } else {
                    fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 24 }
                }
            },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = {
                if (targetState.destination.route in tabRootSet) {
                    fadeIn(tween(240)) + scaleIn(initialScale = 0.96f, animationSpec = tween(240))
                } else {
                    fadeIn(tween(200))
                }
            },
            popExitTransition = {
                if (initialState.destination.route in tabRootSet) {
                    fadeOut(tween(160)) + scaleOut(targetScale = 1.03f, animationSpec = tween(160))
                } else {
                    fadeOut(tween(150))
                }
            }
        ) {
            composable(Routes.TODAY) {
                DoubleBackToExit()
                TodayScreen(
                    onGoPlan = { goTab(Routes.PLAN) },
                    onOpenDiet = {
                        // 必须用不带占位符的具体值：若把 "{meal}" 模式串原样传入，
                        // 占位符会变成字面量参数值，触发添加饮食页的自动弹层并吞掉一次返回
                        navController.navigate("diet_record")
                    },
                    onOpenCalendar = {
                        navController.navigate(Routes.CHECKIN_CALENDAR)
                    },
                    onOpenTraining = {
                        navController.navigate(Routes.TODAY_TRAINING)
                    },
                    onOpenWeight = {
                        navController.navigate(Routes.WEIGHT_TREND)
                    },
                    onOpenProfileEdit = {
                        navController.navigate(Routes.PROFILE_EDIT)
                    }
                )
            }
            composable(Routes.PLAN) {
                DoubleBackToExit()
                PlanListScreen(
                    onOpenPlan = { planId -> navController.navigate(Routes.planDetail(planId)) },
                    onCreatePlan = { navController.navigate(Routes.PLAN_EDIT) },
                    onOpenLibrary = { navController.navigate("exercise_library") }
                )
            }
            composable(
                Routes.EXERCISE_LIBRARY,
                arguments = listOf(navArgument("tab") { type = NavType.StringType; defaultValue = "" })
            ) {
                ExerciseLibraryScreen(
                    onBack = { navController.popBackStack() },
                    initialCustom = it.arguments?.getString("tab") == "custom",
                    onOpenExercise = { name -> navController.navigate(Routes.exerciseDetail(name)) },
                    onCreateCustom = { prefill -> navController.navigate(Routes.customExerciseEdit(prefill.ifBlank { null })) },
                    onEditCustom = { name -> navController.navigate(Routes.customExerciseEdit(name)) }
                )
            }
            composable(Routes.EXERCISE_PICKER) {
                ExercisePickerScreen(
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() },
                    onOpenExercise = { navController.navigate(Routes.exerciseDetail(it, viewOnly = true)) }
                )
            }
            composable(
                route = Routes.PLAN_DETAIL,
                arguments = listOf(navArgument("planId") { type = NavType.LongType })
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getLong("planId") ?: 0L
                PlanDetailScreen(
                    planId = planId,
                    onBack = { navController.popBackStack() },
                    onEditPlan = { id -> navController.navigate(Routes.planEdit(id)) },
                    onOpenExercise = { name -> navController.navigate(Routes.exerciseDetail(name)) }
                )
            }
            composable(
                route = Routes.PLAN_EDIT,
                arguments = listOf(navArgument("planId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val editId = backStackEntry.arguments?.getLong("planId") ?: -1L
                PlanEditScreen(
                    planId = editId.takeIf { it > 0 },
                    onBack = { navController.popBackStack() },
                    onPickExercise = { navController.navigate(Routes.EXERCISE_PICKER) }
                )
            }
            composable(Routes.DIET) {
                DoubleBackToExit()
                DietScreen(
                    onOpenFoodLibrary = { navController.navigate(Routes.FOOD_LIBRARY) },
                    onOpenFoodDetail = { navController.navigate(Routes.foodDetail(it, viewOnly = true)) },
                    onOpenProfileEdit = { navController.navigate(Routes.PROFILE_EDIT) }
                )
            }
            composable(
                Routes.DIET_RECORD,
                arguments = listOf(navArgument("meal") { type = NavType.StringType; defaultValue = "" })
            ) { backStackEntry ->
                DietScreen(
                    initialTab = 1,
                    mealHint = backStackEntry.arguments?.getString("meal").orEmpty(),
                    onOpenFoodLibrary = { navController.navigate(Routes.FOOD_LIBRARY) },
                    onOpenFoodDetail = { navController.navigate(Routes.foodDetail(it, viewOnly = true)) },
                    onOpenProfileEdit = { navController.navigate(Routes.PROFILE_EDIT) }
                )
            }
            composable(
                Routes.FOOD_LIBRARY,
                arguments = listOf(navArgument("tab") { type = NavType.StringType; defaultValue = "" })
            ) {
                FoodLibraryScreen(
                    onBack = { navController.popBackStack() },
                    initialCustom = it.arguments?.getString("tab") == "custom",
                    onOpenDetail = { name -> navController.navigate(Routes.foodDetail(name)) },
                    onCreateCustomFood = { navController.navigate(Routes.customFoodEdit()) },
                    onEditCustomFood = { name -> navController.navigate(Routes.customFoodEdit(name)) }
                )
            }
            composable(Routes.PHOTO_WALL) {
                PhotoWallScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.WEEKLY_REPORT) {
                com.yuedong.fitness.ui.records.WeeklyReportScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPoster = { period -> navController.navigate(Routes.reportPoster(period)) }
                )
            }
            composable(
                Routes.REPORT_POSTER,
                arguments = listOf(navArgument("period") {
                    type = NavType.StringType
                    defaultValue = "MONTHLY"
                })
            ) { backStackEntry ->
                val posterPeriod = runCatching {
                    com.yuedong.fitness.ui.records.ReportPeriod.valueOf(
                        backStackEntry.arguments?.getString("period") ?: "MONTHLY"
                    )
                }.getOrElse { com.yuedong.fitness.ui.records.ReportPeriod.MONTHLY }
                com.yuedong.fitness.ui.records.ReportPosterScreen(
                    period = posterPeriod,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Routes.FOOD_DETAIL,
                arguments = listOf(navArgument("viewOnly") {
                    type = NavType.BoolType; defaultValue = false
                })
            ) { backStackEntry ->
                val foodName = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("name").orEmpty(), "UTF-8"
                )
                com.yuedong.fitness.ui.diet.FoodDetailScreen(
                    name = foodName,
                    viewOnly = backStackEntry.arguments?.getBoolean("viewOnly") ?: false,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.customFoodEdit(foodName)) }
                )
            }
            composable(Routes.CHECKIN_CALENDAR) {
                CheckInCalendarScreen(
                    onBack = { navController.popBackStack() },
                    onOpenDay = { date -> navController.navigate(Routes.dayDetail(date)) }
                )
            }
            composable(Routes.WEIGHT_TREND) {
                com.yuedong.fitness.ui.today.WeightTrendScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.TODAY_TRAINING) {
                TodayTrainingScreen(
                    onBack = { navController.popBackStack() },
                    onOpenExercise = { name -> navController.navigate(Routes.exerciseDetail(name)) }
                )
            }
            composable(Routes.RECORDS) {
                DoubleBackToExit()
                RecordsScreen(
                    onGoDiet = {
                        navController.navigate("diet_record")
                    },
                    onOpenFullHistory = { mode -> navController.navigate(Routes.fullHistory(mode)) },
                    onOpenWeeklyReport = { navController.navigate(Routes.WEEKLY_REPORT) },
                    onOpenDay = { date -> navController.navigate(Routes.dayDetail(date)) },
                    onOpenWeightTrend = { navController.navigate(Routes.WEIGHT_TREND) },
                    onOpenCheckInCalendar = { navController.navigate(Routes.CHECKIN_CALENDAR) }
                )
            }
            composable(Routes.DAY_DETAIL) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date")
                DayDetailScreen(
                    date = date,
                    onBack = { navController.popBackStack() },
                    onOpenExercisePicker = { navController.navigate(Routes.EXERCISE_PICKER) }
                )
            }
            composable(Routes.FULL_HISTORY) { backStackEntry ->
                FullHistoryScreen(
                    mode = backStackEntry.arguments?.getString("mode") ?: "checkin",
                    onBack = { navController.popBackStack() },
                    onOpenDay = { date -> navController.navigate(Routes.dayDetail(date)) }
                )
            }
            composable(Routes.PROFILE) {
                DoubleBackToExit()
                ProfileScreen(
                    onGoPlan = { goTab(Routes.PLAN) },
                    onEditProfile = { navController.navigate(Routes.PROFILE_EDIT) },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(Routes.BACKUP) {
                BackupScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.REMINDER) {
                ReminderListScreen(
                    onBack = { navController.popBackStack() },
                    onOpenEdit = { id -> navController.navigate("reminder_edit/$id") }
                )
            }
            composable(
                Routes.REMINDER_EDIT,
                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
            ) {
                ReminderEditScreen(
                    reminderId = it.arguments?.getLong("id") ?: -1L,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ABOUT) {
                AboutScreen(
                    onBack = { navController.popBackStack() },
                    onOpenGuide = { navController.navigate(Routes.GUIDE) },
                    onOpenBackup = { navController.navigate(Routes.BACKUP) },
                    onOpenRecycleBin = { navController.navigate(Routes.RECYCLE_BIN) }
                )
            }
            composable(Routes.MEMO_NOTES) {
                MemoScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.RECYCLE_BIN) {
                RecycleBinScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.GUIDE) {
                com.yuedong.fitness.ui.guide.GuideScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.PROFILE_EDIT) {
                com.yuedong.fitness.ui.profile.ProfileEditScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.EXERCISE_DETAIL,
                arguments = listOf(
                    navArgument("name") { type = NavType.StringType },
                    navArgument("viewOnly") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name").orEmpty()
                ExerciseDetailScreen(
                    name = java.net.URLDecoder.decode(name, "UTF-8"),
                    viewOnly = backStackEntry.arguments?.getBoolean("viewOnly") ?: false,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.customExerciseEdit(name, startInEdit = true)) },
                    onDelete = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.CUSTOM_EXERCISE_EDIT,
                arguments = listOf(
                    navArgument("name") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("edit") {
                        type = NavType.StringType
                        defaultValue = "false"
                    }
                )
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name").orEmpty()
                com.yuedong.fitness.ui.exercise.CustomExerciseEditScreen(
                    editName = name.takeIf { it.isNotBlank() }?.let { java.net.URLDecoder.decode(it, "UTF-8") },
                    prefillName = "",
                    startInEdit = backStackEntry.arguments?.getString("edit") == "true",
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.CUSTOM_FOOD_EDIT,
                arguments = listOf(navArgument("name") {
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name").orEmpty()
                com.yuedong.fitness.ui.diet.CustomFoodEditScreen(
                    editName = name.takeIf { it.isNotBlank() }?.let { java.net.URLDecoder.decode(it, "UTF-8") },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    val toastBottom by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (currentRoute in tabRootSet) 92.dp else 24.dp,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "toastBottom"
    )
    // 悬浮导航条：主界面滑入显示，进二级页滑出隐藏
    androidx.compose.animation.AnimatedVisibility(
        visible = currentRoute in tabRootSet,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
        ) { it } + fadeIn(tween(200)),
        exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(180))
    ) {
        YueDongBottomBar(currentRoute = tabRootOf(currentRoute) ?: currentRoute) { route ->
            goTab(route)
        }
    }
    YueToastHost(
        message = toastMessage,
        onDismiss = { YueToaster.dismiss() },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = toastBottom)
    )
    } // Box
}
/** 主页面双击返回退出:2 秒内两次返回才退出应用(注册于标签根页内部,优先级高于 NavHost 返回处理) */
@Composable
private fun DoubleBackToExit() {
    val context = LocalContext.current
    var lastBackPress by remember { mutableStateOf(0L) }
    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBackPress < 2000) {
            (context as? android.app.Activity)?.finish()
        } else {
            lastBackPress = now
            YueToaster.show("再按一次退出应用")
        }
    }
}
