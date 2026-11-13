# 分阶段编码提示词 —— 「悦动」安卓健身 APP

> **使用方法**：用 ZCode（或 Claude Code / Cursor）打开 `YueDong` 项目文件夹，按阶段顺序，每次只复制**一个阶段提示词**发给 AI 编程工具。AI 执行完毕后，检查它输出的"运行自检结果"报告：只有显示"可进入下一阶段"才继续下一阶段；若"未通过"，把报错原样发回让它修复。
>
> **给您的提示**：阶段 0 需要您先安装 Android Studio（官方中国站 https://developer.android.google.cn/studio 下载稳定版），安装时全部默认选项即可。

---

## 主提示词（每次阶段对话都放在最前面 / 或作为项目规则文件 AGENTS.md 保存）

```text
【项目背景】
「悦动」是一款中文原生安卓健身 APP（求职作品集项目）：首次问卷采集性别、年龄、身高、当前/目标体重、每周运动天数、健身目的 → 本地计算 BMI、BMR、TDEE、目标热量与三大营养素 → 推荐/切换训练计划 → 每日打卡 + 日记 + 体重趋势。完整设计见项目 docs/ 目录（需求确认稿、LLM 开发设计包）。

【技术栈】
Kotlin 2.x + Jetpack Compose (Material 3) + Navigation Compose + Room(KSP) + DataStore + ViewModel + Flow；MVVM + Repository；纯逻辑（FitnessCalculator/InputValidator/PlanRecommender/DietAdvisor）为无 Android 依赖的纯 Kotlin；图表用 Canvas 自绘；测试 JUnit4。

【UI 语言规则】
全中文界面：所有按钮、标签、提示、空状态、错误文案必须为简体中文，禁止英文占位文本。

【UI 风格规则】
Material 3 简洁清爽：主色淡蓝 #64B5F6，primaryContainer #BBDEFB，onPrimaryContainer #0D47A1，强调深蓝 #1E6BB8，背景 #F7FAFC，成功 #66BB6A，错误 #E57373；卡片白底 16dp 圆角；关闭动态取色；空状态必须有中文引导语和行动按钮；禁止生成无样式灰白演示界面。

【架构规则】
责任完整但不过度复杂。模块高内聚低耦合：Screen 不写 SQL 和业务计算；ViewModel 只依赖 Repository 接口；纯逻辑不依赖 Android 类；不做 UseCase 层、不引 DI 框架、不拆多模块。目录分层：ui/（界面）、domain/（纯逻辑+模型+校验+扩展接口）、data/（Room/DataStore/Repository）、util/。

【模块契约规则】
新建核心模块必须写清：职责、非职责、输入、输出、公开接口、隐藏内部、依赖、扩展点（可写在文件头 KDoc 注释中）。

【数据真实性规则】
禁止 mock 假数据冒充真实功能：不得伪造打卡记录、体重数据、用户数据或任何"示例结果"。允许的只有：内置计划种子数据（真实产品内容）、空状态、加载状态、错误状态。V1 无 AI 功能，不得实现任何 AI 假回复。

【通用禁止事项】
1. 不实现本阶段之外未确认的功能
2. 不引入登录、支付、后台、权限系统
3. 不把业务逻辑写进 UI 组件
4. 不引入非必要第三方依赖
5. 不用英文界面
6. 不生成无样式界面
7. 运行自检未通过不得进入下一阶段
```

---

## 阶段 0：环境搭建与项目骨架

