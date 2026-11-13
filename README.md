# 悦动 · YueDong

一款**纯本地、无广告、无账号**的中文健身记录 Android 应用。跟随训练计划每日打卡，记录体重与心情，自动计算热量与营养建议，用月历和折线图看见自己的坚持。

> 定位：个人求职作品集项目 —— 由 AI 编程工具（ZCode）在全流程引导下分阶段开发，从需求确认到架构设计再到编码落地，全程文档可溯（见 [docs/](docs/)）。

## ✨ 功能一览

| 模块 | 功能 |
|---|---|
| 首次问卷 | 一屏一问的分步问卷：性别、年龄、身高、体重、目标体重、每周运动量、健身目的；年龄/身高滚轮、体重横向标尺、实时 BMI 分类卡、完成彩纸动效 |
| 训练计划 | 3 套内置计划（居家燃脂 / 健身房力量 / 居家健康）按目的智能推荐；自定义计划编辑器（7 天循环、110 个动作库带视频示范、就地校验）；支持编辑已建计划 |
| 今日训练 | 按激活计划推算"今天该练什么"，一键打卡（幂等）、体重速记（标尺弹窗）、每日心情日记 |
| 饮食 | 热量目标（BMR/TDEE/缺口）、三大营养素推荐、三餐分配参考、进度反馈（预计每周减重）；50+ 食物库按克记录、自定义食物、摄入 vs 目标进度条、按真实缺口动态生成饮食建议 |
| 记录 | 本周概况（打卡/饮食达标/体重变化）、打卡月历（饮食达标😄 / 超标😟）、体重趋势折线（点击/滑动查看节点数值）、打卡历史（含心情）、**照片墙**（按月归档训练留影）、一键生成演示数据 |
| 当日详情 | 补录工作台：当天补打卡/补饮食/补体重与备注，跨零点数据不再遗漏 |
| 提醒 | 多类型自定义提醒（训练/称重/喝水等），WorkManager 后台调度 |
| 备忘录 | 训练笔记与备忘，支持回收站软删除（误删可恢复） |
| 备份恢复 | 全量数据导出/导入 zip（数据库+媒体文件），换机与卸载重装不丢数据 |
| 我的 | 个人资料卡、BMI 分类卡、目标进度条、修改资料（复用问卷）、数据与备份、关于页（检查更新） |

## 🛠 技术栈

- **语言**：Kotlin 2.x
- **UI**：Jetpack Compose + Material 3（品牌淡蓝主题，关闭动态取色）
- **架构**：MVVM + Repository，模块化单体（`ui / domain / data` 三层分包）
- **存储**：Room（含版本 1→2 迁移实践）+ DataStore Preferences
- **导航**：Navigation Compose（悬浮层导航 + 遮罩式新手引导）
- **图表**：折线图与月历均为纯 Canvas 自绘（零第三方图表依赖）
- **视频**：Media3 ExoPlayer 播放 110 个动作示范视频（res/raw 内置，离线可用）
- **后台**：WorkManager（提醒调度）、应用内检查更新（HttpURLConnection 零第三方网络库）
- **测试**：JUnit4 单元测试（计算器/校验器/推荐器/文案库）+ Room DAO 真机测试（connectedDebugAndroidTest）
- **构建**：Gradle Kotlin DSL + version catalog + KSP；release 签名经 gitignore 的 keystore.properties 注入

## 🏛 架构与模块职责

```text
ui/        界面层：各功能 Screen + ViewModel（状态编排，不含业务规则）
domain/    纯逻辑层：FitnessCalculator(指标计算) / DietAdvisor(建议文案)
           PlanRecommender(计划推荐) / InputValidator(校验) / BuiltinPlanSeed(内置计划)
           CommonExercises / FoodCatalog(动作与食物库) / extension(预留接口)
data/      数据层：Room 实体与 DAO / 6 个 Repository 接口+实现 / DataStore / 演示数据生成器
util/      UiState 密封类等通用件
navigation/ AppShell：底部导航 + 路由（AppShell 模块）
```

核心原则：**责任完整，不过度复杂**。每个模块只回答一个问题：Screen 不写 SQL、ViewModel 只依赖 Repository 接口、纯逻辑零 Android 依赖（因此可被 46 个单元测试覆盖）。

### 预留扩展点（接口已声明、未实现）

| 接口 | 场景 |
|---|---|
| `PlanRecommender` | 已有规则实现；未来 `AiPlanRecommender` 接入真实 LLM 生成计划，UI 零改动 |
| `AiCoachService` | AI 健身教练问答（接入须配置真实 API Key，禁止 mock 数据） |
| `DataExporter` | 打卡/体重/饮食 CSV 导出（SAF） |
| `CloudSync` | 账号与云同步（offline-first，本地为权威副本） |

## 🧪 质量保障

- 46 个单元测试：BMI/BMR/TDEE/营养素公式、热量下限保护、打卡连击算法、输入校验边界、计划推荐规则、饮食建议文案库
- Room 数据库测试跑在真机上（插入/查询/同日幂等/级联删除/事务写入）
- 数据库版本 1→2 迁移在真机验证，老数据零丢失
- 每阶段开发均执行 `assembleDebug` + `testDebugUnitTest` + `lint`（记录见 docs/分阶段编码提示词）

## 🚀 运行方式

1. Android Studio（稳定版）打开本项目
2. 等待 Gradle Sync 完成
3. 连接 Android 8.0+ 真机（开启 USB 调试），点击 Run ▶

> 首次启动完成健康问卷后，可在「记录」页底部点击"生成演示数据"快速查看图表效果。

## 🔒 数据与隐私

- 所有数据仅保存在手机本地 Room 数据库，**无账号、无上传、无任何业务联网**；唯一的网络请求是「检查更新」拉取 version.json 与下载安装包
- 卸载即彻底删除全部数据（重要数据可先用「我的 → 数据与备份」导出 zip）
- 计划与热量建议为一般性参考，不构成医疗意见

## 📁 设计文档

完整的需求确认稿、LLM 开发设计包（模块契约/数据结构/变更影响分析）与分阶段编码提示词见 [docs/](docs/) 目录——展示"AI 辅助开发"时人与 AI 如何协作完成一个规范工程的全过程。
