# 《AI 编程项目 LLM 开发设计包》—— 「悦动」安卓健身 APP

> 按 ArchPilot 三层机制产出：需求翻译层 → 架构设计层 → LLM 执行层。
> 本设计包为原生安卓项目，原模板中"Prompt 管理 / LLM 调用 / 结果解析"层不适用于 V1（无 AI 功能），已替换为等效的"纯业务计算层 + 扩展接口层"设计。

---

## 1. 项目定位

中文原生安卓健身 APP「悦动」（Kotlin + Jetpack Compose）：首次问卷采集用户身体数据与健身目的 → 本地计算 BMI、每日热量与三大营养素目标 → 推荐/切换训练计划 → 每日打卡 + 日记 → 体重趋势追踪。求职作品集定位，架构可讲解、界面可演示。

## 2. 需求理解与合理假设

### 用户明确说了什么
1. 求职作品集，目标岗位安卓开发；技术路线选原生 Kotlin + Compose
2. 首次问卷：性别、年龄、身高、当前/目标体重、运动量、健身目的
3. 5 大板块：计划、推荐饮食、日记、锻炼记录、个人信息
4. 饮食做到"热量目标 + 营养素建议"，不做食物数据库
5. 体型用 BMI 自动判断；界面中文、淡蓝简洁风；数据本地存储；V1 无 AI

### 用户没说清楚什么
1. 内置计划的具体训练内容（已假设覆盖居家与健身房）
2. 打卡与日记的交互细节（已按健身 APP 惯例设计）
3. 是否需要深色模式（V1 不做，预留）

### 当前合理假设
1. 单人使用，无账号；日记每日一条（心情 + 一句话）。**（v2.1 采纳用户建议）** 新增「本周概况」自动总结卡：App 根据本地**真实数据**按固定规则生成（本周打卡 x 次、连续坚持 y 天、体重较上周 ±z kg）——这是规则计算，不是 AI，也不允许伪造数据
2. 体重手动录入，合法范围 30~300 kg
3. 开发环境为 Windows + Android Studio，运行于用户自己的安卓手机（minSdk 26）

### 假设对架构的影响
1. "单人无账号" → Room 单库直存，不引入 userId 维度；将来加云同步时通过 Repository 接口隔离替换，不影响 UI 层
2. "无 AI" → 业务计算全部本地纯函数实现（可单测）；AI 教练仅以接口形式预留，不写任何假实现
3. "饮食只到建议层" → `DietAdvisor` 为纯计算模块，输出结构化建议；将来扩展食物数据库时只需新增表与页面，不动计算层

## 3. MVP 功能边界

### 第一版必须做
1. 首次问卷（校验 + 保存 + 自动计算指标）
2. 内置 3 套计划 + 计划推荐 + 自定义新建 + 切换当前计划
3. 今日页：今日训练、打卡、体重速记、日记
4. 记录页：打卡日历、连续/累计统计、锻炼历史、体重折线图、「本周概况」自动总结卡（规则生成）
5. 饮食页：热量/营养素目标卡片 + 分目的饮食建议
6. 我的页：个人信息查看/修改、目标进度、关于
7. 空状态 / 加载 / 错误 / 校验全覆盖，全中文文案

### 第二版再做
1. AI 健身教练（问答、生成计划）
2. 训练提醒通知
3. 数据导出 CSV
4. 腰围等更多身体指标

### 暂时不做
1. 登录注册、云同步、账号体系
2. 食物数据库、饮食拍照记录
3. 社区、排行榜、视频教程
4. 平板适配、深色模式、多语言切换

## 4. 用户流程

> **（v2.1 采纳用户建议）** 关键指标用小卡片示意图帮助理解：BMI 分类（偏瘦/正常/超重/肥胖）配简笔示意图 + 分类配色卡（正常绿 / 偏瘦蓝 / 超重橙 / 肥胖红）。V1 用 Compose 绘制或 Vector Drawable 矢量图实现，V2 可替换为精美插画素材。