```text
当前阶段目标：创建 Android 项目骨架，实现淡蓝主题 + 底部 5 Tab 静态壳，跑在真机上。

【项目背景】
（粘贴主提示词全文）

需要完成：
1. 指导创建项目：New Project → Empty Activity (Compose)，项目名 YueDong，包名 com.yuedong.fitness，minSdk 26，Kotlin DSL + version catalog
2. 依赖：Compose BOM（最新稳定）、material-icons-extended、Navigation Compose；暂不引入 Room/DataStore（后续阶段再加）
3. ui/theme：按主提示词颜色定义 Color.kt / Theme.kt（关闭动态取色）、Type.kt
4. navigation/BottomNavBar：5 个 Tab——今日(Today图标)、计划(FitnessCenter)、饮食(Restaurant)、记录(Insights)、我的(Person)，中文标签
5. 5 个占位 Screen（onboarding/today/plan/diet/records/profile 各建文件，居中显示中文页面名 + "开发中"占位卡片样式）
6. MainActivity 装配 NavHost 与底部导航

创建或修改的文件：
1. app/build.gradle.kts、gradle/libs.versions.toml
2. ui/theme/Color.kt、Theme.kt、Type.kt
3. navigation/YueDongNavHost.kt、BottomNavBar.kt
4. 各 feature 目录占位 Screen 与 MainActivity.kt

不要做：
1. 数据库、DataStore、任何 ViewModel
2. 问卷流程逻辑
3. 引入 Room/DataStore 依赖

验收标准：
1. gradlew.bat assembleDebug 成功
2. 真机运行：显示今日页占位，5 Tab 可切换，切换不丢 Tab 状态
3. 全部文案中文，配色为淡蓝主题

运行自检要求（执行并把结果按下方模板输出）：
```bash
gradlew.bat assembleDebug
gradlew.bat lint
```
并在 Android Studio 中 Run 到真机，人工走查上述验收标准。

### 运行自检结果
执行的命令：
1. [命令]
结果：[通过/失败]
错误：[错误或无]
修复：[修复或无]
当前阶段状态：可进入下一阶段 / 未通过，需修复
```

---

## 阶段 1：数据层（Room + DataStore + Repository）

```text
当前阶段目标：完成全部本地数据层：Room 实体/DAO/数据库、DataStore 偏好、5 个 Repository 接口与实现、DAO 单元测试。无任何 UI 改动。

【项目背景】
（粘贴主提示词全文）

需要完成：
1. data/db/entity：UserProfile(id=1单行, gender, age, heightCm, currentWeightKg, targetWeightKg, weeklyExerciseDays, purpose, createdAt, updatedAt)、WorkoutPlan(name, description, source[BUILTIN/CUSTOM], purposeTag, daysPerWeek, isActive)、PlanDay(planId, orderIndex, title, isRestDay)、PlanExercise(planDayId, name, sets, repsOrDuration, tip)、CheckInRecord(date 唯一索引, planId, note?, createdAt)、BodyMetric(date 唯一索引, weightKg, 预留 waistCm 可空列)、DiaryEntry(date 唯一索引, mood 1~5, content ≤100字, updatedAt)
2. data/db/dao：对应 DAO，返回 Flow；CheckInRecord/BodyMetric/DiaryEntry 提供按日查询与当日 upsert（幂等）
3. data/db/AppDatabase：version 1，外键 + 索引
4. data/settings/SettingsDataStore：onboardingCompleted: Boolean、activePlanId: Long?
5. data/repository：ProfileRepository / PlanRepository / CheckInRepository / DiaryRepository / BodyMetricRepository —— 接口 + Room 实现，暴露 Flow 与 suspend 方法
6. YueDongApplication：轻量手动容器（单例 Database/Repositories，不用 DI 框架）
7. app/src/test：Room in-memory DAO 测试——插入/查询/当日 upsert 幂等/唯一索引冲突覆盖

创建或修改的文件：
1. data/db/**、data/settings/**、data/repository/**、YueDongApplication.kt
2. gradle 依赖：room-runtime/room-ktx/room-compiler(KSP)、datastore-preferences、coroutines-test、room-testing
3. app/src/test/java/com/yuedong/fitness/dao/**

不要做：
1. 任何 Screen/ViewModel 改动
2. 业务计算逻辑
3. 数据库预填数据（种子计划属于阶段 3）

验收标准：
1. gradlew.bat testDebugUnitTest 全绿
2. gradlew.bat assembleDebug 成功，App 真机启动无崩溃

运行自检要求：
```bash
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```

### 运行自检结果
（同阶段 0 模板）
```

---

## 阶段 2：问卷与健身指标计算

```text
当前阶段目标：欢迎页 + 分步问卷 + 输入校验 + Profile 落库 + FitnessCalculator（含单测）+ 启动去向控制。

【项目背景】
（粘贴主提示词全文）

需要完成：
1. domain/validation/InputValidator（纯 Kotlin）：年龄10~80、身高100~250、体重30~300、目标体重与当前差≤50、每周运动天数0~7；返回错误码+中文文案；配单元测试覆盖边界值
2. domain/logic/FitnessCalculator（纯 Kotlin，含 KDoc 契约）：
   - BMI = weight / (height/100)^2；分类：偏瘦<18.5 / 正常18.5~24 / 超重24~28 / 肥胖≥28
   - BMR（Mifflin-St Jeor）：男 10w+6.25h−5age+5，女同式−161
   - TDEE = BMR × 活动系数（0天1.2 / 1-2天1.375 / 3-4天1.55 / 5-7天1.725）
   - 目标热量：FAT_LOSS −400 / TONE −200 / HEALTH ±0 / MUSCLE_GAIN +300，下限保护女1200/男1500
   - 营养素：蛋白质 g/kg（FAT_LOSS 1.8 / MUSCLE_GAIN 1.8 / TONE 1.5 / HEALTH 1.2），脂肪=25%热量÷9，碳水=剩余÷4，取5的倍数
   - 输出 FitnessMetrics(bmi, bmiCategory, bmr, tdee, targetCalories, proteinG, carbG, fatG)；配男女各一组断言单测
3. ui/onboarding：欢迎页（App名+一句介绍+开始按钮）→ 3步问卷（①基本情况：性别/年龄/身高 ②体重目标：当前/目标体重 ③运动与目的：每周天数/目的单选），顶部步骤条，每步底部"下一步"，最后"完成"，实时 BMI 预览示意卡（分类配色：正常绿/偏瘦蓝/超重橙/肥胖红，配简笔矢量示意图或图标，见设计包§4）
4. OnboardingViewModel：逐项校验 → 保存 Profile（ProfileRepository）→ onboardingCompleted=true
5. 启动路由：未完成问卷 → 欢迎页；已完成 → 主界面（BottomNavBar 壳）

创建或修改的文件：
1. domain/validation/InputValidator.kt、domain/logic/FitnessCalculator.kt、domain/model/（枚举 Gender/Purpose、FitnessMetrics）
2. ui/onboarding/**、对应 ViewModel
3. YueDongNavHost.kt（启动去向）
4. app/src/test：InputValidatorTest、FitnessCalculatorTest

不要做：
1. 计划推荐（阶段 3）
2. 打卡/日记/体重记录页面
3. 任何假数据演示

验收标准：
1. 非法输入就地显示中文错误提示且无法进入下一步
2. 完成问卷后重启 App 直达主界面；"我的"数据可查到所填内容（临时在今日占位页显示 Profile 摘要便于验证）
3. InputValidatorTest / FitnessCalculatorTest 全绿

运行自检要求：
```bash
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```
真机走查：完整填写一遍问卷（含故意输错再纠正），确认提示与跳转。

### 运行自检结果
（同阶段 0 模板）
```

---

## 阶段 3：训练计划模块