1. 首次启动 → 欢迎页 → 分步问卷（性别/年龄/身高/体重/目标体重/每周运动天数/健身目的）→ 校验 → 保存
2. 自动计算：BMI（含分类）、BMR（Mifflin-St Jeor）、TDEE（活动系数）、目标热量（按目的增减）、蛋白质/碳水/脂肪克数
3. 按目的+运动量推荐内置计划（高亮推荐），用户确认或自选 → 进入主界面
4. 主界面 5 标签：
   - **今日**：显示当前计划今日安排 → 训练完成点"完成今日训练"打卡 → 可记当日体重 → 可写日记（心情 emoji + 一句话）
   - **计划**：内置计划列表（含推荐标记）、计划详情（每日动作组数次数要点）、新建自定义计划、切换当前计划
   - **饮食**：热量目标卡片（TDEE、目标热量、三大营养素）、按目的显示饮食建议卡
   - **记录**：月历打卡视图（打卡日变色）、连续天数/累计天数、历史打卡列表、体重折线图
   - **我的**：个人信息卡、目标体重进度条、修改资料（重新填写问卷）、关于
5. 数据全程本地 Room 存储；卸载即清除，无任何上传

## 5. 界面语言与 UI 设计规范

### 界面语言
全中文。所有文案（按钮、提示、空状态、错误、无障碍描述）使用简体中文，不出现英文占位文本。

### UI 风格
Material 3 简洁清爽风：卡片圆角 16dp、留白充足、每页一个主操作按钮、字重分层（标题 18sp 加粗 / 正文 14sp / 辅助 12sp 灰）。

### 主色调
- 主色 primary：`#64B5F6`（淡蓝），onPrimary 白
- 主色容器 primaryContainer：`#BBDEFB`，onPrimaryContainer 深蓝 `#0D47A1`
- 强调/按钮深蓝：`#1E6BB8`
- 背景 background：`#F7FAFC`，surface 白
- 成功/打卡色：`#66BB6A`；错误：`#E57373`
- 关闭 Material 动态取色（保证品牌色一致）

### 页面结构规范（每页通用）
1. 顶栏：页标题（28sp 加粗）+ 必要的返回/编辑入口
2. 内容区：信息卡片纵向排列，卡片白底圆角带浅阴影
3. 操作区：每页最多一个主按钮（淡蓝实心圆角）+ 次要文字按钮
4. 空状态：居中插画式图标 + 一句中文引导（如"还没有体重记录，去记下今天的体重吧"）+ 行动按钮
5. 加载/错误：加载用小圈圈占位；错误显示原因 + "重试"按钮；表单错误就地显示在字段下方（红色中文提示）

## 6. 责任完整但不过度复杂的架构说明

### 已覆盖的应用责任（16 项对照）
1. 页面/UI 层 → `ui/` 各 Screen + 通用组件
2. 用户输入层 → 问卷表单、体重输入、日记输入、计划编辑表单
3. 校验层 → `domain/validation/InputValidator`（纯函数，可单测）
4. 业务流程编排层 → 各 `ViewModel`（状态编排：加载→数据/空/错误）
5. Prompt 管理层 → **V1 不适用**；由 `domain/logic/PlanRecommender` + `DietAdvisor` 承担"策略/文案生成"职责，AI 版本将来以同接口替换
6. 真实 LLM 调用层 → **V1 不适用**；预留 `AiCoachService` 接口位置（见 §10）
7. 结果解析层 → **V1 不适用**；计算结果直接为强类型 `DietTarget`/`FitnessMetrics`
8. 数据结构层 → `data/model`（Room 实体）+ `domain/model`（枚举与值对象）
9. 文件处理层 → **V1 不适用**（无文件输入）；导出 CSV 预留 `DataExporter`
10. 结果展示层 → 统计卡片、日历、折线图（自绘 Canvas，零第三方图表依赖）
11. 导出/复制层 → 预留接口（V1 不实现）
12. 错误处理层 → `UiState<T>` 密封类（Loading/Empty/Success/Error）+ 中文错误文案集中管理
13. 配置管理层 → `ui/theme`（颜色字体）+ `AppConfig`（常量：热量下限、活动系数表、营养素比例）
14. UI 语言/风格层 → 全中文硬文案集中在各 Screen；主题色集中 `Color.kt`
15. 后续扩展接口层 → `PlanRecommender` / `AiCoachService` / `DataExporter` / Repository 均为接口（见 §10）
16. 试运行与验证层 → 每阶段 `gradlew assembleDebug` + `testDebugUnitTest` + 手机真机走查（见分阶段提示词）

### 明确不引入的复杂设计
微服务 / 消息队列 / 复杂 DDD / 多租户权限 / 企业 SSO / 支付系统 / 插件化 / 无用抽象基类 / UseCase 层堆叠 / 依赖注入框架全全家桶（仅用轻量手动注入）。

### 架构原则
模块化单体：一个 Android 工程、一个 App，内部按 `ui / domain / data` 三层分包，边界靠 Kotlin 接口约束。MVVM + Repository，ViewModel 只依赖 Repository 接口，不接触 Room。