```text
当前阶段目标：内置 3 套种子计划幂等写入；计划列表/详情/新建/切换；按目的+运动量推荐。

【项目背景】
（粘贴主提示词全文）

需要完成：
1. domain/logic/BuiltinPlanSeed（Kotlin 对象，真实中文内容）：
   - 居家徒手·燃脂入门（FAT_LOSS，3天/周）：全身唤醒 / 有氧燃脂 / 核心塑形，各4~6个动作（名称/组数/次数或时长/一句要点）
   - 健身房·力量入门（MUSCLE_GAIN，3天/周）：胸+三头 / 背+二头 / 腿+肩
   - 居家·健康活力（HEALTH、TONE 共用，3天/周）：拉伸激活 / 基础力量 / 有氧+核心
   - 每套含休息日 PlanDay(isRestDay=true)
2. 种子写入：App 启动时若 workout_plan 表无 BUILTIN 记录则写入（幂等，不重复）
3. domain/logic/PlanRecommender：interface PlanRecommender + BuiltinRuleRecommender（FAT_LOSS→燃脂入门；MUSCLE_GAIN→力量入门；TONE/HEALTH→健康活力；每周≥5天时优先健身房类；输出推荐理由一句中文）；配单测
4. ui/plan：PlanListScreen（卡片列表，推荐计划带"为你推荐"角标+理由，当前计划高亮，底部"新建自定义计划"按钮）、PlanDetailScreen（按天分组动作表：名称/组数/次数/要点，休息日显示"休息"）、PlanEditScreen（名称+每周天数+逐日添加动作表单，校验复用 InputValidator）
5. PlanViewModel：切换当前计划写 DataStore.activePlanId；新建计划入库
6. 计划详情页底部固定一行小字免责："本计划仅供参考，请量力而行，如有不适请停止训练并咨询专业人士"

创建或修改的文件：
1. domain/logic/BuiltinPlanSeed.kt、PlanRecommender.kt
2. ui/plan/**（3 个 Screen + ViewModel）
3. util/（若需种子写入辅助）
4. app/src/test：PlanRecommenderTest

不要做：
1. 今日页打卡（阶段 4）
2. 删除/编辑内置计划
3. AI 推荐实现（只留接口）

验收标准：
1. 首启种子写入且二次启动不重复
2. 三种目的推荐结果正确（单测验证）
3. 新建自定义计划后立即出现在列表，可被设为当前计划
4. 界面全中文、淡蓝风格一致

运行自检要求：
```bash
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```
真机走查：列表→详情→新建→切换。

### 运行自检结果
（同阶段 0 模板）
```

---

## 阶段 4：今日页（打卡 / 日记 / 体重速记）

```text
当前阶段目标：今日训练展示、幂等打卡、每日日记、体重速记。

【项目背景】
（粘贴主提示词全文）

需要完成：
1. 今日安排推算：以激活计划的 PlanDay 按 orderIndex 从用户开始日起逐日顺推（含休息日），得出"今天该练什么"；无激活计划显示引导卡（去选计划）
2. 打卡：大按钮"完成今日训练"→ 写 CheckInRecord(date=今天)；已打卡显示绿色"已完成 ✓"状态；重复点击不产生第二条记录（幂等 upsert）
3. 休息日：按钮变为不可点的"今天休息"卡
4. 体重速记卡：显示今日体重（无则显示最近一次）+ "记录今天体重"输入框（复用 InputValidator.validateWeight）
5. 日记卡：5 档心情 emoji（😞😟😐🙂😄）+ ≤100 字输入，当日 upsert（可修改）
6. TodayViewModel 汇编以上状态为 UiState（Loading/Empty/Success/Error）
7. FitnessCalculator.streak 单测：含今天连续 / 断签 / 空列表 / 含昨天不含今天用例

创建或修改的文件：
1. ui/today/TodayScreen.kt、TodayViewModel.kt、（日记弹层组件）
2. util/UiState.kt（如尚未建立）
3. app/src/test：FitnessCalculatorTest 补 streak 用例

不要做：
1. 记录页日历/图表（阶段 6）
2. 历史打卡的修改/删除
3. 通知提醒

验收标准：
1. 同一天多次点击打卡仅一条记录；日历数据可查到（临时日志或阶段6验证）
2. 休息日不出现打卡按钮
3. 日记二次编辑覆盖当日内容
4. 今日页数值与"我的"资料联动

运行自检要求：
```bash
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```
真机走查：打卡→重进 App 状态保持；写日记→改日记；记体重→显示更新。

### 运行自检结果
（同阶段 0 模板）
```