## 7. 推荐技术栈

### 主方案
| 项 | 选择 |
|---|---|
| 语言 | Kotlin 2.x |
| UI | Jetpack Compose + Material 3（关闭动态取色） |
| 导航 | Navigation Compose（底部 5 Tab + 二级页） |
| 数据库 | Room 2.7.x（KSP） |
| 偏好 | DataStore Preferences（是否完成问卷、当前计划 id） |
| 架构 | MVVM + Repository + Flow |
| 图表 | 自绘 Canvas 折线（不引第三方图表库） |
| 图标 | material-icons-extended（FitnessCenter/Restaurant 等） |
| 测试 | JUnit4 + kotlinx-coroutines-test |
| 构建 | Gradle（Kotlin DSL）+ version catalog |

### 选择原因
1. 全官方栈，面试标准配置，资料最多，AI 编程工具生成质量最高
2. 纯本地应用零后端，作品集可完整开源展示
3. 不用第三方图表库：少一个依赖少一份风险，自绘 Canvas 本身就是面试讲解点

### 备选方案
若用户电脑性能不足以运行 Android Studio 模拟器：真机 USB 调试运行（推荐，更真实）；UI 微调阶段可用 `gradlew installDebug` 快速部署。

## 8. 模块职责设计

### Module: AppShell（应用骨架与导航）
**Responsibility**
1. 承载 MainActivity、全局主题、底部 5 Tab 导航与二级页路由
2. 决定启动去向：未完成问卷 → 问卷页；已完成 → 主界面今日页

**Non-responsibility**
1. 不含任何业务计算；不直接访问数据库

**Input**
1. 用户点击 Tab / 页面路由事件；`SettingsDataStore.onboardingCompleted` 标志

**Output**
1. 当前显示的 Screen；主题样式应用到全局

**Public interface**
1. `YueDongNavHost(navController, startDestination)`；路由常量 `Routes`

**Hidden internals**
1. 导航图内部结构；Tab 与路由映射表

**Dependencies**
1. ui/theme、各 Feature Screen、SettingsDataStore

**Extension points**
1. 新增二级页只需加路由；未来深色模式在 theme 层扩展

**Run/test focus**
1. App 启动后 5 Tab 可切换、无崩溃；问卷完成前后启动去向正确

---

### Module: Onboarding（首次问卷模块）
**Responsibility**
1. 分步问卷表单 UI；收集 7 项信息；就地校验；保存 Profile 并写入 `onboardingCompleted`

**Non-responsibility**
1. 不计算 BMI/热量（交给 FitnessCalculator）；不推荐计划（交给 PlanRecommender）；不写 SQL

**Input**
1. 用户逐项输入：性别、年龄、身高cm、当前体重kg、目标体重kg、每周运动天数、健身目的

**Output**
1. 校验通过后的 `UserProfile` 草稿 → 交由 ProfileRepository 落库

**Public interface**
1. `OnboardingScreen(onFinished: () -> Unit)`；`OnboardingViewModel.submit(profile): Unit`

**Hidden internals**
1. 分步进度逻辑、字段级错误文案、软键盘处理

**Dependencies**
2. InputValidator、ProfileRepository、FitnessCalculator（仅展示实时 BMI 预览）

**Extension points**
1. 未来新增字段（如腰围）只需扩展表单与实体列；未来"问卷驱动 AI 生成计划"在此出口接入

**Run/test focus**
1. 空值/超范围输入被拦截且有中文提示；提交后重启 App 不再出现问卷

---

### Module: FitnessCalculator（健身指标计算器，纯 Kotlin）
**Responsibility**
1. BMI 及分类、BMR（Mifflin-St Jeor）、TDEE（活动系数）、目标热量（按目的：减脂 -400 / 塑形 -200 / 健康 ±0 / 增肌 +300，下限保护：女 1200 / 男 1500 kcal）、三大营养素克数（蛋白质按体重系数，脂肪 25% 热量，碳水补足）
2. 打卡连击天数计算

**Non-responsibility**
1. 不做 IO、不依赖 Android 类、不持有状态

**Input**
1. `UserProfile` 字段值；打卡日期列表

**Output**
1. `FitnessMetrics(bmi, bmiCategory, bmr, tdee, targetCalories, proteinG, carbG, fatG)`；`Int` 连击天数

**Public interface**
1. `object FitnessCalculator { fun metrics(profile): FitnessMetrics; fun bmi(...); fun streak(dates: List<LocalDate>, today: LocalDate): Int }`

**Hidden internals**
1. 活动系数表、各目的热量调整与营养素比例常量（集中 `AppConfig`）

**Dependencies**
1. 无（纯函数，仅 Kotlin 标准库）

**Extension points**
1. 未来新增指标（体脂率估算、周热量差）只加函数；AI 教练可复用同一计算结果

**Run/test focus**
2. 单元测试：男女各一组成熟案例值断言；BMI 边界（18.5/24/28）；连击含今天/断签/空列表用例

---

### Module: PlanRecommender（计划推荐器）
**Responsibility**
1. 按 健身目的+每周运动天数 → 从内置计划中选出推荐计划并说明推荐理由（一句中文）

**Non-responsibility**
1. 不创建/修改计划数据；不判断用户身体风险（V1 明确不做医疗建议，界面展示免责一句话）

**Input**
1. `UserProfile.purpose`、`weeklyExerciseDays`、内置计划清单

**Output**
1. `PlanRecommendation(planId, reason: String)`

**Public interface**
1. `interface PlanRecommender { fun recommend(profile, plans): PlanRecommendation }` + 内置实现 `BuiltinRuleRecommender`

**Hidden internals**
1. 推荐规则表（如：减脂→燃脂入门；增肌→健身房力量；健康/塑形→居家活力；运动天数≥5 优先 gym 类）

**Dependencies**
1. domain/model 枚举

**Extension points**
1. **核心预留点**：未来 `AiPlanRecommender implements PlanRecommender`，用真实 LLM 生成个性化计划，UI 与 ViewModel 零改动

**Run/test focus**
1. 单测：每种目的映射到正确计划；理由文案非空且中文

---

### Module: Plan（训练计划模块）
**Responsibility**
1. 内置计划种子数据（3 套 × 每日动作明细）首次启动写入数据库；计划列表/详情/新建/切换 UI 与逻辑

**Non-responsibility**
1. 不做打卡记录写入；不含计算逻辑；不弹健康免责以外的新规则

**Input**
1. 用户的新建计划表单（名称、每周天数、每日动作：名称/组数/次数或时长/要点）；切换计划选择

**Output**
1. `WorkshopPlan`/`PlanDay`/`PlanExercise` 记录；当前激活计划 id 写入 DataStore

**Public interface**
1. `PlanListScreen / PlanDetailScreen / PlanEditScreen`；`PlanViewModel`；`PlanRepository.upsertCustomPlan(...)`、`setActivePlan(planId)`

**Hidden internals**
1. 种子数据结构（`BuiltinPlanSeed` Kotlin 对象）；Room 事务写入顺序

**Dependencies**
1. Room DAO、SettingsDataStore、PlanRecommender（列表页标记推荐）

**Extension points**
1. 未来计划模板市场、AI 生成计划、计划分享均为数据来源扩展，结构不变

**Run/test focus**
1. 首启种子仅写入一次（幂等）；新建自定义计划后立即可在列表与今日页生效；切换计划后今日页跟随

---

### Module: CheckIn（打卡与日记模块）
**Responsibility**
1. 今日训练展示（来自激活计划按周几/顺序推算当日安排）；一键打卡（当日幂等，重复点击不重复记录）；当日日记（心情 5 档 + ≤100 字，每日一条可修改）

**Non-responsibility**
1. 不计算统计（记录页职责）；不管理体重输入之外的任何数据

**Input**
1. 当前日期、激活计划、用户点击打卡/写日记/记体重

**Output**
1. `CheckInRecord`、`DiaryEntry`、`BodyMetric` 写入；今日页状态刷新

**Public interface**
1. `TodayScreen`；`TodayViewModel.checkInToday()` / `saveDiary(mood, text)` / `quickLogWeight(kg)`

**Hidden internals**
1. "今日该练哪天"的推算规则（按激活日逐日顺推，休息日显示休息卡）；打卡防重复

**Dependencies**
1. PlanRepository、CheckInRepository、DiaryRepository、BodyMetricRepository、InputValidator

**Extension points**
1. 未来提醒通知挂在此模块（打卡时间偏好）；未来训练计时器在此扩展

**Run/test focus**
1. 同一天重复打卡只产生一条；日记覆盖更新；休息日打卡按钮变为"今天休息"

---

### Module: Records（记录统计模块）
**Responsibility**
1. 月历打卡视图、连续/累计天数、历史列表、体重折线趋势图（自绘 Canvas）