---

## 阶段 5：饮食建议页

```text
当前阶段目标：热量/营养素目标展示 + 分目的饮食建议 + 免责。

【项目背景】
（粘贴主提示词全文）

需要完成：
1. domain/logic/DietAdvisor（纯 Kotlin）：按 Purpose 返回 3~4 条中文建议（集中文案库便于审校，如减脂：每日热量缺口不超过500、蛋白质优先、少含糖饮料、主食粗细搭配）；输出结构化 List<DietTip>(title, detail)
2. ui/diet/DietScreen：
   - 顶部目标卡：TDEE、目标热量（大数字）、目的中文标签
   - 三大营养素三列卡片：蛋白质/碳水/脂肪 克数
   - "怎么吃"建议卡列表（DietAdvisor 文案）
   - 底部免责："以上为一般性建议，不构成医疗意见，特殊身体状况请咨询医生"
3. DietViewModel：订阅 Profile → FitnessCalculator.metrics() → 组装 UiState；无 Profile 显示空状态卡"先完成健康问卷，才能算出你的热量目标" + 按钮跳问卷
4. 今日页体重速记若与 BMI 相关信息展示有复用，保持引用 FitnessCalculator，不重复实现

创建或修改的文件：
1. domain/logic/DietAdvisor.kt
2. ui/diet/DietScreen.kt、DietViewModel.kt
3. app/src/test：DietAdvisorTest（各目的条数>0、文案非空）

不要做：
1. 食物数据库、饮食记录
2. 卡路里查询

验收标准：
1. 修改资料后饮食页数值联动
2. 未完成问卷进入显示空状态而非崩溃
3. 四种目的建议文案各不相同且全中文

运行自检要求：
```bash
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```
真机走查。

### 运行自检结果
（同阶段 0 模板）
```

---

## 阶段 6：记录页（日历 / 统计 / 体重趋势）

```text
当前阶段目标：月历打卡视图、连击与累计统计、历史列表、体重折线图（Canvas 自绘）。

【项目背景】
（粘贴主提示词全文）

需要完成：
1. ui/records/RecordsScreen 三段式：
   - 统计行：连续天数（FitnessCalculator.streak）、累计打卡天数、本月打卡 x 天；顶部另加「本周概况」自动总结卡（规则聚合：本周打卡 x 次、连续 y 天、体重较上周 ±z kg；纯本地真实数据计算，无假数据）
   - 月历卡：当月网格，打卡日淡蓝实心圆点标记，今日描边；左右切换月份
   - 体重趋势卡：Canvas 自绘折线（淡蓝线+深蓝点），X=日期 Y=体重，自适应 Y 轴范围（min−2 ~ max+2 kg）；仅 1 个点只画点；空数据显示空状态文案"还没有体重记录"
2. 历史列表卡：近期打卡倒序（日期 + 当日计划标题 + 心情 emoji 若有），分页加载（每次20条）
3. RecordsViewModel：组合 CheckIn/BodyMetric/Diary 三个 Flow，按月过滤，输出 UiState
4. 月历与折线组件放 ui/components（可复用、可单测的纯展示组件）

创建或修改的文件：
1. ui/components/MonthCalendar.kt、LineChart.kt
2. ui/records/RecordsScreen.kt、RecordsViewModel.kt

不要做：
1. 在记录页写数据或删除打卡
2. 引入第三方图表库
3. 更多图表类型

验收标准：
1. 空数据/1个点/多点三种形态正确渲染
2. 月份切换流畅，打卡标记与数据一致
3. 连击数与阶段 4 打卡事实一致；「本周概况」卡数字与打卡/体重事实一致

运行自检要求：
```bash
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```
真机走查：连续两天打卡后验证连击=2、日历标记、折线更新。

### 运行自检结果
（同阶段 0 模板）
```