**Non-responsibility**
1. 不写数据（只读聚合）；不做打卡操作

**Input**
1. `CheckInRecord` 列表、`BodyMetric` 列表（Flow 订阅）

**Output**
1. 渲染数据：日历标记、统计数字、折线点位

**Public interface**
1. `RecordsScreen`；`RecordsViewModel`（暴露 `UiState<RecordsUiModel>`）

**Hidden internals**
1. 折线坐标换算、按月分组、空数据降级展示

**Dependencies**
1. CheckInRepository、BodyMetricRepository、FitnessCalculator.streak

**Extension points**
1. 未来更多图表（周打卡柱状图）在此扩展；未来导出 CSV 直接消费本模块只读数据

**Run/test focus**
1. 无记录时空状态；有数据时日历与折线数值正确；体重只有 1 个点时不画线只画点

---

### Module: Diet（饮食建议模块）
**Responsibility**
1. 展示 FitnessCalculator 结果：TDEE、目标热量、蛋白质/碳水/脂肪卡片；按目的显示通用饮食建议文案组（每目的 3~4 条中文建议）；免责说明

**Non-responsibility**
1. 不记录饮食；不查食物热量（V1 边界）；不修改 Profile

**Input**
1. 当前 `UserProfile` → `FitnessCalculator.metrics()`

**Output**
1. 饮食页渲染数据

**Public interface**
1. `DietScreen`；`DietViewModel`

**Hidden internals**
1. 建议文案库（集中一处便于审校）

**Dependencies**
1. ProfileRepository、FitnessCalculator

**Extension points**
1. 未来接食物数据库/拍照记录饮食，在此页加 Tab，不动计算层

**Run/test focus**
1. 修改资料后饮食页数值联动更新；未完成问卷进入时引导回问卷

---

### Module: Profile（个人信息模块）
**Responsibility**
1. 个人信息卡展示、目标体重进度（当前/目标差值 + 进度条）、重新填写问卷入口、关于页（版本、作品集说明、免责）

**Non-responsibility**
1. 不直接改库（复用 ProfileRepository）；不做账号/头像上传

**Input**
1. Profile 读取/更新请求

**Output**
1. 更新后的 `UserProfile`；进度渲染数据

**Public interface**
1. `ProfileScreen`；`ProfileViewModel`

**Hidden internals**
1. 进度百分比口径（超重减脂：已减/需减；过瘦增重同理）

**Dependencies**
1. ProfileRepository、FitnessCalculator

**Extension points**
1. 未来账号/云同步：此处加登录入口，Repository 换实现

**Run/test focus**
1. 改体重后"我的"与"饮食/今日"联动；进度条方向正确（减脂/增重两种场景）

---

### Module: DataLayer（数据层：Room + DataStore + Repository）
**Responsibility**
1. Room 实体/DAO/Database；DataStore 偏好；5 个 Repository 接口与 Room 实现；类型映射与线程调度（Flow）

**Non-responsibility**
1. 不含业务规则与计算；不感知 UI

**Input**
1. 各 Repository 方法调用

**Output**
1. `Flow<List<T>>` / 挂起写入结果

**Public interface**
1. `ProfileRepository / PlanRepository / CheckInRepository / DiaryRepository / BodyMetricRepository`（接口）+ Room 实现

**Hidden internals**
1. 表结构、索引、SQL、迁移

**Dependencies**
1. Room、DataStore

**Extension points**
1. 未来云同步：新增 `RemoteRepository` 实现或装饰器；未来导出：DAO 增只读查询

**Run/test focus**
2. DAO 单测（in-memory database）：插入/查询/幂等；DataStore 读写

---

### Module: ValidationService（校验模块，纯 Kotlin）
**Responsibility**
1. 全部输入校验规则与中文错误文案：年龄 10~80、身高 100~250、体重 30~300、目标体重合理性（与当前差 ≤ 50kg）、日记 ≤100 字、计划名非空 ≤20 字、动作组数 1~10

**Non-responsibility**
1. 不弹对话框、不持有状态（纯函数）

**Input**
1. 各表单原始字符串/数值

**Output**
1. `ValidationResult`（通过 / 错误码 + 中文文案）

**Public interface**
1. `object InputValidator { fun validateProfile(...); fun validateWeight(...); fun validatePlanForm(...) }`

**Hidden internals**
1. 阈值常量（与 AppConfig 共享）

**Dependencies**
1. 无

**Extension points**
1. 新表单只加校验函数；文案统一可审

**Run/test focus**
1. 单测覆盖每个规则的合法/非法边界值

---

### Module: ErrorPresenter（错误与状态模块）
**Responsibility**
1. `UiState<T>` 密封类定义；统一空状态/加载/错误组件；异常 → 中文用户文案映射（数据库错误、未知错误）

**Non-responsibility**
1. 不决定重试后的业务动作（回调交还 ViewModel）

**Input**
1. ViewModel 抛出的状态/异常

**Output**
1. 全屏/局部状态组件展示

**Public interface**
1. `sealed class UiState<out T>`；`EmptyState / LoadingBox / ErrorBox` 通用组件

**Hidden internals**
1. 异常映射表

**Dependencies**
1. 无（ui/components 层）

**Extension points**
1. 未来埋点/日志上报在此挂载

**Run/test focus**
1. 断网/异常场景人工走查不出现英文堆栈给用户

---

### Module: ExtensionPointRegistry（扩展接口预留层，仅声明）
**Responsibility**
1. 集中声明 V1 不实现但保留位置的接口与注释：`AiCoachService`、`DataExporter`、`CloudSync`（说明见 §10）

**Non-responsibility**
1. 不写任何假实现/桩数据

**Input / Output**
1. 无运行时行为（仅接口与 KDoc）

**Public interface**
1. 上述接口签名（KDoc 注明"V1 未实现，接入需真实服务"）

**Hidden internals**
1. 无

**Dependencies**
1. domain/model

**Extension points**
1. 本模块即扩展点本身

**Run/test focus**
1. 编译通过即可（不被 V1 代码调用）

## 9. 数据结构设计

### UserProfile（表 `user_profile`，单行）
Fields:
1. `id: Long = 1`：固定单行主键
2. `gender: String`：MALE / FEMALE
3. `age: Int`；`heightCm: Int`；`currentWeightKg: Double`；`targetWeightKg: Double`
4. `weeklyExerciseDays: Int`（0~7）
5. `purpose: String`：FAT_LOSS / MUSCLE_GAIN / TONE / HEALTH
6. `createdAt / updatedAt: Long`

Used by: Onboarding、FitnessCalculator、Diet、Profile、PlanRecommender

### WorkoutPlan（表 `workout_plan`）
Fields:
1. `id: Long`；`name: String`（≤20 字）；`description: String`
2. `source: String`：BUILTIN / CUSTOM
3. `purposeTag: String`（同 purpose 枚举，推荐用）
4. `daysPerWeek: Int`；`isActive: Boolean`（全局唯一激活）

Used by: Plan、CheckIn（今日推算）、PlanRecommender

### PlanDay（表 `plan_day`）
Fields:
1. `id: Long`；`planId: Long`（外键）；`orderIndex: Int`；`title: String`（如"上肢力量"）；`isRestDay: Boolean`

Used by: Plan 详情、CheckIn 今日推算

### PlanExercise（表 `plan_exercise`）
Fields:
1. `id: Long`；`planDayId: Long`（外键）；`name: String`；`sets: Int`；`repsOrDuration: String`（"12次"/"30秒"）；`tip: String`（要点一句）

Used by: Plan 详情、CheckIn 今日

### CheckInRecord（表 `check_in_record`）
Fields:
1. `id: Long`；`date: String`（ISO yyyy-MM-dd，建唯一索引）；`planId: Long`；`note: String?`；`createdAt: Long`

Used by: CheckIn、Records（日历/统计/连击）

### BodyMetric（表 `body_metric`）
Fields:
1. `id: Long`；`date: String`（唯一索引，同日覆盖更新）；`weightKg: Double`；`createdAt/updatedAt: Long`
2. 预留列注释：`waistCm: Double?`（V2 启用）

Used by: CheckIn（速记）、Records（趋势）、Profile（进度）

### DiaryEntry（表 `diary_entry`）
Fields:
1. `id: Long`；`date: String`（唯一索引，每日一条）；`mood: Int`（1~5 对应 😞→😄）；`content: String`（≤100 字）；`updatedAt: Long`

Used by: CheckIn（今日页卡片）、Records（历史可选展示）

### 值对象（domain/model，非表）
1. `FitnessMetrics`（§8）；`PlanRecommendation`；`UiState<T>`；枚举 `Gender / Purpose / PlanSource`

## 10. 接口与扩展点设计

### 第一版实现
1. `PlanRecommender`（规则版 `BuiltinRuleRecommender`）
2. 5 个 Repository 接口 + Room 实现
3. `FitnessCalculator / InputValidator / DietAdvisor` 纯函数