---

## 阶段 7：我的页与作品集打磨（收尾）

```text
当前阶段目标：我的页完整 + 全局状态统一 + 应用收尾 + 作品集 README。

【项目背景】
（粘贴主提示词全文）

需要完成：
1. ui/profile/ProfileScreen：
   - 个人信息卡（性别/年龄/身高/目的，中文标签）+ BMI 卡（数值+分类色：正常绿/偏瘦蓝/超重橙/肥胖红）
   - 目标进度卡：减脂=已减/需减进度条，增重同理；显示当前体重 vs 目标体重差值
   - 菜单：修改资料（复用问卷表单，预填当前值）、切换计划（跳计划页）、关于
   - 关于页：App 介绍、版本 1.0.0、技术栈说明（Kotlin/Compose/Room/MVVM）、数据隐私声明（所有数据仅存本机）、免责
2. 全局打磨：所有页面空状态/错误态统一组件复查；删除阶段 2/3 的临时验证代码；返回键行为与 Tab 状态保持检查
3. 收尾：App 名称显示"悦动"；应用图标（可用 Compose 默认模板图标换成淡蓝背景+白色"悦"字样式的自适应图标）；strings 集中管理检查（无硬编码遗漏的英文）
4. docs/README.md（作品集门面）：项目简介、功能截图占位清单、技术栈与架构图（文字版分层图）、模块职责一览、如何运行、亮点讲解（纯逻辑单测/幂等打卡/自绘图表/扩展接口预留）
5. domain/extension/：落三个纯接口文件 AiCoachService.kt、DataExporter.kt、CloudSync.kt（仅签名+KDoc 注明"V1 未实现，接入须配置真实服务，禁止 mock 数据"），确保编译通过且无调用

创建或修改的文件：
1. ui/profile/**、ui/components（统一状态组件微调）
2. domain/extension/*
3. app 图标资源、AndroidManifest 名称核对
4. docs/README.md

不要做：
1. 实现任何扩展接口
2. 新功能
3. 大规模重构（只做局部清理）

验收标准：
1. 全流程真机走查：首装问卷→推荐计划→打卡→日记→记体重→记录页→饮食页→我的页，无崩溃、无英文占位、无假数据
2. gradlew.bat lint 无 error 级问题
3. README 完整可作面试讲解材料

运行自检要求：
```bash
gradlew.bat assembleDebug
gradlew.bat testDebugUnitTest
gradlew.bat lint
```
全流程人工走查（列出每页走查结果）。

### 运行自检结果
（同阶段 0 模板）
```

---

## 最终自检提示词（阶段 7 通过后发给 AI 工具）

```text
请对本项目做最终架构与代码自检，逐项回答"通过/不通过+证据"：
1. 每个核心模块是否职责单一、有清晰输入输出（检查 Screen 是否混入 SQL/计算）
2. ViewModel 是否只依赖 Repository 接口；纯逻辑（domain/logic、validation）是否无 Android 依赖
3. 是否存在 mock 假数据、假记录、假 AI 结果（种子计划除外）
4. 界面是否全中文、淡蓝主题一致、空状态齐全
5. 扩展接口（PlanRecommender/AiCoachService/DataExporter/CloudSync）是否只声明未实现、KDoc 是否注明真实服务要求
6. 依赖是否最小（官方栈+icons-extended，无图表库）
7. 全部单元测试是否通过；assembleDebug/lint 是否通过
8. docs/README.md 是否可作为面试讲解材料
输出自检报告；任何"不通过"项请给出修复建议并等待确认后再动手修复。
```