### 后续预留（V1 只声明接口与 KDoc，不写实现）
1. `interface AiCoachService { suspend fun chat(history): AiReply; suspend fun generatePlan(profile): WorkoutPlan }` —— 接入时必须配置真实 LLM API Key（推荐智谱 GLM），调用真实服务，**禁止任何 mock/假数据实现**
2. `interface DataExporter { suspend fun exportCheckIns(): Uri; suspend fun exportBodyMetrics(): Uri }`（CSV， SAF 导出）
3. `interface CloudSync { suspend fun push(); suspend fun pull() }`
4. `AiPlanRecommender : PlanRecommender`（替换规则推荐器）

### 预留但不实现的原因
1. 用户确认 V1 无 AI/云同步/导出需求；空接口不增加运行时复杂度，却锁定未来替换成本
2. skill 红线：不允许"先放假数据占位"——V1 界面绝不出现假分析/假记录

## 11. 后续功能变更影响说明

### 如果未来增加 AI 教练
需要改：
1. `data/remote/`（新增 OkHttp/Retrofit 客户端 + API Key 读取）
2. `domain/ai/`（新增 `AiCoachService` 实现）、设置页加 Key 配置入口、今日/计划页加对话入口

不应该改：
1. 各 Screen 布局主体、Repository/DAO、`FitnessCalculator`、`InputValidator`

需要新增的数据结构：
1. `AiChatMessage`、`AiCoachSession`（本地会话缓存表）

当前预留接口：
1. `AiCoachService`、`PlanRecommender`（AI 推荐器直接实现）

### 如果未来增加登录与云同步
需要改：
1. `data/`（新增 `AuthRepository`、`CloudSync` 实现）、DataStore 增用户标识

不应该改：
1. 全部 ViewModel 业务逻辑与 UI（它们只依赖 Repository 接口）

当前预留接口：
1. `CloudSync`；Repository 接口签名不变，仅换实现

### 如果未来增加 CSV 导出
需要改：
1. `domain/export/`（`DataExporter` 实现）、我的页加导出按钮

不应该改：
1. 记录页、数据库结构

当前预留接口：
1. `DataExporter`

### 如果未来增加提醒通知
需要改：
1. 新增 `notification/` 模块（WorkManager）、今日页加"提醒时间"设置项

不应该改：
1. 打卡逻辑、统计口径

### 如果未来增加腰围/体脂
需要改：
1. `BodyMetric` 实体加可空列 + Room 迁移、记录页图表加数据源开关

不应该改：
1. 体重折线组件（参数化扩展）、`FitnessCalculator` 既有函数

## 12. 目录结构

```text
YueDong/
├── app/src/main/java/com/yuedong/fitness/
│   ├── MainActivity.kt
│   ├── YueDongApplication.kt          # 手动依赖容器（轻量）
│   ├── navigation/
│   │   ├── YueDongNavHost.kt
│   │   └── BottomNavBar.kt
│   ├── ui/
│   │   ├── theme/                     # Color.kt(淡蓝) / Type.kt / Theme.kt
│   │   ├── components/                # EmptyState / LoadingBox / ErrorBox / StatCard / PrimaryButton / LineChart
│   │   ├── onboarding/                # 欢迎页 + 分步问卷
│   │   ├── today/                     # 今日：训练卡/打卡/体重速记/日记
│   │   ├── plan/                      # 列表/详情/新建
│   │   ├── diet/                      # 饮食建议
│   │   ├── records/                   # 日历/统计/历史/折线
│   │   └── profile/                   # 我的/关于
│   ├── domain/
│   │   ├── model/                     # 枚举与值对象(FitnessMetrics等)
│   │   ├── logic/                     # FitnessCalculator / PlanRecommender / DietAdvisor / BuiltinPlanSeed
│   │   ├── validation/                # InputValidator
│   │   └── extension/                 # AiCoachService / DataExporter / CloudSync（仅接口）
│   ├── data/
│   │   ├── db/                        # AppDatabase + dao/ + entity/
│   │   ├── repository/                # 接口 + Room 实现
│   │   └── settings/                  # SettingsDataStore
│   └── util/                          # DateUtils / UiState / 中文错误文案映射
├── app/src/test/java/com/yuedong/fitness/
│   ├── FitnessCalculatorTest.kt
│   ├── InputValidatorTest.kt
│   ├── PlanRecommenderTest.kt
│   └── dao/                           # Room in-memory DAO 测试
└── docs/                              # 需求确认稿 / 设计包 / 阶段提示词（随作品集开源展示）
```

## 13. 分阶段开发顺序

> 详细可复制提示词见《分阶段编码提示词-悦动APP.md》。每阶段结束必须运行自检并在报告中给出 Ready / Not ready。

### Phase 0: 环境搭建与项目骨架
目标：装好 Android Studio；创建空项目；淡蓝主题 + 底部 5 Tab 静态壳跑在真机上
修改文件：Gradle 配置、theme、MainActivity、BottomNavBar、各 Tab 占位页
不要做：任何业务逻辑与数据库
验收标准：真机安装成功，5 Tab 可切换，中文占位文案 + 淡蓝配色
试运行：`gradlew.bat assembleDebug` + Android Studio Run 到真机

### Phase 1: 数据层
目标：Room 实体/DAO/库 + DataStore + 5 个 Repository 接口与实现 + DAO 单测
修改文件：data/db、data/repository、data/settings、test/dao
不要做：任何 UI
验收标准：单测全绿；`assembleDebug` 通过
试运行：`gradlew.bat testDebugUnitTest` + `gradlew.bat assembleDebug`

### Phase 2: 问卷与计算
目标：欢迎页 + 分步问卷 + 校验 + 保存 + FitnessCalculator（含单测）+ 启动去向控制
验收标准：非法输入被中文提示拦截；提交后重启直达主界面；计算单测全绿
试运行：`gradlew.bat testDebugUnitTest` + 真机走查问卷全流程

### Phase 3: 计划模块
目标：种子计划幂等写入；计划列表/详情/新建/切换；推荐高亮
验收标准：三种目的推荐正确；新建自定义计划当日生效
试运行：`gradlew.bat testDebugUnitTest`（含 PlanRecommenderTest）+ 真机走查

### Phase 4: 今日页（打卡/日记/体重速记）
目标：今日安排推算、幂等打卡、日记每日一条、体重速记
验收标准：重复打卡不重复记录；休息日显示休息卡；数据即时刷新
试运行：单测（连击计算）+ 真机走查

### Phase 5: 饮食页
目标：热量/营养素卡片 + 分目的建议 + 免责文案
验收标准：改资料后数值联动；无资料引导回问卷
试运行：真机走查

### Phase 6: 记录页
目标：月历打卡视图、连击/累计统计、历史列表、体重折线（自绘）
验收标准：空/1个点/多点三种形态正确
试运行：真机走查 + 既有单测回归

### Phase 7: 我的页与作品集打磨
目标：资料查看/修改、目标进度、关于页、空状态统一、应用图标与命名、README
验收标准：全流程从问卷到趋势图无崩溃、无英文占位；README 可作为面试材料
试运行：`gradlew.bat assembleDebug testDebugUnitTest lint` + 全流程真机走查

## 14. 给 LLM 编程工具的提示词

见《分阶段编码提示词-悦动APP.md》：含 1 个主提示词 + 8 个阶段提示词 + 1 个最终自检提示词，每阶段内置运行自检报告模板（执行命令 / 通过与否 / 错误 / 修复 / 是否可进入下一阶段）。

## 15. 架构与代码自检清单

1. ✅ 高内聚：每个模块只答一个问题（计算归 Calculator、存储归 Data、校验归 Validator）
2. ✅ 低耦合：ViewModel 只依赖 Repository 接口；UI 不接触 Room；纯逻辑无 Android 依赖
3. ✅ 每模块有明确输入/输出（§8 契约）
4. ✅ 无"一个文件做太多事"：Screen 不写 SQL、不写计算
5. ✅ 无 Prompt 拼接进页面问题（V1 无 AI；未来 AI 走 `AiCoachService`）
6. ✅ 无 API Key 前端暴露问题（V1 无 Key；未来 Key 走本地安全存储 + 不入库）
7. ✅ 依赖最少化：仅官方栈 + icons-extended，图表自绘
8. ✅ 无 Mock 假结果：种子计划是真实产品内容（允许）；绝不伪造打卡/体重/AI 结果
9. ✅ 扩展点合理：4 个预留接口各对应明确变更场景（§11）
10. ✅ 无过度设计：无 UseCase 层、无 DI 框架全家桶、无多模块 Gradle 拆分
11. ✅ 每阶段可试运行：Gradle 命令 + 真机走查（§13）
12. ✅ 可直接交给 LLM 分阶段开发（见阶段提示词文件）
